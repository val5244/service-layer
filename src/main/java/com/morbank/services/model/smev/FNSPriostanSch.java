package com.morbank.services.model.smev;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import org.springframework.boot.jackson.JsonComponent;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.morbank.services.connector.SmevConnector;
import com.morbank.services.ServiceConfig;
import bankostsch.fns.artefacts.x.root._171_08._4_0.FNSPriostanSchRequest;
import bankostsch.fns.artefacts.x.root._171_08._4_0.FNSPriostanSchResponse;
import bankostsch.fns.artefacts.x.root._171_08._4_0.ObjectFactory;

/*
 * 
 * Сведения о наличии действующих решений о приостановлении операций по счетам налогоплательщика
 * VS00626v002-FNS001
 * 
 * Namespace URI	urn://x-artefacts-fns-bankostsch/root/171-08/4.0.1
 * Версия			4.0.1
 * Версия МР		3.0
 * 
 * Дата регистрации	08-11-2017
 *
 * Структура класса по данным соответствует схеме данных сервиса.
 * В простейших случаях структура создается руками. Но при наличии схем xsl, можно прибегать к автоматической 
 * генерации по схемам с помощью утилиты xjc.
 * Файл для запуска размещается в src/main/resources/schema,
 * там же в подкаталогах - схемы, по которым и генериуются классы.
 * 
 * Для целей наложения/снятия подписи, возможно, что нужно создавать классы, производные от сгенерённых.
 *  
 */

@JsonComponent
@JsonTypeName(value = "FNSPriostanSch")
@JsonPropertyOrder({ "Result", "Id", "Name" })
@XmlRootElement(name = "FNSPriostanSch")
public class FNSPriostanSch {
	private static final Logger log = LoggerFactory.getLogger(FNSPriostanSch.class);
	private ServiceConfig cfg;

	private String result;
	private String id;
	private String bik;
	private String innFl;
	private String innUl;
	private String kio;
	private String inList;
	
	@JsonGetter("Result")
	@XmlElement(required = true, type = String.class, name = "Result")
	public String getResult() {
		return result;
	}
	
	@JsonGetter("Id")
	@XmlElement(required = false, type = Long.class, name = "Id")
	public String getId() {
		return id;
	}
	
	@JsonGetter("Bik")
	@XmlElement(required = true, type = String.class, name = "Bik")
	public String getBik() {
		return bik;
	}
	
	@JsonGetter("InnFL")
	@XmlElement(required = true, type = String.class, name = "InnFL")
	public String getInnFl() {
		return innFl;
	}
	
	@JsonGetter("InnUL")
	@XmlElement(required = true, type = String.class, name = "InnUL")
	public String getInnUl() {
		return innUl;
	}
	
	@JsonGetter("KIO")
	@XmlElement(required = true, type = String.class, name = "KIO")
	public String getKio() {
		return kio;
	}
	
	@JsonGetter("InList")
	@XmlElement(required = true, type = String.class, name = "InList")
	public String getInList() {
		return inList;
	}
	public void setInList(String inList) {
		this.inList = inList;
	}

	public FNSPriostanSch() {
	}

