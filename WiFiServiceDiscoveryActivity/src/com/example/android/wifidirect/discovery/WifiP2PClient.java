package com.example.android.wifidirect.discovery;

import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.GroupInfoListener;
import android.util.Log;

public class WifiP2PClient {
	WifiP2pManager mManager;
	Channel mChannel;
	
	public WifiP2PClient(WifiP2pManager manager, Channel channel)
	{
		mManager = manager;
		mChannel = channel;
	}
	
	public void start()
	{
		mManager.requestGroupInfo(mChannel, new GroupInfoListener() {
			
			@Override
			public void onGroupInfoAvailable(WifiP2pGroup group) {
				// TODO Auto-generated method stub
				if(null!= group )
				{
					String passPhrase = group.getPassphrase();
					Log.v(WiFiServiceDiscoveryActivity.TAG, "passPhrase is " + passPhrase);					
				}

			}
		});
	}

}
