package com.scs.sdfs.test;

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.PrivateKey;
import java.security.cert.Certificate;

import com.scs.sdfs.Constants;
import com.scs.sdfs.KeyStoreHelper;
import com.scs.sdfs.delegation.DelegationPrimitive;
import com.scs.sdfs.delegation.DelegationToken;
import com.scs.sdfs.delegation.DelegationVerifier;

public class DelegationTest {

	public static void main(String[] args) throws InterruptedException {
		Certificate rootCert = getRootCertificate();
		
		DelegationPrimitive prim1 = new DelegationPrimitive("node_a", "node_b", "18465846583468", true, true, 
															true, System.currentTimeMillis(), 10000);
		Certificate certificate1 = KeyStoreHelper.getCertificate("node_a", "nodea");
		PrivateKey privateKey1 = KeyStoreHelper.getPrivateKey("node_a", "nodea");
		DelegationToken token1 = new DelegationToken(prim1, certificate1, null, privateKey1);
		
		 System.out.println(token1.hasValidSignnature(rootCert));
		
		DelegationPrimitive prim2 = new DelegationPrimitive("node_b", "node_c", "18465846583468", true, true, 
															false, System.currentTimeMillis(), 3000);
		Certificate certificate2 = KeyStoreHelper.getCertificate("node_b", "nodeb");
		PrivateKey privateKey2 = KeyStoreHelper.getPrivateKey("node_b", "nodeb");
		DelegationToken token2 = new DelegationToken(prim2, certificate2, token1, privateKey2);
		
		 System.out.println(token2.hasValidSignnature(rootCert));
		
		DelegationVerifier.init(rootCert);
		System.out.println(DelegationVerifier.validateToken("node_a", "node_c", "18465846583468", 
															token2, true, System.currentTimeMillis()));
		
		Thread.sleep(3500);
		
		System.out.println(DelegationVerifier.validateToken("node_a", "node_c", "18465846583468", 
				token2, true, System.currentTimeMillis()));
		System.out.println(DelegationVerifier.validateToken("node_a", "node_b", "18465846583468", 
				token1, true, System.currentTimeMillis()));
	}

	private static Certificate getRootCertificate(){
		KeyStore trustedKeyStore = KeyStoreHelper.getKeyStore(Constants.TRUSTED_STORE_PATH, 
				Constants.TRUSTED_STORE_PASSWORD, Constants.TRUSTED_STORE_TYPE);
		Certificate ROOT_CERTIFICATE = null;
		try {
			ROOT_CERTIFICATE = trustedKeyStore.getCertificate(Constants.ROOT_ALIAS);
		} catch (KeyStoreException e) {
			e.printStackTrace();
		}
		return ROOT_CERTIFICATE;
	}
}