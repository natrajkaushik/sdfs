package com.scs.sdfs.rspns;

import com.scs.sdfs.ErrorCode;

public class CmdDelegateRightsResponse extends CommandResponse {

	public ErrorCode code;
	
	public CmdDelegateRightsResponse() {
		code = ErrorCode.OK;
	}
	
	public CmdDelegateRightsResponse(ErrorCode error) {
		code = error;
	}
}