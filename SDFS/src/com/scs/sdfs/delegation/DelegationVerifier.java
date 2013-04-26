package com.scs.sdfs.delegation;

import java.security.cert.Certificate;

public abstract class DelegationVerifier {
	
	private static Certificate rootCert;
	
	public static void init(Certificate rootCertificate) {
		rootCert = rootCertificate;
	}

	public static boolean validateToken(String owner, String client, String UID, 
										DelegationToken token, boolean write, long instant) {
		if (owner == null || client == null || UID == null || token == null) {
			return false;
		}
		
		return 	checkRightClient(client, token) &&
				checkTokenActive(instant, token) &&
				checkPermission(write, token) &&
				checkCorrectUid(UID, token) &&
				checkTokenValid(token) &&
				checkAuthorizedIssuer(owner, client, UID, token, write, instant);
	}
	
	/**
	 * Check if token presented by the right client
	 */
	private static boolean checkRightClient(String client, DelegationToken token) {
		return client.equals(token.primitive.target);
	}
	
	/**
	 * Check if the token is actively valid
	 */
	private static boolean checkTokenActive(long instant, DelegationToken token) {
		return (instant >= token.primitive.startEpoch && instant <= token.primitive.endEpoch);
	}
	
	/**
	 * Check if requested action is permitted
	 */
	private static boolean checkPermission(boolean write, DelegationToken token) {
		return write ? token.primitive.canWrite : token.primitive.canRead;
	}

	/**
	 * Check if token is for the right fileUID
	 */
	private static boolean checkCorrectUid(String UID, DelegationToken token) {
		return UID.equals(token.primitive.fileUID);
	}
	
	/**
	 * Verifies the authenticity and integrity of the token
	 */
	private static boolean checkTokenValid(DelegationToken token) {
		return token.hasValidSignnature(rootCert);
	}
	
	/**
	 * Verifies that the token either comes from the owner of 
	 * the fileUID, or has a valid chain going back to the owner.
	 */
	private static boolean checkAuthorizedIssuer(String owner, String client, String UID, 
												DelegationToken token, boolean write, long instant) {
		return (owner.equals(token.primitive.source) || 
				((token.parentToken != null) && token.parentToken.primitive.canDelegate && 
						validateToken(owner, token.primitive.source, UID, token.parentToken, write, instant)));
	}
}