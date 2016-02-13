/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package veteranNews.httpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.logging.Level;
import java.util.logging.Logger;
import veteranNews.error.Alert;
import veteranNews.error.CriticalException;

/**
 *
 * @author zmc94
 */
public class HttpServer {
	com.sun.net.httpserver.HttpServer server;
	
	boolean requestService = false;
	
	public HttpServer(int port) throws CriticalException{
		try {
			server = com.sun.net.httpserver.HttpServer.create(new InetSocketAddress(port), 0);
		} catch (IOException ex) {
			Alert.warning("http server IOException");
			Alert.exception(HttpServer.class, ex);
			throw new CriticalException("http server IOException");
		}
	}
	
	public void createRequestService(){
		if(requestService) return;
		server.createContext("/request", new RequestServer());
		server.setExecutor(null);
        server.start();
		requestService = true;
	}
}
