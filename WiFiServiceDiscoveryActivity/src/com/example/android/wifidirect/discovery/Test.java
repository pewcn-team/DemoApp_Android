package com.example.android.wifidirect.discovery;

import java.io.IOException;

import android.test.AndroidTestCase;

public class Test extends AndroidTestCase {
	protected void setUp() throws Exception {
		super.setUp();
	}
	
	public void testGPIO()
	{
		try {
			Runtime.getRuntime().exec("hwacc w 0xd4019054 0x00002000");
			Runtime.getRuntime().exec("hwacc w 0xd4019024 0x00002000");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			Runtime.getRuntime().exec("hwacc w 0xd4019054 0x00002000");
			Runtime.getRuntime().exec("hwacc w 0xd4019018 0x00002000");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
