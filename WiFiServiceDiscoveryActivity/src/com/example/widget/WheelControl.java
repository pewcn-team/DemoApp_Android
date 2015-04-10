package com.example.widget;

import android.app.Activity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import com.example.android.wifidirect.discovery.WiFiServiceDiscoveryActivity;
import com.example.connection.ControlCommand;
import org.jscience.mathematics.vector.Float64Vector;

/**
 * Created by yihao on 15/4/10.
 * 一个方向盘控制器，使用该控制器需要传递一个方向盘的View，一般是ImageView
 * 一个包含方向盘的{@link MyRelativeLayout}。
 * MyRelativeLayout会代替方向盘的view接收触摸事件，并通过{@link com.example.widget.WheelControl.RotateCallback}把方向盘的转动情况传回
 *
 */
public class WheelControl {
    public interface RotateCallback
    {
        /**
         * 手指触摸方向盘
         */
        public void onHold();

        /**
         * 手指滑动方向盘
         * @param degree 转动的角度，以弧度表示，逆时针旋转为正。
         */
        public void onDrag(final double degree);

        /**
         *  手指松开方向盘
         */
        public void onRelease();
    }

    private Thread sampleThread = new Thread(new Runnable() {
        @Override
        public void run() {
            while(!mIsStop)
            {
                double degree = calcRotation();
                //Log.v(WiFiServiceDiscoveryActivity.TAG, "" + degree);
                if(null != mRotateCallback)
                {
                    mRotateCallback.onDrag(degree);
                }
                applyToWheel(degree);
                try {
                    Thread.currentThread().sleep(30);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }) ;
    private View mWheel = null;
    private MyRelativeLayout mWheelContainer = null;
    private boolean mIsStop = true;
    private RotateCallback mRotateCallback;
    float mPrevX = 99999.0f, mPrevY = 99999.0f, mCurrX = 99999.0f, mCurrY = 99999.0f, mPivotX, mPivotY;
    private Activity mActivity = null;
    /**
     * 创建一个WheelControl需要一个方向盘的View，和一个包含方向盘的{@link MyRelativeLayout}
     * @param wheel
     * @param wheelContainer
     */
    public WheelControl(Activity activity, View wheel, MyRelativeLayout wheelContainer, RotateCallback callback)
    {
        mActivity = activity;
        mWheel = wheel;
        mWheelContainer = wheelContainer;
        mPivotX = mWheel.getPivotX();
        mPivotY = mWheel.getPivotY();
        mRotateCallback = callback;
        mWheelContainer.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Log.v(WiFiServiceDiscoveryActivity.TAG, "touch" + event.getX() + " " + event.getY());
                Log.v(WiFiServiceDiscoveryActivity.TAG, "piovt" + mPivotX + " " + mPivotY);
                if(event.getAction() == MotionEvent.ACTION_DOWN)
                {
                    mPrevX = event.getX();
                    mPrevY = event.getY();
                    ControlCommand command = new ControlCommand();

                }
                else if(event.getAction() == MotionEvent.ACTION_MOVE)
                {
                    mCurrX = event.getX();
                    mCurrY = event.getY();
                }
                else if(event.getAction() == MotionEvent.ACTION_UP)
                {
                    mPrevX = mCurrX = 99999.0f;
                    mPrevY = mCurrY = 99999.0f;
                }
                return false;
            }
        });
    }

    /**
     * 启动WheelControl
     */
    public void start()
    {
        if(mIsStop)
        {
            sampleThread.start();
            mIsStop = false;
        }

    }

    /**
     * 停止WheelControl
     */
    public void stop()
    {
        mIsStop = true;
    }

    private double calcRotation()
    {
        Float64Vector vec1 = Float64Vector.valueOf(mPrevX-mPivotX, mPrevY-mPivotY, 0);
        Float64Vector vec2 = Float64Vector.valueOf(mCurrX-mPivotX, mCurrY-mPivotY, 0);
        vec1 = vec1.times(vec1.norm().inverse());
        vec2 = vec2.times(vec2.norm().inverse());
        double degree;
        if(Math.abs(vec1.times(vec2).doubleValue()-1)<0.001)
        {
            degree = 0;
        }
        else
        {
            degree = Math.acos(vec1.times(vec2).doubleValue());
        }
        if(vec1.cross(vec2).get(2).doubleValue()<0)
        {
            degree = -degree;
        }
        return degree;
    }

    private void applyToWheel(double degree)
    {
        final float degreeR = (float)(degree/Math.PI*180.0);


        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mWheel.setRotation(degreeR);
            }
        });

    }
}
