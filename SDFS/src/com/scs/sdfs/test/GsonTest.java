package com.scs.sdfs.test;

import com.google.gson.Gson;
import com.scs.sdfs.ErrorCode;
import com.scs.sdfs.args.CmdGetFileArgument;
import com.scs.sdfs.args.CommandArgument;
import com.scs.sdfs.delegation.DelegationPrimitive;
import com.scs.sdfs.delegation.DelegationToken;
import com.scs.sdfs.rspns.CmdGetFileResponse;
import com.scs.sdfs.rspns.CommandResponse;

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

		DelegationToken sub2 = new DelegationToken();
		sub2.primitiveSignature = "SUB2".getBytes();
		sub2.primitive = new DelegationPrimitive("PQR", "KLM", "65834658535868", false, true, false,
				System.currentTimeMillis() - 60000, 80000);

		DelegationToken sub3 = new DelegationToken();
		sub3.primitiveSignature = "SUB3".getBytes();
		sub3.primitive = new DelegationPrimitive("GHI", "STU", "65834658535868", false, true, false,
				System.currentTimeMillis() - 60000, 80000);

		sub2.parentToken = sub3;
		sub1.parentToken = sub2;
		token.parentToken = sub1;
		
		Gson gson = new Gson();
		System.out.println("**********************************");
		System.out.println(gson.toJson(token));
		System.out.println("**********************************");
		
		String json = gson.toJson(token);
		DelegationToken newToken = gson.fromJson(json, DelegationToken.class);
		
		System.out.println(new String(newToken.primitiveSignature));
		
		CommandArgument arg = new CmdGetFileArgument("1234", token);
		System.out.println(arg.toString());
		
		CommandResponse rsp = new CmdGetFileResponse("deleted".getBytes(), ErrorCode.FILE_DELETED);
		System.out.println(gson.toJson(rsp));
		
		String rjson = gson.toJson(rsp);
		CmdGetFileResponse nrsp = gson.fromJson(rjson, CmdGetFileResponse.class);
		
		System.out.println(nrsp.code.msg);
		System.out.println(new String(nrsp.data));
	}
}