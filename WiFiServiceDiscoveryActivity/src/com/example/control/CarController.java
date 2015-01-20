package com.example.control;

import android.util.Log;

import java.io.IOException;

/**
 * Created by yihao on 15/1/19.
 */
public class CarController {

    private MotorPWM mMotorPWM = null;
    private int mCurrDirection = 0;
    public CarController()
    {
        mMotorPWM = new MotorPWM();
        mMotorPWM.start();
    }

    /**
     *
     * @param level
     */
    public void setMotorLevel(int level)
    {
        mMotorPWM.changeLevel(level);
    }

    /**
     *
     */
    public void turnLeft(){
        mCurrDirection = -1;
        DirectionPulse pulse = new DirectionPulse(mCurrDirection);
        pulse.fire();
    }

    /**
     *
     */
    public void turnRight(){
        mCurrDirection = 1;
        DirectionPulse pulse = new DirectionPulse(mCurrDirection);
        pulse.fire();
    }

    /**
     *
     */
    public void resetDirection()
    {
        DirectionPulse pulse = new DirectionPulse(-mCurrDirection);
        pulse.fire();
        mCurrDirection = 0;
    }

    private class MotorPWM
    {
        int mLevel = 0;
        public MotorPWM()
        {

        }

        public void changeLevel(int level)
        {
            mLevel = level;
        }

        public void start()
        {
            Runnable engine = new Runnable() {
                @Override
                public void run() {
                    while(true)
                    {
                        if(mLevel == 2)
                        {
                            try {
                                Runtime.getRuntime().exec("hwacc w 0xd4019154 0x00000002");
                                Runtime.getRuntime().exec("hwacc w 0xd4019118 0x00000002");
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                        }
                        else if(mLevel == 1)
                        {
                            try {
                                Log.v("Control", "High Pulse");
                                Runtime.getRuntime().exec("hwacc w 0xd4019154 0x00000002");
                                Runtime.getRuntime().exec("hwacc w 0xd4019118 0x00000002");
                                try {
                                    Thread.currentThread().sleep(5);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                Log.v("Control", "Low Pulse");
                                Runtime.getRuntime().exec("hwacc w 0xd4019154 0x00000002");
                                Runtime.getRuntime().exec("hwacc w 0xd4019124 0x00000002");
                                try {
                                    Thread.currentThread().sleep(5);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        else if(mLevel == 0)
                        {
                            try {
                                Runtime.getRuntime().exec("hwacc w 0xd4019154 0x00000002");
                                Runtime.getRuntime().exec("hwacc w 0xd4019124 0x00000002");
                                Runtime.getRuntime().exec("hwacc w 0xd4019054 0x00002000");
                                Runtime.getRuntime().exec("hwacc w 0xd4019024 0x00002000");
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        else if(mLevel == -1)
                        {
                            try {
                                Runtime.getRuntime().exec("hwacc w 0xd4019054 0x00002000");
                                Runtime.getRuntime().exec("hwacc w 0xd4019018 0x00002000");
                            } catch (IOException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
/*                            try {
                                Thread.currentThread().sleep(500);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                            try {
                                Runtime.getRuntime().exec("hwacc w 0xd4019054 0x00002000");
                                Runtime.getRuntime().exec("hwacc w 0xd4019024 0x00002000");
                            } catch (IOException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                            try {
                                Thread.currentThread().sleep(500);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }*/
                        }
                    }
                }
            };
            Thread t= new Thread(engine);
            t.start();

        }

    }

    private class DirectionPulse
    {
        int mDirection;
        public DirectionPulse(int direction)
        {
            mDirection = direction;
        }

        public void fire()
        {
            Runnable dirPulse = new Runnable() {
                @Override
                public void run() {
                    if(mDirection == -1)
                    {
                        Log.v("Control", "left pulse");
                        try {
                            Runtime.getRuntime().exec("hwacc w 0xd4019154 0x00020000");
                            Runtime.getRuntime().exec("hwacc w 0xd4019118 0x00020000");
                            try {
                                Thread.currentThread().sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

/*                            Runtime.getRuntime().exec("hwacc w 0xd4019154 0x00020000");
                            Runtime.getRuntime().exec("hwacc w 0xd4019124 0x00020000");
                            try {
                                Thread.currentThread().sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }*/
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    else if(mDirection == 1)
                    {
                        Log.v("Control", "right pulse");
                        try {
                            Runtime.getRuntime().exec("hwacc w 0xd4019054 0x00010000");
                            Runtime.getRuntime().exec("hwacc w 0xd4019018 0x00010000");
                            try {
                                Thread.currentThread().sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

/*                            Runtime.getRuntime().exec("hwacc w 0xd4019054 0x00010000");
                            Runtime.getRuntime().exec("hwacc w 0xd4019024 0x00010000");
                            try {
                                Thread.currentThread().sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }*/
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            };
            Thread t = new Thread(dirPulse);
            t.start();


        }

    }

}
