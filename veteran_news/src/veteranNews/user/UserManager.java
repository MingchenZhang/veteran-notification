/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package veteranNews.user;

import java.security.NoSuchAlgorithmException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Random;
import java.util.regex.Pattern;
import veteranNews.error.Alert;
import veteranNews.error.CriticalException;
import veteranNews.error.IllegalParameterException;
import veteranNews.error.Prompt;
import veteranNews.mysql.MysqlConnector;
import veteranNews.util.Encryption;
import veteranNews.util.Format;
import veteranNews.util.Hash;

/**
 *
 * @author zmc94
 */
public class UserManager {
	public static final int MIN_USER_NAME_LENGTH = 5;
	public static final int MAX_USER_NAME_LENGTH = 50;
	public static final int MIN_PASSWORD_LENGTH = 8;
	public static final int MAX_PASSWORD_LENGTH = 50;
	
	public static final String DB_USER_INFO = "user_info";
	public static final String[][] DB_USER_INFO_COL = {
		{"user_id","bigint(20)","PRIMARY KEY AUTO_INCREMENT"},
		{"user_name","varchar("+MAX_USER_NAME_LENGTH+")"," NOT NULL"},
		{"password","varchar(64)"," NOT NULL"},
		{"password_salt","varchar(80)"," NOT NULL"},
		{"email","varchar(100)"," NOT NULL"},
		{"email_hash","varchar(64)"," NOT NULL"},
		{"register_time","TIMESTAMP","DEFAULT CURRENT_TIMESTAMP"},
		{"user_status","smallint(6)","NOT NULL"}
	};
	public static final String DB_USER_INFO_EXTRA = "INDEX(email_hash),INDEX(user_name),INDEX(email)";
	
	public static final String DB_LOGIN_KEY = "user_login_key";
	public static final String[][] DB_LOGIN_KEY_COL = {
		{"user_id","bigint(20)","NOT NULL"},
		{"login_key","bigint(20)","NOT NULL"},
		{"enter_time","TIMESTAMP","DEFAULT CURRENT_TIMESTAMP"},
	};
	public static final String DB_LOGIN_KEY_EXTRA = "INDEX(user_id),INDEX(login_key)";
	
	public static final String DB_USER_SESSION = "user_session";
	public static final String[][] DB_USER_SESSION_COL = {
		{"user_id","bigint(20)","NOT NULL"},
		{"token","bigint(20)","NOT NULL"},
		{"time_created","TIMESTAMP","DEFAULT CURRENT_TIMESTAMP"},
		{"last_login","TIMESTAMP",""},
		{"device_description","varchar(80)",""},
	};
	public static final String DB_USER_SESSION_EXTRA = "INDEX(user_id)";
	
	private final MysqlConnector db;
	
	public UserManager(MysqlConnector db) throws CriticalException{
		this.db = db;
		db.reserveTable(DB_USER_INFO,DB_USER_INFO_COL,DB_USER_INFO_EXTRA);
		db.reserveTable(DB_LOGIN_KEY, DB_LOGIN_KEY_COL, DB_LOGIN_KEY_EXTRA);
		db.reserveTable(DB_USER_SESSION, DB_USER_SESSION_COL, DB_USER_SESSION_EXTRA);
	}
	
	public long createNewUser(String userName, String password, String passwordSalt, String emailAddress, int status) throws SQLException, NoSuchAlgorithmException, IllegalParameterException{
		
		if(!Pattern.compile("^[a-z0-9_-]{"+MIN_USER_NAME_LENGTH+","+MAX_USER_NAME_LENGTH+"}$").matcher(userName).matches())
			throw new IllegalArgumentException("illegal user name:"+userName);
		if(!Pattern.compile("^[0-9a-fA-F]{64}$").matcher(password).matches())
			throw new IllegalArgumentException("illegal password:"+password);
		password = password.toLowerCase();
		if(!Pattern.compile("^[0-9a-zA-Z]{80}$").matcher(passwordSalt).matches())
			throw new IllegalArgumentException("illegal salt:"+password);
		if(!Pattern.compile("^([a-z0-9_\\.-]+)@([\\da-z\\.-]+)\\.([a-z\\.]{2,6})$").matcher(emailAddress).matches())
			throw new IllegalArgumentException("illegal email address:"+emailAddress);
		try {
			ResultSet repeatUserName = db.executeQuery("SELECT COUNT(*) FROM "+DB_USER_INFO+" WHERE user_name='"+userName+"';");
			if(repeatUserName.next() && repeatUserName.getInt(1)>0)
				throw new IllegalParameterException("user name existed");
			ResultSet repeatEmail = db.executeQuery("SELECT COUNT(*) FROM "+DB_USER_INFO+" WHERE email='"+emailAddress+"';");
			if(repeatEmail.next() && repeatEmail.getInt(1)>0)
				throw new IllegalParameterException("email existed");
			String email256 = Hash.SHA256Digest(emailAddress.getBytes(), 10000);
			db.executeUpdate("INSERT INTO "+DB_USER_INFO+" (user_name,password,password_salt,email,email_hash,user_status)"
					+ " VALUE ('"+userName+"','"+password+"','"+passwordSalt+"','"
					+ emailAddress + "','"+email256+"',"+status+");");
			ResultSet result = db.executeQuery("SELECT user_id FROM "+DB_USER_INFO+" WHERE email_hash='"+email256+"';");
			result.next();
			long userID = result.getLong(1);
			Prompt.log("new user created: "+userName+" userID="+userID, 4);
			return userID;
		} catch (SQLException ex) {
			Alert.warning("SQLException while creating new user");
			Alert.exception(UserManager.class, ex);
			throw ex;
		} catch (NoSuchAlgorithmException ex) {
			Alert.warning("SHA256 not supported");
			Alert.exception(UserManager.class, ex);
			throw ex;
		}
	}
	
