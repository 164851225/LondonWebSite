package com.oddfar.campus.common.utils.http;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.oddfar.campus.common.constant.Constants;
import com.oddfar.campus.common.exception.ServiceException;
import com.oddfar.campus.common.utils.StringUtils;
import org.apache.http.client.methods.HttpPost;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.collections4.MapUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import javax.net.ssl.*;
import java.io.*;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 通用http发送方法
 * 
 * @author ruoyi
 */
public class HttpUtils
{
    private static final Logger log = LoggerFactory.getLogger(HttpUtils.class);

    /**
     * 向指定 URL 发送GET方法的请求
     *
     * @param url 发送请求的 URL
     * @return 所代表远程资源的响应结果
     */
    public static String sendGet(String url)
    {
        return sendGet(url, StringUtils.EMPTY);
    }

    /**
     * 向指定 URL 发送GET方法的请求
     *
     * @param url 发送请求的 URL
     * @param param 请求参数，请求参数应该是 name1=value1&name2=value2 的形式。
     * @return 所代表远程资源的响应结果
     */
    public static String sendGet(String url, String param)
    {
        return sendGet(url, param, Constants.UTF8);
    }

    /**
     * 向指定 URL 发送GET方法的请求
     *
     * @param url 发送请求的 URL
     * @param param 请求参数，请求参数应该是 name1=value1&name2=value2 的形式。
     * @param contentType 编码类型
     * @return 所代表远程资源的响应结果
     */
    public static String sendGet(String url, String param, String contentType)
    {
        StringBuilder result = new StringBuilder();
        BufferedReader in = null;
        try
        {
            String urlNameString = StringUtils.isNotBlank(param) ? url + "?" + param : url;
            log.info("sendGet - {}", urlNameString);
            URL realUrl = new URL(urlNameString);
            URLConnection connection = realUrl.openConnection();
            connection.setRequestProperty("accept", "*/*");
            connection.setRequestProperty("connection", "Keep-Alive");
            connection.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            connection.connect();
            in = new BufferedReader(new InputStreamReader(connection.getInputStream(), contentType));
            String line;
            while ((line = in.readLine()) != null)
            {
                result.append(line);
            }
            log.info("recv - {}", result);
        }
        catch (ConnectException e)
        {
            log.error("调用HttpUtils.sendGet ConnectException, url=" + url + ",param=" + param, e);
        }
        catch (SocketTimeoutException e)
        {
            log.error("调用HttpUtils.sendGet SocketTimeoutException, url=" + url + ",param=" + param, e);
        }
        catch (IOException e)
        {
            log.error("调用HttpUtils.sendGet IOException, url=" + url + ",param=" + param, e);
        }
        catch (Exception e)
        {
            log.error("调用HttpsUtil.sendGet Exception, url=" + url + ",param=" + param, e);
        }
        finally
        {
            try
            {
                if (in != null)
                {
                    in.close();
                }
            }
            catch (Exception ex)
            {
                log.error("调用in.close Exception, url=" + url + ",param=" + param, ex);
            }
        }
        return result.toString();
    }

