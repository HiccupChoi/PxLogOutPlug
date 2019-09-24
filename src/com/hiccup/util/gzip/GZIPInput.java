package com.hiccup.util.gzip;

import com.hiccup.tools.IPool;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;
import java.util.zip.ZipException;

/**
 *
 * @author chen
 */
public class GZIPInput extends InputStream implements IPool {

    @SuppressWarnings("unused")
    private final static String TAG = "GZIPInput";
    /*
     * GZIP header magic number.
     */
    private final static int GZIP_MAGIC = 0x8b1f;
    /**
     * CRC-32 for uncompressed data.
     */
    private final CRC32 crc = new CRC32();

    /**
     * Indicates end of input stream.
     */
    protected boolean eos;

    /**
     * Decompressor for this stream.
     */
    private final Inflater inf;

    /**
     * Input buffer for decompression.
     */
    protected byte[] buf;

    /**
     * Length of input buffer.
     */
    protected int len;

    private boolean closed = false;

    /**
     * The input stream to be filtered.
     */
    private InputStream in;
    
    private volatile boolean busy = false;

    public GZIPInput() {
        buf = new byte[1024];
        inf = new Inflater(true);
    }

    public GZIPInput(InputStream in) throws IOException {
        this.in = in;
        buf = new byte[1024];
        readHeader(in);
        inf = new Inflater(true);
    }

    public InputStream getIn() {
        return in;
    }

    public void setIn(InputStream in) throws IOException {
        this.in = in;
        readHeader(in);
        eos = false;
        inf.reset();
    }

    /*
     * File header flags.
     */
    private final static int FHCRC = 2;    // Header CRC
    private final static int FEXTRA = 4;    // Extra field
    private final static int FNAME = 8;    // File name
    private final static int FCOMMENT = 16;   // File comment
      /*
     * Reads GZIP member header and returns the total byte number
     * of this member header.
     */

    private int readHeader(InputStream this_in) throws IOException {
        CheckedInputStream cin = new CheckedInputStream(this_in, crc);
        crc.reset();
        // Check header magic
        if (readUShort(cin) != GZIP_MAGIC)
        {
            throw new ZipException("Not in GZIP format");
        }
        // Check compression method
        if (readUByte(cin) != 8)
        {
            throw new ZipException("Unsupported compression method");
        }
        // Read flags
        int flg = readUByte(cin);
        // Skip MTIME, XFL, and OS fields
        skipBytes(cin, 6);
        int n = 2 + 2 + 6;
        // Skip optional extra field
        if ((flg & FEXTRA) == FEXTRA)
        {
            int m = readUShort(cin);
            skipBytes(cin, m);
            n += m + 2;
        }
        // Skip optional file name
        if ((flg & FNAME) == FNAME)
        {
            do
            {
                n++;
            } while (readUByte(cin) != 0);
        }
        // Skip optional file comment
        if ((flg & FCOMMENT) == FCOMMENT)
        {
            do
            {
                n++;
            } while (readUByte(cin) != 0);
        }
        // Check optional header CRC
        if ((flg & FHCRC) == FHCRC)
        {
            int v = (int) crc.getValue() & 0xffff;
            if (readUShort(cin) != v)
            {
                throw new ZipException("Corrupt GZIP header");
            }
            n += 2;
        }
        crc.reset();
        return n;
    }

    /*
     * Reads GZIP member trailer and returns true if the eos
     * reached, false if there are more (concatenated gzip
     * data set)
     */
    private boolean readTrailer() throws IOException {
        InputStream mIn = this.in;
        int n = inf.getRemaining();
        if (n > 0)
        {
            mIn = new SequenceInputStream(
                    new ByteArrayInputStream(buf, len - n, n), mIn);
        }
        // Uses left-to-right evaluation order
        if ((readUInt(mIn) != crc.getValue())
                || // rfc1952; ISIZE is the input size modulo 2^32
                (readUInt(mIn) != (inf.getBytesWritten() & 0xffffffffL)))
        {
            throw new ZipException("Corrupt GZIP trailer");
        }

        // If there are more bytes available in "in" or
        // the leftover in the "inf" is > 26 bytes:
        // this.trailer(8) + next.header.min(10) + next.trailer(8)
        // try concatenated case
        if (this.in.available() > 0 || n > 26)
        {
            int m = 8;                  // this.trailer
            try
            {
                m += readHeader(mIn);    // next.header
            } catch (IOException ze)
            {
                return true;  // ignore any malformed, do nothing
            }
            inf.reset();
            if (n > m)
            {
                inf.setInput(buf, len - n + m, n - m);
            }
            return false;
        }
        return true;
    }

    /*
     * Skips bytes of input data blocking until all bytes are skipped.
     * Does not assume that the input stream is capable of seeking.
     */
    private void skipBytes(InputStream in, int n) throws IOException {
        while (n > 0)
        {
            int readLen = in.read(buf, 0, n < buf.length ? n : buf.length);
            if (readLen == -1)
            {
                throw new EOFException();
            }
            n -= readLen;
        }
    }

