package com.scs.sdfs.rspns;

import com.scs.sdfs.ErrorCode;

public class CmdGetFileResponse extends CommandResponse {

	public byte[] data;
	public ErrorCode code;
	
	public CmdGetFileResponse() {
		data = null;
		code = ErrorCode.OK;
	}
	
	public CmdGetFileResponse(byte[] data, ErrorCode error) {
		this.data = data;
		this.code = error;
	}
}