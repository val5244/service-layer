package com.morbank.services;

/*
 * 
 * Класс-синглетон для чтения параметров конфигурации из файла.
 * Сам файл размещается в каталоге ./resources
 * Некоторые стандартные параметры, используемые spring boot, остаются в стандартном файле
 * application.properties в стандартном для spring boot каталоге src/main/resources.
 * При сборке application.properties включаются в состав JAR, тогда как resources/service-layer.conf, 
 * по-умолчанию, нет (также не включаются в сборку шаблоны soap запросов Спарк, хранящиеся в том же каталоге).
 * Эти шаблоны и настройки нужно не забывать деплоить на сервера исполнения вместе с JAR
 * 
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServiceConfig {

	private static final Logger log = LoggerFactory.getLogger(ServiceConfig.class);
	public static final ServiceConfig INSTANCE = new ServiceConfig();	// for Singleton

	private Properties configFile = new Properties();
	
	private boolean sparkProxyUsing;
	private String sparkProxyUrl;
	private String sparkProxyPort;
	private String sparkUrl;
	private String sparkCharset;
	private String sparkPort;
	private String sparkResource;
	private String sparkConttype;
	private String sparkLogin;
	private String sparkPassword;

	private boolean smevProxyUsing;
	private String smevProxyUrl;
	private String smevProxyPort;
	private String smevUrl;
	private String smevCharset;
	private String smevPort;
	private String smevResource;
	private String smevConttype;

	private ServiceConfig() {
		String path = new File(".").getAbsolutePath();
		log.info(path);

		try {
			configFile.load(new FileInputStream("resources/service-layer.conf"));
			sparkProxyUsing = Boolean.parseBoolean(configFile.getProperty("spark.proxy.using", "true"));
			sparkProxyUrl = configFile.getProperty("spark.proxy.url",""); // squid-temp.morbank.com
			sparkProxyPort = configFile.getProperty("spark.proxy.port",""); // 3128

			sparkUrl = configFile.getProperty("spark.url", "http://sparkgatetest.interfax.ru");
			sparkPort = configFile.getProperty("spark.port", "80");
			sparkResource = configFile.getProperty("spark.resource", "/iFaxWebService/");
			sparkConttype = configFile.getProperty("spark.contenttype", "application/soap+xml;charset=utf-8");
			sparkCharset = configFile.getProperty("spark.charset", java.nio.charset.StandardCharsets.UTF_8.name());
			sparkLogin = configFile.getProperty("spark.login", "");
			sparkPassword = configFile.getProperty("spark.password", "");
			
			smevProxyUsing = Boolean.parseBoolean(configFile.getProperty("smev.proxy.using", "true"));
			smevProxyUrl = configFile.getProperty("smev.proxy.url",""); // squid-temp.morbank.com
			smevProxyPort = configFile.getProperty("smev.proxy.port",""); // 3128

			smevUrl = configFile.getProperty("spark.url", "http://sparkgatetest.interfax.ru");
			smevPort = configFile.getProperty("spark.port", "80");
			smevResource = configFile.getProperty("spark.resource", "/iFaxWebService/");
			smevConttype = configFile.getProperty("spark.contenttype", "application/soap+xml;charset=utf-8");
			smevCharset = configFile.getProperty("spark.charset", java.nio.charset.StandardCharsets.UTF_8.name());
			
		}
		catch (IOException e) {
			log.error(e.getMessage());
		}
		
	}

	
	public boolean isSparkProxyUsing() { return sparkProxyUsing; }
	public String getSparkProxyUrl() { return sparkProxyUrl; }	
	public String getSparkProxyPort() { return sparkProxyPort; }
	public String getSparkUrl() { return sparkUrl; }
	public String getSparkCharset() { return sparkCharset; }
	public String getSparkPort() { return sparkPort; }
	public String getSparkResource() { return sparkResource; }
	public String getSparkConttype() { return sparkConttype; }
	public String getSparkServicePostTemplate(String serviceName) { return "resources/spark."+serviceName+".xml"; }
	public String getSparkLogin() { return sparkLogin; }
	public String getSparkPassword() { return sparkPassword; }

	public boolean isSmevProxyUsing() { return smevProxyUsing; }
	public String getSmevProxyUrl() { return smevProxyUrl; }
	public String getSmevProxyPort() { return smevProxyPort; }
	public String getSmevUrl() { return smevUrl; }
	public String getSmevCharset() { return smevCharset; }
	public String getSmevPort() { return smevPort; }
	public String getSmevResource() { return smevResource; }
	public String getSmevConttype() { return smevConttype;  }
	public String getSmevServicePostTemplate(String serviceName) { return "resources/smev."+serviceName+".xml"; }


}
