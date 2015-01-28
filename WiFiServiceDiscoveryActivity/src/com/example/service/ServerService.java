package com.example.service;


import com.example.android.wifidirect.discovery.WiFiServiceDiscoveryActivity;
import com.example.app.ServerActivity;
import com.example.connection.BatteryState;
import com.example.connection.IConnection.ConnectionState;
import com.example.connection.IConnection.IOnStateChangeListener;
import com.example.wifiap.WifiAPServer;

import android.app.ApplicationErrorReport.BatteryInfo;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class ServerService extends Service {
	WifiAPServer mServer = null;
	public class LocalService extends Binder
	{
		public ServerService getService()
		{
			return ServerService.this;
		}
	}
	
	
	LocalService mBinder = new LocalService();
	private int mCurrentBatteryPercent = 0;
	
	BroadcastReceiver mBatteryReceiver = new  BroadcastReceiver(){  
		  
	    @Override  
	    public void onReceive(Context context, Intent intent) {  
	        if (Intent.ACTION_BATTERY_CHANGED.equals(intent.getAction())) {  
	            int  level=intent.getIntExtra("level", 0);  
	            int scal=intent.getIntExtra("scal", 100);  
	            int percent = level*100/scal;
	            BatteryState state = new BatteryState();
	            mCurrentBatteryPercent = percent;
	            state.mBatteryPercent = percent;
	            mServer.sendData(state.toBytes());
	        }  
	    }  
	  
	};  
	
	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}
	
	
	
	@Override
	public void onCreate() {
		super.onCreate();
		Log.v(WiFiServiceDiscoveryActivity.TAG, "ServerService onCreate");
		mServer = new WifiAPServer(this);
		mServer.registerOnStateChangeListener(new IOnStateChangeListener() {
			@Override
			public void onStateChange(ConnectionState state) {
				switch (state) {
				case CONNECTED:
					BatteryState bs = new BatteryState(); 
					bs.mBatteryPercent = mCurrentBatteryPercent;
					mServer.sendData(bs.toBytes());
					Intent intent = new Intent();
					intent.putExtra("isServer", true);
					intent.putExtra("address", mServer.getHostAddress());
					intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					intent.setClass(ServerService.this, ServerActivity.class);
					startActivity(intent);
					break;
				case DISCONNECT:
					mServer.reset();
					break;
				case TIMEOUT:
					break;
				default:
					break;
				}
			}
		});
		mServer.initial();
		mServer.seekPeer();
		
		//侦测电量
        BatteryInfo batteryInfo=new BatteryInfo();  
        IntentFilter filter=new IntentFilter(Intent.ACTION_BATTERY_CHANGED);  
        this.registerReceiver(mBatteryReceiver, filter);  
	}
	
	@Override
	public void onDestroy()
	{
		mServer.disconnect();
		unregisterReceiver(mBatteryReceiver);
		super.onDestroy();
	}
	
	public WifiAPServer getServer()
	{
		return mServer;
	}
	
	 //由于该Activity会被stop掉，导致报Activity has been destroy 的错误，所以另外拉起一个Activity。

	
	
	
//	public void onStop()
//	{
//		Intent intent =new Intent(this, WiFiServiceDiscoveryActivity.class);
//		this.startActivity(intent);
//	}

}
