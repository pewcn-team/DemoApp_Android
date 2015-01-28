package com.example.connection;

public class BatteryState implements ICommand {

	
	public int mBatteryPercent = 0;
	
	@Override
	public byte[] toBytes() {
		byte[] buffer = new byte[2];
		buffer[0] = ICommand.TYPE_BATTERY;
		buffer[1] = (byte)mBatteryPercent;
		return buffer;
	}

	@Override
	public ICommand fromBytes(byte[] buffer) {
		if(buffer[0] == ICommand.TYPE_BATTERY)
		{
			mBatteryPercent = buffer[1];
			return this;
		}
		return null;
	}

}
