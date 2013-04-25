package com.scs.sdfs;


public enum ErrorCode {

	OK(200, "OK"),
	
	FILE_NOT_FOUND(404, "File not found!"),
	UNAUTHORIZED_ACCESS(403, "Unauthorized access!"),
	
	FILE_DELETED(401, "File deleted from server!");
	
	public int code;
	public String msg;

	ErrorCode(int code, String msg) {
		this.code = code;
		this.msg = msg;
	}
}