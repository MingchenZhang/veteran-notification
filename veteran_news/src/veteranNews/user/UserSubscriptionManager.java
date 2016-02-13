/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package veteranNews.user;

import java.sql.ResultSet;
import java.sql.SQLException;
import veteranNews.error.Alert;
import veteranNews.error.CriticalException;
import veteranNews.mysql.MysqlConnector;

/**
 *
 * @author zmc94
 */
public class UserSubscriptionManager {
	public static final String DB_USER_SUB = "user_subscription";
	public static final String[][] DB_USER_SUB_COL = {
		{"user_id","bigint(20)","NOT NULL"},
		{"sub_id","int(11)","NOT NULL"},
	};
	public static final String DB_USER_SUB_EXTRA = "INDEX(user_id),INDEX(sub_id)";
	
	private MysqlConnector db;
	
	public UserSubscriptionManager(MysqlConnector db) throws CriticalException{
		this.db = db;
		db.reserveTable(DB_USER_SUB, DB_USER_SUB_COL, DB_USER_SUB_EXTRA);
	}
	
	public String listUserSubID(long userID){
		return "SELECT sub_id FROM "+DB_USER_SUB+" WHERE user_id="+userID;
	}
	public ResultSet listUserSubIDExe(long userID) throws SQLException{
		return db.executeQuery(listUserSubID(userID));
	}
	
	public String listSubUserID(int sub_id){
		return "SELECT user_id FROM "+DB_USER_SUB+" WHERE sub_id="+sub_id;
	}
	
	public void addUserSub(long userID, int[] userSubs) throws SQLException{
		if(userSubs.length==0)return;
		StringBuilder userSubsStr = new StringBuilder();
		for(int i=0; i<userSubs.length;i++){
			userSubsStr.append("(").append(userID).append(",").append(userSubs[i]).append("),");
		}
		userSubsStr.deleteCharAt(userSubsStr.length()-1);
		db.executeUpdate("INSERT INTO "+DB_USER_SUB+"(user_id,sub_id) VALUE "+userSubsStr.toString());
	}
	
	public void removeUserSub(long userID, int[] userSubs) throws SQLException{
		if(userSubs.length==0)return;
		StringBuilder userSubsStr = new StringBuilder();
		for(int i=0; i<userSubs.length;i++){
			userSubsStr.append(userSubs[i]).append(",");
		}
		userSubsStr.deleteCharAt(userSubsStr.length()-1);
		db.executeUpdate("DELETE FROM "+DB_USER_SUB+" WHERE user_id="+userID+" sub_id IN ("+userSubsStr+")");
	}
}
