package com.pmzhongguo.wallet.helper;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONArray;
import org.json.JSONObject;

import com.alibaba.fastjson.JSON;
import com.googlecode.jsonrpc4j.JsonRpcHttpClient;
import com.pmzhongguo.wallet.UpdateCoinToServer;

/**
 * @author zhuzhisheng
 * @Description
 * @date on 2016/12/31.
 */
public class UpdateETH {
	private static JsonRpcHttpClient client = null;
	
	private static Map ethAddrMap = null;
	private static Map<String, Map> ethContractMap = null;
	
	private static Integer curScanBlockNum = 0;
	private static Integer latestBlockNum = 0;
	
	public static void main(String[] args) throws Exception {
		update();
	}

	
	public static void update() throws Exception {
		Map<String,String> headers = new HashMap(); 
		headers.put("content-type", "application/json");
		client = new JsonRpcHttpClient(new URL(UpdateCoinToServer.eth_rpcHost), headers);
		
		ethAddrMap = DBDo.getAllEthAddr();
		ethContractMap = DBDo.getAllEthContract();
		
        Integer cacheTime = 1000 * 2;   //2秒
        Timer timer = new Timer();  
        // (TimerTask task, long delay, long period)任务，延迟时间，多久执行  
        timer.schedule(new TimerTask() {  
            @Override  
            public void run() {  
            	// 循环处理
            	//System.out.println(HelpUtils.formatDate8(new Date()) + ":-------------------------------以太坊循环处理");
            	try {
            		ethProcess();
            		//ethProcessEtherScan();
            	} catch (Exception e) {
            		e.printStackTrace();
            	}
            }  
        }, 1000, cacheTime);    
	}
	
