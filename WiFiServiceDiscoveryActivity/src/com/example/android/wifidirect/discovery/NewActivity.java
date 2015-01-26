package com.example.android.wifidirect.discovery;

import org.webrtc.webrtcdemo.MediaEngineObserver;

import com.example.app.ControlFragmentCar;
import com.example.wifiap.WifiAPServer;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;

public class NewActivity extends Activity {

    private Fragment mControlFragment = null;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(LayoutParams.FLAG_TURN_SCREEN_ON | LayoutParams.FLAG_DISMISS_KEYGUARD | LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.main);
        
        Button mBtnTank = (Button)findViewById(R.id.button_tank);
        mBtnTank.setVisibility(View.GONE);
        
        Button mBtnCar = (Button)findViewById(R.id.button_car);
        mBtnCar.setVisibility(View.GONE);
        Intent intent = getIntent();
        String address = intent.getStringExtra("address");
        boolean isServer = intent.getBooleanExtra("isServer", true);
        mControlFragment = new ControlFragmentCar(this, address, new MediaEngineObserver() {
            @Override
            public void newStats(String stats) {


            }
        }, WifiAPServer.mInstance.getDataTransfer(), isServer);
        
        getFragmentManager().beginTransaction().add(R.id.container_root, mControlFragment, "control").commitAllowingStateLoss();
    }
}
