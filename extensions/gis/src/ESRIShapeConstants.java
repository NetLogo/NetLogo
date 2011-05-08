//
// Copyright (c) 2000 the National Geographic Society. All rights reserved.
//

package org.myworldgis.io.shapefile;


/**
 *
 */
public interface ESRIShapeConstants {
    
    //--------------------------------------------------------------------------
    // Instance methods
    //--------------------------------------------------------------------------
    
    /** Filename extension for spatial index files */
    public final static String SHAPE_INDEX_EXTENSION = "shx";
    
    /** Filename extension for shape files */
    public static final String SHAPEFILE_EXTENSION = "shp";
    
    /** A Shape File's magic number. */
    public static final int SHAPE_FILE_CODE = 9994;

    /** The currently handled version of Shape Files. */
    public static final int SHAPE_FILE_VERSION = 1000;

    /** A default record size.  Automatically increased on demand. */
    public static final int DEFAULT_BUFFER_SIZE = 50000;
    
    /** The length of a shape file header in bytes.  (100) */
    public static final int SHAPE_FILE_HEADER_LENGTH = 100;

    /** The indicator for a null shape type. (0) */
    public static final int SHAPE_TYPE_NULL      = 0;

    /** The indicator for a point shape type. (1) */
    public static final int SHAPE_TYPE_POINT      = 1;

    /** The indicator for an polyline shape type. (3) */
    public static final int SHAPE_TYPE_POLYLINE      = 3;

    /** The indicator for a polygon shape type. (5) */
    public static final int SHAPE_TYPE_POLYGON      = 5;

    /** The indicator for a multipoint shape type. (8) */
    public static final int SHAPE_TYPE_MULTIPOINT = 8;
    
    /** Size of a shape file record header. (8 bytes) */
    public final static int SHAPE_RECORD_HEADER_LENGTH = 8;

    /** (8 bytes) */
    public final static int SHAPE_INDEX_RECORD_LENGTH = 8;
}
