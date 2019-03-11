package com.pmzhongguo.wallet;

import java.math.BigDecimal;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSONObject;
import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.HttpMethod;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.WebResponse;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.nitinsurana.bitcoinlitecoin.rpcconnector.APICalls;
import com.nitinsurana.bitcoinlitecoin.rpcconnector.JSONRequestBody;
import com.nitinsurana.bitcoinlitecoin.rpcconnector.exception.AuthenticationException;
import com.nitinsurana.bitcoinlitecoin.rpcconnector.exception.CallApiCryptoCurrencyRpcException;
import com.nitinsurana.bitcoinlitecoin.rpcconnector.exception.CryptoCurrencyRpcException;
import com.nitinsurana.bitcoinlitecoin.rpcconnector.exception.CryptoCurrencyRpcExceptionHandler;
import com.nitinsurana.bitcoinlitecoin.rpcconnector.pojo.Transaction;
import com.pmzhongguo.wallet.helper.DBDo;

public class FNRPC {

	public static final Logger LOG = Logger.getLogger("rpcLogger");

	private WebClient client;
	private String baseUrl;
	private CryptoCurrencyRpcExceptionHandler cryptoCurrencyRpcExceptionHandler = new CryptoCurrencyRpcExceptionHandler();
	private Gson gson = new Gson();

	public FNRPC(String rpcHost, String rpcPort) throws AuthenticationException {
		// 构造一个webClient 模拟Chrome浏览器
		client = new WebClient(BrowserVersion.CHROME);
		client.getOptions().setThrowExceptionOnFailingStatusCode(false);
		client.getOptions().setThrowExceptionOnScriptError(false);
		client.getOptions().setPrintContentOnFailingStatusCode(false);
		client.getOptions().setJavaScriptEnabled(false);
		client.getOptions().setCssEnabled(false);
		// 使用url作为参数创建新对象
		baseUrl = new String("http://" + rpcHost + ":" + rpcPort + "/");

		try {
			if (client.getPage(baseUrl).getWebResponse().getStatusCode() == 401) {  //401 is Http Unauthorized
				throw new AuthenticationException();
			}
		} catch (Exception ex) {
			LOG.error(ex.getMessage(), ex);
		}
	}

	public String GetBlock(long height) throws CryptoCurrencyRpcException {
		// 请求json接口对象,参考接口
		JSONObject jobj = new JSONObject();
		jobj.put("height", height);
		// 调用
		JsonObject jsonObj = callFNAPIMethod(APICalls.FN_GET_BLOCK, jobj);
		cryptoCurrencyRpcExceptionHandler.checkException(jsonObj);

		// result对象,jsonObj.get("result");
		JsonObject jsonResult = jsonObj.get("result").getAsJsonObject();
		// block对象
		JsonObject jsonStr = jsonResult.get("block").getAsJsonObject();
		// tcount值
		JsonElement confirmations = jsonStr.get("tcount");
		JsonElement txid = jsonStr.get("transactions");
        String txidStr = txid.toString();
        String txidTo= txidStr.replace("\"", "");

        JsonElement address = null;
		JsonElement amount = null;
		JsonElement timestamp = null;
        String addressStr = null;

		// trans数组
		JsonArray transJson = jsonResult.getAsJsonArray("trans");
		for(int i=0, len=transJson.size(); i<len; i++) {
			// trans里面的数组对象
			JsonObject str = (JsonObject) transJson.getAsJsonArray().get(i);
			address = str.get("to");
            addressStr = address.toString().replace("\"", "");
            amount = str.get("value");
			timestamp = str.get("timestamp");
		}
		
		// 转换
		BigDecimal amountNum = new BigDecimal(amount.toString()).divide(new BigDecimal(1000000000));
		long lconfirmations = Long.parseLong(confirmations.toString());
		System.out.println("FN" + addressStr + amountNum + txidTo + lconfirmations);

		DBDo.addCoin("FN", addressStr, amountNum, txidTo, TimeStamp2Date( timestamp+""), lconfirmations);
		return "success";
	}

    public static String TimeStamp2Date(String timestampString) {
    	String formats = "yyyy-MM-dd HH:mm:ss";
        Long timestamp = Long.parseLong(timestampString) * 1000;
        String date = new SimpleDateFormat(formats, Locale.CHINA).format(new Date(timestamp));
        return date;
    }
    
	private JsonObject callFNAPIMethod(APICalls callMethod, Object param) throws CallApiCryptoCurrencyRpcException {
		try {
			JsonObject jsonObj = null;
			WebRequest req = new WebRequest(new URL(baseUrl));
			req.setAdditionalHeader("Content-type", "application/json");
			req.setHttpMethod(HttpMethod.POST);
			JSONObject body = new JSONObject();
			body.put("method", callMethod.toString());
			if (param != null ) {
				body.put("params", param);
			}
			String bodystr = body.toJSONString();
			req.setRequestBody(bodystr);
			WebResponse resp = client.getPage(req).getWebResponse();
			jsonObj = new JsonParser().parse(resp.getContentAsString()).getAsJsonObject();

			StringBuffer buffer = new StringBuffer("");
			if (param != null) {
				buffer.append(param.toString() + " | ");
			}
			LOG.info("FN RPC Request: Method: " + callMethod + " Params: " + buffer.toString() +
					"\nFN RPC Response : " + jsonObj);

			return jsonObj;
		} catch (Exception e) {
			throw new CallApiCryptoCurrencyRpcException(e.getMessage());
		}
	}
}

