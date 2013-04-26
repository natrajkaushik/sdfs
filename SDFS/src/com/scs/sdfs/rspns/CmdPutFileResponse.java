package com.scs.sdfs.rspns;

import com.scs.sdfs.ErrorCode;

public class CmdPutFileResponse extends CommandResponse {
	
	public CmdPutFileResponse() {
		this.code = ErrorCode.OK;
	}
	
	public CmdPutFileResponse(ErrorCode error) {
		this.code = error;
	}
}