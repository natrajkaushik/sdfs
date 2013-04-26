package com.scs.sdfs.client;

import java.io.IOException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import com.scs.sdfs.Constants;
import com.scs.sdfs.args.CommandArgument;

public class ServerConnection {

	private SSLContext sslContext;
	private SSLSocket socket;
	
	public ServerConnection(SSLContext sslContext) {
		this.sslContext = sslContext;
		init();
	}
	
	/**
	 * creates the socket connection to the server and stores it in 'socket'
	 */
	private void init(){
		SSLSocketFactory factory = (SSLSocketFactory) sslContext.getSocketFactory();
		try {
			socket = (SSLSocket) factory.createSocket(Constants.LOCALHOST, Constants.SERVER_LISTENER_PORT);
			socket.setNeedClientAuth(true);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void sendCommand(CommandArgument argument){
		
	}
	
	/**
	 * reads file contents from socket
	 * @return byte[] contents of file
	 */
	public byte[] readFromServer(){
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
		
		return data;
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
