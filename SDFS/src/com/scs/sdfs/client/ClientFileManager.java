package com.scs.sdfs.client;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.scs.sdfs.delegation.DelegationToken;

public class ClientFileManager {

	private static ClientFileManager clientFileManager;
	
	// contains mappings between file UIDs and access rights (null or DelegationTokens)
	private Map<String, Set<DelegationToken>> FILE_ACCESS_RIGHTS_TABLE;
	
	public static ClientFileManager getClientFileManager(String alias){
		if(clientFileManager == null){
			clientFileManager = new ClientFileManager(alias);
		}
		return clientFileManager;
	}
	
	private String alias;
	
	private ClientFileManager(String alias){
		this.alias = alias;
		FILE_ACCESS_RIGHTS_TABLE = new HashMap<String, Set<DelegationToken>>();
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
		Set<DelegationToken> tokenSet;
		if(FILE_ACCESS_RIGHTS_TABLE.containsKey(uid)){
			tokenSet = FILE_ACCESS_RIGHTS_TABLE.get(uid);
			if(tokenSet == null){
				tokenSet = new HashSet<DelegationToken>();
				tokenSet.add(token);
			}
			else{
				tokenSet.add(token);
			}
		}
		else{
			tokenSet = new HashSet<DelegationToken>();
			tokenSet.add(token);
			FILE_ACCESS_RIGHTS_TABLE.put(uid, tokenSet);
		}
	}

}
