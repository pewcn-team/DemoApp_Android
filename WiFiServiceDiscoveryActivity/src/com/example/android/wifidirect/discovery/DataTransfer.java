package com.example.android.wifidirect.discovery;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import android.os.Handler;
import android.util.Log;

public class DataTransfer {
	
	public interface IDataReceiver
	{
		public void onReceiveData(byte[] data);
	}
	
    private InputStream iStream;
    private OutputStream oStream;
    private Handler handler;
    private ServerSocket server_socket = null;
    private Socket socket = null;
    private IDataReceiver dataReceiver = null;
    private Boolean mIsServer = false;
    private InetAddress mGroupOwnerAddress;
    private ArrayList<IDataReceiver> mDataReceiverList = new ArrayList<IDataReceiver>();
    private InetAddress mPeer;
    public void registerDataReceiver(IDataReceiver dataReceiver)
    {
    	mDataReceiverList.add(dataReceiver);
    }
    
    public void unregisterDataReceiver(IDataReceiver dataReceiver)
    {
    	mDataReceiverList.remove(dataReceiver);
    }
    
	public static DataTransfer createClientTransfer(Handler handler, InetAddress groupOwnerAddress)
	{
		DataTransfer transfer = null;
		try {
			transfer = new DataTransfer();
			transfer.mIsServer = false;
			transfer.socket = new Socket();
			transfer.socket.bind(null);
			transfer.mGroupOwnerAddress = groupOwnerAddress;
			Log.v(WiFiServiceDiscoveryActivity.TAG, "createClientTransfer");
			transfer.handler = handler;
			transfer.startThread();
		} catch (IOException e) {
			try {
				transfer.socket.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			transfer = null;
			e.printStackTrace();
		}
		return transfer;
	}
	
	public static DataTransfer createServerTransfer(Handler handler, IDataReceiver dataReceiver)
	{
		DataTransfer transfer = null;
		try {
			transfer = new DataTransfer();
			transfer.mIsServer = true;
			transfer.server_socket = new ServerSocket();
			transfer.server_socket.setReuseAddress(true);
			transfer.server_socket.bind(new InetSocketAddress(4545));
			transfer.handler = handler;
			transfer.dataReceiver = dataReceiver;
			Log.v(WiFiServiceDiscoveryActivity.TAG, "createServerTransfer");
			transfer.startThread();
		} catch (IOException e) {
			
			e.printStackTrace();
			Log.w(WiFiServiceDiscoveryActivity.TAG, e.getMessage()); 
			if(null != transfer.server_socket)
			{
				try {
					transfer.server_socket.close();
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
            oStream.write(data);
        } catch (IOException e) {
            Log.e(WiFiServiceDiscoveryActivity.TAG, "Exception during write", e);
        }	
	}
	
	
	private void startThread()
	{
		Thread t = new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					if (!mIsServer) {

						socket.connect(
								new InetSocketAddress(
										mGroupOwnerAddress.getHostAddress(),
										WiFiServiceDiscoveryActivity.SERVER_PORT),
								5000);

					} 
					else 
					{
						socket = server_socket.accept();
					}
					mPeer = socket.getInetAddress();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				handler.obtainMessage(WiFiServiceDiscoveryActivity.MY_HANDLE,
						this).sendToTarget();
				Log.d(WiFiServiceDiscoveryActivity.TAG, "start loop");
				try {
					iStream = socket.getInputStream();
					oStream = socket.getOutputStream();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
					return;
				}
				byte[] buffer = new byte[1024];
				int bytes;
				while (true) {
					try {
						// Read from the InputStream
						bytes = iStream.read(buffer);
						if (bytes == -1) {
							break;
						}
						Log.d(WiFiServiceDiscoveryActivity.TAG,
								"Rec:" + String.valueOf(buffer));
						receiveData(buffer);
					} catch (IOException e) {
						Log.e(WiFiServiceDiscoveryActivity.TAG, "disconnected",
								e);
					}
				}
			}
		});
		t.start();
	}
	
	private void receiveData(byte[] buffer)
	{
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
			server_socket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
	
	
}
