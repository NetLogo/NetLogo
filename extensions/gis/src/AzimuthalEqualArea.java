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
 * Lambert Azimuthal Equal-Area projection.
 * Formulas from Snyder, John P. (1987). "Map Projections -- A Working Manual".
 * US Geological Survey Professional Paper 1395, US Government Printing Office,
 * Washington, DC. pp. 187-189
 */
public final strictfp class AzimuthalEqualArea extends Azimuthal {

    //--------------------------------------------------------------------------
    // Class variables
    //--------------------------------------------------------------------------
    
    /** */
    public static final String WKT_NAME = "Lambert_Azimuthal_Equal_Area";
    
    /** */
    public static final String CENTER_LON_PROPERTY = "longitude_of_center";
    
    /** */
    public static final String CENTER_LAT_PROPERTY = "latitude_of_center";
    
    //-------------------------------------------------------------------------
    // Instance variables
    //-------------------------------------------------------------------------
    
    /** Pre-compute these values for the given ellipsoid & projection center
        to save time when projecting. */
    private double _e, _qp, _Rq, _D, _sinBeta1, _cosBeta1, _subPhi[];
    
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
    public AzimuthalEqualArea (Ellipsoid ellipsoid, 
                               Coordinate center, 
                               Unit<Length> units,
                               double falseEasting,
                               double falseNorthing) {
        super(ellipsoid, center, units, falseEasting, falseNorthing);
        _name = WKT_NAME;
        _subPhi = new double[3];
        computeParameters();
    }
    
    /** */
    public AzimuthalEqualArea (Ellipsoid ellipsoid, ProjectionParameters parameters) 
            throws ParseException {
        super(ellipsoid, parameters);
        _name = WKT_NAME;
        _subPhi = new double[3];
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
        return GeometryUtils.HALF_PI;
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
        double sinPhi = StrictMath.sin(phi);
        double q = (1.0 - _e2) * ((sinPhi / (1.0 - (_e2*sinPhi*sinPhi))) - ((1 / (2.0 * _e)) * StrictMath.log((1.0 - _e*sinPhi) / (1.0 + (_e * sinPhi)))));
        double beta = StrictMath.asin(q / _qp);
        double B = _Rq * StrictMath.sqrt(2.0 / (1.0 + (_sinBeta1 * StrictMath.sin(beta)) + (_cosBeta1 * StrictMath.cos(beta) * StrictMath.cos(GeometryUtils.wrap_longitude(lambda - _lambda0)))));
        storage.x = B * _D * StrictMath.cos(beta) * StrictMath.sin(GeometryUtils.wrap_longitude(lambda - _lambda0));
        storage.y = (B / _D) * ((_cosBeta1 * StrictMath.sin(beta)) - (_sinBeta1 * StrictMath.cos(beta) *  StrictMath.cos(GeometryUtils.wrap_longitude(lambda - _lambda0))));
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
        double rho = StrictMath.sqrt((x/_D)*(x/_D) + (_D*y*_D*y));
        if (rho == 0.0) {
            storage.y = _phi0;
            storage.x = _lambda0;
        } else {
            double ce = 2.0 * StrictMath.asin(rho / (2.0 * _Rq));
            double beta = StrictMath.asin(StrictMath.cos(ce)*_sinBeta1 + (_D*y*StrictMath.sin(ce)*_cosBeta1/rho));
            storage.y = beta + (_subPhi[0] * StrictMath.sin(2.0 * beta)) + (_subPhi[1] * StrictMath.sin(4.0 * beta)) + (_subPhi[2] * StrictMath.sin(6.0 * beta));
            storage.x = _lambda0 + StrictMath.atan2(x*StrictMath.sin(ce), _D*rho*_cosBeta1*StrictMath.cos(ce) - _D*_D*y*_sinBeta1*StrictMath.sin(ce));
        }
        return storage;
    }
    
    /** 
     * Initialize parameters, and recompute them whenever the ellipsoid or 
     * projection center changes.
     */
    protected void computeParameters () {
        _e = StrictMath.sqrt(_e2);
        _qp = (1.0 - _e2) * ((1.0 / (1.0 - _e2)) - ((1 / (2.0 * _e)) * StrictMath.log((1.0 - _e) / (1.0 + _e))));
        _Rq = _a * StrictMath.sqrt(_qp / 2.0);
        double sinPhi1 = StrictMath.sin(_phi0);
        double cosPhi1 = StrictMath.cos(_phi0);
        double q1 = (1.0 - _e2) * ((sinPhi1 / (1.0 - (_e2*sinPhi1*sinPhi1))) - ((1 / (2.0 * _e)) * StrictMath.log((1.0 - _e*sinPhi1) / (1.0 + (_e * sinPhi1)))));
        double beta1 = StrictMath.asin(q1 / _qp);
        _sinBeta1 = StrictMath.sin(beta1);
        _cosBeta1 = StrictMath.cos(beta1);
        double m1 = cosPhi1 / StrictMath.sqrt(1.0 - (_e2*sinPhi1*sinPhi1));
        _D = _a * m1 / (_Rq * _cosBeta1);
        _subPhi[0] = (_e2 / 3.0) + ((31.0 * _e2 * _e2) / 180.0) + ((517.0 * _e2 *_e2 * _e2) / 5040.0);
        _subPhi[1] = ((23.0 * _e2 * _e2) / 360.0) + ((251 * _e2 * _e2 * _e2) / 3780.0);
        _subPhi[2] = ((761 * _e2 * _e2 * _e2) / 45360.0);
        super.computeParameters();
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

    //--------------------------------------------------------------------------
    // Cloneable implementation
    //--------------------------------------------------------------------------
    
    /** */
    public Object clone () {
        AzimuthalEqualArea clone = (AzimuthalEqualArea)super.clone();
        clone._subPhi = _subPhi.clone();
        return clone;
    }
}
