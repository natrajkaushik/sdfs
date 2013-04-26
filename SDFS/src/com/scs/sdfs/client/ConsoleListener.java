package com.scs.sdfs.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.util.HashSet;
import java.util.Set;

import javax.net.ssl.SSLContext;

import com.scs.sdfs.KeyStoreHelper;
import com.scs.sdfs.Method;
import com.scs.sdfs.Right;
import com.scs.sdfs.Utils;
import com.scs.sdfs.args.CmdDelegateRightsArgument;
import com.scs.sdfs.args.CmdGetFileArgument;
import com.scs.sdfs.args.CmdPutFileArgument;
import com.scs.sdfs.args.CommandArgument;
import com.scs.sdfs.delegation.DelegationPrimitive;
import com.scs.sdfs.delegation.DelegationToken;
import com.scs.sdfs.rspns.CmdGetFileResponse;
import com.scs.sdfs.rspns.CmdPutFileResponse;

/**
 * Thread listens to console 
 */
public class ConsoleListener extends Thread{

	private SSLContext sslContext;
	private ServerConnection serverConnection;
	private ClientConnection clientConnection;
	private ClientFileManager clientFileManager = ClientFileManager.getClientFileManager();
	

	
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
		
		Method method = Method.valueOf(tokens[0].toUpperCase());
		
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
			handleDelegate(tokens, true);
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
			System.out.println("Usage: GET <fileUID>");
		}else{
			String uid = tokens[1];
			
			CommandArgument get = null;
			if(clientFileManager.isOwner(uid)){
				get = new CmdGetFileArgument(uid, null);
			}
			else if(clientFileManager.hasValidDelegationToken(uid, Method.GET)){
				DelegationToken token = clientFileManager.getDelegationToken(uid, Method.GET);
				get = new CmdGetFileArgument(uid, token);
			}else{
				System.out.println("Client has no read access to file [" + "]" + uid);
				return;
			}
			
			/* blocking calls to server */
			serverConnection.sendCommand(get); 
			CmdGetFileResponse response = (CmdGetFileResponse)serverConnection.readFromServer(Method.GET);
			//TODO write fileContents to file
		}
	}
	
	/**
	 * handle PUT command
	 * @param tokens
	 */
	private void handlePut(String[] tokens) {
		if (tokens.length != 2) {
			System.out.println("Usage: PUT <fileUID>");
		} else {
			byte[] fileContents = null; // TODO get this from file
			String uid = tokens[1];

			CommandArgument put = null;
			if (clientFileManager.isOwner(uid)) {
				put = new CmdPutFileArgument(uid, fileContents, null);
			} else if (clientFileManager.hasValidDelegationToken(uid, Method.PUT)) {
				DelegationToken token = clientFileManager.getDelegationToken(uid, Method.PUT);
				put = new CmdGetFileArgument(uid, token);
			} else {
				System.out.println("Client has no write access to file [" + "]" + uid);
				return;
			}
			
			/* blocking calls to server */
			serverConnection.sendCommand(put); 
			CmdPutFileResponse response = (CmdPutFileResponse)serverConnection.readFromServer(Method.PUT);
		}
		
		
	}
	
	/**
	 * handle DELEGATE command
	 * @param tokens
	 */
	private void handleDelegate(String[] tokens) {
		handleDelegate(tokens, false);
	}
	
	private void handleDelegate(String[] tokens, boolean star) {
		if(tokens.length < 5){
			System.out.println("Usage: DELEGATE <fileUID> <CLIENT> <DURATION> <[RIGHTS]>");
		}else{
			String uid = tokens[1];
			String target = tokens[2];
			
			String[] clientAddress = target.split(":");
			String host = clientAddress[0];
			int port;
			try{
				port = Integer.parseInt(clientAddress[1]);
			}catch(NumberFormatException e){
				System.out.println("CLIENT must be of the form IP:PORT where PORT is a valid port number");
				return;
			}
			
			long duration;
			try{
				duration = Long.valueOf(tokens[3]) * 1000;
			}catch(NumberFormatException e){
				System.out.println("DURATION has to be a number");
				return;
			}
			
			Set<Right> rights = new HashSet<Right>();
			for(int i = 4; i < tokens.length; i++){
				rights.add(Right.valueOf(tokens[i].toUpperCase()));
			}
			
			long startEpoch = System.currentTimeMillis();
			
			clientConnection = new ClientConnection(sslContext, host, port);
			
			DelegationPrimitive primitive = new DelegationPrimitive(clientFileManager.getAlias(), target, 
					uid, rights.contains(Right.READ), rights.contains(Right.WRITE), 
					rights.contains(Right.DELEGATE), startEpoch, duration);
			
			Certificate certificate = KeyStoreHelper.getCertificate(clientFileManager.getAlias(), clientFileManager.getPassword());
			PrivateKey privateKey = KeyStoreHelper.getPrivateKey(clientFileManager.getAlias(), clientFileManager.getPassword());
			
			if(clientFileManager.isOwner(uid)){
				DelegationToken token = new DelegationToken(primitive, certificate, null, privateKey);
				CommandArgument delegate = new CmdDelegateRightsArgument(uid, token);
				clientConnection.sendCommand(delegate);
			}
			else if(clientFileManager.hasValidDelegationToken(uid, Method.DELEGATE)){
				
			}
			else{
				System.out.println("Client has no target access to file [" + "]" + uid);
			}
			
			clientConnection.close();
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