package com.scs.sdfs.args;

import com.scs.sdfs.Method;
import com.scs.sdfs.delegation.DelegationToken;

public class CmdPutFileArgument extends CommandArgument {

	public String uid;
	public byte[] data;
	public DelegationToken token;
	
	public CmdPutFileArgument(String uid, byte[] data, DelegationToken token) {
		this.uid = uid;
		this.data = data;
		this.token = token;
		this.command = Method.PUT;
	}
}