package com.example.connection;

public class ControlCommand implements ICommand {
	public final static byte BUTTON_INDEX_UP = 0;
	public final static byte BUTTON_INDEX_DOWN = 1;
	public final static byte BUTTON_INDEX_LEFT = 2;
	public final static byte BUTTON_INDEX_RIGHT = 3;
	public final static byte BUTTON_STATE_DOWN = 0;
	public final static byte BUTTON_STATE_UP = 1;
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

	public char getCharMessage()
	{
		char message = ' ';
		switch (mState)
		{
			case BUTTON_STATE_DOWN:
				switch (mDirection)
				{
					case BUTTON_INDEX_UP:
						message = 'u';
						break;
					case BUTTON_INDEX_DOWN:
						message = 'd';
						break;
					case BUTTON_INDEX_LEFT:
						message = 'l';
						break;
					case BUTTON_INDEX_RIGHT:
						message = 'r';
						break;
				}
				break;
			case BUTTON_STATE_UP:
				switch (mDirection)
				{
					case BUTTON_INDEX_UP:
						message = 'i';
						break;
					case BUTTON_INDEX_DOWN:
						message = 'f';
						break;
					case BUTTON_INDEX_LEFT:
						message = ';';
						break;
					case BUTTON_INDEX_RIGHT:
						message = 't';
						break;
				}
				break;

		}
		return message;
	}

}
