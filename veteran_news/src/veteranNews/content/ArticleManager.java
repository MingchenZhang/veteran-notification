/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package veteranNews.content;

import java.sql.ResultSet;
import java.sql.SQLException;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import veteranNews.error.CriticalException;
import veteranNews.error.IllegalParameterException;
import veteranNews.main.SingletonManager;
import veteranNews.mysql.MysqlConnector;

/**
 *
 * @author zmc94
 */
public class ArticleManager {
	public static final String DB_ARTICLES = "articles";
	public static final String[][] DB_ARTICLES_COL = {
		{"article_id","int(11)","PRIMARY KEY AUTO_INCREMENT"},
		{"sub_id","int(11)","NOT NULL"},
		{"time_created","TIMESTAMP","DEFAULT CURRENT_TIMESTAMP"},
		{"url","varchar(1024)",""},
		{"title","varchar(200)","NOT NULL"},
		{"description","varchar(1024)",""},
	};
	public static final String DB_ARTICLES_EXTRA = "INDEX(sub_id,time_created)";
	
	private SingletonManager sm;
	private MysqlConnector db;
	
	public ArticleManager(SingletonManager sm) throws CriticalException{
		this.sm = sm;
		db = sm.getMysqlConnector();
		db.reserveTable(DB_ARTICLES, DB_ARTICLES_COL, DB_ARTICLES_EXTRA);
	}
	
	public void addNewArticle(int subID, String url, String title, String description) throws SQLException{
		String[] info = {url,title,description};
		db.executeUpdate("INSERT INTO "+DB_ARTICLES+"(sub_id,url,title,description) VALUE("+subID+",?,?,?)",info);
	}
	
	public JsonArrayBuilder getRecentArticlesForUser(long userID, int listSize) throws SQLException, IllegalParameterException{
		JsonArrayBuilder articles = Json.createArrayBuilder();
		if(listSize<0 || listSize>200) throw new IllegalParameterException("list size is out of bound");
		ResultSet userSubs = sm.getUserSubscriptionManager().listUserSubIDExe(userID);
		if (!userSubs.isBeforeFirst()) return articles;
		StringBuilder query = new StringBuilder();
		while(userSubs.next()){
			query.append("(SELECT * FROM "+DB_ARTICLES+" WHERE sub_id="+userSubs.getInt(1)+" ORDER BY time_created DESC LIMIT "+listSize+") UNION ALL");
		}
		query.delete(query.length()-10, query.length());
		query.append(" ORDER BY time_created DESC LIMIT "+listSize);
		ResultSet articlesSet = db.executeQuery(query.toString());
		while(articlesSet.next()){
			JsonObjectBuilder article = Json.createObjectBuilder()
					.add("articleID", articlesSet.getInt("article_id"))
					.add("subID", articlesSet.getInt("sub_id"))
					.add("url", articlesSet.getInt("url"))
					.add("title", articlesSet.getInt("title"))
					.add("description", articlesSet.getInt("description"));
			articles.add(article);
		}
		return articles;
	}
}
