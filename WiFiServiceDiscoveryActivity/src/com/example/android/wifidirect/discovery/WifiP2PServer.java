package com.example.android.wifidirect.discovery;

import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.GroupInfoListener;
import android.util.Log;

public class WifiP2PServer {
	WifiP2pManager mManager;
	Channel mChannel;
	
	public WifiP2PServer(WifiP2pManager manager, Channel channel)
	{
		mManager = manager;
		mChannel = channel;
	}
	
	 public void start()
	{
		
		mManager.createGroup(mChannel, new ActionListener() {
			
			@Override
			public void onSuccess() {

//				synchronized (mLock)
//				{
//					mLock.notify();
//				}
			}
			
			@Override
			public void onFailure(int reason) {
//				synchronized (mLock)
//				{
//					mLock.notify();
//				}
			}
		});
//		try {
//			synchronized (mLock)
//			{
//				mLock.wait();
//			}
//			
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		

	}

}