	/**
	 * 访问EtherScan获取
	 */
	public final static void ethProcessEtherScan() {
		//System.out.println("已递归次数：" + recursionNum ++); 
		try {
			if (latestBlockNum == 0 || latestBlockNum <= curScanBlockNum + 12) {
				String blockNumStr = HelpUtils.loadJSON("http://api.etherscan.io/api?module=proxy&action=eth_blockNumber&apikey=4KSA3JW9CEECB46XATXAUFHETU1NYY8SV6");
				//System.out.println("blockNumStr:" + blockNumStr);
				if (HelpUtils.nullOrBlank(blockNumStr)) {
					return;
				}
				com.alibaba.fastjson.JSONObject blockNumJson = JSON.parseObject(blockNumStr);
				latestBlockNum = Integer.parseInt((blockNumJson.getString("result")).replace("0x", ""), 16);
			}
			
			curScanBlockNum = DBDo.getLastBlockNum("eth");
			System.out.println(HelpUtils.formatDate8(new Date()) + " ETH 当前读取区块数量：" + curScanBlockNum + "当前最大区块号：" + latestBlockNum + ",确认数:" + (latestBlockNum - curScanBlockNum));
			if (latestBlockNum < curScanBlockNum + 12) {
				//System.out.println("本区块暂未达到12个确认，本区块号：" + curScanBlockNum + ", 当前最大区块号：" + latestBlockNum);
				return;
			}
			
			final Object block = HelpUtils.loadJSON("http://api.etherscan.io/api?module=proxy&action=eth_getBlockByNumber&tag=0x" + Integer.toHexString(curScanBlockNum) + "&boolean=true&apikey=4KSA3JW9CEECB46XATXAUFHETU1NYY8SV6");
			if (null == block || "".equals(block)) {
				System.out.println("ETH暂无最新区块或者读取错误");
				return;
			}
			//System.out.println("区块：" + block);	
			
			JSONObject blockJson = new JSONObject((block + ""));
			String etherscanBlock = blockJson.getJSONObject("result").toString();
			
			threadProcEtherScan(etherscanBlock);
			
			
			DBDo.updateLastBlockNum("eth");
			
			// 递归,直到读完ETH区块
			//if (recursionNum ++ < 30) { //如果递归次数超过30，就重启一次。避免假死
			//	ethProcessEtherScan();
			//}
			
		} catch (Throwable throwable) {
			throwable.printStackTrace();
		}
	}
	private static void threadProcEtherScan(Object block) throws Throwable {
		JSONObject blockJson = new JSONObject((block + "").replace("=", ":"));
		JSONArray blockArr = blockJson.getJSONArray("transactions");
		String timestamp = blockJson.getString("timestamp");
		//System.out.println(timestamp);
		for (int i = 0; i < blockArr.length(); i++) {
			JSONObject ticketObject = blockArr.getJSONObject(i);
			
			//System.out.println("HASH：" + ticketObject.get("hash") + "---FROM：" + ticketObject.get("from") + "---TO：" + ticketObject.get("to") + "---VALUE：" + new BigInteger((ticketObject.get("value") + "").replace("0x", ""), 16).doubleValue() / 1000000000000000000L);
			//System.out.println(ticketObject.getString("to"));
			if (null == ticketObject.get("to") || "null".equals(ticketObject.get("to") + "")) {
				continue;
			}
			
			
			// 如果from是我们的账号，表示内部币归总，不能入账
			if (null == ticketObject.get("from") || "null".equals(ticketObject.get("from") + "") || isExistsAddress(ticketObject.getString("from"), "ETH")) {
				continue;
			}
			
			//System.out.println(ticketObject);
			
			// 直接是ETH会员的地址
			if (isExistsAddress(ticketObject.getString("to"), "ETH")) {
				
				// 表示是我们的会员地址
				System.err.println("收到ETH：" + ticketObject.get("to") + "," + BigDecimal.valueOf(new BigInteger((ticketObject.get("value") + "").replace("0x", ""), 16).doubleValue()));
				DBDo.addCoin("ETH", ticketObject.getString("to"), BigDecimal.valueOf(new BigInteger((ticketObject.get("value") + "").replace("0x", ""), 16).doubleValue() / 1000000000000000000L), ticketObject.get("hash") + "", HelpUtils.TimeStamp2Date(Long.valueOf((timestamp).replace("0x", ""), 16) + "000", null), 1);
				DBDo.addCollectAddr(ticketObject.getString("to"), "ETH");
				
			} else if (null != getETHContractAddress(ticketObject.getString("to"))) { //是在我们平台上的山寨币的地址，需要深入获得
				Map coinMap = getETHContractAddress(ticketObject.getString("to"));
				
				Object reciptStr = HelpUtils.loadJSON("http://api.etherscan.io/api?module=proxy&action=eth_getTransactionReceipt&txhash=" + ticketObject.get("hash") + "&apikey=4KSA3JW9CEECB46XATXAUFHETU1NYY8SV6");
				JSONObject reciptJson = new JSONObject((reciptStr + "")).getJSONObject("result");
				//System.out.println(recipt);
				//JSONObject reciptJson = new JSONObject((recipt + "").replace("=", ":"));
				if ((reciptJson.get("to") + "").equalsIgnoreCase((coinMap.get("contract_address")) + "")) {
					JSONArray logsArr = reciptJson.getJSONArray("logs");
					//System.out.println(timestamp);
					for (int j = 0; j < logsArr.length(); j++) {
						JSONObject logObj = logsArr.getJSONObject(j);
						JSONArray topicsArr = logObj.getJSONArray("topics");
						if (null != topicsArr && topicsArr.length() == 3) {
							String curAddress = "0x" + topicsArr.getString(2).substring(26, 66);
							if (isExistsAddress(curAddress, "ETH")) {
								System.err.println("收到了" + coinMap.get("currency") + "：" + curAddress + "," + BigDecimal.valueOf(new BigInteger((logObj.get("data") + "").replace("0x", ""), 16).doubleValue()));
								Long decimals = 1L;
								for (int k = 0; k < Integer.parseInt(coinMap.get("decimals_of_unit") + ""); k++) {
									decimals *= 10;
								}
								DBDo.addCoin(coinMap.get("currency") + "", curAddress, BigDecimal.valueOf(new BigInteger((logObj.get("data") + "").replace("0x", ""), 16).doubleValue() / decimals), ticketObject.get("hash") + "", HelpUtils.TimeStamp2Date(Long.valueOf((timestamp).replace("0x", ""), 16) + "000", null), 1);
								DBDo.addCollectAddr(curAddress, "ETH");
							}
						}
					}
				}					
			}				
		}
	}
	
	/**
	 * 启动geth方法："C:\Program Files\ETH\Geth" 
	 * geth --rpc --rpcapi "db,eth,web3,personal,admin" --rpcaddr 127.0.0.1
	 */
	public final static void ethProcess() {
		try {
			Integer latestBlockNum = Integer.parseInt((client.invoke("eth_blockNumber", null, Object.class) + "").replace("0x", ""), 16);
			
			Integer lastBlockNum = DBDo.getLastBlockNum("eth");
			System.out.println(HelpUtils.formatDate8(new Date()) + " ETH 当前读取区块数量：" + lastBlockNum);
			if (latestBlockNum < lastBlockNum + 12) {
				//System.out.println("本区块暂未达到12个确认，本区块号：" + lastBlockNum + ", 当前最大区块号：" + latestBlockNum);
				return;
			}
			
			Object[] params = new Object[] {"0x" + Integer.toHexString(lastBlockNum), true}; //11841
			final Object block = client.invoke("eth_getBlockByNumber", params, Object.class);
			if (null == block) {
				System.out.println("ETH暂无最新区块");
				return;
			}
			//System.out.println("区块：" + block);		
			
			threadProc(block);
			
			/*
			Thread t = new Thread(new Runnable() {
				public void run() {
					try {
						threadProc(block);
					} catch (Exception e) {
						e.printStackTrace();
					} catch (Throwable t) {
						t.printStackTrace();
					}
				}
			});
			t.start();
			*/
			
			DBDo.updateLastBlockNum("eth");
			
			// 递归,直到读完ETH区块
			ethProcess();
			
		} catch (Throwable throwable) {
			throwable.printStackTrace();
		}
	}
	
