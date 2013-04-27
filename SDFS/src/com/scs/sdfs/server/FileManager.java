package com.scs.sdfs.server;

import java.io.File;
import java.util.HashMap;
import java.util.UUID;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.scs.sdfs.Constants;
import com.scs.sdfs.ErrorCode;
import com.scs.sdfs.args.CmdGetFileArgument;
import com.scs.sdfs.args.CmdPutFileArgument;
import com.scs.sdfs.delegation.DelegationVerifier;
import com.scs.sdfs.rspns.CmdGetFileResponse;
import com.scs.sdfs.rspns.CmdPutFileResponse;

public class FileManager {

	private static final Gson GSON = new Gson();
	
	/**
	 * This maps the file fileUID to the metadata object for that file.
	 */
	public HashMap<String, MetaFile> files;

	private FileManager() {}

	private static class SingletonHolder {
		public static final FileManager INSTANCE = new FileManager();
	}

	public static FileManager getInstance() {
		return SingletonHolder.INSTANCE;
	}

	public boolean init() {
		if (new File(Constants.META_FILE).exists()) {
			loadMetadata();
		}
		else {
			files = new HashMap<String, MetaFile>();
		}
		File fileStore = new File(Constants.FILE_FOLDER);
		if (!(fileStore.exists() || fileStore.mkdirs())) {
			return false;
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
			if (files.containsKey(arg.uid)) {
				MetaFile meta = files.get(arg.uid);
				long now = System.currentTimeMillis();
				if (meta.owner.equals(client) ||
						DelegationVerifier.validateToken(meta.owner, client, arg.uid, arg.token, false, now)) {
					File file = new File(Constants.FILE_FOLDER + File.separator + meta.diskName);
					if (file.exists()) {
						response.data = Crypto.loadFromDisk(file.getAbsolutePath(), meta.fileKey, meta.fileIv);
						response.code = ErrorCode.OK;
						System.out.println("Delivered " + arg.uid + " to " + client);
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
			if (files.containsKey(arg.uid)) {
				MetaFile meta = files.get(arg.uid);
				long now = System.currentTimeMillis();
				if (meta.owner.equals(client) ||
						DelegationVerifier.validateToken(meta.owner, client, arg.uid, arg.token, true, now)) {
					File file = new File(Constants.FILE_FOLDER + File.separator + meta.diskName);
					byte[] iv = Crypto.saveToDisk(file.getAbsolutePath(), arg.data, meta.fileKey, true);
					if (iv != null) {
						System.arraycopy(iv, 0, meta.fileIv, 0, Crypto.IV_LEN);
						response.code = ErrorCode.OK;
						System.out.println("Put " + arg.uid + " to " + client);
					} else {
						response.code = ErrorCode.FILE_NOT_SAVED;
					}
				} else {
					response.code = ErrorCode.UNAUTHORIZED_ACCESS;
				}
			} else {
				MetaFile newFile = new MetaFile(client, arg.uid, generateNewDiskName(), 
												Crypto.getKeyFromData(arg.data));
				File file = new File(Constants.FILE_FOLDER + File.separator + newFile.diskName);
				byte[] iv = Crypto.saveToDisk(file.getAbsolutePath(), arg.data, newFile.fileKey, true);
				if (iv != null) {
					System.arraycopy(iv, 0, newFile.fileIv, 0, Crypto.IV_LEN);
					response.code = ErrorCode.OK;
					files.put(arg.uid, newFile);
				} else {
					response.code = ErrorCode.FILE_NOT_SAVED;
				}
			}
		}
		return response;
	}
	
	private void loadMetadata() {
		byte[] metadata = Crypto.loadFromDisk(Constants.META_FILE);
		if (metadata == null || metadata.length == 0) {
			System.err.println("Insufficient saved metadata!");
		}
		else {
			files = GSON.fromJson(new String(metadata), new TypeToken<HashMap<String, MetaFile>>(){}.getType());
			System.out.println("Loaded " + files.size() + " files!");
			return;
		}
		files = new HashMap<String, MetaFile>();
	}
	
	private void saveMetadata() {
		if (files == null) {
			return;
		}
		if (new File(Constants.DATA_FOLDER).exists() || new File(Constants.DATA_FOLDER).mkdir()) {
			byte[] data = GSON.toJson(files).getBytes();
			if (!Crypto.saveToDisk(Constants.META_FILE, data, true)) {
				System.err.println("Unable to save metadata to file!");
			}
		} else {
			System.err.println("Couldn't create data folder!");
		}
	}
	
	private String generateNewDiskName() {
		do {
			String newName = UUID.randomUUID().toString();
			if (!new File(Constants.FILE_FOLDER + File.separator + newName).exists()) {
				return newName;
			}
		}
		while (true);
	}
}