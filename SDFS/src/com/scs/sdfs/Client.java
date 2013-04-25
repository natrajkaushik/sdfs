package com.scs.sdfs;

/**
 * SDFS Client
 */
public class Client {

	private String name; // name of the client
	private String password;

	/**
	 * creates the listener socket which waits for delegation messages from other clients
	 */
	public void createDelegationListener(){
		new DelegationServer().start();
	}

	/**
	 * creates the console listener
	 */
	public void createConsoleListener(){
		new ConsoleListener().start();
	}

	
	/**
	 * Initializes the Client
	 */
	private void init(){
		String keyStorePath = Constants.KEY_DUMP_FOLDER + "/" + name + ".p12";
		SSLHelper.getSSLHelper(keyStorePath, password).setSSLContext(); // Initialize SSL Context
	}
	
	
	public Client(String name, String password) {
		super();
		this.name = name;
		this.password = password;
		init();
	}



	public static void main(String[] args) {

	}
}