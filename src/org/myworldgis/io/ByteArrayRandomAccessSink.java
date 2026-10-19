package org.myworldgis.io;

import java.util.Arrays;


/**
 *
 */
public final class ByteArrayRandomAccessSink extends RandomAccessSink {

    /** */
    private byte[] _bytes = new byte[1024];

    /** */
    private int _length = 0;

    /** */
    private int _position = 0;

    /** */
    private void ensureCapacity (int capacity) {
        if (capacity > _bytes.length) {
            _bytes = Arrays.copyOf(_bytes, StrictMath.max(capacity, _bytes.length * 2));
        }
    }

    /** */
    public void seek (long position) {
        _position = (int)position;
    }

    /** */
    public void setLength (long newLength) {
        ensureCapacity((int)newLength);
        _length = (int)newLength;
        if (_position > _length) {
            _position = _length;
        }
    }

    /** */
    public void write (int b) {
        ensureCapacity(_position + 1);
        _bytes[_position] = (byte)b;
        _position += 1;
        _length = StrictMath.max(_length, _position);
    }

    /** */
    public void write (byte[] b, int off, int len) {
        ensureCapacity(_position + len);
        System.arraycopy(b, off, _bytes, _position, len);
        _position += len;
        _length = StrictMath.max(_length, _position);
    }

    /** */
    public void close () { }

    /** */
    public byte[] toByteArray () {
        return Arrays.copyOf(_bytes, _length);
    }
}
