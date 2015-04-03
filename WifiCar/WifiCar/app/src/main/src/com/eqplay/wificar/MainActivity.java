package com.eqplay.wificar;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;


public class MainActivity extends Activity implements Button.OnClickListener, View.OnTouchListener {
    private Button upButton;
    private Button downButton;
    private Button leftButton;
    private Button rightButton;
    private Handler mOtherThreadHandler=null;
    private final static String TAG = "WifiCar";
    private final static int MSG_SEND_UDP = 1;
    char[] udp_msg = new char[1];
    private UdpThread udpThread = null;
    private WifiManager mWifiManager;
    private String mTargetName = "EQPlayDemo";
    private String mTargetPassword = "eqplaydemo";
    private boolean mIsConnected = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        connectAP();
        setContentView(R.layout.activity_main);
        upButton = (Button)findViewById(R.id.buttonUp);
        downButton = (Button)findViewById(R.id.buttonDown);
        leftButton = (Button)findViewById(R.id.buttonLeft);
        rightButton = (Button)findViewById(R.id.buttonRight);
        upButton.setOnClickListener(this);
        downButton.setOnClickListener(this);
        leftButton.setOnClickListener(this);
        rightButton.setOnClickListener(this);
        upButton.setOnTouchListener(this);
        downButton.setOnTouchListener(this);
        leftButton.setOnTouchListener(this);
        rightButton.setOnTouchListener(this);
    }

    @Override
    public void onResume()
    {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        registerReceiver(mBroadcastReceiver, (intentFilter));
    }

    @Override
    public void onPause()
    {
        super.onPause();
        unregisterReceiver(mBroadcastReceiver);
    }





    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {

//        if(v == leftButton){
//            udp_msg[0] = 'l';
//        }else if(v == rightButton){
//            udp_msg[0] = 'r';
//        }else if(v == upButton){
//            udp_msg[0] = 'u';
//        }else if(v == downButton) {
//            udp_msg[0] = 'd';
//        }else{
//            assert(false);
//        }
//        assert (mOtherThreadHandler != null);
//        //主线程发送消息给other thread
//        if(null!=mOtherThreadHandler){
//            String msg = new String(udp_msg);
//            Message mainThreadMsg = mOtherThreadHandler.obtainMessage(MSG_SEND_UDP, msg);
//            mOtherThreadHandler.sendMessage(mainThreadMsg);
//        }
    }

    /**
     * Called when a touch event is dispatched to a view. This allows listeners to
     * get a chance to respond before the target view.
     *
     * @param v     The view the touch event has been dispatched to.
     * @param event The MotionEvent object containing full information about
     *              the event.
     * @return True if the listener has consumed the event, false otherwise.
     */
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if(event.getAction() == MotionEvent.ACTION_DOWN)
        {
            if (v == leftButton) {
                udp_msg[0] = 'l';
            } else if (v == rightButton) {
                udp_msg[0] = 'r';
            } else if (v == upButton) {
                udp_msg[0] = 'u';
            } else if (v == downButton) {
                udp_msg[0] = 'd';
            } else {
                assert (false);
            }
        }
        if(event.getAction() == MotionEvent.ACTION_UP)
        {
            if (v == leftButton) {
                udp_msg[0] = ';';
            } else if (v == rightButton) {
                udp_msg[0] = 't';
            } else if (v == upButton) {
                udp_msg[0] = 'i';
            } else if (v == downButton) {
                udp_msg[0] = 'f';
            } else {
                assert (false);
            }
        }
        assert (mOtherThreadHandler != null);
        //主线程发送消息给other thread
        if(null!=mOtherThreadHandler){
            String msg = new String(udp_msg);
            Message mainThreadMsg = mOtherThreadHandler.obtainMessage(MSG_SEND_UDP, msg);
            mOtherThreadHandler.sendMessage(mainThreadMsg);
        }
        return false;
    }

    //ReceiveMessageThread has his own message queue by execute Looper.prepare();
    class UdpThread extends Thread{
        DatagramSocket client_socket;
        InetAddress server_ip_address;
        int peer_port = 33333;
        final String server_ip_str = "192.168.10.1";
        //final String server_ip_str = "10.28.55.72";
        public UdpThread(){
            try {
                client_socket = new DatagramSocket(peer_port);
                server_ip_address = InetAddress.getByName(server_ip_str);
            }catch (SocketException e){
                e.printStackTrace();
            }catch (UnknownHostException e){
                e.printStackTrace();
            }
        }


        @Override
        public void run() {
            Looper.prepare();
            mOtherThreadHandler = new Handler(){
                public void handleMessage(Message msg) {
                    String str = (String)msg.obj;
                    byte [] send_data = str.getBytes();
                    Log.e(TAG, "handleMessage dir:"+str);
                    DatagramPacket send_packet = new DatagramPacket(send_data,str.length(), server_ip_address, peer_port);
                    try {
                        client_socket.send(send_packet);
                    }catch (IOException e){
                        e.printStackTrace();
                    }
                }
            };
            Looper.loop();
            client_socket.close();
        }
    }


    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            if (intent.getAction().equals(
                    WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
//                if (!mIsConnecting) {
//                    selectedAP();
//                }
            } else {
                if (intent.getAction().equals(
                        WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
                    NetworkInfo info = intent
                            .getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                    if (info.isConnected()) {
                        //WifiInfo winfo = mWifiManager.getConnectionInfo();
                        //int ip = winfo.getIpAddress();
                        //InetAddress address = intToInetAddress(ip);
                        //createDataTransfer(address);

                        Toast.makeText(MainActivity.this, "Wifi Connected!", Toast.LENGTH_SHORT).show();
                        if(!mIsConnected)
                        {
                            udpThread = new UdpThread();
                            udpThread.start();
                        }

                        mIsConnected = true;

                    } else if (mIsConnected == true
                            && (0 == info.getDetailedState().compareTo(
                            NetworkInfo.DetailedState.DISCONNECTING) || 0 == info
                            .getDetailedState().compareTo(
                                    NetworkInfo.DetailedState.DISCONNECTED))) {
                        Log.v(TAG, "wifi disconnect!");
                        Toast.makeText(MainActivity.this, "Wifi Disconnect!", Toast.LENGTH_SHORT).show();
                        mOtherThreadHandler.getLooper().quit();
                        mIsConnected = false;
                        //disconnect();
                    }

                }
            }
        }
    };

    public void connectAP()
    {
        mWifiManager.setWifiEnabled(false);
        mWifiManager.setWifiEnabled(true);
        WifiConfiguration config = new WifiConfiguration();
        config.allowedAuthAlgorithms.clear();
        config.allowedGroupCiphers.clear();
        config.allowedKeyManagement.clear();
        config.allowedPairwiseCiphers.clear();
        config.allowedProtocols.clear();
        config.SSID = "\"" + mTargetName + "\"";

        config.preSharedKey = "\"" + mTargetPassword + "\"";
        config.hiddenSSID = true;
        config.allowedAuthAlgorithms
                .set(WifiConfiguration.AuthAlgorithm.OPEN);
        config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
        config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
        config.allowedPairwiseCiphers
                .set(WifiConfiguration.PairwiseCipher.TKIP);
        config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
        config.allowedPairwiseCiphers
                .set(WifiConfiguration.PairwiseCipher.CCMP);
        config.status = WifiConfiguration.Status.ENABLED;

        int wcgID = mWifiManager.addNetwork(config);
        boolean result = mWifiManager.enableNetwork(wcgID, true);
    }

}
