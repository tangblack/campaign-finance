package com.g0v.campaignfinance.model;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;

/**
 * Created by tangblack on 2014/7/25.
 */
public class Cell implements Serializable
{
	@Expose
	private String ans;

	@Expose
	private int count;

	@Expose
	private int id;

	@Expose
	@SerializedName("img_url")
	private String imgUrl;

	@Expose
	private int page;

	@Expose
	private int x;

	@Expose
	private int y;

	@Expose(serialize = false, deserialize = false)
	private Bitmap bitmap;

	public int getY()
	{
		return y;
	}

	public String getAns()
	{
		return ans;
	}

	public int getCount()
	{
		return count;
	}

	public int getId()
	{
		return id;
	}

	public String getImgUrl()
	{
		return imgUrl;
	}

	public int getPage()
	{
		return page;
	}

	public int getX()
	{
		return x;
	}

	/**
	 * 
	 * @return
	 * @see <a href="http://blog.sina.com.cn/s/blog_5a6f39cf0101aqsw.html">Android中Bitmap和Drawable(转)</a>
	 */
	public Bitmap getBitmap()
	{
		if (bitmap == null)
		{
			try
			{
				/* 使用copy的方式，解決 java.lang.IllegalStateException: Immutable bitmap passed to Canvas constructor 的問題 */
				bitmap = BitmapFactory.decodeStream((InputStream) new URL(imgUrl).getContent()).copy(Bitmap.Config.ARGB_8888, true);
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}

		return bitmap;
	}

	@Override
	public String toString()
	{
		return "Cell{" +
				"ans='" + ans + '\'' +
				", count=" + count +
				", id=" + id +
				", imgUrl='" + imgUrl + '\'' +
				", page=" + page +
				", x=" + x +
				", y=" + y +
				'}';
	}
}
