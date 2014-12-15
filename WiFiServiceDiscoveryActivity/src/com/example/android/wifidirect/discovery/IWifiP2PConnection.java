package com.example.android.wifidirect.discovery;

import android.net.wifi.p2p.WifiP2pDevice;

public interface IWifiP2PConnection {
	public void initial();
	public void seekPeer();
	public void connect(WifiP2pDevice device);
	public void timeout();
	public void disconnect();
	public void reset();

}
