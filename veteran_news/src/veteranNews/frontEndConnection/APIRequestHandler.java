/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package veteranNews.frontEndConnection;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.URLDecoder;
import java.security.NoSuchAlgorithmException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonValue;
import veteranNews.error.Alert;
import veteranNews.error.IllegalParameterException;
import veteranNews.error.Prompt;
import veteranNews.main.SingletonManager;
import veteranNews.user.UserManager;

/**
 *
 * @author zmc94
 */
public class APIRequestHandler extends TCPRequestHandler {
	public static final String REQUEST_TYPE = "r";
	
	private SingletonManager sm;
	private UserManager um;

	public APIRequestHandler(Socket socket, SingletonManager sm) {
		super(socket);
		this.sm = sm;
		um = sm.getUserManager();
	}
	
	@Override
	public void run() {
		//throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
		if (socket == null) {
			throw new RuntimeException("No socket assigned");
		}
		
		Prompt.log("API request received from front end", 5);
		
		JsonObject request;
		try {
			request = Json.createReader(socket.getInputStream()).readObject();
		} catch (IOException ex) {
			Alert.warning("IOException while receving from front end");
			Alert.exception(APIRequestHandler.class, ex);
			try {socket.close();} catch (IOException ex1) {}
			return;
		}
		
		JsonObject respond = decryptRequest(request);
		
		try {
			Json.createWriter(socket.getOutputStream()).writeObject(respond);
		} catch (IOException ex) {
			Alert.warning("IOException while sending respond to front end");
			Alert.exception(APIRequestHandler.class, ex);
			try {socket.close();} catch (IOException ex1) {}
			return;
		}
		
		Prompt.log("API request respond to front end", 5);
		
		try {socket.close();} catch (IOException ex) {}
	}

	@Override
	public TCPRequestHandler clone() {
		return new APIRequestHandler(socket, sm);
	}
	
	public JsonObject decryptRequest(JsonObject request){
		switch(request.getString(REQUEST_TYPE, "none")){
			case "new-user":return optionNewUser(request);
			case "get-username":return optionGetUserName(request);
			case "get-account-status":return optionGetAccountStatus(request);
			case "login-attempt":return optionLoginAttempt(request);
			case "get-token":return optionGetToken(request);
			case "logout":return optionLogout(request);
			case "list-available-sub":return optionListAvailableSub(request);
			case "list-user-sub-id":return optionListUserSubID(request);
			case "add-user-sub":return optionAddUserSub(request);
			case "remove-user-sub":return optionRemoveUserSub(request);
			case "list-user-recent-article":return optionListUserRecentArticle(request);
			case "none":
			default:return optionIllegalRequestType(request);
		}
	}
	
