//
// Copyright (c) 2004 the National Geographic Society. All rights reserved.
//

package org.myworldgis.projection;

import org.locationtech.jts.geom.Coordinate;
import java.text.ParseException;
import org.myworldgis.util.GeometryUtils;
import org.ngs.ngunits.SI;
import org.ngs.ngunits.Unit;
import org.ngs.ngunits.quantity.Length;


/**
 * Hotine Oblique Mercator projection.
 * Formulas from Snyder, John P. (1987). "Map Projections -- A Working Manual".
 * US Geological Survey Professional Paper 1395, US Government Printing Office,
 * Washington, DC. pp. 72-75
 */
public final class ObliqueMercator extends HemisphericalProjection {

    //--------------------------------------------------------------------------
    // Class variables
    //--------------------------------------------------------------------------

    /** */
    public static final String WKT_NAME = "Oblique_Mercator";

    /** */
    public static final String ALTERNATE_WKT_NAME = "hotine_oblique_mercator";

    /** */
    public static final String CENTER_LON_PROPERTY = "longitude_of_center";

    /** */
    public static final String CENTER_LAT_PROPERTY = "latitude_of_center";

    /** */
    public static final String SCALE_FACTOR_PROPERTY = "scale_factor";

    /** */
    public static final String AZIMUTH_PROPERTY = "azimuth";

    //--------------------------------------------------------------------------
    // Instance variables
    //--------------------------------------------------------------------------

    /** Scale factor along the central meridian */
    private double _k0;

    /** Azimuth of the central meridian as it passes through projection center */
    private double _alpha;

    /** Ellipsoid eccentricity */
    private double _e;

    /** Rectified center longitude */
    private double _lambdaZ;

    /** Sine of azimuth */
    private double _sinAlpha;

    /** Cosine of azimuth */
    private double _cosAlpha;

    /** Sine of rectified azimuth */
    private double _sinGamma0;

    /** Cosine of rectified azimuth */
    private double _cosGamma0;

    /** Other cached intermediate values */
    private double _B, _A, _E, _subPhi[];

    /** */
    private Coordinate _hemisphereCenter;

    //--------------------------------------------------------------------------
    // Constructors
    //--------------------------------------------------------------------------

    /**
     * Construct a ObliqueMercator projection.
     * @param ellipsoid the ellipsoid for the projection
     * @param center the center of the projection
     * @param falseEasting value to add to x coordinate of each projected point, in METERS
     * @param falseNorthing value to add to y coordinate of each projected point, in METERS
     * @param k0 the scale factor along the central meridian, usually 1.0
     * @param centralLineAzimuth Azimuth of the central meridian as it passes through projection center, in RADIANS
     */
    public ObliqueMercator (Ellipsoid ellipsoid,
                            Coordinate center,
                            Unit<Length> units,
                            double falseEasting,
                            double falseNorthing,
                            double k0,
                            double centralLineAzimuth) {
        super(ellipsoid, center, units, falseEasting, falseNorthing);
        _name = WKT_NAME;
        _k0 = k0;
        _alpha = centralLineAzimuth;
        _subPhi = new double[4];
        computeParameters();
    }

