package com.example.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.RelativeLayout;
import com.example.android.wifidirect.discovery.WiFiServiceDiscoveryActivity;

/**
 * Created by yihao on 15/4/10.
 */
public class MyRelativeLayout extends RelativeLayout {
    public MyRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event){
        super.dispatchTouchEvent(event);
        return true;
    }

    //該方法只會在dispatchTouchEvent調用了super.dispatchTouchEvent之後才會觸發。
    //默認return false 只有return false子View才能接收到触摸事件
    @Override
    public boolean onInterceptTouchEvent(MotionEvent event){
        System.out.println("onInterceptTouchEvent method is start##");
        super.onInterceptTouchEvent(event);

        return true;
    }
    //該方法是用來消耗觸摸事件的
    //默認return false。return true 的話，就表示該事件已經處理結束。
    @Override
    public boolean onTouchEvent(MotionEvent event){
        Log.v(WiFiServiceDiscoveryActivity.TAG, "on MyTouch");
        super.onTouchEvent(event);
        return true;
    }
}
