package com.scs.sdfs.server;

import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.Certificate;

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
				
				ClientConnection clientConnection = new ClientConnection(socket, peerCN);
				clientConnection.start();
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Initializes the Server
	 */
	private void init(){
		String keyStorePath = Constants.KEY_DUMP_FOLDER + "/server.p12";
		sslContext = SSLHelper.getSSLHelper(keyStorePath, password).getSSLContext(); // Initialize SSL Context
		
		boolean success = Crypto.init(password, getRootCertificate(), Constants.SERVER_ALIAS, Constants.SERVER_ALIAS + ".p12");
		if(!success){
			System.err.println("Unable to initialize Crypto");
			System.exit(1);
		}
		
		runServer();
	}
	
	/**
	 * @return Root CA's public key object
	 */
	private Certificate getRootCertificate(){
		KeyStore trustedKeyStore = KeyStoreHelper.getKeyStore(Constants.TRUSTED_STORE_PATH, 
				Constants.TRUSTED_STORE_PASSWORD, Constants.TRUSTED_STORE_TYPE);
		Certificate ROOT_CERTIFICATE = null;
		try {
			ROOT_CERTIFICATE = trustedKeyStore.getCertificate(Constants.ROOT_ALIAS);
		} catch (KeyStoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ROOT_CERTIFICATE;
	}
	
	
	public Server(String password) {
		super();
		this.password = password;
		init();
	}
	
}