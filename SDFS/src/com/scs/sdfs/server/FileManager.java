package com.scs.sdfs.server;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;

import com.scs.sdfs.ErrorCode;
import com.scs.sdfs.args.CmdGetFileArgument;
import com.scs.sdfs.args.CmdPutFileArgument;
import com.scs.sdfs.delegation.DelegationVerifier;
import com.scs.sdfs.rspns.CmdGetFileResponse;
import com.scs.sdfs.rspns.CmdPutFileResponse;

public class FileManager {

	private static final byte[] META_BOM = {8, 16, 32, 64};
	
	private static final String DATA_FOLDER = "data";
	private static final String FILE_FOLDER = "data/files";
	
	private static final String META_FILE = "data/meta.info";
	
	/**
	 * This maps the file UID to the metadata object for that file.
	 */
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
	
	/**
	 * Executes the the GetFile command on the server if the request is valid.
	 * @param client The client identifier extracted from the connection
	 * @param arg Arguments to the GetFile command
	 * @return Contents of the file if the request is valid and file exists, error code otherwise
	 */
	public synchronized CmdGetFileResponse commandGetFile(String client, CmdGetFileArgument arg) {
		CmdGetFileResponse response = new CmdGetFileResponse();
		if (arg == null) {
			response.code = ErrorCode.INVALID_ARGUMENT;
		}
		else {
			if (files.containsKey(arg.UID)) {
				MetaFile meta = files.get(arg.UID);
				if (meta.owner.equals(client) ||
						DelegationVerifier.validateToken(client, arg.UID, arg.token, false)) {
					File file = new File(FILE_FOLDER + File.separator + meta.diskName);
					if (file.exists()) {
						response.data = Crypto.loadFromDisk(file.getAbsolutePath(), meta.fileKey);
						response.code = ErrorCode.OK;
					} else {
						response.code = ErrorCode.FILE_DELETED;
					}
				} else {
					response.code = ErrorCode.UNAUTHORIZED_ACCESS;
				}
			} else {
				response.code = ErrorCode.FILE_NOT_FOUND;
			}
		}
		return response;
	}
	
	/**
	 * Executes the the PutFile command on the server if the request is valid.
	 * @param client The client identifier extracted from the connection
	 * @param arg Arguments to the PutFile command
	 * @return An OK response if the command succeeds, error code otherwise
	 */
	public synchronized CmdPutFileResponse commandPutFile(String client, CmdPutFileArgument arg) {
		CmdPutFileResponse response = new CmdPutFileResponse();
		if (arg == null) {
			response.code = ErrorCode.INVALID_ARGUMENT;
		}
		else {
			if (files.containsKey(arg.UID)) {
				MetaFile meta = files.get(arg.UID);
				if (meta.owner.equals(client) ||
						DelegationVerifier.validateToken(client, arg.UID, arg.token, true)) {
					File file = new File(FILE_FOLDER + File.separator + meta.diskName);
					if (Crypto.saveToDisk(file.getAbsolutePath(), arg.data, true)) {
						response.code = ErrorCode.OK;
					} else {
						response.code = ErrorCode.FILE_NOT_SAVED;
					}
				} else {
					response.code = ErrorCode.UNAUTHORIZED_ACCESS;
				}
			} else {
				MetaFile newFile = new MetaFile(client, arg.UID, generateNewDiskName(), 
												Crypto.getKeyFromData(arg.data));
				File file = new File(FILE_FOLDER + File.separator + newFile.diskName);
				if (Crypto.saveToDisk(file.getAbsolutePath(), arg.data, true)) {
					response.code = ErrorCode.OK;
					files.put(arg.UID, newFile);
				} else {
					response.code = ErrorCode.FILE_NOT_SAVED;
				}
			}
		}
		return response;
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
	
	private String generateNewDiskName() {
		do {
			String newName = UUID.randomUUID().toString();
			if (!new File(FILE_FOLDER + File.separator + newName).exists()) {
				return newName;
			}
		}
		while (true);
	}
}