package com.hiccup.util;



import com.hiccup.tools.DataPool;
import com.hiccup.tools.IPool;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.EOFException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UTFDataFormatException;
import java.util.Arrays;

/**
 * 带简单加密的输入&输出流
 * @author chen
 */
public class AccessOut extends OutputStream implements DataOutput, DataInput, IPool {

    private static final boolean DEBUG = false;
    private final static String TAG = "AccessOut";
    /**
     * The buffer where data is stored.
     */
    protected byte buf[];

    /**
     * The number of valid bytes in the buffer.
     */
    protected int count = 0;

    private final byte writeBuffer[] = new byte[8];
    /**
     * bytearr is initialized on demand by writeUTF working arrays initialized
     * on demand by readUTF
     */
    private byte bytearr[] = null;
    private char chararr[] = null;
    private volatile boolean busy = false;

    private static DataPool<AccessOut> aoPool;

//    private long useTime;
//    private Exception ex;
    private static void init() {
        aoPool = new DataPool<AccessOut>(true, new AccessOut[16]) {
            @Override
            protected AccessOut newInstance() {
                return new AccessOut(1024, false);
            }

            @Override
            public void run() {
                super.run();
                aoPool = null;
            }

        };
    }

    static
    {
        init();
    }

//    private Exception ex;
//    private long useTime = 0;
    public static AccessOut getFromPool() {
        AccessOut ret = aoPool.getData();
        if (ret != null)
        {
            ret.resetInOut();
        }
        return ret;
    }

    /**
     * Closing a <tt>ByteArrayOutputStream</tt> has no effect. The methods in
     * this class can be called after the stream has been closed without
     * generating an <tt>IOException</tt>.
     * <p>
     *
     */
    @Override
    public void close() {
        busy = false;
    }

    @Override
    public boolean isBusy() {
        return busy;
    }

    @Override
    public void setBusy(boolean busy) {
        this.busy = busy;
    }

    public AccessOut(int size) {
        buf = new byte[size];
    }

    public AccessOut(int size, boolean print) {
        buf = new byte[size];
    }

    public AccessOut(byte[] data) {
        this.buf = data;
        this.count = data.length;
        this.pos = 0;
    }

    public AccessOut(byte[] data, int off, int len) {
        this.buf = data;
        this.count = off + len;
        this.pos = off;
    }

    /**
     * Increases the capacity if necessary to ensure that it can hold at least
     * the number of elements specified by the minimum capacity argument.
     *
     * @param minCapacity the desired minimum capacity
     * @throws OutOfMemoryError if {@code minCapacity < 0}. This is interpreted
     * as a request for the unsatisfiably large capacity
     * {@code (long) Integer.MAX_VALUE + (minCapacity - Integer.MAX_VALUE)}.
     */
    private void ensureCapacity(int minCapacity) {
        // overflow-conscious code
        if (minCapacity - buf.length > 0)
        {
            grow(minCapacity);
        }
    }

    /**
     * Increases the capacity to ensure that it can hold at least the number of
     * elements specified by the minimum capacity argument.
     *
     * @param minCapacity the desired minimum capacity
     */
    private void grow(int minCapacity) {
        // overflow-conscious code
        int oldCapacity = buf.length;
        int newCapacity = oldCapacity << 1;
        if (newCapacity - minCapacity < 0)
        {
            newCapacity = minCapacity;
        }
        if (newCapacity < 0)
        {
            if (minCapacity < 0) // overflow
            {
                throw new OutOfMemoryError();
            }
            newCapacity = Integer.MAX_VALUE;
        }
        buf = Arrays.copyOf(buf, newCapacity);
    }

    /**
     * Writes the specified byte to this byte array output stream.
     *
     * @param b the byte to be written.
     */
    @Override
    public void write(int b) {
        ensureCapacity(count + 1);
        buf[count] = (byte) b;
        count++;
    }

    /**
     * Writes <code>len</code> bytes from the specified byte array starting at
     * offset <code>off</code> to this byte array output stream.
     *
     * @param b the data.
     * @param off the start offset in the data.
     * @param len the number of bytes to write.
     */
    @Override
    public void write(byte b[], int off, int len) {
        if ((off < 0) || (off > b.length) || (len < 0)
                || ((off + len) - b.length > 0))
        {
            throw new IndexOutOfBoundsException();
        }
        ensureCapacity(count + len);
        System.arraycopy(b, off, buf, count, len);
        count += len;
    }

    public void writeSkip(int len) {
        ensureCapacity(count + len);
        count += len;
    }

    /**
     * Resets the <code>count</code> field of this byte array output stream to
     * zero, so that all currently accumulated output in the output stream is
     * discarded. The output stream can be used again, reusing the already
     * allocated buffer space.
     *
     * @see AccessOut#count
     */
    public void reset() {
        count = 0;
    }

