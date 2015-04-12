package com.example.app;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;

import android.view.animation.RotateAnimation;
import android.widget.*;
import com.example.connection.BatteryState;
import com.example.connection.ControlCommand;
import com.example.connection.ICommand;

import com.example.widget.MyRelativeLayout;
import com.example.widget.WheelControl;
import org.jscience.mathematics.number.FloatingPoint;
import org.jscience.mathematics.vector.Float64Vector;
import org.jscience.mathematics.vector.Vector;
import org.webrtc.webrtcdemo.MediaEngineObserver;
import org.webrtc.webrtcdemo.WebRTCLib;

import com.example.android.wifidirect.discovery.R;
import com.example.android.wifidirect.discovery.WiFiServiceDiscoveryActivity;
import com.example.wifiap.WifiAPBase;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

public class ControlFragmentCar extends Fragment {
	Button mBtnUp;
	Button mBtnDown;
	Button mBtnLeft;
	Button mBtnRight;
	LinearLayout mRemoteLayout;
	LinearLayout mLocalLayout;
	TextView mTvBattery;
	EditText mEtxInput;
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
	private boolean mIsWebRtcEnabled = false;
	private WheelControl mWheelControl;
	public ControlFragmentCar(Activity activity, String remoteIP, MediaEngineObserver observer, WifiAPBase wifiap, boolean isServer, boolean isWebRtcEnabled)
	{
		mActivity = activity;
		mWifiAP = wifiap;
//		wifiap.registerDataReceiver(new IDataReceiver() {
//
//			@Override
//			public void onReceiveData(byte[] data) {
//				if(data[0] == ICommand.TYPE_CONTROL)
//				{
//					final ControlCommand command = (ControlCommand) new ControlCommand().fromBytes(data);
//					if(null != command)
//					{
//						mActivity.runOnUiThread(new Runnable() {
//
//							@Override
//							public void run() {
//								changeButtonState(command.mDirection, command.mState);
//
//							}
//						});
//
//					}
//				}
//				else if(data[0] == ICommand.TYPE_BATTERY)
//				{
//					final BatteryState state = (BatteryState) new BatteryState().fromBytes(data);
//					if(null != state)
//					{
//						mActivity.runOnUiThread(new Runnable() {
//
//							@Override
//							public void run() {
//								mTvBattery.setText("小车电量 " + state.mBatteryPercent + "%");
//
//							}
//						});
//					}
//				}
//				else if(data[0] == ICommand.TYPE_EXIT)
//				{
//					mWifiAP.disconnect();
//					getActivity().finish();
//				}
//
//
//			}
//		});
		mRemoteIP = remoteIP;
		mObserver = observer;
		mIsServer = isServer;
		mIsWebRtcEnabled = isWebRtcEnabled;

	}
	
