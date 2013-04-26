package com.scs.sdfs.delegation;

import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.cert.Certificate;

import com.google.gson.Gson;
import com.scs.sdfs.Constants;

public class DelegationToken {
	
	private static transient final Gson GSON = new Gson();

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

	public DelegationToken(DelegationPrimitive primitive, Certificate sourceCert, 
			DelegationToken parentToken, PrivateKey privateKey) {
		this.primitive = primitive;
		this.sourceCert = sourceCert;
		this.parentToken = parentToken;
		this.primitiveSignature = null;
		
		fitPrimitive();
		signToken(privateKey);
	}

	public DelegationToken() {}

	/**
	 * Fits the primitives to the intersection of the user requests
	 * and the permissions obtained by the parent token, if present.
	 * No parent means this node is the owner, and so no fitting is
	 * needed.
	 */
	private void fitPrimitive() {
		if (parentToken != null) {
			primitive.canRead &= parentToken.primitive.canRead;
			primitive.canWrite &= parentToken.primitive.canWrite;
			primitive.canDelegate &= parentToken.primitive.canDelegate;
			primitive.startEpoch = Math.max(primitive.startEpoch, parentToken.primitive.startEpoch);
			primitive.endEpoch = Math.min(primitive.endEpoch, parentToken.primitive.endEpoch);
		}
	}
	
	private void signToken(PrivateKey key) {
		try {
			Signature signer = Signature.getInstance(Constants.SIGN_ALGO);
			signer.initSign(key);
			signer.update(GSON.toJson(primitive).getBytes());
			primitiveSignature = signer.sign();
		} catch (GeneralSecurityException e) {
			System.err.println("Couldn't sign token primitive!");
			e.printStackTrace();
		}
	}
}