    public void resetInput() {
        pos = 0;
    }

    public void resetInOut() {
        count = 0;
        pos = 0;
    }

    /**
     * Creates a newly allocated byte array. Its size is the current size of
     * this output stream and the valid contents of the buffer have been copied
     * into it.
     *
     * @return the current contents of this output stream, as a byte array.
     * @see java.io.ByteArrayOutputStream#size()
     */
    public byte[] toByteArray() {
        return Arrays.copyOf(buf, count);
    }

    /**
     * Returns the current size of the buffer.
     *
     * @return the value of the <code>count</code> field, which is the number of
     * valid bytes in this output stream.
     * @see AccessOut#count
     */
    public int size() {
        return count;
    }

    public byte[] toGzip() {
        AccessOut tmp = AccessOut.getFromPool();
        try
        {
            return Gzip.encrypt(tmp, buf, 0, count).toByteArray();
        } finally
        {
            tmp.close();
        }
    }

    public byte[] getBuf() {
        return buf;
    }

    public DataInput getInputStream() {
        return this;
    }

    @Override
    public void write(byte[] b) {
        write(b, 0, b.length);
    }

    @Override
    public void writeBoolean(boolean v) {
        write(v ? 1 : 0);
    }

    @Override
    public void writeByte(int v) {
        write(v);
    }

    @Override
    public void writeShort(int v) {
        write((v >>> 8) & 0xFF);
        write((v) & 0xFF);
    }


    public void writeLShort(int v) {
        write((v) & 0xFF);
        write((v >>> 8) & 0xFF);
    }

    public void writeShort(int v, int off) {
        buf[off] = (byte) (v >>> 8);
        buf[off + 1] = (byte) (v);
    }

    @Override
    public void writeChar(int v) {
        write((v >>> 8) & 0xFF);
        write((v) & 0xFF);
    }

    @Override
    public void writeInt(int v) {
        write((v >>> 24) & 0xFF);
        write((v >>> 16) & 0xFF);
        write((v >>> 8) & 0xFF);
        write((v) & 0xFF);
    }

    public void writeLInt(int v) {
        write((v) & 0xFF);
        write((v >>> 8) & 0xFF);
        write((v >>> 16) & 0xFF);
        write((v >>> 24) & 0xFF);
    }

    @Override
    public void writeLong(long v) {
        writeBuffer[0] = (byte) (v >>> 56);
        writeBuffer[1] = (byte) (v >>> 48);
        writeBuffer[2] = (byte) (v >>> 40);
        writeBuffer[3] = (byte) (v >>> 32);
        writeBuffer[4] = (byte) (v >>> 24);
        writeBuffer[5] = (byte) (v >>> 16);
        writeBuffer[6] = (byte) (v >>> 8);
        writeBuffer[7] = (byte) (v);
        write(writeBuffer, 0, 8);
    }

    @Override
    public void writeFloat(float v) {
        writeInt(Float.floatToIntBits(v));
    }

    @Override
    public void writeDouble(double v) {
        writeLong(Double.doubleToLongBits(v));
    }

    @Override
    public void writeBytes(String s) {
        int len = s.length();
        for (int i = 0; i < len; i++)
        {
            write((byte) s.charAt(i));
        }
    }

    @Override
    public void writeChars(String s) {
        int len = s.length();
        for (int i = 0; i < len; i++)
        {
            int v = s.charAt(i);
            write((v >>> 8) & 0xFF);
            write((v) & 0xFF);
        }
    }

    public void writeGBK(String str) throws IOException {
        if (str == null || str.isEmpty())
        {
            writeShort(0);
            return;
        }
        byte[] data = str.getBytes("GBK");
        writeShort(data.length);
        write(data);
    }

    public String readGBK() throws IOException {
        int len = readUnsignedShort();
        byte[] data = new byte[len];
        readFully(data);
        return new String(data, "GBK");
    }

    public void writeObjectArray(Object[] args) throws IOException {
        if (args != null)
        {
            int len = args.length;
            for (int i = 0; i < len; i++)
            {
                String str = args[i] == null ? null : args[i].toString();
                if (DEBUG)
                {
                }
                writeUTF(str);
            }
        }
    }

