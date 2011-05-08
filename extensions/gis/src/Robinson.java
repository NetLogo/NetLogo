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
 * Robinson pseudocylindrical projection.
 * Formulas from the source code of the USGS computer program "gctpc2", 
 * available <a href="http://mapping.usgs.gov/ftp/software/current_software/gctpc2/">Here</a>
 */
public final strictfp class Robinson extends Cylindrical {
    
    //-------------------------------------------------------------------------
    // Class variables
    //-------------------------------------------------------------------------
    
    /** */
    public static final String WKT_NAME = "Robinson";
    
    /** */
    public static final String CENTER_LON_PROPERTY = "longitude_of_center";
    
    /** */
    private static final double EPSLN = 0.000001;
    
    /** */
    private static final double[] pr = { 0.0, -0.062, 0.0, 0.062, 0.124, 
                                         0.186, 0.248, 0.31, 0.372, 0.434, 
                                         0.4958, 0.5571, 0.6176, 0.6769, 
                                         0.7346, 0.7903, 0.8435, 0.8936, 
                                         0.9394, 0.9761, 1.0 };
    
    /** */
    private static final double[] xlr = { 0.0, 0.9986, 1.0, 0.9986, 0.9954, 
                                          0.99, 0.9822, 0.973, 0.96, 0.9427,
                                          0.9216, 0.8962, 0.8679, 0.835,
                                          0.7986, 0.7597, 0.7186, 0.6732,
                                          0.6213, 0.5722, 0.5322 };
    
    /** */
    static {
        for (int i = 0; i < 21; i += 1) {
            xlr[i] *= 0.9858;
        }
    }

    //-------------------------------------------------------------------------
    // Class methods
    //-------------------------------------------------------------------------
    
    /** */
    private static ProjectionParameters addCenterLon (ProjectionParameters params) {
        // HACK: the AbstractProjectedProjection will barf if the
        // latitude_of_center isn't present.
        params.addParameter("latitude_of_center", Double.valueOf(0.0));
        return params;
    }
    
    //-------------------------------------------------------------------------
    // Constructors
    //-------------------------------------------------------------------------
    
    /**
     * Construct a Robinson projection.
     * @param ellipsoid the ellipsoid for the projection
     * @param center the center of the projection
     * @param units the linear units of the projected data
     * @param falseEasting value to add to x coordinate of each projected point, in projected units
     * @param falseNorthing value to add to y coordinate of each projected point, in projected units
     */
    public Robinson (Ellipsoid ellipsoid, 
                     Coordinate center, 
                     Unit<Length> units,
                     double falseEasting,
                     double falseNorthing) {
        super(ellipsoid, center, units, falseEasting, falseNorthing);
        _name = WKT_NAME;
        computeParameters();
    }
    
    /** */
    public Robinson (Ellipsoid ellipsoid, ProjectionParameters parameters) 
            throws ParseException {
        super(ellipsoid, addCenterLon(parameters));
        _name = WKT_NAME;
        computeParameters();
    }
    
    //-------------------------------------------------------------------------
    // AbstractProjection implementation
    //-------------------------------------------------------------------------
    
    /** */
    public boolean isRhumbRectangular () {
        return(false);
    }
    
    /** 
     * Forward projects a point.
     * @param lat the latitude of the point to project, in RADIANS
     * @param lat the longitude of the point to project, in RADIANS
     * @param storage a place to store the result
     * @return The projected point. Same object as <code>storage</code>
     */
    protected Coordinate forwardPointRaw (double lon, double lat, Coordinate storage) {
        double dlon = GeometryUtils.wrap_longitude(lon - _lambda0);
        double p2 = StrictMath.abs(lat / 5.0 / 0.01745329252);
        int ip1 = StrictMath.min((int)StrictMath.floor(p2 - EPSLN), 17);
        p2 -= (double)ip1;
        storage.x = _a * (xlr[ip1 + 2] + p2 * (xlr[ip1 + 3] - xlr[ip1 + 1]) / 2.0 + p2 * p2 * (xlr[ip1 + 3] - 2.0 * xlr[ip1 + 2] + xlr[ip1 + 1])/2.0) * dlon;
        storage.y = GeometryUtils.sign(lat) * _a * (pr[ip1 + 2] + p2 * (pr[ip1 + 3] - pr[ip1 +1]) / 2.0 + p2 * p2 * (pr[ip1 + 3] - 2.0 * pr[ip1 + 2] + pr[ip1 + 1]) / 2.0) * GeometryUtils.HALF_PI;
        return(storage);
    }
    
    /** 
     * Inverse projects a point.
     * @param x the x coordinate of the point to be inverse projected
     * @param y the y coordinate of the point to be inverse projected
     * @param storage a place to store the result
     * @return The inverse of <code>pt</code>. Same object as <code>storage</code>.
     */
    protected Coordinate inversePointRaw (double x, double y, Coordinate storage) {
        double yy = 2.0 * y / StrictMath.PI / _a;
        double phid = yy * 90.0;
        double p2 = StrictMath.abs(phid / 5.0);
        int ip1 = (int)StrictMath.floor(p2 - EPSLN);
        if (ip1 >= 18) {
            storage.x = Double.NaN;
            storage.y = Double.NaN;
            return storage;
        }
        if (ip1 == 0) ip1 = 1;
        for (int i = 0;;) {
            double u = pr[ip1 + 3] - pr[ip1 + 1];
            double v = pr[ip1 + 3] - 2.0 * pr[ip1 + 2] + pr[ip1 + 1];
            double t = 2.0 * (StrictMath.abs(yy) - pr[ip1 + 2]) / u;
            double c = v / u;
            p2 = t * (1.0 - c * t * (1.0 - 2.0 * c * t));
            if ((p2 >= 0.0) || (ip1 == 1)) {
                phid = GeometryUtils.sign(y) * (p2 + (double) ip1 ) * 5.0;
                double y1;
                do {
                    p2 = StrictMath.abs(phid / 5.0);
                    ip1 = (int)(p2 - EPSLN);
                    if (ip1 >= 18) {
                        storage.x = Double.NaN;
                        storage.y = Double.NaN;
                        return storage;
                    }
                    p2 -= (double)ip1;
                    y1 = GeometryUtils.sign(y) * _a * (pr[ip1 +2] + p2 *(pr[ip1 + 3] - pr[ip1 +1]) / 2.0 + p2 * p2 * (pr[ip1 + 3] - 2.0 * pr[ip1 + 2] + pr[ip1 + 1])/2.0) * GeometryUtils.HALF_PI;
                    phid += -180.0 * (y1 - y) / StrictMath.PI / _a;
                    i += 1;
                    if (i > 75) {
                        throw(new ArithmeticException("too many iterations in inverse"));
                    }
                } while (StrictMath.abs(y1 - y) > 0.00001);
                break;
            } else {
                ip1 -= 1;
                if (ip1 < 0) {
                    throw(new ArithmeticException("too many iterations in inverse"));
                }
            }
        }
        storage.y = phid * 0.01745329252;
        storage.x = GeometryUtils.wrap_longitude(_lambda0 + x / _a / (xlr[ip1 + 2] + p2 * (xlr[ip1 + 3] - xlr[ip1 + 1]) / 2.0 + p2 * p2 * (xlr[ip1 + 3] - 2.0 * xlr[ip1 + 2] + xlr[ip1 + 1]) / 2.0));
        return storage;
    }

    //-------------------------------------------------------------------------
    // Projection implementation
    //-------------------------------------------------------------------------
    
    /** */
    public ProjectionParameters getParameters () {
        ProjectionParameters result = super.getParameters();
        result.addAngularParameter(CENTER_LON_PROPERTY, _lambda0, SI.RADIAN);
        return result;
    }
}
