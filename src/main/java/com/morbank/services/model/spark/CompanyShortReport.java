package com.morbank.services.model.spark;

import java.io.InputStream;
import java.io.StringReader;


/*
 * СПАРК
 * Service: GetCompanyShortReport
 * Метод возвращает краткую справку по компании. 
 * Должен быть задан хотя бы один из параметров: ИНН, ОГРН, либо оба. Поиск происходит по логическому «И» без учета филиалов.
 * 
 */

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.jackson.JsonComponent;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.morbank.services.ServiceConfig;
import com.morbank.services.connector.SparkConnector;

/*
 * Структура класса по данным соответствует схеме данных сервиса.
 * В простейших случаях структура создается руками. Иногда можне прибегать к автоматической генерации по xsl схемам.
 */
@JsonComponent
@JsonPropertyOrder({ "GetCompanyShortReportResult", "xmlData" })
@JsonTypeName(value = "GetCompanyShortReportResponse")
@XmlRootElement(name = "GetCompanyShortReportResponse")
public class CompanyShortReport {
	private static final Logger log = LoggerFactory.getLogger(CompanyShortReport.class);
	private ServiceConfig cfg;
	private StringBuilder pureXmlDataBuf;
	private String pureXmlData;

	public	static class XmlData {
		public static class Response {
			public static class Data {
				public static class Report {
					public static String sparkId;
					public static String inn;
					public static String kpp;
					public static String ogrn;
					public static String okpo;
					public static String fullNameRus;
					public static String shortNameRus;
					public static String shortNameEn;
					
					@JsonGetter("SparkID")
					@XmlElement(required = true, type = String.class, name = "SparkID")
					public String getSparkId() { return sparkId; }

					@JsonGetter("INN")
					@XmlElement(required = true, type = String.class, name = "INN")
					public String getInn() { return inn; }

					@JsonGetter("KPP")
					@XmlElement(required = true, type = String.class, name = "KPP")
					public String getKpp() { return kpp; }

					@JsonGetter("OGRN")
					@XmlElement(required = true, type = String.class, name = "OGRN")
					public String getOgrn() { return ogrn; }
										
					@JsonGetter("OKPO")
					@XmlElement(required = true, type = String.class, name = "OKPO")
					public String getOkpo() { return okpo; }
										
					@JsonGetter("FullNameRus")
					@XmlElement(required = true, type = String.class, name = "FullNameRus")
					public String getFullNameRus() { return fullNameRus; }
					
					@JsonGetter("ShortNameRus")
					@XmlElement(required = true, type = String.class, name = "ShortNameRus")
					public String getShortNameRus() { return shortNameRus; }
					
					@JsonGetter("ShortNameEn")
					@XmlElement(required = true, type = String.class, name = "ShortNameEn")
					public String getShortNameEn() { return shortNameEn; }
					
				}
				public Report report = new Report();
			}
			public Data data = new Data();
		}
		public Response response = new Response();
	}

	private String result;
	public XmlData xmlData = new XmlData();

	@JsonGetter("Result")
	@XmlElement(required = true, type = String.class, name = "Result")
	public String getCompanyShortReportResult() {
		return result;
	}

	public CompanyShortReport() {}

	public CompanyShortReport(String sparkid, String inn, String ogrn) {
		if (cfg == null) cfg = ServiceConfig.INSTANCE;
		SparkConnector connector = null;
		
		try {
			connector = new SparkConnector();
			connector.setBody(String.format(connector.bodyTemplate("Authmethod"), cfg.getSparkLogin(), cfg.getSparkPassword()));
			InputStream response = connector.runService();
			connector.close();
			log.info("Service Authmethod finished. connector = {}", connector.toString());

			connector.setBody(String.format(connector.bodyTemplate("GetCompanyShortReport"),sparkid,inn,ogrn));
			response = connector.runService();
			parseGetCompanyShortReportResponse(response);
			connector.close();
			log.info("Service GetCompanyShortReport finished. connector = {}", connector.toString());

			// this step exception must not depends to all-service result 
			try {
				connector.setBody(connector.bodyTemplate("End"));
				response = connector.runService();
				connector.close();
				log.info("Service End finished. connector = {}", connector.toString());
			} catch (Exception e) {
				log.info("Error due request End service. {}", e.getMessage()); // do nothing
			} 
			setNormalResponse();
		}
		catch (Exception e) {
			setErrorResponse();
		}
		finally {
			connector.repair();
		}
	}

	private void setNormalResponse() {
		result = "True";
	}

	private void setErrorResponse() {
		result = "False";
	}

