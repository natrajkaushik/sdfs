package com.scs.sdfs.server;

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.Certificate;

import javax.net.ssl.SSLContext;

import com.scs.sdfs.Constants;
import com.scs.sdfs.KeyStoreHelper;
import com.scs.sdfs.SSLHelper;

public class Server {

	private String password;
	private SSLContext sslContext;
	
	/**
	 * creates the server thread
	 */
	private void createServerThread(){
		new ServerThread(sslContext).start();
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