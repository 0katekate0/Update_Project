package com.pmzhongguo.wallet;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import com.nitinsurana.bitcoinlitecoin.rpcconnector.CryptoCurrencyRPC;
import com.nitinsurana.bitcoinlitecoin.rpcconnector.pojo.Transaction;
import com.pmzhongguo.wallet.helper.DBDo;

public class Test01 {

	private static Map btcAddrMap = null;
	
	public static void main(String[] args) {
//		final CryptoCurrencyRPC bitcoinRPC = new CryptoCurrencyRPC("xjjd", "xjjd.io", "127.0.0.1", "18334");
//	    List<Transaction>  btcTranLst = bitcoinRPC.listOmniTransactions("*", 10000, 0);
//	    System.out.println(btcTranLst.size());
//	    for (Transaction transaction : btcTranLst) {
//			System.out.println(transaction.getPropertyid());
//		}
		btcAddrMap = DBDo.getAllBtcAddr();
		boolean existsAddress = isExistsAddress("3DS7NrjDRxdcMe7WH1wokAc6MEPS9BWoqo");
		System.out.println(existsAddress);
		if(!existsAddress) {
			System.out.println("不行");
		}
	}
	
	private static boolean isExistsAddress(String curAddr) {
		if ("1".equals(btcAddrMap.get(curAddr))) {
			return true;
		}
		return false;
	}
}
