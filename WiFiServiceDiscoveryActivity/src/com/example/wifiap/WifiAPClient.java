package com.example.wifiap;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import com.example.android.wifidirect.discovery.WiFiServiceDiscoveryActivity;
import com.example.android.wifidirect.discovery.WiFiChatFragment.MessageTarget;
import com.example.connection.DataTransfer;
import com.example.connection.DataTransfer.IConnectionListener;
import com.example.connection.IConnection;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.DetailedState;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

public class WifiAPClient implements IConnection{
	
    public static final int TYPE_NO_PASSWD = 0x11;  
    public static final int TYPE_WEP = 0x12;  
    public static final int TYPE_WPA = 0x13; 
    
    public static final int WIFI_CONNECTED = 0x01;  
    public static final int WIFI_CONNECT_FAILED = 0x02;  
    public static final int WIFI_CONNECTING = 0x03;  
 
    public String TAG = "WifiAPClient";
    private WifiManager mWifiManager;
    private Context mContext;
    private boolean  mIsConnecting =false;
    private boolean mIsConnected = false;
	private String mTargetName = "tank_test";
	private String mTargetPassword = "12345678";
	private DataTransfer mDataTransfer;
	private boolean mIsClientCreated = false;
	private ArrayList<IConnection.IOnStateChangeListener> mListeners = new ArrayList<IConnection.IOnStateChangeListener>();
	public WifiAPClient(Context context)
	{

		mContext = context;
		mWifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);  
	}

    /**地址转换程序
     * @param hostAddress
     * @return
     */
    public static InetAddress intToInetAddress(int hostAddress) {
        byte[] addressBytes = { (byte)(0xff & hostAddress),
                                (byte)(0xff & (hostAddress >> 8)),
                                (byte)(0xff & (hostAddress >> 16)),
                                (byte)(0xff & (hostAddress >> 24)) };

        try {
           return InetAddress.getByAddress(addressBytes);
        } catch (UnknownHostException e) {
           throw new AssertionError();
        }
    }
    

	    

