package com.scs.sdfs.server;

public class MetaFile {

	String owner;
	String UID;
	String diskName;
	
	byte[] fileKey;						// encrypted with server's public key
										// decrypt when trying to read the file
	
	byte[] fileIv = new byte[16];		// IV used while encrypting file contents
										// before saving to disk, in plaintext
	
	public MetaFile() {}

	public MetaFile(String owner, String uID, String diskName, byte[] fileKey) {
		super();
		this.owner = owner;
		UID = uID;
		this.diskName = diskName;
		this.fileKey = fileKey;
	}
}