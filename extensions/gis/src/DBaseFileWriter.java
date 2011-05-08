//
// Copyright (c) 2000 the National Geographic Society. All rights reserved.
//

package org.myworldgis.io.shapefile;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Date;


/**
 * Class for writing a dBASE file.
 */
public final strictfp class DBaseFileWriter implements DBaseConstants {
    
    //--------------------------------------------------------------------------
    // Instance variables
    //--------------------------------------------------------------------------

    /** */
    private RandomAccessFile _raf;

    /** */
    private DBaseBuffer _buffer;

    /** */
    private int _headerSize;

    /** */
    private DBaseFieldDescriptor[] _fieldDescriptors;

    /** */
    private int _recordLength;

    /** */
    private int _recordCount;

    //--------------------------------------------------------------------------
    // Constructors
    //--------------------------------------------------------------------------

    /** */
    public DBaseFileWriter (RandomAccessFile file, DBaseFieldDescriptor[] fieldDescriptors) throws IOException {
        _raf = file;
        _buffer = new DBaseBuffer();
        String[] fieldNames = new String[fieldDescriptors.length];
        for (int i = 0; i < fieldDescriptors.length; i += 1) {
            fieldNames[i] = fieldDescriptors[i].getName();
        }
        fieldNames = DBaseFieldDescriptor.makeLegalFieldNames(fieldNames);
        _fieldDescriptors = new DBaseFieldDescriptor[fieldDescriptors.length];
        for (int i = 0; i < fieldDescriptors.length; i += 1) {
            _fieldDescriptors[i] = new DBaseFieldDescriptor(fieldDescriptors[i].getType(),
                                                            fieldNames[i],
                                                            fieldDescriptors[i].getLength(),
                                                            fieldDescriptors[i].getDecimalCount());
        }
        writeHeader();
        writeFileTerminator();
        _raf.seek(_headerSize);
    }

    //--------------------------------------------------------------------------
    // Instance methods
    //--------------------------------------------------------------------------
    
    /** */
    private void writeHeader () throws IOException {
        _headerSize = DBF_HEADER_SIZE + (DBF_FIELD_DESCRIPTOR_SIZE * _fieldDescriptors.length) + 1;
        _buffer.ensureCapacity(_headerSize);
        _buffer.clear(0, _headerSize);
        _buffer.putByte(DBF_FILE_CODE_OFFSET, DBF_FILE_CODE);
        _buffer.putHeaderDate(DBF_DATE_OFFSET, new Date(System.currentTimeMillis()));
        _buffer.putInt(DBF_RECORD_COUNT_OFFSET, _recordCount);
        _buffer.putShort(DBF_HEADER_SIZE_OFFSET, (short)_headerSize);
        _buffer.putByte(DBF_CODEPAGE_OFFSET, (byte)1); // MS DOS codepage
        _recordLength = 1;
        int offset = DBF_HEADER_SIZE;
        for (int i = 0; i < _fieldDescriptors.length; i += 1) {
            _buffer.putFieldDescriptor(offset, _fieldDescriptors[i]);
            offset += DBF_FIELD_DESCRIPTOR_SIZE;
            _recordLength += _fieldDescriptors[i].getLength();
        }
        _buffer.ensureCapacity(_recordLength);
        _buffer.putShort(DBF_RECORD_SIZE_OFFSET, (short)_recordLength);
        _buffer.putByte(_headerSize - 1, DBF_FIELD_TERMINATOR);
        _raf.seek(0);
        _buffer.write(_raf, 0, _headerSize);
    }

    /** */
    private void writeFileTerminator () throws IOException {
        _raf.seek(_headerSize + (_recordLength * _recordCount));
        _raf.write(DBF_FILE_TERMINATOR);
    }

    /** */
    public void writeRecord (Object[] record) throws IOException {
        if (record.length != _fieldDescriptors.length) {
            throw new IOException("incompatable number of fields in writeRecord (record has " + record.length + " fields, file has " + _fieldDescriptors.length + ")");
        }
        _buffer.clear(0, _recordLength);
        _buffer.putRecord(0, _fieldDescriptors, record);
        _raf.seek(_headerSize + (_recordLength * _recordCount));
        _buffer.write(_raf, 0, _recordLength);
        _recordCount += 1;
    }

    /** */
    public void close () throws IOException { 
        writeHeader();
        writeFileTerminator();
        _raf.close();
    }
}
