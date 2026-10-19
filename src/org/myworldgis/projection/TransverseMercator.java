//
// Copyright (c) 2000 the National Geographic Society. All rights reserved.
//

package org.myworldgis.projection;

import org.locationtech.jts.geom.Coordinate;
import java.text.ParseException;
import org.myworldgis.util.GeometryUtils;
import org.ngs.ngunits.SI;
import org.ngs.ngunits.Unit;
import org.ngs.ngunits.quantity.Length;


/**
 * Transverse Mercator projection.
 * Formulas from Snyder, John P. (1987). "Map Projections -- A Working Manual".
 * US Geological Survey Professional Paper 1395, US Government Printing Office,
 * Washington, DC. pp. 48-65
 */
public final class TransverseMercator extends HemisphericalProjection {

    //--------------------------------------------------------------------------
    // Class variables
    //--------------------------------------------------------------------------

    /** */
    public static final String WKT_NAME = "Transverse_Mercator";

    /** */
    public static final String CENTER_LON_PROPERTY = "central_meridian";

    /** */
    public static final String CENTER_LAT_PROPERTY = "latitude_of_origin";

    /** */
    public static final String SCALE_FACTOR_PROPERTY = "scale_factor";

    //--------------------------------------------------------------------------
    // Instance variables
    //--------------------------------------------------------------------------

    /** Scale factor along the central meridian */
    private double _k0;

    /** Pre-compute these values for the given ellipsoid & projection center
        to save time when projecting. */
    private double _ePrimeSq, _subMU, _M0, _subM[], _subPhi1[];

    /** */
    private Coordinate _hemisphereCenter;

    //--------------------------------------------------------------------------
    // Constructors
    //--------------------------------------------------------------------------

    /**
     * Construct a TransverseMercator projection.
     * @param ellipsoid the ellipsoid for the projection
     * @param center the center of the projection
     * @param falseEasting value to add to x coordinate of each projected point, in projected units
     * @param falseNorthing value to add to y coordinate of each projected point, in projected units
     * @param k0 the scale factor along the central meridian, usually 1.0
     */
    public TransverseMercator (Ellipsoid ellipsoid,
                               Coordinate center,
                               Unit<Length> units,
                               double falseEasting,
                               double falseNorthing,
                               double k0) {
        super(ellipsoid, center, units, falseEasting, falseNorthing);
        _name = WKT_NAME;
        _k0 = k0;
        _subM = new double[4];
        _subPhi1 = new double[4];
        computeParameters();
    }