    public void print(String str) {
        if (str == null || str.isEmpty())
        {
            return;
        }
        int strlen = str.length();
        int utflen = 0;
        int c, byteCount = 0;

        /* use charAt instead of copying String to char array */
        for (int i = 0; i < strlen; i++)
        {
            c = str.charAt(i);
            if ((c >= 0x0001) && (c <= 0x007F))
            {
                utflen++;
            } else if (c > 0x07FF)
            {
                utflen += 3;
            } else
            {
                utflen += 2;
            }
        }

        byte[] bytembufarr;

        if (bytearr == null || (bytearr.length < (utflen + 2)))
        {
            bytearr = new byte[(utflen * 2) + 2];
        }
        bytembufarr = bytearr;
        int i;
        for (i = 0; i < strlen; i++)
        {
            c = str.charAt(i);
            if (!((c >= 0x0001) && (c <= 0x007F)))
            {
                break;
            }
            bytembufarr[byteCount++] = (byte) c;
        }

        for (; i < strlen; i++)
        {
            c = str.charAt(i);
            if ((c >= 0x0001) && (c <= 0x007F))
            {
                bytembufarr[byteCount++] = (byte) c;

            } else if (c > 0x07FF)
            {
                bytembufarr[byteCount++] = (byte) (0xE0 | ((c >> 12) & 0x0F));
                bytembufarr[byteCount++] = (byte) (0x80 | ((c >> 6) & 0x3F));
                bytembufarr[byteCount++] = (byte) (0x80 | ((c) & 0x3F));
            } else
            {
                bytembufarr[byteCount++] = (byte) (0xC0 | ((c >> 6) & 0x1F));
                bytembufarr[byteCount++] = (byte) (0x80 | ((c) & 0x3F));
            }
        }
        write(bytembufarr, 0, utflen);
    }

    @Override
    public void writeUTF(String str) throws IOException {
        if (str == null || str.isEmpty())
        {
            writeShort(0);
            return;
        }
        int strlen = str.length();
        int utflen = 0;
        int c, byteCount = 0;

        /* use charAt instead of copying String to char array */
        for (int i = 0; i < strlen; i++)
        {
            c = str.charAt(i);
            if ((c >= 0x0001) && (c <= 0x007F))
            {
                utflen++;
            } else if (c > 0x07FF)
            {
                utflen += 3;
            } else
            {
                utflen += 2;
            }
        }

        if (utflen > 65535)
        {
            throw new UTFDataFormatException("encoded string too long: " + utflen + " bytes");
        }
        byte[] bytembufarr;

        if (bytearr == null || (bytearr.length < (utflen + 2)))
        {
            bytearr = new byte[(utflen * 2) + 2];
        }
        bytembufarr = bytearr;
        bytembufarr[byteCount++] = (byte) ((utflen >>> 8) & 0xFF);
        bytembufarr[byteCount++] = (byte) ((utflen) & 0xFF);
        int i;
        for (i = 0; i < strlen; i++)
        {
            c = str.charAt(i);
            if (!((c >= 0x0001) && (c <= 0x007F)))
            {
                break;
            }
            bytembufarr[byteCount++] = (byte) c;
        }

        for (; i < strlen; i++)
        {
            c = str.charAt(i);
            if ((c >= 0x0001) && (c <= 0x007F))
            {
                bytembufarr[byteCount++] = (byte) c;

            } else if (c > 0x07FF)
            {
                bytembufarr[byteCount++] = (byte) (0xE0 | ((c >> 12) & 0x0F));
                bytembufarr[byteCount++] = (byte) (0x80 | ((c >> 6) & 0x3F));
                bytembufarr[byteCount++] = (byte) (0x80 | ((c) & 0x3F));
            } else
            {
                bytembufarr[byteCount++] = (byte) (0xC0 | ((c >> 6) & 0x1F));
                bytembufarr[byteCount++] = (byte) (0x80 | ((c) & 0x3F));
            }
        }
        write(bytembufarr, 0, utflen + 2);
    }

    /**
     * The index of the next character to read from the input stream buffer.
     * This value should always be nonnegative and not larger than the value of
     * <code>count</code>. The next byte to be read from the input stream buffer
     * will be <code>buf[pos]</code>.
     */
    protected int pos;

    /**
     * The currently marked position in the stream. ByteArrayInputStream objects
     * are marked at position zero by default when constructed. They may be
     * marked at another position within the buffer by the <code>mark()</code>
     * method. The current buffer position is set to this point by the
     * <code>reset()</code> method.
     * <p>
     * If no mark has been set, then the value of mark is the offset passed to
     * the constructor (or 0 if the offset was not supplied).
     *
     * @since JDK1.1
     */
    protected int mark = 0;

    /**
     * Reads the next byte of data from this input stream. The value byte is
     * returned as an <code>int</code> in the range <code>0</code> to
     * <code>255</code>. If no byte is available because the end of the stream
     * has been reached, the value <code>-1</code> is returned.
     * <p>
     * This <code>read</code> method cannot block.
     *
     * @return the next byte of data, or <code>-1</code> if the end of the
     * stream has been reached.
     */
    public int read() {
        return (pos < count) ? (buf[pos++] & 0xff) : -1;
    }