	private JsonObject optionNewUser(JsonObject request){
		try {
			//https://abc.com/request?r=new-user&userName=|String encoded in URL encoding|&password=|encryptedPassword(Hex)|&passwordSalt=|encoded in URL encoding|&emailAddress=|String encoded in URL encoding|
			long userID = um.createNewUser(
					URLDecoder.decode(request.getString("userName"), "UTF-8"),
					request.getString("password"),
					URLDecoder.decode(request.getString("passwordSalt"), "UTF-8"),
					URLDecoder.decode(request.getString("emailAddress"), "UTF-8"),
					1);//email-pending
			return Json.createObjectBuilder().add("r", "new-user").add("errorCode", 0).add("userID", userID).build();
		} catch (IllegalParameterException ex) {
			if(ex.getMessage().equals("user name existed"))
				return Json.createObjectBuilder().add("r", "new-user").add("errorCode", 2).build();
			if(ex.getMessage().equals("email existed"))
				return Json.createObjectBuilder().add("r", "new-user").add("errorCode", 4).build();
			else
				return Json.createObjectBuilder().add("r", "new-user").add("errorCode", 102).build();
		} catch (NullPointerException ex) {
			Alert.warning("encounter NullPointerException while creating new user, insufficient arguments");
			Alert.exception(APIRequestHandler.class, ex);
			return Json.createObjectBuilder().add("r", "new-user").add("errorCode", 5).build();
		} catch (IllegalArgumentException ex) {
			Alert.warning("encounter IllegalArgumentException while creating new user");
			Alert.exception(APIRequestHandler.class, ex);
			return Json.createObjectBuilder().add("r", "new-user").add("errorCode", 7).build();
		} catch (SQLException ex) {
			Alert.warning("encounter SQLException while creating new user");
			Alert.exception(APIRequestHandler.class, ex);
			return Json.createObjectBuilder().add("r", "new-user").add("errorCode", 102).build();
		} catch (NoSuchAlgorithmException ex) {
			Alert.warning("encounter NoSuchAlgorithmException while creating new user");
			Alert.exception(APIRequestHandler.class, ex);
			return Json.createObjectBuilder().add("r", "new-user").add("errorCode", 103).build();
		} catch (UnsupportedEncodingException ex) {
			Alert.warning("encounter UnsupportedEncodingException while creating new user");
			Alert.exception(APIRequestHandler.class, ex);
			return Json.createObjectBuilder().add("r", "new-user").add("errorCode", 103).build();
		}
	}
	private JsonObject optionGetUserName(JsonObject request){
		try {
			String userName = um.getUserName(Long.parseLong(request.getString("userID")));
			if(userName!=null)
				return Json.createObjectBuilder().add("r", "get-username").add("userName",userName).add("errorCode", 0).build();
			else
				return Json.createObjectBuilder().add("r", "get-username").add("errorCode", 1).build();
		} catch (SQLException ex) {
			Alert.warning("encounter SQLException while getting user name");
			Alert.exception(APIRequestHandler.class, ex);
			return Json.createObjectBuilder().add("r", "get-username").add("errorCode", 102).build();
		} catch (NullPointerException ex) {
			Alert.warning("encounter NullPointerException while creating new user, insufficient arguments");
			Alert.exception(APIRequestHandler.class, ex);
			return Json.createObjectBuilder().add("r", "get-username").add("errorCode", 5).build();
		} catch (NumberFormatException  ex) {
			Alert.warning("encounter NumberFormatException while getting user name");
			Alert.exception(APIRequestHandler.class, ex);
			return Json.createObjectBuilder().add("r", "get-username").add("errorCode", 7).build();
		}
	}
	private JsonObject optionGetAccountStatus(JsonObject request){
		try {
			int statusCode = um.getAccountStatus(Long.parseLong(request.getString("userID")));
			if(statusCode!=-1)
				return Json.createObjectBuilder().add("r", "get-account-status").add("statusCode",statusCode).add("errorCode", 0).build();
			else
				return Json.createObjectBuilder().add("r", "get-account-status").add("errorCode", 1).build();
		} catch (SQLException ex) {
			Alert.warning("encounter SQLException while getting user name");
			Alert.exception(APIRequestHandler.class, ex);
			return Json.createObjectBuilder().add("r", "get-account-status").add("errorCode", 102).build();
		} catch (NullPointerException ex) {
			Alert.warning("encounter NullPointerException while creating new user, insufficient arguments");
			Alert.exception(APIRequestHandler.class, ex);
			return Json.createObjectBuilder().add("r", "get-account-status").add("errorCode", 5).build();
		} catch (NumberFormatException  ex) {
			Alert.warning("encounter NumberFormatException while creating new user");
			Alert.exception(APIRequestHandler.class, ex);
			return Json.createObjectBuilder().add("r", "get-account-status").add("errorCode", 7).build();
		}
	}
	private JsonObject optionLoginAttempt(JsonObject request){
		try{
			ArrayList<String[]> userList = um.getLoginAttempt(request.getString("email"));
			if(userList.size()<1) throw new IllegalParameterException("email does not exist");
			JsonArrayBuilder userListJson = Json.createArrayBuilder();
			for(int i=0; i<userList.size(); i++){
				if(userList.size()<=1)
					userListJson.add(Json.createObjectBuilder().add("salt",userList.get(i)[0]).add("key",userList.get(i)[1]));
				else
					userListJson.add(Json.createObjectBuilder().add("salt",userList.get(i)[0]).add("key",userList.get(i)[1]).add("email",userList.get(i)[2]));
			}
			return Json.createObjectBuilder().add("r", "login-attempt").add("user",userListJson).add("errorCode", 0).build();
		} catch(SQLException ex){
			Alert.warning("encounter SQLException while giving login attempt");
			Alert.exception(APIRequestHandler.class, ex);
			return Json.createObjectBuilder().add("r", "login-attempt").add("errorCode", 102).build();
		} catch (NullPointerException ex) {
			Alert.warning("encounter NullPointerException while logging in, insufficient arguments");
			Alert.exception(APIRequestHandler.class, ex);
			return Json.createObjectBuilder().add("r", "login-attempt").add("errorCode", 5).build();
		} catch (IllegalParameterException ex) {
			Alert.warning("encounter IllegalParameterException while giving login attempt, email hash: "+request.getString("email")+" does not exist");
			Alert.exception(APIRequestHandler.class, ex);
			return Json.createObjectBuilder().add("r", "login-attempt").add("errorCode", 1).build();
		} catch (IllegalArgumentException ex) {
			Alert.warning("encounter IllegalArgumentException while giving login attempt, argument format error");
			Alert.exception(APIRequestHandler.class, ex);
			return Json.createObjectBuilder().add("r", "login-attempt").add("errorCode", 7).build();
		}
	}
	private JsonObject optionGetToken(JsonObject request){
		try{
			long userID = um.getLoginToken(
					request.getString("email"), 
					Long.parseLong(request.getString("key")), 
					Long.parseLong(request.getString("token")));
			return Json.createObjectBuilder().add("r", "get-token").add("errorCode", 0).add("userID",userID).build();
		} catch (IllegalParameterException ex){
			if(ex.getMessage().equals("does not exist")){
				Alert.warning("encounter IllegalParameterException while getting token, user info does not exist");
				Alert.exception(APIRequestHandler.class, ex);
				return Json.createObjectBuilder().add("r", "get-token").add("errorCode", 1).build();
			}else if(ex.getMessage().equals("Decryption error")){
				Alert.warning("encounter IllegalParameterException while getting token, Decryption error");
				Alert.exception(APIRequestHandler.class, ex);
				return Json.createObjectBuilder().add("r", "get-token").add("errorCode", 6).build();
			}else
				return Json.createObjectBuilder().add("r", "get-token").add("errorCode", 254).build();
		} catch (SQLException ex){
			Alert.warning("encounter SQLException while getting token");
			Alert.exception(APIRequestHandler.class, ex);
			return Json.createObjectBuilder().add("r", "get-token").add("errorCode", 102).build();
		} catch (NoSuchAlgorithmException ex) {
			Alert.warning("encounter NoSuchAlgorithmException while getting token");
			Alert.exception(APIRequestHandler.class, ex);
			return Json.createObjectBuilder().add("r", "get-token").add("errorCode", 103).build();
		} catch (NumberFormatException  ex) {
			Alert.warning("encounter NumberFormatException while getting token");
			Alert.exception(APIRequestHandler.class, ex);
			return Json.createObjectBuilder().add("r", "get-token").add("errorCode", 7).build();
		} catch (IllegalArgumentException  ex) {
			Alert.warning("encounter IllegalArgumentException while getting token");
			Alert.exception(APIRequestHandler.class, ex);
			return Json.createObjectBuilder().add("r", "get-token").add("errorCode", 7).build();
		} catch (NullPointerException  ex) {
			Alert.warning("encounter NullPointerException while getting token");
			Alert.exception(APIRequestHandler.class, ex);
			return Json.createObjectBuilder().add("r", "get-token").add("errorCode", 5).build();
		}
	}
	private JsonObject optionLogout(JsonObject request){
		try {
			um.logout(
					Long.parseLong(request.getString("userID")), 
					Long.parseLong(request.getString("userToken")));
			return Json.createObjectBuilder().add("r", "logout").add("errorCode", 0).build();
		} catch (IllegalParameterException ex) {
			Alert.warning("encounter IllegalParameterException while logging out, info does not exist");
			Alert.exception(APIRequestHandler.class, ex);
			return Json.createObjectBuilder().add("r", "logout").add("errorCode", 1).build();
		} catch (SQLException ex) {
			Alert.warning("encounter SQLException while logging out");
			Alert.exception(APIRequestHandler.class, ex);
			return Json.createObjectBuilder().add("r", "logout").add("errorCode", 102).build();
		} catch (NumberFormatException  ex) {
			Alert.warning("encounter NumberFormatException while logging out");
			Alert.exception(APIRequestHandler.class, ex);
			return Json.createObjectBuilder().add("r", "logout").add("errorCode", 7).build();
		} catch (NullPointerException ex){
			Alert.userError("NullPointerException in logout request", APIRequestHandler.class, ex);
			return Json.createObjectBuilder().add("r", "logout").add("errorCode", 5).build();
		}
	}
	private JsonObject optionIllegalRequestType(JsonObject request){
		return Json.createObjectBuilder().add("r", request.getString(REQUEST_TYPE, "none")).add("errorCode", 255).build();
	}
	
