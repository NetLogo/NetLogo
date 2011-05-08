//
// Copyright (c) 2000 the National Geographic Society. All rights reserved.
//

package org.myworldgis.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import javax.swing.SwingConstants;


/**
 * Utilites for reading and writing from byte arrays. Java 1.4's java.nio
 * package does this same kind of thing, but it lacks the ability to go back
 * and forth between big-endian and little-endian that we need for reading
 * shapefiles.
 */
public strictfp class Buffer {
    
    //--------------------------------------------------------------------------
    // Inner classes
    //--------------------------------------------------------------------------
    
    /** */
    public enum ByteOrder { BIG_ENDIAN, LITTLE_ENDIAN }
    
    //--------------------------------------------------------------------------
    // Class variables
    //--------------------------------------------------------------------------
    
    /** */
    private static final transient byte PAD_BYTE = 0x20;
    
    //--------------------------------------------------------------------------
    // Class methods - reading & writing big-endian values
    //--------------------------------------------------------------------------
    
    /**
     * Reads a 2-byte signed integer in big endian format.
     *
     * @param b the raw data buffer
     * @param off the offset into the buffer where the short resides
     * @return the short read from the buffer at the offset location
     */
    private static final short getBEShort (byte[] b, int off) {
        return (short)(((b[off + 0] & 0xff) << 8) |
                       ((b[off + 1] & 0xff)     ));
    }
    
    /**
     * Writes a 2-byte signed integer in big endian format.
     *
     * @param b the data buffer
     * @param off the offset into the buffer where writing should occur
     * @param val the short to write
     * @return the number of bytes written
     */
    private static final int putBEShort (byte[] b, int off, short val) {
        b[off + 0] = (byte) ((val >> 8) & 0xff);
        b[off + 1] = (byte) ((val     ) & 0xff);
        return 2;
    }
    
    /**
     * Reads a 4-byte signed integer in big endian format.
     *
     * @param b the raw data buffer
     * @param off the offset into the buffer where the int resides
     * @return the int read from the buffer at the offset location
     */
    private static final int getBEInt (byte[] b, int off) {
        return ((b[off + 0] & 0xff) << 24) | 
               ((b[off + 1] & 0xff) << 16) | 
               ((b[off + 2] & 0xff) <<  8) |
               ((b[off + 3] & 0xff)      );
    }
    
    /**
     * Writes a 4-byte signed integer in big endian format.
     *
     * @param b the data buffer
     * @param off the offset into the buffer where writing should occur
     * @param val the integer to write
     * @return the number of bytes written
     */
    private static final int putBEInt (byte[] b, int off, int val) {
        b[off + 0] = (byte) ((val >> 24) & 0xff);
        b[off + 1] = (byte) ((val >> 16) & 0xff);
        b[off + 2] = (byte) ((val >>  8) & 0xff);
        b[off + 3] = (byte) ((val       ) & 0xff);
        return 4;
    }
    
    /**
     * Reads an 8-byte signed integer in big endian format.
     *
     * @param b the raw data buffer
     * @param off the offset into the buffer where the long resides
     * @return the long read from the buffer at the offset location
     */
    private static final long getBELong (byte[] b, int off) {
        return ((b[off + 0] & 0xffL) << 56) |
               ((b[off + 1] & 0xffL) << 48) |
               ((b[off + 2] & 0xffL) << 40) |
               ((b[off + 3] & 0xffL) << 32) |
               ((b[off + 4] & 0xffL) << 24) |
               ((b[off + 5] & 0xffL) << 16) |
               ((b[off + 6] & 0xffL) <<  8) |
               ((b[off + 7] & 0xffL)      );
    }
    
    /**
     * Writes an 8-byte signed integer in big endian format.
     *
     * @param b the raw data buffer
     * @param off the offset into the buffer where the long resides
     * @return the long read from the buffer at the offset location
     */
    private static final int putBELong (byte[] b, int off, long val) {
        b[off + 0] = (byte) ((val >> 56) & 0xff);
        b[off + 1] = (byte) ((val >> 48) & 0xff);
        b[off + 2] = (byte) ((val >> 40) & 0xff);
        b[off + 3] = (byte) ((val >> 32) & 0xff);
        b[off + 4] = (byte) ((val >> 24) & 0xff);
        b[off + 5] = (byte) ((val >> 16) & 0xff);
        b[off + 6] = (byte) ((val >>  8) & 0xff);
        b[off + 7] = (byte) ((val      ) & 0xff);
        return 8;
    }
    
    //--------------------------------------------------------------------------
    // Class methods - reading little-endian values
    //--------------------------------------------------------------------------
    
    /**
     * Reads a 2-byte signed integer in little endian format.
     *
     * @param b the raw data buffer
     * @param off the offset into the buffer where the short resides
     * @return the short read from the buffer at the offset location
     */    
    private static final short getLEShort (byte[] b, int off) {
        return (short)(((b[off + 1] & 0xff) << 8) |
                       ((b[off + 0] & 0xff)));
    }
    
    /**
     * Writes a 2-byte signed integer in little endian format.
     *
     * @param b the data buffer
     * @param off the offset into the buffer where writing should occur
     * @param val the short to write
     * @return the number of bytes written
     */
    private static final int putLEShort (byte[] b, int off, short val) {
        b[off + 0] = (byte) (val & 0xff);
        b[off + 1] = (byte) ((val >> 8) & 0xff);
        return 2;
    }

    /**
     * Reads a 4-byte signed integer in little endian format.
     *
     * @param b the raw data buffer
     * @param off the offset into the buffer where the int resides
     * @return the int read from the buffer at the offset location
     */
    private static final int getLEInt (byte[] b, int off) {
        return ((b[off + 3] & 0xff) << 24) |
               ((b[off + 2] & 0xff) << 16) |
               ((b[off + 1] & 0xff) <<  8) |
               ((b[off + 0] & 0xff)      );
    }
    
    /**
     * Writes a 4-byte signed integer in little endian format.
     *
     * @param b the data buffer
     * @param off the offset into the buffer where writing should occur
     * @param val the integer to write
     * @return the number of bytes written
     */
    private static final int putLEInt (byte[] b, int off, int val) {
        b[off + 0] = (byte) ((val       ) & 0xff);
        b[off + 1] = (byte) ((val >>  8) & 0xff);
        b[off + 2] = (byte) ((val >> 16) & 0xff);
        b[off + 3] = (byte) ((val >> 24) & 0xff);
        return 4;
    }

    /**
     * Reads an 8-byte signed integer in little endian format.
     *
     * @param b the raw data buffer
     * @param off the offset into the buffer where the long resides
     * @return the long read from the buffer at the offset location
     */
    private static final long getLELong (byte[] b, int off) {
        return ((b[off + 0] & 0xffL)      ) |
               ((b[off + 1] & 0xffL) <<  8) |
               ((b[off + 2] & 0xffL) << 16) |
               ((b[off + 3] & 0xffL) << 24) |
               ((b[off + 4] & 0xffL) << 32) |
               ((b[off + 5] & 0xffL) << 40) |
               ((b[off + 6] & 0xffL) << 48) |
               ((b[off + 7] & 0xffL) << 56);
    }
    
    /**
     * Writes an 8-byte signed integer in little endian format.
     *
     * @param b the data buffer
     * @param off the offset into the buffer where writing should occur
     * @param val the long to write
     * @return the number of bytes written
     */
    private static final int putLELong (byte[]  b, int off, long val) {
        b[off + 0] = (byte) ((val       ) & 0xff);
        b[off + 1] = (byte) ((val >>  8) & 0xff);
        b[off + 2] = (byte) ((val >> 16) & 0xff);
        b[off + 3] = (byte) ((val >> 24) & 0xff);
        b[off + 4] = (byte) ((val >> 32) & 0xff);
        b[off + 5] = (byte) ((val >> 40) & 0xff);
        b[off + 6] = (byte) ((val >> 48) & 0xff);
        b[off + 7] = (byte) ((val >> 56) & 0xff);
        return 8;
    }
    
    /** */
    private static final short fromUnsigned (byte b) {
        return (b < 0) ? (short)(b + 256) : (short)b;
    }
    
    /** */
    private static final int fromUnsigned (short s) {
        return (s < 0) ? (s + 65536) : (int)s;
    }
    
    /** */
    private static final long fromUnsigned (int i) {
        return (i < 0) ? (i + 4294967296L) : (long)i;
    }
    
    
    /** */
    private static final byte toUnsigned (short s) {
        return (s > 127) ? (byte)(s - 256) : (byte)s;
    }
    
    /** */
    private static final short toUnsigned (int i) {
        return (i > 32767) ? (short)(i - 65536) : (short)i;
    }
    
    /** */
    private static final int toUnsigned (long l) {
        return (l > 2147483647L) ? (int)(l - 4294967296L) : (int)l;
    }
    
    /** */
    private static final boolean isUTF (byte[] b, int off, int len) {
        boolean isUTF = false;
        int sequenceLength = -1;
        for (int i = 0; i < len; i += 1) {
            short c = fromUnsigned(b[off+i]);
            if (c < 0x80) { // normal ASCII character
                if (isUTF && (sequenceLength >= 0)) {
                    return false;
                }
            } else if (c < 0xC0) { // 10xxxxxx : continues UTF8 byte sequence
                if (isUTF && (sequenceLength >= 0)) {
                    sequenceLength -= 1;
                    if (sequenceLength < 0) {
                        return false;
                    } else if (sequenceLength == 0) {
                        return true;
                    }
                } else {
                    return false;
                }
            } else if ((c >= 0xC2) && (c < 0xF5)) { // beginning of byte sequence
                if (isUTF && (sequenceLength >= 0)) {
                    return false;
                }
                isUTF = true;
                if (c < 0xE0) { 
                    sequenceLength = 1; // one more byte following 
                } else if (c < 0xF0) { 
                    sequenceLength = 2; // two more bytes following 
                } else { 
                    sequenceLength = 3; // three more bytes following 
                }
            } else {
                // 0xc0, 0xc1, 0xf5 to 0xff are invalid in UTF-8 (see RFC 3629) 
                return false;
            }
        }
        return (isUTF && (sequenceLength < 0));
    }
    
    /** */
    private static final String getString (byte[] b, int off, int len) {
        String charset = isUTF(b, off, len) ? "UTF-8" : "ISO-8859-1";
        try {
            return new String(b, off, len, charset);
        } catch (UnsupportedEncodingException e) {
            return new String(b, off, len);
        }
    }
    
    /** */
    private static final String getTrimmedString (byte[] b, int off, int len) {
        // would use String.trim() for this, but it leaves the entire
        // character array hard-referenced, wasting memory (which can
        // really add up in a 100+ MB data file).
        int begin = off;
        int end = off + (len - 1);
        while ((begin < end) && (fromUnsigned(b[begin]) <= ' ')) {
            begin += 1;
        }
        while ((end >= begin) && (fromUnsigned(b[end]) <= ' ')) {
            end -= 1;
        }
        return getString(b, begin, (end - begin) + 1);
    }
    
    /** */
    public static final byte[] getBytes (String str) {
        try {
            return str.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            return str.getBytes();
        }
    }
    
    //--------------------------------------------------------------------------
    // Instance variables
    //--------------------------------------------------------------------------
    
    /** */
    private byte[] _bytes;
    
    /** */
    private int _size = 0;
    
    /** */
    private ByteOrder _byteOrder;
    
    //--------------------------------------------------------------------------
    // Constructors
    //--------------------------------------------------------------------------
    
    /** */
    public Buffer (int maxSize) {
        this(maxSize, ByteOrder.BIG_ENDIAN);
    }
    
    /** */
    public Buffer (int maxSize, ByteOrder byteOrder) {
        _bytes = new byte[maxSize];
        _size = 0;
        _byteOrder = byteOrder;
    }
    
    //--------------------------------------------------------------------------
    // Instance methods
    //--------------------------------------------------------------------------
    
    /** */
    public int read (RandomAccessFile in, int offset, int byteCount) throws IOException {
        if (_bytes.length + offset < byteCount) {
            byte[] newBytes = new byte[byteCount + offset];
            System.arraycopy(_bytes, 0, newBytes, 0, _bytes.length);
            _bytes = newBytes;
        }
        _size = in.read(_bytes, offset, byteCount);
        return _size;
    }
    
    /** */
    public int read (InputStream in, int offset, int byteCount) throws IOException {
        if (_bytes.length + offset < byteCount) {
            byte[] newBytes = new byte[byteCount + offset];
            System.arraycopy(_bytes, 0, newBytes, 0, _bytes.length);
            _bytes = newBytes;
        }
        _size = in.read(_bytes, offset, byteCount);
        return _size;
    }
    
    /** */
    public void write (RandomAccessFile out, int offset, int byteCount) throws IOException {
        out.write(_bytes, offset, byteCount);
    }
    
    /** */
    public void write (OutputStream out, int offset, int byteCount) throws IOException {
        out.write(_bytes, offset, byteCount);
    }
    
    /** */
    public void clear () {
        Arrays.fill(_bytes, (byte)0);
        _size = 0;
    }
    
    /** */
    public void clear (int offset, int size) {
        Arrays.fill(_bytes, offset, StrictMath.min(offset + size, _bytes.length), (byte)0);
    }
    
    /** */
    public int getSize () {
        return _size;
    }
    
    /** */
    public int getMaxSize () {
        return _bytes.length;
    }
    
    /** */
    public void ensureCapacity (int newMaxSize) {
        if (newMaxSize > _bytes.length) {
            byte[] newBytes = new byte[newMaxSize];
            System.arraycopy(_bytes, 0, newBytes, 0, _bytes.length);
            _bytes = newBytes;
        }
    }
    
    /** */
    public ByteOrder getByteOrder () {
        return _byteOrder;
    }
    
    /** */
    public void setByteOrder (ByteOrder newOrder) {
        _byteOrder = newOrder;
    }
    
    /**
     * Read null-terminated string, trimming off any whitespace
     *
     * @param off the offset into the buffer where reading should begin
     * @param len number of bytes to read
     * @return the string read from the buffer
     */
    public final String getCString (int offset, int maxLen) {
        int len = maxLen;
        for (int i = 0; i < len; i += 1) {
            if (_bytes[offset + i] == 0) {
                len = i;
            }
        }
        return getTrimmedString(_bytes, offset, len);
    }
    
    /**
     * Writes a null terminated string, filling any excess space with zeros.
     *
     * @param off the offset into the buffer where writing should begin
     * @param len the number of bytes to write
     * @param str the string to write
     * @return the number of bytes written
     */    
    public final int putCString (int offset, int len, String str) {
        byte[] stringBytes = getBytes(str);
        System.arraycopy(stringBytes, 0, _bytes, offset, StrictMath.min(stringBytes.length, len));
        int padBytes = len - stringBytes.length;
        for (int i = 0; i < padBytes; i += 1) {
            _bytes[offset+stringBytes.length+i] = 0;
        }
        return len;
    }
    
    /** */
    public final String getJustifiedString (int offset, int len) {
        return getTrimmedString(_bytes, offset, len);
    }
    
    /** */
    public final int putJustifiedString (int offset, int len, String str, int just) {
        byte[] stringBytes = getBytes(str);
        int totalPadChars = len - stringBytes.length;
        int leftPadChars = 0;
        int rightPadChars = 0;
        if (totalPadChars > 0) {
            switch (just) {
                case SwingConstants.LEFT:
                    rightPadChars = totalPadChars;
                    break;
                case SwingConstants.CENTER:
                    leftPadChars = (int)StrictMath.floor(totalPadChars / 2.0);
                    rightPadChars = (int)StrictMath.ceil(totalPadChars / 2.0);
                    break;
                case SwingConstants.RIGHT:
                    leftPadChars = totalPadChars;
                    break;
                default:
                    throw(new IllegalArgumentException("unknown justification: "+just));
            }
        }
        for (int i = 0; i < leftPadChars; i += 1) {
            _bytes[offset+i] = PAD_BYTE;
        }
        System.arraycopy(stringBytes, 0, _bytes, offset+leftPadChars, StrictMath.min(stringBytes.length, len));
        for (int i = 0; i < rightPadChars; i += 1) {
            _bytes[offset+leftPadChars+stringBytes.length+i] = PAD_BYTE;
        }
        return len;
    }
    
    /** 
     * NOTE: this trims its result, so don't rely on the length of the result string!
     */
    public final String getPascalString (int offset) {
        return getTrimmedString(_bytes, offset + 1, getUnsignedByte(offset));
    }
    
    /** */
    public final int putPascalString (int offset, String str) {
        byte[] stringBytes = getBytes(str);
        short length = (short)StrictMath.min(stringBytes.length, 255);
        putUnsignedByte(offset, length);
        for (int i = 0; i < length; i += 1) {
            _bytes[offset + 1 + i] = stringBytes[i];
        }
        return length + 1;
    }
    
    /** */
    public final char getChar (int offset) {
        return getString(_bytes, offset, 1).charAt(0);
    }
    
    /** */
    public final int putChar (int offset, char val) {
        _bytes[offset] = getBytes(new String(new char[] { val }))[0];
        return 1;
    }
    
    /** */
    public final byte getByte (int offset) {
        return _bytes[offset];
    }
    
    /** */
    public final int putByte (int offset, byte val) {
        _bytes[offset] = val;
        return 1;
    }
    
    /**
     * Java doesn't support unsigned bytes, so we have to fudge
     * the signed value by adding 256 if it's negative
     */
    public final short getUnsignedByte (int offset) {
        return fromUnsigned(_bytes[offset]);
    }
    
    /**
     * Java doesn't support unsigned bytes, so we have to fudge
     * the signed value by subtracting 256 if it's greater than 127
     */
    public final int putUnsignedByte (int offset, short val) {
        return putByte(offset, toUnsigned(val));
    }
    
    /** */
    public final short getShort (int offset) {
        return (_byteOrder == ByteOrder.BIG_ENDIAN) ? 
                   getBEShort(_bytes, offset) : 
                   getLEShort(_bytes, offset);
    }
    
    /** */
    public final int putShort (int offset, short val) {
        return (_byteOrder == ByteOrder.BIG_ENDIAN) ? 
                   putBEShort(_bytes, offset, val) : 
                   putLEShort(_bytes, offset, val);
    }
    
    /**
     * Java doesn't support unsigned shorts, so we have to fudge
     * the signed value by adding 2^16 if it's negative
     */
    public final int getUnsignedShort (int offset) {
        return fromUnsigned(getShort(offset));
    }
    
    /**
     * Java doesn't support unsigned shorts, so we have to fudge
     * the signed value by subtracting 2^16 if it's greater than 2^15
     */
    public final int putUnsignedShort (int offset, int val) {
        return putShort(offset, toUnsigned(val));
    }
    
    /** */
    public final int getInt (int offset) {
        return (_byteOrder == ByteOrder.BIG_ENDIAN) ? 
                   getBEInt(_bytes, offset) : 
                   getLEInt(_bytes, offset);
    }
    
    /** */
    public final int putInt (int offset, int val) {
        return (_byteOrder == ByteOrder.BIG_ENDIAN) ? 
                   putBEInt(_bytes, offset, val) : 
                   putLEInt(_bytes, offset, val);
    }
    
    /**
     * Java doesn't support unsigned integers, so we have to fudge
     * the signed value by adding 2^32 if it's negative
     */
    public final long getUnsignedInt (int offset) {
        return fromUnsigned(getInt(offset));
    }
    
    /**
     * Java doesn't support unsigned integers, so we have to fudge
     * the signed value by subtracting 2^32 if it's greater than 2^31
     */
    public final int putUnsignedInt (int offset, long val) {
        return putInt(offset, toUnsigned(val));
    }
    
    /** */
    public final long getLong (int offset) {
        return (_byteOrder == ByteOrder.BIG_ENDIAN) ? 
                   getBELong(_bytes, offset) : 
                   getLELong(_bytes, offset);
    }
    
    /** */
    public final int putLong (int offset, long val) {
        return (_byteOrder == ByteOrder.BIG_ENDIAN) ? 
                   putBELong(_bytes, offset, val) : 
                   putLELong(_bytes, offset, val);
    }
    
    /** */
    public final float getFloat (int offset) {
        return Float.intBitsToFloat(getInt(offset));
    }
    
    /** */
    public final int putFloat (int offset, float val) {
        return putInt(offset, Float.floatToIntBits(val));
    }
    
    /** */
    public final double getDouble (int offset) {
        return Double.longBitsToDouble(getLong(offset));
    }
    
    /** */
    public final int putDouble (int offset, double val) {
        return putLong(offset, Double.doubleToLongBits(val));
    }
}
