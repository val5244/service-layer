package com.morbank.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ServiceLayerApplication extends SpringBootServletInitializer {

	private static final Logger log = LoggerFactory.getLogger(ServiceLayerApplication.class);

	public static void main(String[] args)  {
		try {
			log.info("Starting Service");
			SpringApplication.run(ServiceLayerApplication.class, args);
		}
		catch (Exception e1) {
			log.error(e1.toString());
		}
		finally { 
			log.info("Finish starting Service");
		}
	}
}
