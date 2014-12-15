/*
 *  Copyright (c) 2013 The WebRTC project authors. All Rights Reserved.
 *
 *  Use of this source code is governed by a BSD-style license
 *  that can be found in the LICENSE file in the root of the source
 *  tree. An additional intellectual property rights grant can be found
 *  in the file PATENTS.  All contributing project authors may
 *  be found in the AUTHORS file in the root of the source tree.
 */

package org.webrtc.webrtcdemo;


import android.content.Context;
import android.view.SurfaceView;
import android.widget.LinearLayout;

public class WebRTCLib {

  private NativeWebRtcContextRegistry contextRegistry = null;
  private MediaEngine mediaEngine = null;
  private MediaEngine getEngine() { return mediaEngine; }
  private LinearLayout llRemoteSurface = null;
  private LinearLayout llLocalSurface = null;
  /***
   * open -- 打开WebRTCLib
   * @param context 使用WebRTCLib的Activity的对象
   * @param remoteIP 对端的IP
   * @param rxVideo Enable接受视频 -- 现在都是true
   * @param txVideo Enable发送视屏 -- 现在都是true
   */
  public void open(Context context,String remoteIP,boolean rxVideo,boolean txVideo) {
    // State.
    // Must be instantiated before MediaEngine.
    contextRegistry = new NativeWebRtcContextRegistry();
    contextRegistry.register(context);

    // Load all settings dictated in xml.
    mediaEngine = new MediaEngine(context);
    mediaEngine.setRemoteIp(remoteIP);
    mediaEngine.setTrace(true);

    mediaEngine.setAudio(true);
    mediaEngine.setAudioCodec(mediaEngine.getIsacIndex());
    if(rxVideo){
        mediaEngine.setAudioRxPort(11113);
    }
    if(txVideo){
        mediaEngine.setAudioTxPort(11113);
    }
    mediaEngine.setSpeaker(false);
    mediaEngine.setDebuging(true);

    mediaEngine.setReceiveVideo(rxVideo);
    mediaEngine.setSendVideo(txVideo);
    mediaEngine.setVideoCodec(0);
    //VGA
    mediaEngine.setResolutionIndex(3);
    if(txVideo){
        mediaEngine.setVideoTxPort(11111);
    }
    if(rxVideo){
        mediaEngine.setVideoRxPort(11111);
    }
    mediaEngine.setNack(true);
    //opengl
    mediaEngine.setViewSelection(context.getResources().getInteger(R.integer.openGl));
  }

  /***
   * close -- 关闭WebRTCLib，请和open成对使用
   */
  public void close() {
      assert(mediaEngine!=null);
    mediaEngine.dispose();
    contextRegistry.unRegister();
  }

  /***
   * setEngineObserver 设置状态监听器
   * @param ob 上报的当前状态 包括in/out的帧率，比特率，丢包率等。
   */
  public void setEngineObserver(MediaEngineObserver ob){
      assert(mediaEngine!=null);
      mediaEngine.setObserver(ob);
  }

  private void setViews(LinearLayout llRemoteSurface,LinearLayout llLocalSurface) {
      SurfaceView remoteSurfaceView = getEngine().getRemoteSurfaceView();
      if (remoteSurfaceView != null) {
        llRemoteSurface.addView(remoteSurfaceView);
      }
      SurfaceView svLocal = getEngine().getLocalSurfaceView();
      if (svLocal != null) {
        llLocalSurface.addView(svLocal);
      }
    }

    private void clearViews(LinearLayout llRemoteSurface,LinearLayout llLocalSurface) {
      SurfaceView remoteSurfaceView = getEngine().getRemoteSurfaceView();
      if (remoteSurfaceView != null) {
        llRemoteSurface.removeView(remoteSurfaceView);
      }
      SurfaceView svLocal = getEngine().getLocalSurfaceView();
      if (svLocal != null) {
        llLocalSurface.removeView(svLocal);
      }
    }

    /***
     * stopCall 结束呼叫对端
     */
    public void stopCall() {
        clearViews(this.llRemoteSurface,this.llLocalSurface);
        getEngine().stop();
      }
    
    /***
     * startCall 开始呼叫对端, 需要确保传入的llRemoteSurface，llLocalSurface 完全初始化
     * @param llRemoteSurface - 容纳远端视频的LinearLayout（Layout里为空）
     * @param llLocalSurface - 容纳近端视频的LinearLayout（Layout里为空）
     */
      public void startCall(LinearLayout llRemoteSurface,LinearLayout llLocalSurface) {
          this.llRemoteSurface = llRemoteSurface;
          this.llLocalSurface = llLocalSurface;
        getEngine().start();
        setViews(llRemoteSurface,llLocalSurface);
      }
}