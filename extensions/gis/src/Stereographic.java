//
// Copyright (c) 2000 the National Geographic Society. All rights reserved.
//

package org.myworldgis.projection;

import com.vividsolutions.jts.geom.Coordinate;
import java.text.ParseException;
import javax.measure.quantity.Length;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;
import org.myworldgis.util.GeometryUtils;


/**
 * Stereographic projection.
 * Formulas from Snyder, John P. (1987). "Map Projections -- A Working Manual".
 * US Geological Survey Professional Paper 1395, US Government Printing Office,
 * Washington, DC. pp. 154-163
 */
public final strictfp class Stereographic extends Azimuthal {
    
    //--------------------------------------------------------------------------
    // Class variables
    //--------------------------------------------------------------------------
    
    /** */
    public static final String WKT_NAME = "Stereographic";
    
    /** */
    public static final String CENTER_LON_PROPERTY = "central_meridian";
    
    /** */
    public static final String CENTER_LAT_PROPERTY = "latitude_of_origin";
    
    /** */
    public static final String SCALE_FACTOR_PROPERTY = "scale_factor";
    
    //-------------------------------------------------------------------------
    // Instance variables
    //-------------------------------------------------------------------------
    
    /** Scale factor at the center of the projection, usually 1.0 */
    private double _k0;
    
    //-------------------------------------------------------------------------
    // Constructors
    //-------------------------------------------------------------------------
    
    /**
     * Construct a Stereographic projection.
     * @param ellipsoid the ellipsoid for the projection
     * @param center the center of the projection
     * @param units the linear units of the projected data
     * @param falseEasting value to add to x coordinate of each projected point, in projected units
     * @param falseNorthing value to add to y coordinate of each projected point, in projected units
     */
    public Stereographic (Ellipsoid ellipsoid, 
                          Coordinate center, 
                          Unit<Length> units,
                          double falseEasting,
                          double falseNorthing) {
        this(ellipsoid, center, units, falseEasting, falseNorthing, 1.0);
    }
    
    /**
     * Construct a Stereographic projection.
     * @param ellipsoid the ellipsoid for the projection
     * @param center the center of the projection
     * @param units the linear units of the projected data
     * @param falseEasting value to add to x coordinate of each projected point, in projected units
     * @param falseNorthing value to add to y coordinate of each projected point, in projected units
     * @param k0 the scale factor at the center of the projection. 1.0 by default.
     */
    public Stereographic (Ellipsoid ellipsoid, 
                          Coordinate center, 
                          Unit<Length> units,
                          double falseEasting,
                          double falseNorthing,
                          double k0) {
        super(ellipsoid, center, units, falseEasting, falseNorthing);
        _name = WKT_NAME;
        _k0 = k0;
        computeParameters();
    }
    
    /** */
    public Stereographic (Ellipsoid ellipsoid, ProjectionParameters parameters) 
            throws ParseException {
        super(ellipsoid, parameters);
        _name = WKT_NAME;
        _k0 = parameters.getDimensionlessParameter(SCALE_FACTOR_PROPERTY);
        computeParameters();
    }
    
    //--------------------------------------------------------------------------
    // Instance methods
    //-------------------------------------------------------------------------
    
    /** */
    public double getCenterScaleFactor () {
        return _k0;
    }
    
    /** */
    public void setCenterScaleFactor (double newScale) {
        if (newScale != _k0) {
            _k0 = newScale;
        }
    }
    
    /** */
    public boolean equals (Object obj) {
        if (super.equals(obj)) {
            Stereographic proj = (Stereographic)obj;
            return (StrictMath.abs(proj._k0 - this._k0) < GeometryUtils.EPSILON);
        } else {
            return(false);
        }
    }
    
    //-------------------------------------------------------------------------
    // HemisphericalProjection implementation
    //-------------------------------------------------------------------------
    
    /** 
     * Returns the maximum angular distance from the center of the clipping
     * hemisphere to which polylines & polygons are clipped.
     * Our clipping hemisphere has a radius of pi/2, so it's a proper hemisphere.
     * @return the radius of the clipping hemisphere
     */
    protected double getMaxC () {
        return GeometryUtils.HALF_PI;
    }
    
    //-------------------------------------------------------------------------
    // AbstractProjection implementation
    //-------------------------------------------------------------------------
    
    /** 
     * Forward projects a point.
     * @param lat the latitude of the point to project, in RADIANS
     * @param lat the longitude of the point to project, in RADIANS
     * @param storage a place to store the result
     * @return The projected point. Same object as <code>storage</code>
     */
    protected Coordinate forwardPointRaw (double lon, double lat, Coordinate storage) {
        double sinPhi = StrictMath.sin(lat);
        double cosPhi = StrictMath.cos(lat);
        double cosLonMinusLambda0 = StrictMath.cos(lon - _lambda0);
        double k = 2.0*_k0 / (1.0 + _sinPhi0*sinPhi + _cosPhi0*cosPhi*cosLonMinusLambda0);
        storage.x = _a * k * cosPhi * StrictMath.sin(lon - _lambda0);
        storage.y = _a * k * (_cosPhi0*sinPhi - _sinPhi0*cosPhi*cosLonMinusLambda0);
        return storage;
    }
    
    /** 
     * Inverse projects a point.
     * @param x the x coordinate of the point to be inverse projected
     * @param y the y coordinate of the point to be inverse projected
     * @param storage a place to store the result
     * @return The inverse of <code>pt</code>. Same object as <code>storage</code>.
     */
    protected Coordinate inversePointRaw (double x, double y, Coordinate storage) {
        double rho = StrictMath.sqrt(x*x + y*y);
        if (rho == 0.0) {
            storage.x = _lambda0;
            storage.y = _phi0;
        } else {
            double c = 2.0 * StrictMath.atan(rho / (2.0 * _a * _k0));
            double sinC = StrictMath.sin(c);
            double cosC = StrictMath.cos(c);
            if (_phi0 == GeometryUtils.HALF_PI) {
                storage.x = _lambda0 + StrictMath.atan2(x, -y);
            } else if (_phi0 == -GeometryUtils.HALF_PI) {
                storage.x = _lambda0 + StrictMath.atan2(x, y);
            } else {
                storage.x = _lambda0 + StrictMath.atan2(x * sinC, (rho*_cosPhi0*cosC - y*_sinPhi0*sinC));
            }
            storage.y = StrictMath.asin(cosC*_sinPhi0 + (y*sinC*_cosPhi0/rho));
        }
        return storage;
    }

    //-------------------------------------------------------------------------
    // Projection implementation
    //-------------------------------------------------------------------------
    
    /** */
    public ProjectionParameters getParameters () {
        ProjectionParameters result = super.getParameters();
        result.addAngularParameter(CENTER_LON_PROPERTY, _lambda0, SI.RADIAN);
        result.addAngularParameter(CENTER_LAT_PROPERTY, _phi0, SI.RADIAN);
        result.addDimensionlessParameter(SCALE_FACTOR_PROPERTY, _k0);
        return result;
    }
}
