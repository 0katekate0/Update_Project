package com.pmzhongguo.wallet.helper;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DBDo {
    static String sql = null;  
    static DBHelper db1 = null;  
    static ResultSet ret = null;  
    
    /**
     * ���ݱ������ƣ���õ�ǰ���һ��id
     * @param currency
     * @return
     */
    public static Integer getLastAddressId(String currency) {
    	Integer alreadyNum = 0;
        db1 = new DBHelper("");//����DBHelper����  
        try {  
        	ResultSet rs = db1.pst.executeQuery("select member_id from m_coin_recharge_addr where currency = '" + currency + "' order by member_id desc limit 0, 1");
        	if (rs.next()){
        		alreadyNum = rs.getInt(1);
            	if (null == alreadyNum) {
            		alreadyNum =  0;
            	}
        	}
        	rs.close();
            db1.close();//�ر�����  
        } catch (SQLException e) {
            e.printStackTrace();  
        }
        
        return alreadyNum;
    }
  
    public static String getLastCoinTxid(String currency) {
    	String txid = "";
        db1 = new DBHelper("");//����DBHelper����  
        try {  
        	ResultSet rs = db1.pst.executeQuery("select txid from transactions where currency = '" + currency + "' order by id desc limit 0, 1");
        	if (rs.next()){
        		txid = rs.getString(1);
            	if (null == txid) {
            		txid =  "";
            	}
        	}
        	rs.close();
            db1.close();//�ر�����  
        } catch (SQLException e) {
            e.printStackTrace();  
        }
        
        return txid;
    }
    
    public static void execUpdate(String strSql) {
        db1 = new DBHelper("");//����DBHelper����  
        try {  
			db1.pst.execute(strSql);
            db1.close();//�ر�����  
        } catch (SQLException e) {
            e.printStackTrace();  
        }
		
	}
    
	public static void addCoin(String currency, String address,
			BigDecimal amount, String txid, String timereceived,
			long confirmations) {
        db1 = new DBHelper("");//����DBHelper����  
        try {  
			db1.pst.execute("insert into transactions (currency, address, amount, txid, timereceived, confirmations) values('"
					+ currency + "', '" + address + "', " + amount + ", '" + txid + "', '" + timereceived + "', " + confirmations + ")");
            db1.close();//�ر�����  
        } catch (SQLException e) {
            e.printStackTrace();  
        }
		
	}
	
	public static void addCollectAddr(String address, String currency) {
        db1 = new DBHelper("");//����DBHelper����  
        try {  
			db1.pst.execute("insert into need_collect_addr (address, currency) values('" + address + "', '" + currency + "')");
            db1.close();//�ر�����  
        } catch (SQLException e) {
            e.printStackTrace();  
        }
	}
	
	public static void updateCoin(Integer id) {
		db1 = new DBHelper("");//����DBHelper����  
        try {  
			db1.pst.execute("update transactions set is_update = 1 where id = " + id);
            db1.close();//�ر�����  
        } catch (SQLException e) {
            e.printStackTrace();  
        }
	}
    
	
	public static List<Map<String, Object>> getWKCAddress(int from, int to) {
		List<Map<String, Object>> lst = new ArrayList<Map<String, Object>>();
        db1 = new DBHelper("");//����DBHelper����  
        try {  
        	ResultSet rs = db1.pst.executeQuery("SELECT * FROM m_coin_recharge_addr WHERE currency = 'WKC' and member_id >= " + from + " and member_id <= " + to);
        	while (rs.next()) {
        		Map<String, Object> map = new HashMap<String, Object>();
        		map.put("address", rs.getString("address"));        		
        		lst.add(map);
        	}
        	rs.close();
            db1.close();//�ر�����  
        } catch (SQLException e) {
            e.printStackTrace();  
        }
        
        return lst;
	}


	public static List<Map<String, Object>> getWaitingCoin() {
		List<Map<String, Object>> lst = new ArrayList<Map<String, Object>>();
        db1 = new DBHelper("");//����DBHelper����  
        try {  
        	ResultSet rs = db1.pst.executeQuery("select * from transactions where is_update = 0");
        	while (rs.next()) {
        		Map<String, Object> map = new HashMap<String, Object>();
        		map.put("id", rs.getString("id"));
        		map.put("currency", rs.getString("currency"));
        		map.put("r_address", rs.getString("address"));
        		map.put("r_amount", rs.getBigDecimal("amount").stripTrailingZeros());
        		map.put("r_create_time", rs.getString("timereceived"));
        		map.put("r_address", rs.getString("address"));
        		map.put("r_txid", rs.getString("txid"));
        		map.put("r_confirmations", rs.getInt("confirmations"));
        		
        		lst.add(map);
        	}
        	rs.close();
            db1.close();//�ر�����  
        } catch (SQLException e) {
            e.printStackTrace();  
        }
        
        return lst;
	}
	
	
	public static List<Map<String, Object>> getWaitingAddress() {
		List<Map<String, Object>> lst = new ArrayList<Map<String, Object>>();
        db1 = new DBHelper("");//����DBHelper����  
        try {  
        	ResultSet rs = db1.pst.executeQuery("select * from m_coin_recharge_addr where is_up_to_server = 0 limit 0, 100");
        	while (rs.next()) {
        		Map<String, Object> map = new HashMap<String, Object>();
        		map.put("member_id", rs.getString("member_id"));
        		map.put("currency", rs.getString("currency"));
        		map.put("address", rs.getString("address"));
        		
        		lst.add(map);
        	}
        	rs.close();
            db1.close();//�ر�����  
        } catch (SQLException e) {
            e.printStackTrace();  
        }
        
        return lst;
	}
	
	
	/**
	 * 
	 * @param type Ŀǰ֧��eth etc
	 * @return
	 */
	public static Integer getLastBlockNum(String type) {
    	Integer blockNum = 0;
        db1 = new DBHelper("");//����DBHelper����  
        try {  
        	ResultSet rs = db1.pst.executeQuery("select " + type + "_last_block from t_config where id = 1");
        	if (rs.next()){
        		blockNum = rs.getInt(1);
            	if (null == blockNum) {
            		blockNum =  0;
            	}
        	}
        	rs.close();
            db1.close();//�ر�����  
        } catch (SQLException e) {
            e.printStackTrace();  
        }
        
        return blockNum;
	}
	
	
	/**
	 * ��ʽ������ yyyy-MM-dd HH:mm:ss
	 * 
	 * @param myDate
	 * @return
	 */
	public static String formatDate8(Date myDate) {
		return formatDateByFormatStr(myDate, "yyyy-MM-dd HH:mm:ss");
	}
	
	public static String formatDateByFormatStr(Date myDate, String formatStr) {
		SimpleDateFormat formatter = new SimpleDateFormat(formatStr);
		return formatter.format(myDate);
	}


	public static void updateLastBlockNum(String type) {
		db1 = new DBHelper("");//����DBHelper����  
        try {  
			db1.pst.execute("update t_config set " + type + "_last_block = " + type + "_last_block + 1 where id = 1");
            db1.close();//�ر�����  
        } catch (SQLException e) {
            e.printStackTrace();  
        }
	}

	public static Map getAllEthAddr() {
    	Map ethAddrMap = new HashMap();
        db1 = new DBHelper("");//����DBHelper����  
        try {  
        	ResultSet rs = db1.pst.executeQuery("select address from m_coin_recharge_addr where currency = 'ETH'");
        	while (rs.next()){
        		ethAddrMap.put(rs.getString(1), "1");
        	}
        	rs.close();
            db1.close();//�ر�����  
        } catch (SQLException e) {
            e.printStackTrace();  
        }
        
        return ethAddrMap;
	}
	
	public static Map getAllBtcAddr() {
    	Map btcAddrMap = new HashMap();
        db1 = new DBHelper("");//����DBHelper����  
        try {  
        	ResultSet rs = db1.pst.executeQuery("select address from m_coin_recharge_addr where currency = 'BTC'");
        	while (rs.next()){
        		btcAddrMap.put(rs.getString(1), "1");
        	}
        	rs.close();
            db1.close();//�ر�����  
        } catch (SQLException e) {
            e.printStackTrace();  
        }
        return btcAddrMap;
	}
	
	public static Map<String, Map> getAllEthContract() {
    	Map ethContractMap = new HashMap();
        db1 = new DBHelper("");//����DBHelper����  
        try {  
        	ResultSet rs = db1.pst.executeQuery("select * from t_eth_coin");
        	while (rs.next()){
        		Map retMap = new HashMap();
        		retMap.put("currency", rs.getString("currency"));
        		retMap.put("decimals_of_unit", rs.getInt("decimals_of_unit"));
        		retMap.put("contract_address", rs.getString("contract_address"));
        		
        		ethContractMap.put(rs.getString("contract_address"), retMap);
        	}
        	rs.close();
            db1.close();//�ر�����  
        } catch (SQLException e) {
            e.printStackTrace();  
        }
        
        return ethContractMap;
	}
	
	public static boolean isExistsAddress(String address, String type) {
    	Integer addressNum = 0;
        db1 = new DBHelper("");//����DBHelper����  
        try {  
        	ResultSet rs = db1.pst.executeQuery("select count(1) from m_coin_recharge_addr where currency = '" + type + "' and address = '" + address + "'");
        	if (rs.next()){
        		addressNum = rs.getInt(1);
            	if (null == addressNum) {
            		addressNum =  0;
            	}
        	}
        	rs.close();
            db1.close();//�ر�����  
        } catch (SQLException e) {
            e.printStackTrace();  
        }
        
        return addressNum == 1;
	}
	
	/**
	 * �Ƿ����ɽկ�ҵ�ַ
	 * @param address
	 * @param type
	 * @return
	 */
	public static Map getETHContractAddress(String address) {
    	Integer addressNum = 0;
        db1 = new DBHelper("");//����DBHelper����  
        Map retMap = null;
        try {  
        	ResultSet rs = db1.pst.executeQuery("select * from t_eth_coin where contract_address = '" + address + "'");
        	if (rs.next()){
        		retMap = new HashMap();
        		retMap.put("currency", rs.getString("currency"));
        		retMap.put("decimals_of_unit", rs.getInt("decimals_of_unit"));
        		retMap.put("contract_address", rs.getString("contract_address"));
        	}
        	rs.close();
            db1.close();//�ر�����  
        } catch (SQLException e) {
            e.printStackTrace();  
        }
        
        return retMap;
	}


	public static Integer isExistsTxID(String txid, String address, String currency) {
    	Integer addressNum = 0;
        db1 = new DBHelper("");//����DBHelper����  
        try {  
        	ResultSet rs = db1.pst.executeQuery("select count(1) from transactions where currency = '" + currency + "' and txid = '" + txid + "' and address = '" + address + "'");
        	if (rs.next()){
        		addressNum = rs.getInt(1);
            	if (null == addressNum) {
            		addressNum =  0;
            	}
        	}
        	rs.close();
            db1.close();//�ر�����  
        } catch (SQLException e) {
            e.printStackTrace();  
        }
        
        return addressNum;
	}

	public static void updateAddress(int member_id, String currency, String address) {
		db1 = new DBHelper("");//����DBHelper����  
        try {  
			db1.pst.execute("update m_coin_recharge_addr set is_up_to_server = 1 where member_id = " + member_id + " and currency = '" + currency + "' and address = '" + address + "'");
            db1.close();//�ر�����  
        } catch (SQLException e) {
            e.printStackTrace();  
        }
	}
}