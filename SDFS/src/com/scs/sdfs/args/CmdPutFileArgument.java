package com.scs.sdfs.args;

import com.scs.sdfs.delegation.DelegationToken;

public class CmdPutFileArgument extends CommandArgument {

	public String UID;
	public byte[] data;
	public DelegationToken token;
	
	public CmdPutFileArgument(String uid, DelegationToken token) {
		this.UID = uid;
		this.token = token;
	}
}