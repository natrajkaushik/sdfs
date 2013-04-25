package com.scs.sdfs.delegation;

import java.security.cert.Certificate;


public class DelegationToken {

	/**
	 * The plaintext primitives
	 */
	public DelegationPrimitive primitive;
	
	/**
	 * Signed encrypted form of the primitives
	 */
	public byte[] primitiveSignature;
	
	/**
	 * Certificate of the source of this token
	 */
	public Certificate sourceCert;
	
	/**
	 * Parent of the current token, which authorized
	 * the source to grant this token, or null if the
	 * source is the owner
	 */
	public DelegationToken parentToken;
}