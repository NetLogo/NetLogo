//
// Copyright (c) 2000 the National Geographic Society. All rights reserved.
//

package org.myworldgis.io.shapefile;


/**
 *
 */
public final strictfp class ESRIShapeIndexRecord {
    
    //--------------------------------------------------------------------------
    // Instance variables
    //--------------------------------------------------------------------------
    
    /** */
    private int _offsetBytes;
    
    /** */
    private int _sizeBytes;
    
    //--------------------------------------------------------------------------
    // Constructors
    //--------------------------------------------------------------------------
    
    /** */
    public ESRIShapeIndexRecord (int offsetBytes, int sizeBytes) {
        _offsetBytes = offsetBytes;
        _sizeBytes = sizeBytes;
    }
    
    //--------------------------------------------------------------------------
    // Instance methods
    //--------------------------------------------------------------------------
    
    /** */
    public int getOffsetBytes () {
        return(_offsetBytes);
    }
    
    /** */
    public int getSizeBytes ()  {
        return(_sizeBytes);
    }
    
    /** */
    public String toString () {
        return("shapeIndex["+_offsetBytes+","+_sizeBytes+"]");
    }
}
