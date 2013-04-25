package com.scs.sdfs;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

/**
 * 
 * Does all the processing required to establish two way SSL sockets by setting up the KeyStore and TrustedStore
 */
public class SSLHelper {

	// KeyStore parameters
	String keyStorePath, keyStoreType, keyStorePass;
	
	// TrustedStore parameters
	String trustedStorePath, trustedStoreType, trustedStorePass;
	
	/**
	 * 
	 * @param path
	 * @param password
	 * @param keyStoreType
	 * @return Array of KeyManager objects
	 */
	public KeyManager[] getKeyManagers(String path, String password, String keyStoreType){
		KeyManagerFactory keyFactory = null;
		try {
			keyFactory = KeyManagerFactory.getInstance(KeyManagerFactory
					.getDefaultAlgorithm());
		} catch (NoSuchAlgorithmException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		try {
			keyFactory.init(KeyStoreHelper.getKeyStore(path,
					password, keyStoreType), password.toCharArray());
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
		
		return keyManagers;
	}
	
	/**
	 * 
	 * @param path
	 * @param password
	 * @param keyStoreType
	 * @return
	 */
	public TrustManager[] getTrustManagers(String path, String password, String keyStoreType){
		TrustManagerFactory trustedFactory = null;
		try {
			trustedFactory = TrustManagerFactory
					.getInstance(TrustManagerFactory.getDefaultAlgorithm());
		} catch (NoSuchAlgorithmException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		try {
			trustedFactory.init(KeyStoreHelper.getKeyStore(path, password, null));
		} catch (KeyStoreException e3) {
			// TODO Auto-generated catch block
			e3.printStackTrace();
		}

		TrustManager[] trustManagers = trustedFactory.getTrustManagers();
		return trustManagers;
	}
	
	/**
	 * sets the SSL context
	 * @param keyManagers
	 * @param trustManagers
	 */
	public void setSSLContext(KeyManager[] keyManagers, TrustManager[] trustManagers){
		SSLContext sslContext = null;
		try {
			sslContext = SSLContext.getInstance("SSL");
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
		SSLContext.setDefault(sslContext);
	}
	
	
	public void setSSLContext(){
		KeyManager[] keyManagers = this.getKeyManagers(keyStorePath, keyStorePass, keyStoreType);
		TrustManager[] trustManagers = this.getTrustManagers(trustedStorePath, trustedStorePass, trustedStoreType);
		this.setSSLContext(keyManagers, trustManagers);
	}
	
	/**
	 * 
	 * @param keyStorePath
	 * @param keyStoreType
	 * @param keyStorePass
	 * @param trustedStorePath
	 * @param trustedStoreType
	 * @param trustedStorePass
	 * @return SSLHelper
	 */
	public static SSLHelper getSSLHelper(String keyStorePath, String keyStoreType,
			String keyStorePass, String trustedStorePath,
			String trustedStoreType, String trustedStorePass){
		return new SSLHelper(keyStorePath, keyStoreType, keyStorePass, 
				trustedStorePath, trustedStoreType, trustedStorePass);
	}

	private SSLHelper(String keyStorePath, String keyStoreType,
			String keyStorePass, String trustedStorePath,
			String trustedStoreType, String trustedStorePass) {
		super();
		this.keyStorePath = keyStorePath;
		this.keyStoreType = keyStoreType;
		this.keyStorePass = keyStorePass;
		this.trustedStorePath = trustedStorePath;
		this.trustedStoreType = trustedStoreType;
		this.trustedStorePass = trustedStorePass;
	}	

}
