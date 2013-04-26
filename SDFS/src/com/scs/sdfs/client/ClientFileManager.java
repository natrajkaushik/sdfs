package com.scs.sdfs.client;

import java.util.HashMap;
import java.util.Map;

import com.scs.sdfs.client.ConsoleListener.Methods;
import com.scs.sdfs.delegation.DelegationToken;

public class ClientFileManager {

	private static ClientFileManager clientFileManager = null;
	
	// contains mappings between file UIDs and access rights (null or DelegationTokens)
	private Map<String, DelegationToken> FILE_ACCESS_RIGHTS_TABLE;
	
	public static ClientFileManager getClientFileManager(String alias, String password){
		if(clientFileManager == null){
			clientFileManager = new ClientFileManager(alias, password);
		}
		return clientFileManager;
	}
	
	public static ClientFileManager getClientFileManager(){
		return clientFileManager;
	}
	
	private String alias;
	private String password;
	
	private ClientFileManager(String alias, String password){
		this.alias = alias;
		this.password = password;
		FILE_ACCESS_RIGHTS_TABLE = new HashMap<String, DelegationToken>();
	}
	
	public String getAlias(){
		return alias;
	}
	
	public String getPassword(){
		return password;
	}
	
	/**
	 * @param uid makes client owner of file (uid)
	 */
	public synchronized void makeOwner(String uid){
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
	public boolean hasValidDelegationToken(String uid, Methods method){
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
	public DelegationToken getDelegationToken(String uid, Methods method){
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

}
