package com.scs.sdfs.client;

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
	 * Sends a CommandResponse object to the server
	 * @param response
	 */
	public void sendResponse(CommandResponse response){
		byte[] toSend = response.toBytes();
		DataOutputStream dos = null;
		
		try {
			dos = new DataOutputStream(socket.getOutputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
			dos.write(toSend);
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
		byte[] data = new byte[0];
		byte[] buffer = new byte[4096];
		int byteCount = -1;
		try {
			while ((byteCount = socket.getInputStream().read(buffer, 0,
					buffer.length)) > -1) {
				byte[] tempBuffer = new byte[data.length + byteCount];
				System.arraycopy(data, 0, tempBuffer, 0, data.length);
				System.arraycopy(buffer, 0, tempBuffer, data.length,
						byteCount);
				data = tempBuffer;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		String _data = new String(data);
		CommandArgument argument = gson.fromJson(_data, CommandArgument.class);
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
