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
 * Mercator projection.
 * Formulas from Snyder, John P. (1987). "Map Projections -- A Working Manual".
 * US Geological Survey Professional Paper 1395, US Government Printing Office,
 * Washington, DC. pp. 38-47
 */
public final strictfp class Mercator extends Cylindrical {
    
    //--------------------------------------------------------------------------
    // Class variables
    //--------------------------------------------------------------------------
    
    /** */
    public static final String WKT_NAME = "Mercator_1SP";
    
    /** */
    public static final String CENTER_LON_PROPERTY = "central_meridian";
    
    /** */
    public static final String CENTER_LAT_PROPERTY = "latitude_of_origin";
    
    /** */
    public static final String SCALE_FACTOR_PROPERTY = "scale_factor";
    
    //-------------------------------------------------------------------------
    // Instance variables
    //-------------------------------------------------------------------------
    
    /** Scale factor along the equator */
    private double _k0;
    
    /** Pre-compute these values for the given ellipsoid & projection center
        to save time when projecting. */
    private double _e, _subLat[], _spq;
    
    //-------------------------------------------------------------------------
    // Constructors
    //-------------------------------------------------------------------------
        
    /**
     * Construct a Mercator projection.
     * @param ellipsoid the ellipsoid for the projection
     * @param phi0 the center latitude of the projection, in RADIANS
     * @param lambda0 the center longitude of the projection, in RADIANS
     * @param falseEasting value to add to x coordinate of each projected point, in METERS
     * @param falseNorthing value to add to y coordinate of each projected point, in METERS
     */
    public Mercator (Ellipsoid ellipsoid, 
                     Coordinate center, 
                     Unit<Length> units, 
                     double falseEasting,
                     double falseNorthing) {
        this(ellipsoid, center, units, falseEasting, falseNorthing, 1.0);
    }
    
    /**
     * Construct a Mercator projection.
     * @param ellipsoid the ellipsoid for the projection
     * @param phi0 the center latitude of the projection, in RADIANS
     * @param lambda0 the center longitude of the projection, in RADIANS
     * @param falseEasting value to add to x coordinate of each projected point, in METERS
     * @param falseNorthing value to add to y coordinate of each projected point, in METERS
     * @param k0 the scale factor along the equator, usually 1.0
     */
    public Mercator (Ellipsoid ellipsoid, 
                     Coordinate center, 
                     Unit<Length> units, 
                     double falseEasting,
                     double falseNorthing,
                     double scaleFactor) {
        super(ellipsoid, center, units, falseEasting, falseNorthing);
        _name = WKT_NAME;
        _k0 = scaleFactor;
        _subLat = new double[4];
        computeParameters();
    }
    
    /** */
    public Mercator (Ellipsoid ellipsoid, ProjectionParameters parameters) 
            throws ParseException {
        super(ellipsoid, parameters);
        _name = WKT_NAME;
        _k0 = parameters.getDimensionlessParameter(SCALE_FACTOR_PROPERTY);
        _subLat = new double[4];
        computeParameters();
    }
    
    //--------------------------------------------------------------------------
    // Property methods
    //--------------------------------------------------------------------------
    
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
            Mercator proj = (Mercator)obj;
            return (StrictMath.abs(proj._k0 - this._k0) < GeometryUtils.EPSILON);
        } else {
            return(false);
        }
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
        storage.x = _k0 * _a * GeometryUtils.wrap_longitude(lon - _lambda0) * _spq;
        double sinPhi = StrictMath.sin(lat);
        if (sinPhi == 1.0) {
            storage.y = (lat > 0) ? Float.POSITIVE_INFINITY : Float.NEGATIVE_INFINITY;
        } else {
            storage.y = _k0 * ((_a / 2.0) * StrictMath.log(((1 + sinPhi) / (1 - sinPhi)) * StrictMath.pow((1 - (_e * sinPhi)) / (1 + (_e * sinPhi)), _e))) * _spq;
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
        storage.x = ((x / _spq) / (_k0 * _a)) + _lambda0;
        double t = StrictMath.pow(StrictMath.E, -((y / _spq) / (_k0 * _a)));;
        double chi = GeometryUtils.HALF_PI - 2 * StrictMath.atan(t);
        storage.y = chi + 
                    _subLat[0] * StrictMath.sin(2.0 * chi) +
                    _subLat[1] * StrictMath.sin(4.0 * chi) +
                    _subLat[2] * StrictMath.sin(6.0 * chi) +
                    _subLat[3] * StrictMath.sin(8.0 * chi);
        return storage;
    }
    
    /** 
     * Initialize parameters, and recompute them whenever the ellipsoid or 
     * projection center changes.
     */
    protected void computeParameters () {
        _e = StrictMath.sqrt(_e2);
        _subLat[0] = _e2/2.0 + 5.0*_e2*_e2/24.0 + _e2*_e2*_e2/12.0 + 13.0*_e2*_e2*_e2*_e2*360.0;
        _subLat[1] = 7.0*_e2*_e2/48.0 + 29.0*_e2*_e2*_e2/240.0 + 811.0*_e2*_e2*_e2*_e2/11520.0;
        _subLat[2] = 7.0*_e2*_e2*_e2/120.0 + 81.0*_e2*_e2*_e2*_e2/1120.0;
        _subLat[3] = 4279.0*_e2*_e2*_e2*_e2/161280.0;
        if (_phi0 == 0.0) {
            _spq = 1.0;
        } else {
            double sinPhi0 = StrictMath.sin(_phi0);
            _spq = StrictMath.cos(_phi0) / StrictMath.sqrt(1.0 - (_e2 * sinPhi0 * sinPhi0));
        }
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
        Mercator clone = (Mercator)super.clone();
        clone._subLat = _subLat.clone();
        return clone;
    }
}
