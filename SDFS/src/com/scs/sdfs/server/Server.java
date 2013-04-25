package com.scs.sdfs.server;

import javax.net.ssl.SSLContext;

import com.scs.sdfs.Constants;
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
	}
	
	
	public Server(String password) {
		super();
		this.password = password;
		init();
	}
	
}