package com.scs.sdfs.server;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.charset.Charset;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;

import com.scs.sdfs.Constants;
import com.scs.sdfs.SSLHelper;

/**
 * Server thread that listens for incoming requests from clients
 */
public class ServerThread extends Thread{

	private SSLContext sslContext;
	
	public ServerThread(SSLContext sslContext) {
		super();
		this.sslContext = sslContext;
	}

	@Override
	public void run() {
		SSLServerSocketFactory factory = (SSLServerSocketFactory) sslContext.getServerSocketFactory();
		SSLServerSocket serverSocket = null;
		try {
			serverSocket = (SSLServerSocket) factory.createServerSocket(Constants.SERVER_LISTENER_PORT);
			serverSocket.setNeedClientAuth(true);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		SSLSocket socket = null;
		try {
			System.out.println("Server has started");
			while(true){
				socket = (SSLSocket) serverSocket.accept();
				String peerPrincipal = socket.getSession().getPeerPrincipal().getName();
				String peerCN = null;
				if(peerPrincipal != null && !peerPrincipal.trim().isEmpty()){
					peerCN = SSLHelper.getCNFromPrincipal(peerPrincipal);
				}
				
				new ClientConnection(socket, peerCN).start();
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * @param msg received from peer client
	 */
	private void processIncomingMessage(String msg){
		
	}
	
	/**
	 * Handles each connection 
	 */
	class ClientConnection extends Thread {
		public static final int READ_BUFFER_SIZE = 4096;
		SSLSocket socket;
		String peerCN;

		public ClientConnection(SSLSocket socket, String peerCN) {
			super();
			this.socket = socket;
			this.peerCN = peerCN;
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
		}

	}

	public ServerThread() {
		
	}

}
