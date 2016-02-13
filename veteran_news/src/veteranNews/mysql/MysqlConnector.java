/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package veteranNews.mysql;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import veteranNews.error.Alert;
import veteranNews.error.CriticalException;
import veteranNews.error.Prompt;

/**
 *
 * @author zmc94
 */
public class MysqlConnector {
	private static final String MYSQL_DATABASE_NAME = "test_database";
	private static final String MYSQL_ADDRESS = "jdbc:mysql://localhost:3306/"+MYSQL_DATABASE_NAME;//todo: new account for mysql
	private static final String MYSQL_USERNAME = "root";
	private static final String MYSQL_PASSWORD = "1qazxsw2";
	
	
	private Connection conn;
	
	public MysqlConnector() throws CriticalException{
		try {
			Class.forName("org.gjt.mm.mysql.Driver").newInstance();
			conn = DriverManager.getConnection(MYSQL_ADDRESS, MYSQL_USERNAME, MYSQL_PASSWORD);
		} catch (ClassNotFoundException ex) {
			Alert.warning("MySQL driver cannot be found");
			Alert.exception(MysqlConnector.class,ex);
			throw new CriticalException("MySQL driver cannot be found");
		} catch (InstantiationException ex) {
			Alert.warning("MySQL driver instantiation error");
			Alert.exception(MysqlConnector.class,ex);
			throw new CriticalException("MySQL driver instantiation error");
		} catch (IllegalAccessException ex) {
			Alert.warning("MySQL driver illegal access error");
			Alert.exception(MysqlConnector.class,ex);
			throw new CriticalException("MySQL driver illegal access error");
		} catch (SQLException ex) {
			Alert.warning("MySQL Exception");
			Alert.exception(MysqlConnector.class,ex);
			throw new CriticalException("MySQL Exception");
		}
	}
	
	public void checkConnection() throws SQLException{
		if(!conn.isValid(2))
			conn = DriverManager.getConnection(MYSQL_ADDRESS, MYSQL_USERNAME, MYSQL_PASSWORD);
	}
	
	public ResultSet executeQuery(String query) throws SQLException{
		checkConnection();
		Statement state = conn.createStatement();
		return state.executeQuery(query);
	}
	
	public int executeUpdate(String query) throws SQLException{
		checkConnection();
		Statement state = conn.createStatement();
		return state.executeUpdate(query);
	}
	
	public int executeUpdate(String query, String[] parameter) throws SQLException{
		PreparedStatement ps = conn.prepareStatement(query);
		for(int i=0; i<parameter.length; i++){
			ps.setString(i+1, parameter[i]);
		}
		return ps.executeUpdate();
	}
	
	public int executeUpdateWithIntIDReturn(String query, String[] parameter) throws SQLException{
		PreparedStatement ps = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
		for(int i=0; i<parameter.length; i++){
			ps.setString(i+1, parameter[i]);
		}
		ps.executeUpdate();
		ResultSet id = ps.getGeneratedKeys();
		id.next();
		return id.getInt(1);
	}
	
	public boolean createTable(String tableName,String[][] name_types_extra,String extra) throws SQLException {
		checkConnection();
		Statement state = conn.createStatement();
		StringBuilder value = new StringBuilder();
		for(String[] pair: name_types_extra){
			value.append(pair[0]).append(" ")
					.append(pair[1].toUpperCase()).append(" ")
					.append(pair[2].toUpperCase()).append(",");
		}
		if(value.length()!=0)value.deleteCharAt(value.length()-1);
		try {
			state.executeUpdate("CREATE TABLE "+tableName+" ("+value.toString()+","+extra+");");
			return true;
		} catch (SQLException ex) {
			String[] strs = ex.getMessage().split(" ");
			if(strs[strs.length-1].equals("exists")) return false;
			else throw ex;
		}
	}
	
	public void forceDropTable(String tableName){
		try {
			checkConnection();
			Statement state = conn.createStatement();
			state.executeUpdate("DROP TABLE "+tableName+";");
			Prompt.log("table droped: "+tableName, 5);
		} catch (SQLException ex) {
			Alert.warning("table drop failed: "+ex.getMessage());
		}
	}
	
	public boolean hasTable(String tableName) throws SQLException{
		checkConnection();
		Statement state = conn.createStatement();
		ResultSet rs = state.executeQuery("SELECT COUNT(*) FROM information_schema.tables WHERE  table_name = '"+tableName+"';");
		if(rs.next() && rs.getInt(1)>0) return true;
		else return false;
//		DatabaseMetaData dbm = conn.getMetaData();
//		ResultSet table = dbm.getTables(null, null, tableName, null);
//		if(table.next()) return true;
//		else return false;
	}
	
	public boolean hasTable(String tableName, String[][] name_types_extra, boolean ignoreExtraCol) throws SQLException{
		checkConnection();
		Statement state = conn.createStatement();
//		ResultSet colName = state.executeQuery("SELECT COLUMN_NAME FROM information_schema.COLUMNS WHERE table_schema = '"+MYSQL_DATABASE_NAME+"' AND table_name = '"+tableName+"';");
//		ResultSet colType = state.executeQuery("SELECT DATA_TYPE FROM information_schema.COLUMNS WHERE table_schema = '"+MYSQL_DATABASE_NAME+"' AND table_name = '"+tableName+"';");
//		for(String[] pair: name_types){
//			if(colName.next() && colType.next() && 
//					pair[0].equals(colName.getString(1)) && 
//					pair[1].equals(colType.getString(1)));
//			else return false;
//		}
//		return ignoreExtraCol || (!colName.next() && !colType.next());
		if(!hasTable(tableName)) return false;
		ResultSet cols = state.executeQuery("SHOW COLUMNS FROM "+tableName+";");//mysql specific
		for(String[] pair: name_types_extra){
			if(cols.next()  && 
					pair[0].equals(cols.getString("Field")) && 
					pair[1].equalsIgnoreCase(cols.getString("Type")));
			else return false;
		}
		return ignoreExtraCol || !cols.next();
	}
	
	public void reserveTable(String tableName, String[][] colInfo, String extra) throws CriticalException{
		try {
			if (!hasTable(tableName, colInfo, true)) {
				if (hasTable(tableName)) {
					throw new CriticalException("user info table format error");
				}else{
					createTable(tableName, colInfo,extra);
				}
			}
		} catch (SQLException ex) {
			Alert.warning("user manager initialization failed");
			Alert.exception(MysqlConnector.class,ex);
			throw new CriticalException("user manager initialization failed");
		}
	}
}
