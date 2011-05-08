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
 * Polyconic projection.
 * Formulas from Snyder, John P. (1987). "Map Projections -- A Working Manual".
 * US Geological Survey Professional Paper 1395, US Government Printing Office,
 * Washington, DC. pp. 129-130
 */
public final strictfp class Polyconic extends HemisphericalProjection {
    
    //--------------------------------------------------------------------------
    // Class variables
    //--------------------------------------------------------------------------
    
    /** */
    public static final String WKT_NAME = "Polyconic";
    
    /** */
    public static final String CENTER_LON_PROPERTY = "central_meridian";
    
    /** */
    public static final String CENTER_LAT_PROPERTY = "latitude_of_origin";
    
    /** */
    private static final double EPSILON = 0.000001;
    
    /** */
    private static final int MAX_ITERATIONS = 75;
    
    //-------------------------------------------------------------------------
    // Instance variables
    //-------------------------------------------------------------------------
    
    /** Pre-compute these values for the given ellipsoid & projection center
        to save time when projecting. */
    private double _subM[], _M0;
    
    /** */
    protected Coordinate _hemisphereCenter;
    
    //-------------------------------------------------------------------------
    // Constructors
    //-------------------------------------------------------------------------
    
    /**
     * Construct an AzimuthalEqualArea projection.
     * @param ellipsoid the ellipsoid for the projection
     * @param center the center of the projection
     * @param units the linear units of the projected data
     * @param falseEasting value to add to x coordinate of each projected point, in projected units
     * @param falseNorthing value to add to y coordinate of each projected point, in projected units
     */
    public Polyconic (Ellipsoid ellipsoid, 
                      Coordinate center, 
                      Unit<Length> units,
                      double falseEasting,
                      double falseNorthing) {
        super(ellipsoid, center, units, falseEasting, falseNorthing);
        _name = WKT_NAME;
        _subM = new double[4];
        computeParameters();
    }
    
    /** */
    public Polyconic (Ellipsoid ellipsoid, ProjectionParameters parameters) 
            throws ParseException {
        super(ellipsoid, parameters);
        _name = WKT_NAME;
        _subM = new double[4];
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
    
    /**
     * Returns the center of the clipping hemisphere.
     * The center of the clipping hemisphere for an azimuthal projection is the
     * same as the center of the projection.
     * @return the center of the clipping hemisphere
     */
    protected Coordinate getHemisphereCenter () {
        return _hemisphereCenter;
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
    protected Coordinate forwardPointRaw (double lambda, double phi, Coordinate storage) {
        if (phi == 0.0) {
            storage.x = _a * GeometryUtils.wrap_longitude(lambda - _lambda0);
            storage.y = - _M0;
        } else {
            double sinPhi = StrictMath.sin(phi);
            double cotPhi = 1.0 / StrictMath.tan(phi);
            double E = GeometryUtils.wrap_longitude(lambda - _lambda0) * sinPhi;
            double N = _a / StrictMath.sqrt(1.0 - _e2*sinPhi*sinPhi);
            double M = _a * ((_subM[0] * phi) - (_subM[1] * StrictMath.sin(2.0 * phi)) + (_subM[2] * StrictMath.sin(4.0 * phi)) - (_subM[3] * StrictMath.sin(6.0 * phi)));
            storage.x = N * cotPhi * StrictMath.sin(E);
            storage.y = M - _M0 + N * cotPhi * (1.0 - StrictMath.cos(E));
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
        double A = (_M0 + y) / _a;
        double B = ((x*x)/(_a*_a)) + A*A;
        double phiN, phiN1 = A, C;
        int i = 0;
        do {
            phiN = phiN1;
            C = StrictMath.sqrt(1.0 - _e2*StrictMath.sin(phiN)*StrictMath.sin(phiN))*StrictMath.tan(phiN);
            double Ma = (_subM[0] * phiN) - (_subM[1] * StrictMath.sin(2.0 * phiN)) + (_subM[2] * StrictMath.sin(4.0 * phiN)) - (_subM[3] * StrictMath.sin(6.0 * phiN));
            double MnPrime = _subM[0] - (2.0 * _subM[1] * StrictMath.cos(2.0 * phiN)) + (4.0 * _subM[2] * StrictMath.cos(4.0 * phiN)) - (6.0 * _subM[3] * StrictMath.cos(6.0 * phiN));
            phiN1 = phiN - (A*(C*Ma + 1.0) - Ma - 0.5*(Ma*Ma + B)*C) / (_e2*StrictMath.sin(2.0 * phiN)*(Ma*Ma + B - 2.0*A*Ma)/(4.0*C) + (A-Ma)*(C*MnPrime - 2.0/StrictMath.sin(2.0*phiN)) - MnPrime);
        } while ((StrictMath.abs(phiN - phiN1) > EPSILON) && (i++ < MAX_ITERATIONS));
        storage.y = ((i > MAX_ITERATIONS) ? Double.NaN : phiN1);
        storage.x = _lambda0 + StrictMath.asin((x*C)/_a)/StrictMath.sin(storage.y);
        return storage;
    }
    
    /** 
     * Initialize parameters, and recompute them whenever the ellipsoid or 
     * projection center changes.
     */
    protected void computeParameters () {
        _subM[0] = 1.0 - (_e2 / 4.0) - ((3.0 * _e2 *_e2) / 64.0) - ((5.0 * _e2 * _e2 * _e2) / 256.0);
        _subM[1] = ((3.0 * _e2) / 8.0) + ((3.0 * _e2 * _e2) / 32.0) + ((45.0 * _e2 * _e2 * _e2) / 1024.0);
        _subM[2] = ((15.0 * _e2 * _e2) / 256.0) + ((45.0 * _e2 * _e2 * _e2) / 1024.0);
        _subM[3] = ((35.0 * _e2 * _e2 * _e2) / 3072.0);
        _M0 = _a * ((_subM[0] * _phi0) - (_subM[1] * StrictMath.sin(2.0 * _phi0)) + (_subM[2] * StrictMath.sin(4.0 * _phi0)) - (_subM[3] * StrictMath.sin(6.0 * _phi0)));
        _hemisphereCenter = new Coordinate(0.0, _lambda0);
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
        Polyconic clone = (Polyconic)super.clone();
        clone._subM = _subM.clone();
        return clone;
    }
}