	private JsonObject optionListAvailableSub(JsonObject request){
		//{userID:2314,token:54235,r:list-available-sub}
		//{r:list-available-sub,errorCode:0,list:[{},{}]}
		long userID;
		long token;
		try {
			userID = Long.parseLong(request.getString("userID"));
			token = Long.parseLong(request.getString("token"));
		}catch(ClassCastException|NumberFormatException ex){
			Alert.userError("ClassCastException|NumberFormatException(cannot convert to number) in list-available-sub request", APIRequestHandler.class, ex);
			return Json.createObjectBuilder().add("r", "list-available-sub").add("errorCode", 7).build();
		}catch(NullPointerException ex){
			Alert.userError("NullPointerException in list-available-sub request", APIRequestHandler.class, ex);
			return Json.createObjectBuilder().add("r", "list-available-sub").add("errorCode", 5).build();
		}
		try {
			boolean verifiedUser = um.verifyLogin(userID, token);
			if(!verifiedUser) return Json.createObjectBuilder().add("r", "list-available-sub").add("errorCode", 20).build();
			JsonArrayBuilder allInfo = sm.getSubscriptionInfo().getSubGeneralInfoAll();
			return Json.createObjectBuilder().add("r", "list-available-sub").add("errorCode", 0).add("list", allInfo).build();
		} catch (SQLException ex) {
			Alert.userError("SQLException in list-available-sub request", APIRequestHandler.class, ex);
			return Json.createObjectBuilder().add("r", "list-available-sub").add("errorCode", 102).build();
		} catch (ClassCastException ex){
			Alert.userError("ClassCastException(cannot convert to number) in list-available-sub request", APIRequestHandler.class, ex);
			return Json.createObjectBuilder().add("r", "list-available-sub").add("errorCode", 7).build();
		} catch (NullPointerException ex){
			Alert.userError("NullPointerException in list-available-sub request", APIRequestHandler.class, ex);
			return Json.createObjectBuilder().add("r", "list-available-sub").add("errorCode", 5).build();
		}
	}
	
