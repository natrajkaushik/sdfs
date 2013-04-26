package com.scs.sdfs.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import javax.net.ssl.SSLSocket;

import com.google.gson.Gson;
import com.scs.sdfs.args.CmdDelegateRightsArgument;
import com.scs.sdfs.args.CommandArgument;
import com.scs.sdfs.rspns.CmdDelegateRightsResponse;
import com.scs.sdfs.rspns.CommandResponse;

public class DelegationConnection {

	private SSLSocket socket;
	private String peerCN;
	private Gson gson = new Gson();
	
	public DelegationConnection(SSLSocket sslSocket, String peerCN) {
		this.socket = sslSocket;
		this.peerCN = peerCN;
	}
	
	/**
	 * Sends a CommandResponse object to the client
	 * @param response
	 */
	public void sendResponse(CommandResponse response){
		if (response == null) {
			return;
		}
		
		DataOutputStream dos = null;
		try {
			dos = new DataOutputStream(socket.getOutputStream());
			dos.writeUTF(response.toString());
			dos.flush();
		} catch (IOException e) {
			System.err.println("Unable to send response!");
			e.printStackTrace();
		}
		
		if(dos != null){
			try {
				dos.close();
			} catch (IOException e) {
			}
		}
	}
	
	/**
	 * reads CommandArgument from client
	 * @return CommandArgument
	 */
	public CommandArgument readFromClient(){
		DataInputStream dis = null;
		String data = null;
		try {
			dis = new DataInputStream(socket.getInputStream());
			data = dis.readUTF();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		
		return gson.fromJson(data, CommandArgument.class);
	}
	
	/**
	 * process a client delegate/delegate* request
	 */
	public void processRequest(){
		CommandArgument argument = readFromClient();
		CommandResponse response = processArgument(argument);
		sendResponse(response);
	}
	
	/**
	 * @param argument
	 * @return process CommandArgument and return CommandResponse
	 */
	private CommandResponse processArgument(CommandArgument argument){
		if (argument == null) {
			return null;
		}
		CommandResponse response = null;
		switch(argument.command) {
		case DELEGATE:
		case _DELEGATE:
			CmdDelegateRightsArgument delArg = (CmdDelegateRightsArgument) argument;
			ClientManager clientManager = ClientManager.getClientManager();
			if (peerCN.equals(delArg.token.primitive.source) && 
				clientManager.getAlias().equals(delArg.token.primitive.target)) {
				clientManager.addDelegationToken(delArg.uid, delArg.token);
			}
			response = new CmdDelegateRightsResponse();
			break;
		default:
			System.err.println("Invalid command received!");
		}
		return response;
	}
	
	/**
	 * close the connection
	 */
	public void close() {
		try {
			if (socket != null) {
				socket.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}