	public FNSPriostanSch(String smevid, String bik, String innfl, String innul, String kio) {
		if (cfg == null) cfg = ServiceConfig.INSTANCE;
		SmevConnector connector = null;		
		
		/*
		 * Create Request class (FNSPriostanSchRequest) and fill it.  
		 * Create by ObjectFactory call
		 */
		
		ObjectFactory factory = new ObjectFactory();
		/*
		 * Создание Запроса и Ответа (request и response) - через фабрику (для инверсия управления).
		 */
		FNSPriostanSchRequest request = factory.createFNSPriostanSchRequest();

		@SuppressWarnings("unused") // пока не будет дописана обработка response
		FNSPriostanSchResponse response= factory.createFNSPriostanSchResponse();
		request.setИдЗапрос(smevid);
		request.setБИК(bik);
		request.setИННФЛ(innfl);
		request.setИННЮЛ(innul);
		request.setКИО(kio);
		
		try {
			/*
			 * 1. Создание connector
			 * Коннектор создается один (пока это так, вероятно и дальше будет возможно) на всех 
			 * поставщиков сведений, которые лишь отличаются типом [классами Request и Response].
			 * Специфика каждого класса (Request/Response) остаётся ВНЕ класса SmevConnector.
			 *   
			 */
			connector = new SmevConnector();
			/*
			 * os - это выходной поток для записи данных перед вызовом сервиса, 
			 * в основном это body, properties, headers - уже записаны при вызове connector.prepareService()
			 * 
			 * Запись body - напрямую через маршаллинг в поток объекта класса [FNSPriostanSchRequest]
			 * Но!!! Для СМЭВ-3 сначала нужно наложить подпись и добавить неоторые теги с сигнатурами ключей,
			 * чтобы СМЭВ на том конце понимал, кому он данные передавать будет (нас он узнает по нашему ключу)
			 * Скорее всего, можно построить производный класс, дополнив его нужными тегами.
			 *  
			 */
			OutputStream os = connector.prepareService();

			/*
			 * Здесь в поток os выдаём [подписанный] запрос (Request), через маршаллинг
			 */
			writeBody(os,request);
			
			/*
			 * Соединяемся с сервисом и получаем поток с ответом
			 */
			InputStream is = connector.runService();
			
			/*
			 * Здесь, возможно, надо будет снимать подпись. 
			 * Либо ниже, после парсинга. И, возможно, производного класса.
			 * 
			 * Парсинг потока ответа заключается в анмаршаллинге потока в класс FNSPriostanSchResponse 
			 */
			response = parseResonse(is);
			
			/* 
			 * Закрываем соединение
			 */
			connector.close();
			
			/*
			 * Класс FNSPriostanSchResponse (или производный от него, смотря как релизовано снятие подписи)
			 * генерируется по схеме сервиса и не корректируется вручную.
			 * Но, для внутреннего потребителя, возможно, что:
			 * 1. Нет нужды обрабатывать полный набор атрибутов, возвращаемых сервисом, достаточно сокращенного.
			 * 2. Важно сохранить стабильность (прозрачность) при изменении схемы внешнего сервиса.
			 * 3. Вероятно, нужные данные могут располагаться в другиой древовидной структуре.
			 * 4. Для выдачи результата внутреннему потребителю в формате JSON необходимо другое, 
			 *    отличное от автоматически сгенерированного, аннотирование.  
			 * 
			 * Все эти вопросы требуют архитектурной проработки (на уровне архитектурных принципов, конвенций).
			 * Здесь же, пока, происходит "ручной" маппинг атрибутов обекта класса FNSPriostanSchResponse на атрибуты 
			 * текущего объекта, класса FNSPriostanSch.
			 * 
			 * Общий для всех сервисов (первое допущение в будующей конвенции!!!) атрибут Result заполняется
			 * в методах setNormalResponse() и setErrorResponse().
			 * 
			 * Обощенный результат InList ("есть в списках приостановления") - прямо в следующих строках.
			 */
			
			//if (/*здесь критерий присутствия в списке*/) {
				setInList("true");
			//}
			//else {
			//	setInList("false");
			//}
			
			
			log.info("Service FNSPriostanSch finished");

			setNormalResponse();
		}
		catch (Exception e) {
			setErrorResponse();
		}
	}

	private void writeBody(OutputStream os, FNSPriostanSchRequest body) throws JAXBException, IOException {
    	// Возможно, будет передаваться подписанные класс - производный от FNSPriostanSchRequest
        JAXBContext context = JAXBContext.newInstance(FNSPriostanSchRequest.class);	 
        // устанавливаем флаг для читабельного вывода XML в JAXB
        Marshaller marshaller = context.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        // маршаллинг объекта в выходной поток соединения
        marshaller.marshal(body, os);
		os.flush();
		os.close();
		log.info("runService. Body writted to Data Output Stream");
	}

	private FNSPriostanSchResponse parseResonse(InputStream is) throws JAXBException {
        // создаем объект JAXBContext - точку входа для JAXB
    	// Возможно, будет передаваться подписанные класс - производный от FNSPriostanSchResponse
        JAXBContext jaxbContext = JAXBContext.newInstance(FNSPriostanSchResponse.class);
        Unmarshaller un = jaxbContext.createUnmarshaller();
        return (FNSPriostanSchResponse) un.unmarshal(is);
	}

	private void setNormalResponse() {
		result = "0";
	}

	private void setErrorResponse() {
		result = "Error";
	}

}