	private JsonObject optionListUserSubID(JsonObject request){
		//{userID:"2314",token:"54235",r:list-user-sub-id}
		//{r:list-user-sub-id,errorCode:0,list:[21,32]}
		long userID;
		long token;
		try {
			userID = Long.parseLong(request.getString("userID"));
			token = Long.parseLong(request.getString("token"));
		}catch(ClassCastException|NumberFormatException ex){
			Alert.userError("ClassCastException|NumberFormatException(cannot convert to number) in list-user-sub-id request", APIRequestHandler.class, ex);
			return Json.createObjectBuilder().add("r", "list-user-sub-id").add("errorCode", 7).build();
		}catch(NullPointerException ex){
			Alert.userError("NullPointerException in list-user-sub-id request", APIRequestHandler.class, ex);
			return Json.createObjectBuilder().add("r", "list-user-sub-id").add("errorCode", 5).build();
		}
		try {
			boolean verifiedUser = um.verifyLogin(userID, token);
			if(!verifiedUser) return Json.createObjectBuilder().add("r", "list-user-sub-id").add("errorCode", 20).build();
			JsonArrayBuilder subId = Json.createArrayBuilder();
			ResultSet result = sm.getUserSubscriptionManager().listUserSubIDExe(userID);
			while(result.next()){
				subId.add(result.getInt("sub_id"));
			}
			return Json.createObjectBuilder().add("r", "list-user-sub-id").add("errorCode", 0).add("list", subId).build();
		} catch (SQLException ex) {
			Alert.userError("SQLException in list-available-sub request", APIRequestHandler.class, ex);
			return Json.createObjectBuilder().add("r", "list-user-sub-id").add("errorCode", 102).build();
		}
	}
	
