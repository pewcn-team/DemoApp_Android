
package com.example.android.wifidirect.discovery;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.TextView;

import com.example.android.wifidirect.discovery.WiFiChatFragment.MessageTarget;
import com.example.android.wifidirect.discovery.WifiP2PConnection.StateChangeListener;
import com.example.android.wifidirect.discovery.WifiPeerList.DeviceClickListener;
import com.example.android.wifidirect.discovery.WifiPeerList.WiFiDevicesAdapter;
import com.example.app.ControlFragmentCar;
import com.example.app.ControlFragmentTank;
import com.example.connection.DataTransfer;
import com.example.connection.DataTransfer.IConnectionListener;
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

    // TXT RECORD properties
    public static final String TXTRECORD_PROP_AVAILABLE = "available";
    public static final String SERVICE_INSTANCE = "_wifidemotest";
    public static final String SERVICE_REG_TYPE = "_presence._tcp";

    public static final int MESSAGE_READ = 0x400 + 1;
    public static final int MY_HANDLE = 0x400 + 2;
    public static final int UPDATE_STATE = 0x400 + 3;

    public static final String TYPE_CAR = "car";
    public static final String TYPE_TANK = "tank";

    private WifiP2pManager mP2pmanager;

    static final int SERVER_PORT = 4545;

    private final IntentFilter intentFilter = new IntentFilter();
    private Channel channel;
    private WiFiDirectBroadcastReceiver receiver = null;
    private WifiP2pDnsSdServiceRequest serviceRequest;

    private WiFiChatFragment chatFragment;
    //private WiFiDirectServicesList servicesList;
 //   private WifiPeerList peerList;
    private WebRTCFragment webRTCFragment;

    private TextView statusTxtView;
    private boolean hasPeer = false;
    private WifiP2PConnection mConnection;
    private WifiManager mWifiManager;
    private boolean mIsServer;
    private boolean mIsClientCreated = false;
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
       // peerList = new WifiPeerList();
//        getFragmentManager().beginTransaction()
//                .add(R.id.container_root, peerList, "services").commit();
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

    /**
     * Registers a local service and then initiates a service discovery
     */
    private void startRegistrationAndDiscovery() {
        Map<String, String> record = new HashMap<String, String>();
        record.put(TXTRECORD_PROP_AVAILABLE, "visible");

//        manager.addLocalService(channel, service, new ActionListener() {
//
//            @Override
//            public void onSuccess() {
//                appendStatus("Added Local Service");
//            }
//
//            @Override
//            public void onFailure(int error) {
//                appendStatus("Failed to add a service");
//            }
//        });

//        manager.discoverPeers(channel, new ActionListener() {
//			
//			@Override
//			public void onSuccess() {
//				// TODO Auto-generated method stub
//				Toast.makeText(WiFiServiceDiscoveryActivity.this, "Find a peer", Toast.LENGTH_SHORT).show();
//			}
//				@Override
//				public void onFailure(int reason) {
//					// TODO Auto-generated method stub
//					
//				}
//			});
    }

    private void discoverService() {

        /*
         * Register listeners for DNS-SD services. These are callbacks invoked
         * by the system when a service is actually discovered.
         */

//        manager.setDnsSdResponseListeners(channel,
//                new DnsSdServiceResponseListener() {
//
//                    @Override
//                    public void onDnsSdServiceAvailable(String instanceName,
//                            String registrationType, WifiP2pDevice srcDevice) {
//
//                        // A service has been discovered. Is this our app?
//
//                        if (instanceName.equalsIgnoreCase(SERVICE_INSTANCE)) {
//
//                            // update the UI and add the item the discovered
//                            // device.
//                            WiFiDirectServicesList fragment = (WiFiDirectServicesList) getFragmentManager()
//                                    .findFragmentByTag("services");
//                            if (fragment != null) {
//                            }
//                        }
//
//                    }
//                }, new DnsSdTxtRecordListener() {
//
//                    /**
//                     * A new TXT record is available. Pick up the advertised
//                     * buddy name.
//                     */
//                    @Override
//                    public void onDnsSdTxtRecordAvailable(
//                            String fullDomainName, Map<String, String> record,
//                            WifiP2pDevice device) {
//                        Log.d(TAG,
//                                device.deviceName + " is "
//                                        + record.get(TXTRECORD_PROP_AVAILABLE));
//                    }
//                });

        // After attaching listeners, create a service request and initiate
        // discovery.
//        serviceRequest = WifiP2pDnsSdServiceRequest.newInstance();
//        manager.addServiceRequest(channel, serviceRequest,
//                new ActionListener() {
//
//                    @Override
//                    public void onSuccess() {
//                        appendStatus("Added service discovery request");
//                    }
//
//                    @Override
//                    public void onFailure(int arg0) {
//                        appendStatus("Failed adding service discovery request");
//                    }
//                });
//        manager.discoverServices(channel, new ActionListener() {
//
//            @Override
//            public void onSuccess() {
//                appendStatus("Service discovery initiated");
//            }
//
//            @Override
//            public void onFailure(int arg0) {
//                appendStatus("Service discovery failed");
//
//            }
//        });


    }

    WifiP2pDevice mDevice;

    @Override
    public void connectP2p(WifiP2pDevice device) {
//    	mDevice = device;
//        WifiP2pConfig config = new WifiP2pConfig();
//        config.deviceAddress = device.deviceAddress;
//        config.wps.setup = WpsInfo.PBC;
//        if (serviceRequest != null)
//        	mP2pmanager.removeServiceRequest(channel, serviceRequest,
//                    new ActionListener() {
//
//                        @Override
//                        public void onSuccess() {
//                        }
//
//                        @Override
//                        public void onFailure(int arg0) {
//                        }
//                    });
//
//        mP2pmanager.connect(channel, config, new ActionListener() {
//
//            @Override
//            public void onSuccess() {
//                appendStatus("Connecting to service");
//            }
//
//            @Override
//            public void onFailure(int errorCode) {
//                appendStatus("Failed connecting to service");
//            }
//        });
//    	mConnection.connect(device);
    }

