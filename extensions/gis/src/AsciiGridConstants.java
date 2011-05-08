//
// Copyright (c) 2000 the National Geographic Society. All rights reserved.
//

package org.myworldgis.io.asciigrid;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * 
 */
public interface AsciiGridConstants {
    
    //--------------------------------------------------------------------------
    // Class variables
    //--------------------------------------------------------------------------
    
    /** */
    public static final String ASCII_GRID_FILE_EXTENSION_1 = "asc";
    
    /** */
    public static final String ASCII_GRID_FILE_EXTENSION_2 = "grd";
    
    /** */
    public static final String COLUMN_COUNT = "NCOLS";
    
    /** */
    public static final String ROW_COUNT = "NROWS";
    
    /** */
    public static final String LEFT_LONGITUDE = "XLLCORNER";
    
    /** */
    public static final String BOTTOM_LATITUDE = "YLLCORNER";
    
    /** */
    public static final String CELL_SIZE = "CELLSIZE";
    
    /** */
    public static final String NAN_VALUE = "NODATA_VALUE";
    
    /** */
    public static final NumberFormat DIMENSION_FORMAT = new DecimalFormat("##0.######", new DecimalFormatSymbols(Locale.US));
    
    /** */
    public static final NumberFormat VALUE_FORMAT = new DecimalFormat("####################0.##########", 
                                                                      new DecimalFormatSymbols(Locale.US));
}
