//
// Copyright (c) 2000 the National Geographic Society. All rights reserved.
//

package org.myworldgis.io.asciigrid;

import com.vividsolutions.jts.geom.Envelope;
import java.awt.Dimension;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferDouble;
import java.io.BufferedReader;
import java.io.IOException;
import java.text.ParseException;
import java.util.StringTokenizer;
import org.myworldgis.util.StringUtils;


/**
 *
 */
public final strictfp class AsciiGridFileReader implements AsciiGridConstants {
    
    //--------------------------------------------------------------------------
    // Instance variables
    //--------------------------------------------------------------------------
    
    /** */
    private BufferedReader _in;
    
    /** */
    private Dimension _gridSize;
    
    /** */
    private Envelope _gridEnvelope;
    
    /** */
    private double _nanValue;
    
    /** */
    private String _cachedLine;
    
    //--------------------------------------------------------------------------
    // Constructors
    //--------------------------------------------------------------------------
    
    /** */
    public AsciiGridFileReader (BufferedReader in) throws IOException {
        _in = in;
        _nanValue = Double.NaN;
        _cachedLine = null;
        StringTokenizer tokenizer = new StringTokenizer(_in.readLine());
        try {
            int columnCount = 0;
            if (StringUtils.startsWithIgnoreCase(tokenizer.nextToken(), COLUMN_COUNT)) {
                columnCount = Integer.parseInt(tokenizer.nextToken());
            } else {
                throw(new IOException("invalid column count marker on line 1"));
            }
            tokenizer = new StringTokenizer(_in.readLine());
            int rowCount = 0;
            if (StringUtils.startsWithIgnoreCase(tokenizer.nextToken(), ROW_COUNT)) {
                rowCount = Integer.parseInt(tokenizer.nextToken());
            } else {
                throw(new IOException("invalid row count marker on line 2"));
            }
            _gridSize = new Dimension(columnCount, rowCount);
            
            
            tokenizer = new StringTokenizer(_in.readLine());
            double originX = 0.0;
            if (StringUtils.startsWithIgnoreCase(tokenizer.nextToken(), LEFT_LONGITUDE)) {
                originX = Double.parseDouble(tokenizer.nextToken());
            } else {
                throw(new IOException("invalid corner x on line 3"));
            }
            tokenizer = new StringTokenizer(_in.readLine());
            double originY = 0.0;
            if (StringUtils.startsWithIgnoreCase(tokenizer.nextToken(), BOTTOM_LATITUDE)) {
                originY = Double.parseDouble(tokenizer.nextToken());
            } else { 
                throw(new IOException("invalid corner y on line 4"));
            }
            tokenizer = new StringTokenizer(_in.readLine());
            double cellSize = 0.0;
            if (StringUtils.startsWithIgnoreCase(tokenizer.nextToken(), CELL_SIZE)) {
                cellSize = Double.parseDouble(tokenizer.nextToken());
            } else {
                throw(new IOException("invalid cell size on line 5"));
            }
            _gridEnvelope = new Envelope(originX,
                                         originX + (cellSize * columnCount),
                                         originY,
                                         originY + (cellSize * rowCount));
            
            String lastLine = _in.readLine();
            tokenizer = new StringTokenizer(lastLine);
            if (StringUtils.startsWithIgnoreCase(tokenizer.nextToken(), NAN_VALUE)) {
                if (tokenizer.hasMoreTokens()) {
                    _nanValue = Double.parseDouble(tokenizer.nextToken());
                }
            } else {
                _cachedLine = lastLine;
            }
        } catch (NumberFormatException e) {
            IOException ex = new IOException("error parsing number");
            ex.initCause(e);
            throw ex;
        }
    }
    
    //--------------------------------------------------------------------------
    // GridInputMethod implementation
    //--------------------------------------------------------------------------
    
    /** */
    public Dimension getSize () {
        return _gridSize;
    }

    /** */
    public Envelope getEnvelope () {
        return _gridEnvelope;
    }

    /** */
    public double getNaNValue () {
        return _nanValue;
    }
    
    /** */
    public DataBuffer getData () throws IOException {
        int size = _gridSize.width * _gridSize.height;
        DataBufferDouble result = new DataBufferDouble(size);
        int index = 0;
        while (true) {
            String line;
            if (_cachedLine != null) {
                line = _cachedLine;
                _cachedLine = null;
            } else {
                line = _in.readLine();
            }
            if (line == null) {
                break;
            }
            StringTokenizer tokens = new StringTokenizer(line);
            while (tokens.hasMoreTokens()) {
                double value = Double.NaN;
                String token = tokens.nextToken();
                if (token.length() > 0) {
                    try {
                        Number n = VALUE_FORMAT.parse(token);
                        if ((n != null) && (n.doubleValue() != _nanValue)) { 
                            value = n.doubleValue();
                        }   
                    } catch (ParseException e) {
                        // should we report?
                    }
                }
                result.setElemDouble(index++, value);
                if (index >= size) {
                    break;
                }
            }
            if (index >= size) {
                break;
            }
        }
        return result;
    }
    
    /** */
    public void close () throws IOException {
        try {
            _in.close();
        } finally { 
            _in = null;
        }
    }
}