	public void parseGetCompanyShortReportResponse(InputStream response) {
		log.info("Response to parse: {}", response.toString());

		pureXmlDataBuf = new StringBuilder();
		
		/* 
		 * Двойное сканирование. поскольку результат сохраняется в теге <xmlData>
		 * внутри <![CDATA[...]]>
		 *  
		 */
		
		// Проходим по "наружному" xml, по нужным тегам (xmlData), сохраняем результат в pureXmlData
		try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
 
            DefaultHandler handler = new DefaultHandler() {
                String name = null;
                
				// Start tag
                @Override
                public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
                    // Если тэг имеет имя TimeResult, то мы этот момент отмечаем - начался тэг 
                    if (qName.equalsIgnoreCase("xmlData")) { name = "xmlData"; }
                }
 
                @Override
				public void endElement(String uri, String localName, String qName) throws SAXException {
                    if (qName.equalsIgnoreCase("xmlData")) { name = null; }
				}

				// Text inside tag
                @Override
                public void characters(char ch[], int start, int length) throws SAXException {
                	if (name != null) {
	                	String buf = new String(ch, start, length);
	                    // Если перед этим мы отметили, что имя тэга TimeResult - значит нам надо текст использовать
	                	switch (name) {
	            		case "xmlData":
		                	log.info("Parsed string fragment: {} {}", name, buf);
	            			pureXmlDataBuf.append(buf);
	            			break;
	            		default: 
	            			break;
	                	}
                	}
                }
            };
 
            // Стартуем разбор методом parse, которому передаем наследника от DefaultHandler, который будет вызываться в нужные моменты
            saxParser.parse(response, handler);

        	// Test only, because SPARK test period expired
            //saxParser.parse(pureXmlDataSample, handler); 
 
        } catch (Exception e) {
        	log.error("Exception due parse response: {} {}", e.toString(), e.getMessage());
        }
		
		pureXmlData = pureXmlDataBuf.toString();
		if (pureXmlData == null) pureXmlData = ""; 
		
		log.info("Parser: pureXmlData = {}", pureXmlData);
		
		// Второй проход, по содержимому результата
		try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
 
            DefaultHandler innerHandler = new DefaultHandler() {
                String name = null;
                
                // Start tag
                @Override
                public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
                    // Если тэг имеет имя TimeResult, то мы этот момент отмечаем - начался тэг 
                    if (qName.equalsIgnoreCase("sparkid")) { name = "sparkId"; }
                    if (qName.equalsIgnoreCase("inn")) { name = "inn"; }
                    if (qName.equalsIgnoreCase("kpp")) { name = "kpp"; }
                    if (qName.equalsIgnoreCase("ogrn")) { name = "ogrn"; }
                    if (qName.equalsIgnoreCase("okpo")) { name = "okpo"; }
                    if (qName.equalsIgnoreCase("fullNameRus")) { name = "fullNameRus"; }
                    if (qName.equalsIgnoreCase("shortNameRus")) { name = "shortNameRus"; }
					if (qName.equalsIgnoreCase("shortNameEn")) { name = "shortNameEn"; }
                }
 
                // Text inside tag
                @Override
                public void characters(char ch[], int start, int length) throws SAXException {
                	if (name != null) {
	                	String buf = new String(ch, start, length);
	                	log.info("Parsed string fragment: {} {}", name, buf);
	                    // Если перед этим мы отметили, что имя тэга TimeResult - значит нам надо текст использовать
	                	switch (name) {
	            		case "sparkId":
	            			CompanyShortReport.XmlData.Response.Data.Report.sparkId = buf;
	            			name = null;
	            			break;
	            		case "inn":
	            			CompanyShortReport.XmlData.Response.Data.Report.inn = buf;
	            			name = null;
	            			break;
	            		case "kpp":
	            			CompanyShortReport.XmlData.Response.Data.Report.kpp = buf;
	            			name = null;
	            			break;
	            		case "ogrn":
	            			CompanyShortReport.XmlData.Response.Data.Report.ogrn = buf;
	            			name = null;
	            			break;
	            		case "okpo":
	            			CompanyShortReport.XmlData.Response.Data.Report.okpo = buf;
	            			name = null;
	            			break;
	            		case "fullNameRus":
	            			CompanyShortReport.XmlData.Response.Data.Report.fullNameRus = buf;
	            			name = null;
	            			break;
	            		case "shortNameRus":
	            			CompanyShortReport.XmlData.Response.Data.Report.shortNameRus = buf;
	            			name = null;
	            			break;
	            		case "shortNameEn":
	            			CompanyShortReport.XmlData.Response.Data.Report.shortNameEn = buf;
	            			name = null;
	            			break;
	            		default: 
	            			break;
	                	}
                	}
                }
            };
 
            // Стартуем разбор методом parse, которому передаем наследника от DefaultHandler, который будет вызываться в нужные моменты
            saxParser.parse(new InputSource( new StringReader(pureXmlData)), innerHandler);
 
        } catch (Exception e) {
        	log.error("Exception due parse inner content of response: {} {}", e.toString(), e.getMessage());
        } 	
		log.info("parseGetCompanyShortReportResponse finished");
	}

}
