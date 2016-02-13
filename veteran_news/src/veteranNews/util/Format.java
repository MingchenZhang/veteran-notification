/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package veteranNews.util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 *
 * @author zmc94
 */
public class Format {
	public static byte[] getByte(long data){
		ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
		buffer.order(ByteOrder.BIG_ENDIAN);
		buffer.putLong(data);
		return buffer.array();
	}
	
	public static byte[] getByteFromHex(String s) {//todo: copyed code
		int len = s.length();
		byte[] data = new byte[len / 2];
		for (int i = 0; i < len; i += 2) {
			data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
					+ Character.digit(s.charAt(i + 1), 16));
		}
		return data;
	}
	
	public static long getLongFromByte(byte[] data) {
//		long value = 0;
//		for (int i = 0; i < by.length; i++) {
//			value += ((long) by[i] & 0xffL) << (8 * i);
//		}//small endian
		
		long value = 0;
		for (int i = 0; i < data.length; i++) {
			value = (value << 8) + (data[i] & 0xff);
		}
		return value;//big endian
	}
	
	public static byte[] concat(byte[]... data){
		int arrayCount = 0;
		for(byte[] b:data)
			arrayCount+=b.length;
		byte[] array = new byte[arrayCount];
		arrayCount = 0;
		for (byte[] d : data) {
			for (int j = 0; j < d.length; j++) {
				array[arrayCount++] = d[j];
			}
		}
		return array;
	}
	
	public static byte[] cutByte(byte[] data, int keep){
		byte[] result = new byte[keep];
		for(int i=0; i<keep; i++){
			result[i] = data[i];
		}
		return result;
	}
}
