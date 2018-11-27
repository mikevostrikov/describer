package com.describer;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import com.describer.util.Utils;

/**
 * Zip file opener/saver
 * ignores directories
 * @author Mike V.
 */
public class Archiver {
	
	private HashMap<String, byte[]> entriesMap = new HashMap<String, byte[]>();
	
	/**
	 * Open zip file
	 * @param file to open
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	public void open(File file) throws IOException {
		ZipFile zf = new ZipFile(file);
		for (Enumeration<ZipEntry> en = (Enumeration<ZipEntry>) zf.entries(); en.hasMoreElements();) {
			ZipEntry entry = en.nextElement();
			// ignore directories
			if (!entry.isDirectory()){
				InputStream is = zf.getInputStream(entry);
				entriesMap.put(entry.getName(), Utils.convertIStreamToBA(is));
			}
		}
		zf.close();
	}
	
	/**
	 * Save zip file
	 * @param file save
	 * @throws IOException
	 */
	public void save(File file) throws IOException {
		if (file.isDirectory()) {
			return;
		}
		if (file.exists()) {
			file.delete();
		}
		file.createNewFile();
		
		ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(file));
		ZipEntry entry;
		
		for (String key : entriesMap.keySet()) {
			entry = new ZipEntry(key);
			zos.putNextEntry(entry);
			zos.write(entriesMap.get(key));
			zos.closeEntry();
		}
		zos.close();	
	}
	
	/**
	 * Get compressed content
	 * @param entryName
	 * @return content
	 */
	public byte[] getContent(String entryName) {
		return entriesMap.get(entryName);
	}
	
	/**
	 * Set content-entry to compress
	 * @param name entry-file name
	 * @param content entry-file content
	 * @return true if there if all ok
	 */
	public boolean setContent(String name, byte[] content) {
		if (name != null && !name.isEmpty()) {
			entriesMap.put(name, content);
			return true;
		} else {
			assert false;
			return false;
		}			
	}
	
	/**
	 * Remove content-entry by name
	 * @param name
	 * @return true if there was associated non-null content
	 */
	public boolean removeContent(String name) {
		if (entriesMap.remove(name) != null)
			return true;
		else {
			assert false;
			return false;
		}
	}
	
	/*
	// just for testing
	public static void main(String... args) {
		
		Archiver arch = new Archiver();
		try {
			arch.open(new File("vspom.zip"));
			System.out.println(new String(arch.getContent("document.xml"), "UTF-8"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	*/
}
