package com.scs.sdfs.delegation;

import java.security.GeneralSecurityException;
import java.security.Signature;
import java.security.cert.Certificate;

import com.google.gson.Gson;
import com.scs.sdfs.Constants;

public class DelegationVerifier {
	
	private static final Gson GSON = new Gson();
	
	private static Certificate rootCert = null;
	
	public static void init(Certificate rootCertificate) {
		rootCert = rootCertificate;
	}

	public static boolean validateToken(String client, String UID, DelegationToken token, boolean write) {
		return false;
	}
	
	/**
	 * Attests the validity of a token by verifying its signature and the
	 * integrity of the certificate used to sign the token.
	 */
	public static boolean validateTokenSign(DelegationToken token) {
		try {
			Signature verifier = Signature.getInstance(Constants.SIGN_ALGO);
			verifier.initVerify(token.sourceCert);
			verifier.update(GSON.toJson(token.primitive).getBytes());
			if (verifier.verify(token.primitiveSignature)) {
				try {
					token.sourceCert.verify(rootCert.getPublicKey());
					return true;
				} catch (GeneralSecurityException e) {
					System.err.println("Unable to verify token signer's certificate!");
				} catch (NullPointerException e) {
					System.err.println("Unable to validate against root certificate!");
				}
			} else {
				System.err.println("Token signature failed to verify!");
			}
		} catch (GeneralSecurityException e) {
			System.err.println("Unable to verify token!");
			e.printStackTrace();
		}
		return false;
	}
}