	public String getUserName(long userID) throws SQLException{
		try {
			ResultSet result = db.executeQuery("SELECT user_name FROM "+DB_USER_INFO+" WHERE user_id="+userID+";");
			if(result.next())
				return result.getString(1);
			else
				return null;
		} catch (SQLException ex) {
			Alert.warning("cannot get user name (SQLException)");
			Alert.exception(UserManager.class, ex);
			throw ex;
		}
	}
	
	public int getAccountStatus(long userID) throws SQLException{
		try {
			ResultSet result = db.executeQuery("SELECT user_status FROM "+DB_USER_INFO+" WHERE user_id="+userID+";");
			if(result.next())
				return result.getInt(1);
			else
				return -1;
		} catch (SQLException ex) {
			Alert.warning("cannot get account status (SQLException)");
			Alert.exception(UserManager.class, ex);
			throw ex;
		}
	}
	
	public ArrayList<String[]> getLoginAttempt(String emailHash) throws SQLException{
		if(!Pattern.compile("^[0-9a-fA-F]{64}$").matcher(emailHash).matches())
			throw new IllegalArgumentException("illegal password:"+emailHash);
		try {
			ResultSet result = db.executeQuery("SELECT user_id,password_salt,email FROM "+DB_USER_INFO+" WHERE email_hash='"+emailHash+"';");
			ArrayList <String[]> info = new ArrayList<>(1);
			while(result.next()){
				long userID = result.getLong("user_id");
				String passwordSalt = result.getString("password_salt");
				String email = result.getString("email");
				long key = addNewLoginAttempt(userID, emailHash);
				String[] loginInfoBundle = {passwordSalt,Long.toString(key),email};
				info.add(loginInfoBundle);
			}
			return info;
		} catch (SQLException ex) {
			Alert.warning("fail to prepare login attempt");
			Alert.exception(UserManager.class, ex);
			throw ex;
		}
	}
	
	private long addNewLoginAttempt(long user_id, String emailHash) throws SQLException{
		if(!Pattern.compile("^[0-9a-fA-F]{64}$").matcher(emailHash).matches())
			throw new IllegalArgumentException("illegal user_id in you database!!!:"+user_id);
		Random rand = new Random();
		long key;
		ResultSet keyCount;
		do{
			key = rand.nextLong();
			keyCount = db.executeQuery("SELECT COUNT(*) FROM "+DB_LOGIN_KEY+" WHERE login_key="+key+";");
			keyCount.next();
		}while(keyCount.getInt(1)>0);
		db.executeUpdate("INSERT INTO "+DB_LOGIN_KEY+"(user_id,login_key)"+" VALUE "+"("+user_id+","+key+")");
		return key;
	}
	
	public long getLoginToken(String emailHash, long key, long encryptedToken) throws SQLException, IllegalParameterException, NoSuchAlgorithmException{
		if(!Pattern.compile("^[0-9a-fA-F]{64}$").matcher(emailHash).matches())
			throw new IllegalArgumentException("illegal password:"+emailHash);
		ResultSet info = db.executeQuery("SELECT "+DB_USER_INFO+".user_id,"+DB_USER_INFO+".password FROM "
				+DB_LOGIN_KEY+" JOIN "+DB_USER_INFO +" ON "+DB_USER_INFO+".user_id="+DB_LOGIN_KEY+".user_id"
				+" WHERE "+DB_USER_INFO+".email_hash='"+emailHash+"' AND "+DB_LOGIN_KEY+".login_key="+key);
		if(!info.next())
			throw new IllegalParameterException("does not exist");
		long userID = info.getLong(DB_USER_INFO+".user_id");
		String password = info.getString(DB_USER_INFO+"password");
		byte[] decryptKey = Format.concat(Format.getByteFromHex(password),Format.getByte(key));
		decryptKey = Format.cutByte(Hash.SHA256DigestToByte(decryptKey, 1), 16);
		byte[] iv = "ThisIsARandomNum".getBytes();
		long token;
		try {
			token = Format.getLongFromByte(Encryption.aesDecrypt(decryptKey, iv, Format.getByte(encryptedToken)));
		} catch (Exception ex) {
			Alert.warning("Decryption error");
			Alert.exception(UserManager.class, ex);
			throw new IllegalParameterException("Decryption error");
		}
		db.executeUpdate("INSERT INTO "+DB_USER_SESSION+" (user_id,token) VALUE ("+userID+","+token+");");
		return userID;
	}
	
	public void logout(long userID, long token) throws IllegalParameterException, SQLException{
		int rowEffected = db.executeUpdate("DELETE FROM "+DB_USER_SESSION+" WHERE user_id="+userID+" AND token="+token);
		if(rowEffected < 0)
			throw new IllegalParameterException("does not exist");
	}
	
	public boolean verifyLogin(long userID, long token) throws SQLException{
		ResultSet result = db.executeQuery("SELECT COUNT(*) FROM "+DB_USER_SESSION+" WHERE user_id="+userID+" AND token="+token);
		return (result.next() && result.getInt(1)==1);
	}
}
