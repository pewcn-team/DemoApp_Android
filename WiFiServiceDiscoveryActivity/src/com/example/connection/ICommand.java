package com.example.connection;

public interface ICommand {
	public static byte TYPE_CONTROL = 0;
	public static byte TYPE_EXIT = -1;
	byte[] toBytes();
	ICommand fromBytes(byte[] buffer);
	
}