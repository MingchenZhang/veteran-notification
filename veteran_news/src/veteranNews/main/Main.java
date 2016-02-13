/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package veteranNews.main;

import newpackage.contentCrawler.CrawlerManager;
import veteranNews.content.ArticleManager;
import veteranNews.content.SubscriptionInfo;
import veteranNews.error.Alert;
import veteranNews.error.CriticalException;
import veteranNews.error.Prompt;
import veteranNews.frontEndConnection.APIRequestHandler;
import veteranNews.frontEndConnection.SocketServer;
import veteranNews.mysql.MysqlConnector;
import veteranNews.user.UserManager;
import veteranNews.user.UserSubscriptionManager;

/**
 *
 * @author zmc94
 */
public class Main {
	public static void main(String[] args){
		int listeningPort;
		try {
			listeningPort = Integer.parseInt(args[0]);
			if (listeningPort < 1 || listeningPort > 65535) {
				Alert.criticalWarning("port assigned is illegal: " + listeningPort);
				return;
			}
		} catch (ArrayIndexOutOfBoundsException ex){
			Alert.criticalWarning("no port assigned");
			return;
		} catch (NumberFormatException ex) {
			Alert.criticalWarning(ex.getMessage());
			Alert.criticalWarning("port assigned is illegal:"+args[0]);
			return;
		}
		Prompt.log("java daemon initializing", 1);
		
		SingletonManager sm = new SingletonManager();
		
		Prompt.log("service started",0);
		Prompt.log("establishing mysql connection",0);
		MysqlConnector db;
		try {
			db = new MysqlConnector();
			sm.setMysqlConnector(db);
		} catch (CriticalException ex) {
			Alert.criticalWarning(ex.getMessage());
			Alert.criticalWarning("establishing mysql connection failed");
			return;
		}
		Prompt.log("establishing mysql connection completed",0);
		
		Prompt.log("starting UserManager",0);
		UserManager users;
		try {
			users = new UserManager(sm.getMysqlConnector());
			sm.setUserManager(users);
		} catch (CriticalException ex) {
			Alert.criticalWarning(ex.getMessage());
			Alert.criticalWarning("starting UserManager failed");
			return;
		}
		Prompt.log("starting UserManager compeleted",0);
		
		Prompt.log("starting UserSubscriptionManager",0);
		UserSubscriptionManager sub;
		try {
			sub = new UserSubscriptionManager(sm.getMysqlConnector());
			sm.setUserSubscriptionManager(sub);
		} catch (CriticalException ex) {
			Alert.criticalWarning(ex.getMessage());
			Alert.criticalWarning("starting UserSubscriptionManager failed");
			return;
		}
		Prompt.log("starting UserSubscriptionManager compeleted",0);
		
		Prompt.log("starting SubscriptionInfo",0);
		SubscriptionInfo subInfo;
		try {
			subInfo = new SubscriptionInfo(sm);
			sm.setSubscriptionInfo(subInfo);
		} catch (CriticalException ex) {
			Alert.criticalWarning(ex.getMessage());
			Alert.criticalWarning("starting SubscriptionInfo failed");
			return;
		}
		Prompt.log("starting SubscriptionInfo compeleted",0);
		
		Prompt.log("starting ArticleManager",0);
		ArticleManager articleManager;
		try {
			articleManager = new ArticleManager(sm);
			sm.setArticleManager(articleManager);
		} catch (CriticalException ex) {
			Alert.criticalWarning(ex.getMessage());
			Alert.criticalWarning("starting ArticleManager failed");
			return;
		}
		Prompt.log("starting ArticleManager compeleted",0);
		
		Prompt.log("starting ArticleManager",0);
		CrawlerManager crawlerManager;
		try {
			crawlerManager = new CrawlerManager(sm);
			crawlerManager.scheduleCrawler(10);
		} catch (Exception ex) {
			Alert.criticalWarning(ex.getMessage());
			Alert.criticalWarning("starting ArticleManager failed");
			return;
		}
		Prompt.log("starting ArticleManager compeleted",0);
		
		Prompt.log("starting server",0);
		try {
			SocketServer socketServer = new SocketServer(listeningPort,new APIRequestHandler(null,sm));
			socketServer.startServer();
		} catch (CriticalException ex) {
			Alert.criticalWarning(ex.getMessage());
			Alert.criticalWarning("starting server failed");
			return;
		}
		Prompt.log("starting server completed",0);
	}
}
