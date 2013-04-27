package com.scs.sdfs.test;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

public class JsonTest {

	public static void main(String[] args) {
		byte[] test = new byte[] {34, 43, 56, 65};
		System.out.println(new Gson().toJsonTree(test));
		JsonElement elem = new Gson().toJsonTree(test);
		byte[] got = new Gson().fromJson(elem, byte[].class);
		System.out.println(got[2]);
	}
}