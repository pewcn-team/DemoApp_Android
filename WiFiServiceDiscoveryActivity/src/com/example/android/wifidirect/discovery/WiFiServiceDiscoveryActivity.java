
package com.example.android.wifidirect.discovery;

import android.app.Activity;
import android.app.Fragment;
import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardLock;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.ChannelListener;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
import android.net.wifi.p2p.WifiP2pManager.DnsSdServiceResponseListener;
import android.net.wifi.p2p.WifiP2pManager.DnsSdTxtRecordListener;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.wifidirect.discovery.DataTransfer.IConnectionListener;
import com.example.android.wifidirect.discovery.WiFiChatFragment.MessageTarget;
import com.example.android.wifidirect.discovery.WifiP2PConnection.StateChangeListener;
import com.example.android.wifidirect.discovery.WifiPeerList.DeviceClickListener;
import com.example.android.wifidirect.discovery.WifiPeerList.WiFiDevicesAdapter;
import com.example.wifiap.WifiAPClient;
import com.example.wifiap.WifiAPServer;

import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

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
public class WiFiServiceDiscoveryActivity extends Activity implements
        DeviceClickListener, Handler.Callback, MessageTarget
        {
	
	

    public static final String TAG = "wifidirectdemo";

    // TXT RECORD properties
    public static final String TXTRECORD_PROP_AVAILABLE = "available";
    public static final String SERVICE_INSTANCE = "_wifidemotest";
    public static final String SERVICE_REG_TYPE = "_presence._tcp";

    public static final int MESSAGE_READ = 0x400 + 1;
    public static final int MY_HANDLE = 0x400 + 2;
    public static final int UPDATE_STATE = 0x400 + 3;

    
    private WifiP2pManager mP2pmanager;

    static final int SERVER_PORT = 4545;

    private final IntentFilter intentFilter = new IntentFilter();
    private Channel channel;
    private WiFiDirectBroadcastReceiver receiver = null;
    private WifiP2pDnsSdServiceRequest serviceRequest;

    private Handler handler = new Handler(this);
    private WiFiChatFragment chatFragment;
    //private WiFiDirectServicesList servicesList;
    private WifiPeerList peerList;
    private WebRTCFragment webRTCFragment;
    
    private TextView statusTxtView;
    private boolean hasPeer = false;
    private DataTransfer mDataTransfer = null;
    private WifiP2PConnection mConnection;
    private WifiManager mWifiManager;
    private boolean mIsServer;
    private boolean mIsClientCreated = false;
    private WifiAPServer mServer = null;
    private WifiAPClient mClient = null;
    private PowerManager.WakeLock mWakeLock = null;
    private ControlFragment mControlFragment = null;
    public Handler getHandler() {
        return handler;
    }

    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v(TAG, "onCreate");
   	 KeyguardManager manager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);  
   	 KeyguardLock keyguardlock = manager.newKeyguardLock("");
   	 keyguardlock.disableKeyguard();
        setContentView(R.layout.main);
        init();
		statusTxtView = (TextView) findViewById(R.id.status_text);
		peerList = new WifiPeerList();
		getFragmentManager().beginTransaction()
				.add(R.id.container_root, peerList, "services").commit();

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
    	//mConnection.disconnect();
    	if(null != mDataTransfer)
    	{
        	mDataTransfer.destroy();    		
    	}
		if(getFragmentManager().findFragmentByTag("control") != null)
		{
			getFragmentManager().beginTransaction().remove(mControlFragment).commit();
		}
        super.onStop();
        System.exit(10);
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
    	mConnection.connect(device);
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case MESSAGE_READ:
                byte[] readBuf = (byte[]) msg.obj;
                // construct a string from the valid bytes in the buffer
                String readMessage = new String(readBuf, 0, msg.arg1);
                Log.d(TAG, readMessage);
                (chatFragment).pushMessage("Buddy: " + readMessage);
                break;

            case MY_HANDLE:
            	mControlFragment = new ControlFragment(this, mDataTransfer.getPeerAddress().getHostAddress(), new MediaEngineObserver() {
					@Override
					public void newStats(String stats) {
                      handler.obtainMessage(WiFiServiceDiscoveryActivity.UPDATE_STATE, stats)
                      .sendToTarget();
						
					}
				}, mDataTransfer, mIsServer);
            	getFragmentManager().beginTransaction().remove(peerList).commit();
            	getFragmentManager().beginTransaction().replace(R.id.container_root, mControlFragment, "control").commit();           	
                break;
            case UPDATE_STATE:
                String stateString = (String)msg.obj;
                this.statusTxtView.setText(stateString);

        }
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        if(null == mWakeLock)
        {
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);  
            mWakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, this.getClass().getCanonicalName());  
            mWakeLock.acquire();         	
        }

        if(mIsServer)
        {
        	mServer.onResume();
        }
        else
        {
        	mClient.onResume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mWakeLock !=null&& mWakeLock.isHeld()) {  
            mWakeLock.release();  
            mWakeLock =null;  
        }    
        if(mIsServer)
        {
        	 mServer.onPause();
        }
        else
        {
        	mClient.onPause();
        }
       
    }


    public void appendStatus(String status) {
        String current = statusTxtView.getText().toString();
        statusTxtView.setText(current + "\n" + status);
    }
    
    private void showAvailablePeers(WifiP2pDeviceList peers)
    {
    	if(null != peers)
    	{
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
    
    private void displayState(String message)
    {
    	statusTxtView.setText(message);
    }
    
    @Deprecated
    private void initWifiP2P()
    {
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
			switch(state)
			{
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
		            if(null!= mDataTransfer)
		            {
		            	mDataTransfer.destroy();
		            }
		            mIsServer = true;
//		            mDataTransfer = DataTransfer.createServerTransfer(((MessageTarget)WiFiServiceDiscoveryActivity.this).getHandler(), new DataTransfer.IDataReceiver() {
//						
//						@Override
//						public void onReceiveData(byte[] data) {
//							// TODO Auto-generated method stub
//			                final String readMessage = new String(data);
//			                Log.d(TAG, readMessage);
//			                runOnUiThread(new Runnable() {
//								@Override
//								public void run() {
//									(chatFragment).pushMessage("Buddy: " + readMessage);							
//								}
//							});				
//						}
//					});
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
    
    private void init()
    {
    	mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        boolean isServer = true;
		if (!isServer) {
			mWifiManager.setWifiEnabled(false);
        	try {
				Thread.currentThread().sleep(2000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			mWifiManager.setWifiEnabled(true);
			mClient = new WifiAPClient(this,
					new WifiAPClient.OnConnectListener() {

						@Override
						public void onConnect(InetAddress address) {
							mIsServer = false;
							final InetAddress fAddress = address;
							if (false == mIsClientCreated) {
								mIsClientCreated = true;
								Thread t = new Thread(new Runnable() {
									@Override
									public void run() {
										if (null == mDataTransfer) {
											mDataTransfer = DataTransfer
													.createClientTransfer(
															((MessageTarget) WiFiServiceDiscoveryActivity.this)
																	.getHandler(),
															fAddress, mConnectionListener);
										}
									}
								});
								t.start();
							}

						}

						@Override
						public void onDisconnect() {
							disconnect();
							
						}
					});
			mClient.scanAP();
		}
        else
        {

        	mWifiManager.setWifiEnabled(true);
        	try {
				Thread.currentThread().sleep(2000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        	mWifiManager.setWifiEnabled(false);
        	mServer = new WifiAPServer(this,
					new WifiAPServer.OnConnectListener() {
						@Override
						public void onConnect() {
							mIsServer = true;
							mDataTransfer = DataTransfer
									.createServerTransfer(
											((MessageTarget) WiFiServiceDiscoveryActivity.this)
													.getHandler(),
											new DataTransfer.IDataReceiver() {

												@Override
												public void onReceiveData(
														byte[] data) {
													final String readMessage = new String(
															data);
													Log.d(TAG, readMessage);
													runOnUiThread(new Runnable() {
														@Override
														public void run() {
															(chatFragment)
																	.pushMessage("Buddy: "
																			+ readMessage);
														}
													});
												}
											}, mConnectionListener);
						}

						@Override
						public void onDisconnect() {
							disconnect();
						}
					});
			
		}
    }
    
    DataTransfer.IConnectionListener mConnectionListener = new IConnectionListener() {
		
		@Override
		public void onDisconnect() {
			disconnect();
		}
	};
	
	private void disconnect()
	{
		if(getFragmentManager().findFragmentByTag("control") != null)
		{
			getFragmentManager().beginTransaction().remove(mControlFragment).commit();
		}
		if(null != mDataTransfer)
		{
			mDataTransfer.destroy();
		}
		mServer.onPause();
		init();	
		
        if(mIsServer)
        {
        	mServer.onResume();
        }
        else
        {
        	mClient.onResume();
        }
	}
}
