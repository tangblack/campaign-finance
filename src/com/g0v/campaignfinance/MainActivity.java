package com.g0v.campaignfinance;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.*;
import android.widget.*;
import com.g0v.campaignfinance.controller.CampaignFinanceController;
import com.g0v.campaignfinance.controller.OcrController;
import com.g0v.campaignfinance.model.Cell;
import com.g0v.campaignfinance.model.CellCount;

import java.io.IOException;
import java.net.MalformedURLException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends Activity
{
	private static final String TAG = MainActivity.class.getSimpleName();
	private static final Pattern PATTERN_ONLY_NUMBER = Pattern.compile("^(\\d+)$"); // ^(\d+)$
	private static final NumberFormat FORMAT_MONEY = new DecimalFormat("#,###,###");

	private ProgressBar progressBar;
	private ImageView dataImageView;
	private TextView currentAnswerTextView;
	private Button isRightButton;
	private Button isEmptyButton;
	private Button quickInputButton;
	private Button inputButton;
	private Button notClearButton;
	private Button ignoreButton;
	private TextView linkToFanPageTextView;
	private ArrayAdapter<String> quickInputItemArrayAdapter;
	private MenuItem shareMenuItem;

	private MediaPlayer coinSound;
	private MediaPlayer kickSound;
	private MediaPlayer oneUpSound;

	private CampaignFinanceController campaignFinanceController;
	private Cell cell;
	private CellCount cellCount;
	private Bitmap dataBitmap;

	private long pollCellCount = 0;
	private long playCoinSoundCount = 0;

	private OcrController ocrController;


	/**
	 * Called when the activity is first created.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		try
		{
			campaignFinanceController = CampaignFinanceController.getInstance();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		ocrController = new OcrController(getApplicationContext());

		quickInputItemArrayAdapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.select_dialog_singlechoice);
		quickInputItemArrayAdapter.add(getString(R.string.quick_input_item_1));
		quickInputItemArrayAdapter.add(getString(R.string.quick_input_item_2));
		quickInputItemArrayAdapter.add(getString(R.string.quick_input_item_3));
		quickInputItemArrayAdapter.add(getString(R.string.quick_input_item_4));
		quickInputItemArrayAdapter.add(getString(R.string.quick_input_item_5));
		quickInputItemArrayAdapter.add(getString(R.string.quick_input_item_6));
		quickInputItemArrayAdapter.add(getString(R.string.quick_input_item_7));
		quickInputItemArrayAdapter.add(getString(R.string.quick_input_item_8));
		quickInputItemArrayAdapter.add(getString(R.string.quick_input_item_9));
		quickInputItemArrayAdapter.add(getString(R.string.quick_input_item_10));
		quickInputItemArrayAdapter.add(getString(R.string.quick_input_item_11));
		quickInputItemArrayAdapter.add(getString(R.string.quick_input_item_12));
		quickInputItemArrayAdapter.add(getString(R.string.quick_input_item_13));

		progressBar = (ProgressBar) findViewById(R.id.progressBar);

		dataImageView = (ImageView) findViewById(R.id.dataImageView);

		currentAnswerTextView = (TextView) findViewById(R.id.currentAnswerTextView);

		isRightButton = (Button)findViewById(R.id.isRightButton);
		isRightButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				playCoinSound();

				campaignFinanceController.openThreadToFillCellIsRight(cell);
				MainActivity.this.doNextWork();
			}
		});

		isEmptyButton = (Button)findViewById(R.id.isEmptyButton);
		isEmptyButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				playCoinSound();

				campaignFinanceController.openThreadToFillCellIsEmpty(cell);
				MainActivity.this.doNextWork();
			}
		});

		quickInputButton = (Button)findViewById(R.id.quickInputButton);
		quickInputButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				popupQuickDialog();
			}
		});

		inputButton = (Button)findViewById(R.id.inputButton);
		inputButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				//TODO Open task to do OCR.
				new PopupInputDialogTask().execute();
			}
		});

		notClearButton = (Button)findViewById(R.id.notClearButton);
		notClearButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				playKickSound();

				campaignFinanceController.openThreadToFillCellNotClear(cell);
				MainActivity.this.doNextWork();
			}
		});

		ignoreButton = (Button)findViewById(R.id.ignoreButton);
		ignoreButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				playKickSound();

				MainActivity.this.doNextWork();
			}
		});

		linkToFanPageTextView = (TextView)findViewById(R.id.linkToFanPageTextView);
		linkToFanPageTextView.setMovementMethod(LinkMovementMethod.getInstance());

		doNextWork();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);

		shareMenuItem = menu.findItem(R.id.shareItem);
		shareMenuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener()
		{
			@Override
			public boolean onMenuItemClick(MenuItem item)
			{
				logDebug("shareMenuItem onMenuItemClick");

				Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
				sharingIntent.setType("text/plain");

				String subject;
				String body;
				if (cellCount == null || cellCount.getTodo().longValue() == 0)
				{
					subject = String.format(getString(R.string.share_subject), cellCount.getTodo());
					body = String.format(getString(R.string.share_body), cellCount.getTodo());
				}
				else
				{
					subject = String.format(getString(R.string.share_subject_with_todo), cellCount.getTodo());
					body = String.format(getString(R.string.share_body_with_todo), cellCount.getTodo());
				}

				sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, subject);
				sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, body);
				startActivity(Intent.createChooser(sharingIntent, getString(R.string.share_chooser_title)));
				return true;
			}
		});

		return true;
	}

	/**
	 * @see <a href="http://stackoverflow.com/questions/15762905/how-to-display-list-view-in-alert-dialog-in-android">How to display list view in Alert Dialog in android? [closed]</a>
	 */
	private void popupQuickDialog()
	{
		final AlertDialog.Builder quickInputDialogBuilder = new AlertDialog.Builder(this);
//		quickInputDialogBuilder.setTitle(getString(R.string.quick_input));

		LayoutInflater factory = LayoutInflater.from(this);
		final View quickInputDialogView = factory.inflate(R.layout.dialog_quick_input, null);
		quickInputDialogBuilder.setView(quickInputDialogView);

		ImageView imageView = (ImageView) quickInputDialogView.findViewById(R.id.quickInputDialogDataImageView);
		imageView.setImageBitmap(dataBitmap);

		quickInputDialogBuilder.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				// 如果不做任何事情 就會直接關閉 對話方塊
			}
		});
		final AlertDialog quickInputDialog = quickInputDialogBuilder.create();

		if (quickInputItemArrayAdapter == null)
		{
			quickInputItemArrayAdapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.select_dialog_singlechoice);
			quickInputItemArrayAdapter.add(getString(R.string.quick_input_item_1));
			quickInputItemArrayAdapter.add(getString(R.string.quick_input_item_2));
			quickInputItemArrayAdapter.add(getString(R.string.quick_input_item_3));
			quickInputItemArrayAdapter.add(getString(R.string.quick_input_item_4));
			quickInputItemArrayAdapter.add(getString(R.string.quick_input_item_5));
			quickInputItemArrayAdapter.add(getString(R.string.quick_input_item_6));
			quickInputItemArrayAdapter.add(getString(R.string.quick_input_item_7));
			quickInputItemArrayAdapter.add(getString(R.string.quick_input_item_8));
			quickInputItemArrayAdapter.add(getString(R.string.quick_input_item_9));
			quickInputItemArrayAdapter.add(getString(R.string.quick_input_item_10));
			quickInputItemArrayAdapter.add(getString(R.string.quick_input_item_11));
			quickInputItemArrayAdapter.add(getString(R.string.quick_input_item_12));
			quickInputItemArrayAdapter.add(getString(R.string.quick_input_item_13));
			quickInputItemArrayAdapter.add(getString(R.string.quick_input_item_14));
		}
		ListView itemListView = (ListView) quickInputDialogView.findViewById(R.id.quickInputDialogItemlistView);
		itemListView.setAdapter(quickInputItemArrayAdapter);
		itemListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{
				playCoinSound();

				String quickInputItem = quickInputItemArrayAdapter.getItem(position);
				logDebug("You click " + quickInputItem);

				campaignFinanceController.openThreadToFillCell(cell, quickInputItem);
				doNextWork();
				quickInputDialog.dismiss();
			}
		});

		quickInputDialog.show();
	}

	/**
	 *
	 * @see <a href="http://stackoverflow.com/questions/2403632/android-show-soft-keyboard-automatically-when-focus-is-on-an-edittext">Android: show soft keyboard automatically when focus is on an EditText</a>
	 */
	private void popupInputDialog(String defaultAnswer)
	{
		final AlertDialog.Builder inputDialogBuilder = new AlertDialog.Builder(this);
//		inputDialogBuilder.setTitle(getString(R.string.input));

		LayoutInflater factory = LayoutInflater.from(this);
		final View inputDialogView = factory.inflate(R.layout.dialog_input, null);
		inputDialogBuilder.setView(inputDialogView);
		final EditText editText = (EditText) inputDialogView.findViewById(R.id.inputDialogDataEditText);
		ImageView imageView = (ImageView) inputDialogView.findViewById(R.id.inputDialogDataImageView);
//		imageView.setImageDrawable(dataImage);
		imageView.setImageBitmap(dataBitmap);

		ImageButton clearImageButton = (ImageButton) inputDialogView.findViewById(R.id.clearImageButton);
		clearImageButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				editText.setText("");
			}
		});

		inputDialogBuilder.setPositiveButton(getResources().getString(R.string.send), new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				// Set it later.
			}
		});

		inputDialogBuilder.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				// 如果不做任何事情 就會直接關閉 對話方塊
			}
		});

		final AlertDialog inputDialog = inputDialogBuilder.create();

		/* Force to show keyboard when user open this dialog. */
		editText.setOnFocusChangeListener(new View.OnFocusChangeListener()
		{
			@Override
			public void onFocusChange(View v, boolean hasFocus)
			{
				if (hasFocus)
				{
					inputDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
				}
			}
		});

		editText.setText(defaultAnswer);
		editText.setSelection(defaultAnswer.length());

		inputDialog.setOnShowListener(new DialogInterface.OnShowListener()
		{
			@Override
			public void onShow(DialogInterface dialog)
			{
				Button b = inputDialog.getButton(AlertDialog.BUTTON_POSITIVE);
				b.setOnClickListener(new View.OnClickListener()
				{
					@Override
					public void onClick(View view)
					{
						playCoinSound();

						String inputString = editText.getText().toString().trim();
						if (isOnlyNumber(inputString))
						{
							long inputNumber = Long.valueOf(inputString).longValue();
							inputString = FORMAT_MONEY.format(inputNumber);
						}

						logDebug("You send " + inputString);
						campaignFinanceController.openThreadToFillCell(cell, inputString);
						MainActivity.this.doNextWork();
						inputDialog.dismiss();
					}
				});
			}
		});
		inputDialog.show();
	}

	private boolean isOnlyNumber(String inputStr)
	{
		Matcher matcher = PATTERN_ONLY_NUMBER.matcher(inputStr);
		return matcher.find();
	}

	private void logDebug(String message)
	{
		if (AppConfig.DEBUG)
		{
			Log.d(TAG, message);
		}
	}

	private void doNextWork()
	{
		logDebug("pollCellCount % 10 = " + pollCellCount % 10);
		if (pollCellCount % 10 == 0)
		{
			new GetCellCountTask().execute();
		}
		new PollCellTask().execute();
	}

	private void playCoinSound()
	{
		if (AppConfig.DEBUG)
		{
			playCoinSoundCount += 1;

			if (coinSound == null)
			{
				coinSound = MediaPlayer.create(MainActivity.this, R.raw.smb_coin);
				coinSound.setLooping(false);
			}

			if (oneUpSound == null)
			{
				oneUpSound = MediaPlayer.create(MainActivity.this, R.raw.smb_1_up);
				oneUpSound.setLooping(false);
			}

			if (playCoinSoundCount % 10 == 0)
			{
				oneUpSound.start();
			}
			else
			{
				coinSound.stop();
				try
				{
					coinSound.prepare();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
				coinSound.start();
			}
		}
	}

	private void playKickSound()
	{
		if (AppConfig.DEBUG)
		{
			if (kickSound == null)
			{
				kickSound = MediaPlayer.create(MainActivity.this, R.raw.smb_kick);
				kickSound.setLooping(false);
			}

			kickSound.stop();
			try
			{
				kickSound.prepare();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			kickSound.start();
		}
	}

	private class PollCellTask extends AsyncTask<Void, Void, Void> // Params, Progress, Result
	{
		private boolean isAnyException = false;

		public PollCellTask()
		{
			pollCellCount += 1;
		}

		@Override
		protected void onPreExecute()
		{
			super.onPreExecute();

			dataImageView.setVisibility(View.GONE);
			progressBar.setVisibility(View.VISIBLE);

			currentAnswerTextView.setText(getString(R.string.current_answer));

			setUiEnaled(false);
		}

		@Override
		protected Void doInBackground(Void... params)
		{
			try
			{
				cell = campaignFinanceController.poolCell();
				logDebug("cell=" + cell);
				dataBitmap = null;

				while (cell.getBitmap() == null)
				{
					Thread.sleep(2000);
				}

				dataBitmap = cell.getBitmap();
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
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result)
		{
			// Update UI in UI thread!
			if (isAnyException)
			{
				Toast.makeText(getApplicationContext(), getString(R.string.network_error), Toast.LENGTH_LONG).show();
				//        		Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_LONG).show();

				finish();
			}

			if (dataBitmap != null)
			{
				progressBar.setVisibility(View.GONE);
				dataImageView.setImageBitmap(dataBitmap);
				dataImageView.setVisibility(View.VISIBLE);
			}

			currentAnswerTextView.setText(getString(R.string.current_answer) + cell.getAns());

			setUiEnaled(true);
		}
	}

	private class GetCellCountTask extends AsyncTask<Void, Void, Void> // Params, Progress, Result
	{
		private boolean isAnyException = false;

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
			try
			{
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

		@Override
		protected void onPostExecute(Void result)
		{
			// Update UI in UI thread!
			if (isAnyException)
			{
				Toast.makeText(getApplicationContext(), getString(R.string.network_error), Toast.LENGTH_LONG).show();
				//        		Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_LONG).show();

				finish();
			}

			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
			{
				if (cellCount == null)
				{
					String subtitile = getString(R.string.current_progress) + "? / ?";
					getActionBar().setSubtitle(subtitile);
				}
				else
				{
					String subtitile = getString(R.string.current_progress) + cellCount.getDone() + " / " + cellCount.getCount();
					getActionBar().setSubtitle(subtitile);
				}
			}
		}
	}


	private class PopupInputDialogTask extends AsyncTask<Void, Void, Void> // Params, Progress, Result
	{
		private boolean isAnyException = false;
		private String defaultAnswer;

		public PopupInputDialogTask()
		{

		}

		@Override
		protected void onPreExecute()
		{
			super.onPreExecute();
			setUiEnaled(false);
		}

		@Override
		protected Void doInBackground(Void... params)
		{
			if (cell != null && cell.getAns() != null)
			{
				// This cell has answer so no need to do OCR.
				defaultAnswer = cell.getAns();
			}
			else
			{
				// Try OCR.
				defaultAnswer = ocrController.recognize(cell);
			}

			return null;
		}

		@Override
		protected void onPostExecute(Void result)
		{
			// Update UI in UI thread!
			if (isAnyException)
			{
				Toast.makeText(getApplicationContext(), getString(R.string.network_error), Toast.LENGTH_LONG).show();
				//        		Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_LONG).show();

				finish();
			}

			popupInputDialog(defaultAnswer);
			setUiEnaled(true);
		}
	}

	private void setUiEnaled(boolean enable)
	{
		if (enable)
		{
			if (cell.getAns() != null)
			{
				isRightButton.setEnabled(true);
			}
			isEmptyButton.setEnabled(true);
			quickInputButton.setEnabled(true);
			inputButton.setEnabled(true);
			notClearButton.setEnabled(true);
			ignoreButton.setEnabled(true);

			if (shareMenuItem != null)
			{
				shareMenuItem.setEnabled(true);
			}
		}
		else
		{
			isRightButton.setEnabled(false);
			isEmptyButton.setEnabled(false);
			quickInputButton.setEnabled(false);
			inputButton.setEnabled(false);
			notClearButton.setEnabled(false);
			ignoreButton.setEnabled(false);

			if (shareMenuItem != null)
			{
				shareMenuItem.setEnabled(false);
			}
		}
	}
}
