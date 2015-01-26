package service;


import com.example.android.wifidirect.discovery.WiFiServiceDiscoveryActivity;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class MyService extends Service {

	public class LocalService extends Binder
	{
		public MyService getService()
		{
			return MyService.this;
		}
	}
	
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
        	Log.v("wifidirectdemo", "receive stop");
        	if(intent.getAction().equals("stop"))
        	{
        		Intent newIntent =new Intent(MyService.this, WiFiServiceDiscoveryActivity.class);
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
	
	
	
//	public void onStop()
//	{
//		Intent intent =new Intent(this, WiFiServiceDiscoveryActivity.class);
//		this.startActivity(intent);
//	}

}