	private View.OnTouchListener mOnTouchListenerVertical = new View.OnTouchListener() {
		
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
			
			if(v.getId() == R.id.button_up)
			{
				command.mDirection = (byte) BUTTON_INDEX_UP;
			}
			else if(v.getId() == R.id.button_down)
			{
				command.mDirection = (byte) BUTTON_INDEX_DOWN;
			}

			if(command.mDirection == (byte) BUTTON_INDEX_UP)
			{
				if(event.getAction() == MotionEvent.ACTION_DOWN)
				{
					playStartSound();
				}
				else if(event.getAction() == MotionEvent.ACTION_UP)
				{
					stopStartSound();
				}

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
				command.mDirection = (byte) BUTTON_INDEX_LEFT;

			}
			else if(v.getId() == R.id.button_right)
			{
				command.mDirection = (byte) BUTTON_INDEX_RIGHT;

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
		MyRelativeLayout rl = (MyRelativeLayout)view.findViewById(R.id.rl_wheel);
		ImageView IvWheel = (ImageView)view.findViewById(R.id.imageView_wheel);
		mWheelControl = new WheelControl(getActivity(), IvWheel, rl, new WheelControl.RotateCallback() {
			@Override
			public void onHold() {

			}

			@Override
			public void onDrag(double degree) {
				applyToWheel(degree);
			}

			@Override
			public void onRelease() {

			}
		});


		mBtnUp = (Button)view.findViewById(R.id.button_up);
		mBtnUp.setOnTouchListener(mOnTouchListenerVertical);
		mBtnDown = (Button)view.findViewById(R.id.button_down);
		mBtnDown.setOnTouchListener(mOnTouchListenerVertical);
		mBtnLeft = (Button)view.findViewById(R.id.button_left);
		mBtnLeft.setOnTouchListener(mOnTouchListenerHorizontal);
		mBtnRight = (Button)view.findViewById(R.id.button_right);
		mBtnRight.setOnTouchListener(mOnTouchListenerHorizontal);
		//mRemoteLayout = (LinearLayout)view.findViewById(R.id.layout_remote);
		//mLocalLayout = (LinearLayout)view.findViewById(R.id.layout_local);
		mTvBattery = (TextView)view.findViewById(R.id.textView_battery);
		//mEtxInput = (EditText)view.findViewById(R.id.editText_input);
//		mEtxInput.setVisibility(View.GONE);
//		mEtxInput.addTextChangedListener(new TextWatcher() {
//
//			@Override
//			public void onTextChanged(CharSequence s, int start, int before, int count) {
//				// TODO Auto-generated method stub
//
//			}
//
//			@Override
//			public void beforeTextChanged(CharSequence s, int start, int count,
//					int after) {
//				// TODO Auto-generated method stub
//
//			}
//
//			@Override
//			public void afterTextChanged(Editable s) {
//				String str = s.toString();
//				if(str.equals("前进"))
//				{
//					ControlCommand command = new ControlCommand();
//					command.mState = (byte) BUTTON_STATE_DOWN;
//					command.mDirection = (byte) BUTTON_INDEX_DOWN;
//					sendCommand(command);
//
//				}
//				else if(str.endsWith("后退"))
//				{
//					ControlCommand command = new ControlCommand();
//					command.mState = (byte) BUTTON_STATE_DOWN;
//					command.mDirection = (byte) BUTTON_INDEX_UP;
//					sendCommand(command);
//				}
//
//			}
//		});



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


			}	
			if(btnIndex == BUTTON_INDEX_DOWN)
			{
				mBtnDown.setSelected(true);
				mBtnDown.setBackgroundColor(0xFFFF0000);

			}
			if(btnIndex == BUTTON_INDEX_LEFT)
			{
				mBtnLeft.setSelected(true);
				mBtnLeft.setBackgroundColor(0xFFFF0000);


			}
			if(btnIndex == BUTTON_INDEX_RIGHT)
			{
				mBtnRight.setSelected(true);
				mBtnRight.setBackgroundColor(0xFFFF0000);

			}
		}
		else if(state == BUTTON_STATE_UP)
		{
			if(btnIndex == BUTTON_INDEX_UP)
			{
				mBtnUp.setSelected(false);
				mBtnUp.setBackgroundColor(0xFFFFFFFF);
				stopStartSound();
			}	
			if(btnIndex == BUTTON_INDEX_DOWN)
			{
				mBtnDown.setSelected(false);
				mBtnDown.setBackgroundColor(0xFFFFFFFF);

			}
			if(btnIndex == BUTTON_INDEX_LEFT)
			{
				mBtnLeft.setSelected(false);
				mBtnLeft.setBackgroundColor(0xFFFFFFFF);

			}
			if(btnIndex == BUTTON_INDEX_RIGHT)
			{
				mBtnRight.setSelected(false);
				mBtnRight.setBackgroundColor(0xFFFFFFFF);
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
		if(mIsWebRtcEnabled)
		{
			mWebrtc = new WebRTCLib();
			if(mIsServer)
			{
				mWebrtc.open(this.getActivity(),mRemoteIP);
			}
			else
			{
				mWebrtc.open(this.getActivity(),mRemoteIP);
			}
			mWebrtc.startCall(mRemoteLayout, mLocalLayout);
			if(this.mObserver != null){
				mWebrtc.setEngineObserver(this.mObserver);
			}
		}

    }
    
    @Override
    public void onPause(){
        super.onPause();
    }


	@Override
	public void onStart()
	{
		super.onStart();
		initSound();
		mWheelControl.start();

	}

    @Override
    public void onStop() {
		super.onStop();

		if(mIsWebRtcEnabled)
		{
			if (mWebrtc != null) {
				Log.v(WiFiServiceDiscoveryActivity.TAG, "webrtc release");
				mWebrtc.setEngineObserver(null);
				mWebrtc.stopCall();
				mWebrtc.close();
				mWebrtc = null;
			}
		}
		mWheelControl.stop();
	}

	MediaPlayer mMediaPlayer = null;
	private void initSound()
	{
		mMediaPlayer=MediaPlayer.create(getActivity(), R.raw.engine_start);
		mMediaPlayer.setVolume(1.0f, 1.0f);

	}

	private void playStartSound()
	{
		mMediaPlayer.seekTo(0);
		mMediaPlayer.start();
	}

	private void stopStartSound()
	{
		mMediaPlayer.pause();
	}


	private void applyToWheel(double degree)
	{
		final float degreeR = (float)(degree/Math.PI*180.0);
		Log.v(WiFiServiceDiscoveryActivity.TAG, "degreeR " + degreeR);
		if(degreeR<-0.01)
		{
			Log.v(WiFiServiceDiscoveryActivity.TAG, "turn left");
			ControlCommand command = new ControlCommand();
			command.mState = (byte) BUTTON_STATE_DOWN;
			command.mDirection = (byte) BUTTON_INDEX_LEFT;
			sendCommand(command);
		}
		else if(degreeR>0.01)
		{
			Log.v(WiFiServiceDiscoveryActivity.TAG, "turn right");
			ControlCommand command = new ControlCommand();
			command.mState = (byte) BUTTON_STATE_DOWN;
			command.mDirection = (byte) BUTTON_INDEX_RIGHT;
			sendCommand(command);
		}
		else if(Math.abs(degreeR)<0.01)
		{
			Log.v(WiFiServiceDiscoveryActivity.TAG, "reset wheel");
			ControlCommand command = new ControlCommand();
			command.mState = (byte) BUTTON_STATE_UP;
			command.mDirection = (byte) BUTTON_INDEX_RIGHT;
			sendCommand(command);
			command.mState = (byte) BUTTON_STATE_UP;
			command.mDirection = (byte) BUTTON_INDEX_LEFT;
			sendCommand(command);
		}
	}

}
