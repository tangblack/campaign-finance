package com.g0v.campaignfinance.controller;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.util.Log;
import com.g0v.campaignfinance.AppConfig;
import com.g0v.campaignfinance.model.Cell;
import com.googlecode.leptonica.android.Pix;
import com.googlecode.leptonica.android.ReadFile;
import com.googlecode.leptonica.android.Scale;
import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by tangblack on 2014/7/29.
 * 
 * @see <a href="http://www.cnblogs.com/muyun/archive/2012/06/12/2546693.html">安卓平台tess-two的图片识别终于成功</a>
 * @see <a href="https://github.com/rmtheis/tess-two">rmtheis/tess-two</a>
 */
public class OcrController
{
	private static final String TRAINED_DATA_LANGUAGE = "eng"; // eng or chi_tra
	private static final String TRAINED_DATA_FILE_NAME = TRAINED_DATA_LANGUAGE + ".traineddata";

	private static final String BLACKLIST =
		"abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ〔一—【〈〕ˍ×°í§£ë“ˉ′€”ﬁμ‘!’ˊ〞‧~`!@#$%^&()-_+=;:'<>.|[]{}?\\\"";


	private static final Pattern PATTERN_DATE = Pattern.compile("\\D*(\\d\\d\\d)\\D*(\\d\\d)\\D*(\\d\\d)");
	private static final Pattern PATTERN_MONEY = Pattern.compile("\\D*(\\d+),(\\d+)|\\D*(\\d+)\\.(\\d+)|\\D*(\\d+)"); // \D*(\d+),(\d+)|\D*(\d+)\.(\d+)|\D*(\d+)
	private static final NumberFormat FORMAT_MONEY = new DecimalFormat("#,###,###");


	private TessBaseAPI baseApi;

	private Context context;
	private AssetManager assetManager;
	private String trainedDataFilePath;
	private String tessDataParentPath;


	public OcrController(Context context)
	{
		this.context = context;
		assetManager = context.getAssets();

		logDebug("context=" + context);
		logDebug("context.getExternalFilesDir(null).getAbsolutePath()=" + context.getExternalFilesDir(null).getAbsolutePath());

		trainedDataFilePath = context.getExternalFilesDir(null).getAbsolutePath() + "tesseract/tessdata/" + TRAINED_DATA_FILE_NAME;
		tessDataParentPath = context.getExternalFilesDir(null).getAbsolutePath() + "tesseract";

		if (new File(trainedDataFilePath).exists() == false)
		{
			copyTrainedDataToSdCard();
		}


		baseApi = new TessBaseAPI();
		if (AppConfig.DEBUG)
		{
			baseApi.setDebug(true);
		}
		baseApi.init(new File(tessDataParentPath).getAbsolutePath(), TRAINED_DATA_LANGUAGE, TessBaseAPI.OEM_DEFAULT);
//		baseApi.setVariable(TessBaseAPI.VAR_CHAR_WHITELIST, "0123456789*/,");
		baseApi.setVariable(TessBaseAPI.VAR_CHAR_BLACKLIST, BLACKLIST);
//		baseApi.setPageSegMode(TessBaseAPI.PSM_SINGLE_LINE);
	}

	/**
	 * @see <a href="http://android.9tech.cn/news/2013/1125/38867.html">如何将assets文件复制到SD卡？</a>
	 */
	private void copyTrainedDataToSdCard()
	{
		InputStream in = null;
		OutputStream out = null;
		try
		{
			in = assetManager.open(TRAINED_DATA_FILE_NAME);
			File outFile = new File(trainedDataFilePath);
			outFile.getParentFile().mkdirs();
			out = new FileOutputStream(outFile);
			copyFile(in, out);
			in.close();
			in = null;
			out.flush();
			out.close();
			out = null;
		}
		catch(IOException e)
		{
			Log.e("tag", "Failed to copy asset file: ", e);
		}
	}

	private void copyFile(InputStream in, OutputStream out) throws IOException {
		byte[] buffer = new byte[1024];
		int read;
		while((read = in.read(buffer)) != -1){
			out.write(buffer, 0, read);
		}
	}

	/**
	 *
	 * @param cell
	 * @return
	 * @see <a href="http://wolfpaulus.com/jounal/android-journal/android-and-ocr/">Android and OCR</a>
	 */
	public String recognize(Cell cell)
	{
		Pix pix = processImage(cell.getBitmap());

		try
		{
			baseApi.setImage(pix);

			String recognizedText = baseApi.getUTF8Text();
			logDebug("recognizedText=" + recognizedText);

			//TODO find date or money.
			String dateString = findDate(recognizedText);
			if (dateString != null)
			{
				logDebug("dateString=" + dateString.trim());
				return dateString.trim();
			}

			String moneyString = findMoney(recognizedText);
			if (moneyString != null)
			{
				logDebug("moneyString=" + moneyString.trim());
				return moneyString.trim();
			}

			logDebug("Can't find date or money.");

			//				String processRecognizedText = recognizedText.replaceAll("[^a-zA-Z0-9/*,]+|,{2,}?", "");
			//				logDebug("processRecognizedText=" + processRecognizedText.trim());
			//				return processRecognizedText.trim();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
//			baseApi.end();
		}

		return "";
	}

	private String findDate(String inputStr)
	{
		Matcher matcher = PATTERN_DATE.matcher(inputStr);
		boolean matchFound = matcher.find();

		if (matchFound)
		{
			StringBuffer date = new StringBuffer();
			date.append(matcher.group(1)).append("/").append(matcher.group(2)).append("/").append(matcher.group(3));

			logDebug("[findDate] date=" + date);
			return date.toString();
		}
		else
		{
			return null;
		}
	}

	private String findMoney(String inputStr)
	{
		Matcher matcher = PATTERN_MONEY.matcher(inputStr);
		boolean matchFound = matcher.find();

		if (matchFound)
		{
			StringBuffer money = new StringBuffer();
			for (int i = 1; i <= matcher.groupCount(); i++)
			{
				if (matcher.group(i) != null)
				{
					money.append(matcher.group(i));
				}
			}
			logDebug("[findMoney] money=" + money.toString());
//			Long moneyLong = Long.valueOf(money.toString());
//			logDebug("[findMoney] moneyLong=" + FORMAT_MONEY.format(moneyLong.longValue()));
			return money.toString();
		}
		else
		{
			return null;
		}
	}

	/**
	 *
	 * see http://www.javaworld.com.tw/jute/post/view?bid=20&id=130126&sty=1&tpg=1&age=0
	 */
	private String getMatch(String inputStr, Pattern pattern)
	{
		String result = null;

		Matcher matcher = pattern.matcher(inputStr);
		boolean matchFound = matcher.find();

		if (matchFound)
		{
			result = matcher.group(1);
			logDebug("[getMatch] result=" + result);
			return result;
		}
		else
		{
			return null;
		}
	}

	private Pix processImage(Bitmap bitmap)
	{
		Pix pix = ReadFile.readBitmap(bitmap);
		Pix scalePix = Scale.scale(pix, 8);
		//		Pix enhancePix = Enhance.unsharpMasking(scalePix, 2, 0.5F);

		return scalePix;
	}

	private void logDebug(String message)
	{
		if (AppConfig.DEBUG)
		{
			System.out.println("[OcrController]" + message);
		}
	}
}
