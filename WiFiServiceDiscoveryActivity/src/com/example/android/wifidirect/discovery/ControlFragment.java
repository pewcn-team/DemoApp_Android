package com.example.android.wifidirect.discovery;

import org.webrtc.webrtcdemo.MediaEngineObserver;
import org.webrtc.webrtcdemo.WebRTCLib;

import com.example.android.wifidirect.discovery.DataTransfer.IDataReceiver;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

public class ControlFragment extends Fragment {
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
	public DataTransfer mDataTransfer = null;
	private Button currentButton = null;
	private Button prevButton = null;
	private String mRemoteIP;
	private MediaEngineObserver mObserver;
	private WebRTCLib mWebrtc;
	private Activity mActivity;
    boolean mIsServer = false;
	public ControlFragment(Activity activity, String remoteIP, MediaEngineObserver observer, DataTransfer dataTransfer, boolean isServer)
	{
		mActivity = activity;
		mDataTransfer = dataTransfer;
		mDataTransfer.registerDataReceiver(new IDataReceiver() {
			
			@Override
			public void onReceiveData(byte[] data) {
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
		});
		mRemoteIP = remoteIP;
		mObserver = observer;
		mIsServer = true;
		
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
			sendCommand(command);
			return false;
		}
	};
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.fragment_control, null);
		mBtnUp = (Button)view.findViewById(R.id.button_up);
		mBtnUp.setOnTouchListener(mOnTouchListener);
		mBtnDown = (Button)view.findViewById(R.id.button_down);
		mBtnDown.setOnTouchListener(mOnTouchListener);
		mBtnLeft = (Button)view.findViewById(R.id.button_left);
		mBtnLeft.setOnTouchListener(mOnTouchListener);
		mBtnRight = (Button)view.findViewById(R.id.button_right);
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
		mDataTransfer.sendData(command.toBytes(command));
	}
	
    @Override
    public void onResume (){
        super.onResume();
        mWebrtc = new WebRTCLib();
        if(mIsServer)
        {
        	mWebrtc.open(this.getActivity(),mRemoteIP,false,true);
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
    
    public void onPause(){
        super.onPause();

        if(mWebrtc != null){
        	mWebrtc.setEngineObserver(null);
        	mWebrtc.stopCall();
        	mWebrtc.close();
        	mWebrtc = null;
        }

    }
	

}
