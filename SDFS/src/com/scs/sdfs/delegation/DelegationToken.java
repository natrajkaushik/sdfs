package com.scs.sdfs.delegation;

import java.io.ByteArrayInputStream;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;

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
	public byte[] sourceCert;

	/**
	 * Parent of the current token, which authorized
	 * the source to grant this token, or null if the
	 * source is the owner
	 */
	public DelegationToken parentToken;

	public DelegationToken(DelegationPrimitive primitive, Certificate sourceCert, 
			DelegationToken parentToken, PrivateKey privateKey) {
		this.primitive = primitive;
		this.parentToken = parentToken;
		this.primitiveSignature = null;
		
		try {
			this.sourceCert = sourceCert.getEncoded();
		} catch (CertificateEncodingException e) {
			System.err.println("Invalid format certificate!");
		}
		
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
	
	/**
	 * Signs the token primitive with the provided private key
	 */
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
	
	/**
	 * Attests the validity of this token by verifying its signature and the
	 * integrity of the certificate used to sign the token.
	 */
	public boolean hasValidSignnature(Certificate rootCert) {
		try {
			Signature verifier = Signature.getInstance(Constants.SIGN_ALGO);
			Certificate signerCert = buildCertificate(sourceCert);
			verifier.initVerify(signerCert);
			verifier.update(GSON.toJson(primitive).getBytes());
			if (verifier.verify(primitiveSignature)) {
				try {
					signerCert.verify(rootCert.getPublicKey());
					return true;
				} catch (GeneralSecurityException e) {
					System.err.println("Unable to verify token signer's certificate! " + primitive.source);
				} catch (NullPointerException e) {
					System.err.println("Unable to validate against root certificate! " + primitive.source);
				}
			} else {
				System.err.println("Token signature failed to verify!");
			}
		} catch (GeneralSecurityException e) {
			System.err.println("Unable to verify token!");
			e.printStackTrace();
		} catch (NullPointerException e) {
			System.err.println("Failed to load certificate!");
			e.printStackTrace();
		}
		return false;
	}
	
	private Certificate buildCertificate(byte[] certData) {
		try {
			CertificateFactory cf = CertificateFactory.getInstance("X.509");
			return cf.generateCertificate(new ByteArrayInputStream(certData));
		} catch (CertificateException e) {
			e.printStackTrace();
		}
		return null;
	}
}