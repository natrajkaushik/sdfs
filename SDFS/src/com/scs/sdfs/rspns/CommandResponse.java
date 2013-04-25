package com.scs.sdfs.rspns;

import com.google.gson.Gson;

public abstract class CommandResponse {

	protected static transient final Gson gson = new Gson();

	public String toString() {
		return gson.toJson(this);
	}

	public byte[] toBytes() {
		return toString().getBytes();
	}
}