    /** */
    public ObliqueMercator (Ellipsoid ellipsoid, ProjectionParameters parameters)
            throws ParseException {
        super(ellipsoid, parameters);
        _name = WKT_NAME;
        _k0 = parameters.getDimensionlessParameter(SCALE_FACTOR_PROPERTY);
        _alpha = parameters.getAngularParameter(AZIMUTH_PROPERTY);
        _subPhi = new double[4];
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
            computeParameters();
        }
    }

    /** */
    public double getCentralLineAzimuth () {
        return _alpha;
    }

    /** */
    public void setCentralLineAzimuth (double newAzimuth) {
        if (newAzimuth != _alpha) {
            _alpha = newAzimuth;
            computeParameters();
        }
    }

    /** */
    public boolean equals (Object obj) {
        if (super.equals(obj)) {
            ObliqueMercator proj = (ObliqueMercator)obj;
            return (StrictMath.abs(proj._k0 - this._k0) < GeometryUtils.EPSILON) &&
                   (StrictMath.abs(proj._alpha - this._alpha) < GeometryUtils.EPSILON);
        } else {
            return(false);
        }
    }

    public int hashCode() {
        return (int) ((super.hashCode() *
              Math.round(this._k0 / GeometryUtils.EPSILON) *
              Math.round(this._alpha / GeometryUtils.EPSILON)) % Integer.MAX_VALUE);
    }

    /** */
    private double getT (double phi) {
        double eSinPhi = _e*StrictMath.sin(phi);
        return(StrictMath.tan(GeometryUtils.QUARTER_PI - (phi / 2.0)) / StrictMath.pow((1.0 - eSinPhi) / (1.0 + eSinPhi), _e / 2.0));
    }

    //-------------------------------------------------------------------------
    // HemisphericalProjection implementation
    //-------------------------------------------------------------------------

    /**
     * Returns the center of the clipping hemisphere.
     * The center of the clipping hemisphere for an azimuthal projection is the
     * same as the center of the projection.
     * @return the center of the clipping hemisphere
     */
    protected Coordinate getHemisphereCenter () {
        return _hemisphereCenter;
    }

    /**
     * Returns the maximum angular distance from the center of the clipping
     * hemisphere to which polylines & polygons are clipped.
     * Our clipping hemisphere has a radius of 81 degrees.
     * @return the radius of the clipping hemisphere
     */
    protected double getMaxC () {
        return GeometryUtils.HALF_PI;
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
        double t = getT(phi);
        double Q = _E / StrictMath.pow(t, _B);
        double S = (Q - (1.0 / Q)) / 2.0;
        double T = (Q + (1.0 / Q)) / 2.0;
        double V = StrictMath.sin(_B * GeometryUtils.wrap_longitude(lambda - _lambdaZ));
        double U = (-V*_cosGamma0 + S*_sinGamma0) / T;
        double v = _A * StrictMath.log((1.0-U)/(1.0+U))/(2.0*_B);
        double u = (_A/_B)*StrictMath.atan2((S*_cosGamma0)+(V*_sinGamma0),StrictMath.cos(_B*GeometryUtils.wrap_longitude(lambda-_lambdaZ)));
        storage.x = v * _cosAlpha + u * _sinAlpha;
        storage.y = u * _cosAlpha - v * _sinAlpha;
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
        double vp = (x * _cosAlpha) - (y * _sinAlpha);
        double up = (y * _cosAlpha) + (x * _sinAlpha);
        double Qp = StrictMath.pow(StrictMath.E, -((_B*vp)/_A));
        double Sp = (Qp - (1.0 / Qp)) / 2.0;
        double Tp = (Qp + (1.0 / Qp)) / 2.0;
        double Vp = StrictMath.sin((_B * up) / _A);
        double Up = ((Vp*_cosGamma0) + (Sp*_sinGamma0)) / Tp;
        if (StrictMath.abs(Up) == 1.0) {
            storage.y = GeometryUtils.HALF_PI * GeometryUtils.sign(Up);
        } else {
            double t = StrictMath.pow(_E / StrictMath.sqrt((1.0 + Up) / (1.0 - Up)), (1.0 / _B));
            double chi = GeometryUtils.HALF_PI - 2.0 * StrictMath.atan(t);
            storage.y = chi +
                        (_subPhi[0] * StrictMath.sin(2.0 * chi)) +
                        (_subPhi[1] * StrictMath.sin(4.0 * chi)) +
                        (_subPhi[2] * StrictMath.sin(6.0 * chi)) +
                        (_subPhi[3] * StrictMath.sin(8.0 * chi));
        }
        storage.x = _lambdaZ - (StrictMath.atan2((Sp*_cosGamma0)-(Vp*_sinGamma0), StrictMath.cos((_B*up)/_A)) / _B);
        return storage;
    }

    /**
     * Initialize parameters, and recompute them whenever the ellipsoid or
     * projection center changes.
     */
    protected void computeParameters () {
        _e = StrictMath.sqrt(_e2);
        double sinPhi0 = StrictMath.sin(_phi0);
        double cosPhi0 = StrictMath.cos(_phi0);
        _B = StrictMath.sqrt(1.0 + (_e2*cosPhi0*cosPhi0*cosPhi0*cosPhi0)/(1.0 - _e2));
        _A = (_a*_B*_k0*StrictMath.sqrt(1.0 - _e2)) / (1.0 - _e2*sinPhi0*sinPhi0);
        double t0 = getT(_phi0);
        double D = (_B*StrictMath.sqrt(1.0 - _e2))/(cosPhi0*StrictMath.sqrt(1.0 - _e2*sinPhi0*sinPhi0));
        double Dsq = D*D;
        if (Dsq < 1.0) {
            Dsq = 1.0;
        }
        double F = D + (StrictMath.sqrt(Dsq - 1.0) * GeometryUtils.sign(_phi0));
        _E = F*StrictMath.pow(t0, _B);
        double G = (F - (1.0 / F)) / 2.0;
        _sinAlpha = StrictMath.sin(_alpha);
        _cosAlpha = StrictMath.cos(_alpha);
        double gamma0 = StrictMath.asin(_sinAlpha / D);
        _sinGamma0 = StrictMath.sin(gamma0);
        _cosGamma0 = StrictMath.cos(gamma0);
        _lambdaZ = GeometryUtils.wrap_longitude(_lambda0 - (StrictMath.asin(G * StrictMath.tan(gamma0)) / _B));
        _subPhi[0] = (_e2/2.0) + ((5.0*_e2*_e2)/24.0) + ((_e2*_e2*_e2)/12.0) + ((13.0*_e2*_e2*_e2*_e2)/360.0);
        _subPhi[1] = ((7.0*_e2*_e2)/48.0) + ((29.0*_e2*_e2*_e2)/240.0) + ((811.0*_e2*_e2*_e2*_e2)/11520.0);
        _subPhi[2] = ((7.0*_e2*_e2*_e2)/120.0) + ((81.0*_e2*_e2*_e2*_e2)/1120);
        _subPhi[3] = ((4279.0*_e2*_e2*_e2*_e2)/161280.0);
        _hemisphereCenter = new Coordinate(_phi0, _lambda0);
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
        result.addAngularParameter(AZIMUTH_PROPERTY, _alpha, SI.RADIAN);
        return result;
    }

    //--------------------------------------------------------------------------
    // Cloneable implementation
    //--------------------------------------------------------------------------

    /** */
    public Object clone () {
        ObliqueMercator clone = (ObliqueMercator)super.clone();
        clone._subPhi = _subPhi.clone();
        return clone;
    }
}
