package com.pmzhongguo.wallet.helper;

/**
 * 说明：文本及日期处理方法
 * 编写者：edward
 * 日期：Aug 8, 2007
 * KevinChow版权
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;


public class HelpUtils {


	public static Map newHashMap(Object... args) {
		return toMap(args);
	}

	public static Map toMap(Object[] args) {
		Map map = new HashMap();
		for (int i = 1; i < args.length; i += 2) {
			map.put(args[i - 1], args[i]);
		}
		return map;
	}


	/**
	 * Intelligently chops a String at a word boundary (whitespace) that occurs
	 * at the specified index in the argument or before. However, if there is a
	 * newline character before <code>length</code>, the String will be chopped
	 * there. If no newline or whitespace is found in <code>string</code> up to
	 * the index <code>length</code>, the String will chopped at
	 * <code>length</code>.
	 * <p>
	 * For example, chopAtWord("This is a nice String", 10) will return "This is
	 * a" which is the first word boundary less than or equal to 10 characters
	 * into the original String.
	 * 
	 * @param string
	 *            the String to chop.
	 * @param length
	 *            the index in <code>string</code> to start looking for a
	 *            whitespace boundary at.
	 * @return a substring of <code>string</code> whose length is less than or
	 *         equal to <code>length</code>, and that is chopped at whitespace.
	 */
	public static final String chopAtWord(String string, int length) {
		if (string == null) {
			return string;
		}

		char[] charArray = string.toCharArray();
		int sLength = string.length();
		if (length < sLength) {
			sLength = length;
		}

		// First check if there is a newline character before length; if so,
		// chop word there.
		for (int i = 0; i < sLength - 1; i++) {
			// Windows
			if (charArray[i] == '\r' && charArray[i + 1] == '\n') {
				return string.substring(0, i + 1);
			}
			// Unix
			else if (charArray[i] == '\n') {
				return string.substring(0, i);
			}
		}
		// Also check boundary case of Unix newline
		if (charArray[sLength - 1] == '\n') {
			return string.substring(0, sLength - 1);
		}

		// Done checking for newline, now see if the total string is less than
		// the specified chop point.
		if (string.length() < length) {
			return string;
		}

		// No newline, so chop at the first whitespace.
		for (int i = length - 1; i > 0; i--) {
			if (charArray[i] == ' ') {
				return string.substring(0, i).trim();
			}
		}

		// Did not find word boundary so return original String chopped at
		// specified length.
		return string.substring(0, length);
	}

	private static final char[] zeroArray = "0000000000000000".toCharArray();

	/**
	 * 指定字符串长度，不足的加0
	 * 
	 * @param string
	 * @param length
	 * @return String
	 */
	public static final String zeroPadString(String string, int length) {
		if (string == null || string.length() > length) {
			return string;
		}
		StringBuffer buf = new StringBuffer(length);
		buf.append(zeroArray, 0, length - string.length()).append(string);
		return buf.toString();
	}

	/**
	 * 将日期转换成毫秒，以15位记录，不足的补0
	 * 
	 * @param date
	 * @return String
	 */
	public static final String dateToMillis(Date date) {
		return zeroPadString(Long.toString(date.getTime()), 15);
	}



	/**
	 * 格式化日期yyyy-MM-dd
	 * 
	 * @param date
	 * @return String
	 */
	public static String formatDate(Date date) {
		return formatDateByFormatStr(date, "yyyy-MM-dd");
	}

	/**
	 * 格式化日期MM-dd HH:mm
	 * 
	 * @param myDate
	 * @return String
	 */
	public static String formatDate3(Date myDate) {
		return formatDateByFormatStr(myDate, "MM-dd HH:mm");
	}

	/**
	 * 格式化日期HH:mm
	 * 
	 * @param myDate
	 * @return String
	 */
	public static String formatDate10(Date myDate) {
		return formatDateByFormatStr(myDate, "HH:mm");
	}

	/**
	 * 格式化日期yyyyMMdd
	 * 
	 * @param myDate
	 * @return String
	 */
	public static String formatDate4(Date myDate) {
		return formatDateByFormatStr(myDate, "yyyyMMdd");
	}

	/**
	 * 格式化日期时间yyyy-MM-dd HH:mm
	 * 
	 * @param myDate
	 * @return String
	 */
	public static String formatDate6(Date myDate) {
		return formatDateByFormatStr(myDate, "yyyy-MM-dd HH:mm");
	}



	/**
	 * 格式化日期yyyy-MM-dd HH:mm:ss
	 * 
	 * @param myDate
	 * @return
	 */
	public static String formatDate8(Date myDate) {
		return formatDateByFormatStr(myDate, "yyyy-MM-dd HH:mm:ss");
	}

	public static String formatDateOrdreNo(Date myDate) {
		return formatDateByFormatStr(myDate, "yyMMddHHmmss");
	}


	public static String formatDateByFormatStr(Object myDate, String formatStr) {
		if (myDate == null)
			return "";
		SimpleDateFormat formatter = new SimpleDateFormat(formatStr);
		return formatter.format(myDate);
	}


	/**
	 * 将年月日转换成long
	 * 
	 * @param year
	 * @param month
	 * @param date
	 * @return String
	 */
	public static long Date2Long(int year, int month, int date) {
		Calendar cld = Calendar.getInstance();
		month = month - 1;
		cld.set(year, month, date);
		return cld.getTime().getTime();
	}

	/**
	 * 将年月日时分秒转换成long
	 * 
	 * @param year
	 * @param month
	 * @param date
	 * @param hour
	 * @param minute
	 * @param second
	 * @return long
	 */
	public static long Time2Long(int year, int month, int date, int hour,
			int minute, int second) {
		Calendar cld = Calendar.getInstance();
		month = month - 1;
		cld.set(year, month, date, hour, minute, second);
		return cld.getTime().getTime();
	}


	/**
	 * 从Date中获得秒
	 * 
	 * @param date
	 * @return int
	 */
	public static int getSecond(Date date) {
		Calendar cld = Calendar.getInstance();
		cld.setTime(date);
		return cld.get(Calendar.SECOND);
	}

	/**
	 * 获得当前年份
	 * 
	 * @return int
	 */
	public static int getYear() {
		Calendar cld = Calendar.getInstance();
		cld.setTime(new java.util.Date());
		return cld.get(Calendar.YEAR);
	}

	/**
	 * 获得当前月份
	 * 
	 * @return int
	 */
	public static int getMonth() {
		Calendar cld = Calendar.getInstance();
		cld.setTime(new java.util.Date());
		return cld.get(Calendar.MONTH) + 1;
	}

	/**
	 * 获得当前日期
	 * 
	 * @return int
	 */
	public static int getDay() {
		Calendar cld = Calendar.getInstance();
		cld.setTime(new java.util.Date());
		return cld.get(Calendar.DAY_OF_MONTH);
	}



	public static boolean nullOrBlank(Object param) {
		return (param == null || param.toString().length() == 0
				|| param.toString().trim().equals("")
				|| param.toString().trim().equalsIgnoreCase("null") || param
				.toString().trim().equals("undefined")) ? true : false;
	}

	public static String clearCheckBoxVal(String sourceStr) {
		String newStr = sourceStr;

		if (!HelpUtils.nullOrBlank(newStr)) {
			while (newStr.indexOf(",,") >= 0) {
				newStr = newStr.replaceAll(",,", ",");
			}
			if (newStr.endsWith(",")) {
				newStr = newStr.substring(0, newStr.length() - 1);
			}
		} else {
			newStr = "";
		}

		return newStr;
	}

	/**
	 * dateToString(Date inDate) 把日期型转换成字符型"yyyy-MM-dd HH:mm:ss"
	 * 
	 * @param inDate
	 *            Date
	 * @return String
	 */
	public static String dateToString(Date inDate) {
		String outDateStr = "";
		if (inDate != null) {
			SimpleDateFormat formatter = new SimpleDateFormat(
					"yyyy-MM-dd HH:mm:ss");
			outDateStr = formatter.format(inDate);
		}
		return outDateStr;
	}


	public static String TimeStamp2Date(String timestampString, String formats) {
		if (null == formats) {
			formats = "yyyy-MM-dd HH:mm:ss";
		}
		Long timestamp = Long.parseLong(timestampString);
		String date = new SimpleDateFormat(formats, Locale.CHINA)
				.format(new Date(timestamp));
		return date;
	}

	
	/**
	 * 取得当前时间戳（精确到毫秒）
	 * 
	 * @return nowTimeStamp
	 */
	public static long getNowTimeStampMillisecond() {
		return System.currentTimeMillis();
	}
	
	public static String loadJSON(String url) {
		StringBuilder json = new StringBuilder();
		try {
			URL oracle = new URL(url);
			URLConnection yc = oracle.openConnection();
			BufferedReader in = new BufferedReader(new InputStreamReader(
					yc.getInputStream(), "utf-8"));
			yc.setConnectTimeout(30000);  
			yc.setReadTimeout(30000);

			String inputLine = null;
			while ((inputLine = in.readLine()) != null) {
				json.append(inputLine);
			}
			in.close();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return json.toString();
	}
	
    /** 
     * 获取网页html 
     */  
	public static String loadJSONHttps(String currentUrl) {
		HttpClient httpClient = new DefaultHttpClient();
		httpClient = HttpsClient.getNewHttpsClient(httpClient);
		String html = "";
		HttpGet request = new HttpGet(currentUrl);
		HttpResponse response = null;
		try {
			response = httpClient.execute(request);
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				HttpEntity mEntity = response.getEntity();
				html = EntityUtils.toString(mEntity);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return html.toString();
	}

	public static void main(String[] args) throws Exception {
		
	}
}
