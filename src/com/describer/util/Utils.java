package com.describer.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Utils {
	
	private Utils() {}

	public static String getFileNameWithoutExtension(String filename) {
		if (filename.lastIndexOf(".") != -1){
			return filename.substring(0, filename.lastIndexOf("."));			
		}
		return filename;		
	}
	
	public static String getFileExtension(File file) {
		String substr = null;
		if (file.getName().lastIndexOf(".") != -1){
			substr = file.getName().substring(file.getName().lastIndexOf("."));
			return substr.toLowerCase();
		}
		return "";
	}

	/**
	 * To convert the InputStream to String we use the
	 * BufferedReader.readLine() method. We iterate until the BufferedReader
	 * return null which means there's no more data to read. Each line will
	 * appended to a StringBuilder and returned as String.
	 * @param is inputStream to convert
	 */
	public static String convertIStreamToString(InputStream is) {
		
		
		StringBuilder sb = new StringBuilder();

		String line = null;
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));			
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return sb.toString();
	}
	
	public static byte[] convertIStreamToBA(InputStream is) {
		BufferedInputStream bufIn = new BufferedInputStream(is);
		ByteArrayOutputStream bAOut = new ByteArrayOutputStream();
		BufferedOutputStream bufOut = new BufferedOutputStream(bAOut, 512);
		try {
			byte[] bufAr = new byte[100];
			int numread;
			while ((numread = bufIn.read(bufAr)) != -1) {
				bufOut.write(bufAr, 0, numread);
			} 
			bufOut.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}		
		return bAOut.toByteArray();
	}

}
