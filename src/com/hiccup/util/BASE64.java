package com.hiccup.util;

/**
 *
 * @author chenlong
 */
public class BASE64 {

    private static char getChar(int sixBit) {
        return (char) (sixBit < 26
                ? sixBit + 65
                : sixBit < 52
                        ? sixBit + 71
                        : sixBit < 62
                                ? sixBit - 4
                                : sixBit == 62
                                        ? 43
                                        : sixBit == 63
                                                ? 47
                                                : 65);
    }

    public static byte[] decode(String base64) {
        if (base64 == null || base64.isEmpty())
        {
            return null;
        }
        return decode(base64, 0, base64.length());
    }

    /**
     *
     * @param base64
     * @return
     */
    public static byte[] decode(String base64, int off, int len) {
        //	Log.d("BASE64", "base64=%s", base64);
        if (base64 == null || base64.isEmpty())
        {
            return null;
        }
        // how many padding digits?
        int pad = 0;
        int end = off + len;
        for (int i = end - 1; base64.charAt(i) == '='; i--)
        {
            pad++;
        }
        int l = len;
        // we know know the lenght of the target byte array.
        int length = l * 3 / 4 - pad;
        byte[] raw = new byte[length];
        int rawIndex = 0;
        // loop through the base64 value.  A correctly formed
        // base64 string always has a multiple of 4 characters.
        int mend = end - 3;
        for (int i = off; i < mend; i += 4)
        {
            int block = (getValue(base64.charAt(i)) << 18)
                    + (getValue(base64.charAt(i + 1)) << 12)
                    + (getValue(base64.charAt(i + 2)) << 6)
                    + (getValue(base64.charAt(i + 3)));
            // based on the block, the byte array is filled with the
            // appropriate 8 bit values
            for (int j = 0; j < 3 && rawIndex + j < raw.length; j++)
            {
                raw[rawIndex + j] = (byte) ((block >> (8 * (2 - j))) & 0xff);
            }
            rawIndex += 3;
        }
        return raw;
    }

    /*
     * getValue
     *
     * translates from base64 digits to their 6 bit value
     */
    private static int getValue(char c) {
        return c > 64 && c < 91
                ? c - 65
                : c > 96 && c < 123
                        ? c - 71
                        : c > 47 && c < 58
                                ? c + 4
                                : c == 43
                                        ? 62
                                        : c == 47
                                                ? 63
                                                : 0;
    }

    /**
     *
     * @param raw
     * @return
     */
    public static String encode(AccessOut ao) {
        if (ao == null)
        {
            return "";
        }
        return encode(ao.getBuf(), ao.size());
    }

    /**
     *
     * @param raw
     * @return
     */
    public static String encode(byte[] raw) {
        if (raw == null)
        {
            return "";
        }
        return encode(raw, 0, raw.length);
    }

    /**
     *
     * @param raw
     * @return
     */
    public static String encode(byte[] raw, int len) {
        return encode(raw, 0, len);
    }

    /**
     *
     * @param raw
     * @return
     */
    public static String encode(byte[] raw, int offset, int len) {
        if (raw == null || len < 1)
        {
            return "";
        }
        int calc = (len * 4 / 3);
        if (calc % 4 != 0)
        {
            calc += 4 - calc % 4;
        }
        char encoded[] = new char[calc];
        int off = 0;
        int endOff = offset + len - 1;
        for (int i = 0; i < len; i += 3)
        {
            encodeBlock(raw, endOff, i + offset, encoded, off);
            off += 4;
        }
        return new String(encoded);
    }

    private static void encodeBlock(byte[] raw, int endOff, int offset, char[] base64, int off) {
        int block = 0;
        // how much space left in input byte array
        int slack = endOff - offset;
        // if there are fewer than 3 bytes in this block, calculate end
        int end = (slack >= 2) ? 2 : slack;
        // convert signed quantities into unsigned
        for (int i = 0; i <= end; i++)
        {
            byte b = raw[offset + i];
            int neuter = (b < 0) ? b + 256 : b;
            block += neuter << (8 * (2 - i));
        }

        // extract the base64 digets, which are six bit quantities.
        for (int i = 0; i < 4; i++)
        {
            int sixbit = (block >>> (6 * (3 - i))) & 0x3f;
            base64[i + off] = getChar(sixbit);
        }
        // pad return block if needed
        if (slack < 1)
        {
            base64[off + 2] = '=';
        }
        if (slack < 2)
        {
            base64[off + 3] = '=';
        }
    }

    public static String encodeGzip(AccessOut bout) {
        AccessOut gzip = AccessOut.getFromPool();
        try
        {
            return BASE64.encode(Gzip.encrypt(gzip, bout));
        } finally
        {
            IOTool.safeClose(gzip);
        }
    }

    /**
     *
     * @param raw
     * @return
     */
    public static byte[] encodeByte(byte[] raw) {
        if (raw == null)
        {
            return new byte[0];
        }
        return encodeByte(raw, 0, raw.length);
    }

    /**
     *
     * @param raw
     * @return
     */
    public static byte[] encodeByte(byte[] raw, int offset, int len) {
        if (raw == null || len < 1)
        {
            return new byte[0];
        }
        int calc = (len * 4 / 3);
        if (calc % 4 != 0)
        {
            calc += 4 - calc % 4;
        }
        byte encoded[] = new byte[calc];
        int off = 0;
        int endOff = offset + len - 1;
        for (int i = 0; i < len; i += 3)
        {
            encodeBlockByte(raw, endOff, i + offset, encoded, off);
            off += 4;
        }
        return encoded;
    }
    
        private static byte getByte(int sixBit) {
        return (byte) (sixBit < 26
                ? sixBit + 65
                : sixBit < 52
                        ? sixBit + 71
                        : sixBit < 62
                                ? sixBit - 4
                                : sixBit == 62
                                        ? 43
                                        : sixBit == 63
                                                ? 47
                                                : 65);
    }

    private static void encodeBlockByte(byte[] raw, int endOff, int offset, byte[] base64, int off) {
        int block = 0;
        // how much space left in input byte array
        int slack = endOff - offset;
        // if there are fewer than 3 bytes in this block, calculate end
        int end = (slack >= 2) ? 2 : slack;
        // convert signed quantities into unsigned
        for (int i = 0; i <= end; i++)
        {
            byte b = raw[offset + i];
            int neuter = (b < 0) ? b + 256 : b;
            block += neuter << (8 * (2 - i));
        }

        // extract the base64 digets, which are six bit quantities.
        for (int i = 0; i < 4; i++)
        {
            int sixbit = (block >>> (6 * (3 - i))) & 0x3f;
            base64[i + off] = getByte(sixbit);
        }
        // pad return block if needed
        if (slack < 1)
        {
            base64[off + 2] = '=';
        }
        if (slack < 2)
        {
            base64[off + 3] = '=';
        }
    }
}