    /** */
    public TransverseMercator (Ellipsoid ellipsoid, ProjectionParameters parameters)
            throws ParseException {
        super(ellipsoid, parameters);
        _name = WKT_NAME;
        _k0 = parameters.getDimensionlessParameter(SCALE_FACTOR_PROPERTY);
        _subM = new double[4];
        _subPhi1 = new double[4];
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
            TransverseMercator proj = (TransverseMercator)obj;
            return (StrictMath.abs(proj._k0 - this._k0) < GeometryUtils.EPSILON);
        } else {
            return(false);
        }
    }

    public int hashCode() {
        return (int) ((super.hashCode() * Math.round(this._k0 / GeometryUtils.EPSILON)) % Integer.MAX_VALUE);
    }

    //-------------------------------------------------------------------------
    // HemisphericalProjection implementation
    //-------------------------------------------------------------------------

    /**
     * Returns the maximum angular distance from the center of the clipping
     * hemisphere to which polylines & polygons are clipped.
     * Our clipping hemisphere has a radius of 81 degrees.
     * @return the radius of the clipping hemisphere
     */
    protected double getMaxC () {
        return 1.4137167; // 81 degrees expressed in radians
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

    //--------------------------------------------------------------------------
    // AbstractProjection implementation
    //--------------------------------------------------------------------------

    /**
     * Forward projects a point.
     * @param lat the latitude of the point to project, in RADIANS
     * @param lat the longitude of the point to project, in RADIANS
     * @param storage a place to store the result
     * @return The projected point. Same object as <code>storage</code>
     */
    protected Coordinate forwardPointRaw (double lambda, double phi, Coordinate storage) {
        if (StrictMath.abs(StrictMath.abs(phi) - GeometryUtils.HALF_PI) < GeometryUtils.EPSILON) {
            double M = _a * (_subM[0]*phi - _subM[1]*StrictMath.sin(2.0 * phi) + _subM[2]*StrictMath.sin(4.0 * phi) - _subM[3]*StrictMath.sin(6.0 * phi));
            storage.x = 0.0;
            storage.y = _k0 * (M - _M0);
        } else {
            double sinPhi = StrictMath.sin(phi);
            double cosPhi = StrictMath.cos(phi);
            double tanPhi = StrictMath.tan(phi);
            double N = _a / StrictMath.sqrt(1 - (_e2 * (sinPhi * sinPhi)));
            double T = tanPhi * tanPhi;
            double C = _ePrimeSq * (cosPhi * cosPhi);
            double A = (lambda - _lambda0) * cosPhi;
            double M = _a * (_subM[0]*phi - _subM[1]*StrictMath.sin(2.0 * phi) + _subM[2]*StrictMath.sin(4.0 * phi) - _subM[3]*StrictMath.sin(6.0 * phi));
            storage.x = _k0 * N * (A + (1.0 - T + C)*A*A*A/6.0 + (5.0 - 18.0*T + T*T + 72.0*C - 58.0*_ePrimeSq)*A*A*A*A*A/120.0);
            storage.y = _k0 * (M - _M0 + N*tanPhi*(A*A/2.0f + (5 - T + 9.0*C + 4.0*C*C)*A*A*A*A/24.0 + (61.0 - 58.0*T + T*T + 600.0*C - 330.0*_ePrimeSq)*A*A*A*A*A*A/720.0));
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
        double M = _M0 + (y / _k0);
        double mu = M / _subMU;
        double phi1 = mu + _subPhi1[0]*StrictMath.sin(2.0 * mu) + _subPhi1[1]*StrictMath.sin(4.0 * mu) + _subPhi1[2]*StrictMath.sin(6.0 * mu) + _subPhi1[3]*StrictMath.sin(8.0 * mu);
        double sinPhi1 = StrictMath.sin(phi1);
        double cosPhi1 = StrictMath.cos(phi1);
        double tanPhi1 = StrictMath.tan(phi1);
        double C1 = _ePrimeSq *cosPhi1*cosPhi1;
        double T1 = tanPhi1*tanPhi1;
        double N1 = _a / StrictMath.sqrt(1 - _e2*sinPhi1*sinPhi1);
        double R1 = _a*(1.0 - _e2) / StrictMath.pow(1.0 - _e2*sinPhi1*sinPhi1, 1.5);
        double D = x / (N1*_k0);
        storage.y = phi1 - (N1*tanPhi1/R1)*(D*D/2.0 - (5.0 + 3.0*T1 + 10.0*C1 - 4.0*C1*C1 - 9.0*_ePrimeSq)*D*D*D*D/24.0 + (61.0 + 90.0*T1 + 298.0*C1 + 45.0*T1*T1 - 252.0*_ePrimeSq - 3.0*C1*C1)*D*D*D*D*D*D/720.0);
        storage.x = _lambda0 + (D - (1.0 + 2.0*T1 + C1)*D*D*D/6.0 + (5.0 - 2.0*C1 + 28.0*T1 - 3.0*C1*C1 + 8.0*_ePrimeSq + 24.0*T1*T1)*D*D*D*D*D/120.0)/cosPhi1;
        return storage;
    }

    /**
     * Initialize parameters, and recompute them whenever the ellipsoid or
     * projection center changes.
     */
    protected void computeParameters () {
        _ePrimeSq = _e2 / (1.0 - _e2);
        _subM[0] = 1.0 - _e2/4.0 - 3.0*_e2*_e2/64.0 - 5.0*_e2*_e2*_e2/256.0;
        _subM[1] = 3.0*_e2/8.0 + 3.0*_e2*_e2/32.0 + 45.0*_e2*_e2*_e2/1024.0;
        _subM[2] = 15.0*_e2*_e2/256.0 + 45.0*_e2*_e2*_e2/1024.0;
        _subM[3] = 35.0*_e2*_e2*_e2/3072.0;
        _M0 = _a * (_subM[0]*_phi0 - _subM[1]*StrictMath.sin(2.0 * _phi0) + _subM[2]*StrictMath.sin(4.0 * _phi0) - _subM[3]*StrictMath.sin(6.0 * _phi0));
        _subMU = _a * (1.0 - _e2/4.0 - 3.0*_e2*_e2/64.0 - 5.0*_e2*_e2*_e2/256.0);
        double e1 = (1.0 - StrictMath.sqrt(1.0 - _e2)) / (1.0 + StrictMath.sqrt(1.0 - _e2));
        _subPhi1[0] = 3.0*e1/2.0 - 27.0*e1*e1*e1/32.0;
        _subPhi1[1] = 21.0*e1*e1/16.0 - 55.0*e1*e1*e1*e1/32.0;
        _subPhi1[2] = 151.0*e1*e1*e1/96.0;
        _subPhi1[3] = 1097.0*e1*e1*e1*e1/512.0;
        _hemisphereCenter = new Coordinate(_lambda0, 0.0f);
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
        result.addDimensionlessParameter(SCALE_FACTOR_PROPERTY, _k0);
        return result;
    }

    //--------------------------------------------------------------------------
    // Cloneable implementation
    //--------------------------------------------------------------------------

    /** */
    public Object clone () {
        TransverseMercator clone = (TransverseMercator)super.clone();
        clone._subM = _subM.clone();
        clone._subPhi1 = _subPhi1.clone();
        return clone;
    }
}
