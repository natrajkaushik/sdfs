package com.scs.sdfs.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import com.google.gson.Gson;
import com.scs.sdfs.Constants;
import com.scs.sdfs.Method;
import com.scs.sdfs.args.CommandArgument;
import com.scs.sdfs.rspns.CmdGetFileResponse;
import com.scs.sdfs.rspns.CmdPutFileResponse;
import com.scs.sdfs.rspns.CommandResponse;

public class ServerConnection {

	private SSLContext sslContext;
	private SSLSocket socket;
	private Gson gson = new Gson();
	
	public ServerConnection(SSLContext sslContext) {
		this.sslContext = sslContext;
		init();
	}
	
	/**
	 * create the socket connection to the server
	 */
	private void init(){
		SSLSocketFactory factory = (SSLSocketFactory) sslContext.getSocketFactory();
		try {
			socket = (SSLSocket) factory.createSocket(Constants.LOCALHOST, Constants.SERVER_LISTENER_PORT);
			socket.setNeedClientAuth(true);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Sends a CommandArgument object to the server
	 * @param argument
	 */
	public void sendCommand(CommandArgument argument){
		DataOutputStream dos = null;		
		try {
			dos = new DataOutputStream(socket.getOutputStream());
			dos.writeUTF(argument.toString());
			dos.flush();
		} catch (IOException e) {
			System.err.println("Couldn't send command to server!");
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
	 * reads response from server
	 * @return CommandResponse
	 */
	public CommandResponse readFromServer(Method method){
		DataInputStream dis = null;
		String data = null;
		try {
			dis = new DataInputStream(socket.getInputStream());
			data = dis.readUTF();
		} catch (IOException e) {
			System.err.println("Couldn't read response from server!");
			e.printStackTrace();
			return null;
		}
		
		switch(method){
		case GET:
			return gson.fromJson(data, CmdGetFileResponse.class);
		case PUT:
			return gson.fromJson(data, CmdPutFileResponse.class);
		default:
			return null;
		}
	}
	
	/**
	 * close the server connection
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