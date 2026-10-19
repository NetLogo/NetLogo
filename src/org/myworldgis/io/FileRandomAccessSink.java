package org.myworldgis.io;

import java.io.IOException;
import java.io.RandomAccessFile;


/**
 *
 */
public final class FileRandomAccessSink extends RandomAccessSink {

    /** */
    private final RandomAccessFile _raf;

    /** */
    public FileRandomAccessSink (RandomAccessFile raf) {
        _raf = raf;
    }

    /** */
    public void seek (long position) throws IOException {
        _raf.seek(position);
    }

    /** */
    public void setLength (long newLength) throws IOException {
        _raf.setLength(newLength);
    }

    /** */
    public void write (int b) throws IOException {
        _raf.write(b);
    }

    /** */
    public void write (byte[] b, int off, int len) throws IOException {
        _raf.write(b, off, len);
    }

    /** */
    public void close () throws IOException {
        _raf.close();
    }
}
