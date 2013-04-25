package com.scs.sdfs.server;

public class MetaFile {

	private String owner;
	private String UID;
	private String diskName;
	
	private byte[] fileKey;			// encrypted with server's public key
									// decrypt when trying to read the file
}