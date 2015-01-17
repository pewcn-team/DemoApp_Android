package com.example.wifiap;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.util.ArrayList;

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
import com.example.android.wifidirect.discovery.WiFiChatFragment;
import com.example.connection.DataTransfer;
import com.example.connection.IConnection;

public class WifiAPServer implements IConnection {
    String TAG = "WifiAPServer";
    WifiManager mWifiManager;
    String mSSID = "tank_test";
    String mPasswd = "12345678";
    Context mContext = null;
    private DataTransfer mDataTransfer = null;
    public static final int WIFI_CONNECTED = 0x01;
    public static final int WIFI_CONNECT_FAILED = 0x02;
    public static final int WIFI_CONNECTING = 0x03;
    private ArrayList<IOnStateChangeListener> mListeners = new ArrayList<IConnection.IOnStateChangeListener>();

    @Override
    public void initial() {
        /**
         * 建立一个wifi热点
         */
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
            netConfig.allowedKeyManagement
                    .set(WifiConfiguration.KeyMgmt.WPA_PSK);
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

        createDataTransfer();
    }

    @Override
    public void seekPeer() {

    }

    @Override
    public void connect() {

    }

    @Override
    public void timeout() {

    }

    @Override
    public void disconnect() {
        mDataTransfer.destroy();
        changeState(ConnectionState.DISCONNECT);
    }

    @Override
    public void reset() {

    }

    public DataTransfer getDataTransfer()
    {
        return mDataTransfer;
    }


    public WifiAPServer(Context context) {
        mContext = context;
        mWifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
    }


    public void onPause() {
        mContext.unregisterReceiver(mBroadcastReceiver);
    }

    public void onResume() {
        IntentFilter filter = new IntentFilter(WifiManager.RSSI_CHANGED_ACTION);
        mContext.registerReceiver(mBroadcastReceiver, new IntentFilter(filter));
    }


    @Override
    public void registerOnStateChangeListener(IOnStateChangeListener listener) {
        if (null == listener) {
            throw new IllegalArgumentException("listener is null!");
        }
        mListeners.add(listener);

    }

    @Override
    public void unregisterOnStateChangeListener(IOnStateChangeListener listener) {
        if (null == listener) {
            throw new IllegalArgumentException("listener is null!");
        }
        mListeners.remove(listener);
    }

    /**
     * 判断wifi是否连接成功,不是network
     *
     * @param context
     * @return
     */
    private int isWifiContected(Context context) {
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

                    //stopTimer();
                } else if (isWifiContected(mContext) == WIFI_CONNECT_FAILED) {
                    Log.d(TAG, " WIFI_CONNECTED Failed");
                    //stopTimer();
                    //closeWifi();
                } else if (isWifiContected(mContext) == WIFI_CONNECTING) {

                }
            } else if (intent.getAction().equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
                NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                if (info.isConnected()) {
//	                	WifiInfo winfo = mWifiManager.getConnectionInfo();
//	                	int ip = winfo.getIpAddress();
//	                	InetAddress address = intToInetAddress(ip);
//	                	Log.d(TAG, address.getHostName());
//	                	mListener.onConnect(address);


                } else if (0 == info.getDetailedState().compareTo(NetworkInfo.DetailedState.DISCONNECTING) || 0 == info.getDetailedState().compareTo(NetworkInfo.DetailedState.DISCONNECTED)) {
                    Log.v(TAG, "wifi disconnect!");
                    disconnect();
                }

            }
        }
    };

    private void changeState(IConnection.ConnectionState state)
    {
        for(IOnStateChangeListener listener : mListeners)
        {
            listener.onStateChange(state);
        }
    }

    private void createDataTransfer() {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                if (null == mDataTransfer) {
                    mDataTransfer = DataTransfer
                            .createServerTransfer(
                                    null,new DataTransfer.IConnectionListener() {
                                        @Override
                                        public void onConnect() {
                                            changeState(ConnectionState.CONNECTED);
                                        }

                                        @Override
                                        public void onDisconnect() {
                                            changeState(ConnectionState.DISCONNECT);
                                        }
                                    });
                }
            }
        });
        t.start();

    }


}