	private JsonObject optionAddUserSub(JsonObject request){
		//{userID:2314,token:54235,r:add-user-sub,list:"[32,45]"}
		//{r:add-user-sub,errorCode:0}
		long userID;
		long token;
		try {
			userID = Long.parseLong(request.getString("userID"));
			token = Long.parseLong(request.getString("token"));
		}catch(ClassCastException|NumberFormatException ex){
			Alert.userError("ClassCastException|NumberFormatException(cannot convert to number) in add-user-sub request", APIRequestHandler.class, ex);
			return Json.createObjectBuilder().add("r", "add-user-sub").add("errorCode", 7).build();
		}catch(NullPointerException ex){
			Alert.userError("NullPointerException in add-user-sub request", APIRequestHandler.class, ex);
			return Json.createObjectBuilder().add("r", "add-user-sub").add("errorCode", 5).build();
		}
		try {
			boolean verifiedUser = um.verifyLogin(userID, token);
			if(!verifiedUser) return Json.createObjectBuilder().add("r", "add-user-sub").add("errorCode", 20).build();
			JsonArray subList = Json.createReader(new ByteArrayInputStream(request.getString("list").getBytes())).readArray();
			int[] subIntList = new int[subList.size()];
			for(int i=0; i<subIntList.length; i++){
				subIntList[i] = subList.getInt(i);
			}
			sm.getUserSubscriptionManager().addUserSub(userID, subIntList);
			return Json.createObjectBuilder().add("r", "add-user-sub").add("errorCode", 0).build();
		} catch (SQLException ex) {
			Alert.userError("SQLException in add-user-sub request", APIRequestHandler.class, ex);
			return Json.createObjectBuilder().add("r", "add-user-sub").add("errorCode", 102).build();
		} catch (ClassCastException ex){
			Alert.userError("ClassCastException(cannot convert to number) in add-user-sub request", APIRequestHandler.class, ex);
			return Json.createObjectBuilder().add("r", "add-user-sub").add("errorCode", 7).build();
		}catch(NullPointerException ex){
			Alert.userError("NullPointerException in add-user-sub request", APIRequestHandler.class, ex);
			return Json.createObjectBuilder().add("r", "add-user-sub").add("errorCode", 5).build();
		}
	}
	
