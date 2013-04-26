package com.scs.sdfs;

public class Constants {
	public static final String KEY_DUMP_FOLDER = "./keydump";
	
	public static final int SERVER_LISTENER_PORT = 31337;
	
	public static final String KEY_STORE_TYPE = "PKCS12";
	
	public static final String TRUSTED_STORE_PATH = "./store/trusted.jks";
	public static final String TRUSTED_STORE_PASSWORD = "server";
	public static final String TRUSTED_STORE_TYPE = "JKS";
	
	public static final String ROOT_ALIAS = "root";
	public static final String SERVER_ALIAS = "server";
	public static final String SERVER_KEY_STORE = "server.p12";
	
	public static final String LOCALHOST = "localhost";

	public static final String ASYM_ENC_ALGO = "RSA";
	public static final String SYM_ENC_ALGO = "AES/CBC/PKCS5Padding";
	public static final String HASH_ALGO = "SHA-256";
	public static final String SIGN_ALGO = "SHA256withRSA";
	
	public static final String SYM_KEY_FMT = "AES";

	public static final String META_FILE = "data/meta.info";
	public static final String FILE_FOLDER = "data/files";
	public static final String DATA_FOLDER = "data";
	public static final String META_SUFFIX = "meta.info";
}