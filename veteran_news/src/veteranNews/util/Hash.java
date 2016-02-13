/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package veteranNews.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 *
 * @author zmc94
 */
public class Hash {
	public static String SHA256Digest(byte[] data, int iteration) throws NoSuchAlgorithmException{
		MessageDigest sha256Digest = MessageDigest.getInstance("SHA-256");
		for(int i=0; i<iteration; i++) data = sha256Digest.digest(data);
		return bytesToHex(data);
	}
	
	public static byte[] SHA256DigestToByte(byte[] data, int iteration) throws NoSuchAlgorithmException{
		MessageDigest sha256Digest = MessageDigest.getInstance("SHA-256");
		for(int i=0; i<iteration; i++) data = sha256Digest.digest(data);
		return data;
	}
	
	private final static char[] hexArray = "0123456789ABCDEF".toCharArray();
	private static String bytesToHex(byte[] bytes) {//todo: copyed code
		char[] hexChars = new char[bytes.length * 2];
		for (int j = 0; j < bytes.length; j++) {
			int v = bytes[j] & 0xFF;
			hexChars[j * 2] = hexArray[v >>> 4];
			hexChars[j * 2 + 1] = hexArray[v & 0x0F];
		}
		return new String(hexChars);
	}
}
