package com.hiccup.util.gzip;



import com.hiccup.tools.IPool;
import com.hiccup.util.AccessOut;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.CRC32;
import java.util.zip.Deflater;

/**
 *
 * @author chen
 */
public class GZIPOutput implements Closeable, IPool {

    @SuppressWarnings("unused")
    private final static String TAG = "GZIPOutput";
    /**
     * CRC-32 of uncompressed data.
     */
    private final CRC32 crc = new CRC32();

    /*
     * GZIP header magic number.
     */
    private final static int GZIP_MAGIC = 0x8b1f;

    /*
     * Trailer size in bytes.
     *
     */
    private final static int TRAILER_SIZE = 8;
    private OutputStream out;
    /**
     * Compressor for this stream.
     */
    private final Deflater def;

    /**
     * Output buffer for writing compressed data.
     */
    private final byte[] buf;

    private boolean closed = false;

    private volatile boolean busy = false;

    public GZIPOutput() {
        def = new Deflater(Deflater.DEFAULT_COMPRESSION, true);
        this.buf = new byte[1024];
    }

    public GZIPOutput(AccessOut out) throws IOException {
        this.out = out;
        writeHeader();
        def = new Deflater(Deflater.DEFAULT_COMPRESSION, true);
        this.buf = new byte[1024];
    }

    public OutputStream getOut() {
        return out;
    }

    public final void setOut(OutputStream out) throws IOException {
        this.out = out;
        writeHeader();
        this.crc.reset();
        this.def.reset();
    }

    /**
     * Writes array of bytes to the compressed output stream. This method will
     * block until all the bytes are written.
     *
     * @param buf the data to be written
     * @param off the start offset of the data
     * @param len the length of the data
     * @exception IOException If an I/O error has occurred.
     */
    public void write(byte[] buf, int off, int len)
            throws IOException {
        writeDef(buf, off, len);
        crc.update(buf, off, len);
    }

    /**
     * Writes an array of bytes to the compressed output stream. This method
     * will block until all the bytes are written.
     *
     * @param b the data to be written
     * @param off the start offset of the data
     * @param len the length of the data
     * @exception IOException if an I/O error has occurred
     */
    public void writeDef(byte[] b, int off, int len) throws IOException {
        if (def.finished())
        {
            throw new IOException("write beyond end of stream");
        }
        if ((off | len | (off + len) | (b.length - (off + len))) < 0)
        {
            throw new IndexOutOfBoundsException();
        } else if (len == 0)
        {
            return;
        }
        if (!def.finished())
        {
            def.setInput(b, off, len);
            while (!def.needsInput())
            {
                deflate();
            }
        }
    }

    /**
     * Writes next block of compressed data to the output stream.
     *
     * @throws IOException if an I/O error has occurred
     */
    protected void deflate() throws IOException {
        int len = def.deflate(buf, 0, buf.length);
        if (len > 0)
        {
            out.write(buf, 0, len);
        }
    }

    /*
     * Writes GZIP member header.
     */
    private void writeHeader() throws IOException {
        out.write(new byte[]
        {
            (byte) GZIP_MAGIC, // Magic number (short)
            (byte) (GZIP_MAGIC >> 8), // Magic number (short)
            Deflater.DEFLATED, // Compression method (CM)
            0, // Flags (FLG)
            0, // Modification time MTIME (int)
            0, // Modification time MTIME (int)
            0, // Modification time MTIME (int)
            0, // Modification time MTIME (int)
            0, // Extra flags (XFLG)
            0                         // Operating system (OS)
        });
    }

    /**
     * Finishes writing compressed data to the output stream without closing the
     * underlying stream. Use this method when applying multiple filters in
     * succession to the same output stream.
     *
     * @exception IOException if an I/O error has occurred
     */
    public void finish() throws IOException {
        if (!def.finished())
        {
            def.finish();
            while (!def.finished())
            {
                int len = def.deflate(buf, 0, buf.length);
                if (def.finished() && len <= buf.length - TRAILER_SIZE)
                {
                    // last deflater buffer. Fit trailer at the end
                    writeTrailer(buf, len);
                    len = len + TRAILER_SIZE;
                    out.write(buf, 0, len);
                    return;
                }
                if (len > 0)
                {
                    out.write(buf, 0, len);
                }
            }
            // if we can't fit the trailer at the end of the last
            // deflater buffer, we write it separately
            writeTrailer(buf, 0);
            out.write(buf, 0, TRAILER_SIZE);
        }
    }

    /*
     * Writes GZIP member trailer to a byte array, starting at a given
     * offset.
     */
    private void writeTrailer(byte[] buf, int offset) throws IOException {
        writeInt((int) crc.getValue(), buf, offset); // CRC-32 of uncompr. data
        writeInt(def.getTotalIn(), buf, offset + 4); // Number of uncompr. bytes
    }

    /*
     * Writes integer in Intel byte order to a byte array, starting at a
     * given offset.
     */
    private void writeInt(int i, byte[] buf, int offset) throws IOException {
        writeShort(i & 0xffff, buf, offset);
        writeShort((i >> 16) & 0xffff, buf, offset + 2);
    }

    /*
     * Writes short integer in Intel byte order to a byte array, starting
     * at a given offset
     */
    private void writeShort(int s, byte[] buf, int offset) throws IOException {
        buf[offset] = (byte) (s & 0xff);
        buf[offset + 1] = (byte) ((s >> 8) & 0xff);
    }

    @Override
    public void close() throws IOException {
        if (!closed)
        {
            try {
                finish();
                def.end();
                if (out != null) {
                    out.close();
                }
            } finally {
                out = null;
            }
            closed = true;
        }
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
