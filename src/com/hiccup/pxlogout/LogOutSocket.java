package com.hiccup.pxlogout;

import com.hiccup.json.JsonObject;
import com.hiccup.util.AccessOut;
import com.hiccup.util.BASE64;
import com.hiccup.util.Gzip;
import com.hiccup.util.IOTool;
import com.hiccup.util.NetTool;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.Charset;

/**
 * @Author: Hiccup
 * @Date: 2019/9/24 4:44 下午
 */
public class LogOutSocket {
    private final static String TAG = "PxLogout";
    private final static Charset UTF8 = Charset.forName("UTF-8");
    private static String sessionId;

    public static void doPost(OutputStream out, String host, String url, String json, String version, boolean toGz) {
        byte[] data = json.getBytes(UTF8);
        int len = data.length;
        byte[] gz = Gzip.encrypt2(data).toByteArray();
        String head = "";
        if (toGz){
            head = String.format("POST %s HTTP/1.1\r\n" +
                    "version: %s\r\n" +
                    "Connection: keep-alive\r\nHost: %s\r\n" +
                    "Content-Type:application/json;charset=UTF-8\r\n"+
                    "Data-Length: %d\r\nContent-Length: %d\r\n\r\n", url, version, host, len, gz.length);
            try {
                out.write(head.getBytes(UTF8));
                out.write(gz);
            } catch (Throwable e) {
            }
        } else {
            head = String.format("POST %s HTTP/1.1\r\n" +
                    "version: %s\r\n" +
                    "Cookie:IRSSID=%s\n"+
                    "Connection: keep-alive\r\nHost: %s\r\n" +
                    "Content-Type:application/json;charset=UTF-8\r\n"+
                    "Data-Length: %d\r\nContent-Length: %d\r\n\r\n", url, version, sessionId, host, len, data.length);
            try {
                out.write(head.getBytes(UTF8));
                out.write(data);
            } catch (Throwable e) {
            }
        }
    }

    public static void main(String[] args) {
        String result = doLogOut("alitest.fanxiaojian.cn", "alixin", "xin123", "2.30.02");
        System.out.println(result);
    }

    public static String doLogOut(String server, String name, String pwd, String version) {
        String ip = server;
        String loginUrl = "/irs-iface/om/inf/v1/login";
        String uploadUrl = Urls.NEW_URL_PATH;
        Socket so = NetTool.connect(ip, 80, 10000);
        if (so != null) {
            String json = String.format("{\"userName\":\"%s\",\"password\":\"%s\"}", name, pwd);
            try {
                OutputStream out = so.getOutputStream();
                InputStream in = so.getInputStream();
                doPost(out, ip, loginUrl, json, version, true);
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
                        js.close();
                        JsonObject jsonObject = JsonObject.parse(json);
                        String err = jsonObject.getString("errorCode");
                        if (err.equals("0")) {
                            JsonObject dataObj = jsonObject.getJSONObject("data");
                            if (dataObj != null) {
                                sessionId = dataObj.readUTF("sessionId");
                                if (!sessionId.isEmpty()) {
                                    json = String.format("{\"userId\":\"0\",\"equipmentType\":\"1\",\"macAddr\":[\"30-D9-D9-EE-AE-A9\"],\"sessionId\":\"%s\"}", sessionId);

                                    doPost(out, ip, uploadUrl, json, version, false);
                                    tmp = new byte[2048];
                                    readLen = in.read(tmp);
                                    data = new String(tmp, 0, readLen, UTF8);
                                    data = data.substring(data.indexOf("{"), data.lastIndexOf("}") + 1);
                                    return data;
                                }
                            }
                        }else{
                            return json;
                        }
                    }
                }
            } catch (Throwable e) {
                return "服务器错误";
            }
            IOTool.safeClose(so);
        }
        return "连接云服务器失败，请确认服务器地址正确";
    }
}
