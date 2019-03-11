package com.gop.forum.incentive.system.utils;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.googlecode.jsonrpc4j.Base64;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;


/**
 * 工具类http-post方法
 *
 * @author qiangkezhen
 */
public class SDKHttpClient {

    private Logger log = LoggerFactory.getLogger(SDKHttpClient.class);


    /**
     * post请求
     */
    public String post(String url, String key, String method, JSONArray jsonArray) {
    	CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpPost httppost = null;
        String result = null;
        String rpcAuth = (int) ((Math.random() * 9 + 1) * 100000) + "" + Base64.encodeBytes(key.getBytes());
        try {
            httppost = new HttpPost(url);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("method", method);
            jsonObject.put("id", ((int) ((Math.random() * 9 + 1) * 10)));
            jsonObject.put("jsonrpc", "2.0");
            jsonObject.put("params", jsonArray);
            //System.out.println(JSON.toJSONString(jsonObject));
            httppost.setEntity(new StringEntity(jsonObject.toString(), Charset.forName("UTF-8")));
            httppost.setHeader("Content-type", "application/json");
            httppost.setHeader("Authorization", rpcAuth);
            //log.info("【SDKHttpClient】｜POST开始：url=[{}]", url);
            CloseableHttpResponse response = httpclient.execute(httppost);
            //log.info("【SDKHttpClient】｜POST开始 URL:[{}][method={}][jsonArray={}],响应结果[response={}]!", url, method, jsonArray, response);
            if (null != response) {
                if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                    try {
                        result = EntityUtils.toString(response.getEntity(), "UTF-8");
                        //log.info("【SDKHttpClient】｜响应结果：{},[{}]", response.getStatusLine(), result);
                    } finally {
                        response.close();
                    }
                } else {
                    log.info("【SDKHttpClient】｜POST URL:[{}],响应结果[{}]!", url, response.getStatusLine().getStatusCode());
                }
            } else {
                log.info("【SDKHttpClient】｜POST URL:[{}],响应结果为空!", url);
            }
        } catch (Exception e) {
            log.error("【SDKHttpClient】｜POST URL:[{}] 出现异常[{}]!", url, e.getStackTrace());
        } finally {
            try {
                if (null != httppost) {
                    httppost.releaseConnection();
                }
            } catch (Exception e) {
                log.error("【SDKHttpClient】｜POST URL:[{}] 关闭httpclient.close()异常[{}]!", url, e.getStackTrace());
            }
        }
        return result;
    }
}
