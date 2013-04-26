package com.scs.sdfs;

public enum Right {

	READ ("READ"),
	WRITE ("WRITE"),
	DELEGATE ("DELEGATE");
	
	private String right;
	
	Right(String right){
		this.right = right;
	}
	
	public String toString() {
		return right;
	}
}