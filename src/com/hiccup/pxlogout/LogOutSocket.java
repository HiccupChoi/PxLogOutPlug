package com.hiccup.pxlogout;

import com.hiccup.json.JsonObject;
import com.hiccup.util.AccessOut;
import com.hiccup.util.BASE64;
import com.hiccup.util.Gzip;
import com.hiccup.util.IOTool;
import com.hiccup.util.Logger;
import com.hiccup.util.NetTool;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * @Author: Hiccup
 * @Date: 2019/9/24 4:44 下午
 * 2.40及以上版本注销账号设备绑定用
 */
class LogOutSocket {
    private final static Charset UTF8 = StandardCharsets.UTF_8;
    private static String sessionId;

    private static void doPost(OutputStream out, String host, String url, String json, String version, boolean toGz) {
        byte[] data = json.getBytes(UTF8);
        int len = data.length;
        byte[] gz = Gzip.encrypt2(data).toByteArray();
        String head;
        if (toGz){
            head = String.format("POST %s HTTP/1.1\r\n" +
                    "version: %s\r\n" +
                    "Connection: keep-alive\r\nHost: %s\r\n" +
                    "Content-Type:application/json;charset=UTF-8\r\n"+
                    "Data-Length: %d\r\nContent-Length: %d\r\n\r\n", url, version, host, len, gz.length);
            try {
                out.write(head.getBytes(UTF8));
                out.write(gz);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            // 调用接口需要在Cookie中添加sessionId信息
            head = String.format("POST %s HTTP/1.1\r\n" +
                    "version: %s\r\n" +
                    "Cookie:IRSSID=%s\n"+
                    "Connection: keep-alive\r\nHost: %s\r\n" +
                    "Content-Type:application/json;charset=UTF-8\r\n"+
                    "Data-Length: %d\r\nContent-Length: %d\r\n\r\n", url, version, sessionId, host, len, data.length);
            try {
                out.write(head.getBytes(UTF8));
                out.write(data);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    static Result doLogOut(String server, String name, String pwd) {
        String loginUrl = Urls.LOGIN_URL;
        String uploadUrl = Urls.NEW_URL_PATH;
        Socket so = NetTool.connect(server, 80, 10000);
        if (so != null) {
            String json = String.format("{\"userName\":\"%s\",\"password\":\"%s\"}", name, pwd);
            try {
                OutputStream out = so.getOutputStream();
                InputStream in = so.getInputStream();
                doPost(out, server, loginUrl, json, "2.41.00", true);
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
                                sessionId = dataObj.readUTF("sessionId");
                                if (!sessionId.isEmpty()) {
                                    json = String.format("{\"userId\":\"0\",\"equipmentType\":\"1\",\"macAddr\":[\"30-D9-D9-EE-AE-A9\"],\"sessionId\":\"%s\"}", sessionId);

                                    doPost(out, server, uploadUrl, json, "2.41", false);
                                    tmp = new byte[2048];
                                    readLen = in.read(tmp);
                                    data = new String(tmp, 0, readLen, UTF8);
                                    data = data.substring(data.indexOf("{"), data.lastIndexOf("}") + 1);
                                    jsonObject = JsonObject.parse(data);
                                    if (jsonObject.getString(Urls.CODE).equals(Urls.SUCCESS_CODE)) {
                                        Logger.info("账号：" + name + "注销成功");
                                        return new Result(true, "注销成功");
                                    }
                                    String errorMessage = jsonObject.getString("errorMsg");
                                    Logger.debug("账号：" + name + "注销失败，失败原因" + errorMessage);
                                    return new Result(false, errorMessage);
                                }
                            }
                        } else {
                            String errorMessage = jsonObject.getString("errorMsg");
                            Logger.debug("账号：" + name + "登录失败，失败原因" + errorMessage);
                            return new Result(false, errorMessage);
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
