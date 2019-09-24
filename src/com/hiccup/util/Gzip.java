package com.hiccup.util;

import com.hiccup.util.gzip.GZIPInput;
import com.hiccup.util.gzip.GZIPOutput;
import com.hiccup.util.gzip.GzipPool;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Comparator;

/**
 *
 * @author chen Created on 2013-4-1, 9:27:02
 */
public class Gzip {

    private static final String TAG = "Gzip";
    private static GzipPool gzipPool;

    private static void init() {
        gzipPool = new GzipPool(16);
    }

    static
    {
        init();
    }

    /**
     *
     * @param data
     * @param offset
     * @param len
     * @return
     */
    public static AccessOut encrypt2(byte[] data, int offset, int len) {
        return encrypt(data, offset, len);
    }

    /**
     *
     * @param data
     * @param offset
     * @param len
     * @return
     */
    public static AccessOut encrypt(byte[] data, int offset, int len) {
        return encrypt(new AccessOut(len + 20), data, offset, len);
    }

    /**
     *
     * @param data
     * @return
     */
    public static AccessOut encrypt2(byte[] data) {
        return encrypt2(data, 0, data.length);
    }

    /**
     *
     * @param data
     * @return
     */
    public static AccessOut encrypt(AccessOut ao, byte[] data) {
        return encrypt(ao, data, 0, data.length);
    }

    /**
     * @return
     */
    public static AccessOut encrypt(AccessOut ao, AccessOut src) {
        return encrypt(ao, src.getBuf(), 0, src.size());
    }

    /**
     *
     * @param gzip
     * @return
     */
    public static AccessOut decrypt(byte[] gzip) {
        return decrypt(gzip, 0, gzip.length);
    }

    /**
     *
     * @param gzip
     * @return
     */
    public static byte[] decryptByte(byte[] gzip) {
        return decryptByte(gzip, 0, gzip.length);
    }

    public static byte[] decryptByte(byte[] gzip, int offset, int len) {
        AccessOut data = decrypt(gzip, offset, len);
        try
        {
            return data == null ? null : data.toByteArray();
        } finally
        {
            IOTool.safeClose(data);
        }
    }

    /**
     *
     * @param gzip
     * @return
     */
    public static byte[] decrypt2(byte[] gzip, int decSize) {
        return decrypt2(gzip, 0, gzip.length, decSize);
    }

    /**
     *
     * @param gzip
     * @param offset
     * @param len
     * @return
     */
    public static byte[] decrypt2(byte[] gzip, int offset, int len, int decSize) {
        ByteArrayInputStream in = new ByteArrayInputStream(gzip, offset, len);
        GZIPInput zin = gzipPool.geIn(in);
        boolean needClose = zin == null;
        try
        {
            byte[] ret = new byte[decSize];
            if (zin == null)
            {
                zin = new GZIPInput(in);
            }
            IOTool.readFully(zin, ret);
            return ret;
        } catch (Throwable ex)
        {
        } finally {
            if (needClose) {
                IOTool.safeClose(zin);
            } else {
                zin.setBusy(false);
            }
        }
        return null;
    }

    /**
     *
     * @param gzip
     * @param offset
     * @param len
     * @return
     */
    public static AccessOut decrypt(byte[] gzip, int offset, int len) {
        ByteArrayInputStream in = new ByteArrayInputStream(gzip, offset, len);
        GZIPInput zin = gzipPool.geIn(in);
        boolean needClose = zin == null;
        AccessOut ret = null;
        try
        {
            if (zin == null)
            {
                zin = new GZIPInput(in);
            }
            ret = IOTool.readAccessOutInputStream(zin);
            return ret;
        } catch (Throwable ex)
        {
            IOTool.safeClose(ret);
        } finally {
            if (needClose) {
                IOTool.safeClose(zin);
            } else {
                zin.setBusy(false);
            }
        }
        return null;
    }

