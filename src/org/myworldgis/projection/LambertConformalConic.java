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
 * Lambert Conformal Conic projection.
 * Formulas from Snyder, John P. (1987). "Map Projections -- A Working Manual".
 * US Geological Survey Professional Paper 1395, US Government Printing Office,
 * Washington, DC. pp. 104-110
 */
public final class LambertConformalConic extends Conic {

    //--------------------------------------------------------------------------
    // Class variables
    //--------------------------------------------------------------------------

    /** */
    public static final String WKT_NAME = "Lambert_Conformal_Conic_2SP";

    /** */
    public static final String CENTER_LON_PROPERTY = "central_meridian";

    /** */
    public static final String CENTER_LAT_PROPERTY = "latitude_of_origin";

    //-------------------------------------------------------------------------
    // Instance variables
    //-------------------------------------------------------------------------

    /** Pre-compute these values for the given ellipsoid & projection center
        to save time when projecting. */
    private double _e, _n, _inverseN, _F, _rho0, _subPhi[];

    //-------------------------------------------------------------------------
    // Constructors
    //-------------------------------------------------------------------------

    /**
     * Construct a LambertConformalConic projection.
     * @param ellipsoid the ellipsoid for the projection
     * @param center the center of the projection
     * @param falseEasting value to add to x coordinate of each projected point, in projected units
     * @param falseNorthing value to add to y coordinate of each projected point, in projected units
     * @param phi1 latitude of the first standard parallel of the projection, in RADIANS
     * @param phi2 latitude of the second standard parallel of the projection, in RADIANS
     */
    public LambertConformalConic (Ellipsoid ellipsoid,
                                  Coordinate center,
                                  Unit<Length> units,
                                  double falseEasting,
                                  double falseNorthing,
                                  double phi1,
                                  double phi2) {
        super(ellipsoid, center, units, falseEasting, falseNorthing, phi1, phi2);
        _name = WKT_NAME;
        _subPhi = new double[4];
        computeParameters();
    }

    /** */
    public LambertConformalConic (Ellipsoid ellipsoid, ProjectionParameters parameters)
            throws ParseException {
        super(ellipsoid, parameters);
        _name = WKT_NAME;
        _subPhi = new double[4];
        computeParameters();
    }

    //-------------------------------------------------------------------------
    // Instance methods
    //-------------------------------------------------------------------------

    /**
     * Compute t of the given latitude (See equation 15-9a on p. 108)
     */
    private double getT (double phi) {
        double sinPhi = StrictMath.sin(phi);
        return(StrictMath.sqrt(((1.0 - sinPhi) / (1.0 + sinPhi)) * StrictMath.pow((1.0 + _e * sinPhi) / (1.0 - _e * sinPhi), _e)));
    }

    //-------------------------------------------------------------------------
    // AbstractProjection implementation
    //-------------------------------------------------------------------------

    /**
     * Forward projects a point.
     * @param lon the longitude of the point to project, in RADIANS
     * @param lat the latitude of the point to project, in RADIANS
     * @param storage a place to store the result
     * @return The projected point. Same object as <code>storage</code>
     */
    protected Coordinate forwardPointRaw (double lon, double lat, Coordinate storage) {
        double rho = _a * _F * StrictMath.pow(getT(lat), _n);
        double theta = _n * GeometryUtils.wrap_longitude(lon - _lambda0);
        storage.x = rho * StrictMath.sin(theta);
        storage.y = _rho0 - (rho * StrictMath.cos(theta));
        return storage;
    }

    /**
     * Inverse projects a point.
     * @param x float the x coordinate of the point to be inverse projected
     * @param y float the y coordinate of the point to be inverse projected
     * @param storage a place to store the result
     * @return The inverse of <code>pt</code>. Same object as <code>storage</code>.
     */
    protected Coordinate inversePointRaw (double x, double y, Coordinate storage) {
        double rho0minusY = _rho0 - y;
        double rho = StrictMath.sqrt(x*x + rho0minusY*rho0minusY) * GeometryUtils.sign(_n);
        double theta = (_n < 0.0) ?  StrictMath.atan2(-x, -rho0minusY) : StrictMath.atan2(x, rho0minusY);
        if (rho == 0.0) {
            storage.y = GeometryUtils.HALF_PI * GeometryUtils.sign(_n);
        } else {
            double t = StrictMath.pow(rho / (_a * _F), _inverseN);
            double chi = GeometryUtils.HALF_PI - 2.0 * StrictMath.atan(t);
            storage.y = chi +
                        _subPhi[0] * StrictMath.sin(2.0 * chi) +
                        _subPhi[1] * StrictMath.sin(4.0 * chi) +
                        _subPhi[2] * StrictMath.sin(6.0 * chi) +
                        _subPhi[3] * StrictMath.sin(8.0 * chi);
        }
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
        double t0 = getT(_phi0);
        double t1 = getT(_phi1);
        double sinPhi1 = StrictMath.sin(_phi1);
        double m1 = StrictMath.cos(_phi1) / StrictMath.sqrt(1 - _e2 * sinPhi1*sinPhi1);
        if (_phi1 == _phi2) {
            _n = sinPhi1;
        } else {
            double t2 = getT(_phi2);
            double sinPhi2 = StrictMath.sin(_phi2);
            double m2 = StrictMath.cos(_phi2) / StrictMath.sqrt(1.0 - _e2 * sinPhi2*sinPhi2);
            _n = (StrictMath.log(m1) - StrictMath.log(m2)) / (StrictMath.log(t1) - StrictMath.log(t2));
        }
        _inverseN = 1.0 / _n;
        _F = m1 / (_n * StrictMath.pow(t1, _n));
        _rho0 = _a * _F * StrictMath.pow(t0, _n);
        _subPhi[0] = (_e2/2.0) + (5.0*_e2*_e2/24.0) + (_e2*_e2*_e2/12.0) + (13.0*_e2*_e2*_e2*_e2/360.0);
        _subPhi[1] = (7.0*_e2*_e2/48.0) + (29.0*_e2*_e2*_e2/240.0) + (811.0*_e2*_e2*_e2*_e2/11520.0);
        _subPhi[2] = (7.0*_e2*_e2*_e2/120.0) + (81.0*_e2*_e2*_e2*_e2/1120.0);
        _subPhi[3] = (4279.0*_e2*_e2*_e2*_e2/161280.0);
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
        LambertConformalConic clone = (LambertConformalConic)super.clone();
        clone._subPhi = _subPhi.clone();
        return clone;
    }
}
