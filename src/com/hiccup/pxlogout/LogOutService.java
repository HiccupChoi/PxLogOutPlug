package com.hiccup.pxlogout;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * @Author: Hiccup
 * @Date: 2019/9/24 11:10 上午
 * 2.40以下版本账号注销用
 */
class LogOutService {

    static String getURLContent(String arg) throws Exception {

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
        //设置超时
        connection.setConnectTimeout(2000);
        connection.setReadTimeout(5000);

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
        StringBuilder buffer = new StringBuilder();
        while ((line = reader.readLine()) != null) {
            buffer.append(line);
        }

        //该干的都干完了,记得把连接断了
        reader.close();
        connection.disconnect();

        return buffer.toString();
    }

}
