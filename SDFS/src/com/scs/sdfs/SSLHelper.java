package com.scs.sdfs;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

	public static final String TRUSTED_STORE_PATH = "./store/trusted.jks";
	public static final String TRUSTED_STORE_PASSWORD = "server";
	public static final String TRUSTED_STORE_TYPE = "JKS";
	
	public static final String KEY_STORE_TYPE = "PKCS12";
	
	// KeyStore parameters
	String keyStorePath, keyStoreType, keyStorePass;
	
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
	public SSLContext getSSLContext(KeyManager[] keyManagers, TrustManager[] trustManagers){
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
		return sslContext;
	}
	
	/*
	 * sets the SSLContext
	 */
	public SSLContext getSSLContext(){
		KeyManager[] keyManagers = this.getKeyManagers(keyStorePath, KEY_STORE_TYPE, keyStoreType);
		TrustManager[] trustManagers = this.getTrustManagers(TRUSTED_STORE_PATH, TRUSTED_STORE_PASSWORD, TRUSTED_STORE_TYPE);
		return this.getSSLContext(keyManagers, trustManagers);
	}
	
	/**
	 * 
	 * @param keyStorePath
	 * @param keyStorePass
	 * @return SSLHelper
	 */
	public static SSLHelper getSSLHelper(String keyStorePath, String keyStorePass){
		return new SSLHelper(keyStorePath, keyStorePass);
	}

	private SSLHelper(String keyStorePath, String keyStorePass) {
		super();
		this.keyStorePath = keyStorePath;
		this.keyStorePass = keyStorePass;
	}
	
	/**
	 * @param principal of peer
	 * @return CN
	 */
	public static String getCNFromPrincipal(String principal){
		Pattern pattern  = Pattern.compile("CN=([^\\,]*)");
		Matcher matcher = pattern.matcher(principal);
		String CN = matcher.find() ? matcher.group(1) : null;
		return CN;
	}

}
