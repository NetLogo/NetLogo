//
// Copyright (c) 2004 the National Geographic Society. All rights reserved.
//

package org.myworldgis.projection;

import com.vividsolutions.jts.geom.Coordinate;
import java.text.ParseException;
import javax.measure.quantity.Length;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;
import org.myworldgis.util.GeometryUtils;


/**
 * Azimuthal Equidistant projection.
 * Formulas from Snyder, John P. (1987). "Map Projections -- A Working Manual".
 * US Geological Survey Professional Paper 1395, US Government Printing Office,
 * Washington, DC. pp. 195-196
 */
public final strictfp class AzimuthalEquidistant extends Azimuthal {

    //--------------------------------------------------------------------------
    // Class variables
    //--------------------------------------------------------------------------
    
    /** */
    public static final String WKT_NAME = "Azimuthal_Equidistant";
    
    /** */
    public static final String CENTER_LON_PROPERTY = "longitude_of_center";
    
    /** */
    public static final String CENTER_LAT_PROPERTY = "latitude_of_center";
    
    //-------------------------------------------------------------------------
    // Constructors
    //-------------------------------------------------------------------------
    
    /**
     * Construct an AzimuthalEqualArea projection.
     * @param ellipsoid the ellipsoid for the projection
     * @param center the center of the projection
     * @param units the linear units of the projected data
     * @param falseEasting value to add to x coordinate of each projected point, in METERS
     * @param falseNorthing value to add to y coordinate of each projected point, in METERS
     */
    public AzimuthalEquidistant (Ellipsoid ellipsoid, 
                                   Coordinate center, 
                                   Unit<Length> units,
                                   double falseEasting,
                                   double falseNorthing) {
        super(ellipsoid, center, units, falseEasting, falseNorthing);
        _name = WKT_NAME;
        computeParameters();
    }
    
    /** */
    public AzimuthalEquidistant (Ellipsoid ellipsoid, ProjectionParameters parameters) 
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
     * The radius of our clipping hemisphere is 3pi/4 
     * @return the radius of our clipping hemisphere
     */
    protected double getMaxC () {
        return (StrictMath.PI - GeometryUtils.QUARTER_PI);
    }
    
    //-------------------------------------------------------------------------
    // AbstractProjection implementation
    //-------------------------------------------------------------------------
    
    /** 
     * Forward projects a point.
     * @param lambda the longitude of the point to project, in RADIANS
     * @param phi the latitude of the point to project, in RADIANS
     * @param storage a place to store the result
     * @return The projected point. Same object as <code>storage</code>
     */
    protected Coordinate forwardPointRaw (double lambda, double phi, Coordinate storage) {
        double cosC = _sinPhi0*StrictMath.sin(phi) + _cosPhi0*StrictMath.cos(phi)*StrictMath.cos(GeometryUtils.wrap_longitude(lambda - _lambda0));
        if (cosC == 1.0) {
            storage.x = 0.0;
            storage.y = 0.0;
        } else {
            double c = StrictMath.acos(cosC);
            double kPrime = c / StrictMath.sin(c);
            storage.x = _a * kPrime * StrictMath.cos(phi) * StrictMath.sin(GeometryUtils.wrap_longitude(lambda - _lambda0));
            storage.y = _a * kPrime * (_cosPhi0*StrictMath.sin(phi) - _sinPhi0*StrictMath.cos(phi)*StrictMath.cos(GeometryUtils.wrap_longitude(lambda - _lambda0)));
        }
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
            double c = rho / _a;
            storage.y = StrictMath.asin(StrictMath.cos(c)*_sinPhi0 + (y*StrictMath.sin(c)*_cosPhi0/rho));
            storage.x = _lambda0 + StrictMath.atan2(x * StrictMath.sin(c), ((rho * _cosPhi0 * StrictMath.cos(c)) - (y * _sinPhi0 * StrictMath.sin(c))));
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
