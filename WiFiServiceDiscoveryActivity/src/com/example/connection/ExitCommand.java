package com.example.connection;

public class ExitCommand implements ICommand {

	@Override
	public byte[] toBytes() {
		// TODO Auto-generated method stub
		byte[] buffer = new byte[1];
		buffer[0] = ICommand.TYPE_EXIT;
		return buffer;
	}

	@Override
	public ICommand fromBytes(byte[] buffer) {
		// TODO Auto-generated method stub
		if(buffer[0] == ICommand.TYPE_EXIT)
		{
			return this;
		}
		else 
		{
			return null;
		}
	}

}
