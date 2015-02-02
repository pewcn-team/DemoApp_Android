package com.example.app;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.example.android.wifidirect.discovery.R;
import com.example.connection.BatteryState;
import com.example.connection.ControlCommand;
import com.example.connection.DataTransfer;
import com.example.connection.ICommand;
import com.example.wifiap.WifiAPBase;
import org.webrtc.webrtcdemo.MediaEngineObserver;
import org.webrtc.webrtcdemo.WebRTCLib;

import java.io.IOException;

/**
 * Created by yihao on 15/2/2.
 */
public class ControlFragmentLight extends Fragment{

    public static int LIGHT_INDEX_TAI = 0;
    public static int LIGHT_INDEX_DIAO = 1;
    public static int LIGHT_INDEX_BI = 2;
    public static int LIGHT_INDEX_RIGUANG = 3;
    public static int LIGHT_STATE_OFF = 0;
    public static int LIGHT_STATE_ON = 1;

    Button mBtnTai;
    Button mBtnDiao;
    Button mBtnBi;
    Button mBtnRiguang;
    ImageView mIvTai;
    ImageView mIvDiao;
    ImageView mIvBi;
    ImageView mIvRiguang;
    TextView mTvBattery;
    boolean mStateTai = false;
    boolean mStateDiao = false;
    boolean mStateBi = false;
    boolean mStateRiguang = false;
    private String mRemoteIP;
    private Activity mActivity;
    boolean mIsServer = false;
    private WifiAPBase mWifiAP;

