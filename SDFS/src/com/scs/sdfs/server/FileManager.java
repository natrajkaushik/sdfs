package com.scs.sdfs.server;

import java.io.File;
import java.util.HashMap;

public class FileManager {
	
	private static final String DATA_FOLDER = "data";
	private static final String FILE_FOLDER = "data/files";
	
	private static final String META_FILE = "data/meta.info";
	
	private HashMap<String, MetaFile> files;
	
	private String keystorePassword;
	
	public FileManager(String password) {
		this.keystorePassword = password;
	}

	public boolean init() {
		if (!Crypto.init(keystorePassword)) {
			return false;
		}
		if (new File(META_FILE).exists()) {
			loadMetadata();
		}
		else {
			files = new HashMap<String, MetaFile>();
		}
		return true;
	}
	
	private void loadMetadata() {
		byte[] metadata = Crypto.loadFromDisk(META_FILE);
		// TODO parse file contents into meta objects
	}
}