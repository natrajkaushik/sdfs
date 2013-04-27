package com.scs.sdfs.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import com.google.gson.Gson;
import com.scs.sdfs.args.CommandArgument;
import com.scs.sdfs.rspns.CmdDelegateRightsResponse;
import com.scs.sdfs.rspns.CommandResponse;

/**
 * Handles the client connection for delegation requests 
 */
public class PeerConnection {

	private SSLContext sslContext;
	private String host;
	private int port;
	private SSLSocket socket;
	private static final Gson GSON = new Gson();
	
	public PeerConnection(SSLContext sslContext, String host, int port) {
		this.sslContext = sslContext;
		this.host = host;
		this.port = port;
		init();
	}
	
	/**
	 * create the socket connection to client host:port
	 */
	private void init(){
		SSLSocketFactory factory = (SSLSocketFactory) sslContext.getSocketFactory();
		try {
			socket = (SSLSocket) factory.createSocket(host, port);
			socket.startHandshake();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Sends a CommandArgument object to the client
	 * @param argument
	 */
	public void sendCommand(CommandArgument argument){
		DataOutputStream dos = null;		
		try {
			dos = new DataOutputStream(socket.getOutputStream());
			dos.writeUTF(argument.toString());
			dos.flush();
		} catch (IOException e) {
			System.err.println("Unable to send command to peer!");
			e.printStackTrace();
		}
	}
	
	/**
	 * reads response from peer
	 * @return CommandResponse
	 */
	public CommandResponse readFromServer(){
		DataInputStream dis = null;
		String data = null;
		try {
			dis = new DataInputStream(socket.getInputStream());
			data = dis.readUTF();
			return GSON.fromJson(data, CmdDelegateRightsResponse.class);
		} catch (IOException e) {
			System.err.println("Unable to read peer response!");
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * close the client connection
	 */
	public void close(){
		try {
			if (socket != null) {
				socket.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}