/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package veteranNews.util;

import java.util.Base64;

/**
 *
 * @author zmc94
 */
public class Encoding {
	public static String base64Encoding(byte[] data){
		return Base64.getEncoder().encodeToString(data);
	}
	
	public static byte[] base64Decoding(String base64Str){
		return Base64.getDecoder().decode(base64Str);
	}
}
