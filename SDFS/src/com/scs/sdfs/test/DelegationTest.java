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

	public static void main(String[] args) {
		DelegationVerifier.init(getRootCertificate());
		
		DelegationPrimitive prim = new DelegationPrimitive("ABC", "DEF", "18465846583468", true, true, 
															false, System.currentTimeMillis(), 10000);
		Certificate certificate = KeyStoreHelper.getCertificate("node_a", "nodea");
		PrivateKey privateKey = KeyStoreHelper.getPrivateKey("node_a", "nodea");
		DelegationToken token = new DelegationToken(prim, certificate, null, privateKey);
		System.out.println(DelegationVerifier.validateTokenSign(token));
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