/*    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case MESSAGE_READ:
                byte[] readBuf = (byte[]) msg.obj;
                // construct a string from the valid bytes in the buffer
                String readMessage = new String(readBuf, 0, msg.arg1);
                Log.d(TAG, readMessage);
                (chatFragment).pushMessage("Buddy: " + readMessage);
                break;

            case UPDATE_STATE:
                String stateString = (String)msg.obj;
                this.statusTxtView.setText(stateString);

        }
        return true;
    }*/

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
            if(mServer != null)
            {
                //mServer.onPause();
            }
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

    @Deprecated
    private void initWifiP2P() {
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter
                .addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter
                .addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        mP2pmanager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel = mP2pmanager.initialize(this, getMainLooper(), mConnection);

        startRegistrationAndDiscovery();

        mConnection = new WifiP2PConnection(mP2pmanager, channel, mWifiManager, (WiFiDirectBroadcastReceiver) receiver);
        mConnection.setStateChangeListener(new StateChangeListener() {

            @Override
            public void onStateChanged(int state) {
                switch (state) {
                    case WifiP2PConnection.STATE_INIT:
                        displayState("initial.....");
                        break;
                    case WifiP2PConnection.STATE_READY:
                        displayState("ready......");
                        mConnection.seekPeer();
                        break;
                    case WifiP2PConnection.STATE_SEEKPEER:
                        displayState("seeking available peers....");
                        break;
                    case WifiP2PConnection.STATE_FOUNDPEER:
                        displayState("found available peers");
                        WifiP2pDeviceList peers = mConnection.getAvailablePeers();
                        showAvailablePeers(peers);
                        break;
                    case WifiP2PConnection.STATE_CONNECTING:
                        displayState("connecting......");
                        break;
                    case WifiP2PConnection.STATE_CONNECTED:
                        WifiP2pInfo info = mConnection.getWifiP2pInfo();
                        displayState("connected......");
                        if (info.isGroupOwner) {
                            Log.d(TAG, "Connected as group owner");
                            mIsServer = true;
                        } else {
                            Log.d(TAG, "Connected as peer");
                            mIsServer = false;
                            // mDataTransfer = DataTransfer.createClientTransfer(((MessageTarget)WiFiServiceDiscoveryActivity.this).getHandler(), info.groupOwnerAddress);
                        }
                        break;
                    case WifiP2PConnection.STATE_TIMEOUT:
                        displayState("time out");
                        break;
                    case WifiP2PConnection.STATE_DISCONNECT:
                        displayState("disconnect");
                        break;
                }
            }
        });

        mConnection.initial();
    }

    private void init() {
		Intent intent = new Intent(this, MyService.class);
		startService(intent);		
        mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);

        if (!mIsServer) {
            mWifiManager.setWifiEnabled(false);
            try {
                Thread.currentThread().sleep(2000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
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

        	//重置Wifi状态
            mWifiManager.setWifiEnabled(true);
            try {
                Thread.currentThread().sleep(2000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            mWifiManager.setWifiEnabled(false);
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

    private void disconnect() {
        if (getFragmentManager().findFragmentByTag("control") != null) {
            getFragmentManager().beginTransaction().remove(mControlFragment).commit();
        }

        mServer.onPause();
        init();

        if (mIsServer) {
            mServer.onResume();
        } else {
            mClient.onResume();
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
