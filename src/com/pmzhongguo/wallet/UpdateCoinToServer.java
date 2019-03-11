package com.pmzhongguo.wallet;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.json.JSONArray;
import org.json.JSONObject;

import com.nitinsurana.bitcoinlitecoin.rpcconnector.CryptoCurrencyRPC;
import com.nitinsurana.bitcoinlitecoin.rpcconnector.pojo.Transaction;
import com.pmzhongguo.wallet.helper.DBDo;
import com.pmzhongguo.wallet.helper.HelpUtils;
import com.pmzhongguo.wallet.helper.HttpRequestUtils;
import com.pmzhongguo.wallet.helper.MacMD5;
import com.pmzhongguo.wallet.helper.UpdateETH;

/**
 * @author zhuzhisheng
 * @Description
 * @date on 2016/12/31.
 */
public class UpdateCoinToServer {
    public final static String rpcUser = "xjjd";        // 钱包RPC用户名
    public final static String rpcPassword = "xjjd.io"; // 钱包RPC密码
    public final static String btc_rpcHost = "127.0.0.1";    // 比特币钱包所在机器
    public final static String ltc_rpcHost = "127.0.0.1";    // 莱特币钱包所在机器
    public final static String rpcUSDTHost = "127.0.0.1";    // 比特币版USDT钱包所在机器
    public final static String btc_rpcPort = "18336";     // 比特币RPC端口
    public final static String ltc_rpcPort = "18335";     // 莱特币RPC端口
    public final static String usdt_rpcPort = "18336";	// 比特币版USDT的RPC端口
    public final static String eth_rpcHost = "http://172.21.218.169:45455";    // 以太坊钱包URL
    public final static String serverUrl = "http://123.207.124.48"; // 交易平台后端的URL
    public final static String usdt_btcSXFAddr = "1Ni84yNXVJjWi5RCLusmU96Ce9dnsumDSY"; //USDT_BTC 手续费地址
    
    // FN钱包
    public final static String fn_rpcHost = "47.74.210.15";
    public final static String fn_rpcPort = "50051";
    
    //BTC全部地址
    private static Map btcAddrMap = null;
    
	public static void main(String[] args) throws Exception {
		update();
		
		UpdateETH.update();
	}

	
	public static void update() {
		btcAddrMap = DBDo.getAllBtcAddr();
        Integer cacheTime = 1000 * 10 * 1;   //1秒 * 60秒 * 30分
        Timer timer = new Timer();  
        // (TimerTask task, long delay, long period)任务，延迟时间，多久执行  
        timer.schedule(new TimerTask() {  
            @Override  
            public void run() {  
            	// 循环处理
            	upload();
            	
            	System.out.println(HelpUtils.formatDate8(new Date()) + ":btc");
            	btcProcess();  
            	upload();
            	
            	System.out.println(HelpUtils.formatDate8(new Date()) + ":ltc");
            	ltcProcess();
            	upload();
            	
            	System.out.println(HelpUtils.formatDate8(new Date()) + ":fn");
            	fnProcess();
            	upload();
            	
            	System.out.println(HelpUtils.formatDate8(new Date()) + ":btc_usdt");
            	usdtProcess();
            	upload();
            }  
        }, 1000, cacheTime);    
	}
	
	// FN
	public final static void fnProcess() {
		try {
			FNRPC fnRpc = new FNRPC(fn_rpcHost, fn_rpcPort);
			fnRpc.GetBlock(415);
		} catch (Throwable throwable) {
			throwable.printStackTrace();
		}
	}


