package com.example.app;

import android.util.Log;

import com.example.connection.BatteryState;
import com.example.connection.ControlCommand;
import com.example.connection.DataTransfer;
import com.example.connection.ExitCommand;
import com.example.connection.ICommand;
import com.example.control.CarController;

import org.webrtc.webrtcdemo.MediaEngineObserver;
import org.webrtc.webrtcdemo.WebRTCLib;

import com.example.android.wifidirect.discovery.R;
import com.example.connection.DataTransfer.IDataReceiver;
import com.example.wifiap.WifiAPBase;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.IOException;

public class ControlFragmentCar extends Fragment {
	Button mBtnUp;
	Button mBtnDown;
	Button mBtnLeft;
	Button mBtnRight;
	LinearLayout mRemoteLayout;
	LinearLayout mLocalLayout;
	TextView mTvBattery;
	public static int BUTTON_INDEX_UP = 0;
	public static int BUTTON_INDEX_DOWN = 1;
	public static int BUTTON_INDEX_LEFT = 2;
	public static int BUTTON_INDEX_RIGHT = 3;
	public static int BUTTON_STATE_DOWN = 0;
	public static int BUTTON_STATE_UP = 1;
	private String mRemoteIP;
	private MediaEngineObserver mObserver;
	private WebRTCLib mWebrtc;
	private Activity mActivity;
    boolean mIsServer = false;
    private ControlCommand mCurrControlCommandVert = null;
	private ControlCommand mCurrControlCommandHori = null;
	private WifiAPBase mWifiAP;
	public ControlFragmentCar(Activity activity, String remoteIP, MediaEngineObserver observer, WifiAPBase wifiap, boolean isServer)
	{
		mActivity = activity;
		mWifiAP = wifiap;
		wifiap.registerDataReceiver(new IDataReceiver() {
			
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
								changeButtonState(command.mDirection, command.mState);
								
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
		mObserver = observer;
		mIsServer = isServer;
		if(mIsServer)
		{
			//mController = new CarController();
		}


		
	}
	
	private View.OnTouchListener mOnTouchListenerVertical = new View.OnTouchListener() {
		
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			ControlCommand command = new ControlCommand();
			if(event.getAction() == MotionEvent.ACTION_DOWN)
			{
				command.mState = (byte) BUTTON_STATE_DOWN;
				
//				ExitCommand exit = new ExitCommand();
//				mWifiAP.sendData(exit.toBytes());
			}
			else if(event.getAction() == MotionEvent.ACTION_UP)
			{
				command.mState = (byte) BUTTON_STATE_UP;
			}
			
			if(v.getId() == R.id.button_up)
			{
				command.mDirection = (byte) BUTTON_INDEX_DOWN;
			}
			else if(v.getId() == R.id.button_down)
			{
				command.mDirection = (byte) BUTTON_INDEX_UP;
			}
			if(mCurrControlCommandVert == null)
			{
				sendCommand(command);
				mCurrControlCommandVert = command;
				
			}
			else
			{
				if(mCurrControlCommandVert.mState!=command.mState||mCurrControlCommandVert.mDirection!=command.mDirection)
				{
					sendCommand(command);
					mCurrControlCommandVert = command;
				}
			}
			
			return false;
		}
	};

	private View.OnTouchListener mOnTouchListenerHorizontal = new View.OnTouchListener() {

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			ControlCommand command = new ControlCommand();
			if(event.getAction() == MotionEvent.ACTION_DOWN)
			{
				command.mState = (byte) BUTTON_STATE_DOWN;
			}
			else if(event.getAction() == MotionEvent.ACTION_UP)
			{
				command.mState = (byte) BUTTON_STATE_UP;
			}
			if(v.getId() == R.id.button_left)
			{
				command.mDirection = (byte) BUTTON_INDEX_RIGHT;
			}
			else if(v.getId() == R.id.button_right)
			{
				command.mDirection = (byte) BUTTON_INDEX_LEFT;
			}
			if(mCurrControlCommandHori == null)
			{
				sendCommand(command);
				mCurrControlCommandHori = command;

			}
			else
			{
				if(mCurrControlCommandHori.mState!=command.mState||mCurrControlCommandHori.mDirection!=command.mDirection)
				{
					sendCommand(command);
					mCurrControlCommandHori = command;
				}
			}

			return false;
		}
	};
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.fragment_control_car, null);
		mBtnUp = (Button)view.findViewById(R.id.button_up);
		mBtnUp.setOnTouchListener(mOnTouchListenerVertical);
		mBtnDown = (Button)view.findViewById(R.id.button_down);
		mBtnDown.setOnTouchListener(mOnTouchListenerVertical);
		mBtnLeft = (Button)view.findViewById(R.id.button_left);
		mBtnLeft.setOnTouchListener(mOnTouchListenerHorizontal);
		mBtnRight = (Button)view.findViewById(R.id.button_right);
		mBtnRight.setOnTouchListener(mOnTouchListenerHorizontal);
		mRemoteLayout = (LinearLayout)view.findViewById(R.id.layout_remote);
		mLocalLayout = (LinearLayout)view.findViewById(R.id.layout_local);
		mTvBattery = (TextView)view.findViewById(R.id.textView_battery);
		return view;
	}
	
	
	public void changeButtonState(int btnIndex, int state)
	{
		if(state == BUTTON_STATE_DOWN)
		{
			if(btnIndex == BUTTON_INDEX_UP)
			{
				mBtnUp.setSelected(true);
				mBtnUp.setBackgroundColor(0xFFFF0000);
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
			if(btnIndex == BUTTON_INDEX_DOWN)
			{
				mBtnDown.setSelected(true);
				mBtnDown.setBackgroundColor(0xFFFF0000);
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
			if(btnIndex == BUTTON_INDEX_LEFT)
			{
				mBtnLeft.setSelected(true);
				mBtnLeft.setBackgroundColor(0xFFFF0000);
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
			if(btnIndex == BUTTON_INDEX_RIGHT)
			{
				mBtnRight.setSelected(true);
				mBtnRight.setBackgroundColor(0xFFFF0000);
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
		else if(state == BUTTON_STATE_UP)
		{
			if(btnIndex == BUTTON_INDEX_UP)
			{
				mBtnUp.setSelected(false);
				mBtnUp.setBackgroundColor(0xFFFFFFFF);
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
			if(btnIndex == BUTTON_INDEX_DOWN)
			{
				mBtnDown.setSelected(false);
				mBtnDown.setBackgroundColor(0xFFFFFFFF);
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
			if(btnIndex == BUTTON_INDEX_LEFT)
			{
				mBtnLeft.setSelected(false);
				mBtnLeft.setBackgroundColor(0xFFFFFFFF);
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
			if(btnIndex == BUTTON_INDEX_RIGHT)
			{
				mBtnRight.setSelected(false);
				mBtnRight.setBackgroundColor(0xFFFFFFFF);
				try {
					Runtime.getRuntime().exec("hwacc w 0xd4019054 0x00010000");
					Runtime.getRuntime().exec("hwacc w 0xd4019024 0x00010000");
					Log.v("Control", "Release Right Button");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				//mController.resetDirection();
			}
		}
	}
	
	private void sendCommand(ControlCommand command)
	{
		mWifiAP.sendData(command.toBytes());
	}
	
    @Override
    public void onResume (){
        super.onResume();
        mWebrtc = new WebRTCLib();
        if(mIsServer)
        {
        	mWebrtc.open(this.getActivity(),mRemoteIP,true,true);
        }
        else
        {
        	mWebrtc.open(this.getActivity(),mRemoteIP,true,true);
        }
        
        
        mWebrtc.startCall(mRemoteLayout, mLocalLayout); 
        if(this.mObserver != null){
        	mWebrtc.setEngineObserver(this.mObserver);
        }
    }
    
    @Override
    public void onPause(){
        super.onPause();

        if(mWebrtc != null){
        	mWebrtc.setEngineObserver(null);
        	mWebrtc.stopCall();
        	mWebrtc.close();
        	mWebrtc = null;
        }

    }
	
    @Override
    public void onStop() {
		super.onStop();

		if (mWebrtc != null) {
			mWebrtc.setEngineObserver(null);
			mWebrtc.stopCall();
			mWebrtc.close();
			mWebrtc = null;
			System.gc();
		}

	}

}
