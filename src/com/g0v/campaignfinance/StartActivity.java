package com.g0v.campaignfinance;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.g0v.campaignfinance.controller.CampaignFinanceController;
import com.g0v.campaignfinance1.R;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by tangblack on 2014/7/25.
 */
public class StartActivity extends Activity
{
	private static final String TAG = StartActivity.class.getSimpleName();

	private ProgressBar progressBar;


	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		logDebug("onCreate");

		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_start);

		progressBar = (ProgressBar) findViewById(R.id.progressBar1);

		startScheduleCheckService();
		gotoMainActivity();
	}

	/**
	 * @see <a href="http://stackoverflow.com/questions/7335600/autostart-of-service-in-android-app">autostart of service in android app</a>
	 */
	private void startScheduleCheckService()
	{
		logDebug("startScheduleCheckService");

		Intent intent = new Intent();
		intent.setAction("com.g0v.campaignfinance.service.ScheduleCheckService");
		startService(intent);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		logDebug("onCreateOptionsMenu");

		return true;
	}

	@Override
	protected void onStop()
	{
		logDebug("onStop");

		super.onStop();
	}

	private void gotoMainActivity()
	{
		logDebug("gotoMainActivity");

		/*
		 * Start an asyncTask.
		 * Don't do any time-consuming task in main thread(UI thread).
		 */
		new CreateCampaignFinanceControllerTask().execute();
	}

	private void logDebug(String message)
	{
		if (AppConfig.DEBUG)
		{
			Log.d(TAG, message);
		}
	}

	/**
	 * Get products.
	 *
	 * @see <a href="http://www.apkbus.com/home.php?mod=space&uid=188355&do=blog&id=50875">UI线程和工作线程</a>
	 */
	private class CreateCampaignFinanceControllerTask extends AsyncTask<Void, Integer, Map> // Params, Progress, Result
	{
		private static final int PROGRESS_BAR_ADD_ONE = 0;

		private boolean isIOException = false;
		private String errorMessage;


		@Override
		protected void onPreExecute()
		{
			super.onPreExecute();
			publishProgress(3); // Invoke onProgressUpdate.
		}

		@Override
		protected Map doInBackground(Void... params)
		{
			if (AppConfig.DEBUG) Log.d(TAG, "doInBackground");


			if (checkNetwork() == false)
			{
				isIOException = true;
			}
			publishProgress(Integer.valueOf(PROGRESS_BAR_ADD_ONE)); // Invoke onProgressUpdate.


			Map resultMap = new HashMap();

			try
			{
				CampaignFinanceController.getInstance();
				publishProgress(Integer.valueOf(PROGRESS_BAR_ADD_ONE)); // Invoke onProgressUpdate.
			}
			catch (IOException e)
			{
				isIOException = true;
				e.printStackTrace();
				errorMessage = e.getMessage();
			}

			return resultMap;
		}

		/**
		 *
		 *
		 * @return
		 * @see <a href="http://jjnnykimo.pixnet.net/blog/post/30349753-android-%E5%81%B5%E6%B8%AC%E7%B6%B2%E8%B7%AF%E6%98%AF%E5%90%A6%E9%80%A3%E7%B7%9A">android ?�測網路?�否???</a>
		 */
		private boolean checkNetwork()
		{
			ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
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
		protected void onProgressUpdate(Integer... params)
		{
			int param = params[0].intValue();
			logDebug("params[0].intValue() =  " + param);
			if (param > 0) // Receive the qty of menu.
			{
				logDebug("progressBar.setMax as " + param);
				progressBar.setMax(param);

			}
			else if (param == PROGRESS_BAR_ADD_ONE) // Receive finishing parsing one page.
			{
				logDebug("progressBar.incrementProgressBy(1).");
				progressBar.incrementProgressBy(1);
			}
			else
			{
				// Don't update!
			}
		}

		@Override
		protected void onPostExecute(Map result)
		{
			logDebug("onPostExecute result = " + result);
			publishProgress(Integer.valueOf(PROGRESS_BAR_ADD_ONE)); // Invoke onProgressUpdate.

			// Update UI in UI thread!
			if (isIOException)
			{
				Toast.makeText(getApplicationContext(), getString(R.string.network_error), Toast.LENGTH_LONG).show();
				//        		Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_LONG).show();
			}
			else
			{
				Intent intent = new Intent();
				intent.setClass(StartActivity.this, MainActivity.class);
				Bundle bundle = new Bundle();
				intent.putExtras(bundle);
				startActivity(intent);
			}

			finish();
		}
	}
}
