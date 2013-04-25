package com.scs.sdfs.test;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import com.scs.sdfs.KeyStoreHelper;



public class SSLClient {

	private static final String SERVER_ADDRESS = "localhost";
	private static final int SERVER_PORT = 1443;

	private static void connect() {
        
        KeyManagerFactory keyFactory = null;
        try {
			keyFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
		} catch (NoSuchAlgorithmException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
        
        try {
			keyFactory.init(KeyStoreHelper.getKeyStore("./keydump/node_b.p12", "nodeb", "PKCS12"), "nodeb".toCharArray());
		} catch (UnrecoverableKeyException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (KeyStoreException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (NoSuchAlgorithmException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
        
        KeyManager[] keyManagers = keyFactory.getKeyManagers();
        //KeyManager[] keyManagers = null;
        
		TrustManagerFactory trustedFactory = null;
		try {
			trustedFactory = TrustManagerFactory
					.getInstance(TrustManagerFactory.getDefaultAlgorithm());
		} catch (NoSuchAlgorithmException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		try {
			trustedFactory.init(KeyStoreHelper.getKeyStore("./stores/trusted.jks", "server", null));
		} catch (KeyStoreException e3) {
			// TODO Auto-generated catch block
			e3.printStackTrace();
		}
		
        
        TrustManager[] trustManagers = trustedFactory.getTrustManagers();
        
        SSLContext sslContext = null;
		try {
			sslContext = SSLContext.getInstance("TLS");
		} catch (NoSuchAlgorithmException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
        try {
			sslContext.init(keyManagers, trustManagers, null);
		} catch (KeyManagementException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
        //SSLContext.setDefault(sslContext);
		
        
		
		SSLSocketFactory factory = null;
		factory = (SSLSocketFactory) sslContext.getSocketFactory();
		
		SSLSocket sslSocket = null;
		try {
			System.out.println("Creating client socket");
			sslSocket = (SSLSocket) factory.createSocket(SERVER_ADDRESS,
					SERVER_PORT);
			System.out.println("Created client socket");
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		BufferedWriter wr = null;
		try {
			wr = new BufferedWriter(new OutputStreamWriter(
					sslSocket.getOutputStream(), "UTF8"));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			System.out.println("Writing to Server");
			wr.write("Testing SSL Sockets");
			wr.flush();
		} catch (IOException e) {

		}
	}
	
	public static void main(String[] args) {
		connect();
	}

}