//	/**
//	 * 判断wifi是否连接成功,不是network
//	 * 
//	 * @param context
//	 * @return
//	 */
//	private int isWifiContected(Context context) {
//		ConnectivityManager connectivityManager = (ConnectivityManager) context
//				.getSystemService(Context.CONNECTIVITY_SERVICE);
//		NetworkInfo wifiNetworkInfo = connectivityManager
//				.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
//
//		Log.v(TAG,
//				"isConnectedOrConnecting = "
//						+ wifiNetworkInfo.isConnectedOrConnecting());
//		Log.d(TAG,
//				"wifiNetworkInfo.getDetailedState() = "
//						+ wifiNetworkInfo.getDetailedState());
//		if (wifiNetworkInfo.getDetailedState() == DetailedState.OBTAINING_IPADDR
//				|| wifiNetworkInfo.getDetailedState() == DetailedState.CONNECTING) {
//			return WIFI_CONNECTING;
//		} else if (wifiNetworkInfo.getDetailedState() == DetailedState.CONNECTED) {
//			return WIFI_CONNECTED;
//		} else {
//			Log.d(TAG,
//					"getDetailedState() == "
//							+ wifiNetworkInfo.getDetailedState());
//			return WIFI_CONNECT_FAILED;
//		}
//	}
  
    public void onPause()
    {
    	mContext.unregisterReceiver(mBroadcastReceiver);
    }
    
    public void onResume()
    {
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
		intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
		intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
		mContext.registerReceiver(mBroadcastReceiver, (intentFilter));
    }
    
	@Override
	public void initial() {
		
	}
	@Override
	public void seekPeer() {
		scanAP();
	}
	@Override
	public void connect() {
		int type = TYPE_WPA;
		WifiConfiguration config = new WifiConfiguration();  
        config.allowedAuthAlgorithms.clear();  
        config.allowedGroupCiphers.clear();  
        config.allowedKeyManagement.clear();  
        config.allowedPairwiseCiphers.clear();  
        config.allowedProtocols.clear();  
        config.SSID = "\"" + mTargetName + "\"";  
  
        WifiConfiguration tempConfig = this.IsExsits(mTargetName);  
        if (tempConfig != null) {  
            mWifiManager.removeNetwork(tempConfig.networkId);  
        }  
        // 分为三种情况：1没有密码2用wep加密3用wpa加密  
        if (type == TYPE_NO_PASSWD) {// WIFICIPHER_NOPASS  
            config.wepKeys[0] = "";  
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);  
            config.wepTxKeyIndex = 0;  
              
        } else if (type == TYPE_WEP) {  //  WIFICIPHER_WEP   
            config.hiddenSSID = true;  
            config.wepKeys[0] = "\"" + mTargetPassword + "\"";  
            config.allowedAuthAlgorithms  
                    .set(WifiConfiguration.AuthAlgorithm.SHARED);  
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);  
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);  
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);  
            config.allowedGroupCiphers  
                    .set(WifiConfiguration.GroupCipher.WEP104);  
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);  
            config.wepTxKeyIndex = 0;  
        } else if (type == TYPE_WPA) {   // WIFICIPHER_WPA  
            config.preSharedKey = "\"" + mTargetPassword + "\"";  
            config.hiddenSSID = true;  
            config.allowedAuthAlgorithms  
                    .set(WifiConfiguration.AuthAlgorithm.OPEN);  
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);  
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);  
            config.allowedPairwiseCiphers  
                    .set(WifiConfiguration.PairwiseCipher.TKIP);  
            // config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);  
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);  
            config.allowedPairwiseCiphers  
                    .set(WifiConfiguration.PairwiseCipher.CCMP);  
            config.status = WifiConfiguration.Status.ENABLED;  
        }  
        
        int wcgID = mWifiManager.addNetwork(config);  
        boolean result = mWifiManager.enableNetwork(wcgID, true);  
        if(result)
        {
        	mIsConnecting = true;
        }
	}
	@Override
	public void timeout() {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void disconnect() {
		
	}
	@Override
	public void reset() {
		
	}
	@Override
	public void registerOnStateChangeListener(IOnStateChangeListener listener) {
		if(null == listener)
		{
			throw new IllegalArgumentException("listener is null!");
		}
		mListeners.add(listener);
		
	}
	
	@Override
	public void unregisterOnStateChangeListener(IOnStateChangeListener listener) {
		mListeners.remove(listener);
	}  
	
	private void changeState(IConnection.ConnectionState state)
	{
		for(IOnStateChangeListener listener : mListeners)
		{
			listener.onStateChange(state);
		}
	}
	
	private void scanAP()
	{
        mWifiManager.startScan();  
	}
	
	private void findAP()
	{
        List<ScanResult> results = mWifiManager.getScanResults();  
        for(ScanResult result: results)
        {
        	if(result.SSID.equals("tank_test"))
        	{
        		if(WifiManager.calculateSignalLevel(result.level, 100) > 60)
        		{
        			connect();
        			break;
        		}
        	}
        }		
	}
	
    private WifiConfiguration IsExsits(String SSID) {  
        List<WifiConfiguration> existingConfigs = mWifiManager.getConfiguredNetworks();  
        for (WifiConfiguration existingConfig : existingConfigs) {  
            if (existingConfig.SSID.equals("\"" + SSID + "\"") /*&& existingConfig.preSharedKey.equals("\"" + password + "\"")*/) {  
                return existingConfig;  
            }  
        }  
        return null;  
    }  
    
    private void createDataTransfer(InetAddress address)
    {
		final InetAddress fAddress = address;
		if (false == mIsClientCreated) {
			mIsClientCreated = true;
			Thread t = new Thread(new Runnable() {
				@Override
				public void run() {
					if (null == mDataTransfer) {
						mDataTransfer = DataTransfer.createClientTransfer(null,fAddress, new IConnectionListener() {
							
							@Override
							public void onDisconnect() {
								changeState(ConnectionState.DISCONNECT);
							}

							@Override
							public void onConnect() {
								changeState(ConnectionState.CONNECTED);
								
							}
						});
					}
				}
			});
			t.start();
		}
    }
    
	private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			if (intent.getAction().equals(
					WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
				if (!mIsConnecting) {
					findAP();
				}
			} else if (intent.getAction().equals(
					WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
				NetworkInfo info = intent
						.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
				if (info.isConnected()) {
					WifiInfo winfo = mWifiManager.getConnectionInfo();
					int ip = winfo.getIpAddress();
					InetAddress address = intToInetAddress(ip);
					createDataTransfer(address);
					mIsConnected = true;

				} else if (mIsConnected == true
						&& (0 == info.getDetailedState().compareTo(
								NetworkInfo.DetailedState.DISCONNECTING) || 0 == info
								.getDetailedState().compareTo(
										NetworkInfo.DetailedState.DISCONNECTED))) {
					Log.v(TAG, "wifi disconnect!");
					mIsConnected = false;
					changeState(ConnectionState.DISCONNECT);
				}

			}
		}
	};

	
	
}
