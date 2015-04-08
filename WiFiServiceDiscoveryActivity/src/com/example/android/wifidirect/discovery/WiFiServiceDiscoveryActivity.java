
package com.example.android.wifidirect.discovery;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.TextView;

import com.example.android.wifidirect.discovery.WifiPeerList.WiFiDevicesAdapter;
import com.example.app.ControlFragmentCar;
import com.example.app.ControlFragmentTank;
import com.example.connection.ExitCommand;
import com.example.connection.IConnection.ConnectionState;
import com.example.service.ServerService;
import com.example.wifiap.WifiAPClient;
import com.example.connection.IConnection;

import org.webrtc.webrtcdemo.MediaEngineObserver;

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
public class WiFiServiceDiscoveryActivity extends Activity {


    public static final String TAG = "wifidirectdemo";


    public static final int MESSAGE_READ = 0x400 + 1;
    public static final int MY_HANDLE = 0x400 + 2;

    public static final String TYPE_CAR = "car";
    public static final String TYPE_TANK = "tank";

    static final int SERVER_PORT = 4545;
    private final IntentFilter intentFilter = new IntentFilter();
    private Channel channel;
    private WiFiDirectBroadcastReceiver receiver = null;
    private TextView statusTxtView;
    private WifiP2PConnection mConnection;
    private WifiManager mWifiManager;
    private boolean mIsServer;
    //private WifiAPServer mServer = null;
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
                mVehicleType = TYPE_TANK;
                init();
            }
        });

        mBtnCar = (Button)findViewById(R.id.button_car);
        mBtnCar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mVehicleType = TYPE_CAR;
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
    public void onResume() {
    	Log.v(TAG, "onResume");
        super.onResume();
        if (null == mWakeLock) {
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            getWindow().addFlags(LayoutParams.FLAG_KEEP_SCREEN_ON);
        }

        if (mIsServer) {
//            if(mServer != null)
//            {
//                mServer.onResume();
//            }
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
//            if(mServer != null)
//            {
//                //mServer.onPause();
//            }
        } else {
            if(mClient != null)
            {
                mClient.onPause();
            }
        }

    }


    public void appendStatus(String status) {
        String current = statusTxtView.getText().toString();
        statusTxtView.setText(current + "\n" + status);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
        	if(mIsServer)
        	{
        		
        	}
        	else
        	{
            	ExitCommand exit = new ExitCommand();
            	mClient.sendData(exit.toBytes());
            	mWifiManager.setWifiEnabled(false);
        	}

            System.exit(0);
            return true;
        }
        return false;
    }


    private void showAvailablePeers(WifiP2pDeviceList peers) {
        if (null != peers) {
            WifiPeerList fragment = (WifiPeerList) getFragmentManager()
                    .findFragmentByTag("services");
            if (fragment != null) {
                WiFiDevicesAdapter adapter = ((WiFiDevicesAdapter) fragment
                        .getListAdapter());
                adapter.clear();
                adapter.addAll(peers.getDeviceList());
                adapter.notifyDataSetChanged();
            }
        }
    }

    private void displayState(String message) {
        statusTxtView.setText(message);
    }

    private void init() {
	
        mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);

        if (!mIsServer) {
            mWifiManager.setWifiEnabled(true);
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

    		Intent intent = new Intent(this, ServerService.class);
    		startService(intent);	
        	//重置Wifi状态
            mWifiManager.setWifiEnabled(true);
            try {
                Thread.currentThread().sleep(2000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            mWifiManager.setWifiEnabled(false);


        }
    }

    private void disconnect() {
        if (getFragmentManager().findFragmentByTag("control") != null) {
            getFragmentManager().beginTransaction().remove(mControlFragment).commit();
        }

        //mServer.onPause();
        init();

        if (mIsServer) {
            //mServer.onResume();
        } else {
            mClient.onResume();
        }
    }

    private void createControlFragment() {
        if (mIsServer) {
            ;//transfer = mServer.getDataTransfer();
        } else {
            //transfer = mClient.getDataTransfer();
            if(mVehicleType.equals(TYPE_CAR))
            {
                mControlFragment = new ControlFragmentCar(this, mClient.getHostAddress(), new MediaEngineObserver() {
                    @Override
                    public void newStats(String stats) {


                    }
                }, mClient, mIsServer, false);
            }
            else if(mVehicleType.equals(TYPE_TANK))
            {
                mControlFragment = new ControlFragmentTank(this, mClient.getHostAddress(), new MediaEngineObserver() {
                    @Override
                    public void newStats(String stats) {
                    }
                }, mClient, mIsServer);
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
       }
       else
       {
    	   getFragmentManager().beginTransaction().replace(R.id.container_root, mControlFragment, "control").commitAllowingStateLoss();
       }
        
    }
}
