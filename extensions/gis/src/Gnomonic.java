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
 * Gnomonic projection.
 * Formulas from Snyder, John P. (1987). "Map Projections -- A Working Manual".
 * US Geological Survey Professional Paper 1395, US Government Printing Office,
 * Washington, DC. pp. 164-168
 */
public final strictfp class Gnomonic extends Azimuthal {

    //--------------------------------------------------------------------------
    // Class variables
    //--------------------------------------------------------------------------
    
    /** */
    public static final String WKT_NAME = "Gnomonic";
    
    /** */
    public static final String CENTER_LON_PROPERTY = "central_meridian";
    
    /** */
    public static final String CENTER_LAT_PROPERTY = "latitude_of_origin";
    
    //-------------------------------------------------------------------------
    // Constructors
    //-------------------------------------------------------------------------
    
    /**
     * Construct a Gnomonic projection.
     * @param ellipsoid the ellipsoid for the projection
     * @param center the center of the projection
     * @param units the linear units of the projected data
     * @param falseEasting value to add to x coordinate of each projected point, in METERS
     * @param falseNorthing value to add to y coordinate of each projected point, in METERS
     */
    public Gnomonic (Ellipsoid ellipsoid, 
                     Coordinate center, 
                     Unit<Length> units,
                     double falseEasting,
                     double falseNorthing) {
        super(ellipsoid, center, units, falseEasting, falseNorthing);
        _name = WKT_NAME;
        computeParameters();
    }
    
    /** */
    public Gnomonic (Ellipsoid ellipsoid, ProjectionParameters parameters) 
            throws ParseException {
        super(ellipsoid, parameters);
        _name = WKT_NAME;
        computeParameters();
    }
    
    //-------------------------------------------------------------------------
    // HemisphericalProjection implementation
    //-------------------------------------------------------------------------
    
    /** 
     * Returns the maximum angular distance from the center of the clipping
     * hemisphere to which polylines & polygons are clipped.
     * The radius of our clipping hemisphere is pi/3 
     * @return the radius of our clipping hemisphere
     */
    protected double getMaxC () {
        //return(GeometryUtils.THIRD_PI);
        return 1.396; // 80 degrees, expressed in radians
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
        double kPrime = 1 / (_sinPhi0*sinPhi + _cosPhi0*cosPhi*StrictMath.cos(lon - _lambda0));
        storage.x = _a * kPrime * cosPhi * StrictMath.sin(lon - _lambda0);
        storage.y = _a * kPrime * (_cosPhi0*sinPhi - _sinPhi0*cosPhi*StrictMath.cos(lon - _lambda0));
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
            storage.y = _phi0;
            storage.x = _lambda0;
        } else {
            double c = StrictMath.atan(rho / _a);
            double sinC = StrictMath.sin(c);
            double cosC = StrictMath.cos(c);
            if (_phi0 == GeometryUtils.HALF_PI) {
                storage.x = _lambda0 + StrictMath.atan(x / -y);
            } else if (_phi0 == -GeometryUtils.HALF_PI) {
                storage.x = _lambda0 + StrictMath.atan(x / y);
            } else {
                storage.x = _lambda0 + StrictMath.atan(x * sinC / (rho*_cosPhi0*cosC - y*_sinPhi0*sinC));
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
        return result;
    }
}
