package com.example.android.wifidirect.discovery;

import com.example.connection.IConnection;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
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
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

public class WifiP2PConnection implements IConnection, ConnectionInfoListener, ChannelListener {
	
	public interface StateChangeListener
	{
		public void onStateChanged(int state);
	}

	public static final int STATE_INIT = 0;
	public static final int STATE_READY = 1;
	public static final int STATE_SEEKPEER = 2;
	public static final int STATE_FOUNDPEER = 3;  //收到此消息后，调用getAvailablePeers
	public static final int STATE_CONNECTING = 4;
	public static final int STATE_CONNECTED = 5;  //收到此消息后，调用getWifiP2pInfo
	public static final int STATE_TIMEOUT = 6;
	public static final int STATE_DISCONNECT = 7;
	
	WiFiDirectBroadcastReceiver mBroadcastReceiver;
	WifiP2pManager mWifiP2pManager;
	WifiManager mWifiManager;
    private Channel mChannel;
    private int mCurrState;
    private WifiP2pDevice mConnectDevice;
    private StateChangeListener mStateChangeListener;
    private WifiP2pDeviceList mPeers;
    private WifiP2pInfo mWifiP2pInfo;
    
    public WifiP2PConnection(WifiP2pManager p2pManager, Channel channel, WifiManager manager, WiFiDirectBroadcastReceiver broadcastReceiver)
    {
    	mWifiP2pManager = p2pManager;
    	mWifiManager = manager;
    	mBroadcastReceiver = broadcastReceiver;
    	mChannel = channel;
    }
    
    public void setStateChangeListener(StateChangeListener stateChangeListener)
    {
    	mStateChangeListener = stateChangeListener;
    }
    
	@Override
	public void initial() {
		changeState(STATE_INIT);
		AsyncTask<Void, Void, Void> initialTask = new AsyncTask<Void, Void, Void>() {

			@Override
			protected Void doInBackground(Void... params) {
				// TODO Auto-generated method stub
					mWifiManager.setWifiEnabled(false);
					mWifiManager.setWifiEnabled(true);
				return null;
			}
			
		     protected void onPostExecute(Void param) {
		    	 changeState(STATE_READY);
		     }

		};
		initialTask.execute(null, null, null);

	}

