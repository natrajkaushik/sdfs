package com.scs.sdfs.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import javax.net.ssl.SSLSocket;

import com.google.gson.Gson;
import com.scs.sdfs.args.CmdGetFileArgument;
import com.scs.sdfs.args.CmdPutFileArgument;
import com.scs.sdfs.args.CommandArgument;
import com.scs.sdfs.rspns.CommandResponse;

/**
 * Handles each client connection on the server 
 */
public class ClientConnection extends Thread{
	SSLSocket socket;
	String peerCN;
	private static final Gson GSON = new Gson();

	public ClientConnection(SSLSocket socket, String peerCN) {
		super();
		this.socket = socket;
		this.peerCN = peerCN;
	}

	/**
	 * reads from the socket
	 */
	public void run() {
		DataInputStream dis = null;
		try {
			dis = new DataInputStream(socket.getInputStream());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		while(!socket.isClosed()) {
			String data = null;
			try {
				data = dis.readUTF();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			CommandArgument argument = GSON.fromJson(data, CommandArgument.class);
			CommandResponse response = processArgument(argument);
			sendResponse(response);
		}
		
		System.out.println("Closing client connection thread...");
	}
	
	private CommandResponse processArgument(CommandArgument argument){
		CommandResponse response = null;
		switch(argument.command) {
		case START:
			System.err.println("Invalid start command received!");
			break;
		case GET:
			response = FileManager.getInstance().commandGetFile(peerCN, (CmdGetFileArgument) argument);
			break;
		case PUT:
			response = FileManager.getInstance().commandPutFile(peerCN, (CmdPutFileArgument) argument);
			break;
		case CLOSE:
			// TODO Handle close connection
			break;
		default:
			System.err.println("Invalid command received!");
		}
		return response;
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

}
