package com.scs.sdfs;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Enumeration;

import java.security.cert.Certificate;

/**
 * Contains methods to access JAVA KeyStores and TrustedStores
 */
public class KeyStoreHelper {
	
	/**
	 * @param pathToKeyStore path to KeyStore file
	 * @param password KeyStore password
	 * @param keyStoreType store type .. could be PKCS12 or JKS
	 * @return KeyStore object in memory
	 */
	public static KeyStore getKeyStore(String pathToKeyStore, String password, String keyStoreType){
		// open inputstream to read from KeyStore
		FileInputStream keyStoreStream = null;
		try {
			keyStoreStream = new FileInputStream(pathToKeyStore);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		char[] keyStorePassword = password.toCharArray();
		
		KeyStore keyStore = null;
		try {
			String _keyStoreType = keyStoreType == null || keyStoreType.trim().isEmpty() 
					? KeyStore.getDefaultType() : keyStoreType;
			keyStore = KeyStore.getInstance(_keyStoreType);
		} catch (KeyStoreException e) {
			e.printStackTrace();
		}
		
		try {
			keyStore.load(keyStoreStream, keyStorePassword);
			Enumeration<String> aliases = keyStore.aliases();
			while(aliases.hasMoreElements()){
				System.out.println(aliases.nextElement());
			}
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (CertificateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (KeyStoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return keyStore;
	}
	
	public static Certificate getCertificate(String alias, String password){
		KeyStore keyStore = getKeyStore(Constants.KEY_DUMP_FOLDER + "/" + alias + ".p12", password, Constants.KEY_STORE_TYPE);		
		try {
			return keyStore.getCertificate(alias);
		} catch (KeyStoreException e) {
			return null;
		}
	}
	
	public static PrivateKey getPrivateKey(String alias, String password){
		KeyStore keyStore = getKeyStore(Constants.KEY_DUMP_FOLDER + "/" + alias + ".p12", password, Constants.KEY_STORE_TYPE);		
		
			try {
				return (PrivateKey)keyStore.getKey(alias, password.toCharArray());
			} catch (UnrecoverableKeyException e) {
				e.printStackTrace();
				return null;
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
				return null;
			} catch (KeyStoreException e){
				e.printStackTrace();
				return null;
			}
	}
	
	
	public static void main(String[] args) {
		//addCertificate(NODE_A);
		//displayProviders();
		//getCACerts();
		//getKeyStore("./keydump/node_a.p12", "nodea", "PKCS12");
		getKeyStore("./stores/trusted.jks", "server", "JKS");
	}

}