	private JsonObject optionRemoveUserSub(JsonObject request){
		//{userID:"2314",token:"54235",r:remove-user-sub,list:"[32,45]"}
		//{r:remove-user-sub,errorCode:0}
		long userID;
		long token;
		try {
			userID = Long.parseLong(request.getString("userID"));
			token = Long.parseLong(request.getString("token"));
		}catch(ClassCastException|NumberFormatException ex){
			Alert.userError("ClassCastException|NumberFormatException(cannot convert to number) in remove-user-sub request", APIRequestHandler.class, ex);
			return Json.createObjectBuilder().add("r", "remove-user-sub").add("errorCode", 7).build();
		}catch(NullPointerException ex){
			Alert.userError("NullPointerException(cannot convert to number) in remove-user-sub request", APIRequestHandler.class, ex);
			return Json.createObjectBuilder().add("r", "remove-user-sub").add("errorCode", 5).build();
		}
		try {
			boolean verifiedUser = um.verifyLogin(userID, token);
			if(!verifiedUser) return Json.createObjectBuilder().add("r", "remove-user-sub").add("errorCode", 20).build();
			JsonArray subList = Json.createReader(new ByteArrayInputStream(request.getString("list").getBytes())).readArray();
			int[] subIntList = new int[subList.size()];
			for(int i=0; i<subIntList.length; i++){
				subIntList[i] = subList.getInt(i);
			}
			sm.getUserSubscriptionManager().removeUserSub(userID, subIntList);
			return Json.createObjectBuilder().add("r", "remove-user-sub").add("errorCode", 0).build();
		} catch (SQLException ex) {
			Alert.userError("SQLException in add-user-sub request", APIRequestHandler.class, ex);
			return Json.createObjectBuilder().add("r", "remove-user-sub").add("errorCode", 102).build();
		} catch (ClassCastException ex){
			Alert.userError("ClassCastException(cannot convert to number) in add-user-sub request", APIRequestHandler.class, ex);
			return Json.createObjectBuilder().add("r", "remove-user-sub").add("errorCode", 7).build();
		}catch(NullPointerException ex){
			Alert.userError("NullPointerException in add-user-sub request", APIRequestHandler.class, ex);
			return Json.createObjectBuilder().add("r", "remove-user-sub").add("errorCode", 5).build();
		}
	}
	
	/**
	 * {"r":"list-user-recent-article","userID":"123","token":"123","listSize":100}
	 * {"r":"list-user-recent-article","errorCode":0,"list":[{"articleID":123,"subID":1,"title":"this is a title","description":"this is the description","url":"http://test.com"}]}
	 * @param request
	 * @return 
	 */
	private JsonObject optionListUserRecentArticle(JsonObject request){
		long userID;
		long token;
		try {
			userID = Long.parseLong(request.getString("userID"));
			token = Long.parseLong(request.getString("token"));
		}catch(ClassCastException|NumberFormatException ex){
			Alert.userError("ClassCastException|NumberFormatException(cannot convert to number) in list-user-recent-article request", APIRequestHandler.class, ex);
			return Json.createObjectBuilder().add("r", "list-user-recent-article").add("errorCode", 7).build();
		}catch(NullPointerException ex){
			Alert.userError("NullPointerException(cannot convert to number) in list-user-recent-article request", APIRequestHandler.class, ex);
			return Json.createObjectBuilder().add("r", "list-user-recent-article").add("errorCode", 5).build();
		}
		try {
			boolean verifiedUser = um.verifyLogin(userID, token);
			if(!verifiedUser) return Json.createObjectBuilder().add("r", "list-user-recent-article").add("errorCode", 20).build();
			JsonArrayBuilder articleList = sm.getArticleManager().getRecentArticlesForUser(userID, request.getInt("listSize"));
			return Json.createObjectBuilder()
					.add("r", "list-user-recent-article")
					.add("errorCode", 0)
					.add("list",articleList)
					.build();
		} catch (SQLException ex) {
			Alert.userError("SQLException in list-user-recent-article request", APIRequestHandler.class, ex);
			return Json.createObjectBuilder().add("r", "list-user-recent-article").add("errorCode", 102).build();
		} catch (ClassCastException ex){
			Alert.userError("ClassCastException(cannot convert to number) in list-user-recent-article request", APIRequestHandler.class, ex);
			return Json.createObjectBuilder().add("r", "list-user-recent-article").add("errorCode", 7).build();
		}catch(NullPointerException ex){
			Alert.userError("NullPointerException in list-user-recent-article request", APIRequestHandler.class, ex);
			return Json.createObjectBuilder().add("r", "list-user-recent-article").add("errorCode", 5).build();
		} catch (IllegalParameterException ex) {
			Alert.userError("IllegalParameterException in list-user-recent-article request", APIRequestHandler.class, ex);
			return Json.createObjectBuilder().add("r", "list-user-recent-article").add("errorCode", 7).build();
		}
	}
}