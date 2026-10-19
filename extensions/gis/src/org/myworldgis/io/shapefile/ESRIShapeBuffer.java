//
// Copyright (c) 2000 the National Geographic Society. All rights reserved.
//

package org.myworldgis.io.shapefile;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import java.io.IOException;
import org.myworldgis.util.Buffer;
import org.myworldgis.util.JTSUtils;
import org.ngs.ngunits.UnitConverter;
import org.myworldgis.io.PointZWrapper;


/**
 * Utilities for reading & writing ESRI shapefiles
 */
public final class ESRIShapeBuffer extends Buffer implements ESRIShapeConstants {


    //--------------------------------------------------------------------------
    // Instance variables
    //--------------------------------------------------------------------------

    /** */
    private UnitConverter _converter;

    /** */
    private GeometryFactory _factory;

    //--------------------------------------------------------------------------
    // Constructors
    //--------------------------------------------------------------------------

    /** */
    public ESRIShapeBuffer (int size, UnitConverter converter, GeometryFactory factory) {
        super(size, Buffer.ByteOrder.LITTLE_ENDIAN);
        _converter = converter;
        _factory = factory;
    }

    //--------------------------------------------------------------------------
    // Instance methods
    //--------------------------------------------------------------------------

    /** */
    public GeometryFactory getGeometryFactory () {
        return _factory;
    }

    /** */
    public void setGeometryFactory (GeometryFactory newFactory) {
        _factory = newFactory;
    }

    /** */
    public UnitConverter getUnitConverter () {
        return _converter;
    }

    /** */
    public void setUnitConverter (UnitConverter newConverter) {
        _converter = newConverter;
    }

    /**
     * Reads a bounding box record.  A bounding box is four double
     * representing, in order, xmin, ymin, xmax, ymax.
     *
     * @param b the raw data buffer
     * @param off the offset into the buffer where the int resides
     * @return the point read from the buffer at the offset location
     */
    public Envelope getBoundingBox (int offset) {
        double min_lon = _converter.convert(getDouble(offset));
        offset += 8;
        double min_lat = _converter.convert(getDouble(offset));
        offset += 8;
        double max_lon = _converter.convert(getDouble(offset));
        offset += 8;
        double max_lat = _converter.convert(getDouble(offset));
        return new Envelope(min_lon, max_lon, min_lat, max_lat);
    }

    /**
     * Writes the given bounding box  to the given buffer at the
     * given location.  The bounding box is written as four doubles
     * representing, in order, xmin, ymin, xmax, ymax.
     *
     * @param b the data buffer
     * @param off the offset into the buffer where writing should occur
     * @param val the bounding box to write
     * @return the number of bytes written
     */
    public int putBoundingBox (int offset, Envelope box) {
        int bytesWritten = 0;
        bytesWritten += putDouble(offset + bytesWritten, _converter.convert(box.getMinX()));
        bytesWritten += putDouble(offset + bytesWritten, _converter.convert(box.getMinY()));
        bytesWritten += putDouble(offset + bytesWritten, _converter.convert(box.getMaxX()));
        bytesWritten += putDouble(offset + bytesWritten, _converter.convert(box.getMaxY()));
        return(bytesWritten);
    }

    /** */
    public Geometry getESRIRecord (int offset) throws IOException {
        setByteOrder(ByteOrder.BIG_ENDIAN);
        //int recordNumber = getInt(offset);
        offset += 4;
        //int contentLength = getInt(offset);
        offset += 4;
        setByteOrder(ByteOrder.LITTLE_ENDIAN);
        int shapeType = getInt(offset);
        offset += 4;
        switch (shapeType) {
            case SHAPE_TYPE_POINT:
                return getESRIPointRecord(offset);
            case SHAPE_TYPE_POINTZ:
                return getESRIPointZRecord(offset);
            case SHAPE_TYPE_POLYGON:
                return getESRIPolygonRecord(offset);
            case SHAPE_TYPE_POLYGONZ:
                return getESRIPolygonZRecord(offset);
            case SHAPE_TYPE_POLYLINE:
                return getESRIPolyLineRecord(offset);
            case SHAPE_TYPE_POLYLINEZ:
                return getESRIPolyLineZRecord(offset);
            case SHAPE_TYPE_MULTIPOINT:
                return getESRIMultiPointRecord(offset);
            case SHAPE_TYPE_MULTIPOINTZ:
                return getESRIMultiPointZRecord(offset);
            default:
                throw new IOException("unsupported shape type");
        }
    }

