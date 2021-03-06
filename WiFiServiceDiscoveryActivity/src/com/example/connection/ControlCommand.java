package com.example.connection;

public class ControlCommand implements ICommand {

	public byte mType = 0;
	public byte mDirection;
	public byte mState;
	@Override
	public byte[] toBytes() {
		// TODO Auto-generated method stub
		byte[] buffer = new byte[3];
		buffer[0] = mType;
		buffer[1] = mDirection;
		buffer[2] = mState;
		return buffer;
	}

	@Override
	public ICommand fromBytes(byte[] buffer) {
		// TODO Auto-generated method stub
		if(buffer[0] == ICommand.TYPE_CONTROL)
		{
			mDirection = buffer[1];
			mState = buffer[2];
			return this;
		}
		return null;
	}

}
