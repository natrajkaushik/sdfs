package com.scs.sdfs;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;

import javax.net.ssl.SSLServerSocketFactory;


public class DelegationServer extends Thread{

	@Override
	public void run() {
		SSLServerSocketFactory factory = (SSLServerSocketFactory) SSLServerSocketFactory
				.getDefault();
		ServerSocket serverSocket = null;
		try {
			serverSocket = factory.createServerSocket(Constants.SERVER_LISTENER_PORT);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Socket socket = null;
		try {
			System.out.println("Server has started");
			while(true){
				socket = serverSocket.accept();
				new ClientListener(socket).start();
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * @param msg received from another client (delegation)
	 */
	private void processIncomingMessage(String msg){
		
	}
	
	/**
	 * Handles each connection 
	 */
	class ClientListener extends Thread {
		public static final int READ_BUFFER_SIZE = 4096;
		Socket socket;

		public ClientListener(Socket socket) {
			super();
			this.socket = socket;
		}

		/**
		 * reads from the socket
		 */
		public void run() {
			BufferedInputStream is = null;
			try {
				is = new BufferedInputStream(socket.getInputStream());
			} catch (IOException e) {
				e.printStackTrace();
			}

			byte buffer[] = new byte[READ_BUFFER_SIZE];
			String received = null;
			try {
				is.read(buffer);
				received = new String(buffer);
				Charset.forName("UTF-8").encode(received);
				processIncomingMessage(received);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			try{
				if(is != null){
					is.close();
				}
			}catch(IOException e){
				e.printStackTrace();
			}

			try {
				if(socket != null){
					socket.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	
}
