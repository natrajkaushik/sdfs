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
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.util.HashMap;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import com.scs.sdfs.Constants;
import com.scs.sdfs.delegation.DelegationVerifier;

public abstract class Crypto {

	public static final int IV_LEN = 16;
	
	private static String p12File = "";
	private static String selfAlias = "";
	private static String keystorePassword = "";
	
	private static Certificate rootCert = null;
	
	/**
	 * Hashmap mapping the absolute file path to an object
	 * on which read and write methods can synchronize to get
	 * access to the file.
	 */
	private static HashMap<String, Object> lockMap;
	
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
	
	/**
	 * Initializes the keystore and the necessary objects.
	 * Also initializes the DelegationVerifier.
	 */
	public static boolean init(String password, Certificate rootCertificate, String alias, String keyFile) {
		keystorePassword = password;
		rootCert = rootCertificate;
		selfAlias = alias;
		p12File = keyFile;
		
		try {
			File keystoreFile = new File(Constants.KEY_DUMP_FOLDER + File.separator + p12File);
			keystore = KeyStore.getInstance(Constants.KEY_STORE_TYPE);
			keystore.load(new FileInputStream(keystoreFile), password.toCharArray());
			lockMap = new HashMap<String, Object>();
			if (rootCert != null) {				// no need for delegation verifier on the client
				DelegationVerifier.init(rootCert);
			}
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
	 * Loads and decrypts encrypted file contents using the default private key
	 */
	public static byte[] loadFromDisk(String filename) {
		return loadFromDisk(filename, getPrivateKey());
	}
	
	/**
	 * Loads and decrypts encrypted file contents using the hash of the provided 
	 * private key as a symmetric encryption key. The IV is read from the first
	 * few bytes of the encrypted file.
	 */
	public static byte[] loadFromDisk(String filename, Key key) {
		File inFile = new File(filename);
		if (inFile.exists()) {
			byte[] contents = readFromDisk(inFile);
			if (contents.length > IV_LEN) {
				byte[] iv = new byte[IV_LEN];
				byte[] encryptedData = new byte[contents.length-IV_LEN];
				System.arraycopy(contents, 0, iv, 0, IV_LEN);
				System.arraycopy(contents, IV_LEN, encryptedData, 0, (contents.length-IV_LEN));
				return decryptData(encryptedData, digestData(key.getEncoded()), iv);
			} else {
				System.err.println("Insufficient data to load metadata!");
			}
		} else {
			System.err.println("Encrypted file not found: " + filename);
		}
		return null;
	}
	
	/**
	 * Loads and decrypts encrypted file contents using decryption of the provided secret key
	 */
	public static byte[] loadFromDisk(String filename, byte[] key, byte[] iv) {
		File inFile = new File(filename);
		if (inFile.exists()) {
			byte[] encryptedData = readFromDisk(inFile);
			return decryptData(encryptedData, getDecryptedKey(key), iv);
		}
		System.err.println("Encrypted file not found: " + filename);
		return null;
	}
	
	/**
	 * Encrypts and saves data to file using the default public key
	 */
	public static boolean saveToDisk(String filename, byte[] data, boolean overwrite) {
		return saveToDisk(filename, data, overwrite, getPrivateKey());
	}
	
	/**
	 * Encrypts and saves data to file using the hash of the provided public key
	 * as a symmetric decryption key. The IV is prepended to the encrypted file contents.
	 */
	public static boolean saveToDisk(String filename, byte[] data, boolean overwrite, Key key) {
		File outFile = new File(filename);
		if (!outFile.exists() || overwrite) {
			byte[] iv = new byte[IV_LEN];
			byte[] encryptedData = encryptData(data, digestData(key.getEncoded()), iv);
			byte[] contents = new byte[IV_LEN + encryptedData.length];
			System.arraycopy(iv, 0, contents, 0, IV_LEN);
			System.arraycopy(encryptedData, 0, contents, IV_LEN, encryptedData.length);
			return writeToDisk(contents, outFile);
		} else {
			System.err.println("Cannot overwrite file: " + filename);
		}
		return false;
	}
	
	/**
	 * Encrypts and saves data to file using decryption of the provided secret key.
	 * Returns the IV used for encryption <b><i>only if</i></b> the encryption as well as
	 * saving to disk was successful.
	 */
	public static byte[] saveToDisk(String filename, byte[] data, byte[] key, boolean overwrite) {
		File outFile = new File(filename);
		if (!outFile.exists() || overwrite) {
			byte[] iv = new byte[IV_LEN];
			byte[] encryptedData = encryptData(data, getDecryptedKey(key), iv);
			if (writeToDisk(encryptedData, outFile)) {
				return iv;
			}
		}
		else {
			System.err.println("Cannot overwrite file: " + filename);
		}
		return null;
	}
	
	/**
	 * Hashes file data to generate an AES-256 key for 
	 * encrypting the file contents with. The key is returned
	 * encrypted with the private key of the current node.
	 */
	public static byte[] getKeyFromData(byte[] data) {
		return encryptData(digestData(data), getPublicKey());
	}

	/**
	 * Gets the public key for the server from the keystore
	 */
	private static PublicKey getPublicKey() {
		try {
			return keystore.getCertificate(selfAlias).getPublicKey();
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
			Key key = keystore.getKey(selfAlias, keystorePassword.toCharArray());
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
			Cipher cipher = Cipher.getInstance(Constants.ASYM_ENC_ALGO);
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
			Cipher cipher = Cipher.getInstance(Constants.ASYM_ENC_ALGO);
			cipher.init(Cipher.DECRYPT_MODE, key);
			cipher.update(data);
			return cipher.doFinal();
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
	private static byte[] encryptData(byte[] data, byte[] key, byte[] iv) {
		if (data == null || key == null || key.length == 0) {
			return null;
		}
		try {
			Cipher cipher = Cipher.getInstance(Constants.SYM_ENC_ALGO);
			SecretKeySpec secret = new SecretKeySpec(key, Constants.SYM_KEY_FMT);
			cipher.init(Cipher.ENCRYPT_MODE, secret);
			System.arraycopy(cipher.getIV(), 0, iv, 0, IV_LEN);
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
	private static byte[] decryptData(byte[] data, byte[] key, byte[] iv) {
		if (data == null || key == null || key.length == 0) {
			return null;
		}
		try {
			Cipher cipher = Cipher.getInstance(Constants.SYM_ENC_ALGO);
			IvParameterSpec ivSpec = new IvParameterSpec(iv);
			SecretKeySpec secret = new SecretKeySpec(key, Constants.SYM_KEY_FMT);
			cipher.init(Cipher.DECRYPT_MODE, secret, ivSpec);
			return cipher.doFinal(data);
		}
		catch (GeneralSecurityException e) {
			System.err.println("Error decrypting data!");
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Computes the digest of the provided data
	 */
	private static byte[] digestData(byte[] data) {
		try {
			return MessageDigest.getInstance(Constants.HASH_ALGO).digest(data);
		} catch (NoSuchAlgorithmException e) {
			System.err.println("Unable to digest data!");
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Reads data from file
	 */
	private static byte[] readFromDisk(File inFile) {
		Object lockObj = lockMap.get(inFile.getAbsolutePath());
		if (lockObj == null) {
			lockObj = new Object();
			lockMap.put(inFile.getAbsolutePath(), lockObj);
		}
		synchronized (lockObj) {
			try {
				byte[] data = new byte[(int) inFile.length()];
				FileInputStream fis = new FileInputStream(inFile);
				fis.read(data);
				fis.close();
				return data;
			}
			catch (IOException e) {
				System.err.println("Error reading file: " + inFile.getName());
			}
		}
		return null;
	}
	
	/**
	 * Writes data out to file
	 */
	private static boolean writeToDisk(byte[] data, File outFile) {
		Object lockObj = lockMap.get(outFile.getAbsolutePath());
		if (lockObj == null) {
			lockObj = new Object();
			lockMap.put(outFile.getAbsolutePath(), lockObj);
		}
		synchronized (lockObj) {
			try {
				if (data != null) {
					FileOutputStream fos = new FileOutputStream(outFile, false);
					fos.write(data);
					fos.close();
					return true;
				}
			} catch (IOException e) {
				System.err.println("Error saving to file: " + outFile.getName());
				e.printStackTrace();
			}
		}
		return false;
	}
}