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
 * Cylindrical Equal-Area projection.
 * Formulas from Snyder, John P. (1987). "Map Projections -- A Working Manual".
 * US Geological Survey Professional Paper 1395, US Government Printing Office,
 * Washington, DC. pp. 76-85
 */
public final class CylindricalEqualArea extends Cylindrical {

    //--------------------------------------------------------------------------
    // Class variables
    //--------------------------------------------------------------------------

    /** */
    public static final String WKT_NAME = "Cylindrical_Equal_Area";

    /** */
    public static final String CENTER_LON_PROPERTY = "central_meridian";

    /** */
    public static final String CENTER_LAT_PROPERTY = "standard_parallel_1";

    //-------------------------------------------------------------------------
    // Instance variables
    //-------------------------------------------------------------------------

    /** Pre-compute these values for the given ellipsoid & projection center
        to save time when projecting. */
    private double _e, _k0, _qp, _subLat[];

    //-------------------------------------------------------------------------
    // Constructors
    //-------------------------------------------------------------------------

    /**
     * Construct a CylindricalEqualArea projection.
     * @param ellipsoid the ellipsoid for the projection
     * @param phi0 the center latitude of the projection, in RADIANS
     * @param lambda0 the center longitude of the projection, in RADIANS
     * @param falseEasting value to add to x coordinate of each projected point, in METERS
     * @param falseNorthing value to add to y coordinate of each projected point, in METERS
     */
    public CylindricalEqualArea (Ellipsoid ellipsoid,
                                 Coordinate center,
                                 Unit<Length> units,
                                 double falseEasting,
                                 double falseNorthing) {
        super(ellipsoid, center, units, falseEasting, falseNorthing);
        _name = WKT_NAME;
        _subLat = new double[3];
        computeParameters();
    }

    /** */
    public CylindricalEqualArea (Ellipsoid ellipsoid, ProjectionParameters parameters)
            throws ParseException {
        super(ellipsoid, parameters);
        _name = WKT_NAME;
        _subLat = new double[3];
        computeParameters();
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
        storage.x = _a * _k0 * GeometryUtils.wrap_longitude(lon - _lambda0);
        double sinPhi = StrictMath.sin(lat);
        double q = (1.0-_e2) * ( sinPhi/(1.0-(_e2*sinPhi*sinPhi)) - (1.0/(2.0*_e))*StrictMath.log((1.0-_e*sinPhi)/(1.0+_e*sinPhi)) );
        storage.y = (_a * q) / (2.0 * _k0);
        return storage;
    }

    /**
     * Inverse projects a point.
     * @param x float the x coordinate (easting) of the point to be inverse projected
     * @param y float the y coordinate (northing) of the point to be inverse projected
     * @param storage LatLonPoint a place to store the result
     * @return The inverse of <code>pt</code>. Same object as <code>storage</code>.
     */
    protected Coordinate inversePointRaw (double x, double y, Coordinate storage) {
        storage.x = _lambda0 + (x / (_a * _k0));
        double beta = StrictMath.asin((2.0*y*_k0)/(_a*_qp));
        storage.y = beta +
                     _subLat[0] * StrictMath.sin(2.0 * beta) +
                     _subLat[1] * StrictMath.sin(4.0 * beta) +
                     _subLat[2] * StrictMath.sin(6.0 * beta);
        return storage;
    }

    /**
     * Initialize parameters, and recompute them whenever the ellipsoid or
     * projection center changes.
     */
    protected void computeParameters () {
        _e = StrictMath.sqrt(_e2);
        double sinPhi0 = StrictMath.sin(_phi0);
        _k0 = StrictMath.cos(_phi0) / StrictMath.sqrt(1.0 - (_e2 * sinPhi0 * sinPhi0));
        _qp = (1.0-_e2) * ((1.0/(1.0-_e2)) - (1.0/(2.0*_e))*StrictMath.log((1.0-_e)/(1.0+_e)));
        _subLat[0] = _e2/3.0 + 31.0*_e2*_e2/180.0 + 517.0*_e2*_e2*_e2/5040.0;
        _subLat[1] = 23.0*_e2*_e2/360.0 + 251.0*_e2*_e2*_e2/3780.0;
        _subLat[2] = 761.0*_e2*_e2*_e2/45360.0;
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
        CylindricalEqualArea clone = (CylindricalEqualArea)super.clone();
        clone._subLat = _subLat.clone();
        return clone;
    }
}
