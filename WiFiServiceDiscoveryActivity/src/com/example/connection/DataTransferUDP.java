package com.example.connection;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import com.example.android.wifidirect.discovery.WiFiServiceDiscoveryActivity;

import java.io.IOException;
import java.net.*;

/**
 * Created by yihao on 15/4/7.
 */
public class DataTransferUDP implements IDataTransfer{

    /**
     * 启动transfer,会开启一个数据收发的线程
     */
    @Override
    public void startTransfer() {

    }

    /**
     * 发送数据
     *
     * @param data
     */
    @Override
    public void sendData(byte[] data) {

    }

    /**
     * 停止transfer,停止数据收发的线程
     */
    @Override
    public void stopTransfer() {

    }

    /**
     * 注册侦听器
     *
     * @param listener
     */
    @Override
    public void registerTransferListener(ITransferListener listener) {

    }

    /**
     * 注销侦听器
     *
     * @param listener
     */
    @Override
    public void unregisterTransferListener(ITransferListener listener) {

    }

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
                    Log.e(WiFiServiceDiscoveryActivity.TAG, "handleMessage dir:" + str);
                    DatagramPacket send_packet = new DatagramPacket(send_data,str.length(), server_ip_address, peer_port);
                    try {
                        if(null != client_socket)
                        {
                            client_socket.send(send_packet);
                        }

                    }catch (IOException e){
                        e.printStackTrace();
                    }
                }
            };
            Looper.loop();
            client_socket.close();
        }
    }
    private Handler mOtherThreadHandler=null;
    private UdpThread mUdpThread = null;
    private final static int MSG_SEND_UDP = 1;

    /**
     *
     */
    public void start()
    {
        mUdpThread = new UdpThread();
        mUdpThread.start();
    }

    public void stop()
    {
        mOtherThreadHandler.getLooper().quit();
    }

    public void sendMessage(byte buffer[])
    {
        ControlCommand command = new ControlCommand();
        command.fromBytes(buffer);
        if(null!=mOtherThreadHandler){
            String msg = String.valueOf(command.getCharMessage());
            Message mainThreadMsg = mOtherThreadHandler.obtainMessage(MSG_SEND_UDP, msg);
            mOtherThreadHandler.sendMessage(mainThreadMsg);
        }
    }


}