    /** */
    public Point getESRIPointRecord (int offset) {
        double x = _converter.convert(getDouble(offset));
        offset += 8;
        double y = _converter.convert(getDouble(offset));
        offset += 8;
        return _factory.createPoint(new Coordinate(x, y));
    }

    public PointZWrapper getESRIPointZRecord(int offset) {
        double x = _converter.convert(getDouble(offset));
        offset += 8;
        double y = _converter.convert(getDouble(offset));
        offset += 8;
        double z = _converter.convert(getDouble(offset));
        offset += 16; // 8 for the z coordinate, 8 more for the unused M field. -James Hovet 4/23/21
        return new PointZWrapper(_factory.createPoint(new Coordinate(x, y)), z);
    }

    /** */
    public MultiPoint getESRIMultiPointRecord (int offset) {
        //Envelope envelope = getBoundingBox(offset);
        offset += 32;
        int nPoints = getInt(offset);
        offset += 4;
        Coordinate[] coords = new Coordinate[nPoints];
        for (int i = 0; i < nPoints; i += 1) {
            double x = _converter.convert(getDouble(offset));
            offset += 8;
            double y = _converter.convert(getDouble(offset));
            offset += 8;
            coords[i] = new Coordinate(x, y);
        }
        return _factory.createMultiPointFromCoords(coords);
    }

    public MultiPoint getESRIMultiPointZRecord(int offset) {
        offset += 32;
        int nPoints = getInt(offset);
        offset += 4;
        Coordinate[] coords = new Coordinate[nPoints];
        for (int i = 0; i < nPoints; i += 1) {
            double x = _converter.convert(getDouble(offset));
            offset += 8;
            double y = _converter.convert(getDouble(offset));
            offset += 8;
            coords[i] = new Coordinate(x, y);
        }
        offset += 16; // Zmin and Zmax fields
        offset += 8 * nPoints; // Each Z value
        offset += 16; // Mmin and Mmax fields
        offset += 8 * nPoints; // Each M value

        return _factory.createMultiPointFromCoords(coords);
    }

    /** */
    public MultiLineString getESRIPolyLineRecord (int offset) {
        //Envelope envelope = getBoundingBox(offset);
        offset += 32;
        int nParts = getInt(offset);
        offset += 4;
        int nPoints = getInt(offset);
        offset += 4;
        if ((nParts == 0) || (nPoints < 2)) {
            return _factory.createMultiLineString(null);
        }
        int[] offsets = new int[nParts];
        for (int i = 0; i < nParts; i += 1) {
            offsets[i] = getInt(offset);
            offset += 4;
        }
        LineString[] parts = new LineString[nParts];
        int startIndex = offsets[0];
        for (int i = 0; i < nParts; i += 1) {
            int currentIndex = startIndex;
            int endIndex = (i == (nParts - 1)) ? nPoints : offsets[i+1];
            Coordinate[] coords = new Coordinate[endIndex-startIndex];
            int coordIndex = 0;
            while (currentIndex < endIndex) {
                double x = _converter.convert(getDouble(offset));
                offset += 8;
                double y = _converter.convert(getDouble(offset));
                offset += 8;
                coords[coordIndex++] = new Coordinate(x, y);
                currentIndex += 1;
            }
            parts[i] = _factory.createLineString(coords);
            startIndex = endIndex;
        }
        return _factory.createMultiLineString(parts);
    }

    public MultiLineString getESRIPolyLineZRecord(int offset) {
        offset += 32;
        int nParts = getInt(offset);
        offset += 4;
        int nPoints = getInt(offset);
        offset += 4;
        if ((nParts == 0) || (nPoints < 2)) {
            return _factory.createMultiLineString(null);
        }
        int[] offsets = new int[nParts];
        for (int i = 0; i < nParts; i += 1) {
            offsets[i] = getInt(offset);
            offset += 4;
        }
        LineString[] parts = new LineString[nParts];
        int startIndex = offsets[0];
        for (int i = 0; i < nParts; i += 1) {
            int currentIndex = startIndex;
            int endIndex = (i == (nParts - 1)) ? nPoints : offsets[i+1];
            Coordinate[] coords = new Coordinate[endIndex-startIndex];
            int coordIndex = 0;
            while (currentIndex < endIndex) {
                double x = _converter.convert(getDouble(offset));
                offset += 8;
                double y = _converter.convert(getDouble(offset));
                offset += 8;
                coords[coordIndex++] = new Coordinate(x, y);
                currentIndex += 1;
            }
            parts[i] = _factory.createLineString(coords);
            startIndex = endIndex;
        }

        offset += 16; // Zmin and Zmax fields
        offset += 8 * nPoints; // Each Z value
        offset += 16; // Mmin and Mmax fields
        offset += 8 * nPoints; // Each M value

        return _factory.createMultiLineString(parts);
    }

