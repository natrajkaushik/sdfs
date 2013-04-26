package com.scs.sdfs.client;

import java.io.File;

import javax.net.ssl.SSLContext;

import com.scs.sdfs.Constants;
import com.scs.sdfs.SSLHelper;
import com.scs.sdfs.server.Crypto;

/**
 * SDFS Client
 */
public class Client {

	private String alias; // name of the client
	private String password; // password to decrypt the pkcs12 file of the client
	private int port; //port number on which client is listening for delegation requests
	
	private SSLContext sslContext;
	
	/**
	 * creates the listener socket which waits for delegation messages from other clients
	 */
	public void createDelegationServer(){
		DelegationServerThread delServThread = new DelegationServerThread(sslContext, port);
		ClientManager.getClientManager().setDelegationThread(delServThread);
		delServThread.start();
	}

	/**
	 * creates the console listener
	 */
	public void createConsoleListener(){
		ConsoleListener console = new ConsoleListener(sslContext);
		ClientManager.getClientManager().setConsoleListener(console);
		console.start();
	}
	
	/**
	 * Initializes the Client
	 */
	private void init(){
		String keyStorePath = Constants.KEY_DUMP_FOLDER + File.separator + alias + ".p12";
		sslContext = SSLHelper.getSSLHelper(keyStorePath, password).getSSLContext(); // Initialize SSL Context
		
		boolean success = Crypto.init(password, null, alias, alias + ".p12");
		if(!success){
			System.err.println("Unable to initialize Crypto");
			System.exit(1);
		}
		
		ClientManager.getClientManager(alias, password);
		
		createDelegationServer();
		createConsoleListener();
	}
	
	private void addShutdownHook() {
		Runtime.getRuntime().addShutdownHook(new Thread(){
			public void run() {
				System.out.println("Shutting down...");
				ClientManager.getClientManager().close();
			}
		});
	}
	
	public Client(String alias, int port, String password) {
		this.alias = alias;
		this.password = password;
		this.port = port;
		addShutdownHook();
		init();
	}

	/**
	 * @param args
	 * args[0] alias
	 * args[1] port
	 * args[2] password
	 */
	public static void main(String[] args) {
		if(args.length != 3){
			System.out.println("java Client <alias> <port> <password>");
		}else{
			String alias = args[0];
			int port = Integer.parseInt(args[1]);
			String password = args[2];
			new Client(alias, port, password);
		}
	}
}