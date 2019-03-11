package com.pmzhongguo.wallet;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import com.googlecode.jsonrpc4j.JsonRpcHttpClient;
import com.nitinsurana.bitcoinlitecoin.rpcconnector.CryptoCurrencyRPC;
import com.pmzhongguo.wallet.helper.DBDo;


/**
 * @author zhuzhisheng
 * @Description
 * @date on 2016/12/31.
 */
public class GenAddress {
	
	/**
	 * 这里生成的地址，会自动同步到生产服务器
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		mainBTC(0); //比特币，参数表是生成多少个地址，0表是不生成
		mainLTC(0); //莱特币，参数表是生成多少个地址，0表是不生成
		mainETH(100, "yinade888"); //以太坊，参数表是生成多少个地址，0表是不生成。第二个参数是密码
	}
    
    /**
     * 比特币地址生成
     * @param genNum
     * @throws Exception
     */
	public static void mainBTC(int genNum) throws Exception {
		if (0 > genNum) {
			System.err.println(">>>>>>>>>>>>>>>>>>比特币参数错误");
			return;
		}
		if (0 == genNum) {
			return;
		}
		
		System.err.println(">>>>>>>>>>>>>>>>>>开始生成比特币地址");
		
	    final String rpcUser = UpdateCoinToServer.rpcUser;
	    final String rpcPassword = UpdateCoinToServer.rpcPassword;
	    final String rpcHost = UpdateCoinToServer.btc_rpcHost;
	    final String rpcPort = UpdateCoinToServer.btc_rpcPort;
	    CryptoCurrencyRPC cryptoCurrencyRPC = new CryptoCurrencyRPC(rpcUser, rpcPassword, rpcHost, rpcPort);

	    Integer lastAddressId = DBDo.getLastAddressId("BTC");
	    for (int i = lastAddressId + 1; i < lastAddressId + 1 + genNum; i++) {
	    	String address = cryptoCurrencyRPC.getNewAddress("trade_" + i);
	    	DBDo.execUpdate("insert into m_coin_recharge_addr values(" + i + ",'BTC','" + address + "', 0)");
		}
	    System.out.println(">>>>>>>>>>>>>>>>>>成功生成比特币地址（个）：" + genNum);
	}
	
	
	/**
	 * 莱特币地址生成
	 * @param genNum
	 * @throws Exception
	 */
	public static void mainLTC(int genNum) throws Exception {
		if (0 > genNum) {
			System.err.println(">>>>>>>>>>>>>>>>>>莱特币参数错误");
			return;
		}
		
		if (0 == genNum) {
			return;
		}
		
		System.err.println(">>>>>>>>>>>>>>>>>>开始生成莱特币地址");
		
	    final String rpcUser = UpdateCoinToServer.rpcUser;
	    final String rpcPassword = UpdateCoinToServer.rpcPassword;
	    final String rpcHost = UpdateCoinToServer.ltc_rpcHost;
	    final String rpcPort = UpdateCoinToServer.ltc_rpcPort;
	    CryptoCurrencyRPC cryptoCurrencyRPC = new CryptoCurrencyRPC(rpcUser, rpcPassword, rpcHost, rpcPort);

	    Integer lastAddressId = DBDo.getLastAddressId("LTC");
	    for (int i = lastAddressId + 1; i < lastAddressId + 1 + genNum; i++) {
	    	String address = cryptoCurrencyRPC.getNewAddress("trade_" + i);
	    	DBDo.execUpdate("insert into m_coin_recharge_addr values(" + i + ",'LTC','" + address + "', 0)");
		}
	    System.out.println(">>>>>>>>>>>>>>>>>>成功生成莱特币地址（个）：" + genNum);
	}
	
	
	/**
	 * 以太坊地址生成
	 * @param genNum
	 */
	public static void mainETH(int genNum, String passwd) {
		if (0 > genNum) {
			System.err.println(">>>>>>>>>>>>>>>>>>以太坊参数错误");
			return;
		}
		if (0 == genNum) {
			return;
		}
		
		System.err.println(">>>>>>>>>>>>>>>>>>开始生成以太坊地址");
		
		// 参数为密码
		Object[] params = new Object[] {passwd};
		try {
			Map<String,String> headers = new HashMap(); 
			headers.put("content-type", "application/json");
			JsonRpcHttpClient client = new JsonRpcHttpClient(new URL(UpdateCoinToServer.eth_rpcHost), headers);
			
		    Integer lastAddressId = DBDo.getLastAddressId("ETH");
		    for (int i = lastAddressId + 1; i < lastAddressId + 1 + genNum; i++) {
		    	Object address = client.invoke("personal_newAccount", params, Object.class);
		    	DBDo.execUpdate("insert into m_coin_recharge_addr values(" + i + ",'ETH','" + address + "', 0)");
			}	
		    System.out.println(">>>>>>>>>>>>>>>>>>成功生成以太坊地址（个）：" + genNum);
		} catch (Throwable throwable) {
			throwable.printStackTrace();
		}
	}
}