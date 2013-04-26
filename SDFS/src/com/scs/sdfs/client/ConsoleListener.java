package com.scs.sdfs.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.net.ssl.SSLContext;

import com.scs.sdfs.Utils;
import com.scs.sdfs.args.CmdGetFileArgument;
import com.scs.sdfs.args.CommandArgument;

/**
 * Thread listens to console 
 */
public class ConsoleListener extends Thread{

	private SSLContext sslContext;
	private ServerConnection serverConnection;
	
	enum Methods{
		START ("START"),
		GET ("GET"),
		PUT ("PUT"),
		DELEGATE ("DELEGATE"),
		CLOSE ("CLOSE");
		
		private String method;
		
		Methods(String method){
			this.method = method;
		}
		
		
	}
	
	public ConsoleListener(SSLContext sslContext) {
		super();
		this.sslContext = sslContext;
	}

	public void run() {
		
		System.out.println("Welcome to the SDFS Client Interface !");
		
		while(true){
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		    try {
				String line = br.readLine();
				if(!Utils.isNullOrEmpty(line)){
					commandProcessor(line);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * @param line input to process
	 */
	private void commandProcessor(String line){
		String[] tokens = line.split("\\s+");
		
		if(tokens.length == 0){
			System.out.println("Invalid command");
		}
		
		Methods method = Methods.valueOf(tokens[0].toUpperCase());
		
		//process each method
		switch (method) {
		case START:
			handleStart(tokens);
			break;
		case GET:
			handleGet(tokens);
			break;
		case PUT:
			handlePut(tokens);
			break;
		case DELEGATE:
			handleDelegate(tokens);
			break;
		case CLOSE:
			handleClose(tokens);
			break;
		}		
	}
	
	/**
	 * handle START command
	 * @param tokens
	 */
	private void handleStart(String[] tokens){
		if(tokens.length != 2){
			System.out.println("Usage: START <SERVER_IP>");
		}else{
			serverConnection = new ServerConnection(sslContext);
		}
	}
	
	/**
	 * handle GET command
	 * @param tokens
	 */
	private void handleGet(String[] tokens){
		if(tokens.length != 2){
			System.out.println("Usage: GET <UID>");
		}else{
			String uid = tokens[1];
			CommandArgument get = new CmdGetFileArgument(uid, null);
			serverConnection.sendCommand(get);
			byte[] fileContents = serverConnection.readFromServer();
		}
	}
	
	/**
	 * handle PUT command
	 * @param tokens
	 */
	private void handlePut(String[] tokens){
		if(tokens.length != 2){
			System.out.println("Usage: PUT <UID>");
		}else{
			
		}
	}
	
	/**
	 * handle DELEGATE command
	 * @param tokens
	 */
	private void handleDelegate(String[] tokens){
		if(tokens.length != 4){
			System.out.println("Usage: DELEGATE <UID> <CLIENT_IP> <DURATION>");
		}else{
			
		}
	}
	
	/**
	 * handle CLOSE command
	 * @param tokens
	 */
	private void handleClose(String[] tokens){
		if(tokens.length != 1){
			System.out.println("Usage: CLOSE");
		}else{
			System.out.println("Exiting .... ");
			serverConnection.close();
		}
	}
	

}


