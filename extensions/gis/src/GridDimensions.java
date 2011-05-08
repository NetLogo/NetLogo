//
// Copyright (c) 2007 Eric Russell. All rights reserved.
//

package org.myworldgis.netlogo;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import java.awt.Dimension;
import org.nlogo.api.ExtensionObject;


/**
 * 
 */
public final strictfp class GridDimensions implements Cloneable, ExtensionObject {
    
    //--------------------------------------------------------------------------
    // Instance variables
    //--------------------------------------------------------------------------
    
    /** */
    private final Dimension _size;
    
    /** */
    private final Envelope _envelope;
    
    //--------------------------------------------------------------------------
    // Constructors
    //--------------------------------------------------------------------------
    
    /** */
    public GridDimensions (int gridWidth,
                           int gridHeight,
                           float cellWidth,
                           float cellHeight,
                           float originX, 
                           float originY) {
        _size = new Dimension(gridWidth, gridHeight);
        _envelope = new Envelope(originX, 
                                 originX + (gridWidth * cellWidth),
                                 originY,
                                 originY + (gridHeight * cellHeight));
    }
    
    /** */
    public GridDimensions (Dimension size, Envelope envelope) {
        _size = new Dimension(size.width, size.height);
        _envelope = new Envelope(envelope);
    }
    
    //--------------------------------------------------------------------------
    // Instance methods
    //--------------------------------------------------------------------------
    
    /** */
    public int getGridWidth () {
        return _size.width;
    }
    
    /** */
    public int getGridHeight () {
        return _size.height;
    }
    
    /** */
    public int getCellCount () {
        return (_size.width * _size.height);
    }
    
    /** Cell width */
    public double getCellWidth () {
        return _envelope.getWidth() / _size.width;
    }
    
    /** Cell height */
    public double getCellHeight () {
        return _envelope.getHeight() / _size.height;
    }
    
    /** Left edge of grid */
    public double getLeft () {
        return _envelope.getMinX();
    }
    
    /** Right edge of grid */
    public double getRight () {
        return _envelope.getMaxX();
    }
    
    /** Bottom edge of grid */
    public double getBottom () {
        return _envelope.getMinY();
    }
    
    /** Top edge of grid */
    public double getTop () {
        return _envelope.getMaxY();
    }
    
    /** */
    public double getWidth () {
        return _envelope.getWidth();
    }
    
    /** */
    public double getHeight () {
        return _envelope.getHeight();
    }
    
    /** */
    public double getColumnLeft (int column) {
        return _envelope.getMinX() + (getCellWidth() * column);
    }
    
    /** */
    public double getColumnCenter (int column) {
        double cellWidth = getCellWidth();
        return _envelope.getMinX() + (cellWidth * column) + (cellWidth * 0.5);
    }
    
    /** */
    public double getColumnRight (int column) {
        return _envelope.getMinX() + (getCellWidth() * (column + 1));
    }
    
    /** */
    public double getRowBottom (int row) {
        return _envelope.getMinY() + (getCellHeight() * row);
    }
    
    /** */
    public double getRowCenter (int row) {
        double cellHeight = getCellHeight();
        return _envelope.getMinY() + (cellHeight * row) + (cellHeight * 0.5);
    }
    
    /** */
    public double getRowTop (int row) {
        return _envelope.getMinY() + (getCellHeight() * (row + 1));
    }
    
    /** */
    public int getColumn (double x) {
        int column = (int)StrictMath.floor((x - _envelope.getMinX()) / getCellWidth());
        if ((column >= 0) && (column < _size.width)) {
            return column;
        } else {
            return -1;
        }
    }
    
    /** */
    public int getRow (double y) {
        int row = (int)StrictMath.floor((y - _envelope.getMinY()) / getCellHeight());
        if ((row >= 0) && (row < _size.height)) {
            return row;
        } else {
            return -1;
        }
    }
    
    /** */
    public Dimension getGridSize() {
        return new Dimension(_size);
    }
    
    /** */
    public Envelope getEnvelope () {
        return new Envelope(_envelope);
    }
    
    /** */
    public Coordinate gisToGrid (Coordinate coord, Coordinate storage) {                               
        if (storage == null) {
            storage = new Coordinate();
        }
        double gridX = (coord.x - _envelope.getMinX()) / getCellWidth();
        if ((gridX >= 0) && (gridX <= _size.width)) {
            storage.x = gridX;
        } else {
            storage.x = Double.NaN;
        }
        double gridY = (coord.y - _envelope.getMinY()) / getCellHeight();
        if ((gridY >= 0) && (gridY <= _size.height)) {
            storage.y = gridY;
        } else {
            storage.y = Double.NaN;
        }
        return storage;
    }

    /** */
    public Coordinate gridToGIS (Coordinate coord, Coordinate storage) {
       if (storage == null) {
           storage = new Coordinate();
       }
       if ((coord.x >= 0) && (coord.x <= _size.width)) {
           storage.x = _envelope.getMinX() + (coord.x * getCellWidth());
       } else {
           storage.x = Double.NaN;
       }
       if ((coord.y >= 0.0) && (coord.y <= _size.height)) {
           storage.y = _envelope.getMinY() + (coord.y * getCellHeight());
       } else {
           storage.y = Double.NaN;
       }
       return storage;
    }
   
    /** */
    public String toString () {
        StringBuffer buffer = new StringBuffer();
        buffer.append("GridDimensions[width=");
        buffer.append(_size.width);
        buffer.append(",height=");
        buffer.append(_size.height);
        buffer.append(",");
        buffer.append(_envelope.toString());
        buffer.append("]");
        return buffer.toString();
    }
    
    /** */
    public boolean equals (Object obj) {
        if (obj instanceof GridDimensions) {
            GridDimensions dim = (GridDimensions)obj;
            return dim._size.equals(this._size) &&
                   dim._envelope.equals(this._envelope);
        } else {
            return false;
        }
    }
    
    //--------------------------------------------------------------------------
    // Cloneable implementation
    //--------------------------------------------------------------------------
    
    /** */
    public Object clone () {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            InternalError err = new InternalError("this should never happen");
            err.initCause(e);
            throw err;
        }
    }
    
    //--------------------------------------------------------------------------
    // ExtensionObject implementation
    //--------------------------------------------------------------------------
    
    /**
     * Returns a string representation of the object.  If readable is
     * true, it should be possible read it as NL code.
     *
     **/
    public String dump (boolean readable, boolean exporting, boolean reference ) {
        return "";
    }

    /** */
    public String getExtensionName () {
        return "gis";
    }

    /** */
    public String getNLTypeName() {
        return "GridDimensions";
    }

    /** */
    public boolean recursivelyEqual (Object obj) {
        if (obj instanceof VectorDataset) {
            GridDimensions gd = (GridDimensions)obj;
            return gd == this;
        } else {
            return false;
        }
    }
}
