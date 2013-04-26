package com.scs.sdfs.test;

import com.google.gson.Gson;
import com.scs.sdfs.ErrorCode;
import com.scs.sdfs.args.CmdGetFileArgument;
import com.scs.sdfs.args.CmdPutFileArgument;
import com.scs.sdfs.rspns.CmdGetFileResponse;
import com.scs.sdfs.server.Crypto;
import com.scs.sdfs.server.FileManager;

public class FileTest {

	public static void main(String[] args) {
		Crypto.init("server", null, "server", "server.p12");
		FileManager fm = FileManager.getInstance();
		fm.init();
		
		System.out.println(new Gson().toJson(fm.files));
		
		CmdPutFileArgument parg1 = new CmdPutFileArgument("file1", "This is file 1".getBytes(), null);
		System.out.println(fm.commandPutFile("node a", parg1).code.msg);
		
		CmdPutFileArgument parg2 = new CmdPutFileArgument("file2", "This is new file 2".getBytes(), null);
		System.out.println(fm.commandPutFile("node a", parg2).code.msg);
		
		CmdPutFileArgument parg3 = new CmdPutFileArgument("file3", "This is file 3".getBytes(), null);
		System.out.println(fm.commandPutFile("node b", parg3).code.msg);
		
		CmdGetFileArgument garg1 = new CmdGetFileArgument("file1", null);
		CmdGetFileResponse rsp1 = fm.commandGetFile("node a", garg1);
		System.out.println(rsp1.code.msg);
		if (rsp1.code == ErrorCode.OK)
			System.out.println(new String(rsp1.data));
		
		CmdGetFileArgument garg2 = new CmdGetFileArgument("file2", null);
		CmdGetFileResponse rsp2 = fm.commandGetFile("node a", garg2);
		System.out.println(rsp2.code.msg);
		if (rsp2.code == ErrorCode.OK)
			System.out.println(new String(rsp2.data));
		
		System.out.println(new Gson().toJson(fm.files));
		fm.wrapUp();
	}

	public static void printByteArray(String label, byte[] array) {
		System.out.print(label + ":\n");
		for (int i = 0; i < array.length; i++) {
			System.out.printf("%02X ", array[i]);
			if (((i+1) % 32 == 0) && (i < array.length-1)) {
				System.out.println();
			}
		}
		System.out.println();
		System.out.println();
	}
}