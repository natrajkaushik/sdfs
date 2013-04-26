package com.scs.sdfs.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import javax.net.ssl.SSLSocket;

import com.google.gson.Gson;
import com.scs.sdfs.args.CommandArgument;
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
		DataOutputStream dos = null;		
		try {
			dos = new DataOutputStream(socket.getOutputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
			dos.writeUTF(response.toString());
			dos.flush();
		} catch (IOException e) {
			
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
		try {
			dis = new DataInputStream(socket.getInputStream());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		String data = null;
		try {
			data = dis.readUTF();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		CommandArgument argument = gson.fromJson(data, CommandArgument.class);
		return argument;
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
		return null;
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
