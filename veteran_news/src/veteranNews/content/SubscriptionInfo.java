/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package veteranNews.content;

import java.sql.ResultSet;
import java.sql.SQLException;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import veteranNews.error.Alert;
import veteranNews.error.CriticalException;
import veteranNews.main.SingletonManager;
import veteranNews.mysql.MysqlConnector;
import veteranNews.user.UserManager;

/**
 *
 * @author zmc94
 */
public class SubscriptionInfo {
	public static final String DB_SUB_INFO = "subscription_info";
	public static final String[][] DB_SUB_INFO_COL = {
		{"sub_id","int(11)","NOT NULL AUTO_INCREMENT"},
		{"type","int(11)","NOT NULL"},
		{"url","varchar(1024)",""},
		{"title","varchar(200)","NOT NULL"},
		{"description","varchar(1024)",""},
		{"script_name","varchar(100)","NOT NULL"},
		{"update_interval","int(11)","NOT NULL"},
		{"last_update","TIMESTAMP","DEFAULT CURRENT_TIMESTAMP"},
		{"last_update_content","TEXT",""}
	};
	public static final String DB_SUB_INFO_EXTRA = "INDEX(sub_id),INDEX(title)";
	
	private MysqlConnector db;
	private SingletonManager sm;
	
	public SubscriptionInfo(SingletonManager sm) throws CriticalException{
		this.db = sm.getMysqlConnector();
		this.sm = sm;
		db.reserveTable(DB_SUB_INFO, DB_SUB_INFO_COL, DB_SUB_INFO_EXTRA);
	}
	
	public JsonArrayBuilder getSubGeneralInfo(String listSubIDQuery) throws SQLException{
		ResultSet list = db.executeQuery("SELECT sub_id,type,url,title,description FROM "+DB_SUB_INFO+" WHERE sub_id IN ("+listSubIDQuery+")");
		JsonArrayBuilder result = Json.createArrayBuilder();
		while(list.next()){
			JsonObjectBuilder singleSub = Json.createObjectBuilder()
					.add("sub_id", list.getInt("sub_id"))
					.add("type", list.getInt("type"))
					.add("url", list.getString("url"))
					.add("title", list.getString("title"))
					.add("description", list.getString("description"));
			result.add(singleSub);
		}
		return result;
	}
	
	public JsonArrayBuilder getSubGeneralInfoAll() throws SQLException{
		ResultSet list = db.executeQuery("SELECT sub_id,type,url,title,description FROM "+DB_SUB_INFO);
		JsonArrayBuilder result = Json.createArrayBuilder();
		while(list.next()){
			JsonObjectBuilder singleSub = Json.createObjectBuilder()
					.add("sub_id", list.getInt("sub_id"))
					.add("type", list.getInt("type"))
					.add("url", list.getString("url"))
					.add("title", list.getString("title"))
					.add("description", list.getString("description"));
			result.add(singleSub);
		}
		return result;
	}
	
	public ResultSet getAllSubScript() throws SQLException{
		ResultSet result = db.executeQuery("SELECT sub_id,script_name FROM "+DB_SUB_INFO+" WHERE NOW()-UNIX_TIMESTAMP(last_update)>update_interval");
		while(result.next()){
			db.executeUpdate("UPDATE "+DB_SUB_INFO+" SET last_update=NOW() WHERE sub_id="+result.getInt("sub_id"));
		}
		result.beforeFirst();
		return result;
	}
	
	public String getLastUpdateContent(int subID) throws SQLException{
		ResultSet result = db.executeQuery("SELECT last_update_content FROM "+DB_SUB_INFO+" WHERE sub_id="+subID);
		return result.getString("last_update_content");
	}
	
	public void setLastUpdateContent(int subID, String newContent) throws SQLException{
		String[] para = {newContent};
		db.executeUpdate("UPDATE "+DB_SUB_INFO+" SET last_update_content=? WHERE sub_id="+subID, para);
	}
}