    /**
     * Reads up to <code>len</code> bytes of data into an array of bytes from
     * this input stream. If <code>pos</code> equals <code>count</code>, then
     * <code>-1</code> is returned to indicate end of file. Otherwise, the
     * number <code>k</code> of bytes read is equal to the smaller of
     * <code>len</code> and <code>count-pos</code>. If <code>k</code> is
     * positive, then bytes <code>buf[pos]</code> through
     * <code>buf[pos+k-1]</code> are copied into <code>b[off]</code> through
     * <code>b[off+k-1]</code> in the manner performed by
     * <code>System.arraycopy</code>. The value <code>k</code> is added into
     * <code>pos</code> and <code>k</code> is returned.
     * <p>
     * This <code>read</code> method cannot block.
     *
     * @param b the buffer into which the data is read.
     * @param off the start offset in the destination array <code>b</code>
     * @param len the maximum number of bytes read.
     * @return the total number of bytes read into the buffer, or
     * <code>-1</code> if there is no more data because the end of the stream
     * has been reached.
     * @exception NullPointerException If <code>b</code> is <code>null</code>.
     * @exception IndexOutOfBoundsException If <code>off</code> is negative,
     * <code>len</code> is negative, or <code>len</code> is greater than
     * <code>b.length - off</code>
     */
    public int read(byte b[], int off, int len) {
        if (off < 0 || len < 0 || len > b.length - off)
        {
            throw new IndexOutOfBoundsException();
        }

        if (pos >= count)
        {
            return -1;
        }

        int avail = count - pos;
        if (len > avail)
        {
            len = avail;
        }
        if (len <= 0)
        {
            return 0;
        }
        System.arraycopy(buf, pos, b, off, len);
        pos += len;
        return len;
    }

    public final int read(byte b[]) {
        return read(b, 0, b.length);
    }

    public long skip(long n) {
        long k = count - pos;
        if (n < k)
        {
            k = n < 0 ? 0 : n;
        }
        pos += k;
        return k;
    }

    @Override
    public void readFully(byte[] b) throws IOException {
        readFully(b, 0, b.length);
    }

    @Override
    public void readFully(byte[] b, int off, int len) throws IOException {
        if (len < 0)
        {
            throw new IndexOutOfBoundsException();
        }
        int n = 0;
        while (n < len)
        {
            int readLen = read(b, off + n, len - n);
            if (readLen < 0)
            {
                throw new EOFException();
            }
            n += readLen;
        }
    }

    @Override
    public int skipBytes(int n) throws IOException {
        int total = 0;
        int cur = 0;

        while ((total < n) && ((cur = (int) skip(n - total)) > 0))
        {
            total += cur;
        }

        return total;
    }

    @Override
    public boolean readBoolean() throws IOException {
        int ch = read();
        if (ch < 0)
        {
            throw new EOFException();
        }
        return (ch != 0);
    }

    @Override
    public byte readByte() throws IOException {
        int ch = read();
        if (ch < 0)
        {
            throw new EOFException();
        }
        return (byte) (ch);
    }

    @Override
    public int readUnsignedByte() throws IOException {
        int ch = read();
        if (ch < 0)
        {
            throw new EOFException();
        }
        return ch;
    }

    @Override
    public short readShort() throws IOException {
        int ch1 = read();
        int ch2 = read();
        if ((ch1 | ch2) < 0)
        {
            throw new EOFException();
        }
        return (short) ((ch1 << 8) + (ch2));
    }

    public short readLShort() throws IOException {
        int ch1 = read();
        int ch2 = read();
        if ((ch1 | ch2) < 0)
        {
            throw new EOFException();
        }
        return (short) ((ch2 << 8) + (ch1));
    }

    @Override
    public int readUnsignedShort() throws IOException {
        int ch1 = read();
        int ch2 = read();
        if ((ch1 | ch2) < 0)
        {
            throw new EOFException();
        }
        return (ch1 << 8) + (ch2);
    }

    public int readUnsignedShortNoCheck() {
        int ch1 = read();
        int ch2 = read();
        return (ch1 << 8) | (ch2);
    }

    @Override
    public char readChar() throws IOException {
        int ch1 = read();
        int ch2 = read();
        if ((ch1 | ch2) < 0)
        {
            throw new EOFException();
        }
        return (char) ((ch1 << 8) + (ch2));
    }

    @Override
    public int readInt() throws IOException {
        if (count - pos < 4)
        {
            pos = count;
            throw new EOFException();
        }
        int ch1 = buf[pos++] & 0xff;
        int ch2 = buf[pos++] & 0xff;
        int ch3 = buf[pos++] & 0xff;
        int ch4 = buf[pos++] & 0xff;
        return ((ch1 << 24) | (ch2 << 16) | (ch3 << 8) | (ch4));
    }

