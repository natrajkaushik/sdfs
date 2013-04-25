package com.scs.sdfs.delegation;

public class DelegationPrimitive {

	String source;
	String delegate;
	
	String UID;
	boolean canRead;
	boolean canWrite;
	boolean canDelegate;
	
	long startEpoch;
	long endEpoch;
	
	public DelegationPrimitive(String source, String delegate, String uID, 
			boolean canRead, boolean canWrite, boolean canDelegate, 
			long startEpoch, long duration) {
		this.source = source;
		this.delegate = delegate;
		UID = uID;
		this.canRead = canRead;
		this.canWrite = canWrite;
		this.canDelegate = canDelegate;
		this.startEpoch = startEpoch;
		this.endEpoch = startEpoch + duration;
	}
}