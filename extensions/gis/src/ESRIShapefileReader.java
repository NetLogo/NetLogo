//
// Copyright (c) 2000 the National Geographic Society. All rights reserved.
//

package org.myworldgis.io.shapefile;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import java.io.IOException;
import java.io.InputStream;
import javax.measure.converter.UnitConverter;


/**
 *
 */
public final strictfp class ESRIShapefileReader implements ESRIShapeConstants {
    
    //--------------------------------------------------------------------------
    // Instance variables
    //--------------------------------------------------------------------------
    
    /** */
    private InputStream _in;
    
    /** */
    private ESRIShapeBuffer _buffer;
    
    /** */
    private Envelope _envelope;
    
    /** */
    private int _shapeType;
    
    /** */
    private int _fileSizeBytes;
    
    //--------------------------------------------------------------------------
    // Constructors
    //--------------------------------------------------------------------------
    
    /** */
    public ESRIShapefileReader (InputStream in, 
                                UnitConverter inputConverter,
                                GeometryFactory factory) throws IOException {
        _in = in;
        _buffer = new ESRIShapeBuffer(DEFAULT_BUFFER_SIZE, inputConverter, factory);
        _buffer.read(_in, 0, SHAPE_FILE_HEADER_LENGTH);
        _envelope = _buffer.getBoundingBox(36);
        _shapeType = _buffer.getInt(32);
        _buffer.setByteOrder(ESRIShapeBuffer.ByteOrder.BIG_ENDIAN);
        _fileSizeBytes = _buffer.getInt(24) * 2;
        _buffer.setByteOrder(ESRIShapeBuffer.ByteOrder.LITTLE_ENDIAN);
    }
    
    //--------------------------------------------------------------------------
    // instance methods
    //--------------------------------------------------------------------------
    
    /** */
    public Envelope getEnvelope () {
        return _envelope;
    }
    
    /** */
    public int getShapeType () {
        return _shapeType;
    }
    
    /** */
    public int getSizeBytes () {
        return _fileSizeBytes;
    }
    
    /** */
    public Geometry getNextShape () throws IOException {
        int bytesRead = _buffer.read(_in, 0, SHAPE_RECORD_HEADER_LENGTH);
        if (bytesRead < SHAPE_RECORD_HEADER_LENGTH) {
            return null;
        }
        _buffer.setByteOrder(ESRIShapeBuffer.ByteOrder.BIG_ENDIAN);
        int contentByteLength = _buffer.getInt(4) * 2;
        _buffer.ensureCapacity(SHAPE_RECORD_HEADER_LENGTH + contentByteLength);
        _buffer.setByteOrder(ESRIShapeBuffer.ByteOrder.LITTLE_ENDIAN);
        _buffer.read(_in, SHAPE_RECORD_HEADER_LENGTH, contentByteLength);
        return _buffer.getESRIRecord(0);
    }
    
    /** */
    public void close () throws IOException {
        _in.close();
    }    
}
