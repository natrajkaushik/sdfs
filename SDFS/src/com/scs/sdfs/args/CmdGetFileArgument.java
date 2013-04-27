package com.scs.sdfs.args;

import com.scs.sdfs.Method;
import com.scs.sdfs.delegation.DelegationToken;

public class CmdGetFileArgument extends CommandArgument {
	
	public String uid;
	public DelegationToken token;
	
	public CmdGetFileArgument() {}
	
	public CmdGetFileArgument(String uid, DelegationToken token) {
		this.uid = uid;
		this.token = token;
		this.command = Method.GET;
	}
}