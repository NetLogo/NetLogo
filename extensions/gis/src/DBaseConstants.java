//
// Copyright (c) 2000 the National Geographic Society. All rights reserved.
//

package org.myworldgis.io.shapefile;


/**
 *
 */
public interface DBaseConstants {
    
    //--------------------------------------------------------------------------
    // Instance variables
    //--------------------------------------------------------------------------
    
    /** */
    public static final int DBF_HEADER_SIZE = 32;
    
    /** */
    public static final byte DBF_FILE_CODE = 0x03;
    
    /** */
    public static final int DBF_FILE_TERMINATOR = 0x1a;
    
    /** */
    public static final byte DBF_FIELD_TERMINATOR = 0x0d;
    
    /** */
    public static final int DBF_FILE_CODE_OFFSET = 0;
    
    /** */
    public static final int DBF_DATE_OFFSET = 1;
    
    /** */
    public static final int DBF_RECORD_COUNT_OFFSET = 4;
    
    /** */
    public static final int DBF_HEADER_SIZE_OFFSET = 8;
    
    /** */
    public static final int DBF_RECORD_SIZE_OFFSET = 10;
    
    /** */
    public static final int DBF_CODEPAGE_OFFSET = 29;
    
    /** */
    public static final int DBF_FIELD_DESCRIPTOR_SIZE = 32; 
    
    /** */
    public static final int DBF_FIELD_DESCRIPTOR_NAME_OFFSET = 0;
    
    /** */
    public static final int DBF_FIELD_DESCRIPTOR_NAME_LENGTH = 11;
    
    /** */
    public static final int DBF_FIELD_DESCRIPTOR_TYPE_OFFSET = 11;
    
    /** */
    public static final int DBF_FIELD_DESCRIPTOR_LENGTH_OFFSET = 16;
    
    /** */
    public static final int DBF_FIELD_DESCRIPTOR_DECIMAL_COUNT_OFFSET = 17;
    
    /** */
    public static final char FIELD_TYPE_CHARACTER = 'C';
    
    /** */
    public static final char FIELD_TYPE_DATE = 'D';
    
    /** */
    public static final char FIELD_TYPE_NUMBER = 'N';
    
    /** */
    public static final char FIELD_TYPE_LOGICAL = 'L';
    
    /** */
    public static final char FIELD_TYPE_MEMO = 'M';
    
    /** */
    public static final char FIELD_TYPE_FLOAT = 'F';
    
    /** */
    public static final char RECORD_ACTIVE = (char)0x20;    
    
    /** */
    public static final char RECORD_DELETED = (char)0x2A; 

    /** */
    public static final String DBASE_FILE_EXTENSION = "dbf";
}
