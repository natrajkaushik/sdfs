package com.scs.sdfs.delegation;

import java.util.ArrayList;

public class DelegationToken {

	/**
	 * The plaintext primitives
	 */
	DelegationPrimitive primitive;
	
	/**
	 * Signed encrypted form of the primitives
	 */
	byte[] primitiveSignature;
	
	/**
	 * Chain of ordered delegation tokens going 
	 * back to the owner of this UID
	 */
	ArrayList<DelegationToken> tokenChain;
}