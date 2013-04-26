package com.scs.sdfs.args;

import com.scs.sdfs.Method;
import com.scs.sdfs.delegation.DelegationToken;

public class CmdDelegateRightsArgument extends CommandArgument{

	public String uid;
	public DelegationToken token;
	
	public CmdDelegateRightsArgument() {}

	public CmdDelegateRightsArgument(String uid, DelegationToken token) {
		this.uid = uid;
		this.token = token;
		this.command = Method.DELEGATE;
	}
}