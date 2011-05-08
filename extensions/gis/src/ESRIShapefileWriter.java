//
// Copyright (c) 2000 the National Geographic Society. All rights reserved.
//

package org.myworldgis.io.shapefile;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import java.io.IOException;
import java.io.RandomAccessFile;
import javax.measure.converter.UnitConverter;


/**
 *
 */
public final strictfp class ESRIShapefileWriter implements ESRIShapeConstants {

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
    public ESRIShapefileWriter (RandomAccessFile file, 
                                Envelope envelope, 
                                int esriShapeType,
                                UnitConverter outputConverter,
                                GeometryFactory factory) throws IOException {
        _raf = file;
        _buffer = new ESRIShapeBuffer(DEFAULT_BUFFER_SIZE, outputConverter, factory);
        _envelope = envelope;
        _esriShapeType = esriShapeType;
        _fileSizeBytes = SHAPE_FILE_HEADER_LENGTH;
        writeHeader();
        _raf.seek(SHAPE_FILE_HEADER_LENGTH);
    }
    
    //--------------------------------------------------------------------------
    // instance methods
    //--------------------------------------------------------------------------
    
    /** */
    private void writeHeader () throws IOException {
        _buffer.clear(0, SHAPE_FILE_HEADER_LENGTH);
        _buffer.setByteOrder(ESRIShapeBuffer.ByteOrder.BIG_ENDIAN);
        _buffer.putInt(0, 9994);
        _buffer.putInt(24, _fileSizeBytes / 2);
        _buffer.setByteOrder(ESRIShapeBuffer.ByteOrder.LITTLE_ENDIAN);
        _buffer.putInt(28, 1000);
        _buffer.putInt(32, _esriShapeType);
        _buffer.putBoundingBox(36, _envelope);
        _buffer.write(_raf, 0, SHAPE_FILE_HEADER_LENGTH);
    }
    
    /** */
    public ESRIShapeIndexRecord writeShape (int index, Geometry feature) throws IOException {
        int recordSizeBytes = _buffer.putESRIRecord(0, feature, _esriShapeType, index);
        ESRIShapeIndexRecord indexRecord = new ESRIShapeIndexRecord(_fileSizeBytes, recordSizeBytes - SHAPE_RECORD_HEADER_LENGTH);
        _buffer.write(_raf, 0, recordSizeBytes);
        _fileSizeBytes += recordSizeBytes;
        return(indexRecord);
    }
    
    /** */
    public void close () throws IOException {
        _raf.seek(0);
        writeHeader();
        _raf.setLength(_fileSizeBytes);
        _raf.close();
    }        
}
