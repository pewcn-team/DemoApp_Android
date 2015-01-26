package service;


import com.example.android.wifidirect.discovery.WiFiServiceDiscoveryActivity;
import com.example.wifiap.WifiAPServer;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class ServerService extends Service {

	public class LocalService extends Binder
	{
		public ServerService getService()
		{
			return ServerService.this;
		}
	}
	
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
        	Log.v("wifidirectdemo", "receive stop");
        	if(intent.getAction().equals("stop"))
        	{
        		Intent newIntent =new Intent(ServerService.this, WiFiServiceDiscoveryActivity.class);
        		startActivity(newIntent);      		
        	}
        }
    };
	
	LocalService mBinder = new LocalService();
	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}
	
	@Override
	public void onCreate()
	{
		super.onCreate();
		registerReceiver(mBroadcastReceiver, null);
		
	}
	
	@Override
	public void onDestroy()
	{
		unregisterReceiver(mBroadcastReceiver);
		super.onDestroy();
	}
	
	public WifiAPServer getServer()
	{
		return #
	}
	
	
	
	
	
//	public void onStop()
//	{
//		Intent intent =new Intent(this, WiFiServiceDiscoveryActivity.class);
//		this.startActivity(intent);
//	}

}
