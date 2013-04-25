package com.scs.sdfs.delegation;

import java.util.ArrayList;

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
	 * Chain of ordered delegation tokens going 
	 * back to the owner of this UID
	 */
	public ArrayList<DelegationToken> tokenChain;
}