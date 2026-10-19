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
 * Equidistant Conic projection.
 * Formulas from Snyder, John P. (1987). "Map Projections -- A Working Manual".
 * US Geological Survey Professional Paper 1395, US Government Printing Office,
 * Washington, DC. pp. 112-115
 */
public final class EquidistantConic extends Conic {

    //--------------------------------------------------------------------------
    // Class variables
    //--------------------------------------------------------------------------

    /** */
    public static final String WKT_NAME = "Equidistant_Conic";

    /** */
    public static final String CENTER_LON_PROPERTY = "longitude_of_center";

    /** */
    public static final String CENTER_LAT_PROPERTY = "latitude_of_center";

    //-------------------------------------------------------------------------
    // Instance variables
    //-------------------------------------------------------------------------

    /** Pre-compute these values for the given ellipsoid & projection center
        to save time when projecting. */
    private double _n, _aG, _rho0, _subM[], _subPhi[];

    //-------------------------------------------------------------------------
    // Constructors
    //-------------------------------------------------------------------------

    /**
     * Construct a EquidistantConic projection.
     * @param ellipsoid the ellipsoid for the projection
     * @param center the center of the projection
     * @param falseEasting value to add to x coordinate of each projected point, in METERS
     * @param falseNorthing value to add to y coordinate of each projected point, in METERS
     * @param phi1 latitude of the first standard parallel of the projection, in RADIANS
     * @param phi2 latitude of the second standard parallel of the projection, in RADIANS
     */
    public EquidistantConic (Ellipsoid ellipsoid,
                             Coordinate center,
                             Unit<Length> units,
                             double falseEasting,
                             double falseNorthing,
                             double phi1,
                             double phi2) {
        super(ellipsoid, center, units, falseEasting, falseNorthing, phi1, phi2);
        _name = WKT_NAME;
        _subM = new double[4];
        _subPhi = new double[4];
        computeParameters();
    }

    /** */
    public EquidistantConic (Ellipsoid ellipsoid, ProjectionParameters parameters)
            throws ParseException {
        super(ellipsoid, parameters);
        _name = WKT_NAME;
        _subM = new double[4];
        _subPhi = new double[4];
        computeParameters();
    }

    //-------------------------------------------------------------------------
    // Instance methods
    //-------------------------------------------------------------------------

    /** */
    private double getm (double phi) {
        double sinPhi = StrictMath.sin(phi);
        return(StrictMath.cos(phi) / StrictMath.sqrt(1.0 - (_e2*sinPhi*sinPhi)));
    }

    /** */
    private double getM (double phi) {
        return(_a * ((_subM[0]*phi) + (_subM[1]*StrictMath.sin(2.0 * phi)) + (_subM[2]*StrictMath.sin(4.0 * phi)) + (_subM[3]*StrictMath.sin(6.0 * phi))));
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
        double M = getM(phi);
        double rho = _aG - M;
        double theta = _n * GeometryUtils.wrap_longitude(lambda - _lambda0);
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
        double rho = StrictMath.sqrt((x*x) + (rho0minusY*rho0minusY)) * GeometryUtils.sign(_n);
        double M = _aG - rho;
        double mu = M / (_a * _subM[0]);
        double theta = (_n < 0.0) ?  StrictMath.atan2(-x, -rho0minusY) : StrictMath.atan2(x, rho0minusY);
        storage.y = mu +
                     (_subPhi[0]*StrictMath.sin(2.0 * mu)) +
                     (_subPhi[1]*StrictMath.sin(4.0 * mu)) +
                     (_subPhi[2]*StrictMath.sin(6.0 * mu)) +
                     (_subPhi[3]*StrictMath.sin(8.0 * mu));
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
     * projection center changses.
     */
    protected void computeParameters () {
        _subM[0] = 1.0 - (_e2/4.0) - ((3.0*_e2*_e2)/64.0) - ((5.0*_e2*_e2*_e2)/256.0);
        _subM[1] = ((3.0*_e2)/8.0) + ((3.0*_e2*_e2)/32.0) + ((45.0*_e2*_e2*_e2)/1024.0);
        _subM[2] = ((15.0*_e2*_e2)/256.0) + ((45.0*_e2*_e2*_e2)/1024.0);
        _subM[3] = ((35.0*_e2*_e2*_e2)/3072.0);
        double M0 = getM(_phi0);
        double m1 = getm(_phi1);
        double M1 = getM(_phi1);
        double m2 = getm(_phi2);
        double M2 = getM(_phi2);
        _n = _a*((m1 - m2) / (M2 - M1));
        _aG = _a * ((m1 / _n) + (M1 / _a));
        _rho0 = _aG - M0;
        double e1 = (1.0 - StrictMath.sqrt(1.0 - _e2)) / (1.0 + StrictMath.sqrt(1.0 - _e2));
        _subPhi[0] = ((3.0*e1)/2.0) - ((27.0*e1*e1*e1)/32.0);
        _subPhi[1] = ((21.0*e1*e1)/16.0) - ((55.0*e1*e1*e1*e1)/32.0);
        _subPhi[2] = ((151.0*e1*e1*e1)/96.0);
        _subPhi[3] = ((1097.0*e1*e1*e1*e1)/512.0);
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
        EquidistantConic clone = (EquidistantConic)super.clone();
        clone._subM = _subM.clone();
        clone._subPhi = _subPhi.clone();
        return clone;
    }
}
