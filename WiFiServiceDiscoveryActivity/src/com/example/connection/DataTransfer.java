package com.example.connection;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

import com.example.android.wifidirect.discovery.WiFiServiceDiscoveryActivity;

import android.os.Handler;
import android.util.Log;

public class DataTransfer {
	
	public interface IDataReceiver
	{
		public void onReceiveData(byte[] data);
	}
	
	public interface IConnectionListener
	{
		
		public void onConnect();
		
		public void onDisconnect();
		
	}
	
    private InputStream mIStream;
    private OutputStream mOStream;
    private ServerSocket mServerSocket = null;
    private Socket mSocket = null;
    private IDataReceiver mDataReceiver = null;
    private Boolean mIsServer = false;
    private InetAddress mGroupOwnerAddress;
    private ArrayList<IDataReceiver> mDataReceiverList = new ArrayList<IDataReceiver>();
    private InetAddress mPeer;
    private IConnectionListener mConnectionListener = null;
    private boolean mIsConnected = false;
    public void registerDataReceiver(IDataReceiver dataReceiver)
    {
    	mDataReceiverList.add(dataReceiver);
    }
    
    public void unregisterDataReceiver(IDataReceiver dataReceiver)
    {
    	mDataReceiverList.remove(dataReceiver);
    }
    
	public static DataTransfer createClientTransfer(Handler handler, InetAddress groupOwnerAddress, IConnectionListener listener)
	{
		DataTransfer transfer = null;
		transfer = new DataTransfer();
		transfer.mIsServer = false;
		transfer.mGroupOwnerAddress = groupOwnerAddress;
		Log.v(WiFiServiceDiscoveryActivity.TAG, "createClientTransfer");
		transfer.mConnectionListener = listener;
		transfer.startClientThread();
		return transfer;
	}
	
	public static DataTransfer createServerTransfer(Handler handler, IConnectionListener listener)
	{
		DataTransfer transfer = null;
		try {
			transfer = new DataTransfer();
			transfer.mIsServer = true;
			transfer.mServerSocket = new ServerSocket(4545);
			transfer.mConnectionListener = listener;
			Log.v(WiFiServiceDiscoveryActivity.TAG, "createServerTransfer");
			transfer.startServerThread();
		} catch (IOException e) {
			
			e.printStackTrace();
			Log.w(WiFiServiceDiscoveryActivity.TAG, e.getMessage()); 
			if(null != transfer.mServerSocket)
			{
				try {
					transfer.mServerSocket.close();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
			transfer = null;
			
			Log.w(WiFiServiceDiscoveryActivity.TAG, e.getMessage());
		}
		return transfer;
	}
	
	public void sendData(byte[] data){
        try {
            mOStream.write(data);
        } catch (IOException e) {
            Log.e(WiFiServiceDiscoveryActivity.TAG, "Exception during write", e);
        }	
	}
	
	private void startServerThread()
	{
		Thread t = new Thread(new Runnable() {

			@Override
			public void run() {
				try {

					while (true) {
						mSocket = mServerSocket.accept();
						mIsConnected = true;
						handleSocket();
					}

				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		});
		t.start();
	}
	
	
	private void startClientThread()
	{
		Thread t = new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					String hostAddress = mGroupOwnerAddress.getHostAddress();
					String []sub = hostAddress.split("\\.");
					if(sub.length == 4)
					{
						hostAddress = String.format("%s.%s.%s.1", sub[0], sub[1], sub[2]);
					}
					mSocket = new Socket(hostAddress, 4545);
					mIsConnected = true;
					handleSocket();
				} catch (UnknownHostException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		});
		t.start();
	}
	
	private void receiveData(byte[] buffer)
	{
		if(null != new ExitCommand().fromBytes(buffer))
		{
			mConnectionListener.onDisconnect();
			return;
		}
		
		for(IDataReceiver dataReceiver:mDataReceiverList)
		{
			dataReceiver.onReceiveData(buffer);
		}
	}
	
    public InetAddress getPeerAddress(){
        return mPeer;
    }
    
    public void destroy()
    {
    	try {
    		Log.v(WiFiServiceDiscoveryActivity.TAG, "server_socket close");
    		if(mIsServer)
    		{

    			if(null != mSocket)
    			{
    		    	ExitCommand exit = new ExitCommand();
    		    	sendData(exit.toBytes());
    				mSocket.close();
    			}
    			if(null != mServerSocket)
    			{
    				mServerSocket.close();
    			}
    			
    		}
    		else
    		{
    			if(null != mSocket)
    			{
    		    	ExitCommand exit = new ExitCommand();
    		    	sendData(exit.toBytes());
    				mSocket.close();
    			}
    		}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	mIsConnected = false;
    }
    
    public void handleSocket()
    {
    	mPeer = mSocket.getInetAddress();
		Log.d(WiFiServiceDiscoveryActivity.TAG, "start loop");
		try {
			mIStream = mSocket.getInputStream();
			mOStream = mSocket.getOutputStream();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return;
		}

		mConnectionListener.onConnect();
		byte[] buffer = new byte[1024];
		int bytes;
		while (mIsConnected) {
			try {
				bytes = mIStream.read(buffer);
				Log.d(WiFiServiceDiscoveryActivity.TAG,
						"Rec:" + String.valueOf(buffer));
				receiveData(buffer);
			} catch (IOException e) {
				Log.e(WiFiServiceDiscoveryActivity.TAG, "disconnected",
						e);
				break;
			}
		}    	
    }
    
	
	
}
