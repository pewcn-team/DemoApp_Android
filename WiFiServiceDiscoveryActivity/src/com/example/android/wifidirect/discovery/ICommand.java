package com.example.android.wifidirect.discovery;

public interface ICommand {
	public static byte TYPE_CONTROL = 0;
	byte[] toBytes(ICommand command);
	ICommand fromBytes(byte[] buffer);
	
}
