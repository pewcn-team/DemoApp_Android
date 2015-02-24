
package com.example.android.wifidirect.discovery;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.TextView;

import com.example.android.wifidirect.discovery.WifiPeerList.DeviceClickListener;
import com.example.android.wifidirect.discovery.WifiPeerList.WiFiDevicesAdapter;
import com.example.app.ControlFragmentCar;
import com.example.app.ControlFragmentTank;
import com.example.connection.DataTransfer;
import com.example.connection.IConnection.ConnectionState;
import com.example.connection.IConnection.IOnStateChangeListener;
import com.example.wifiap.WifiAPClient;
import com.example.wifiap.WifiAPServer;
import com.example.connection.IConnection;

import java.util.HashMap;
import java.util.Map;

import org.webrtc.webrtcdemo.MediaEngineObserver;

import service.MyService;

/**
 * The main activity for the sample. This activity registers a local service and
 * perform discovery over Wi-Fi p2p network. It also hosts a couple of fragments
 * to manage chat operations. When the app is launched, the device publishes a
 * chat service and also tries to discover services published by other peers. On
 * selecting a peer published service, the app initiates a Wi-Fi P2P (Direct)
 * connection with the peer. On successful connection with a peer advertising
 * the same service, the app opens up sockets to initiate a chat.
 * {@code WiFiChatFragment} is then added to the the main activity which manages
 * the interface and messaging needs for a chat session.
 */
public class WiFiServiceDiscoveryActivity extends Activity implements
        DeviceClickListener {
    public static final String TAG = "wifidirectdemo";
    public static final String TXTRECORD_PROP_AVAILABLE = "available";
    public static final int MESSAGE_READ = 0x400 + 1;
    public static final int MY_HANDLE = 0x400 + 2;
    private TextView statusTxtView;
    private boolean mIsServer;
    private WifiAPServer mServer = null;
    private WifiAPClient mClient = null;
    private PowerManager.WakeLock mWakeLock = null;
    private Fragment mControlFragment = null;
    private String mVehicleType = "car";
    private Button mBtnTank;
    private Button mBtnCar;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mIsServer = false;
        Log.v(TAG, "onCreate");
        getWindow().addFlags(LayoutParams.FLAG_TURN_SCREEN_ON | LayoutParams.FLAG_DISMISS_KEYGUARD | LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.main);

        statusTxtView = (TextView) findViewById(R.id.status_text);
        mBtnTank = (Button)findViewById(R.id.button_tank);
        mBtnTank.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mVehicleType = "tank";
                init();
            }
        });

        mBtnCar = (Button)findViewById(R.id.button_car);
        mBtnCar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mVehicleType = "car";
                init();
            }
        });
        if(mIsServer)
        {
            init();
        }

    }

    @Override
    protected void onRestart() {
        Log.v(TAG, "onRestart");
        Fragment frag = getFragmentManager().findFragmentByTag("services");
        if (frag != null) {
            getFragmentManager().beginTransaction().remove(frag).commit();
        }
        super.onRestart();
    }

    @Override
    protected void onStop() {

    	Log.v(TAG, "onStop");
    	Intent intent = new Intent("stop");
    	sendBroadcast(intent);
        if (getFragmentManager().findFragmentByTag("control") != null) {
            getFragmentManager().beginTransaction().remove(mControlFragment).commitAllowingStateLoss();
        }
        super.onStop();
        
        //System.exit(10);
    }


    @Override
    public void connectP2p(WifiP2pDevice device) {
    }


    @Override
    public void onResume() {
    	Log.v(TAG, "onResume");
        super.onResume();
        if (null == mWakeLock) {
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            getWindow().addFlags(LayoutParams.FLAG_KEEP_SCREEN_ON);
        }

        if (mIsServer) {
            if(mServer != null)
            {
                mServer.onResume();
            }
        } else {
            if(mClient != null)
            {
                mClient.onResume();
            }

        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mWakeLock != null && mWakeLock.isHeld()) {
            mWakeLock.release();
            mWakeLock = null;
        }
        if (mIsServer) {
        } else {
            if(mClient != null)
            {
                mClient.onPause();
            }
        }

    }



    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            System.exit(0);
            return true;
        }
        return false;
    }


    private void init() {
		Intent intent = new Intent(this, MyService.class);
		startService(intent);

        WifiManager wifiManager;
        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);

        if (!mIsServer) {
            wifiManager.setWifiEnabled(false);
            try {
                Thread.currentThread().sleep(2000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            wifiManager.setWifiEnabled(true);
            mClient = new WifiAPClient(this);
            mClient.registerOnStateChangeListener(new IConnection.IOnStateChangeListener() {

                @Override
                public void onStateChange(ConnectionState state) {
                    switch (state) {
                        case CONNECTED:
                            createControlFragment();
                            break;
                        case DISCONNECT:
                            break;
                        case TIMEOUT:
                            break;
                        default:
                            break;
                    }
                }
            });
            mClient.initial();
            mClient.seekPeer();
            mClient.onResume();
        } else {

        	//重置Wifi状态
            wifiManager.setWifiEnabled(true);
            try {
                Thread.currentThread().sleep(2000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            wifiManager.setWifiEnabled(false);
            mServer = new WifiAPServer(this);
            mServer.registerOnStateChangeListener(new IOnStateChangeListener() {
                @Override
                public void onStateChange(ConnectionState state) {
                    switch (state) {
                        case CONNECTED:
                            createControlFragment();
                            break;
                        case DISCONNECT:
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

        }
    }


    private void createControlFragment() {
        DataTransfer transfer = null;
        if (mIsServer) {
            transfer = mServer.getDataTransfer();
        } else {
            transfer = mClient.getDataTransfer();
            if(mVehicleType.equals("car"))
            {
                mControlFragment = new ControlFragmentCar(this, transfer.getPeerAddress().getHostAddress(), new MediaEngineObserver() {
                    @Override
                    public void newStats(String stats) {


                    }
                }, transfer, mIsServer);
            }
            else if(mVehicleType.equals("tank"))
            {
                mControlFragment = new ControlFragmentTank(this, transfer.getPeerAddress().getHostAddress(), new MediaEngineObserver() {
                    @Override
                    public void newStats(String stats) {


                    }
                }, transfer, mIsServer);
            }
        }
        


        this.runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
		        mBtnCar.setVisibility(View.GONE);
		        mBtnTank.setVisibility(View.GONE);				
			}
		});

        //getFragmentManager().beginTransaction().remove(peerList).commitAllowingStateLoss();
       if(mIsServer)
       {
    	 //由于该Activity会被stop掉，导致报Activity has been destroy 的错误，所以另外拉起一个Activity。
    	   Intent intent = new Intent();
    	   intent.putExtra("isServer", true);
    	   intent.putExtra("address", transfer.getPeerAddress().getHostAddress());
    	   intent.setClass(this, NewActivity.class);
    	   startActivity(intent);
       }
       else
       {
    	   
    	   getFragmentManager().beginTransaction().replace(R.id.container_root, mControlFragment, "control").commitAllowingStateLoss();
       }
        
    }
}
