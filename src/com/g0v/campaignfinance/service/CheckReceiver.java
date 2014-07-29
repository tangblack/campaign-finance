package com.g0v.campaignfinance.service;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import com.g0v.campaignfinance.AppConfig;
import com.g0v.campaignfinance.R;
import com.g0v.campaignfinance.StartActivity;
import com.g0v.campaignfinance.controller.CampaignFinanceController;
import com.g0v.campaignfinance.model.CellCount;

import java.io.IOException;
import java.net.MalformedURLException;

public class CheckReceiver extends BroadcastReceiver
{
	private static final String TAG = CheckReceiver.class.getSimpleName();
	
	private Handler handler = new Handler();
	private Context context;
	
	
	@Override
	public void onReceive(Context arg0, Intent arg1) 
	{
		logDebug("onReceive()");
		context = arg0;
		GetCellCountTask getCellCountTask = new GetCellCountTask();
		getCellCountTask.execute();
	}

	private void logDebug(String message)
	{
		if (AppConfig.DEBUG)
		{
			Log.d(TAG, message);
		}
	}

	private class GetCellCountTask extends AsyncTask<Void, Void, Void> // Params, Progress, Result
	{
		private boolean isAnyException = false;
		private String errorMessage;

		private CampaignFinanceController campaignFinanceController;
		private CellCount cellCount;

		public GetCellCountTask()
		{

		}

		@Override
		protected void onPreExecute()
		{
			super.onPreExecute();
		}

		@Override
		protected Void doInBackground(Void... params)
		{
			logDebug("doInBackground");

			if (checkNetwork() == false)
			{
				logDebug("checkNetwork() == false");
				isAnyException = true;
				errorMessage = "checkNetwork() == false!";
			}

			try
			{
				campaignFinanceController = CampaignFinanceController.getInstance();
				cellCount = campaignFinanceController.getCellCount();
				logDebug("cellCount=" + cellCount);
			}
			catch (MalformedURLException e)
			{
				e.printStackTrace();
				isAnyException = true;
				return null;
			}
			catch (IOException e)
			{
				e.printStackTrace();
				isAnyException = true;
				return null;
			}

			return null;
		}

		/**
		 *
		 *
		 * @return
		 * @see <a href="http://jjnnykimo.pixnet.net/blog/post/30349753-android-%E5%81%B5%E6%B8%AC%E7%B6%B2%E8%B7%AF%E6%98%AF%E5%90%A6%E9%80%A3%E7%B7%9A">android ?�測網路?�否???</a>
		 */
		private boolean checkNetwork()
		{
			ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo info = connManager.getActiveNetworkInfo();

			if (info == null || !info.isConnected())
			{
				return false;
			}
			else
			{
				return info.isAvailable();
			}
		}

		@Override
		protected void onPostExecute(Void result)
		{
			// Update UI in UI thread!
			if (isAnyException)
			{
				if (errorMessage != null)
				{
					Log.e(TAG, errorMessage);
				}
			}

        	/* Always show notification. */
			if (cellCount != null && cellCount.getTodo().longValue() > 0)
			{
				Log.i(TAG, "Has new task and show notification.");
				showNotification(cellCount);
			}
			else
			{
				Log.i(TAG, "No new task.");
				if (AppConfig.DEBUG == true)
				{
					showNotification(null);
				}
			}
		}
	}
	
	private void showNotification(final CellCount cellCount)
	{
		logDebug("showNotification");
		
		handler.post(new Runnable()
		{
			@SuppressLint("NewApi")
			@Override
			public void run()
			{
				Log.i(TAG, "android.os.Build.VERSION.SDK_INT=" + Build.VERSION.SDK_INT);

				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
				{
					Intent intent = new Intent();
	        		intent.setClass(context, StartActivity.class);
	        		PendingIntent pendingIntent = 
	        				PendingIntent.getActivity(context, 0, intent, Intent.FLAG_ACTIVITY_NEW_TASK);
					
					NotificationManager notificationManager = 
							(NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
					logDebug("notificationManager=" + notificationManager);
					
					Bitmap largeIcon = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_launcher);
					
					Notification.Builder builder = new Notification.Builder(context);
					builder.setSmallIcon(R.drawable.ic_stat_alert); // Must set icon for sending it!!
					builder.setLargeIcon(largeIcon);
					builder.setAutoCancel(true);
					builder.setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_VIBRATE);
					builder.setLights(Color.YELLOW, 500, 500);
					
					if (cellCount != null)
					{
						builder.setTicker(context.getString(R.string.new_task_notification_ticker));
						builder.setContentTitle(context.getString(R.string.new_task_notification_content_title));

						String contentText = String.format(context.getString(R.string.new_task_notification_content_text), cellCount.getTodo());
						builder.setContentText(contentText);
					}
					else
					{
						builder.setTicker(context.getString(R.string.new_task_notification_ticker));
						builder.setContentTitle(context.getString(R.string.new_task_notification_content_title));

						String contentText = String.format(context.getString(R.string.new_task_notification_content_text), "" + 19700204);
						builder.setContentText(contentText);
					}
							
					builder.setContentIntent(pendingIntent);
					
					
					Log.d(TAG, "builder.build()=" + builder.build());
					notificationManager.notify(0, builder.build());
			    }
			}
		});
	}
}
