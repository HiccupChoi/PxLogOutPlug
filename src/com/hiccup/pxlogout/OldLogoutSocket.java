package com.hiccup.pxlogout;

import com.hiccup.json.JsonObject;
import com.hiccup.util.AccessOut;
import com.hiccup.util.BASE64;
import com.hiccup.util.Gzip;
import com.hiccup.util.IOTool;
import com.hiccup.util.Logger;
import com.hiccup.util.NetTool;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * @Author: Hiccup
 * @Date: 2019/9/25 10:14 上午
 */
public class OldLogoutSocket {

    private final static Charset UTF8 = StandardCharsets.UTF_8;

    private static void doPost(OutputStream out, String host, String url, String json, String version) {
        byte[] data = json.getBytes(UTF8);
        int len = data.length;
        byte[] gz = Gzip.encrypt2(data).toByteArray();
        String head = String.format("POST %s HTTP/1.1\r\n" +
                "version: %s" +
                "Connection: keep-alive\r\nHost: %s\r\n" +
                "Data-Length: %d\r\nContent-Length: %d\r\n\r\n", url, version, host, len, gz.length);
        try {
            out.write(head.getBytes(UTF8));
            out.write(gz);
        } catch (Throwable e) {
        }
    }

    static Result doLogOut(String server, String name, String pwd, String version) {
        String loginUrl = Urls.LOGIN_URL;
        String uploadUrl = Urls.OLD_URL_PATH;

        Socket so = NetTool.connect(server, 80, 10000);
        if (so != null) {
            String json = String.format("{\"userName\":\"%s\",\"password\":\"%s\"}", name, pwd);
            try {
                OutputStream out = so.getOutputStream();
                InputStream in = so.getInputStream();
                doPost(out, server, loginUrl, json, version);
                byte[] tmp = new byte[2048];
                int readLen = in.read(tmp);
                String data = new String(tmp, 0, readLen, UTF8);
                int off = data.indexOf("H4sIA");
                if (off > 0) {
                    String base64 = data.substring(off);
                    byte[] gz = BASE64.decode(base64);
                    AccessOut js = Gzip.decrypt(gz);
                    if (js != null) {
                        json = new String(js.getBuf(), 0, js.size(), UTF8);
                        Logger.info(json);
                        js.close();
                        JsonObject jsonObject = JsonObject.parse(json);
                        int err = Integer.parseInt(jsonObject.getString("errorCode"));
                        if (err == 0) {
                            JsonObject dataObj = jsonObject.getJSONObject("data");
                            if (dataObj != null) {
                                String sessionId = dataObj.readUTF("sessionId");
                                if (!sessionId.isEmpty()) {
                                    json = String.format("{\"fileName\":\"/login/server_info\",\"file\":\"AA==\",\"sessionId\":\"%s\"}", sessionId);
                                    doPost(out, server, uploadUrl, json, version);
                                    tmp = new byte[2048];
                                    readLen = in.read(tmp);
                                    data = new String(tmp, 0, readLen, UTF8);
                                    if(data.contains(Boolean.TRUE.toString())){
                                        Logger.info("账号：" + name + "注销成功");
                                        return new Result(true, "注销成功");
                                    }
                                    String errorMessage = jsonObject.getString("errorMsg");
                                    Logger.debug("账号：" + name + "注销失败，失败原因" + errorMessage);
                                    return new Result(false, errorMessage);
                                }
                            }
                        }else{
                            String errorMessage = jsonObject.getString("errorMsg");
                            Logger.debug("账号：" + name + "注销失败，失败原因" + errorMessage);
                            return new Result(false, jsonObject.getString("errorMsg"));
                        }
                    }
                }
            } catch (Throwable e) {
                Logger.error(e.getMessage());
                return new Result(false, "服务器错误");
            }
            IOTool.safeClose(so);
        }
        Logger.error("Socker连接失败");
        return new Result(false, "连接云服务器失败，请确认服务器地址正确");
    }

}