    /**
     *
     * @param data
     * @param offset
     * @param len
     * @return
     */
    public static AccessOut encrypt(AccessOut bout, byte[] data, int offset, int len) {
        GZIPOutput zout = gzipPool.getOut(bout);
        boolean needClose = zout == null;
        try
        {
            if (zout == null)
            {
                zout = new GZIPOutput(bout);
            }
            zout.write(data, offset, len);
            zout.finish();
            return bout;
        } catch (Throwable ex)
        {
        } finally {
            if (needClose) {
                IOTool.safeClose(zout);
            } else {
                zout.setBusy(false);
            }
        }
        return null;
    }

    /**
     * 压缩文件
     *
     * @param dstPath 文件保存路径
     * @param srcPath 要压缩的文件路径
     * @param buf
     * @param minGzSize 最小压缩大小
     * @param delSrc 压缩成功后删除源文件
     * @return
     */
    public static boolean encryptFile(String dstPath, String srcPath, byte[] buf, int minGzSize, boolean delSrc, boolean log) {
        try
        {
            File srcFile = new File(srcPath);
            if (srcFile.isFile())
            {
                long size = srcFile.length();
                if (size < minGzSize)
                {
                    return false;
                }
                File dstFile = new File(dstPath);
                if (!dstFile.isDirectory())
                {
                    FileOutputStream fout = new FileOutputStream(dstFile);
                    try
                    {
                        GZIPOutput zout = gzipPool.getOut(fout);
                        InputStream in = null;
                        if (zout != null)
                        {
                            try
                            {
                                in = new FileInputStream(srcFile);
                                if (buf == null)
                                {
                                    buf = new byte[Math.min(10240, (int) size)];
                                }
                                while (size > 0)
                                {
                                    int readLen = in.read(buf);
                                    if (readLen <= 0)
                                    {
                                        break;
                                    } else
                                    {
                                        zout.write(buf, 0, readLen);
                                        size -= readLen;
                                    }
                                }
                                zout.finish();
                                if (size <= 0)
                                {
                                    if (log)
                                    {
                                        long nsize = dstFile.length();
                                        long osize = srcFile.length();
                                    }
                                    if (delSrc)
                                    {
                                        in.close();
                                        boolean delOk = srcFile.delete();
                                        if (!delOk)
                                        {
                                        }
                                    }
                                    return true;
                                }
                            } finally
                            {
                                zout.setBusy(false);
                                IOTool.safeClose(in);
                            }
                        }
                    } finally
                    {
                        IOTool.safeClose(fout);
                    }
                }
            }
        } catch (Throwable e)
        {
        }
        return false;
    }

    /**
     * 压缩目录下的所有非.gz文件
     *
     * @param delSrc 压缩成功后删除源文件
     * @param process 压缩处理判断 0(压缩) 小余0(停止压缩) 大余0(该文件或目录不压缩)
     * @return
     */
    public static boolean encryptDir(String path, byte[] buf, int minGzSize, boolean delSrc, boolean log, Comparator<String> process) {
        try
        {
            File dir = new File(path);
            if (dir.isDirectory())
            {
                File[] files = dir.listFiles();
                if (files != null)
                {
                    for (int i = 0; i < files.length; i++)
                    {
                        File f = files[i];
                        if (f == null)
                        {
                            continue;
                        }
                        String fpath = f.getAbsolutePath();
                        if (f.isFile())
                        {
                            if (!fpath.endsWith(".gz"))
                            {
                                String gzPath = fpath + ".gz";
                                if (process != null)
                                {
                                    int v = process.compare(fpath, gzPath);
                                    if (v < 0)
                                    {
                                        return false;
                                    }
                                    if (v > 0)
                                    {
                                        continue;
                                    }
                                }
                                encryptFile(gzPath, fpath, buf, minGzSize, delSrc, log);
                            }
                        } else
                        {
                            if (process != null)
                            {
                                int v = process.compare(fpath, null);
                                if (v < 0)
                                {
                                    return false;
                                }
                                if (v > 0)
                                {
                                    continue;
                                }
                            }
                            encryptDir(fpath, buf, minGzSize, delSrc, log, process);
                        }
                    }
                }
            }
            return true;
        } catch (Throwable e)
        {
        }
        return false;
    }

}
