package com.scs.sdfs.args;

import com.scs.sdfs.Method;
import com.scs.sdfs.delegation.DelegationToken;

public class CmdGetFileArgument extends CommandArgument {
	
	public String UID;
	public DelegationToken token;
	
	public CmdGetFileArgument(String uid, DelegationToken token) {
		this.UID = uid;
		this.token = token;
		this.command = Method.GET;
	}
}