package com.scs.sdfs.server;

public class MetaFile {

	String owner;
	String UID;
	String diskName;
	
	byte[] fileKey;			// encrypted with server's public key
							// decrypt when trying to read the file
}