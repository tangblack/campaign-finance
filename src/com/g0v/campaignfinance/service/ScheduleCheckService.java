package com.g0v.campaignfinance.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import com.g0v.campaignfinance.AppConfig;

import java.util.Calendar;
import java.util.Date;


/**
 * The class <code>ScheduleCheckService</code> 
 *
 * @version 2013/11/11
 * @since 2013/11/11
 * 
 * @see <a href="http://stackoverflow.com/questions/4562757/alarmmanager-android-every-day">AlarmManager Android Every Day</a>
 */
public class ScheduleCheckService extends Service
{
	private static final String TAG = ScheduleCheckService.class.getSimpleName();
	
	@Override
	public void onCreate()
	{
		super.onCreate();

		logDebug("onCreate()");
		
		Intent intent = new Intent(this, CheckReceiver.class);
		PendingIntent pendingIntent = 
				PendingIntent.getBroadcast(this, 
						0, 
						intent,
						PendingIntent.FLAG_UPDATE_CURRENT);
		
		AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
		alarmManager.cancel(pendingIntent);
		Calendar triggerAtTime = Calendar.getInstance();
		
		long interval;
		if (AppConfig.DEBUG == true)
    	{
			triggerAtTime.setTimeInMillis(System.currentTimeMillis() + 10000);
			interval = 60000; // Interval(ms). 60 * 1000
    	}
		else
		{
			/* 5:00 11:00, 17:00, 23:00 */
//			triggerAtTime.set(Calendar.HOUR_OF_DAY, 5);
//			triggerAtTime.set(Calendar.MINUTE, 0);
//			triggerAtTime.set(Calendar.SECOND, 0);
//			triggerAtTime.add(Calendar.DAY_OF_YEAR, 1); // to avoid firing the alarm immediately.
//			interval = 21600000; // Interval(ms). 1/4 day = 24/4 * 60 * 60 * 1000
			
			triggerAtTime.setTimeInMillis(System.currentTimeMillis() + 10000);
			interval = 43200000; // Interval(ms). 1/2 day = 12 * 60 * 60 * 1000
		}
		
		alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, 
				triggerAtTime.getTimeInMillis(), 				// Trigger at time.
				interval,										// Interval(ms).
                pendingIntent);

		logDebug("ScheduleCheckService be triggered at " + new Date(triggerAtTime.getTimeInMillis()) + " and repeat every " + interval + " ms.");
	}
	
	@Override
	public void onDestroy()
	{
		super.onDestroy();

		logDebug("onDestroy()");
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		logDebug("onStartCommand()");
		
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public IBinder onBind(Intent arg0)
	{
		return null;
	}

	private void logDebug(String message)
	{
		if (AppConfig.DEBUG)
		{
			Log.d(TAG, message);
		}
	}

}
