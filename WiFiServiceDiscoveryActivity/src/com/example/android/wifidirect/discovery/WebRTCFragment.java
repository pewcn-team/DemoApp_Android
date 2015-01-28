package com.example.android.wifidirect.discovery;

import org.webrtc.webrtcdemo.MediaEngineObserver;
import org.webrtc.webrtcdemo.WebRTCLib;

import com.example.android.wifidirect.discovery.R;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;


public class WebRTCFragment extends Fragment {
    private View view;
    LinearLayout llRemoteSurface = null;
    LinearLayout llLocalSurface = null;
    WebRTCLib webrtc = null;
    String remoteIP = null;
    MediaEngineObserver observer = null;
    boolean mIsServer = false;
    public WebRTCFragment(String remoteIP,MediaEngineObserver ob, boolean isServer) {
        this.remoteIP = remoteIP;
        this.observer = ob;
        mIsServer = isServer;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.videocall, container, false);
        llRemoteSurface = (LinearLayout)view.findViewById(R.id.remoteView);
        llLocalSurface = (LinearLayout)view.findViewById(R.id.localView);

        return view;
    }

    @Override
    public void onResume (){
        super.onResume();
        webrtc = new WebRTCLib();
        if(mIsServer)
        {
        	 webrtc.open(this.getActivity(),remoteIP);
        }
        else
        {
        	webrtc.open(this.getActivity(),remoteIP);
        }
       
        webrtc.startCall(llRemoteSurface,llLocalSurface); 
        if(this.observer != null){
            webrtc.setEngineObserver(this.observer);
        }
    }
    
    public void onPause(){
        super.onPause();

        if(webrtc != null){
            webrtc.setEngineObserver(null);
            webrtc.stopCall();
            webrtc.close();
            webrtc = null;
        }

    }

}
