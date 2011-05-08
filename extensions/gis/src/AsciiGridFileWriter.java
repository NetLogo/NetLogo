//
// Copyright (c) 2000 the National Geographic Society. All rights reserved.
//

package org.myworldgis.io.asciigrid;

import com.vividsolutions.jts.geom.Envelope;
import java.awt.Dimension;
import java.awt.image.DataBuffer;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;


/**
 *
 */
public final strictfp class AsciiGridFileWriter implements AsciiGridConstants {
    
    //--------------------------------------------------------------------------
    // Instance variables
    //--------------------------------------------------------------------------
    
    /** */
    private PrintWriter _out;
    
    /** */
    private int _columnCount;
    
    /** */
    private String _nanString;
    
    //--------------------------------------------------------------------------
    // Constructors
    //--------------------------------------------------------------------------
    
    /** */
    public AsciiGridFileWriter (Writer out) throws IOException {
        _out = new PrintWriter(out);
    }
    
    //--------------------------------------------------------------------------
    // Instance methods
    //--------------------------------------------------------------------------
    
    /** */
    public void writeGridInfo (Dimension size, Envelope envelope, double nanValue) {
        _columnCount = size.width;
        _out.print(COLUMN_COUNT);
        _out.print(" ");
        _out.print(Integer.toString(_columnCount));
        _out.println();
        
        _out.print(ROW_COUNT);
        _out.print(" ");
        _out.print(Integer.toString(size.height));
        _out.println();
        
        _out.print(LEFT_LONGITUDE);
        _out.print(" ");
        _out.print(DIMENSION_FORMAT.format(envelope.getMinX()));
        _out.println();
        
        _out.print(BOTTOM_LATITUDE);
        _out.print(" ");
        _out.print(DIMENSION_FORMAT.format(envelope.getMinY()));
        _out.println();
        
        double cellWidth = envelope.getWidth() / size.width;
        double cellHeight = envelope.getHeight() / size.height;
        _out.print(CELL_SIZE);
        _out.print(" ");
        _out.print(DIMENSION_FORMAT.format(StrictMath.min(cellWidth, cellHeight)));
        _out.println();
        
        if (Double.isNaN(nanValue)) {
            _nanString = "NaN";
        } else {
            _nanString = VALUE_FORMAT.format(nanValue);
        }
        _out.print(NAN_VALUE);
        _out.print(" ");
        _out.print(_nanString);
        _out.println();
    }
    
    /** */
    public void writeGridData (DataBuffer data) throws IOException {
        for (int i = 0, column = 0; i < data.getSize(); i += 1, column += 1) {
            if (column == _columnCount) {
                _out.println();
                column = 0;
            }
            double value = data.getElemDouble(i);
            if (Double.isNaN(value)) {
                _out.print(_nanString);
            } else {
                _out.print(VALUE_FORMAT.format(value));
            }
            _out.print(" ");
        }
    }
    
    /** */
    public void close () {
        _out.close();
        _out = null;
    }
}
