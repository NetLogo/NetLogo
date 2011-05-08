//
// Copyright (c) 2000 the National Geographic Society. All rights reserved.
//

package org.myworldgis.io.shapefile;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.GeometryFactory;
import java.io.IOException;
import java.io.RandomAccessFile;
import javax.measure.converter.UnitConverter;


/**
 *
 */
public final strictfp class ESRIShapeIndexWriter implements ESRIShapeConstants {

    //--------------------------------------------------------------------------
    // Instance variables
    //--------------------------------------------------------------------------
    
    /** */
    private RandomAccessFile _raf;
    
    /** */
    private ESRIShapeBuffer _buffer;
    
    /** */
    private Envelope _envelope;
    
    /** */
    private int _esriShapeType;
    
    /** */
    private int _fileSizeBytes;
    
    //--------------------------------------------------------------------------
    // Constructors
    //--------------------------------------------------------------------------
    
    /** */
    public ESRIShapeIndexWriter (RandomAccessFile file, 
                                 Envelope envelope, 
                                 int esriShapeType,
                                 UnitConverter radiansToFile,
                                 GeometryFactory factory) throws IOException {
        _raf = file;
        _envelope = envelope;
        _esriShapeType = esriShapeType;
        _buffer = new ESRIShapeBuffer(DEFAULT_BUFFER_SIZE, radiansToFile, factory);
        _fileSizeBytes = SHAPE_FILE_HEADER_LENGTH;
        _buffer.setByteOrder(ESRIShapeBuffer.ByteOrder.BIG_ENDIAN);
        writeHeader();
        _raf.seek(SHAPE_FILE_HEADER_LENGTH);
    }
    
    //--------------------------------------------------------------------------
    // instance methods
    //--------------------------------------------------------------------------
    
    /** */
    private void writeHeader () throws IOException {
        _buffer.clear(0, SHAPE_FILE_HEADER_LENGTH);
        _buffer.putInt(0, 9994);
        _buffer.putInt(24, _fileSizeBytes / 2);
        _buffer.setByteOrder(ESRIShapeBuffer.ByteOrder.LITTLE_ENDIAN);
        _buffer.putInt(28, 1000);
        _buffer.putInt(32, _esriShapeType);
        _buffer.putBoundingBox(36, _envelope);
        _buffer.setByteOrder(ESRIShapeBuffer.ByteOrder.BIG_ENDIAN);
        _buffer.write(_raf, 0, SHAPE_FILE_HEADER_LENGTH);
    }
    
    
    /** */
    public void writeIndexRecord (ESRIShapeIndexRecord record) throws IOException {
        _buffer.putInt(0, record.getOffsetBytes() / 2);
        _buffer.putInt(4, record.getSizeBytes() / 2);
        _buffer.write(_raf, 0, SHAPE_INDEX_RECORD_LENGTH);
        _fileSizeBytes += SHAPE_INDEX_RECORD_LENGTH;
    }
    
    /** */
    public void close () throws IOException {
        _raf.seek(0);
        writeHeader();
        _raf.setLength(_fileSizeBytes);
        _raf.close();
    }        
}
