package com.example.connection;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

import com.example.android.wifidirect.discovery.WiFiServiceDiscoveryActivity;

import android.os.Handler;
import android.util.Log;

public class DataTransferTCP implements IDataTransfer{

    private InputStream mIStream;
    private OutputStream mOStream;
    private ServerSocket mServerSocket = null;
    private Socket mSocket = null;
    private Boolean mIsServer = false;
    private InetAddress mGroupOwnerAddress;
    private ArrayList<ITransferListener> mTransferListenerList = new ArrayList<ITransferListener>();
    private InetAddress mPeer;
    private IDataTransfer.ITransferListener mConnectionListener = null;
    private boolean mIsConnected = false;
	private boolean mIsStarted = false;

    
	public static DataTransferTCP createClientTransfer(InetAddress groupOwnerAddress, IDataTransfer.ITransferListener listener)
	{
		DataTransferTCP transfer;
		transfer = new DataTransferTCP();
		transfer.mIsServer = false;
		transfer.mGroupOwnerAddress = groupOwnerAddress;
		Log.v(WiFiServiceDiscoveryActivity.TAG, "createClientTransfer");
		transfer.mConnectionListener = listener;
		return transfer;
	}
	
	public static DataTransferTCP createServerTransfer(IDataTransfer.ITransferListener listener)
	{
		DataTransferTCP transfer = null;
		try {
			transfer = new DataTransferTCP();
			transfer.mIsServer = true;
			transfer.mServerSocket = new ServerSocket(4545);
			transfer.mConnectionListener = listener;
			Log.v(WiFiServiceDiscoveryActivity.TAG, "createServerTransfer");
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

	/**
	 * 启动transfer,会开启一个数据收发的线程
	 */
	@Override
	public void startTransfer() {
		if(!mIsStarted)
		{
			Thread t = new Thread(new Runnable() {

				@Override
				public void run() {
					try {
						mSocket = mServerSocket.accept();
						mIsConnected = true;
						handleSocket();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}
			});
			t.start();
		}

	}

	public void sendData(byte[] data){
		if(null != mOStream)
		{
	        try {
	            mOStream.write(data);
	        } catch (IOException e) {
	            Log.e(WiFiServiceDiscoveryActivity.TAG, "Exception during write", e);
	        }					
		}

	}

	/**
	 * 停止transfer,停止数据收发的线程
	 */
	@Override
	public void stopTransfer() {
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

	@Override
	public void registerTransferListener(ITransferListener listener) {
		mTransferListenerList.add(listener);
	}

	@Override
	public void unregisterTransferListener(ITransferListener listener) {
		mTransferListenerList.remove(listener);
	}

	public InetAddress getPeerAddress(){
		return mPeer;
	}


	private void receiveData(byte[] buffer)
	{
		for(ITransferListener listener:mTransferListenerList)
		{
			listener.onDataReceived(buffer);
		}
	}
	

    
    private void handleSocket()
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

		mConnectionListener.onCreate();
		byte[] buffer = new byte[1024];
		while (mIsConnected) {
			Log.d(WiFiServiceDiscoveryActivity.TAG, "Rec:" + String.valueOf(buffer));
			receiveData(buffer);
		}
	}
}
