package com.scs.sdfs.server;

import java.io.File;
import java.io.IOException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;

import com.scs.sdfs.Constants;
import com.scs.sdfs.KeyStoreHelper;
import com.scs.sdfs.SSLHelper;

public class Server{

	private String password;
	private SSLContext sslContext;
	
	
	public void runServer() {
		SSLServerSocketFactory factory = (SSLServerSocketFactory) sslContext.getServerSocketFactory();
		SSLServerSocket serverSocket = null;
		try {
			serverSocket = (SSLServerSocket) factory.createServerSocket(Constants.SERVER_LISTENER_PORT);
			serverSocket.setNeedClientAuth(true);
		} catch (IOException e) {
			System.err.println("Couldn't start server!");
			e.printStackTrace();
			return;
		}
		
		SSLSocket socket = null;
		try {
			System.out.println("Server has started");
			while(true) {
				socket = (SSLSocket) serverSocket.accept();
				String peerPrincipal = socket.getSession().getPeerPrincipal().getName();
				String peerCN = null;
				if(peerPrincipal != null && !peerPrincipal.trim().isEmpty()){
					peerCN = SSLHelper.getCNFromPrincipal(peerPrincipal);
				}
				
				ClientConnection clientConnection = new ClientConnection(socket, peerCN);
				clientConnection.start();
			}
		} catch (IOException e) {
			// e.printStackTrace();
			System.err.println("Server closing...");
		}
	}
	
	/**
	 * Initializes the Server
	 */
	private void init(){
		String keyStorePath = Constants.KEY_DUMP_FOLDER + File.separator + Constants.SERVER_KEY_STORE;
		sslContext = SSLHelper.getSSLHelper(keyStorePath, password).getSSLContext(); // Initialize SSL Context
		
		boolean success = Crypto.init(password, KeyStoreHelper.getRootCertificate(), 
									Constants.SERVER_ALIAS, Constants.SERVER_KEY_STORE);
		if(!success){
			System.err.println("Unable to initialize Crypto");
			System.exit(1);
		}
		
		FileManager.getInstance().init();
		runServer();
	}
	
	private void addShutdownHook() {
		Runtime.getRuntime().addShutdownHook(new Thread(){
			public void run() {
				System.out.println("Shutting down...");
				FileManager.getInstance().wrapUp();
			}
		});
	}
		
	public Server(String password) {
		this.password = password;
		addShutdownHook();
		init();
	}
	
	public static void main(String[] args) {
		if(args.length != 1){
			System.out.println("java Server <password>");
		}else{
			String password = args[0];
			new Server(password);
		}
	}
}