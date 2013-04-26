package com.scs.sdfs;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

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
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			fos.write(contents);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(fos != null){
			try {
				fos.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public static byte[] readFromFile(String filePath){
		File file = new File(filePath);
		
		if(!file.exists()){
			System.out.println("File does not exist [" + filePath + "]");
		}
		
        RandomAccessFile _file = null;
		try {
			_file = new RandomAccessFile(file, "r");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

        try {
            long _length = _file.length();
            int length = (int) _length;
            if (length != _length) {
            	throw new IOException("File size >= 2 GB");
            }

            byte[] data = new byte[length];
            _file.readFully(data);
            return data;
        }catch(IOException e){
        	return null;
        }
        finally {
        	if(_file != null){
            try {
				_file.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        	}
        }
	}

}
