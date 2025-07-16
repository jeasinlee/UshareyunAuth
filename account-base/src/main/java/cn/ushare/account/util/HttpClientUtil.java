package cn.ushare.account.util;

import net.sf.json.JSONObject;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class HttpClientUtil {
    private static final String ENCODING = "utf-8";

    public static String doGet(String url, Map<String, String> param) throws Exception {
        // 创建Httpclient对象
        CloseableHttpClient httpclient = HttpClients.createDefault();
        String resultString = "";
        CloseableHttpResponse response = null;
        try {
            // 创建uri
            URIBuilder builder = new URIBuilder(url);
            if (param != null) {
                for (String key : param.keySet()) {
                    builder.addParameter(key, param.get(key));
                }
            }
            URI uri = builder.build();

            // 创建http GET请求
            HttpGet httpGet = new HttpGet(uri);
            httpGet.setHeader("referer", "http://pay.ushareyun.net");

            // 执行请求
            response = httpclient.execute(httpGet);
            // 判断返回状态是否为200
            if (response.getStatusLine().getStatusCode() == 200) {
                resultString = EntityUtils.toString(response.getEntity(), ENCODING);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            try {
                if (response != null) {
                    response.close();
                }
                httpclient.close();
            } catch (IOException e) {
                e.printStackTrace();
                throw e;
            }
        }
        return resultString;
    }

    public static String doGet(String url) throws Exception {
        return doGet(url, null);
    }

    public static String doPost(String url, Map<String, String> param) {
        // 创建Httpclient对象
        CloseableHttpClient httpClient = HttpClients.createDefault();
        CloseableHttpResponse response = null;
        String resultString = "";
        try {
            // 创建Http Post请求
            HttpPost httpPost = new HttpPost(url);
            // 创建参数列表
            if (param != null) {
                List<NameValuePair> paramList = new ArrayList<>();
                for (String key : param.keySet()) {
                    paramList.add(new BasicNameValuePair(key, param.get(key)));
                }
                // 模拟表单
                UrlEncodedFormEntity entity = new UrlEncodedFormEntity(paramList, ENCODING);
                httpPost.setEntity(entity);
            }
            // 执行http请求
            response = httpClient.execute(httpPost);
            resultString = EntityUtils.toString(response.getEntity(), ENCODING);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                response.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return resultString;
    }

    public static String doPost(String url) {
        return doPost(url, null);
    }

    public static String doPostJson(String url, String json) {
        return doPostJson(url,null,json);
    }

    /**
     * 请求的参数类型为json
     *
     * @param url
     * @param json
     * @return {username:"",pass:""}
     */
    public static String doPostJson(String url,Map<String, String> headerMap, String json) {
        // 创建Httpclient对象
        CloseableHttpClient httpClient = HttpClients.createDefault();
        CloseableHttpResponse response = null;
        String resultString = "";
        try {
            // 创建Http Post请求
            HttpPost httpPost = new HttpPost(url);
            if (headerMap != null) {
                Iterator headerIterator = headerMap.entrySet().iterator();          //循环增加header
                while(headerIterator.hasNext()){
                    Map.Entry<String,String> elem = (Map.Entry<String, String>) headerIterator.next();
                    httpPost.addHeader(elem.getKey(),elem.getValue());
                }
            }

            // 创建请求内容
            StringEntity entity = new StringEntity(json, ContentType.APPLICATION_JSON);
            httpPost.setEntity(entity);
            // 执行http请求
            response = httpClient.execute(httpPost);
            resultString = EntityUtils.toString(response.getEntity(), ENCODING);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                response.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return resultString;
    }

    /**
     * 请求的参数类型为xml,支持SOAP1.2
     *
     * @param url
     * @param xml
     *
     * @return {username:"",pass:""}
     */
    public static String doPostXml(String url, String xml, String soapname) {
        // 创建Httpclient对象
        CloseableHttpClient httpClient = HttpClients.createDefault();
        CloseableHttpResponse response = null;
        String resultString = "";
        try {
            // 创建Http Post请求
            HttpPost httpPost = new HttpPost(url);
            // 设置SOAPAction，支持soap1.2
            httpPost.setHeader("SOAPAction", soapname);
            // 创建请求内容
            StringEntity entity = new StringEntity(xml, ContentType.TEXT_XML);
            httpPost.setEntity(entity);
            // 执行http请求
            response = httpClient.execute(httpPost);
            resultString = EntityUtils.toString(response.getEntity(), ENCODING);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                response.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return resultString;
    }

    /**
     * 请求的参数类型为xml
     *
     * @param url
     * @param xml
     * @return {username:"",pass:""}
     */
    public static String doPostXml(String url, String xml) {
        // 创建Httpclient对象
        CloseableHttpClient httpClient = HttpClients.createDefault();
        CloseableHttpResponse response = null;
        String resultString = "";
        try {
            // 创建Http Post请求
            HttpPost httpPost = new HttpPost(url);
            // 创建请求内容
            StringEntity entity = new StringEntity(xml, ContentType.TEXT_XML);
            httpPost.setEntity(entity);
            // 执行http请求
            response = httpClient.execute(httpPost);
            resultString = EntityUtils.toString(response.getEntity(), ENCODING);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                response.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return resultString;
    }

    public static String doPostWebservice(String url, String xml) {
        // 创建Httpclient对象
        CloseableHttpClient httpClient = HttpClients.createDefault();
        CloseableHttpResponse response = null;
        String resultString = "";
        try {
            // 创建Http Post请求
            HttpPost httpPost = new HttpPost(url);
            httpPost.setHeader("Content-Type", "text/xml;charset=UTF-8");
//            httpPost.setHeader("SOAPAction", "");
            StringEntity data = new StringEntity(xml, Charset.forName("UTF-8"));
            httpPost.setEntity(data);
            response = httpClient.execute(httpPost);
            resultString = EntityUtils.toString(response.getEntity(), ENCODING);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                response.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return resultString;
    }

    public static String doPostMap(String url, Map<String, String> map) {
        // 创建Httpclient对象
        CloseableHttpClient httpClient = HttpClients.createDefault();
        CloseableHttpResponse response = null;
        String resultString = "";
        try {
            // 创建Http Post请求
            HttpPost httpPost = new HttpPost(url);
            httpPost.setHeader("Content-Type", "application/json;charset=utf-8");
//            httpPost.setHeader("SOAPAction", "");
            JSONObject jsonString = JSONObject.fromObject(map);
            //使用StringEntity转换成实体类型
            StringEntity entity = new StringEntity(jsonString.toString());
            httpPost.setEntity(entity);
            response = httpClient.execute(httpPost);
            resultString = EntityUtils.toString(response.getEntity(), ENCODING);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                response.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return resultString;
    }
}
