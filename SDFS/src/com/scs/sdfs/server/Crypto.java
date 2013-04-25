package com.scs.sdfs.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class Crypto {

	private static final String KEYSTORE_FMT = "PKCS12";
	private static final String SYM_KEY_FMT = "AES";
	
	private static final String ASYM_ENC_ALGO = "RSA";
	private static final String SYM_ENC_ALGO = "AES/CBC/PKCS5Padding";
	private static final String HASH_ALGO = "SHA-512";
	
	private static final String CERT_FOLDER = "cert";
	private static final String P12_FILE = "server.p12";
	private static final String SERVER_ALIAS = "server";
	
	private static String keystorePassword = "";
	
	private static KeyStore keystore;

	public static void main(String[] args) throws Exception {
		KeyStore ks = KeyStore.getInstance("PKCS12");
		ks.load(new FileInputStream(args[0]), args[1].toCharArray());

		Key key = ks.getKey(args[2], args[1].toCharArray());
		// System.out.println(new String(key.getEncoded()));
		System.out.println(key.getClass());
		PrivateKey prk = (PrivateKey) key;
		System.out.println(prk);
		System.out.println("****************************************************************");
		PublicKey pbk = ks.getCertificate(args[2]).getPublicKey();
		System.out.println(pbk);
		System.out.println("****************************************************************");

		String plain = "Hello RSA World!";
		Cipher cipher = Cipher.getInstance("RSA");
		cipher.init(Cipher.ENCRYPT_MODE, key);
		byte[] data = cipher.doFinal(plain.getBytes());

		cipher.init(Cipher.DECRYPT_MODE, pbk);
		byte[] decrypted = cipher.doFinal(data);
		System.out.println(new String(decrypted));
	}
	
	public static boolean init(String password) {
		keystorePassword = password;
		try {
			File keystoreFile = new File(CERT_FOLDER + File.separator + P12_FILE);
			keystore = KeyStore.getInstance(KEYSTORE_FMT);
			keystore.load(new FileInputStream(keystoreFile), password.toCharArray());
			return true;
		} catch (GeneralSecurityException e) {
			System.err.println("Security error while loading keystore!");
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("I/O error while loading keystore!");
			e.printStackTrace();
		}
		
		return false;
	}
	
	/**
	 * Loads and decrypts encrypted file contents using the server's private key
	 */
	public static byte[] loadFromDisk(String filename) {
		return loadFromDisk(filename, getPrivateKey());
	}
	
	/**
	 * Loads and decrypts encrypted file contents using the provided private key
	 */
	public static byte[] loadFromDisk(String filename, Key key) {
		try {
			File inFile = new File(filename);
			if (inFile.exists()) {
				byte[] encryptedData = null;
				encryptedData = new byte[(int) inFile.length()];
				FileInputStream fis = new FileInputStream(inFile);
				fis.read(encryptedData);
				fis.close();
				return decryptData(encryptedData, key);
			}
			System.err.println("Encrypted file not found: " + filename);
		} catch (IOException e) {
			System.err.println("Error reading encrypted file: " + filename);
			e.printStackTrace();
		}
		
		return null;
	}
	
	/**
	 * Loads and decrypts encrypted file contents using decryption of the provided secret key
	 */
	public static byte[] loadFromDisk(String filename, byte[] key) {
		try {
			File inFile = new File(filename);
			if (inFile.exists()) {
				byte[] encryptedData = null;
				encryptedData = new byte[(int) inFile.length()];
				FileInputStream fis = new FileInputStream(inFile);
				fis.read(encryptedData);
				fis.close();
				return decryptData(encryptedData, getDecryptedKey(key));
			}
			System.err.println("Encrypted file not found: " + filename);
		} catch (IOException e) {
			System.err.println("Error reading encrypted file: " + filename);
			e.printStackTrace();
		}
		
		return null;
	}
	
	/**
	 * Encrypts and saves data to file using the server's public key
	 */
	public static boolean saveToDisk(String filename, byte[] data, boolean overwrite) {
		return saveToDisk(filename, data, overwrite, getPublicKey());
	}
	
	/**
	 * Encrypts and saves data to file using the provided public key
	 */
	public static boolean saveToDisk(String filename, byte[] data, boolean overwrite, Key key) {
		File outFile = new File(filename);
		if (!outFile.exists() || overwrite) {
			try {
				byte[] encryptedData = encryptData(data, key);
				if (encryptedData != null) {
					FileOutputStream fos = new FileOutputStream(outFile, false);
					fos.write(encryptedData);
					fos.close();
					return true;
				}
			} catch (IOException e) {
				System.err.println("Error saving to file: " + filename);
				e.printStackTrace();
			} 
		}
		else {
			System.err.println("Cannot overwrite file: " + filename);
		}
		
		return false;
	}
	
	/**
	 * Encrypts and saves data to file using decryption of the provided secret key
	 */
	public static boolean saveToDisk(String filename, byte[] data, byte[] key, boolean overwrite) {
		File outFile = new File(filename);
		if (!outFile.exists() || overwrite) {
			try {
				byte[] encryptedData = encryptData(data, getDecryptedKey(key));
				if (encryptedData != null) {
					FileOutputStream fos = new FileOutputStream(outFile, false);
					fos.write(encryptedData);
					fos.close();
					return true;
				}
			} catch (IOException e) {
				System.err.println("Error saving to file: " + filename);
				e.printStackTrace();
			} 
		}
		else {
			System.err.println("Cannot overwrite file: " + filename);
		}
		
		return false;
	}
	
	public static byte[] getKeyFromData(byte[] data) {
		try {
			MessageDigest digest = MessageDigest.getInstance(HASH_ALGO);
			return encryptData(digest.digest(data), getPublicKey());
		}
		catch (GeneralSecurityException e) {
			System.err.println("Error hashing data!");
			e.printStackTrace();
		}
		
		return null;
	}

	/**
	 * Gets the public key for the server from the keystore
	 */
	private static PublicKey getPublicKey() {
		try {
			return keystore.getCertificate(SERVER_ALIAS).getPublicKey();
		} catch (KeyStoreException e) {
			System.err.println("Error loading public key!");
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Gets the private key for the server from the keystore
	 */
	private static PrivateKey getPrivateKey() {
		try {
			Key key = keystore.getKey(SERVER_ALIAS, keystorePassword.toCharArray());
			return (PrivateKey) key;
		} catch (GeneralSecurityException e) {
			System.err.println("Error loading private key!");
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Decrypts the provided secret key with the server's private key
	 */
	private static byte[] getDecryptedKey(byte[] key) {
		return decryptData(key, getPrivateKey());
	}
	
	/**
	 * Does asymmetric encryption of data with the provided key
	 */
	private static byte[] encryptData(byte[] data, Key key) {
		if (data == null || key == null) {
			return null;
		}
		try {
			Cipher cipher = Cipher.getInstance(ASYM_ENC_ALGO);
			cipher.init(Cipher.ENCRYPT_MODE, key);
			return cipher.doFinal(data);
		}
		catch (GeneralSecurityException e) {
			System.err.println("Error encrypting data!");
			e.printStackTrace();
		}
		
		return null;
	}
	
	/**
	 * Does asymmetric decryption of data with the provided key
	 */
	private static byte[] decryptData(byte[] data, Key key) {
		if (data == null || key == null) {
			return null;
		}
		try {
			Cipher cipher = Cipher.getInstance(ASYM_ENC_ALGO);
			cipher.init(Cipher.DECRYPT_MODE, key);
			return cipher.doFinal(data);
		}
		catch (GeneralSecurityException e) {
			System.err.println("Error decrypting data!");
			e.printStackTrace();
		}
		
		return null;
	}
	
	/**
	 * Does symmetric encryption of data with the provided key
	 */
	private static byte[] encryptData(byte[] data, byte[] key) {
		if (data == null || key == null || key.length == 0) {
			return null;
		}
		try {
			Cipher cipher = Cipher.getInstance(SYM_ENC_ALGO);
			SecretKeySpec secret = new SecretKeySpec(key, SYM_KEY_FMT);
			cipher.init(Cipher.ENCRYPT_MODE, secret);
			return cipher.doFinal(data);
		}
		catch (GeneralSecurityException e) {
			System.err.println("Error encrypting data!");
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Does symmetric decryption of data with the provided key
	 */
	private static byte[] decryptData(byte[] data, byte[] key) {
		if (data == null || key == null || key.length == 0) {
			return null;
		}
		try {
			Cipher cipher = Cipher.getInstance(SYM_ENC_ALGO);
			SecretKeySpec secret = new SecretKeySpec(key, SYM_KEY_FMT);
			cipher.init(Cipher.DECRYPT_MODE, secret);
			return cipher.doFinal(data);
		}
		catch (GeneralSecurityException e) {
			System.err.println("Error decrypting data!");
			e.printStackTrace();
		}
		return null;
	}
}