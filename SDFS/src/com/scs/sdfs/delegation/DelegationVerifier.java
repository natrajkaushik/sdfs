package com.scs.sdfs.delegation;

import java.security.cert.Certificate;

public class DelegationVerifier {
	
	private static Certificate rootCert = null;
	
	public static void init(Certificate rootCertificate) {
		rootCert = rootCertificate;
	}

	public static boolean validateToken(String client, String UID, DelegationToken token, boolean write) {
		return false;
	}
}