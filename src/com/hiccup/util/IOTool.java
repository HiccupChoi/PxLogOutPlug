package com.hiccup.util;

import java.io.Closeable;
import java.io.InputStream;
import java.net.Socket;
import java.nio.channels.Selector;
import java.util.zip.ZipFile;

/**
 * @Author: Hiccup
 * @Date: 2019/9/24 2:42 下午
 */
public class IOTool {

    public static void safeClose(Closeable cs) {
        if (cs != null)
        {
            try
            {
                cs.close();
            } catch (Throwable e)
            {
            }
        }
    }

    public static void safeClose(Socket cs) {
        if (cs != null)
        {
            try
            {
                cs.close();
            } catch (Throwable e)
            {
            }
        }
    }

    public static void safeClose(ZipFile cs) {
        if (cs != null)
        {
            try
            {
                cs.close();
            } catch (Throwable e)
            {
            }
        }
    }

    public static void safeClose(Selector cs) {
        if (cs != null)
        {
            try
            {
                cs.close();
            } catch (Throwable e)
            {
            }
        }
    }

    public static boolean readFully(InputStream in, byte[] b) {
        return readFully(in, b, 0, b.length);
    }

    public static boolean readFully(InputStream in, byte[] b, int offset, int size) {
        int len;
        for (; size > 0;)
        {
            try
            {
                len = in.read(b, offset, size);
                if (len == -1)
                {
                    return false;
                }
                offset += len;
                size -= len;
            } catch (Exception ex)
            {
                return false;
            }
        }
        return true;
    }

    public static AccessOut readAccessOutInputStream(InputStream in) {
        AccessOut out = AccessOut.getFromPool();
        byte[] tmp = new byte[1024];
        int l;
        try
        {
            while ((l = in.read(tmp)) > 0)
            {
                out.write(tmp, 0, l);
            }
            out.flush();
        } catch (Throwable e)
        {
        }
        return out;
    }
}
