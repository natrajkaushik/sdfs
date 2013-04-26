package com.scs.sdfs.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Thread listens to console 
 */
public class ConsoleListener extends Thread{

	public void run() {
		
		System.out.println("Welcome to the SDFS Client Interface !");
		
		while(true){
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		    try {
				String line = br.readLine();
				commandProcessor(line);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * @param command to process
	 */
	private void commandProcessor(String command){
		
	}
	

}
