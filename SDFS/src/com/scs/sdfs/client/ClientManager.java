package com.scs.sdfs.client;

import java.io.File;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.scs.sdfs.Constants;
import com.scs.sdfs.KeyStoreHelper;
import com.scs.sdfs.Method;
import com.scs.sdfs.delegation.DelegationToken;
import com.scs.sdfs.server.Crypto;
import com.scs.sdfs.server.MetaFile;

public class ClientManager {
	
	private static final Gson GSON = new Gson();

	private static ClientManager clientManager = null;
	private String alias;
	private String password;
	
	private DelegationServerThread delegator;
	private ConsoleListener console;
	
	private Certificate certificate;
	private PrivateKey privateKey;
	
	private String fileStorePath;
	
	// contains mappings between file UIDs and access rights (null or DelegationTokens)
	private Map<String, DelegationToken> FILE_ACCESS_RIGHTS_TABLE;
	
	public static ClientManager getClientManager(String alias, String password) {
		if(clientManager == null){
			clientManager = new ClientManager(alias, password);
		}
		return clientManager;
	}
	
	public static ClientManager getClientManager(){
		return clientManager;
	}
	
	private ClientManager(String alias, String password){
		this.alias = alias;
		this.password = password;
		FILE_ACCESS_RIGHTS_TABLE = new HashMap<String, DelegationToken>();
		certificate = KeyStoreHelper.getCertificate(alias, password);
		privateKey = KeyStoreHelper.getPrivateKey(alias, password);
		
		fileStorePath = Constants.FILE_FOLDER + File.separator + alias;
		if (!new File(fileStorePath).exists()) {
			new File(fileStorePath).mkdirs();
		}
	}
	
	public String getAlias(){
		return alias;
	}
	
	public String getPassword(){
		return password;
	}
	
	public Certificate getCertificate() {
		return certificate;
	}

	public PrivateKey getPrivateKey() {
		return privateKey;
	}
	
	public String getFileStore() {
		return fileStorePath;
	}
	
	public void setDelegationThread(DelegationServerThread thread) {
		delegator = thread;
	}
	
	public void setConsoleListener(ConsoleListener thread) {
		console = thread;
	}

	/**
	 * @param uid makes client owner of file (uid)
	 */
	public synchronized void makeOwner(String uid) {
		FILE_ACCESS_RIGHTS_TABLE.put(uid, null);
	}
	
	/**
	 * adds delegation token to rights
	 * @param uid
	 * @param token
	 */
	public synchronized void addDelegationToken(String uid, DelegationToken token){
		if(FILE_ACCESS_RIGHTS_TABLE.containsKey(uid)){
			Object value = FILE_ACCESS_RIGHTS_TABLE.get(uid);
			if(value != null){
				FILE_ACCESS_RIGHTS_TABLE.put(uid, token);
			}else{
				//dont do anything as client is owner of file
			}
		}
		else{
			FILE_ACCESS_RIGHTS_TABLE.put(uid, token);
		}
	}
	
	public boolean isNewFile(String uid) {
		return !FILE_ACCESS_RIGHTS_TABLE.containsKey(uid);
	}
	
	/**
	 * @param uid of file
	 * @return if Client is an owner of file
	 */
	public boolean isOwner(String uid){
		return (FILE_ACCESS_RIGHTS_TABLE.containsKey(uid) && (FILE_ACCESS_RIGHTS_TABLE.get(uid) == null));
	}
	
	/**
	 * @param uid of file
	 * @return if Client has valid delegation rights
	 */
	public boolean hasValidDelegationToken(String uid, Method method){
		if(FILE_ACCESS_RIGHTS_TABLE.containsKey(uid) && !isOwner(uid)){
			DelegationToken token = FILE_ACCESS_RIGHTS_TABLE.get(uid);
			if(hasTokenExpired(token)){
				FILE_ACCESS_RIGHTS_TABLE.remove(uid);
				return false;
			}
			else if(hasTokenNotArrived(token)){
				return false;
			}
			else{
				switch(method){
				case GET:
					return token.primitive.canRead;
				case PUT:
					return token.primitive.canWrite;
				case DELEGATE:
				case _DELEGATE:
					return token.primitive.canDelegate;
				default:
					System.err.println("Invalid command received on client!");
				}
				return true;
			}
		}
		return false;
	}
	
	private boolean hasTokenNotArrived(DelegationToken token) {
		long current = System.currentTimeMillis();
		return current < token.primitive.startEpoch;
	}

	/**
	 * 
	 * @param uid
	 * @param method
	 * @return
	 */
	public DelegationToken getDelegationToken(String uid, Method method){
		if(hasValidDelegationToken(uid, method)){
			return FILE_ACCESS_RIGHTS_TABLE.get(uid);
		}
		else{
			return null;
		}
	}
	
	/**
	 * 
	 * @param token
	 * @return if token has expired
	 */
	private boolean hasTokenExpired(DelegationToken token){
		long current = System.currentTimeMillis();
		return current > token.primitive.endEpoch;
	}
	
	public byte[] getSerializedFileMap() {
		return GSON.toJson(FILE_ACCESS_RIGHTS_TABLE).getBytes();
	}
	
	public void replaceFileMap(byte[] data) {
		if (data != null && data.length > 0) {
			FILE_ACCESS_RIGHTS_TABLE = GSON.fromJson(new String(data), 
					new TypeToken<HashMap<String, MetaFile>>(){}.getType());
		}
	}
	
	public void close() {
		delegator.stopServerThread();
		console.stopConsoleThread();
		String metaFile = fileStorePath + File.separator + Constants.META_SUFFIX;
		Crypto.saveToDisk(metaFile, clientManager.getSerializedFileMap(), true);
	}
	
	public void printAccessList() {
		Set<String> uids = FILE_ACCESS_RIGHTS_TABLE.keySet();
		Iterator<String> itr = uids.iterator();
		while (itr.hasNext()) {
			String uid = itr.next();
			System.out.println(uid + " -> " +
			((FILE_ACCESS_RIGHTS_TABLE.get(uid) == null)
					? "Owned!" : (FILE_ACCESS_RIGHTS_TABLE.get(uid).primitive.source)));
		}
	}
}