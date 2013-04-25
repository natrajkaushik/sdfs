package com.scs.sdfs.delegation;

public class DelegationPrimitive {

	public String source;
	public String delegate;
	
	public String UID;
	public boolean canRead;
	public boolean canWrite;
	public boolean canDelegate;
	
	public long startEpoch;
	public long endEpoch;
	
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