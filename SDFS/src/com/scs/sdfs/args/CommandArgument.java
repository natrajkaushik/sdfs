package com.scs.sdfs.args;

import com.google.gson.Gson;
import com.scs.sdfs.Method;

public abstract class CommandArgument {
	
	protected static transient final Gson gson = new Gson();
	
	public Method command;
	
	public CommandArgument() {}
	
	public String toString() {
		return gson.toJson(this);
	}
	
	public byte[] toBytes() {
		return toString().getBytes();
	}
}