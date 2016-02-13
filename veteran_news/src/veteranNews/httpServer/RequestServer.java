/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package veteranNews.httpServer;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import veteranNews.error.Prompt;

/**
 *
 * @author zmc94
 */
public class RequestServer implements HttpHandler {

	@Override
	public void handle(HttpExchange he) throws IOException {
		Prompt.log("new request received", 3);
		String queryStr = he.getRequestURI().getQuery();
		Prompt.log("query: "+queryStr, 4);
		Map<String,String> query = queryToMap(queryStr);
		String result = null;
		switch(query.get("r")){
			case "new-user": result = handleNewUser(query);break;
			case "get-username": result = handleGetUserName(query);break;
			case "get-account-status": result = handleGetAccountStatus(query);break;
			case "login": result = handleLogin(query);break;
			case "logout": result = handleLogout(query);break;
		}
		if(result == null) result = "{}";
		he.sendResponseHeaders(200, result.length());
		OutputStream os = he.getResponseBody();
		os.write(result.getBytes());
		os.close();
		Prompt.log("new request replied", 3);
	}
	
	private static Map<String,String> queryToMap(String query){
		Map<String,String> map = new HashMap<String,String>();
		if(query == null) return map;
		String[] querys = query.split("&");
		for(int i=0; i<querys.length; i++){
			String[] q = querys[i].split("=");
			map.put(q[0], q[1]);
		}
		return map;
	}
	
	private String handleNewUser(Map<String,String> query){
		return "{\"r\":\"new-user\",\"errorCode\":255}";
	}
	private String handleGetUserName(Map<String,String> query){
		return "{\"r\":\"get-username\",\"errorCode\":255}";
	}
	private String handleGetAccountStatus(Map<String,String> query){
		return "{\"r\":\"get-account-status\",\"errorCode\":255}";
	}
	private String handleLogin(Map<String,String> query){
		return "{\"r\":\"login\",\"errorCode\":255}";
	}
	private String handleLogout(Map<String,String> query){
		return "{\"r\":\"logout\",\"errorCode\":255}";
	}
}
