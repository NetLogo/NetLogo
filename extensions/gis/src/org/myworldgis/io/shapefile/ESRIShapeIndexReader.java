//
// Copyright (c) 2000 the National Geographic Society. All rights reserved.
//

package org.myworldgis.io.shapefile;

import org.locationtech.jts.geom.GeometryFactory;
import java.io.IOException;
import java.io.RandomAccessFile;
import org.ngs.ngunits.UnitConverter;


/**
 *
 */
public final class ESRIShapeIndexReader implements ESRIShapeConstants {

    //--------------------------------------------------------------------------
    // Instance variables
    //--------------------------------------------------------------------------

    /** */
    private RandomAccessFile _file;

    /** */
    private ESRIShapeBuffer _buffer;

    /** */
    private int _shapeType;

    /** */
    private int _fileSizeBytes;

    //--------------------------------------------------------------------------
    // Constructors
    //--------------------------------------------------------------------------

    /** */
    public ESRIShapeIndexReader (RandomAccessFile file,
                                 UnitConverter fileToRadians,
                                 GeometryFactory factory) throws IOException {
        _file = file;
        _file.seek(0);
        _buffer = new ESRIShapeBuffer(DEFAULT_BUFFER_SIZE, fileToRadians, factory);
        _buffer.read(_file, 0, SHAPE_FILE_HEADER_LENGTH);
        _shapeType = _buffer.getInt(32);
        _buffer.setByteOrder(ESRIShapeBuffer.ByteOrder.BIG_ENDIAN);
        _fileSizeBytes = _buffer.getInt(24) * 2;
    }

    //--------------------------------------------------------------------------
    // instance methods
    //--------------------------------------------------------------------------

    /** */
    public int getShapeType () {
        return _shapeType;
    }

    /** */
    public int getShapeCount () {
        return (_fileSizeBytes - SHAPE_FILE_HEADER_LENGTH) / SHAPE_INDEX_RECORD_LENGTH;
    }

    /** */
    public ESRIShapeIndexRecord getIndexRecord (int index) throws IOException {
        if (_file == null) {
            throw(new IOException("attempted to read from closed file"));
        }
        _file.seek(SHAPE_FILE_HEADER_LENGTH + (index * SHAPE_INDEX_RECORD_LENGTH));
        int bytesRead = _buffer.read(_file, 0, SHAPE_INDEX_RECORD_LENGTH);
        if (bytesRead < SHAPE_INDEX_RECORD_LENGTH) return(null);
        return(new ESRIShapeIndexRecord(_buffer.getInt(0) * 2,
                                        _buffer.getInt(4) * 2));
    }

    /** */
    public void close () throws IOException {
        _file.close();
    }
}
