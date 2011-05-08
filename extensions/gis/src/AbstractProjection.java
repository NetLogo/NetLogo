//
// Copyright (c) 2000 the National Geographic Society. All rights reserved.
//

package org.myworldgis.projection;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateFilter;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.util.GeometryTransformer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.myworldgis.util.GeometryUtils;
import org.myworldgis.util.JTSUtils;


/**
 * Abstract base class for Projections.
 * 
 * All parameters, unless otherwise noted, are in RADIANS
 */
public abstract strictfp class AbstractProjection implements Projection {

    //--------------------------------------------------------------------------
    // Inner classes
    //--------------------------------------------------------------------------
    
    /** */
    private static class ForwardTransformer extends GeometryTransformer {
        
        private final AbstractProjection _proj;
        private final CoordinateFilter _filter;
        
        public ForwardTransformer (AbstractProjection proj) {
            _proj = proj;
            _filter = new CoordinateFilter() {
                    private Coordinate temp = new Coordinate();
                    public void filter (Coordinate coord) {
                        temp = _proj.forwardPoint(coord.x, coord.y, temp);
                        coord.setCoordinate(temp);
                    }
                };
        }
        
        protected Geometry transformPoint (Point geom, Geometry parent) {
            geom = _proj.process(geom);
            geom.apply(_filter);
            geom.geometryChanged();
            return geom;
        }
        
        protected Geometry transformLineString (LineString geom, Geometry parent) {
            Geometry result = _proj.process(geom);
            result.apply(_filter);
            result.geometryChanged();
            return result;
        }
        
        protected Geometry transformMultiLineString(MultiLineString geom, Geometry parent) {
            List<LineString> lines = new ArrayList<LineString>();
            for (int i = 0; i < geom.getNumGeometries(); i += 1) {
                MultiLineString mls = _proj.process((LineString)geom.getGeometryN(i));
                mls.apply(_filter);
                mls.geometryChanged();
                for (int j = 0; j < mls.getNumGeometries(); j += 1) {
                    lines.add((LineString)mls.getGeometryN(j));
                }
            }
            return factory.createMultiLineString(GeometryFactory.toLineStringArray(lines));
        }
        
        protected Geometry transformPolygon (Polygon geom, Geometry parent) {
            Geometry result = _proj.process(geom);
            result.apply(_filter);
            result.geometryChanged();
            return JTSUtils.repair(result);
        }
        
        protected Geometry transformMultiPolygon(MultiPolygon geom, Geometry parent) {
            List<Polygon> polygons = new ArrayList<Polygon>();
            for (int i = 0; i < geom.getNumGeometries(); i += 1) {
                MultiPolygon mp = _proj.process((Polygon)geom.getGeometryN(i));
                mp.apply(_filter);
                mp.geometryChanged();
                for (int j = 0; j < mp.getNumGeometries(); j += 1) {
                    polygons.add(JTSUtils.repair((Polygon)mp.getGeometryN(j)));
                }
            }
            return factory.createMultiPolygon(GeometryFactory.toPolygonArray(polygons));
        }
    }

    /** */
    private static class InverseTransformer extends GeometryTransformer {
        
        private final AbstractProjection _proj;
        
        public InverseTransformer (AbstractProjection proj) {
            _proj = proj;
        }
        
        protected CoordinateSequence transformCoordinates(CoordinateSequence coords, Geometry parent) {
            List<Coordinate> newCoords = new LinkedList<Coordinate>();
            for (int i = 0; i < coords.size(); i += 1) {
                Coordinate c = _proj.inversePoint(coords.getX(i), coords.getY(i), new Coordinate());
                if ((!Double.isNaN(c.x)) &&
                    (!Double.isNaN(c.y)) &&
                    (StrictMath.abs(c.x) <= StrictMath.PI) ||
                    (StrictMath.abs(c.y) <= GeometryUtils.HALF_PI)) {
                    newCoords.add(c);
                }
            }
            return createCoordinateSequence(newCoords.toArray(new Coordinate[newCoords.size()]));
        }
        
        protected Geometry transformLinearRing(LinearRing geom, Geometry parent) {
            CoordinateSequence seq = transformCoordinates(geom.getCoordinateSequence(), geom);
            int seqSize = seq.size();
            if ((seqSize > 0) && (seqSize < 4)) {
                return factory.createLineString(seq);
            } else if ((seqSize > 2) && (!seq.getCoordinate(0).equals2D(seq.getCoordinate(seqSize - 1)))) {
                // sometimes, dropping out points above creates a linear
                // ring that's no longer closed, which makes JTS very sad
                Coordinate[] newArray = new Coordinate[seqSize+1];
                for (int i = 0; i < seqSize; i += 1) {
                    newArray[i] = seq.getCoordinate(i);
                }
                newArray[seqSize] = seq.getCoordinate(0);
                seq = createCoordinateSequence(newArray);
            }
            return factory.createLinearRing(seq);
        }
    }
    
    //--------------------------------------------------------------------------
    // Instance variables
    //--------------------------------------------------------------------------
    
    /** */
    protected String _name;
    
    /** */
    protected LineType _lineType;
    
    /** Ellipsoid used for this projection. */
    protected Ellipsoid _ellipsoid;
    
    /** Equatorial radius in METERS */
    protected double _a;
    
    /** Ellipsoid eccentricity squared. */
    protected double _e2;
    
    /** Projection center latitude in RADIANS. */
    protected double _phi0;
    
    /** Projection center longitude in RADIANS. */
    protected double _lambda0;
    
    //--------------------------------------------------------------------------
    // Constructors
    //--------------------------------------------------------------------------
    
    /**
     * Construct an AbstractProjection.
     * @param ellipsoid the ellipsoid for the projection
     * @param center the center of the projection
     * @param units the linear units of the projected data
     * @param falseEasting value to add to x coordinate of each projected point, in METERS
     * @param falseNorthing value to add to y coordinate of each projected point, in METERS
     */
    public AbstractProjection (Ellipsoid ellipsoid, 
                               Coordinate center) {
        this(ellipsoid, center.x, center.y);
    }
    
    /** */
    public AbstractProjection (Ellipsoid ellipsoid,
                               double lambda0,
                               double phi0) {
        _name = "";
        _lineType = LineType.STRAIGHT;
        _ellipsoid = ellipsoid;
        _a = _ellipsoid.radius;
        _e2 = ellipsoid.eccsq;
        _phi0 = phi0;
        _lambda0 = lambda0;
    }
    
    //--------------------------------------------------------------------------
    // Abstract instance methods
    //--------------------------------------------------------------------------
    
    /** */
    public abstract Point process (Point point);
    
    /** */
    public abstract MultiPolygon process (Polygon poly);
    
    /** */
    public abstract MultiLineString process (LineString line);
    
    /** */
    protected abstract Coordinate forwardPoint (double lon, double lat, Coordinate storage);
    
    /** 
     * Inverse projects a point, ignoring <code>_units</code> and 
     * any false easting and/or northing.
     * @param x the x coordinate (in projected units) of the point to be inverse projected
     * @param y the y coordinate (in projected units) of the point to be inverse projected
     * @param storage a place to store the result
     * @return The inverse projected point. Same object as <code>storage</code>.
     */
    protected abstract Coordinate inversePoint (double x, double y, Coordinate storage);
    
    /** 
     * Called to initialize parameters, and to recompute them whenever the 
     * ellipsoid or projection center changes.
     */
    protected abstract void computeParameters ();
    
    //--------------------------------------------------------------------------
    // Instance methods
    //--------------------------------------------------------------------------
    
    /** */
    public String toString () {
        return _name;
    }
    
    /** */
    public boolean equals (Object obj) {
        if ((obj != null) && obj.getClass().equals(this.getClass())) {
            AbstractProjection proj = (AbstractProjection)obj;
            return (proj._ellipsoid.equals(this._ellipsoid)) &&
                   (StrictMath.abs(proj._phi0 - this._phi0) < GeometryUtils.EPSILON) &&
                   (StrictMath.abs(proj._lambda0 - this._lambda0) < GeometryUtils.EPSILON);
        } else {
            return(false);
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
    // Projection implementation
    //--------------------------------------------------------------------------
    
    /** */
    public LineType getLineType () {
        return _lineType;
    }
    
    /** */
    public void setLineType (LineType newLineType) {
        if (newLineType != _lineType) {
            _lineType = newLineType;
        }
    }
    
    /**
     * Get the reference ellipsoid used for projection calculations.
     * @return the current ellipsoid.
     */
    public Ellipsoid getEllipsoid () {
        return _ellipsoid;
    }
    
    /**
     * Set the reference ellipsoid used for projection calculations.
     * @param newEllipsoid the new ellipsoid
     */
    public void setEllipsoid (Ellipsoid newEllipsoid) {
        Ellipsoid oldEllipsoid = _ellipsoid;
        if (newEllipsoid != oldEllipsoid) {
            _ellipsoid = newEllipsoid;
            _a =  _ellipsoid.radius;
            _e2 = _ellipsoid.eccsq;
            computeParameters();
        }
    }
    
    /**
     * Get the center of this projection.
     * @return the center of this projection.
     */
    public Coordinate getCenter () {
        return new Coordinate(_lambda0, _phi0);
    }
    
    /**
     * Set the center of this projection.
     * @param newCenter the new center of this projection.
     */
    public void setCenter (Coordinate newCenter) {
        if ((!Double.isNaN(newCenter.x)) &&
            (!Double.isNaN(newCenter.y))) {
            _phi0 = newCenter.y;
            _lambda0 = newCenter.x;
            computeParameters();
        }
    }
    
    /** */
    public double getCenterEasting () {
        return 0.0;
    }
    
    /** */
    public double getCenterNorthing () {
        return 0.0;
    }
    
    /** */
    public boolean isRhumbRectangular () {
        return false;
    }
    
    /** */
    public GeometryTransformer getForwardTransformer () {
        return new ForwardTransformer((AbstractProjection)clone());
    }
    
    /** */
    public GeometryTransformer getInverseTransformer () {
        return new InverseTransformer((AbstractProjection)clone());
    }
}
