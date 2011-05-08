//
// Copyright (c) 2000 the National Geographic Society. All rights reserved.
//

package org.myworldgis.projection;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import java.text.ParseException;
import javax.measure.quantity.Length;
import javax.measure.unit.Unit;
import org.myworldgis.util.GeometryUtils;


/**
 * Abstract class representing a projection whose domain is restricted to
 * points within a given angular distance from a center point. Clips
 * polylines & polygons to that restricted domain before projecting.
 * Superclass of both Azimuthal and Conic projection families.
 */
public abstract strictfp class HemisphericalProjection extends AbstractProjectedProjection {
    
    //-------------------------------------------------------------------------
    // Instance variables
    //-------------------------------------------------------------------------
    
    /** Latitude of the center of the clipping hemisphere */
    private double _hPhi1;
    
    /** Longitude of the center of the clipping hemisphere */
    private double _hLambda0;
    
    //-------------------------------------------------------------------------
    // Constructors
    //-------------------------------------------------------------------------
    
    /**
     * Construct a HemisphericalProjection.
     * @param ellipsoid the ellipsoid for the projection
     * @param center the center of the projection
     * @param falseEasting value to add to x coordinate of each projected point, in projected units
     * @param falseNorthing value to add to y coordinate of each projected point, in projected units
     */
    public HemisphericalProjection (Ellipsoid ellipsoid, 
                                    Coordinate center, 
                                    Unit<Length> units,
                                    double falseEasting,
                                    double falseNorthing) {
        super(ellipsoid, center, units, falseEasting, falseNorthing);
    }
    
    /** */
    public HemisphericalProjection (Ellipsoid ellipsoid, ProjectionParameters parameters) 
            throws ParseException {
        super(ellipsoid, parameters);
    }
    
    //-------------------------------------------------------------------------
    // Abstract instance methods
    //-------------------------------------------------------------------------
    
    /** 
     * Get the center of the clipping hemisphere.
     * @return the center of the clipping hemisphere
     */
    protected abstract Coordinate getHemisphereCenter ();
    
    /**
     * Get the maximum angular distance from the center of the clipping 
     * hemisphere to which polylines & polygons are to be clipped.
     * @return the radius of the clipping hemisphere
     */
    protected abstract double getMaxC ();
    
    //-------------------------------------------------------------------------
    // Instance methods
    //-------------------------------------------------------------------------
    
    /** */
    protected Coordinate inversePoint (double x, double y, Coordinate storage) {
        storage = super.inversePoint(x, y, storage);
        Coordinate center = getHemisphereCenter();
        double c = GeometryUtils.point_point_greatcircle_distance(center.x, center.y, storage.x, storage.y);
        if (c > getMaxC()) {
            storage.x = Double.NaN;
            storage.y = Double.NaN;
        }
        return storage;
    }
    
    /** */
    public boolean equals (Object obj) {
        if (super.equals(obj)) {
            HemisphericalProjection proj = (HemisphericalProjection)obj;
            return (StrictMath.abs(proj._hPhi1 - this._hPhi1) < GeometryUtils.EPSILON) &&
                   (StrictMath.abs(proj._hLambda0 - this._hLambda0) < GeometryUtils.EPSILON) &&
                   (StrictMath.abs(proj.getMaxC() - this.getMaxC()) < GeometryUtils.EPSILON);
        } else {
            return(false);
        }
    }
    
    //-------------------------------------------------------------------------
    // AbstractProjection implementation
    //-------------------------------------------------------------------------
    
    /** */
    public Point process (Point point) {
        if (!point.isEmpty()) {
            Coordinate center = getHemisphereCenter();
            double c = GeometryUtils.point_point_greatcircle_distance(center.x, 
                                                                      center.y, 
                                                                      point.getX(), 
                                                                      point.getY());
            if (c > getMaxC()) {
                return point.getFactory().createPoint((Coordinate)null);
            }
        }
        return point;
    }
    
    /** */
    public MultiPolygon process (Polygon poly) {
        if (_lineType == LineType.RHUMB) {
            poly = ProjectionUtils.createRhumbPoly(poly);
        } else if ((_lineType == LineType.GREATCIRCLE) && (!(this instanceof Gnomonic))) {
            poly = ProjectionUtils.createGreatCirclePoly(poly);
        }
        return ProjectionUtils.clip (poly, getHemisphereCenter(), getMaxC());
    }
    
    /** */
    public MultiLineString process (LineString line) {
        if (_lineType == LineType.RHUMB) {
            line = ProjectionUtils.createRhumbLine(line);
        } else if ((_lineType == LineType.GREATCIRCLE) && (!(this instanceof Gnomonic))) {
            line = ProjectionUtils.createGreatCircleLine(line);
        }
        return ProjectionUtils.clip(line, getHemisphereCenter(), getMaxC());
    }
    
    /** 
     * Initialize parameters, and recompute them whenever the ellipsoid or 
     * projection center changes.
     */
    protected void computeParameters () {
        Coordinate center = getHemisphereCenter();
        _hPhi1 = center.y;
        _hLambda0 = center.x;
    }
}