    public ControlFragmentLight(Activity activity, String remoteIP, WifiAPBase wifiap)
    {
        mActivity = activity;
        mWifiAP = wifiap;
        wifiap.registerDataReceiver(new DataTransfer.IDataReceiver() {

            @Override
            public void onReceiveData(byte[] data) {
                if(data[0] == ICommand.TYPE_CONTROL)
                {
                    final ControlCommand command = (ControlCommand) new ControlCommand().fromBytes(data);
                    if(null != command)
                    {
                        mActivity.runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                changeLigthState(command.mDirection, command.mState);

                            }
                        });

                    }
                }
                else if(data[0] == ICommand.TYPE_BATTERY)
                {
                    final BatteryState state = (BatteryState) new BatteryState().fromBytes(data);
                    if(null != state)
                    {
                        mActivity.runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                mTvBattery.setText("小车电量 " + state.mBatteryPercent + "%");

                            }
                        });
                    }
                }
                else if(data[0] == ICommand.TYPE_EXIT)
                {
                    mWifiAP.disconnect();
                    getActivity().finish();
                }


            }
        });
        mRemoteIP = remoteIP;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_control_ih, null);
        mBtnTai = (Button)view.findViewById(R.id.button_taideng);
        mBtnTai.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ControlCommand command = new ControlCommand();
                command.mDirection = (byte)LIGHT_INDEX_TAI;
                if(false == mStateTai)
                {
                    command.mState = (byte)LIGHT_STATE_ON;
                    mStateTai = true;
                }
                else
                {
                    command.mState = (byte)LIGHT_STATE_OFF;
                    mStateTai = false;
                }
                changeLigthState(command.mDirection, command.mState);
                sendCommand(command);

            }
        });

        mBtnDiao = (Button)view.findViewById(R.id.button_diaodeng);
        mBtnDiao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ControlCommand command = new ControlCommand();
                command.mDirection = (byte)LIGHT_INDEX_DIAO;
                if(false == mStateDiao)
                {
                    command.mState = (byte)LIGHT_STATE_ON;
                    mStateDiao = true;
                }
                else
                {
                    command.mState = (byte)LIGHT_STATE_OFF;
                    mStateDiao = false;
                }
                changeLigthState(command.mDirection, command.mState);
                sendCommand(command);
            }
        });

        mBtnBi = (Button)view.findViewById(R.id.button_bideng);
        mBtnBi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ControlCommand command = new ControlCommand();
                command.mDirection = (byte)LIGHT_INDEX_BI;
                if(false == mStateBi)
                {
                    command.mState = (byte)LIGHT_STATE_ON;
                    mStateBi = true;
                }
                else
                {
                    command.mState = (byte)LIGHT_STATE_OFF;
                    mStateBi = false;
                }
                changeLigthState(command.mDirection, command.mState);
                sendCommand(command);
            }
        });

        mBtnRiguang = (Button)view.findViewById(R.id.button_riguangdeng);
        mBtnRiguang.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ControlCommand command = new ControlCommand();
                command.mDirection = (byte)LIGHT_INDEX_RIGUANG;
                if(false == mStateRiguang)
                {
                    command.mState = (byte)LIGHT_STATE_ON;
                    mStateRiguang = true;
                }
                else
                {
                    command.mState = (byte)LIGHT_STATE_OFF;
                    mStateRiguang = false;
                }
                changeLigthState(command.mDirection, command.mState);
                sendCommand(command);
            }
        });

        mTvBattery = (TextView)view.findViewById(R.id.textView_battery);

        mIvTai = (ImageView)view.findViewById(R.id.imageView_tai);

        mIvDiao = (ImageView)view.findViewById(R.id.imageView_diao);

        mIvBi = (ImageView)view.findViewById(R.id.imageView_bi);

        mIvRiguang = (ImageView)view.findViewById(R.id.imageView_riguang);

        return view;
    }

    public void changeLigthState(int btnIndex, int state)
    {
        if(state == LIGHT_STATE_ON)
        {
            if(btnIndex == LIGHT_INDEX_TAI)
            {
                mIvTai.setImageResource(R.drawable.on);
                try {
                    Runtime.getRuntime().exec("hwacc w 0xd4019154 0x00000002");
                    Runtime.getRuntime().exec("hwacc w 0xd4019118 0x00000002");
                    Log.v("Control", "Press Forward Button");
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                //mMotorPWM.changeLevel(1);
                //mController.setMotorLevel(1);
            }
            if(btnIndex == LIGHT_INDEX_DIAO)
            {
                mIvDiao.setImageResource(R.drawable.on);
                try {
                    Runtime.getRuntime().exec("hwacc w 0xd4019054 0x00002000");
                    Runtime.getRuntime().exec("hwacc w 0xd4019018 0x00002000");
                    Log.v("Control", "Press Back Button");
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
//				mMotorPWM.changeLevel(-1);
                //mController.setMotorLevel(-1);
            }
            if(btnIndex == LIGHT_INDEX_BI)
            {
                mIvBi.setImageResource(R.drawable.on);
                try {
                    Runtime.getRuntime().exec("hwacc w 0xd4019054 0x00020000");
                    Runtime.getRuntime().exec("hwacc w 0xd4019018 0x00020000");
                    Log.v("Control", "Press Left Button");
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                //mController.turnLeft();

            }
            if(btnIndex == LIGHT_INDEX_RIGUANG)
            {
                mIvRiguang.setImageResource(R.drawable.on);
                try {
                    Runtime.getRuntime().exec("hwacc w 0xd4019054 0x00010000");
                    Runtime.getRuntime().exec("hwacc w 0xd4019018 0x00010000");
                    Log.v("Control", "Press Right Button");
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                //mController.turnRight();
            }
        }
        else if(state == LIGHT_STATE_OFF)
        {
            if(btnIndex == LIGHT_INDEX_TAI)
            {
                mIvTai.setImageResource(R.drawable.off);
                try {
                    Runtime.getRuntime().exec("hwacc w 0xd4019154 0x00000002");
                    Runtime.getRuntime().exec("hwacc w 0xd4019124 0x00000002");
                    Log.v("Control", "Release Forward Button");
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
//				mMotorPWM.changeLevel(0);
                //mController.setMotorLevel(0);
            }
            if(btnIndex == LIGHT_INDEX_DIAO)
            {
                mIvDiao.setImageResource(R.drawable.off);
                try {
                    Runtime.getRuntime().exec("hwacc w 0xd4019054 0x00002000");
                    Runtime.getRuntime().exec("hwacc w 0xd4019024 0x00002000");
                    Log.v("Control", "Release Back Button");
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                //mController.setMotorLevel(0);
            }
            if(btnIndex == LIGHT_INDEX_BI)
            {
                mIvBi.setImageResource(R.drawable.off);
                try {
                    Runtime.getRuntime().exec("hwacc w 0xd4019054 0x00020000");
                    Runtime.getRuntime().exec("hwacc w 0xd4019024 0x00020000");
                    Log.v("Control", "Release Left Button");
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                //mController.resetDirection();
            }
            if(btnIndex == LIGHT_INDEX_RIGUANG)
            {
                mIvRiguang.setImageResource(R.drawable.off);
                try {
                    Runtime.getRuntime().exec("hwacc w 0xd4019054 0x00010000");
                    Runtime.getRuntime().exec("hwacc w 0xd4019024 0x00010000");
                    Log.v("Control", "Release Right Button");
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }



    private void sendCommand(ControlCommand command)
    {
        mWifiAP.sendData(command.toBytes());
    }


}