    /*
     * Reads unsigned integer in Intel byte order.
     */
    private long readUInt(InputStream in) throws IOException {
        long s = readUShort(in);
        return ((long) readUShort(in) << 16) | s;
    }

    /*
     * Reads unsigned short in Intel byte order.
     */
    private int readUShort(InputStream in) throws IOException {
        int b = readUByte(in);
        return ((int) readUByte(in) << 8) | b;
    }

    /*
     * Reads unsigned byte.
     */
    private int readUByte(InputStream in) throws IOException {
        int b = in.read();
        if (b == -1)
        {
            throw new EOFException();
        }
        if (b < -1 || b > 255)
        {
            // Report on this.in, not argument in; see read{Header, Trailer}.
            throw new IOException(this.in.getClass().getName()
                    + ".read() returned value out of range -1..255: " + b);
        }
        return b;
    }

    @Override
    public void close() throws IOException {
        if (!closed)
        {
            try {
                inf.end();
                if (in != null) {
                    in.close();
                }
            } finally {
                in = null;
            }
            closed = true;
        }
    }

    /**
     * Reads uncompressed data into an array of bytes. If <code>len</code> is
     * not zero, the method will block until some input can be decompressed;
     * otherwise, no bytes are read and <code>0</code> is returned.
     *
     * @param buf the buffer into which the data is read
     * @param off the start offset in the destination array <code>b</code>
     * @param len the maximum number of bytes read
     * @return the actual number of bytes read, or -1 if the end of the
     * compressed input stream is reached
     *
     * @exception NullPointerException If <code>buf</code> is <code>null</code>.
     * @exception IndexOutOfBoundsException If <code>off</code> is negative,
     * <code>len</code> is negative, or <code>len</code> is greater than
     * <code>buf.length - off</code>
     * @exception ZipException if the compressed input data is corrupt.
     * @exception IOException if an I/O error has occurred.
     *
     */
    @Override
    public int read(byte[] buf, int off, int len) throws IOException {
        ensureOpen();
        if (eos)
        {
            return -1;
        }
        int n = readInf(buf, off, len);
        if (n == -1)
        {
            if (readTrailer())
            {
                eos = true;
            } else
            {
                return this.read(buf, off, len);
            }
        } else
        {
            crc.update(buf, off, n);
        }
        return n;
    }

    /**
     * Reads uncompressed data into an array of bytes. If <code>len</code> is
     * not zero, the method will block until some input can be decompressed;
     * otherwise, no bytes are read and <code>0</code> is returned.
     *
     * @param b the buffer into which the data is read
     * @param off the start offset in the destination array <code>b</code>
     * @param len the maximum number of bytes read
     * @return the actual number of bytes read, or -1 if the end of the
     * compressed input is reached or a preset dictionary is needed
     * @exception NullPointerException If <code>b</code> is <code>null</code>.
     * @exception IndexOutOfBoundsException If <code>off</code> is negative,
     * <code>len</code> is negative, or <code>len</code> is greater than
     * <code>b.length - off</code>
     * @exception ZipException if a ZIP format error has occurred
     * @exception IOException if an I/O error has occurred
     */
    public int readInf(byte[] b, int off, int len) throws IOException {
        if (b == null)
        {
            throw new NullPointerException();
        } else if (off < 0 || len < 0 || len > b.length - off)
        {
            throw new IndexOutOfBoundsException();
        } else if (len == 0)
        {
            return 0;
        }
        try
        {
            int n;
            while ((n = inf.inflate(b, off, len)) == 0)
            {
                if (inf.finished() || inf.needsDictionary())
                {
                    return -1;
                }
                if (inf.needsInput())
                {
                    fill();
                }
            }
            return n;
        } catch (DataFormatException e)
        {
            String s = e.getMessage();
            throw new ZipException(s != null ? s : "Invalid ZLIB data format");
        }
    }

    /**
     * Fills input buffer with more data to decompress.
     *
     * @exception IOException if an I/O error has occurred
     */
    protected void fill() throws IOException {
        ensureOpen();
        len = in.read(buf, 0, buf.length);
        if (len == -1)
        {
            throw new EOFException("Unexpected end of ZLIB input stream");
        }
        inf.setInput(buf, 0, len);
    }

    /**
     * Check to make sure that this stream has not been closed
     */
    private void ensureOpen() throws IOException {
        if (closed)
        {
            throw new IOException("Stream closed");
        }
    }

    @Override
    public int read() throws IOException {
        throw new RuntimeException("Not supported yet.");
    }

    @Override
    public boolean isBusy() {
        return busy;
    }

    @Override
    public void setBusy(boolean busy) {
        this.busy = busy;
    }

}
