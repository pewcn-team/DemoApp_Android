package com.example.app;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.example.android.wifidirect.discovery.R;
import com.example.connection.ControlCommand;
import com.example.connection.DataTransferTCP;
import com.example.control.TankController;
import com.example.wifiap.WifiAPBase;

import org.webrtc.webrtcdemo.MediaEngineObserver;
import org.webrtc.webrtcdemo.WebRTCLib;

public class ControlFragmentTank extends Fragment {
	Button mBtnUp;
	Button mBtnDown;
	Button mBtnLeft;
	Button mBtnRight;
	LinearLayout mRemoteLayout;
	LinearLayout mLocalLayout;
	public static int BUTTON_INDEX_UP = 0;
	public static int BUTTON_INDEX_DOWN = 1;
	public static int BUTTON_INDEX_LEFT = 2;
	public static int BUTTON_INDEX_RIGHT = 3;
	public static int BUTTON_STATE_DOWN = 0;
	public static int BUTTON_STATE_UP = 1;
	public DataTransferTCP mDataTransfer = null;
	private Button currentButton = null;
	private Button prevButton = null;
	private String mRemoteIP;
	private MediaEngineObserver mObserver;
	private WebRTCLib mWebrtc;
	private Activity mActivity;
    boolean mIsServer = false;
    private ControlCommand mCurrControlCommand = null;
	private int mDirection = 0;
	private TankController mController = null;
	private WifiAPBase mWifiAP;
	public ControlFragmentTank()
	{
	}

	public ControlFragmentTank(Activity activity, String remoteIP, MediaEngineObserver observer, WifiAPBase wifiAP, boolean isServer)
	{
		mActivity = activity;
		mWifiAP = wifiAP;
//		mWifiAP.registerDataReceiver(new IDataReceiver() {
//
//			@Override
//			public void onReceiveData(byte[] data) {
//				final ControlCommand command = (ControlCommand) new ControlCommand().fromBytes(data);
//				if(null != command)
//				{
//					mActivity.runOnUiThread(new Runnable() {
//
//						@Override
//						public void run() {
//							changeButtonState(command.mDirection, command.mState);
//
//						}
//					});
//
//				}
//
//			}
//		});

		mRemoteIP = remoteIP;
		mObserver = observer;
		mIsServer = isServer;
		if(mIsServer)
		{
			mController = new TankController();
		}

		
	}
	
	private View.OnTouchListener mOnTouchListener = new View.OnTouchListener() {
		
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
			else if(v.getId() == R.id.button_left)
			{
				command.mDirection = (byte) BUTTON_INDEX_LEFT;
			}
			else if(v.getId() == R.id.button_right)
			{
				command.mDirection = (byte) BUTTON_INDEX_RIGHT;
			}
			if(mCurrControlCommand == null)
			{
				sendCommand(command);
				mCurrControlCommand = command;
				
			}
			else
			{
				if(mCurrControlCommand.mState!=command.mState||mCurrControlCommand.mDirection!=command.mDirection)
				{
					sendCommand(command);
					mCurrControlCommand = command;
				}
			}
			
			return false;
		}
	};
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.fragment_control_tank, null);
		mBtnUp = (Button)view.findViewById(R.id.button_up_left);
		mBtnUp.setOnTouchListener(mOnTouchListener);
		mBtnDown = (Button)view.findViewById(R.id.button_down_left);
		mBtnDown.setOnTouchListener(mOnTouchListener);
		mBtnLeft = (Button)view.findViewById(R.id.button_up_right);
		mBtnLeft.setOnTouchListener(mOnTouchListener);
		mBtnRight = (Button)view.findViewById(R.id.button_down_right);
		mBtnRight.setOnTouchListener(mOnTouchListener);
		mRemoteLayout = (LinearLayout)view.findViewById(R.id.layout_remote);
		mLocalLayout = (LinearLayout)view.findViewById(R.id.layout_local);
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
				mController.startLeftForward();
			}	
			if(btnIndex == BUTTON_INDEX_DOWN)
			{
				mBtnDown.setSelected(true);
				mBtnDown.setBackgroundColor(0xFFFF0000);
				mController.startLeftBack();
			}
			if(btnIndex == BUTTON_INDEX_LEFT)
			{
				mBtnLeft.setSelected(true);
				mBtnLeft.setBackgroundColor(0xFFFF0000);
				mController.startRightForward();
			}
			if(btnIndex == BUTTON_INDEX_RIGHT)
			{
				mBtnRight.setSelected(true);
				mBtnRight.setBackgroundColor(0xFFFF0000);
				mController.startRightBack();
			}
		}
		else if(state == BUTTON_STATE_UP)
		{
			if(btnIndex == BUTTON_INDEX_UP)
			{
				mBtnUp.setSelected(false);
				mBtnUp.setBackgroundColor(0xFFFFFFFF);
				mController.stopLeftForward();
			}	
			if(btnIndex == BUTTON_INDEX_DOWN)
			{
				mBtnDown.setSelected(false);
				mBtnDown.setBackgroundColor(0xFFFFFFFF);
				mController.stopLeftBack();
			}
			if(btnIndex == BUTTON_INDEX_LEFT)
			{
				mBtnLeft.setSelected(false);
				mBtnLeft.setBackgroundColor(0xFFFFFFFF);
				mController.stopRightForward();
			}
			if(btnIndex == BUTTON_INDEX_RIGHT)
			{
				mBtnRight.setSelected(false);
				mBtnRight.setBackgroundColor(0xFFFFFFFF);
				mController.stopRightBack();
			}
		}
	}
	
	private void sendCommand(ControlCommand command)
	{
		mDataTransfer.sendData(command.toBytes());
	}
	
    @Override
    public void onResume (){
        super.onResume();
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
    public void onStop(){
        super.onStop();

        if(mWebrtc != null){
        	mWebrtc.setEngineObserver(null);
        	mWebrtc.stopCall();
        	mWebrtc.close();
        	mWebrtc = null;
        }

    }
}
