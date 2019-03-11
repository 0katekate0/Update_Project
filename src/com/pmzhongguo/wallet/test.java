package com.pmzhongguo.wallet;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.pmzhongguo.wallet.helper.DBHelper;

public class test {
	public static void main(String[] args) {
//		Integer addressNum = 0;
//		DBHelper db1 = new DBHelper("");//����DBHelper����
//        try {
//        	ResultSet rs = db1.pst.executeQuery("select count(1) from transactions where currency = 'BTC' and txid = '3027d9ecde47ec888b7987a50916541046754dccd59d729ff7104a9a47fec9bb' and address = '33Fb6VrKrMXoXDtUjYWR4Kqj4Bzm41qkZ6'");
//        	if (rs.next()){
//        		addressNum = rs.getInt(1);
//            	if (null == addressNum) {
//            		addressNum =  0;
//            	}
//        	}
//        	rs.close();
//            db1.close();//�ر�����
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//        System.out.println(addressNum);

		FNRPC  rpc = new FNRPC("47.74.210.15","50051");
		System.out.println("Result:" + rpc.GetBlock(415));
	}
}
