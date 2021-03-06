package com.g0v.campaignfinance.controller;

import com.g0v.campaignfinance.AppConfig;
import com.g0v.campaignfinance.api.CampaignFinanceApi;
import com.g0v.campaignfinance.model.Cell;
import com.g0v.campaignfinance.model.CellCount;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Created by tangblack on 2014/7/25.
 */
public class CampaignFinanceController
{
	public static CampaignFinanceController sInstance;

	private CampaignFinanceApi api;
	private Queue<Cell> cellList;
	private CellCount cellCount;


	public static CampaignFinanceController getInstance() throws IOException
	{
		if (sInstance != null)
		{
			return sInstance;
		}
		else
		{
			return new CampaignFinanceController();
		}
	}

	private CampaignFinanceController() throws IOException
	{
		api = new CampaignFinanceApi();
		initCellList();

		sInstance = this;
	}

	private void initCellList() throws IOException
	{
		cellList = new LinkedList<Cell>();
		openThreadToAddCellIntoListFromApi(30);
	}

	/**
	 * 
	 * @return
	 * @throws IOException
	 * @see <a href="http://stackoverflow.com/questions/10213707/fifo-based-queue-implementations">FIFO based Queue implementations?</a>
	 */
	public Cell poolCell() throws IOException
	{
		openThreadToAddCellIntoListFromApi(20);

		if (cellList.size() == 0)
		{
			addCellIntoListFromApi(30);
		}

		Cell cell = cellList.poll();
		return cell;
	}

	private void openThreadToAddCellIntoListFromApi(final int expectCellListSize)
	{
		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					addCellIntoListFromApi(expectCellListSize);
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}).start();
	}

	private void addCellIntoListFromApi(int expectCellListSize) throws IOException
	{
		logDebug("addCellIntoListFromApi expectCellListSize=" + expectCellListSize  + " cellList.size()=" + cellList.size());

		while (cellList.size() < expectCellListSize)
		{
			List<Cell> cellListFromApi = api.getRandoms(); // Return 10 cells.
			openThreadToPreloadCellImages(cellListFromApi);

			cellList.addAll(cellListFromApi);
		}

		logDebug("addCellIntoListFromApi cellList.size()=" + cellList.size());
	}

	private synchronized void openThreadToPreloadCellImages(final List<Cell> inCellList)
	{
		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				for (int i = 0; i < inCellList.size(); i++)
				{
					Cell cell = inCellList.get(i);
					cell.getBitmap();
				}
			}
		}).start();
	}

	public CellCount getCellCount() throws IOException
	{
		cellCount = api.getCellCount();
		return cellCount;
	}

	public void openThreadToFillCell(final Cell cell, final String answer)
	{
		if (canCallApi() == false)
		{
			return;
		}

		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					api.fillCell(cell, answer);
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}).start();
	}

	public void openThreadToFillCellIsEmpty(final Cell cell)
	{
		if (canCallApi() == false)
		{
			return;
		}

		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					api.fillCellIsEmpty(cell);
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}).start();
	}

	public void openThreadToFillCellIsRight(final Cell cell)
	{
		if (canCallApi() == false)
		{
			return;
		}

		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					api.fillCellIsRight(cell);
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}).start();
	}

	public void openThreadToFillCellNotClear(final Cell cell)
	{
		if (canCallApi() == false)
		{
			return;
		}

		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					api.reportUnclear(cell);
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}).start();
	}

	private boolean canCallApi()
	{
		if (cellCount == null || cellCount.getTodo() == 0)
		{
			return false;
		}
		else
		{
			return true;
		}
	}

	private void logDebug(String message)
	{
		if (AppConfig.DEBUG)
		{
			System.out.println("[CampaignFinanceController]" + message);
		}
	}

}
