package com.scs.sdfs;


public enum ErrorCode {

	OK(200, "OK"),
	
	INVALID_ARGUMENT(400, "Invalid arguments provided!"),
	
	FILE_DELETED(410, "File deleted from server!"),
	FILE_NOT_FOUND(404, "File not found!"),
	FILE_NOT_SAVED(409, "File couldn't be saved!"),
	
	INSUFFICIENT_CREDS(401, "Insufficient permissions!"),
	UNAUTHORIZED_ACCESS(403, "Unauthorized access!"),
	
	UNKNOWN_ERROR(500, "Unknown error happened on server!")
	
	;
	
	public int code;
	public String msg;

	ErrorCode(int code, String msg) {
		this.code = code;
		this.msg = msg;
	}
}