package com.scs.sdfs.rspns;

import com.google.gson.Gson;
import com.scs.sdfs.ErrorCode;

public abstract class CommandResponse {

	protected static transient final Gson gson = new Gson();

	public ErrorCode code;
	
	public CommandResponse() {}

	public String toString() {
		return gson.toJson(this);
	}

	public byte[] toBytes() {
		return toString().getBytes();
	}
}