    @Override
    public long readLong() throws IOException {
        byte[] readBuffer = writeBuffer;
        readFully(readBuffer, 0, 8);
        return (((long) readBuffer[0] << 56)
                + ((long) (readBuffer[1] & 255) << 48)
                + ((long) (readBuffer[2] & 255) << 40)
                + ((long) (readBuffer[3] & 255) << 32)
                + ((long) (readBuffer[4] & 255) << 24)
                + ((readBuffer[5] & 255) << 16)
                + ((readBuffer[6] & 255) << 8)
                + ((readBuffer[7] & 255)));
    }

    @Override
    public float readFloat() throws IOException {
        return Float.intBitsToFloat(readInt());
    }

    @Override
    public double readDouble() throws IOException {
        return Double.longBitsToDouble(readLong());
    }

    @Override
    public String readLine() throws IOException {
        if (this.available() > 0)
        {
            int off = pos;
            int end = count;
            byte[] b = buf;
            int i = off;
            loop:
            for (; i < end; i++)
            {
                switch (b[i])
                {
                    case '\r':
                        if ((i + 1) < end && b[i + 1] == '\n')
                        {
                            end = i + 2;
                        } else
                        {
                            end = i + 1;
                        }
                        break loop;
                    case '\n':
                        end = i + 1;
                        break loop;
                    default:
                        break;
                }
            }
            pos = end;
            int len = i - off;
            if (len < 1)
            {
                return "";
            }
            return new String(b, off, len);
        }
        return null;
    }

    public byte[] readLineData() {
        if (this.available() > 0)
        {
            int off = pos;
            int end = count;
            byte[] b = buf;
            loop:
            for (int i = off; i < end; i++)
            {
                switch (b[i])
                {
                    case '\r':
                        if ((i + 1) < end && b[i + 1] == '\n')
                        {
                            end = i + 2;
                        } else
                        {
                            end = i + 1;
                        }
                        break loop;
                    case '\n':
                        end = i + 1;
                        break loop;
                    default:
                        break;
                }
            }
            pos = end;
            //   Log.d(TAG, "end=%d off=%d b[end]=%x", end, off, b[end] & 0xff);
            byte[] ret = new byte[end - off];
            System.arraycopy(b, off, ret, 0, ret.length);
            return ret;
        }
        return null;
    }

    @Override
    public String readUTF() throws IOException {
        int utflen = readUnsignedShort();
        if (utflen <= 0)
        {
            return "";
        }
        byte[] mbytearr;
        char[] mchararr;

        if (this.bytearr == null || this.bytearr.length < utflen)
        {
            int size = Math.max(utflen * 2, 80);
            this.bytearr = new byte[size];
        }
        if (this.chararr == null || this.chararr.length < this.bytearr.length)
        {
            this.chararr = new char[this.bytearr.length];
        }
        mchararr = this.chararr;
        mbytearr = this.bytearr;

        int c, char2, char3;
        int byteCount = 0;
        int chararr_count = 0;
        readFully(mbytearr, 0, utflen);
        try
        {
            while (byteCount < utflen)
            {
                c = (int) mbytearr[byteCount] & 0xff;
                if (c > 127)
                {
                    break;
                }
                byteCount++;
                mchararr[chararr_count++] = (char) c;
            }
            while (byteCount < utflen)
            {
                c = (int) mbytearr[byteCount] & 0xff;
                switch (c >> 4)
                {
                    case 0:
                    case 1:
                    case 2:
                    case 3:
                    case 4:
                    case 5:
                    case 6:
                    case 7:
                        /* 0xxxxxxx*/
                        byteCount++;
                        mchararr[chararr_count++] = (char) c;
                        break;
                    case 12:
                    case 13:
                        /* 110x xxxx   10xx xxxx*/
                        byteCount += 2;
                        if (byteCount > utflen)
                        {
                            throw new UTFDataFormatException(
                                    "malformed input: partial character at end");
                        }
                        char2 = (int) mbytearr[byteCount - 1];
                        if ((char2 & 0xC0) != 0x80)
                        {
                            throw new UTFDataFormatException(
                                    "malformed input around byte " + byteCount);
                        }
                        mchararr[chararr_count++] = (char) (((c & 0x1F) << 6)
                                | (char2 & 0x3F));
                        break;
                    case 14:
                        /* 1110 xxxx  10xx xxxx  10xx xxxx */
                        byteCount += 3;
                        if (byteCount > utflen)
                        {
                            throw new UTFDataFormatException(
                                    "malformed input: partial character at end");
                        }
                        char2 = (int) mbytearr[byteCount - 2];
                        char3 = (int) mbytearr[byteCount - 1];
                        if (((char2 & 0xC0) != 0x80) || ((char3 & 0xC0) != 0x80))
                        {
                            throw new UTFDataFormatException(
                                    "malformed input around byte " + (byteCount - 1));
                        }
                        mchararr[chararr_count++] = (char) (((c & 0x0F) << 12)
                                | ((char2 & 0x3F) << 6)
                                | ((char3 & 0x3F)));
                        break;
                    default:
                        /* 10xx xxxx,  1111 xxxx */
                        throw new UTFDataFormatException(
                                "malformed input around byte " + byteCount);
                }
            }
        } catch (Throwable e)
        {
            return new String(mbytearr, 0, utflen, "UTF-8");
        }
        // The number of chars produced may be less than utflen
        return new String(mchararr, 0, chararr_count);
    }

