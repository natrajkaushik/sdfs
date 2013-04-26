package com.scs.sdfs.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.net.ssl.SSLContext;

import com.scs.sdfs.Utils;
import com.scs.sdfs.args.CmdGetFileArgument;
import com.scs.sdfs.args.CmdPutFileArgument;
import com.scs.sdfs.args.CommandArgument;
import com.scs.sdfs.delegation.DelegationToken;
import com.scs.sdfs.rspns.CmdGetFileResponse;
import com.scs.sdfs.rspns.CmdPutFileResponse;

/**
 * Thread listens to console 
 */
public class ConsoleListener extends Thread{

	private SSLContext sslContext;
	private ServerConnection serverConnection;
	private ClientFileManager clientFileManager = ClientFileManager.getClientFileManager();
	
	enum Methods{
		START ("START"),
		GET ("GET"),
		PUT ("PUT"),
		DELEGATE ("DELEGATE"),
		_DELEGATE("DELEGATE*"),
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
		case _DELEGATE:
			handleDelegateStar(tokens);
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
			
			CommandArgument get = null;
			if(clientFileManager.isOwner(uid)){
				get = new CmdGetFileArgument(uid, null);
			}
			else if(clientFileManager.hasValidDelegationToken(uid, Methods.GET)){
				DelegationToken token = clientFileManager.getDelegationToken(uid, Methods.GET);
				get = new CmdGetFileArgument(uid, token);
			}else{
				System.out.println("Client has no read access to file [" + "]" + uid);
				return;
			}
			
			/* blocking calls to server */
			serverConnection.sendCommand(get); 
			CmdGetFileResponse response = (CmdGetFileResponse)serverConnection.readFromServer(Methods.GET);
			//TODO write fileContents to file
		}
	}
	
	/**
	 * handle PUT command
	 * @param tokens
	 */
	private void handlePut(String[] tokens) {
		if (tokens.length != 2) {
			System.out.println("Usage: PUT <UID>");
		} else {
			byte[] fileContents = null; // TODO get this from file
			String uid = tokens[1];

			CommandArgument put = null;
			if (clientFileManager.isOwner(uid)) {
				put = new CmdPutFileArgument(uid, fileContents, null);
			} else if (clientFileManager.hasValidDelegationToken(uid, Methods.PUT)) {
				DelegationToken token = clientFileManager.getDelegationToken(uid, Methods.PUT);
				put = new CmdGetFileArgument(uid, token);
			} else {
				System.out.println("Client has no write access to file [" + "]" + uid);
				return;
			}
			
			/* blocking calls to server */
			serverConnection.sendCommand(put); 
			CmdPutFileResponse response = (CmdPutFileResponse)serverConnection.readFromServer(Methods.PUT);
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
			String uid = tokens[1];
			String client = tokens[2];
			long duration;
			try{
				duration = Long.valueOf(tokens[3]);
			}catch(NumberFormatException e){
				System.out.println("DURATION has to be a number");
				return;
			}
			
			long startEpoch = System.currentTimeMillis();
			long endEpoch = startEpoch + (duration * 1000);
			
			if(clientFileManager.isOwner(uid)){
				
			}
			else if(clientFileManager.hasValidDelegationToken(uid, Methods.DELEGATE)){
				
			}
			else{
				System.out.println("Client has no delegate access to file [" + "]" + uid);
			}
		}
	}
	
	/**
	 * handle DELEGATE* command
	 * @param tokens
	 */
	private void handleDelegateStar(String[] tokens){
		if(tokens.length != 4){
			System.out.println("Usage: DELEGATE* <UID> <CLIENT_IP> <DURATION>");
		}else{
			String uid = tokens[1];
			String client = tokens[2];
			long duration;
			try{
				duration = Long.valueOf(tokens[3]);
			}catch(NumberFormatException e){
				System.out.println("DURATION has to be a number");
				return;
			}
			
			long startEpoch = System.currentTimeMillis();
			long endEpoch = startEpoch + (duration * 1000);
			
			if(clientFileManager.isOwner(uid)){
				
			}
			else if(clientFileManager.hasValidDelegationToken(uid, Methods._DELEGATE)){
				
			}
			else{
				System.out.println("Client has no delegate access to file [" + "]" + uid);
			}
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