    /**
     * 向指定 URL 发送POST方法的请求
     *
     * @param url 发送请求的 URL
     * @param param 请求参数，请求参数应该是 name1=value1&name2=value2 的形式。
     * @return 所代表远程资源的响应结果
     */
    public static String sendPost(String url, String param) {
        PrintWriter out = null;
        BufferedReader in = null;
        StringBuilder result = new StringBuilder();
        try
        {
            log.info("sendPost - {}", url);
            URL realUrl = new URL(url);
            URLConnection conn = realUrl.openConnection();
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            conn.setRequestProperty("Accept-Charset", "utf-8");
            conn.setRequestProperty("contentType", "utf-8");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            out = new PrintWriter(conn.getOutputStream());
            out.print(param);
            out.flush();
            in = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
            String line;
            while ((line = in.readLine()) != null)
            {
                result.append(line);
            }
            log.info("recv - {}", result);
        }
        catch (ConnectException e)
        {
            log.error("调用HttpUtils.sendPost ConnectException, url=" + url + ",param=" + param, e);
        }
        catch (SocketTimeoutException e)
        {
            log.error("调用HttpUtils.sendPost SocketTimeoutException, url=" + url + ",param=" + param, e);
        }
        catch (IOException e)
        {
            log.error("调用HttpUtils.sendPost IOException, url=" + url + ",param=" + param, e);
        }
        catch (Exception e)
        {
            log.error("调用HttpsUtil.sendPost Exception, url=" + url + ",param=" + param, e);
        }
        finally
        {
            try
            {
                if (out != null)
                {
                    out.close();
                }
                if (in != null)
                {
                    in.close();
                }
            }
            catch (IOException ex)
            {
                log.error("调用in.close Exception, url=" + url + ",param=" + param, ex);
            }
        }
        return result.toString();
    }
    
