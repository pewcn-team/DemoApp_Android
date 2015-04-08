package com.example.wifiap;

import com.example.connection.DataTransferTCP;
import com.example.connection.IConnection;

public abstract class WifiAPBase implements IConnection {

	@Override
	abstract public void initial();

	@Override
	abstract  public void seekPeer();

	@Override
	abstract  public void connect();

	@Override
	abstract  public void timeout();
	
	@Override
	abstract  public void disconnect();

	@Override
	abstract  public void reset();

	@Override
	abstract public void destroy();
	
	@Override
	abstract public void registerOnStateChangeListener(IOnStateChangeListener listener);

	@Override
	abstract public void unregisterOnStateChangeListener(IOnStateChangeListener listener);
	
	abstract public void sendData(byte[] data);
	
	abstract public String getHostAddress();
	
    //abstract public void registerDataReceiver(DataTransferTCP.IDataReceiver dataReceiver);
    
    
    //abstract public void unregisterDataReceiver(DataTransferTCP.IDataReceiver dataReceiver);


}