	/**
	 * 启动方法C:\Program Files\Bitcoin\daemon，  bitcoind.exe
	 */
	public final static void btcProcess() {
		try {
			final CryptoCurrencyRPC bitcoinRPC = new CryptoCurrencyRPC(rpcUser, rpcPassword, btc_rpcHost, btc_rpcPort);
		    List<Transaction>  btcTranLst = bitcoinRPC.listTransactions("*", 100, 0);
		    int iStart = 0;
		    
		    for(int i = iStart; i < btcTranLst.size(); i ++) {
		    	if (!"receive".equalsIgnoreCase(btcTranLst.get(i).getCategory().toString())) {
		    		continue;
		    	}
		    	//如果接受地址是手续费地址则跳过此次循环
		    	if(isSXFAddress(btcTranLst.get(i).getAddress())) {
		    		continue;
		    	}
		    	
		    	if (btcTranLst.get(i).getConfirmations() < 2) {
		    		continue;
		    	}
		    	
		    	if (isExistsTxID(btcTranLst.get(i).getTxid(), btcTranLst.get(i).getAddress(), "BTC")){
		    		continue;
		    	}
		    	
		    	DBDo.addCoin("BTC", btcTranLst.get(i).getAddress(), btcTranLst.get(i).getAmount(), btcTranLst.get(i).getTxid(), TimeStamp2Date(btcTranLst.get(i).getTimereceived() + ""), btcTranLst.get(i).getConfirmations());
		    }
		} catch (Throwable throwable) {
			throwable.printStackTrace();
		}
	}
	
	private static boolean isExistsTxID(String txid, String address, String currency) {
		Integer txCount = DBDo.isExistsTxID(txid, address, currency);
		return txCount > 0;
	}


	public final static void ltcProcess() {
		try {
			final CryptoCurrencyRPC bitcoinRPC = new CryptoCurrencyRPC(rpcUser, rpcPassword, ltc_rpcHost, ltc_rpcPort);
		    List<Transaction>  btcTranLst = bitcoinRPC.listTransactions("*", 100, 0);
		    int iStart = 0;
		    
		    
		    for(int i = iStart; i < btcTranLst.size(); i ++) {
		    	if (!"receive".equalsIgnoreCase(btcTranLst.get(i).getCategory().toString())) {
		    		continue;
		    	}
		    	
		    	if (btcTranLst.get(i).getConfirmations() < 2) {
		    		continue;
		    	}
		    	
		    	if (isExistsTxID(btcTranLst.get(i).getTxid(), btcTranLst.get(i).getAddress(), "LTC")){
		    		continue;
		    	}
		    	
		    	DBDo.addCoin("LTC", btcTranLst.get(i).getAddress(), btcTranLst.get(i).getAmount(), btcTranLst.get(i).getTxid(), TimeStamp2Date(btcTranLst.get(i).getTimereceived() + ""), btcTranLst.get(i).getConfirmations());
		    }
		} catch (Throwable throwable) {
			throwable.printStackTrace();
		}
	}
	
	
	//https://data.ripple.com/v2/accounts/瑞波地址/transactions?type=Payment&result=tesSUCCESS&limit=100&descending=true
	public final static void rippleProcess() {
		try {
			String blocks = HelpUtils.loadJSONHttps("https://data.ripple.com/v2/accounts/瑞波地址/transactions?type=Payment&result=tesSUCCESS&limit=10&descending=true");
			JSONObject blocksJson = new JSONObject(blocks);
			JSONArray blocksArr = blocksJson.getJSONArray("transactions");
			for (int i = 0; i < blocksArr.length(); i++) {
				JSONObject block = blocksArr.getJSONObject(i);
				JSONObject tx = block.getJSONObject("tx");
				
				// 没有Tag的全部不要
				if (tx.has("DestinationTag") && "瑞波地址".equals(tx.get("Destination"))) {
					if (isExistsTxID(block.getString("hash"), tx.get("Destination") + "-" + tx.get("DestinationTag"), "XRP")){
						
						// 已经存在这个txid，直接退出循环，因为是按时间排序的
			    		break;
			    	}
					DBDo.addCoin("XRP", tx.get("Destination") + "-" + (tx.get("DestinationTag") + "").trim(), BigDecimal.valueOf((tx.getLong("Amount") * 1.0) / 1000000), block.getString("hash"), (block.getString("date")).substring(0, 19).replace("T", " "), 1);
				}
				
				// 没有Tag，直接下一个循环
			}
		}catch (Exception e) {
			System.err.println("Ripple 错误:" + e.toString());
		}

	}
	
