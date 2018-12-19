package com.morbank.services.connector;

/*
 * Для работы со СМЭВ, по предоставленным на портале  
 */

import com.morbank.services.ServiceConfig;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SmevConnector {
	private static final Logger log = LoggerFactory.getLogger(SmevConnector.class);
	private HttpURLConnection connection = null;
	private InputStream response = null;

	private ServiceConfig cfg = null;
    public String aliveTime = null;
	private String url = null;
	private String contentType = null; 
	
	public SmevConnector () throws MalformedURLException, IOException {
		if (cfg == null) cfg = ServiceConfig.INSTANCE;
		
		log.info("=============== Default config. Cfg= {}",cfg.toString());
		url = cfg.getSmevUrl() + ":" + cfg.getSmevPort() + cfg.getSmevResource();
		log.info("Connection URL: {}", url);
		contentType = cfg.getSmevConttype();
		log.info("Connection Content-Type: {}",contentType);

		CookieManager cookieManager = new CookieManager();
		CookieHandler.setDefault(cookieManager);
	}

	public void reconnect() throws MalformedURLException, IOException {
		
		HttpURLConnection.setFollowRedirects(true);
		if (cfg.isSmevProxyUsing()) {
			connection = (HttpURLConnection) new URL(url).
					openConnection(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(cfg.getSmevProxyUrl(), Integer.parseInt(cfg.getSmevPort()))));
		} else {
			connection = (HttpURLConnection) new URL(url).
					openConnection();
		}
	}


	public OutputStream prepareService() throws MalformedURLException, IOException  {
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

		charSet = cfg.getSmevCharset();
		connection.setRequestProperty("Accept-Charset", charSet);
		log.info("runService. Connection Accept-Charset set to = {}", charSet);

		connection.setRequestProperty("Content-Type", contentType);
		log.info("runService. Connection Content-Type set to = {}", contentType);
		
		return connection.getOutputStream();
	}

	public InputStream runService() throws IOException {
		connection.connect();
		log.info("runService. Connection prepared");
		
		response = connection.getInputStream();
		log.info("runService. Response = {}", response.toString());

		/*
		 * Проверяем Response Code сервиса, чтобы определить, нормально ли он отработал
		 */
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
