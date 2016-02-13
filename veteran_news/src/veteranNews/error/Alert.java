/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package veteranNews.error;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author zmc94
 */
public class Alert {
	public static final String ANSI_RESET = "\u001B[0m";
	public static final String ANSI_BLACK = "\u001B[30m";
	public static final String ANSI_RED = "\u001B[31m";
	public static final String ANSI_GREEN = "\u001B[32m";
	public static final String ANSI_YELLOW = "\u001B[33m";
	public static final String ANSI_BLUE = "\u001B[34m";
	public static final String ANSI_PURPLE = "\u001B[35m";
	public static final String ANSI_CYAN = "\u001B[36m";
	public static final String ANSI_WHITE = "\u001B[37m";
	
	
	public static void warning(String str){
		System.out.println(ANSI_YELLOW+str+ANSI_RESET);
	}
	
	public static void criticalWarning(String str){
		System.out.println(ANSI_RED+"Critical error: "+str+ANSI_RESET);
	}
	
	public static void exception(Class clazz, Exception ex){
		Logger.getLogger(clazz.getName()).log(Level.SEVERE, null, ex);
	}
	
	public static void userError(String description, Class clazz, Exception ex){
		Prompt.log(description, 6);
		Logger.getLogger(clazz.getName()).log(Level.SEVERE, null, ex);
	}
}