	public final static void usdtProcess() {
		try {
			final CryptoCurrencyRPC bitcoinRPC = new CryptoCurrencyRPC(rpcUser, rpcPassword, rpcUSDTHost, usdt_rpcPort);
		    List<Transaction>  btcTranLst = bitcoinRPC.listOmniTransactions("*", 10000, 0);
		    int iStart = 0;
		    
		    for (int i = iStart; i < btcTranLst.size(); i++) {
		    	
		    	if (btcTranLst.get(i).getPropertyid() != 31) {
		    		continue;
		    	}
		    	if (!btcTranLst.get(i).isValid()) {
		    		continue;
		    	}
		    	//查看改地址是否存在数据库.
		    	boolean existsAddress = isExistsAddress(btcTranLst.get(i).getReferenceaddress());
		    	if(!existsAddress) {
		    		continue;
				}
		    	//如果接受地址是手续费地址则跳过此次循环
		    	if(isSXFAddress(btcTranLst.get(i).getReferenceaddress())) {
		    		continue;
		    	}
				if (btcTranLst.get(i).getConfirmations() < 2) {
					continue;
				}

				if (isExistsTxID(btcTranLst.get(i).getTxid(), btcTranLst.get(i).getReferenceaddress(), "USDT")) {
					continue;
				}
				
				DBDo.addCoin("USDT", btcTranLst.get(i).getReferenceaddress(), btcTranLst.get(i).getAmount(),
						btcTranLst.get(i).getTxid(),
						TimeStamp2Date(btcTranLst.get(i).getBlocktime() + ""),
						btcTranLst.get(i).getConfirmations());
			}
		} catch (Throwable throwable) {
			throwable.printStackTrace();
		}
	}
	
	public final static void upload() {
		try {
			
			// 上传充值数据
			List<Map<String, Object>> uploadLst = DBDo.getWaitingCoin();
			
			for(int i = 0; i < uploadLst.size(); i++) {
				Map<String, Object> map = uploadLst.get(i);
				map.put("api_key", "private");
				map.put("sign_type", "MD5");
				map.put("timestamp", getNowTimeStamp());
				String sign = createSign(map, "A819DCB528FABBC2D24FE36A5939412C");
				map.put("sign", sign);
				
				net.sf.json.JSONObject jsonObject = net.sf.json.JSONObject.fromObject(map);  
				net.sf.json.JSONObject ret = HttpRequestUtils.httpPost(serverUrl + "/a/coinrecharge/create", jsonObject);
				if (ret.get("state").toString().equals("1")) {
					DBDo.updateCoin(Integer.parseInt(map.get("id") + ""));
					System.out.println(TimeStamp2Date(getNowTimeStamp()) + "-------同步充值成功");
				} else {
					System.out.println(TimeStamp2Date(getNowTimeStamp()) + "-------同步充值失败： 地址：" + map.get("r_address") + "  错误信息：" + ret);
				}
			}
			
			/*
			// 上传充值地址
			List<Map<String, Object>> uploadAddressLst = DBDo.getWaitingAddress();
			
			for(int i = 0; i < uploadAddressLst.size(); i++) {
				Map<String, Object> map = uploadAddressLst.get(i);
				map.put("api_key", "private");
				map.put("sign_type", "MD5");
				map.put("timestamp", getNowTimeStamp());
				String sign = createSign(map, "A819DCB528FABBC2D24FE36A5939412C");
				map.put("sign", sign);
				
				net.sf.json.JSONObject jsonObject = net.sf.json.JSONObject.fromObject(map);  
				net.sf.json.JSONObject ret = HttpRequestUtils.httpPost(serverUrl + "/a/coinaddress/create", jsonObject);
				if (ret.get("state").toString().equals("1")) {
					DBDo.updateAddress(Integer.parseInt(map.get("member_id") + ""), map.get("currency") + "", map.get("address") + "");
					System.out.println(TimeStamp2Date(getNowTimeStamp()) + "-------同步地址成功");
				} else {
					System.out.println(TimeStamp2Date(getNowTimeStamp()) + "-------同步地址失败： " + ret);
				}
			}
			*/
		} catch (Exception e) {
			System.out.println("同步到服务器失败：" + e.toString());
		}
	}

	
	
