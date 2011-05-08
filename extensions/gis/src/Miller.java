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
 * Miller Cylindrical projection.
 * Formulas from Snyder, John P. (1987). "Map Projections -- A Working Manual".
 * US Geological Survey Professional Paper 1395, US Government Printing Office,
 * Washington, DC. pp. 38-47
 */
public final strictfp class Miller extends Cylindrical {
    
    //--------------------------------------------------------------------------
    // Class variables
    //--------------------------------------------------------------------------
    
    /** */
    public static final String WKT_NAME = "Miller_Cylindrical";
    
    /** */
    public static final String CENTER_LON_PROPERTY = "longitude_of_center";
    
    /** */
    public static final String CENTER_LAT_PROPERTY = "latitude_of_center";
    
    //-------------------------------------------------------------------------
    // Constructors
    //-------------------------------------------------------------------------
    
    /**
     * Construct a Miller projection.
     * @param ellipsoid the ellipsoid for the projection
     * @param center the center of the projection
     * @param units the linear units of the projected data
     * @param falseEasting value to add to x coordinate of each projected point, in METERS
     * @param falseNorthing value to add to y coordinate of each projected point, in METERS
     */
    public Miller (Ellipsoid ellipsoid, 
                   Coordinate center, 
                   Unit<Length> units,
                   double falseEasting,
                   double falseNorthing) {
        super(ellipsoid, center, units, falseEasting, falseNorthing);
        _name = WKT_NAME;
        computeParameters();
    }
    
    /** */
    public Miller (Ellipsoid ellipsoid, ProjectionParameters parameters) 
            throws ParseException {
        super(ellipsoid, parameters);
        _name = WKT_NAME;
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
        storage.x = _a * GeometryUtils.wrap_longitude(lon - _lambda0);
        storage.y = _a * GeometryUtils.asinh(StrictMath.tan(0.8 * lat)) / 0.8;
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
        storage.x = (x / _a) + _lambda0;
        storage.y = StrictMath.atan(GeometryUtils.sinh(0.8 * y / _a)) / 0.8;
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
