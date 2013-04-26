package com.scs.sdfs.test;

import java.io.File;

import com.scs.sdfs.server.Crypto;

public class CryptoTest {

	public static void main(String[] args) {
		new File("data").mkdirs();
		
		Crypto.init("server", null, "server", "server.p12");

		String dataFile = "data/test.file";
		String metaFile = "data/meta.file";
		String dataFile2 = "data/test2.file";
		
		String fileContents = "Hello World!";
		printByteArray("Data", fileContents.getBytes());
		byte[] key = Crypto.getKeyFromData(fileContents.getBytes());
		printByteArray("FileKey", key);
		byte[] iv = Crypto.saveToDisk(dataFile, fileContents.getBytes(), key, true);
		
		String metaContents = "Metadata!";
		Crypto.saveToDisk(metaFile, metaContents.getBytes(), true);
		
		byte[] meta = Crypto.loadFromDisk(metaFile);
		printByteArray("Metadata", meta);
		System.out.println(new String(meta));
		System.out.println();
		
		byte[] data = Crypto.loadFromDisk(dataFile, key, iv);
		printByteArray("FileData", data);
		System.out.println(new String(data));
		System.out.println();
		
		byte[] key2 = Crypto.getKeyFromData(data);
		printByteArray("FileKey2", key2);
		byte[] iv2 = Crypto.saveToDisk(dataFile2, data, key2, true);
		
		byte[] data2 = Crypto.loadFromDisk(dataFile2, key2, iv2);
		printByteArray("FileData2", data2);
		System.out.println(new String(data2));
		System.out.println();
	}

	private static void printByteArray(String label, byte[] array) {
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