	/**
	 * 签名验证
	 * 
	 * @param params
	 * @param api_secret
	 * @return
	 */
	private static String createSign(Map<String, Object> params, String apiSecret) {
		SortedMap<String, Object> sortedMap = new TreeMap<String, Object>(
				params);

		StringBuffer sb = new StringBuffer();
		Set es = sortedMap.entrySet();
		Iterator it = es.iterator();
		while (it.hasNext()) {
			Map.Entry entry = (Map.Entry) it.next();
			String k = (String) entry.getKey();
			Object v = entry.getValue();
			if (null != v && !"".equals(v) && !"sign".equals(k)) {
				sb.append(k + "=" + v + "&");
			}
		}
		if ("MD5".equals(params.get("sign_type"))) {
			sb.append("apiSecret=" + apiSecret); //apiSecret放在最后
		} else {
			sb.deleteCharAt(sb.length() - 1); // 删除最后的&
		}
		
		String payload = sb.toString();
		String actualSign = "";
		if ("MD5".equals(params.get("sign_type"))) {
			actualSign = MacMD5.CalcMD5(payload, 28);
		} else {
			Mac hmacSha256 = null;
			try {
				hmacSha256 = Mac.getInstance("HmacSHA256");
				SecretKeySpec secKey = new SecretKeySpec(
						apiSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
				hmacSha256.init(secKey);
			} catch (NoSuchAlgorithmException e) {
				throw new RuntimeException("No such algorithm: " + e.getMessage());
			} catch (InvalidKeyException e) {
				throw new RuntimeException("Invalid key: " + e.getMessage());
			}
			byte[] hash = hmacSha256.doFinal(payload
					.getBytes(StandardCharsets.UTF_8));
			actualSign = Base64.encodeBase64String(hash);
			actualSign = MacMD5.CalcMD5(actualSign, 28);
		}
		
		//System.out.println("actualSign:" + actualSign);
		//System.out.println("payload:" + payload);
		return actualSign;
	}
	
    /**
     * Java将Unix时间戳转换成指定格式日期字符串
     * @param timestampString 时间戳 如："1473048265";
     * @param formats 要格式化的格式 默认："yyyy-MM-dd HH:mm:ss";
     *
     * @return 返回结果 如："2016-09-05 16:06:42";
     */
    public static String TimeStamp2Date(String timestampString) {
    	String formats = "yyyy-MM-dd HH:mm:ss";
        Long timestamp = Long.parseLong(timestampString) * 1000;
        String date = new SimpleDateFormat(formats, Locale.CHINA).format(new Date(timestamp));
        return date;
    }
    
    /**
     * 取得当前时间戳（精确到秒）
     *
     * @return nowTimeStamp
     */
    public static String getNowTimeStamp() {
        long time = System.currentTimeMillis();
        String nowTimeStamp = String.valueOf(time / 1000);
        return nowTimeStamp;
    }
    
    /**
     * 确认是否是手续费地址,如果是则不入账.
     * @param toAddress 收款地址
     * @return
     */
    private static boolean isSXFAddress(String toAddress) {
    	if(usdt_btcSXFAddr.equals(toAddress)) {
    		return true;
    	}
    	return false;
    }
    
    /**
     * 判断btc地址是否存在数据库
     * @param curAddr
     * @return
     */
    private static boolean isExistsAddress(String curAddr) {
		if ("1".equals(btcAddrMap.get(curAddr))) {
			return true;
		}
		return false;
	}
}