package com.scs.sdfs.args;

import com.scs.sdfs.delegation.DelegationToken;

public class CmdDelegateRightsArgument extends CommandArgument{

	public String uid;
	public DelegationToken token;
	
	public CmdDelegateRightsArgument() {
		// TODO Auto-generated constructor stub
	}

	public CmdDelegateRightsArgument(String uid, DelegationToken token) {
		super();
		this.uid = uid;
		this.token = token;
	}

}
