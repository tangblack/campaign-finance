package com.g0v.campaignfinance.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.g0v.campaignfinance.AppConfig;
import com.g0v.campaignfinance.model.Cell;
import com.g0v.campaignfinance.model.CellCount;
import org.apache.commons.lang3.StringEscapeUtils;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by tangblack on 2014/7/25.
 */
public class CampaignFinanceApi
{
	private static final String API_URL = "http://campaign-finance.g0v.ctiml.tw/api/";

	private static final String FORM_KEY_ANSWER = "ans";
	private static final String FORM_KEY_S_TOKEN = "sToken";
	private static final Pattern PATTERN_HOST = Pattern.compile("http://([\\w].+?)/");
	private Gson gson  = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
	private Type cellListType = new TypeToken<List<Cell>>(){}.getType();


	public CellCount getCellCount() throws IOException
	{
		String response = jsoupGet(API_URL + "getcellcount");

		return gson.fromJson(response, CellCount.class);
	}

	public List<Cell> getRandoms() throws IOException
	{
		String response = jsoupGet(API_URL + "getrandoms");

		return gson.fromJson(response, cellListType);
	}


	/**
	 * Original answer is right > input the same answer again.
	 *
	 * @param cell
	 * @return
	 * @throws IOException
	 */
	public boolean fillCellIsRight(Cell cell) throws IOException
	{
		if (cell.getAns() == null)
		{
			// Do nothing.
		}

		return fillCell(cell, cell.getAns());
	}

	/**
	 * The answer is empty > input empty string.
	 *
	 * @param cell
	 * @return
	 * @throws IOException
	 */
	public boolean fillCellIsEmpty(Cell cell) throws IOException
	{
		return fillCell(cell, "");
	}

	/**
	 * Can't recognize the answer > don't input the answer.
	 *
	 * @param cell
	 * @return
	 * @throws IOException
	 */
	public boolean fillCellNotClear(Cell cell) throws IOException
	{
		String url = API_URL + "fillcell/" + cell.getPage() + "/" + cell.getX() + "/" + cell.getY();

		return jsoupPost(url);
	}

	/**
	 * 
	 * @param cell
	 * @return
	 * @see <a href="http://www.androidsnippets.com/executing-a-http-post-request-with-httpclient">Executing a HTTP POST Request with HttpClient</a>
	 */
	public boolean fillCell(Cell cell, String answer) throws IOException
	{
		String url = API_URL + "fillcell/" + cell.getPage() + "/" + cell.getX() + "/" + cell.getY();

		return jsoupPost(url, FORM_KEY_ANSWER, answer);
	}


	private boolean jsoupPost(String url, String... postData) throws IOException
	{
		logDebug("url=" + url);
		if (postData.length >= 2)
		{
			logDebug("postData=" + postData[0] + "," + postData[1]);
		}

		Connection.Response response = Jsoup.connect(url)
				.header("Host", matchHost(url))
				.header("Connection", "keep-alive")
				.header("Cache-Control", "max-age=0")
				.header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
				.userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/30.0.1599.101 Safari/537.36")
				.header("Accept-Encoding", "gzip,deflate,sdch")
				.header("Accept-Language", "zh-TW,zh;q=0.8,en-US;q=0.6,en;q=0.4")
				.timeout(60000)
				.data(postData)
				.method(Connection.Method.POST)
				.execute();

//		Document doc = response.parse();
//		logDebug("response.cookies()=" + response.cookies());
//		logDebug("response.HISOKU=" + response.cookie("HISOKU"));
//		logDebug("response.sToken=" + response.cookie("sToken"));
//		logDebug("response.headers=" + response.headers());
		logDebug("response.statusCode=" + response.statusCode());

		if (response.statusCode() == 200)
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	private String jsoupGet(String url) throws IOException
	{
		logDebug("url=" + url);

		Connection.Response response = Jsoup.connect(url)
				.ignoreContentType(true)
				.header("Host", matchHost(url))
				.header("Connection", "keep-alive")
				.header("Cache-Control", "max-age=0")
				.header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
				.userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/30.0.1599.101 Safari/537.36")
				.header("Accept-Encoding", "gzip,deflate,sdch")
				.header("Accept-Language", "zh-TW,zh;q=0.8,en-US;q=0.6,en;q=0.4")
				.timeout(60000)
				.method(Connection.Method.GET)
				.execute();

//		logDebug("response.cookies()=" + response.cookies());
//		logDebug("response.HISOKU=" + response.cookie("HISOKU"));
//		logDebug("response.sToken=" + response.cookie("sToken"));
//		logDebug("response.headers=" + response.headers());

		Document doc = response.parse();
		String body = StringEscapeUtils.unescapeXml(doc.body().html());
		logDebug("body=" + body);

		return body;
	}

	private void logDebug(String message)
	{
		if (AppConfig.DEBUG)
		{
			System.out.println("[CampaignFinanceApi]" + message);
		}
	}

	private String matchHost(String inputStr)
	{
		return getMatch(inputStr, PATTERN_HOST);
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

	public static void main(String[] args) throws IOException
	{
		CampaignFinanceApi api = new CampaignFinanceApi();
		CellCount cellCount = api.getCellCount();
		api.logDebug("cellCount count=" + cellCount.getCount().longValue());
		api.logDebug("cellCount todo=" + cellCount.getTodo().longValue());
		api.logDebug("cellCount done=" + cellCount.getDone().longValue());


		List<Cell> cellList = api.getRandoms();
		for (int i=0; i<cellList.size(); i++)
		{
			api.logDebug(cellList.get(i).toString());
		}


		Cell cell = cellList.get(0);
		api.logDebug("Test fill cell " + cell);
		api.fillCell(cell, cell.getAns());
	}
}