	private static void threadProc(Object block) throws Throwable {
		JSONObject blockJson = new JSONObject((block + "").replace("=", ":"));
		JSONArray blockArr = blockJson.getJSONArray("transactions");
		String timestamp = blockJson.getString("timestamp");
		//System.out.println(timestamp);
		for (int i = 0; i < blockArr.length(); i++) {
			JSONObject ticketObject = blockArr.getJSONObject(i);
			
			//System.out.println("HASH：" + ticketObject.get("hash") + "---FROM：" + ticketObject.get("from") + "---TO：" + ticketObject.get("to") + "---VALUE：" + new BigInteger((ticketObject.get("value") + "").replace("0x", ""), 16).doubleValue() / 1000000000000000000L);
			//System.out.println(ticketObject.getString("to"));
			if (null == ticketObject.get("to") || "null".equals(ticketObject.get("to") + "")) {
				continue;
			}
			
			
			// 如果from是我们的账号，表示内部币归总，不能入账
			if (null == ticketObject.get("from") || "null".equals(ticketObject.get("from") + "") || isExistsAddress(ticketObject.getString("from"), "ETH")) {
				continue;
			}
			
			//System.out.println(ticketObject);
			
			// 直接是ETH会员的地址
			if (isExistsAddress(ticketObject.getString("to"), "ETH")) {
				
				// 表示是我们的会员地址
				System.out.println("收到ETH：" + ticketObject.get("to") + "," + BigDecimal.valueOf(new BigInteger((ticketObject.get("value") + "").replace("0x", ""), 16).doubleValue()));
				DBDo.addCoin("ETH", ticketObject.getString("to"), BigDecimal.valueOf(new BigInteger((ticketObject.get("value") + "").replace("0x", ""), 16).doubleValue() / 1000000000000000000L), ticketObject.get("hash") + "", HelpUtils.TimeStamp2Date(Long.valueOf((timestamp).replace("0x", ""), 16) + "000", null), 1);
				DBDo.addCollectAddr(ticketObject.getString("to"), "ETH");
			} else if (null != getETHContractAddress(ticketObject.getString("to"))) { //是在我们平台上的山寨币的地址，需要深入获得
				Map coinMap = getETHContractAddress(ticketObject.getString("to"));
				
				Object recipt = client.invoke("eth_getTransactionReceipt", new Object[] {ticketObject.get("hash")}, Object.class);
				//System.out.println(recipt);
				JSONObject reciptJson = new JSONObject((recipt + "").replace("=", ":"));
				if ((reciptJson.get("to") + "").equalsIgnoreCase((coinMap.get("contract_address")) + "")) {
					JSONArray logsArr = reciptJson.getJSONArray("logs");
					//System.out.println(timestamp);
					for (int j = 0; j < logsArr.length(); j++) {
						JSONObject logObj = logsArr.getJSONObject(j);
						JSONArray topicsArr = logObj.getJSONArray("topics");
						if (null == topicsArr || topicsArr.length() < 3) {
							continue;
						}
						String curAddress = "0x" + topicsArr.getString(2).substring(26, 66);
						if (isExistsAddress(curAddress, "ETH")) {
							System.out.println(logObj);
							System.out.println("收到了" + coinMap.get("currency") + "：" + curAddress + "," + BigDecimal.valueOf(new BigInteger((logObj.get("data") + "").replace("0x", ""), 16).doubleValue()));
							Long decimals = 1L;
							for (int k = 0; k < Integer.parseInt(coinMap.get("decimals_of_unit") + ""); k++) {
								decimals *= 10;
							}
							DBDo.addCoin(coinMap.get("currency") + "", curAddress, BigDecimal.valueOf(new BigInteger((logObj.get("data") + "").replace("0x", ""), 16).doubleValue() / decimals), ticketObject.get("hash") + "", HelpUtils.TimeStamp2Date(Long.valueOf((timestamp).replace("0x", ""), 16) + "000", null), 1);
							DBDo.addCollectAddr(ticketObject.getString("to"), "ETH");
						}
					}
				}					
			}				
		}
	}
	
	private static boolean isExistsAddress(String curAddr, String coin) {
		if ("1".equals(ethAddrMap.get(curAddr))) {
			return true;
		}
		return false;
	}
	
	private static Map getETHContractAddress(String to) {
		return ethContractMap.get(to);
	}
}