package com.scs.sdfs.server;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.HashMap;

import com.scs.sdfs.delegation.DelegationToken;

public class FileManager {

	private static final byte[] META_BOM = {8, 16, 32, 64};
	
	private static final String DATA_FOLDER = "data";
	private static final String FILE_FOLDER = "data/files";
	
	private static final String META_FILE = "data/meta.info";
	
	private HashMap<String, MetaFile> files;
	
	private String keystorePassword;

	private FileManager() {}

	private static class SingletonHolder {
		public static final FileManager INSTANCE = new FileManager();
	}

	public static FileManager getInstance() {
		return SingletonHolder.INSTANCE;
	}

	public boolean init(String password) {
		this.keystorePassword = password;
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
	
	public void wrapUp() {
		saveMetadata();
	}
	
	public synchronized byte[] commandGetFile(String client, String UID, DelegationToken token) {
		
		
		return null;
	}
	
	@SuppressWarnings("unchecked")
	private void loadMetadata() {
		byte[] metadata = Crypto.loadFromDisk(META_FILE);
		if (metadata == null || metadata.length < META_BOM.length) {
			System.err.println("Insufficient saved metadata!");
		}
		else {
			byte[] header = new byte[META_BOM.length];
			System.arraycopy(metadata, 0, header, 0, META_BOM.length);
			if (Arrays.equals(header, META_BOM)) {
				int remLen = metadata.length - META_BOM.length;
				try {
					ObjectInputStream ois = 
							new ObjectInputStream(new ByteArrayInputStream(metadata, META_BOM.length, remLen));
					files = (HashMap<String, MetaFile>) ois.readObject();
					ois.close();
					System.out.println("Loaded " + files.size() + " files!");
					return;
				} catch (IOException e) {
					System.err.println("Unable to load metadata from file!");
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					System.err.println("Unable to read metadata from file!");
					e.printStackTrace();
				}
			} else {
				System.err.println("Invalid saved metadata!");
			}
		}
		
		files = new HashMap<String, MetaFile>();
	}
	
	private void saveMetadata() {
		if (files == null) {
			return;
		}
		if (new File(DATA_FOLDER).exists() || new File(DATA_FOLDER).mkdir()) {
			try {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				baos.write(META_BOM);
				ObjectOutputStream oos = new ObjectOutputStream(baos);
				oos.writeObject(files);
				byte[] data = baos.toByteArray();
				if (!Crypto.saveToDisk(META_FILE, data, true)) {
					System.err.println("Unable to save metadata to file!");
				}
			}
			catch (IOException e) {
				System.err.println("Unable to store metadata!");
				e.printStackTrace();
			}
		} else {
			System.err.println("Couldn't create data folder!");
		}
	}
}