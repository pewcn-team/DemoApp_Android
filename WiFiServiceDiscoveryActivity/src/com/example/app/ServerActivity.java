package com.example.app;

import org.webrtc.webrtcdemo.MediaEngineObserver;

import com.example.android.wifidirect.discovery.R;
import com.example.android.wifidirect.discovery.R.id;
import com.example.android.wifidirect.discovery.R.layout;
import com.example.android.wifidirect.discovery.WiFiServiceDiscoveryActivity;
import com.example.service.ServerService;
import com.example.service.ServerService.LocalService;
import com.example.wifiap.WifiAPServer;

import android.app.Activity;
import android.app.Fragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;

public class ServerActivity extends Activity {

    private Fragment mControlFragment = null;
    private WifiAPServer mServer;
    private String mAddress;
    ServiceConnection mServiceConnection = new ServiceConnection() {
		
		@Override
		public void onServiceDisconnected(ComponentName name) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			// TODO Auto-generated method stub
			ServerService.LocalService localService = (LocalService) service;
			mServer = localService.getService().getServer();
			runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
			        mControlFragment = new ControlFragmentCar(ServerActivity.this, mAddress, new MediaEngineObserver() {
			            @Override
			            public void newStats(String stats) {


			            }
			        }, mServer, true, true);
			        
			        getFragmentManager().beginTransaction().add(R.id.container_root, mControlFragment, "control").commitAllowingStateLoss();
					
				}
			});
		}
	};
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v(WiFiServiceDiscoveryActivity.TAG, "service onCreate");
        getWindow().addFlags(LayoutParams.FLAG_TURN_SCREEN_ON | LayoutParams.FLAG_DISMISS_KEYGUARD | LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.main);
        
        Button mBtnTank = (Button)findViewById(R.id.button_tank);
        mBtnTank.setVisibility(View.GONE);
        
        Button mBtnCar = (Button)findViewById(R.id.button_car);
        mBtnCar.setVisibility(View.GONE);
        Intent intent = getIntent();
        mAddress = intent.getStringExtra("address");
        Intent serviceIntent = new Intent(this, ServerService.class);
        bindService(serviceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);

    }
    
    @Override
    public void onDestroy()
    {
    	super.onDestroy();
    	unbindService(mServiceConnection);
    }
}