    /**
     * 发送POST请求（JSON格式）
     *
     * @param url 请求URL
     * @param requestBody 请求体对象
     * @return 响应字符串
     */
    public static String sendPostJson(String url, Object requestBody) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json; charset=utf-8");
        return sendPostWithHeaders(url, JSON.toJSONString(requestBody), headers);
    }
    
    /**
     * 发送带请求头的POST请求
     *
     * @param url 请求URL
     * @param param 请求参数
     * @param headers 请求头
     * @return 响应字符串
     */
    public static String sendPostWithHeaders(String url, String param, Map<String, String> headers) {
        PrintWriter out = null;
        BufferedReader in = null;
        StringBuilder result = new StringBuilder();
        try {
            log.info("sendPostWithHeaders - {}", url);
            URL realUrl = new URL(url);
            URLConnection conn = realUrl.openConnection();
            
            // 设置默认请求头
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");

            // 添加自定义请求头
            if (headers != null && !headers.isEmpty()) {
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    conn.setRequestProperty(entry.getKey(), entry.getValue());
                }
            }
            
            conn.setDoOutput(true);
            conn.setDoInput(true);
            
            // 获取URLConnection对象对应的输出流
            out = new PrintWriter(conn.getOutputStream());
            // 发送请求参数
            out.print(param);
            // flush输出流的缓冲
            out.flush();
            // 定义BufferedReader输入流来读取URL的响应
            in = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
            String line;
            while ((line = in.readLine()) != null) {
                result.append(line);
            }
            log.info("recv - {}", result);
        } catch (ConnectException e) {
            log.error("调用HttpUtils.sendPostWithHeaders ConnectException, url=" + url + ",param=" + param, e);
            throw new ServiceException("连接服务器失败: " + e.getMessage());
        } catch (SocketTimeoutException e) {
            log.error("调用HttpUtils.sendPostWithHeaders SocketTimeoutException, url=" + url + ",param=" + param, e);
            throw new ServiceException("请求超时: " + e.getMessage());
        } catch (IOException e) {
            log.error("调用HttpUtils.sendPostWithHeaders IOException, url=" + url + ",param=" + param, e);
            throw new ServiceException("IO异常: " + e.getMessage());
        } catch (Exception e) {
            log.error("调用HttpUtils.sendPostWithHeaders Exception, url=" + url + ",param=" + param, e);
            throw new ServiceException("请求异常: " + e.getMessage());
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
                log.error("调用in.close Exception", ex);
            }
        }
        return result.toString();
    }
    public static String sendPostWithHeadersMy(String url, String param, Map<String, String> headers) {
        String result = "";
        //开始请求API接口时间
        long startTime=System.currentTimeMillis();
        //请求API接口的响应时间
        long endTime= 0L;
        HttpEntity httpEntity = null;
        HttpResponse httpResponse = null;
        HttpClient httpClient = null;
        try {
            // 创建连接
            httpClient = HttpClientFactory.getInstance().getHttpClient();
            // 设置请求头和报文
            HttpPost httpPost = HttpClientFactory.getInstance().httpPost(url);
            Header header=new BasicHeader("Accept-Encoding",null);
            httpPost.addHeader(header);
            if (MapUtils.isNotEmpty(headers)) {
                Set<String> keySet = headers.keySet();
                for (String key : keySet) {
                    httpPost.addHeader(key, headers.get(key));
                }
            }


            // 设置报文和通讯格式
            StringEntity stringEntity = new StringEntity(param,HttpConstant.UTF8_ENCODE);
            stringEntity.setContentEncoding(HttpConstant.UTF8_ENCODE);
            stringEntity.setContentType(HttpConstant.APPLICATION_JSON);
            httpPost.setEntity(stringEntity);
            log.info("请求{}接口的参数为{}",url,param);
            //执行发送，获取相应结果
            httpResponse = httpClient.execute(httpPost);
            httpEntity= httpResponse.getEntity();
            result = EntityUtils.toString(httpEntity);
        } catch (Exception e) {
            log.warn("请求{}接口出现异常",url,e);
        }finally {
            try {
                EntityUtils.consume(httpEntity);
            } catch (IOException e) {
                log.warn("http请求释放资源异常",e);
            }
        }
        //请求接口的响应时间
        endTime = System.currentTimeMillis();
        return result;
    }

    /**
     * 发送带认证token的POST请求（JSON格式）
     *
     * @param url 请求URL
     * @param token Bearer token
     * @param requestBody 请求体对象
     * @return 响应字符串
     */
    public static String sendPostWithToken(String url, String token, Object requestBody) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization",  token);
        headers.put("Content-Type", "application/json; charset=utf-8");
        return sendPostWithHeaders(url, JSON.toJSONString(requestBody), headers);
    }
    
    /**
     * 发送带认证token的GET请求
     *
     * @param url 请求URL
     * @param token Bearer token
     * @return 响应字符串
     */
    public static String sendGetWithToken(String url, String token) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", token);
        return sendGetWithHeaders(url, StringUtils.EMPTY, headers);
    }
    
    /**
     * 发送带请求头的GET请求
     *
     * @param url 请求URL
     * @param param 请求参数
     * @param headers 请求头
     * @return 响应字符串
     */
    public static String sendGetWithHeaders(String url, String param, Map<String, String> headers) {
        StringBuilder result = new StringBuilder();
        BufferedReader in = null;
        try {
            String urlNameString = StringUtils.isNotBlank(param) ? url + "?" + param : url;
            log.info("sendGetWithHeaders - {}", urlNameString);
            URL realUrl = new URL(urlNameString);
            URLConnection connection = realUrl.openConnection();
            
            // 设置默认请求头
            connection.setRequestProperty("accept", "*/*");
            connection.setRequestProperty("connection", "Keep-Alive");
            connection.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            
            // 添加自定义请求头
            if (headers != null && !headers.isEmpty()) {
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    connection.setRequestProperty(entry.getKey(), entry.getValue());
                }
            }
            
            connection.connect();
            in = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
            String line;
            while ((line = in.readLine()) != null) {
                result.append(line);
            }
            log.info("recv - {}", result);
        } catch (ConnectException e) {
            log.error("调用HttpUtils.sendGetWithHeaders ConnectException, url=" + url + ",param=" + param, e);
            throw new ServiceException("连接服务器失败: " + e.getMessage());
        } catch (SocketTimeoutException e) {
            log.error("调用HttpUtils.sendGetWithHeaders SocketTimeoutException, url=" + url + ",param=" + param, e);
            throw new ServiceException("请求超时: " + e.getMessage());
        } catch (IOException e) {
            log.error("调用HttpUtils.sendGetWithHeaders IOException, url=" + url + ",param=" + param, e);
            throw new ServiceException("IO异常: " + e.getMessage());
        } catch (Exception e) {
            log.error("调用HttpUtils.sendGetWithHeaders Exception, url=" + url + ",param=" + param, e);
            throw new ServiceException("请求异常: " + e.getMessage());
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception ex) {
                log.error("调用in.close Exception", ex);
            }
        }
        return result.toString();
    }
    
    /**
     * 发送POST请求并将响应解析为指定类型对象
     *
     * @param url 请求URL
     * @param requestBody 请求体对象
     * @param clazz 响应类型
     * @return 响应对象
     */
    public static <T> T postForObject(String url, Object requestBody, Class<T> clazz) {
        String result = sendPostJson(url, requestBody);
        return JSON.parseObject(result, clazz);
    }
    
    /**
     * 发送带认证token的POST请求并将响应解析为指定类型对象
     *
     * @param url 请求URL
     * @param token Bearer token
     * @param requestBody 请求体对象
     * @param clazz 响应类型
     * @return 响应对象
     */
    public static <T> T postForObjectWithToken(String url, String token, Object requestBody, Class<T> clazz) {
        String result = sendPostWithToken(url, token, requestBody);
        return JSON.parseObject(result, clazz);
    }
    
    /**
     * 发送GET请求并将响应解析为指定类型对象
     *
     * @param url 请求URL
     * @param token Bearer token
     * @param clazz 响应类型
     * @return 响应对象
     */
    public static <T> T getForObjectWithToken(String url, String token, Class<T> clazz) {
        String result = sendGetWithToken(url, token);
        return JSON.parseObject(result, clazz);
    }
    
    /**
     * 使用TypeReference解析复杂类型JSON
     *
     * @param json JSON字符串
     * @param typeReference 类型引用
     * @return 对象实例
     */
    public static <T> T parseJson(String json, TypeReference<T> typeReference) {
        return JSON.parseObject(json, typeReference);
    }

    public static String sendSSLPost(String url, String param)
    {
        StringBuilder result = new StringBuilder();
        String urlNameString = url + "?" + param;
        try
        {
            log.info("sendSSLPost - {}", urlNameString);
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, new TrustManager[] { new TrustAnyTrustManager() }, new java.security.SecureRandom());
            URL console = new URL(urlNameString);
            HttpsURLConnection conn = (HttpsURLConnection) console.openConnection();
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            conn.setRequestProperty("Accept-Charset", "utf-8");
            conn.setRequestProperty("contentType", "utf-8");
            conn.setDoOutput(true);
            conn.setDoInput(true);

            conn.setSSLSocketFactory(sc.getSocketFactory());
            conn.setHostnameVerifier(new TrustAnyHostnameVerifier());
            conn.connect();
            InputStream is = conn.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String ret = "";
            while ((ret = br.readLine()) != null)
            {
                if (ret != null && !"".equals(ret.trim()))
                {
                    result.append(new String(ret.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8));
                }
            }
            log.info("recv - {}", result);
            conn.disconnect();
            br.close();
        }
        catch (ConnectException e)
        {
            log.error("调用HttpUtils.sendSSLPost ConnectException, url=" + url + ",param=" + param, e);
        }
        catch (SocketTimeoutException e)
        {
            log.error("调用HttpUtils.sendSSLPost SocketTimeoutException, url=" + url + ",param=" + param, e);
        }
        catch (IOException e)
        {
            log.error("调用HttpUtils.sendSSLPost IOException, url=" + url + ",param=" + param, e);
        }
        catch (Exception e)
        {
            log.error("调用HttpsUtil.sendSSLPost Exception, url=" + url + ",param=" + param, e);
        }
        return result.toString();
    }

    private static class TrustAnyTrustManager implements X509TrustManager
    {
        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType)
        {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType)
        {
        }

        @Override
        public X509Certificate[] getAcceptedIssuers()
        {
            return new X509Certificate[] {};
        }
    }

    private static class TrustAnyHostnameVerifier implements HostnameVerifier
    {
        @Override
        public boolean verify(String hostname, SSLSession session)
        {
            return true;
        }
    }
}