    /** */
    public MultiPolygon getESRIPolygonRecord (int offset) {
        //Envelope envelope = getBoundingBox(offset);
        offset += 32;
        int nParts = getInt(offset);
        offset += 4;
        int nPoints = getInt(offset);
        offset += 4;
        if ((nParts == 0) || (nPoints < 3)) {
            return _factory.createMultiPolygon(null);
        }
        int[] offsets = new int[nParts];
        for (int i = 0; i < nParts; i += 1) {
            offsets[i] = getInt(offset);
            offset += 4;

        }
        LinearRing[] parts = new LinearRing[nParts];
        int startIndex = offsets[0];
        for (int i = 0; i < nParts; i += 1) {
            int currentIndex = startIndex;
            int endIndex = (i == (nParts - 1)) ? nPoints : offsets[i+1];
            Coordinate[] coords = new Coordinate[endIndex-startIndex];
            int coordIndex = 0;
            while (currentIndex < endIndex) {
                double x = _converter.convert(getDouble(offset));
                offset += 8;
                double y = _converter.convert(getDouble(offset));
                offset += 8;
                coords[coordIndex++] = new Coordinate(x, y);
                currentIndex += 1;
            }
            parts[i] = _factory.createLinearRing(coords);
            startIndex = endIndex;
        }
        return JTSUtils.buildPolygonGeometry(parts, _factory, true);
    }

    public MultiPolygon getESRIPolygonZRecord (int offset) {
        offset += 32;
        int nParts = getInt(offset);
        offset += 4;
        int nPoints = getInt(offset);
        offset += 4;
        if ((nParts == 0) || (nPoints < 3)) {
            return _factory.createMultiPolygon(null);
        }
        int[] offsets = new int[nParts];
        for (int i = 0; i < nParts; i += 1) {
            offsets[i] = getInt(offset);
            offset += 4;

        }
        LinearRing[] parts = new LinearRing[nParts];
        int startIndex = offsets[0];
        for (int i = 0; i < nParts; i += 1) {
            int currentIndex = startIndex;
            int endIndex = (i == (nParts - 1)) ? nPoints : offsets[i+1];
            Coordinate[] coords = new Coordinate[endIndex-startIndex];
            int coordIndex = 0;
            while (currentIndex < endIndex) {
                double x = _converter.convert(getDouble(offset));
                offset += 8;
                double y = _converter.convert(getDouble(offset));
                offset += 8;
                coords[coordIndex++] = new Coordinate(x, y);
                currentIndex += 1;
            }
            parts[i] = _factory.createLinearRing(coords);
            startIndex = endIndex;
        }

        offset += 16; // Zmin and Zmax fields
        offset += 8 * nPoints; // Each Z value
        offset += 16; // Mmin and Mmax fields
        offset += 8 * nPoints; // Each M value

        return JTSUtils.buildPolygonGeometry(parts, _factory, true);
    }

    /** */
    public int putESRIRecord (int offset, Geometry shape, int shapeType, int index) {
        switch (shapeType) {
            case SHAPE_TYPE_POINT:
                return(putESRIPointRecord(offset, (Point)shape, index + 1));
            case SHAPE_TYPE_POLYLINE:
                return(putESRIPolyLineRecord(offset, (MultiLineString)shape, index + 1));
            case SHAPE_TYPE_POLYGON:
                return(putESRIPolygonRecord(offset, (MultiPolygon)shape, index + 1));
            case SHAPE_TYPE_MULTIPOINT:
                return(putESRIMultiPointRecord(offset, (MultiPoint)shape, index + 1));
            default:
                throw(new IllegalArgumentException("features of type '" + shape.getClass().getName() + "' can't be written to a ShapeFile"));
        }
    }

    /** */
    public int putESRIPointRecord (int offset, Point shape, int index) {
        int bytesWritten = 0;
        setByteOrder(ByteOrder.BIG_ENDIAN);
        bytesWritten += putInt(offset + bytesWritten, index);
        bytesWritten += putInt(offset + bytesWritten, 10);
        setByteOrder(ByteOrder.LITTLE_ENDIAN);
        bytesWritten += putInt(offset + bytesWritten, SHAPE_TYPE_POINT);
        bytesWritten += putDouble(offset + bytesWritten, _converter.convert(shape.getX()));
        bytesWritten += putDouble(offset + bytesWritten, _converter.convert(shape.getY()));
        return bytesWritten;
    }

