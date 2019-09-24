package com.hiccup.pxlogout;

import com.hiccup.json.JsonObject;
import com.hiccup.util.AccessOut;
import com.hiccup.util.BASE64;
import com.hiccup.util.BindingInfo;
import com.hiccup.util.Gzip;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;

/**
 * @Author: Hiccup
 * @Date: 2019/9/24 11:10 上午
 */
public class LogOutService {

    private String serviceAddress;
    private String name;
    private String pwd;
    private String version;

    public LogOutService(String serviceAddress, String name, String pwd, String version) {
        this.serviceAddress = serviceAddress;
        this.name = name;
        this.pwd = pwd;
        this.version = version;
    }

    String getURLContent(String arg) throws Exception {

        // Post请求的url，与get不同的是不需要带参数
        String postUrl = Urls.OLD_URL_PATH;
        URL url = new URL(postUrl);
        // 打开连接
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
        // 设置是否向connection输出，因为这个是post请求，参数要放在
        // http正文内，因此需要设为true
        connection.setDoOutput(true);
        // Read from the connection. Default is true.
        connection.setDoInput(true);
        // Post 请求不能使用缓存
        connection.setUseCaches(false);

        connection.connect();
        OutputStream out;
        out = connection.getOutputStream();
        out.write(arg.getBytes());
        //流用完记得关
        out.flush();
        out.close();

        //获取响应
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String line;
        StringBuffer buffer = new StringBuffer();
        while ((line = reader.readLine()) != null) {
            buffer.append(line);
        }

        //该干的都干完了,记得把连接断了
        reader.close();
        connection.disconnect();

        return buffer.toString();
    }

    public static void main(String[] args) throws Exception {
        LogOutService logOutService = new LogOutService("alitest.fanxiaojian.cn", "alixin", "xin123", "2.30.01");
        BindingInfo bindingInfo = new BindingInfo();
        bindingInfo.setMacAddr(new String[]{""});
        bindingInfo.setOsVersion(logOutService.version);
        byte by = 1;
        bindingInfo.setEquipmentType(by);
        JsonObject jsonObject = bindingInfo.write(new JsonObject());
        jsonObject.put("sessionId","c88169f786aedc47e2e00a68b25588cf6509f8a55b5800401d008d5cbf8a1a15");
//        String json = logOutService.getURLContent(bindingInfo.write(jsonObject).toString());
        String json = logOutService.readContentFromPost();

        System.out.println(json);

    }

    public String readContentFromPost() throws IOException {
        JsonObject jsonObject = new JsonObject();
        jsonObject.put("password", pwd);
        jsonObject.put("userName", name);
        String arg = jsonObject.toString();
        byte[] data = arg.getBytes(Charset.forName("UTF-8"));
        byte[] gzipData;
        try (AccessOut ao = Gzip.encrypt(AccessOut.getFromPool(), data)) {
            gzipData = ao.toByteArray();
        }


        // Post请求的url，与get不同的是不需要带参数
        String urlString = "http://alitest.e.fanxiaojian.cn/irs-iface/om/inf/v1/login";
        URL postUrl = new URL(urlString);
        // 打开连接
        HttpURLConnection connection = (HttpURLConnection) postUrl.openConnection();
        // 设置是否向connection输出，因为这个是post请求，参数要放在
        // http正文内，因此需要设为true
        connection.setDoOutput(true);
        // Read from the connection. Default is true.
        connection.setDoInput(true);
        // 默认是 GET方式
        connection.setRequestMethod("POST");
        // Post 请求不能使用缓存
        connection.setUseCaches(false);
        //设置本次连接是否自动重定向
        connection.setInstanceFollowRedirects(true);
        // 配置本次连接的Content-type，配置为application/x-www-form-urlencoded的
        // 意思是正文是urlencoded编码过的form参数
        connection.setRequestProperty("Content-Type","application/json;charset=UTF-8");
        connection.setRequestProperty("Data-Length", String.valueOf(data.length));

        // 连接，从postUrl.openConnection()至此的配置必须要在connect之前完成，
        // 要注意的是connection.getOutputStream会隐含的进行connect。
        connection.connect();
        DataOutputStream out = new DataOutputStream(connection.getOutputStream());
        // 正文，正文内容其实跟get的URL中 '? '后的参数字符串一致
        // DataOutputStream.writeBytes将字符串中的16位的unicode字符以8位的字符形式写到流里面
        out.write(gzipData);
        //流用完记得关
        out.flush();
        out.close();
        //获取响应
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String line;
        StringBuffer buffer = new StringBuffer();
        while ((line = reader.readLine()) != null) {
            buffer.append(line);
        }
        reader.close();
        //该干的都干完了,记得把连接断了
        connection.disconnect();

        data = Gzip.decryptByte(BASE64.decode(buffer.toString()));

        String result = parseRet(data,data.length);

        return result;
    }

    private String parseRet(byte[] data, int len) throws IOException {
        int off = -1;
        for (int i = 0; i < len; i++) {
            if (data[i] == '{') {
                off = i;
                break;
            }
        }
        if (off >= 0) {
            String jstr = new String(data, off, len - off, Charset.forName("UTF-8"));
            return jstr;
        }
        return null;
    }

    private String newLogOut(){

        return null;
    }



}
