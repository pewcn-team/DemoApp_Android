package com.example.control;

import android.util.Log;

import java.io.IOException;

/**
 * Created by yihao on 15/1/19.
 */
public class TankController {
    /**
     *
     */
    public void startLeftForward() {
        try {
            Runtime.getRuntime().exec("hwacc w 0xd4019154 0x00000002");
            Runtime.getRuntime().exec("hwacc w 0xd4019118 0x00000002");
            Log.v("Control", "Press Forward Button");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     *
     */
    public void stopLeftForward() {
        try {
            Runtime.getRuntime().exec("hwacc w 0xd4019154 0x00000002");
            Runtime.getRuntime().exec("hwacc w 0xd4019118 0x00000002");
            Log.v("Control", "Press Forward Button");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     *
     */
    public void startLeftBack() {
        try {
            Runtime.getRuntime().exec("hwacc w 0xd4019054 0x00002000");
            Runtime.getRuntime().exec("hwacc w 0xd4019018 0x00002000");
            Log.v("Control", "Press Back Button");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     *
     */
    public void stopLeftBack() {
        try {
            Runtime.getRuntime().exec("hwacc w 0xd4019054 0x00002000");
            Runtime.getRuntime().exec("hwacc w 0xd4019024 0x00002000");
            Log.v("Control", "Release Back Button");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    /**
     *
     */
    public void startRightForward()
    {
        try {
            Runtime.getRuntime().exec("hwacc w 0xd4019054 0x00020000");
            Runtime.getRuntime().exec("hwacc w 0xd4019018 0x00020000");
            Log.v("Control", "Press Left Button");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     *
     */
    public void stopRightForward()
    {
        try {
            Runtime.getRuntime().exec("hwacc w 0xd4019054 0x00020000");
            Runtime.getRuntime().exec("hwacc w 0xd4019024 0x00020000");
            Log.v("Control", "Release Left Button");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     *
     */
    public void startRightBack()
    {
        try {
            Runtime.getRuntime().exec("hwacc w 0xd4019054 0x00010000");
            Runtime.getRuntime().exec("hwacc w 0xd4019018 0x00010000");
            Log.v("Control", "Press Right Button");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     *
     */
    public void stopRightBack()
    {
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
