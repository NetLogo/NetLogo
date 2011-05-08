//
// Copyright (c) 2000 the National Geographic Society. All rights reserved.
//

package org.myworldgis.io.shapefile;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.Locale;
import javax.swing.SwingConstants;
import org.myworldgis.util.Buffer;


/**
 * Utilities for reading & writing dBase files
 */
public final strictfp class DBaseBuffer extends Buffer implements DBaseConstants {
    
    //--------------------------------------------------------------------------
    // Class variables
    //--------------------------------------------------------------------------
    
    /** */
    private static final DecimalFormatSymbols SYMBOLS = new DecimalFormatSymbols(Locale.US);
    
    /** */
    private static final NumberFormat YEAR_FORMAT = new DecimalFormat("0000", SYMBOLS);
    
    /** */
    private static final NumberFormat MONTH_DAY_FORMAT = new DecimalFormat("00", SYMBOLS);
    
    /** */
    public static final NumberFormat FLOAT_FORMAT = new DecimalFormat("0.###########E000", SYMBOLS);
    
    /** */
    public static final NumberFormat DECIMAL_FORMAT = new DecimalFormat("###################0.##################", SYMBOLS);
    
    /** */
    private static final NumberFormat DECIMAL_PRINT_FORMAT = (NumberFormat)DECIMAL_FORMAT.clone();
    
    /** */
    public static final double MAX_DECIMAL_VALUE = 9.99999999999E19;
    
    //--------------------------------------------------------------------------
    // Class methods
    //--------------------------------------------------------------------------
    
    /** */
    static String formatDatum (Object datum) {
        if ((datum instanceof Integer) || (datum instanceof Long)) {
            return DECIMAL_FORMAT.format(((Number)datum).longValue());
        } else if ((datum instanceof Float) || (datum instanceof Double)) {
            return DECIMAL_FORMAT.format(((Number)datum).doubleValue());
        } else {
            return datum.toString();
        }
    }
    
    //--------------------------------------------------------------------------
    // Constructors
    //--------------------------------------------------------------------------
    
    /** */
    public DBaseBuffer () {
        super(254, Buffer.ByteOrder.LITTLE_ENDIAN);
    }
    
    //--------------------------------------------------------------------------
    // Instance methods
    //--------------------------------------------------------------------------
    
    /** */
    @SuppressWarnings("deprecation")
    public Date getDate (int offset) throws IOException {
        String dateStr = getCString(offset, 8);
        if (dateStr.length() < 8) return(null);
        int year = 1900, month = 1, day = 1;
        try {
            year = Integer.parseInt(dateStr.substring(0, 4));
            month = Integer.parseInt(dateStr.substring(4, 6));
            day = Integer.parseInt(dateStr.substring(6, 8));
        } catch (NumberFormatException e) {
            IOException ioe = new IOException("invalid date value");
            ioe.initCause(e);
            throw ioe;
        }
        return(new Date(year, month, day));
    }
    
    /** */
    @SuppressWarnings("deprecation")
    public int putDate (int offset, Date date) {
        StringBuffer output = new StringBuffer();
        output.append(YEAR_FORMAT.format(date.getYear()));
        output.append(MONTH_DAY_FORMAT.format(date.getMonth()));
        output.append(MONTH_DAY_FORMAT.format(date.getDay()));
        putCString(offset, 8, output.toString());
        return(8);
    }
    
    /** */
    @SuppressWarnings("deprecation")
    public int putHeaderDate (int offset, Date date) {
        putByte(offset, (byte)(date.getYear() - 1900));
        putByte(offset + 1, (byte)(date.getMonth()));
        putByte(offset + 2, (byte)(date.getDay()));
        return(6);
    }
    
    /** */
    public DBaseFieldDescriptor getFieldDescriptor (int offset) {
        return new DBaseFieldDescriptor(getChar(offset + DBF_FIELD_DESCRIPTOR_TYPE_OFFSET),
                                        getCString(offset + DBF_FIELD_DESCRIPTOR_NAME_OFFSET, DBF_FIELD_DESCRIPTOR_NAME_LENGTH),
                                        getUnsignedByte(offset + DBF_FIELD_DESCRIPTOR_LENGTH_OFFSET),
                                        getUnsignedByte(offset + DBF_FIELD_DESCRIPTOR_DECIMAL_COUNT_OFFSET));
    }
    
    /** */
    public int putFieldDescriptor (int offset, DBaseFieldDescriptor field) {
        putCString(offset + DBF_FIELD_DESCRIPTOR_NAME_OFFSET, DBF_FIELD_DESCRIPTOR_NAME_LENGTH, field.getName());
        putChar(offset + DBF_FIELD_DESCRIPTOR_TYPE_OFFSET, field.getType());
        putUnsignedByte(offset + DBF_FIELD_DESCRIPTOR_LENGTH_OFFSET, (short)field.getLength());
        putUnsignedByte(offset + DBF_FIELD_DESCRIPTOR_DECIMAL_COUNT_OFFSET, (short)field.getDecimalCount());
        return DBF_FIELD_DESCRIPTOR_SIZE;
    }
    
    /** */
    public Object[] getRecord (int initialOffset, DBaseFieldDescriptor[] fields) throws IOException {
        Object[] result = new Object[fields.length];
        int offset = initialOffset + 1; // skip the status byte
        for (int i = 0; i < fields.length; i += 1) {
            result[i] = getFieldContents(offset, fields[i]);
            offset += fields[i].getLength();
        }
        return result;
    }
    
    /** */
    public int putRecord (int initialOffset, DBaseFieldDescriptor[] fieldDescriptors, Object[] record) {
        int offset = initialOffset;
        offset += putChar(offset, RECORD_ACTIVE);
        for (int i = 0; i < fieldDescriptors.length; i += 1) {
            offset += putFieldContents(offset, fieldDescriptors[i], record[i]);
        }
        return offset - initialOffset;
    }
    
    /** */
    @SuppressWarnings("deprecation")
    private Object getFieldContents (int offset, DBaseFieldDescriptor field) throws IOException {
        switch (field.getType()) {
            case FIELD_TYPE_CHARACTER:
                return getJustifiedString(offset, field.getLength());
            case FIELD_TYPE_DATE:
                Date date = getDate(offset);
                if (date != null) {
                    return YEAR_FORMAT.format(date.getYear()) + "-" +
                           MONTH_DAY_FORMAT.format(date.getMonth()) + "-" +
                           MONTH_DAY_FORMAT.format(date.getDay());
                } else {
                    return "";
                }
            case FIELD_TYPE_NUMBER:
                String nStr = getJustifiedString(offset, field.getLength());
                if (nStr.equals("")) {
                    return null;
                }
                try {
                    return Double.valueOf(DECIMAL_FORMAT.parse(nStr).doubleValue());
                } catch (ParseException e) {
                    return null;
                }
            case FIELD_TYPE_FLOAT:
                String fStr = getJustifiedString(offset, field.getLength());
                if (fStr.equals("")) {
                    return null;
                }
                try {
                    // Make sure the "e" is upper-case, and don't allow any plus signs, 
                    // because our float format won't parse "1.0e+00x" correctly.
                    return Double.valueOf(FLOAT_FORMAT.parse(fStr.replace('e', 'E').replaceAll("\\+", "")).doubleValue());
                } catch (ParseException e) {
                    return null;
                }
            case FIELD_TYPE_LOGICAL:
                char c = getChar(offset);
                if ("yYtT".indexOf(c) != -1) {
                    return Boolean.TRUE;
                } else if ("nNfF".indexOf(c) != -1) {
                    return Boolean.FALSE;
                } else {
                    return null;
                }
            case FIELD_TYPE_MEMO:
                return null;
            default:
                return null;
        }
    }
    
    /** */
    private int putFieldContents (int offset, DBaseFieldDescriptor field, Object datum) {
        if (datum == null) {
            putJustifiedString(offset, field.getLength(), "", SwingConstants.LEFT);
        } else {
            switch(field.getType()) {
                case FIELD_TYPE_CHARACTER:
                    putJustifiedString(offset, field.getLength(), formatDatum(datum), SwingConstants.LEFT);
                    break;
                case FIELD_TYPE_DATE:
                    putDate(offset, (Date)datum);
                    break;
                case FIELD_TYPE_NUMBER:
                    DECIMAL_PRINT_FORMAT.setMaximumFractionDigits(field.getDecimalCount());
                    DECIMAL_PRINT_FORMAT.setMinimumFractionDigits(field.getDecimalCount());
                    putJustifiedString(offset, field.getLength(), DECIMAL_PRINT_FORMAT.format((Number)datum), SwingConstants.RIGHT);
                    break;
                case FIELD_TYPE_FLOAT:
                    putJustifiedString(offset, field.getLength(), FLOAT_FORMAT.format((Number)datum), SwingConstants.RIGHT);
                    break;
                case FIELD_TYPE_LOGICAL:
                    putChar(offset, datum.equals(Boolean.TRUE) ? 'T' : 'F');
                    break;
                case FIELD_TYPE_MEMO:
                     throw new IllegalArgumentException("memo field not supported");
                default:
                     throw new IllegalArgumentException("invalid field type '"+field.getType()+"'");
            }
        }
        return field.getLength();
    }
}
