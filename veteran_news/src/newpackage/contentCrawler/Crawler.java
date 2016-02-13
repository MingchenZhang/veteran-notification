/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package newpackage.contentCrawler;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonException;
import javax.json.JsonObject;
import veteranNews.content.ArticleManager;
import veteranNews.content.SubscriptionInfo;
import veteranNews.error.Alert;
import veteranNews.main.SingletonManager;

/**
 *
 * @author zmc94
 */
public class Crawler implements Runnable{
	private static final int ADAPTER_WAIT_TIMEOUT = 20 *100;
	
	SingletonManager sm;
	
	public Crawler(SingletonManager sm) {
		this.sm = sm;
	}
	
	@Override
	public void run() {
		Socket scriptAdapterSocket = null;
		InputStream in = null;
		PrintWriter out = null;
		ArticleManager am = sm.getArticleManager();
		SubscriptionInfo si = sm.getSubscriptionInfo();
		try {
			scriptAdapterSocket = new Socket();
			scriptAdapterSocket.setSoTimeout(ADAPTER_WAIT_TIMEOUT);
			scriptAdapterSocket.connect(new InetSocketAddress(InetAddress.getByName("localhost"),7001),1000);
			in = scriptAdapterSocket.getInputStream();
			out = new PrintWriter(scriptAdapterSocket.getOutputStream());
		} catch (IOException ex) {
			Alert.warning("cannot connect crawler script");
			Alert.exception(Crawler.class, ex);
		}
		
		Map<Integer,Integer> crawlingScript;
		try{
			ResultSet scripts = si.getAllSubScript();
			scripts.last();
			int scriptsSize = scripts.getRow();
			if(scriptsSize==0) return;
			scripts.beforeFirst();
			crawlingScript = new HashMap<Integer,Integer>(scriptsSize);
			while(scripts.next()){
				int session;
				do{session = (new Random()).nextInt();} while(crawlingScript.containsKey(session));
				int subID = scripts.getInt("sub_id");
				crawlingScript.put(session,subID);
				out.println(Json.createObjectBuilder()
						.add("session",session)
						.add("scriptName",scripts.getString("script_name"))
						.build());
			}
			
			while(true){
				JsonObject newContent = null;
				try{newContent = Json.createReader(in).readObject();}
				catch(JsonException ex){break;}
				int session = newContent.getInt("session");
				if(crawlingScript.containsKey(session)){
					int subID = crawlingScript.remove(session);
					String lastUpdate = si.getLastUpdateContent(subID);
					JsonObject oldContent = Json.createReader(new ByteArrayInputStream(lastUpdate.getBytes())).readObject();
					JsonArray newContentDiff = getNewContent(oldContent, newContent);
					for(int i=0; i<newContentDiff.size(); i++){
						JsonObject newArticle = newContentDiff.getJsonObject(i);
						am.addNewArticle(
								subID, 
								newArticle.getString("url"), 
								newArticle.getString("title"), 
								newArticle.getString("description"));
						//todo: push content
						si.setLastUpdateContent(subID, newContent.toString());
					}
				}
				if(crawlingScript.isEmpty()) break;
			}
		} catch (SQLException ex) {
			Alert.warning("cannot read database during web crawling");
			Alert.exception(Crawler.class, ex);
		} catch (IOException ex) {
			Alert.warning("IOException during web crawling");
			Alert.exception(Crawler.class, ex);
		}
	}
	
	
	private JsonArray getNewContent(JsonObject oldContent, JsonObject newContent){
		
	}
}
