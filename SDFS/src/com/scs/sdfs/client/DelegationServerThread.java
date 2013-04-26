package com.scs.sdfs.client;

import java.io.IOException;
import java.net.SocketException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;

import com.scs.sdfs.SSLHelper;

/**
 * Delegation Server on the client which handles incoming delegation messages
 */
public class DelegationServerThread extends Thread{

	private SSLContext sslContext;
	private int port;
	private DelegationConnection delegationConnection;
	
	private SSLServerSocket serverSocket = null;
	private boolean keepLooping = true;
	
	public DelegationServerThread(SSLContext sslContext, int port) {
		super();
		this.sslContext = sslContext;
		this.port = port;
	}

	@Override
	public void run() {
		SSLServerSocketFactory factory = (SSLServerSocketFactory) sslContext.getServerSocketFactory();
		try {
			serverSocket = (SSLServerSocket) factory.createServerSocket(port);
			serverSocket.setNeedClientAuth(true);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		
		SSLSocket socket = null;
		try {
			System.out.println("Delegation Server has started");
			while(keepLooping) {
				socket = (SSLSocket) serverSocket.accept();
				String peerPrincipal = socket.getSession().getPeerPrincipal().getName();
				String peerCN = null;
				if(peerPrincipal != null && !peerPrincipal.trim().isEmpty()){
					peerCN = SSLHelper.getCNFromPrincipal(peerPrincipal);
				}
				
				delegationConnection = new DelegationConnection(socket, peerCN);
				delegationConnection.processRequest();
			}
		} catch (SocketException e) {
			// stopping server socket
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void stopServerThread() {
		keepLooping = false;
		try {
			if (serverSocket != null) {
				serverSocket.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}