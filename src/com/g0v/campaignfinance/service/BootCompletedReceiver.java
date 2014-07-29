package com.g0v.campaignfinance.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * 
 * The class <code>BootCompletedReceiver</code> 
 *
 * @version 2013/11/8
 * @since 2013/11/8
 * 
 * @see <a href="http://stackoverflow.com/questions/4562734/android-starting-service-at-boot-time"></a>
 * @see <a href="http://stackoverflow.com/questions/7335600/autostart-of-service-in-android-app">autostart of service in android app</a>
 */
public class BootCompletedReceiver extends BroadcastReceiver
{
	private static final String TAG = BootCompletedReceiver.class.getSimpleName();
	
	@Override
	public void onReceive(Context context, Intent intent) 
	{
	    if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) 
	    { 
	    	Log.d(TAG,"Start ScheduleCheckService");  	    	
	    	
	    	Intent myIntent = new Intent(context, ScheduleCheckService.class);
	        context.startService(myIntent);
	    }
	}
}
