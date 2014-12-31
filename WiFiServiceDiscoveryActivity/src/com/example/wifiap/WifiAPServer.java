package com.example.wifiap;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.DetailedState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

public class WifiAPServer {
	String TAG = "WifiAPServer";
	WifiManager mWifiManager;
	String mSSID = "tank_test";
	String mPasswd = "12345678";
	Context mContext;
    public static final int WIFI_CONNECTED = 0x01;  
    public static final int WIFI_CONNECT_FAILED = 0x02;  
    public static final int WIFI_CONNECTING = 0x03;  
    OnConnectListener mListener;
	public interface OnConnectListener
	{
		public void onConnect();
		
	}
	
	public WifiAPServer (Context context, OnConnectListener listener)
	{
		mContext = context;
		mWifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);  
		mListener = listener;
		createAP();
	}
	

	
	
	/**
	 * 建立一个wifi热点
	 */
	public void createAP()
	{
		if (mWifiManager.isWifiEnabled()) {
			mWifiManager.setWifiEnabled(false);
		}
		Method method1 = null;
		try {
			method1 = mWifiManager.getClass().getMethod("setWifiApEnabled",
					WifiConfiguration.class, boolean.class);
			WifiConfiguration netConfig = new WifiConfiguration();

			netConfig.SSID = mSSID;
			netConfig.preSharedKey = mPasswd;

			netConfig.allowedAuthAlgorithms
					.set(WifiConfiguration.AuthAlgorithm.OPEN);
			//netConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
//			netConfig.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
			netConfig.allowedKeyManagement
					.set(WifiConfiguration.KeyMgmt.WPA_PSK);
//			netConfig.allowedPairwiseCiphers
//					.set(WifiConfiguration.PairwiseCipher.CCMP);
//			netConfig.allowedPairwiseCiphers
//					.set(WifiConfiguration.PairwiseCipher.TKIP);
//			netConfig.allowedGroupCiphers
//					.set(WifiConfiguration.GroupCipher.CCMP);
//			netConfig.allowedGroupCiphers
//					.set(WifiConfiguration.GroupCipher.TKIP);

			method1.invoke(mWifiManager, netConfig, true);

		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		mListener.onConnect();
	}
	
    /** 
     * 判断wifi是否连接成功,不是network 
     *  
     * @param context 
     * @return 
     */  
    public int isWifiContected(Context context) {  
        ConnectivityManager connectivityManager = (ConnectivityManager) context  
                .getSystemService(Context.CONNECTIVITY_SERVICE);  
        NetworkInfo wifiNetworkInfo = connectivityManager  
                .getNetworkInfo(ConnectivityManager.TYPE_WIFI);  
          
        Log.v(TAG, "isConnectedOrConnecting = " + wifiNetworkInfo.isConnectedOrConnecting());  
        Log.d(TAG, "wifiNetworkInfo.getDetailedState() = " + wifiNetworkInfo.getDetailedState());  
        if (wifiNetworkInfo.getDetailedState() == DetailedState.OBTAINING_IPADDR  
                || wifiNetworkInfo.getDetailedState() == DetailedState.CONNECTING) {  
            return WIFI_CONNECTING;  
        } else if (wifiNetworkInfo.getDetailedState() == DetailedState.CONNECTED) {  
            return WIFI_CONNECTED;  
        } else {  
            Log.d(TAG, "getDetailedState() == " + wifiNetworkInfo.getDetailedState());  
            return WIFI_CONNECT_FAILED;  
        }  
    } 
    
	 private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {  
		  
	        @Override  
	        public void onReceive(Context context, Intent intent) {  
	            // TODO Auto-generated method stub  
	            if (intent.getAction().equals(WifiManager.RSSI_CHANGED_ACTION)) {  
	                Log.d(TAG, "RSSI changed");  
	                  
	                //有可能是正在获取，或者已经获取了  
	                Log.d(TAG, " intent is " + WifiManager.RSSI_CHANGED_ACTION);  
	                  
	                if (isWifiContected(mContext) == WIFI_CONNECTED) {  
	                	 Log.d(TAG, " WIFI_CONNECTED ");  
	                	 mListener.onConnect();
	                    //stopTimer();  
	                } else if (isWifiContected(mContext) == WIFI_CONNECT_FAILED) {  
	                	Log.d(TAG, " WIFI_CONNECTED Failed");  
	                    //stopTimer();  
	                    //closeWifi();  
	                } else if (isWifiContected(mContext) == WIFI_CONNECTING) {  
	                      
	                }  
	            } 
	            else if(intent.getAction().equals(WifiManager.NETWORK_STATE_CHANGED_ACTION))
	            {
	            	NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
	            	if(info.isConnected())
	            	{
//	                	WifiInfo winfo = mWifiManager.getConnectionInfo();
//	                	int ip = winfo.getIpAddress();
//	                	InetAddress address = intToInetAddress(ip);
//	                	Log.d(TAG, address.getHostName());
//	                	mListener.onConnect(address);
	            		
	                	
	            	}
	            	
	            }
	        }  
	    };  
	    
	  public void register()
	  {
		  IntentFilter filter = new IntentFilter(WifiManager.RSSI_CHANGED_ACTION);
		  mContext.registerReceiver(mBroadcastReceiver, new IntentFilter(filter));
	  }
	  
	  public void unRegister()
	  {
		  mContext.unregisterReceiver(mBroadcastReceiver);
	  }

}