	@Override
	public void seekPeer() {
		changeState(STATE_SEEKPEER);
		mWifiP2pManager.discoverPeers(mChannel, new ActionListener() {
			
			@Override
			public void onSuccess() 
			{
			}
				
			@Override
			public void onFailure(int reason) 
			{
				
			}
		});
		
		//启动一个定时器，开始计算超时
		AsyncTask<Void, Void, Void> searchPeerTask = new AsyncTask<Void, Void, Void>() {

			@Override
			protected Void doInBackground(Void... params) {
				// TODO Auto-generated method stub
				try {
					Thread.sleep(10000);

				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				return null;
			}
			
		     protected void onPostExecute(Void param) {
					if(mCurrState==STATE_SEEKPEER)
					{
						timeout();
					}
		     }

		};
		searchPeerTask.execute(null, null, null);

	}

//	@Override
//	public void connect(WifiP2pDevice device) 
//	{
//		changeState(STATE_CONNECTING);
//        WifiP2pConfig config = new WifiP2pConfig();
//        config.deviceAddress = device.deviceAddress;
//        config.wps.setup = WpsInfo.PBC;
//        mConnectDevice = device;
//        mCurrState = STATE_CONNECTING;
//        mWifiP2pManager.connect(mChannel, config, new ActionListener() {
//
//            @Override
//            public void onSuccess() {
//            }
//
//            @Override
//            public void onFailure(int errorCode) {
//            }
//        });
//        
//		AsyncTask<Void, Void, Void> connectTask = new AsyncTask<Void, Void, Void>() {
//
//			@Override
//			protected Void doInBackground(Void... params) {
//				// TODO Auto-generated method stub
//				try {
//					Thread.sleep(20000);
//
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//				}
//
//				return null;
//			}
//			
//		     protected void onPostExecute(Void param) {
//					if(mCurrState==STATE_CONNECTING)
//					{
//						timeout();
//					}
//		     }
//
//		};
//		connectTask.execute(null, null, null);
//
//	}

	@Override
	public void timeout() {
		// TODO Auto-generated method stub
		//changeState(STATE_TIMEOUT);
		if(mCurrState == STATE_SEEKPEER)
		{
			seekPeer();
		}
		else if(mCurrState == STATE_CONNECTING)
		{
			//connect(mConnectDevice);
		}

	}

	@Override
	public void disconnect() {
		//changeState(STATE_DISCONNECT);
        if (mWifiP2pManager != null && mChannel != null) { 
        	mWifiP2pManager.removeGroup(mChannel, new ActionListener() {

                @Override
                public void onFailure(int reasonCode) {
                    Log.d(WiFiServiceDiscoveryActivity.TAG, "Disconnect failed. Reason :" + reasonCode);
                }

                @Override
                public void onSuccess() {
                }

            });
        }
	}

	@Override
	public void reset() {
		// TODO Auto-generated method stub
		disconnect();
		initial();

	}

	@Override
	public void onChannelDisconnected() {
		if (mWifiP2pManager != null) {
            if (mConnectDevice == null
                    || mConnectDevice.status == WifiP2pDevice.CONNECTED) {
                disconnect();
            } else if (mConnectDevice.status == WifiP2pDevice.AVAILABLE
                    || mConnectDevice.status == WifiP2pDevice.INVITED) {

                mWifiP2pManager.cancelConnect(mChannel, new ActionListener() {

                    @Override
                    public void onSuccess() {
//                        Toast.makeText(mActivity, "Aborting connection",
//                                Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(int reasonCode) {
//                        Toast.makeText(mActivity,
//                                "Connect abort request failed. Reason Code: " + reasonCode,
//                                Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
		
	}

	@Override
	public void onConnectionInfoAvailable(WifiP2pInfo info) {
		if(null != info)
		{
			mWifiP2pInfo = info;
			changeState(STATE_CONNECTED);
		}
		
	}
	
	public void receiveBroadcast(Context context, Intent intent)
	{
        String action = intent.getAction();
        Log.d(WiFiServiceDiscoveryActivity.TAG, action);
        if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {

            if (mWifiP2pManager == null) {
                return;
            }

            NetworkInfo networkInfo = (NetworkInfo) intent
                    .getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
            if (networkInfo.isConnected()) {

                // we are connected with the other device, request connection
                // info to find group owner IP
                Log.d(WiFiServiceDiscoveryActivity.TAG,
                        "Connected to p2p network. Requesting network details");
                mWifiP2pManager.requestConnectionInfo(mChannel,
                        (ConnectionInfoListener) this);
                changeState(STATE_CONNECTING);
            } else {
                // It's a disconnect
            	//changeState(STATE_DISCONNECT);
            	
            }
        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION
                .equals(action)) {

            WifiP2pDevice device = (WifiP2pDevice) intent
                    .getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE);
            Log.d(WiFiServiceDiscoveryActivity.TAG, "Device status -" + device.status);

        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {

            // request available peers from the wifi p2p manager. This is an
            // asynchronous call and the calling activity is notified with a
            // callback on PeerListListener.onPeersAvailable()
            if (mWifiP2pManager != null) {
            	mWifiP2pManager.requestPeers(mChannel, new PeerListListener() {
					
					@Override
					public void onPeersAvailable(WifiP2pDeviceList peers) {
						mPeers = peers;
						changeState(STATE_FOUNDPEER);
					}
				});
            }
        }else if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
//            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
//            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
//            	Toast.makeText(mActivity, "WIFI p2p enable", Toast.LENGTH_SHORT).show();
//            } else {
//                Toast.makeText(mActivity, "WIFI p2p not enable", Toast.LENGTH_SHORT).show();
//                reset();
//            }
        }		
	}
	
	public WifiP2pDeviceList getAvailablePeers()
	{
		return mPeers;
	}
	
	public WifiP2pInfo getWifiP2pInfo()
	{
		return mWifiP2pInfo;
	}
	
	private void changeState(int state)
	{
		mCurrState = state;
		mStateChangeListener.onStateChanged(mCurrState);
	}

	@Override
	public void connect() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void registerOnStateChangeListener(IOnStateChangeListener listener) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void unregisterOnStateChangeListener(IOnStateChangeListener listener) {

	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		
	}


}
