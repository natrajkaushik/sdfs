package com.scs.sdfs;

public enum Method {

	START ("START"),
	GET ("GET"),
	PUT ("PUT"),
	DELEGATE ("DELEGATE"),
	_DELEGATE("DELEGATE*"),
	CLOSE ("CLOSE");
	
	private String method;
	
	Method(String method){
		this.method = method;
	}
	
	public String toString() {
		return method;
	}
}