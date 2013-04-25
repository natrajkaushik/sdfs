package com.scs.sdfs.delegation;

public class DelegationPrimitive {

	String source;
	String delegate;
	
	String UID;
	boolean canRead;
	boolean canWrite;
	boolean canDelegate;
	
	long startEpoch;
	long duration;
}