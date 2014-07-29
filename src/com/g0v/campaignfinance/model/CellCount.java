package com.g0v.campaignfinance.model;

import com.google.gson.annotations.Expose;

import java.io.Serializable;

/**
 * Created by tangblack on 2014/7/25.
 */
public class CellCount implements Serializable
{
	@Expose
	private String count;

	@Expose
	private String todo;

	@Expose(serialize = false, deserialize = false)
	private long done;

	public Long getCount()
	{
		return Long.parseLong(count);
	}

	public Long getTodo()
	{
		return Long.parseLong(todo);
	}

	public Long getDone()
	{
		return new Long(getCount().longValue() - getTodo().longValue());
	}

	@Override
	public String toString()
	{
		return "CellCount{" +
				"count='" + count + '\'' +
				", todo='" + todo + '\'' +
				", done=" + done +
				'}';
	}
}
