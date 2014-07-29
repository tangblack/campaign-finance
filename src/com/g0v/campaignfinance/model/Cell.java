package com.g0v.campaignfinance.model;

import android.graphics.drawable.Drawable;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

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
	private Drawable imgDrawable;

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

	public Drawable getImgDrawable()
	{
		return imgDrawable;
	}

	public void setImgDrawable(Drawable imgDrawable)
	{
		this.imgDrawable = imgDrawable;
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
