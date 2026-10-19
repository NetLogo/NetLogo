package org.myworldgis.io;

import java.io.IOException;
import java.io.OutputStream;


/**
 * The subset of RandomAccessFile operations our file writers need, so the
 * same writers can target either a file or an in-memory byte array. Extends
 * OutputStream so Buffer.write(OutputStream, ...) accepts it unchanged.
 */
public abstract class RandomAccessSink extends OutputStream {

    /** */
    public abstract void seek (long position) throws IOException;

    /** */
    public abstract void setLength (long newLength) throws IOException;
}
