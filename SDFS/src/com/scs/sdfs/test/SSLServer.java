package com.scs.sdfs.test;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import com.scs.sdfs.KeyStoreHelper;

public class SSLServer {

	public static void start() {
		KeyManagerFactory keyFactory = null;
		try {
			keyFactory = KeyManagerFactory.getInstance(KeyManagerFactory
					.getDefaultAlgorithm());
		} catch (NoSuchAlgorithmException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		try {
			keyFactory.init(KeyStoreHelper.getKeyStore("./keydump/server.p12",
					"server", "PKCS12"), "server".toCharArray());
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
			trustedFactory.init(KeyStoreHelper.getKeyStore(
					"./store/trusted.jks", "server", null));
		} catch (KeyStoreException e3) {
			// TODO Auto-generated catch block
			e3.printStackTrace();
		}

		TrustManager[] trustManagers = trustedFactory.getTrustManagers();
		
		//System.out.println(trustManagers.length + "\n" + keyManagers.length);

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

		
		SSLServerSocketFactory factory = null;
		factory = (SSLServerSocketFactory) sslContext.getServerSocketFactory();
		
		SSLServerSocket serverSocket = null;
		try {
			serverSocket = (SSLServerSocket) factory.createServerSocket(1443);
			serverSocket.setNeedClientAuth(true);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		SSLSocket s = null;
		try {
			System.out.println("Server has started");
			s = (SSLSocket) serverSocket.accept();
			SSLSession session = s.getSession();
			String peerName = session.getPeerPrincipal().getName();
			System.out.println("Host: " + session.getPeerHost());
			System.out.println("Local Principal: " + session.getLocalPrincipal().getName());
			System.out.println("Peer Connecting: " + peerName);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		BufferedInputStream is = null;
		try {
			is = new BufferedInputStream(s.getInputStream());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		byte buffer[] = new byte[4096];


		try {
				is.read(buffer);
				System.out.println(new String(buffer));
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			s.close();
			serverSocket.close();
		} catch (IOException e) {

		}
	}

	public static void main(String[] args) {
		start();
	}

}
