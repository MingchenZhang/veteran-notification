/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package veteranNews.main;

import veteranNews.content.ArticleManager;
import veteranNews.content.SubscriptionInfo;
import veteranNews.mysql.MysqlConnector;
import veteranNews.user.UserManager;
import veteranNews.user.UserSubscriptionManager;

/**
 *
 * @author zmc94
 */
public class SingletonManager {
	MysqlConnector mysqlConnector;
	UserManager userManager;
	UserSubscriptionManager userSubscriptionManager;
	SubscriptionInfo subscriptionInfo;
	ArticleManager articleManager;

	public SubscriptionInfo getSubscriptionInfo() {
		return subscriptionInfo;
	}

	public void setSubscriptionInfo(SubscriptionInfo subscriptionInfo) {
		this.subscriptionInfo = subscriptionInfo;
	}

	public MysqlConnector getMysqlConnector() {
		return mysqlConnector;
	}

	public void setMysqlConnector(MysqlConnector mysqlConnector) {
		this.mysqlConnector = mysqlConnector;
	}

	public UserManager getUserManager() {
		return userManager;
	}

	public void setUserManager(UserManager userManager) {
		this.userManager = userManager;
	}

	public UserSubscriptionManager getUserSubscriptionManager() {
		return userSubscriptionManager;
	}

	public void setUserSubscriptionManager(UserSubscriptionManager userSubscriptionManager) {
		this.userSubscriptionManager = userSubscriptionManager;
	}

	public ArticleManager getArticleManager() {
		return articleManager;
	}

	public void setArticleManager(ArticleManager articleManager) {
		this.articleManager = articleManager;
	}
	
	
}