    public int getPos() {
        return pos;
    }

    public int available() {
        return count - pos;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public void setPos(int pos) {
        this.pos = pos;
    }

    public void setData(byte[] data) {
        this.buf = data;
        this.count = data.length;
        this.pos = 0;
    }

    public int readLInt() throws EOFException {
        if (count - pos < 4)
        {
            pos = count;
            throw new EOFException();
        }
        int ch1 = buf[pos++] & 0xff;
        int ch2 = buf[pos++] & 0xff;
        int ch3 = buf[pos++] & 0xff;
        int ch4 = buf[pos++] & 0xff;
        return ((ch4 << 24) | (ch3 << 16) | (ch2 << 8) | (ch1));
    }

    public long readLLong() throws IOException {
        byte[] readBuffer = writeBuffer;
        readFully(readBuffer, 0, 8);
        return (((long) readBuffer[7] << 56)
                + ((long) (readBuffer[6] & 255) << 48)
                + ((long) (readBuffer[5] & 255) << 40)
                + ((long) (readBuffer[4] & 255) << 32)
                + ((long) (readBuffer[3] & 255) << 24)
                + ((readBuffer[2] & 255) << 16)
                + ((readBuffer[1] & 255) << 8)
                + ((readBuffer[0] & 255)));
    }

    public String readGbk() throws IOException {
        int utflen = readUnsignedShort();
        byte[] mbytearr;
        if (this.bytearr == null || this.bytearr.length < utflen)
        {
            int size = Math.max(utflen, 80);
            this.bytearr = new byte[size];
        }
        mbytearr = this.bytearr;
        readFully(mbytearr, 0, utflen);
        return new String(mbytearr, 0, utflen, "GBK");
    }

    public int readUnsignedLShort() throws IOException {
        int ch1 = read();
        int ch2 = read();
        if ((ch1 | ch2) < 0)
        {
            throw new EOFException();
        }
        return (ch2 << 8) + (ch1);
    }

    public void writeLongUTF(String str) {
        if (str == null || str.isEmpty())
        {
            writeInt(0);
            return;
        }
        int strlen = str.length();
        int utflen = 0;
        int c, byteCount = 0;

        /* use charAt instead of copying String to char array */
        for (int i = 0; i < strlen; i++)
        {
            c = str.charAt(i);
            if ((c >= 0x0001) && (c <= 0x007F))
            {
                utflen++;
            } else if (c > 0x07FF)
            {
                utflen += 3;
            } else
            {
                utflen += 2;
            }
        }
        byte[] bytembufarr;
        if (bytearr == null || (bytearr.length < (utflen + 2)))
        {
            bytearr = new byte[(utflen * 2) + 2];
        }
        bytembufarr = bytearr;
        writeInt(utflen);
        int i;
        for (i = 0; i < strlen; i++)
        {
            c = str.charAt(i);
            if (!((c >= 0x0001) && (c <= 0x007F)))
            {
                break;
            }
            bytembufarr[byteCount++] = (byte) c;
        }

        for (; i < strlen; i++)
        {
            c = str.charAt(i);
            if ((c >= 0x0001) && (c <= 0x007F))
            {
                bytembufarr[byteCount++] = (byte) c;
            } else if (c > 0x07FF)
            {
                bytembufarr[byteCount++] = (byte) (0xE0 | ((c >> 12) & 0x0F));
                bytembufarr[byteCount++] = (byte) (0x80 | ((c >> 6) & 0x3F));
                bytembufarr[byteCount++] = (byte) (0x80 | ((c) & 0x3F));
            } else
            {
                bytembufarr[byteCount++] = (byte) (0xC0 | ((c >> 6) & 0x1F));
                bytembufarr[byteCount++] = (byte) (0x80 | ((c) & 0x3F));
            }
        }
        write(bytembufarr, 0, utflen);
    }

    public String readLongUTF() throws IOException {
        int utflen = readInt();
        byte[] mbytearr;
        char[] mchararr;

        if (this.bytearr == null || this.bytearr.length < utflen)
        {
            int size = Math.max(utflen * 2, 80);
            this.bytearr = new byte[size];
        }
        if (this.chararr == null || this.chararr.length < this.bytearr.length)
        {
            this.chararr = new char[this.bytearr.length];
        }
        mchararr = this.chararr;
        mbytearr = this.bytearr;

        int c, char2, char3;
        int byteCount = 0;
        int chararr_count = 0;

        readFully(mbytearr, 0, utflen);

        while (byteCount < utflen)
        {
            c = (int) mbytearr[byteCount] & 0xff;
            if (c > 127)
            {
                break;
            }
            byteCount++;
            mchararr[chararr_count++] = (char) c;
        }

        while (byteCount < utflen)
        {
            c = (int) mbytearr[byteCount] & 0xff;
            switch (c >> 4)
            {
                case 0:
                case 1:
                case 2:
                case 3:
                case 4:
                case 5:
                case 6:
                case 7:
                    /* 0xxxxxxx*/
                    byteCount++;
                    mchararr[chararr_count++] = (char) c;
                    break;
                case 12:
                case 13:
                    /* 110x xxxx   10xx xxxx*/
                    byteCount += 2;
                    if (byteCount > utflen)
                    {
                        throw new UTFDataFormatException(
                                "malformed input: partial character at end");
                    }
                    char2 = (int) mbytearr[byteCount - 1];
                    if ((char2 & 0xC0) != 0x80)
                    {
                        throw new UTFDataFormatException(
                                "malformed input around byte " + byteCount);
                    }
                    mchararr[chararr_count++] = (char) (((c & 0x1F) << 6)
                            | (char2 & 0x3F));
                    break;
                case 14:
                    /* 1110 xxxx  10xx xxxx  10xx xxxx */
                    byteCount += 3;
                    if (byteCount > utflen)
                    {
                        throw new UTFDataFormatException(
                                "malformed input: partial character at end");
                    }
                    char2 = (int) mbytearr[byteCount - 2];
                    char3 = (int) mbytearr[byteCount - 1];
                    if (((char2 & 0xC0) != 0x80) || ((char3 & 0xC0) != 0x80))
                    {
                        throw new UTFDataFormatException(
                                "malformed input around byte " + (byteCount - 1));
                    }
                    mchararr[chararr_count++] = (char) (((c & 0x0F) << 12)
                            | ((char2 & 0x3F) << 6)
                            | ((char3 & 0x3F)));
                    break;
                default:
                    /* 10xx xxxx,  1111 xxxx */
                    throw new UTFDataFormatException(
                            "malformed input around byte " + byteCount);
            }
        }
        // The number of chars produced may be less than utflen
        return new String(mchararr, 0, chararr_count);
    }

    public void writeInt(int v, int off) {
        buf[off] = (byte) (v >>> 24);
        buf[off + 1] = (byte) (v >>> 16);
        buf[off + 2] = (byte) (v >>> 8);
        buf[off + 3] = (byte) (v);
    }

    /**
     * Encode and write a varint.
     *
     * @param value
     * @throws IOException
     */
    public void writeInt64(long value) throws IOException {
        while (true)
        {
            if ((value & ~0x7FL) == 0)
            {
                write((int) value);
                return;
            } else
            {
                write(((int) value & 0x7F) | 0x80);
                value >>>= 7;
            }
        }
    }

    /**
     * Encode and write a varint. {@code value} is treated as unsigned, so it
     * won't be sign-extended if negative.
     *
     * @param value
     * @throws IOException
     */
    public void writeInt32(int value) throws IOException {
        while (true)
        {
            if ((value & ~0x7F) == 0)
            {
                write(value);
                return;
            } else
            {
                write((value & 0x7F) | 0x80);
                value >>>= 7;
            }
        }
    }

    /**
     * Read a raw Varint from the stream. If larger than 32 bits, discard the
     * upper bits.
     *
     * @return
     * @throws IOException
     */
    public int readInt32() throws IOException {
        byte tmp = readByte();
        if (tmp >= 0)
        {
            return tmp;
        }
        int result = tmp & 0x7f;
        if ((tmp = readByte()) >= 0)
        {
            result |= tmp << 7;
        } else
        {
            result |= (tmp & 0x7f) << 7;
            if ((tmp = readByte()) >= 0)
            {
                result |= tmp << 14;
            } else
            {
                result |= (tmp & 0x7f) << 14;
                if ((tmp = readByte()) >= 0)
                {
                    result |= tmp << 21;
                } else
                {
                    result |= (tmp & 0x7f) << 21;
                    result |= (tmp = readByte()) << 28;
                    if (tmp < 0)
                    {
                        // Discard upper 32 bits.
                        for (int i = 0; i < 5; i++)
                        {
                            if (readByte() >= 0)
                            {
                                return result;
                            }
                        }
                        throw new IOException("Invalid Varint32");
                    }
                }
            }
        }
        return result;
    }

    /**
     * Read a raw Varint from the stream.
     *
     * @return
     * @throws IOException
     */
    public long readInt64() throws IOException {
        int shift = 0;
        long result = 0;
        while (shift < 64)
        {
            final byte b = readByte();
            result |= (long) (b & 0x7F) << shift;
            if ((b & 0x80) == 0)
            {
                return result;
            }
            shift += 7;
        }
        throw new IOException("Invalid Varint64");
    }

    /**
     * Read an {@code sint32} field value from the stream.
     *
     * @return
     * @throws IOException
     */
    public int readSInt32() throws IOException {
        return decodeZigZag32(readInt32());
    }

    /**
     * Read an {@code sint64} field value from the stream.
     *
     * @return
     * @throws IOException
     */
    public long readSInt64() throws IOException {
        return decodeZigZag64(readInt64());
    }

    /**
     * Write an {@code sint32} field to the stream.
     *
     * @param value
     * @throws IOException
     */
    public void writeSInt32(final int value) throws IOException {
        writeInt32(encodeZigZag32(value));
    }

    /**
     * Write an {@code sint64} field to the stream.
     *
     * @param value
     * @throws IOException
     */
    public void writeSInt64(final long value) throws IOException {
        writeInt64(encodeZigZag64(value));
    }

    public void writeBoolean(Boolean v) throws IOException {
        writeBoolean(v != null && v);
    }

    public void writeByte(Byte v) throws IOException {
        write(v == null ? 0 : v);
    }

    public void writeShort(Short v) throws IOException {
        writeShort(v == null ? 0 : v.intValue());
    }

    public void writeChar(Character v) throws IOException {
        writeShort(v == null ? 0 : v);
    }

    public void writeInt(Integer v) throws IOException {
        writeInt(v == null ? 0 : v);
    }

    public void writeLong(Long v) throws IOException {
        writeLong(v == null ? 0 : v);
    }

    public void writeFloat(Float v) throws IOException {
        writeFloat(v == null ? 0 : v);
    }

    public void writeDouble(Double v) throws IOException {
        writeDouble(v == null ? 0 : v);
    }

    /**
     * Decode a ZigZag-encoded 32-bit value. ZigZag encodes signed integers into
     * values that can be efficiently encoded with varint. (Otherwise, negative
     * values must be sign-extended to 64 bits to be varint encoded, thus always
     * taking 10 bytes on the wire.)
     *
     * @param n An unsigned 32-bit integer, stored in a signed int because Java
     * has no explicit unsigned support.
     * @return A signed 32-bit integer.
     */
    public static int decodeZigZag32(final int n) {
        return (n >>> 1) ^ -(n & 1);
    }

    /**
     * Decode a ZigZag-encoded 64-bit value. ZigZag encodes signed integers into
     * values that can be efficiently encoded with varint. (Otherwise, negative
     * values must be sign-extended to 64 bits to be varint encoded, thus always
     * taking 10 bytes on the wire.)
     *
     * @param n An unsigned 64-bit integer, stored in a signed int because Java
     * has no explicit unsigned support.
     * @return A signed 64-bit integer.
     */
    public static long decodeZigZag64(final long n) {
        return (n >>> 1) ^ -(n & 1);
    }

    /**
     * Encode a ZigZag-encoded 32-bit value. ZigZag encodes signed integers into
     * values that can be efficiently encoded with varint. (Otherwise, negative
     * values must be sign-extended to 64 bits to be varint encoded, thus always
     * taking 10 bytes on the wire.)
     *
     * @param n A signed 32-bit integer.
     * @return An unsigned 32-bit integer, stored in a signed int because Java
     * has no explicit unsigned support.
     */
    public static int encodeZigZag32(final int n) {
        // Note:  the right-shift must be arithmetic
        return (n << 1) ^ (n >> 31);
    }

    /**
     * Encode a ZigZag-encoded 64-bit value. ZigZag encodes signed integers into
     * values that can be efficiently encoded with varint. (Otherwise, negative
     * values must be sign-extended to 64 bits to be varint encoded, thus always
     * taking 10 bytes on the wire.)
     *
     * @param n A signed 64-bit integer.
     * @return An unsigned 64-bit integer, stored in a signed int because Java
     * has no explicit unsigned support.
     */
    public static long encodeZigZag64(final long n) {
        // Note:  the right-shift must be arithmetic
        return (n << 1) ^ (n >> 63);
    }

}
