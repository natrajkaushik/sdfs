package com.scs.sdfs.client;

import java.io.IOException;

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
	
	
	public DelegationServerThread(SSLContext sslContext, int port) {
		super();
		this.sslContext = sslContext;
		this.port = port;
	}

	@Override
	public void run() {
		SSLServerSocketFactory factory = (SSLServerSocketFactory) sslContext.getServerSocketFactory();
		SSLServerSocket serverSocket = null;
		try {
			serverSocket = (SSLServerSocket) factory.createServerSocket(port);
			serverSocket.setNeedClientAuth(true);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		SSLSocket socket = null;
		try {
			System.out.println("Delegation Server has started");
			while(true){
				socket = (SSLSocket) serverSocket.accept();
				String peerPrincipal = socket.getSession().getPeerPrincipal().getName();
				String peerCN = null;
				if(peerPrincipal != null && !peerPrincipal.trim().isEmpty()){
					peerCN = SSLHelper.getCNFromPrincipal(peerPrincipal);
				}
				
				delegationConnection = new DelegationConnection(socket, peerCN);
				delegationConnection.processRequest();
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
