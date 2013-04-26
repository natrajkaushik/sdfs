package com.scs.sdfs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class Utils {

	public static boolean isNullOrEmpty(String str){
		return (str == null || str.trim().isEmpty()); 
	}
	
	public static void writeToFile(String filePath, byte[] contents){
		File file = new File(filePath);
		
		if(!file.exists()){
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(file);
			fos.write(contents);
		} catch (IOException e) {
			System.err.println("Couldn't write file to disk!");
			e.printStackTrace();
		}
		
		if(fos != null){
			try {
				fos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static byte[] readFromFile(String filePath){
		File file = new File(filePath);
		
		if(!file.exists()) {
			System.out.println("File does not exist [" + filePath + "]");
			return null;
		}
		
		long _length = file.length();
		int length = (int) _length;
		if (length != _length) {
			System.out.println("File size exceeds 2GB!");
			return null;
		}

		byte[] data = new byte[length];
		FileInputStream fis = null;
		
		try {
			fis = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		try {
			fis.read(data);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if(fis != null){
			try {
				fis.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return data;	
	}
}