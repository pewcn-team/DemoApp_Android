package com.example.connection;

/**
 * Created by yihao on 15/2/24.
 */
public class HelloCommand implements ICommand{
    @Override
    public byte[] toBytes() {
        // TODO Auto-generated method stub
        byte[] buffer = new byte[1];
        buffer[0] = ICommand.TYPE_HELLO;
        return buffer;
    }

    @Override
    public ICommand fromBytes(byte[] buffer) {
        // TODO Auto-generated method stub
        if(buffer[0] == ICommand.TYPE_HELLO)
        {
            return this;
        }
        else
        {
            return null;
        }
    }
}
