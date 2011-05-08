//
// Copyright (c) 2000 the National Geographic Society. All rights reserved.
//

package org.myworldgis.io.shapefile;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.nlogo.api.Syntax;


/**
 * Class for reading a dBASE file.
 */
public final strictfp class DBaseFileReader implements DBaseConstants {
        
    //--------------------------------------------------------------------------
    // Instance variables
    //--------------------------------------------------------------------------
    
    /** */
    private InputStream _in;
    
    /** */
    private DBaseBuffer _buffer;
    
    /** */
    private DBaseFieldDescriptor[] _fieldDescriptors;
    
    /** */
    private int _recordCount;
    
    /** */
    private int _recordLength;
    
    //--------------------------------------------------------------------------
    // Constructors
    //--------------------------------------------------------------------------
    
    /** */
    public DBaseFileReader (InputStream in) throws IOException {
        _in = in;
        _buffer = new DBaseBuffer();
        
        // read the whole header
        _buffer.read(_in, 0, DBF_HEADER_SIZE_OFFSET + 2);
        int headerSize = _buffer.getShort(DBF_HEADER_SIZE_OFFSET);
        int headerBytes = headerSize - (DBF_HEADER_SIZE_OFFSET + 2);
        int headerBytesRead = _buffer.read(_in, DBF_HEADER_SIZE_OFFSET + 2, headerBytes);
        if (headerBytesRead < headerBytes) {
            throw(new EOFException());
        }
        byte fileCode = _buffer.getByte(DBF_FILE_CODE_OFFSET);
        if (fileCode != DBF_FILE_CODE) {
            throw new IOException("invalid file code "+fileCode+", probably not a dBase file");
        }
        _recordCount = _buffer.getInt(DBF_RECORD_COUNT_OFFSET);
        _recordLength = _buffer.getShort(DBF_RECORD_SIZE_OFFSET);
        
        // parse the field descriptors out of the header
        List<DBaseFieldDescriptor> fieldList = new ArrayList<DBaseFieldDescriptor>();
        for (int offset = DBF_HEADER_SIZE; offset < headerSize; offset += DBF_FIELD_DESCRIPTOR_SIZE) {
            if (_buffer.getByte(offset) == DBF_FIELD_TERMINATOR) {
                break;
            } else {
                fieldList.add(_buffer.getFieldDescriptor(offset));
            }
        }
        _fieldDescriptors = fieldList.toArray(new DBaseFieldDescriptor[fieldList.size()]);
        _buffer.ensureCapacity(_recordLength);
    }
    
    //--------------------------------------------------------------------------
    // Instance methods
    //--------------------------------------------------------------------------
    
    /** */
    public int getFieldCount () {
        return _fieldDescriptors.length;
    }
    
    /** */
    public String getFieldName (int index) {
        return _fieldDescriptors[index].getName();
    }
    
    /** */
    public DBaseFieldDescriptor getField (int index) {
        return _fieldDescriptors[index];
    }
    
    /** */
    public int getFieldDataType (int index) {
        switch (_fieldDescriptors[index].getType()) {
            case DBaseConstants.FIELD_TYPE_CHARACTER:
            case DBaseConstants.FIELD_TYPE_DATE:
                return Syntax.TYPE_STRING;
            case DBaseConstants.FIELD_TYPE_NUMBER:
            case DBaseConstants.FIELD_TYPE_FLOAT:
                return Syntax.TYPE_NUMBER;
            case DBaseConstants.FIELD_TYPE_LOGICAL:
                return Syntax.TYPE_BOOLEAN;
            default:    
                return Syntax.TYPE_NOBODY;
        }
    }
    
    /** */
    public int getRecordCount () {
        return _recordCount;
    }
    
    /** */
    public Object[] getNextRecord () throws IOException {
        _buffer.read(_in, 0, _recordLength);
        return _buffer.getRecord(0, _fieldDescriptors);
    }
    
    /** */
    public void close () throws IOException {
        _in.close();
    }
}
