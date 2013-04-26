package com.scs.sdfs.client;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.util.HashSet;
import java.util.Set;

import javax.net.ssl.SSLContext;

import com.scs.sdfs.Constants;
import com.scs.sdfs.ErrorCode;
import com.scs.sdfs.Method;
import com.scs.sdfs.Right;
import com.scs.sdfs.Utils;
import com.scs.sdfs.args.CmdDelegateRightsArgument;
import com.scs.sdfs.args.CmdGetFileArgument;
import com.scs.sdfs.args.CmdPutFileArgument;
import com.scs.sdfs.args.CommandArgument;
import com.scs.sdfs.delegation.DelegationPrimitive;
import com.scs.sdfs.delegation.DelegationToken;
import com.scs.sdfs.rspns.CmdDelegateRightsResponse;
import com.scs.sdfs.rspns.CmdGetFileResponse;
import com.scs.sdfs.rspns.CommandResponse;
import com.scs.sdfs.server.Crypto;

/**
 * Thread listens to console 
 */
public class ConsoleListener extends Thread{

	private SSLContext sslContext;
	private ServerConnection serverConnection;
	private PeerConnection peerConnection;
	private ClientManager clientManager = ClientManager.getClientManager();
	
	private boolean keepLooping = true;
	
	public ConsoleListener(SSLContext sslContext) {
		super();
		this.sslContext = sslContext;
	}

	public void run() {
		System.out.println("Welcome to the SDFS Client Interface !");
		
		while(keepLooping) {
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		    try {
				String line = br.readLine();
				if(!Utils.isNullOrEmpty(line)){
					commandProcessor(line);
				}
			} catch (IOException e) {
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
		
		Method method = null;
		try {
			 method = Method.valueOf(tokens[0].toUpperCase());
		}
		catch (IllegalArgumentException e) {
			System.out.println("Invalid command!");
			return;
		}
		
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
	private void handleStart(String[] tokens) {
		if (tokens.length != 2) {
			System.out.println("Usage: START <SERVER_IP>");
		}
		else {
			String metaFile = clientManager.getFileStore() + File.separator + Constants.META_SUFFIX;
			if (new File(metaFile).exists()) {
				clientManager.replaceFileMap(Crypto.loadFromDisk(metaFile));
			}
			serverConnection = new ServerConnection(sslContext, tokens[1]);
		}
	}
	
	/**
	 * handle GET command
	 * @param tokens
	 */
	private void handleGet(String[] tokens) {
		if(tokens.length != 2) {
			System.out.println("Usage: GET <fileUID>");
		}
		else if (serverConnection == null) {
			System.out.println("No connection!");
		}
		else {
			String uid = tokens[1];
			if (Constants.META_SUFFIX.equals(uid)) {
				System.out.println("Please try a different name!");
				return;
			}
			
			CommandArgument get = null;
			
			if (clientManager.hasValidDelegationToken(uid, Method.GET)) {
				DelegationToken token = clientManager.getDelegationToken(uid, Method.GET);
				get = new CmdGetFileArgument(uid, token);
			}
			else {
				get = new CmdGetFileArgument(uid, null);
			}
			
			/* blocking calls to server */
			serverConnection.sendCommand(get); 
			CmdGetFileResponse response = (CmdGetFileResponse)serverConnection.readFromServer(Method.GET);
			if (response == null) {
				System.err.println("Couldn't get response from server!");
			}
			else {
				if (response.code == ErrorCode.OK) {
					String fileStorePath = Constants.FILE_FOLDER + File.separator + clientManager.getAlias();
					Utils.writeToFile(fileStorePath + File.separator + uid, response.data);
				} else {
					System.out.println("Error: " + response.code);
				}
			}
		}
	}
	
	/**
	 * handle PUT command
	 * @param tokens
	 */
	private void handlePut(String[] tokens) {
		if (tokens.length != 2) {
			System.out.println("Usage: PUT <fileUID>");
		}
		else if (serverConnection == null) {
			System.out.println("No connection!");
		}
		else {
			String uid = tokens[1];
			if (Constants.META_SUFFIX.equals(uid)) {
				System.out.println("Please try a different name!");
				return;
			}
			
			String fileStorePath = Constants.FILE_FOLDER + File.separator + clientManager.getAlias();
			byte[] fileContents = Utils.readFromFile(fileStorePath + File.separator + uid);
			if (fileContents == null) {
				return;
			}
			
			boolean addFileEntry = false;
			CommandArgument put = null;
			
			if (clientManager.hasValidDelegationToken(uid, Method.PUT)) {
				DelegationToken token = clientManager.getDelegationToken(uid, Method.PUT);
				put = new CmdGetFileArgument(uid, token);
			}
			else {
				put = new CmdPutFileArgument(uid, fileContents, null);
				addFileEntry = true;
			}
			
			/* blocking calls to server */
			serverConnection.sendCommand(put);
			CommandResponse response = serverConnection.readFromServer(Method.PUT);
			
			if (response != null) {
				if (response.code == ErrorCode.OK) {
					if (addFileEntry) {
						clientManager.makeOwner(uid);
					}
				} else {
					System.out.println("Error: " + response.code);
				}
			}
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
			
			DelegationPrimitive primitive = new DelegationPrimitive(clientManager.getAlias(), target, 
					uid, rights.contains(Right.READ), rights.contains(Right.WRITE), 
					rights.contains(Right.DELEGATE), startEpoch, duration);
			
			Certificate certificate = clientManager.getCertificate();
			PrivateKey privateKey = clientManager.getPrivateKey();
			
			DelegationToken token;
			CommandArgument delegate;
			
			clientManager.printAccessList();
			
			if(clientManager.isOwner(uid)){
				token = new DelegationToken(primitive, certificate, null, privateKey);
				delegate = new CmdDelegateRightsArgument(uid, token);
			}
			else if(clientManager.hasValidDelegationToken(uid, Method.DELEGATE)){
				DelegationToken parentToken = clientManager.getDelegationToken(uid, Method.DELEGATE);
				token = new DelegationToken(primitive, certificate, parentToken, privateKey);
				delegate = new CmdDelegateRightsArgument(uid, token);
			}
			else{
				System.out.println("Client has no delegate access to file [" + uid + "]");
				return;
			}

			peerConnection = new PeerConnection(sslContext, host, port);
			peerConnection.sendCommand(delegate);
			CmdDelegateRightsResponse response = (CmdDelegateRightsResponse) peerConnection.readFromServer();
			peerConnection.close();
			
			if (response != null && response.code == ErrorCode.OK) {
				System.out.println("Delated successfully!");
			} else {
				System.out.println("Error while delegating!");
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
		}
		else{
			keepLooping = false;
			clientManager.close();
		}
	}
	
	public void stopConsoleThread() {
		if (serverConnection != null) {
			try {
				serverConnection.close();
			} catch (Exception e) {
			}
		}
	}
}