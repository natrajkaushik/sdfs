package com.scs.sdfs.test;

import java.util.ArrayList;

import com.google.gson.Gson;
import com.scs.sdfs.args.CommandArgument;
import com.scs.sdfs.args.CmdGetFileArgument;
import com.scs.sdfs.delegation.DelegationPrimitive;
import com.scs.sdfs.delegation.DelegationToken;

public class GsonTest {

	public static void main(String[] args) {
		DelegationToken token = new DelegationToken();
		token.primitiveSignature = "ROOT".getBytes();
		token.primitive = new DelegationPrimitive("ABC", "DEF", "18465846583468", true, true, false,
				System.currentTimeMillis(), 10000);

		DelegationToken sub1 = new DelegationToken();
		sub1.primitiveSignature = "SUB1".getBytes();
		sub1.primitive = new DelegationPrimitive("MNO", "XYZ", "564385643876583476", false, true, false,
				System.currentTimeMillis() - 30000, 60000);

		token.tokenChain = new ArrayList<>();
		token.tokenChain.add(sub1);
		
		Gson gson = new Gson();
		System.out.println(gson.toJson(token));
		
		String json = gson.toJson(token);
		DelegationToken newToken = gson.fromJson(json, DelegationToken.class);
		
		System.out.println(newToken.tokenChain.size());
		System.out.println(new String(newToken.primitiveSignature));
		
		CommandArgument arg = new CmdGetFileArgument("1234", token);
		System.out.println(arg.toString());
	}
}