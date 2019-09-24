package com.hiccup.util;

import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * @Author: Hiccup
 * @Date: 2019/9/24 4:51 下午
 */
public class NetTool {

    public static Socket connect(String addr, int port, int timeOut) {
        Socket fd = null;
        try
        {
            fd = new Socket();
            fd.connect(new InetSocketAddress(addr, port), timeOut);
            fd.setTcpNoDelay(true);
            return fd;
        } catch (Throwable e)
        {
            System.out.println(e);
        }
        IOTool.safeClose(fd);
        return null;
    }

}
