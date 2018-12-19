package com.morbank.services.model.spark;

import java.io.InputStream;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.jackson.JsonComponent;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.morbank.services.connector.SparkConnector;

/*
 * СПАРК
 * Service: Time
 * Возвращает время, хранимое на сервере (тест для проверки работоспособности)
 * 
 */

@JsonComponent
@JsonTypeName(value = "TimeResponse")
@XmlRootElement(name = "TimeResponse")
public class Alive {
	private static final Logger log = LoggerFactory.getLogger(Alive.class);
	private String timeResult;
	
	public Alive() {
		try {
			SparkConnector connector = new SparkConnector();
			connector.setBody(connector.bodyTemplate("Time"));
			InputStream response = connector.runService();
			parseTimeResponse(response);
			response.close();
		}
		catch (Exception e) {
			log.error("An error occurred while accessing the remote service",e);
			setErrorResponse();
		}
	}

	private void setErrorResponse() {
		setTimeResult("Error");
	}

	@JsonGetter("TimeResult")
	@XmlElement(required = true, type = String.class, name = "TimeResult")
	public String getTimeResult() {
		return this.timeResult;
	}

	public void setTimeResult(String timeResult) {
		this.timeResult = timeResult;
	}

	public void parseTimeResponse(InputStream response) {
		
		// и ещё выделить нужный тэг <TimeResult>string</TimeResult>
		try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
 
            DefaultHandler handler = new DefaultHandler() {
                boolean name = false;
                String parsedResponseBuffer = null;
                
                // Start tag
                @Override
                public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
                    // Если тэг имеет имя TimeResult, то мы этот момент отмечаем - начался тэг 
                    if (qName.equalsIgnoreCase("TimeResult")) {
                        name = true;
                    }
                }
 
                // Text inside tag
                @Override
                public void characters(char ch[], int start, int length) throws SAXException {
                    // Если перед этим мы отметили, что имя тэга TimeResult - значит нам надо текст использовать
                    if (name) {
                    	parsedResponseBuffer = new String(ch, start, length);      
                    	setTimeResult(parsedResponseBuffer);
                        name = false;
                    }
                }
            };
 
            // Стартуем разбор методом parse, которому передаем наследника от DefaultHandler, который будет вызываться в нужные моменты
            saxParser.parse(response, handler);
 
        } catch (Exception e) {
            log.error("Exception due parse response: {}", e.toString());
        } finally {
        	if (this.timeResult == null) {
        		this.timeResult = "";
        	}
        }
		log.info("timeGetData. response = {}", response.toString());
		log.info("timeGetData. response.parsed = {}", this.timeResult);
		log.info("timeGetData finished");
	}
}
