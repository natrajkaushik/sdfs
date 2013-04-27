package com.scs.sdfs.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
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
	
	private static final String PUT_KEY = "\"command\":\"PUT\"";
	
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
		System.out.println("Hi " + peerCN);
		DataInputStream dis = null;
		try {
			dis = new DataInputStream(socket.getInputStream());
		} catch (IOException e) {
			System.err.println("Couldn't connect to client!");
			e.printStackTrace();
			return;
		}
		
		while(!socket.isClosed()) {
			String data = null;
			try {
				data = dis.readUTF();
			}
			catch (EOFException e) {
				System.out.println("Goodbye " + peerCN);
				break;
			}
			catch (IOException e) {
				System.err.println("Couldn't read command from client!");
				e.printStackTrace();
				continue;
			}
			
			CommandArgument argument = null;
			
			try {
				if (data.contains(PUT_KEY)) {
					argument = GSON.fromJson(data, CmdPutFileArgument.class);
				} else {
					argument = GSON.fromJson(data, CmdGetFileArgument.class);
				}
			}
			catch (Exception e) {
				System.err.println("Unknown argument received!");
				continue;
			}
			
			CommandResponse response = processArgument(argument);
			sendResponse(response);
		}
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
			dos.writeUTF(response.toString());
			dos.flush();
		} catch (IOException e) {
			System.err.println("Couldn't send response to client!");
			e.printStackTrace();
		}
	}
}