    /** */
    public int putESRIMultiPointRecord (int offset, MultiPoint shape, int index) {
        int bytesWritten = 0;
        setByteOrder(ByteOrder.BIG_ENDIAN);
        bytesWritten += putInt(offset + bytesWritten, index);
        int recordLength = 20 + (shape.getNumGeometries() * 8);
        ensureCapacity((recordLength * 2) + 8);
        bytesWritten += putInt(offset + bytesWritten, recordLength);
        setByteOrder(ByteOrder.LITTLE_ENDIAN);
        bytesWritten += putInt(offset + bytesWritten, SHAPE_TYPE_MULTIPOINT);
        bytesWritten += putBoundingBox(offset + bytesWritten, shape.getEnvelopeInternal());
        bytesWritten += putInt(offset + bytesWritten, shape.getNumGeometries());
        for (int i = 0; i < shape.getNumGeometries(); i += 1) {
            Point pt = (Point)shape.getGeometryN(i);
            bytesWritten += putDouble(offset + bytesWritten, _converter.convert(pt.getX()));
            bytesWritten += putDouble(offset + bytesWritten, _converter.convert(pt.getY()));
        }
        return bytesWritten;
    }

    /** */
    public int putESRIPolyLineRecord (int offset, MultiLineString shape, int index) {
        int bytesWritten = 0;
        setByteOrder(ByteOrder.BIG_ENDIAN);
        bytesWritten += putInt(offset + bytesWritten, index);
        int segmentCount = shape.getNumGeometries();
        int pointCount = shape.getNumPoints();
        int recordLength = 22 + (segmentCount * 2) + (pointCount * 8);
        ensureCapacity((recordLength * 2) + 8);
        bytesWritten += putInt(offset + bytesWritten, recordLength);
        setByteOrder(ByteOrder.LITTLE_ENDIAN);
        bytesWritten += putInt(offset + bytesWritten, SHAPE_TYPE_POLYLINE);
        bytesWritten += putBoundingBox(offset + bytesWritten, shape.getEnvelopeInternal());
        bytesWritten += putInt(offset + bytesWritten, segmentCount);
        bytesWritten += putInt(offset + bytesWritten, pointCount);
        int segmentOffset = 0;
        for (int i = 0; i < segmentCount; i += 1) {
            bytesWritten += putInt(offset + bytesWritten, segmentOffset);
            segmentOffset += shape.getGeometryN(i).getNumPoints();
        }
        for (int i = 0; i < segmentCount; i += 1) {
            LineString ls = (LineString)shape.getGeometryN(i);
            for (int j = 0; j < ls.getNumPoints(); j += 1) {
                Point pt = ls.getPointN(j);
                bytesWritten += putDouble(offset + bytesWritten, _converter.convert(pt.getX()));
                bytesWritten += putDouble(offset + bytesWritten, _converter.convert(pt.getY()));
            }
        }
        return bytesWritten;
    }

    /** */
    public int putESRIPolygonRecord (int offset, MultiPolygon shape, int index) {
        int bytesWritten = 0;
        setByteOrder(ByteOrder.BIG_ENDIAN);
        bytesWritten += putInt(offset + bytesWritten, index);
        GeometryCollection processedShape = JTSUtils.explodeMultiPolygon(shape);
        int ringCount = processedShape.getNumGeometries();
        int pointCount = processedShape.getNumPoints();
        int recordLength = 22 + (ringCount * 2) + (pointCount * 8);
        ensureCapacity((recordLength * 2) + 8);
        bytesWritten += putInt(offset + bytesWritten, recordLength);
        setByteOrder(ByteOrder.LITTLE_ENDIAN);
        bytesWritten += putInt(offset + bytesWritten, SHAPE_TYPE_POLYGON);
        bytesWritten += putBoundingBox(offset + bytesWritten, shape.getEnvelopeInternal());
        bytesWritten += putInt(offset + bytesWritten, ringCount);
        bytesWritten += putInt(offset + bytesWritten, pointCount);
        int ringOffset = 0;
        for (int i = 0; i < ringCount; i += 1) {
            bytesWritten += putInt(offset + bytesWritten, ringOffset);
            ringOffset += processedShape.getGeometryN(i).getNumPoints();
        }
        for (int i = 0; i < ringCount; i += 1) {
            LinearRing lr = (LinearRing)processedShape.getGeometryN(i);
            for (int j = 0; j < lr.getNumPoints(); j += 1) {
                Point pt = lr.getPointN(j);
                bytesWritten += putDouble(offset + bytesWritten, _converter.convert(pt.getX()));
                bytesWritten += putDouble(offset + bytesWritten, _converter.convert(pt.getY()));
            }
        }
        return bytesWritten;
    }
}
