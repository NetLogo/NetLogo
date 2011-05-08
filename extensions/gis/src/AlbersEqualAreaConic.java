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
 * Albers Equal-Area Conic projection.
 * Formulas from Snyder, John P. (1987). "Map Projections -- A Working Manual".
 * US Geological Survey Professional Paper 1395, US Government Printing Office,
 * Washington, DC. pp. 98-103.
 *
 * NOTE: there is a printing error in Snyder's equations (14-12) and (14-12a)
 * that make it appear that the "/n" at the end is supposed to be part of the
 * exponent. It is not. I unfortunately had to figure this out the hard way.
 */
public final strictfp class AlbersEqualAreaConic extends Conic {

    //--------------------------------------------------------------------------
    // Class variables
    //--------------------------------------------------------------------------
    
    /** */
    public static final String WKT_NAME = "Albers_Conic_Equal_Area";
    
    /** */
    public static final String CENTER_LON_PROPERTY = "longitude_of_center";
    
    /** */
    public static final String CENTER_LAT_PROPERTY = "latitude_of_center";
    
    //--------------------------------------------------------------------------
    // Class methods
    //--------------------------------------------------------------------------
    
    /**
     * Compute q for the given latitude (see equation 3-12 on page 101).
     */
    private final double getQ (double phi) {
        double sinPhi = StrictMath.sin(phi);
        return((1-_e2)*((sinPhi/(1.0-_e2*sinPhi*sinPhi)) - ((1.0/(2.0*_e))*StrictMath.log((1.0-_e*sinPhi)/(1.0+_e*sinPhi)))));
    }
    
    //--------------------------------------------------------------------------
    // Instance variables
    //--------------------------------------------------------------------------
    
    /** Pre-compute these values for the given ellipsoid & projection center
        to save time when projecting. */
    private double _e, _n, _C, _rho0, _subBeta, _subPhi[];
    
    //--------------------------------------------------------------------------
    // Constructors
    //--------------------------------------------------------------------------
    
    /**
     * Construct an AlbersEqualAreaConic projection.
     * @param ellipsoid the ellipsoid for the projection
     * @param center the center of the projection
     * @param falseEasting value to add to x coordinate of each projected point, in METERS
     * @param falseNorthing value to add to y coordinate of each projected point, in METERS
     * @param phi1 latitude of the first standard parallel of the projection, in RADIANS
     * @param phi2 latitude of the second standard parallel of the projection, in RADIANS
     */
    public AlbersEqualAreaConic (Ellipsoid ellipsoid,
                                 Coordinate center,
                                 Unit<Length> units,
                                 double falseEasting,
                                 double falseNorthing,
                                 double phi1, 
                                 double phi2) {
        super(ellipsoid, center, units, falseEasting, falseNorthing, phi1, phi2);
        _name = WKT_NAME;
        _subPhi = new double[3];
        computeParameters();
    }
    
    /** */
    public AlbersEqualAreaConic (Ellipsoid ellipsoid, ProjectionParameters parameters) throws ParseException {
        super(ellipsoid, parameters);
        _name = WKT_NAME;
        _subPhi = new double[3];
        computeParameters();
    }
    
    //--------------------------------------------------------------------------
    // AbstractProjection implementation
    //--------------------------------------------------------------------------
    
    /** 
     * Forward projects a point
     * @param lat the latitude of the point to project, in RADIANS
     * @param lat the longitude of the point to project, in RADIANS
     * @param storage a place to store the result
     * @return The projected point. Same object as <code>storage</code>
     */
    protected Coordinate forwardPointRaw (double lon, double lat, Coordinate storage) {
        double q = getQ(lat);
        double theta = _n * GeometryUtils.wrap_longitude(lon - _lambda0);
        double rho = _a * StrictMath.sqrt(_C - (_n*q)) / _n;
        storage.x = rho * StrictMath.sin(theta);
        storage.y = _rho0 - rho*StrictMath.cos(theta);
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
        double rho0minusY = _rho0 - y;
        double rho = StrictMath.sqrt(x*x + rho0minusY*rho0minusY);
        double theta = (_n < 0.0) ?  StrictMath.atan2(-x, -rho0minusY) : StrictMath.atan2(x, rho0minusY);
        double q = (_C - ((rho*rho)*(_n*_n))/(_a*_a)) / _n;
        double beta = StrictMath.asin(q / _subBeta);
        storage.y = beta + 
                     _subPhi[0] * StrictMath.sin(2.0 * beta) +
                     _subPhi[1] * StrictMath.sin(4.0 * beta) +
                     _subPhi[2] * StrictMath.sin(6.0 * beta);
        double thetaOverN = theta/_n;
        if (StrictMath.abs(thetaOverN) > StrictMath.PI) {
            storage.x = Double.NaN;
        } else {
            storage.x = GeometryUtils.wrap_longitude(_lambda0 + thetaOverN);
        }
        return storage;
    }
    
    /** 
     * Initialize parameters, and recompute them whenever the ellipsoid or 
     * projection center changes.
     */
    protected void computeParameters () {
        _e = StrictMath.sqrt(_e2);
        double m1 = StrictMath.cos(_phi1) / StrictMath.sqrt(1.0 - _e2*StrictMath.sin(_phi1)*StrictMath.sin(_phi1));
        double m2 = StrictMath.cos(_phi2) / StrictMath.sqrt(1.0 - _e2*StrictMath.sin(_phi2)*StrictMath.sin(_phi2));
        double q0 = getQ(_phi0);
        double q1 = getQ(_phi1);
        double q2 = getQ(_phi2);
        _n = (m1*m1 - m2*m2) / (q2 - q1);
        _C = m1*m1 + _n*q1;
        _rho0 = _a * StrictMath.sqrt(_C - (_n*q0)) / _n;
        _subBeta = 1.0 - (((1.0-_e2)/(2.0*_e))*StrictMath.log((1.0 - _e) / (1.0 + _e)));
        _subPhi[0] = (_e2/3.0) + ((31.0*_e2*_e2)/180.0) + ((517.0*_e2*_e2*_e2)/5040.0);
        _subPhi[1] = ((23.0*_e2*_e2)/360.0) + ((251.0*_e2*_e2*_e2)/3780.0);
        _subPhi[2] = (761.0*_e2*_e2*_e2)/45360.0;
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
        AlbersEqualAreaConic clone = (AlbersEqualAreaConic)super.clone();
        clone._subPhi = _subPhi.clone();
        return clone;
    }
}
