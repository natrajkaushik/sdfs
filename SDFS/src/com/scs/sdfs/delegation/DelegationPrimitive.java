package com.scs.sdfs.delegation;

public class DelegationPrimitive {

	public String source;
	public String target;
	
	public String fileUID;
	public boolean canRead;
	public boolean canWrite;
	public boolean canDelegate;
	
	public long startEpoch;
	public long endEpoch;
	
	public DelegationPrimitive(String source, String target, String uID, 
			boolean canRead, boolean canWrite, boolean canDelegate, 
			long startEpoch, long duration) {
		this.source = source;
		this.target = target;
		this.fileUID = uID;
		this.canRead = canRead;
		this.canWrite = canWrite;
		this.canDelegate = canDelegate;
		this.startEpoch = startEpoch;
		this.endEpoch = startEpoch + duration;
	}
}