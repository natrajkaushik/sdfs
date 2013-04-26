package com.scs.sdfs.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import com.google.gson.Gson;
import com.scs.sdfs.args.CommandArgument;
import com.scs.sdfs.client.ConsoleListener.Methods;
import com.scs.sdfs.rspns.CmdGetFileResponse;
import com.scs.sdfs.rspns.CmdPutFileResponse;
import com.scs.sdfs.rspns.CommandResponse;

/**
 * Handles the client connection for delegation requests 
 */
public class ClientConnection {

	private SSLContext sslContext;
	private String host;
	private int port;
	private SSLSocket socket;
	private Gson gson = new Gson();
	
	public ClientConnection(SSLContext sslContext, String host, int port) {
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
			socket.setNeedClientAuth(true);
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
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
			dos.writeUTF(argument.toString());
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
	 * reads response from client
	 * @return CommandResponse
	 */
	public CommandResponse readFromServer(Methods method){
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
		
		switch(method){
		case DELEGATE:
			return gson.fromJson(data, CmdGetFileResponse.class);
		case _DELEGATE:
			return gson.fromJson(data, CmdPutFileResponse.class);
		default:
			return null;
		}
		
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


}
