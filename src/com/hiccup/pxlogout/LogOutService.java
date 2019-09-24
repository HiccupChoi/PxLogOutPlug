package com.hiccup.pxlogout;

import gherkin.deps.com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

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

    public static void main(String[] args) throws Exception {
        LogOutService logOutService = new LogOutService("pre.irs.passiontec.cn", "winpre", "qwer1234", "2.30.01");
        String json = logOutService.getURLContent();
        System.out.println(json);
    }

    public String getURLContent() throws Exception {

        // Post请求的url，与get不同的是不需要带参数
        String postUrl = Urls.OLD_URL_PATH;
        URL url = new URL(postUrl);
        // 打开连接
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        connection.setRequestMethod("POST");
        // 设置是否向connection输出，因为这个是post请求，参数要放在
        // http正文内，因此需要设为true
        connection.setDoOutput(true);
        // Read from the connection. Default is true.
        connection.setDoInput(true);
        // Post 请求不能使用缓存
        connection.setUseCaches(false);

        connection.connect();
        OutputStream out;
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("server", serviceAddress);
        jsonObject.addProperty("name", name);
        jsonObject.addProperty("password", pwd);
        jsonObject.addProperty("version", version);
        out = connection.getOutputStream();
        out.write(jsonObject.toString().getBytes());
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

    public static void readContentFromPost() throws IOException {
        // Post请求的url，与get不同的是不需要带参数
        URL postUrl = new URL("http://www.xxxxxxx.com");
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
        connection.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
        // 连接，从postUrl.openConnection()至此的配置必须要在connect之前完成，
        // 要注意的是connection.getOutputStream会隐含的进行connect。
        connection.connect();
        DataOutputStream out = new DataOutputStream(connection
                .getOutputStream());
        // 正文，正文内容其实跟get的URL中 '? '后的参数字符串一致
//        String content = "字段名=" + URLEncoder.encode("字符串值", "编码");
//        // DataOutputStream.writeBytes将字符串中的16位的unicode字符以8位的字符形式写到流里面
//        out.writeBytes(content);
        //流用完记得关
        out.flush();
        out.close();
        //获取响应
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String line;
        while ((line = reader.readLine()) != null){
            System.out.println(line);
        }
        reader.close();
        //该干的都干完了,记得把连接断了
        connection.disconnect();
    }



}
