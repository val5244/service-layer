package com.morbank.services.connector;

import com.morbank.services.ServiceConfig;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SparkConnector {
	private static final Logger log = LoggerFactory.getLogger(SparkConnector.class);
	private HttpURLConnection connection = null;
	private OutputStreamWriter writer = null;
	private InputStream response = null;

	private ServiceConfig cfg = null;
    public String aliveTime = null;
	private String body = null; 
	private String url = null;
	private String contentType = null; 
	
	public SparkConnector () throws MalformedURLException, IOException {
		if (cfg == null) cfg = ServiceConfig.INSTANCE;
		
		log.info("=============== Default config. Cfg= {}",cfg.toString());
		url = cfg.getSparkUrl() + ":" + cfg.getSparkPort() + cfg.getSparkResource();
		log.info("Connection URL: {}", url);
		contentType = cfg.getSparkConttype();
		log.info("Connection Content-Type: {}",contentType);

		CookieManager cookieManager = new CookieManager();
		CookieHandler.setDefault(cookieManager);
	}

	public String bodyTemplate(String serviceName) {
		log.info("Service name: {}", serviceName);
		try {
			log.info("XML InputStream class: {}",this.getClass());
			File file = new File(cfg.getSparkServicePostTemplate(serviceName));
			Path path = Paths.get(file.getAbsolutePath(), "");
			log.info("XML template File: {}, Path: {}", file, path);	
			List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
			StringBuilder bodyTemplate = new StringBuilder((int)(Files.size(path))+1);
			lines.forEach(s -> bodyTemplate.append(s));
			log.info("XML template readed: {}", bodyTemplate);			
			return bodyTemplate.toString();
		} catch (Exception e) {
			log.error("Exception due read service template: {}", e);
		}
		return null;		
	}

	public void setBody(String filledTemplate) {
		body = filledTemplate;
		log.info("XML template filled: {}", body);			
	}

	public void reconnect() throws MalformedURLException, IOException {
		
		HttpURLConnection.setFollowRedirects(true);
		if (cfg.isSparkProxyUsing()) {
			connection = (HttpURLConnection) new URL(url).
					openConnection(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(cfg.getSparkProxyUrl(), Integer.parseInt(cfg.getSparkProxyPort()))));
		} else {
			connection = (HttpURLConnection) new URL(url).
					openConnection();
		}
	}

	public InputStream runService() throws MalformedURLException, IOException {
		String charSet;
		
		reconnect();
		log.info("runService entered");
		
		connection.setDoInput(true);
		connection.setDoOutput(true); // Triggers out- and input- streams
		log.info("runService. Connection triggered POST");

		connection.setRequestMethod("POST");
		log.info("runService. Connection request method setted to POST");
		
		connection.setUseCaches(true);
		connection.setRequestProperty("Connection", "close"); // keep-alive
		connection.setRequestProperty("Proxy-Connection", "close"); // keep-alive
		log.info("runService. Connection setted to Keep-Alive mode");

		charSet = cfg.getSparkCharset();
		connection.setRequestProperty("Accept-Charset", charSet);
		log.info("runService. Connection Accept-Charset set to = {}", charSet);

		connection.setRequestProperty("Content-Type", contentType);
		log.info("runService. Connection Content-Type set to = {}", contentType);
		//log.info("runService. Headers = {}", connection.getHeaderFields().toString());			
		
		writer = new OutputStreamWriter(connection.getOutputStream());
		log.info("runService. Data Output Stream (writer) = {}", writer.toString());
		writer.write(body);
		log.info("runService. Body putted to Data Output Stream = {}", body);
		writer.flush();
		writer.close();

		connection.connect();
		log.info("runService. Connection prepared");
		
		response = connection.getInputStream();
		log.info("runService. Response = {}", response.toString());

		int responseCode = connection.getResponseCode();
		if (responseCode == HttpURLConnection.HTTP_OK) {
			log.info("runService. Connection OK at the url: {}",url);
		}
		else
			log.info("runService. Connection {} at the url: {} failed", responseCode, url);
			
		return response;
	}

	public void close() throws IOException {
		try {
			while (response.read() != -1); // need to read to make available connection send next request
		}
		catch (IOException e) {
			log.info("Response stream closed"); // do nothing, just check response closed or not
		}
		// also headers
		if (connection != null) {
			connection.getContent();
			connection.getHeaderFields();
		}
		response.close();
		connection.disconnect();		
	}

	public void repair() {
		try {
			InputStream is = connection.getErrorStream();
			if (is != null) is.close();
		}
		catch (IOException e) {
			log.info("Exception catched on connection repair operation. {}", e.getMessage());
		}
	}

}
