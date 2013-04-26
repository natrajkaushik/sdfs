package com.scs.sdfs;

import java.security.GeneralSecurityException;
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
			keyFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			keyFactory.init(KeyStoreHelper.getKeyStore(path, password, keyStoreType), password.toCharArray());
		} catch (GeneralSecurityException e) {
			System.err.println("Couldn't initialize key factory!");
			e.printStackTrace();
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
			trustedFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
			trustedFactory.init(KeyStoreHelper.getKeyStore(path, password, null));
		} catch (GeneralSecurityException e) {
			System.err.println("Couldn't load trust manager!");
			e.printStackTrace();
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
			sslContext.init(keyManagers, trustManagers, null);
		} catch (GeneralSecurityException e) {
			System.err.println("Couldn't initialize SSL context!");
			e.printStackTrace();
		}
		return sslContext;
	}
	
	/*
	 * sets the SSLContext
	 */
	public SSLContext getSSLContext(){
		KeyManager[] keyManagers = this.getKeyManagers(keyStorePath, Constants.KEY_STORE_TYPE, keyStoreType);
		TrustManager[] trustManagers = this.getTrustManagers(Constants.TRUSTED_STORE_PATH, 
				Constants.TRUSTED_STORE_PASSWORD, Constants.TRUSTED_STORE_TYPE);
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
		return CN.replaceAll(" ", "_");
	}
}