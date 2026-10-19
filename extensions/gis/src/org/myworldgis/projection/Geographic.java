//
// Copyright (c) 2000 the National Geographic Society. All rights reserved.
//

package org.myworldgis.projection;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.myworldgis.util.GeometryUtils;
import org.ngs.ngunits.SI;
import org.ngs.ngunits.Unit;
import org.ngs.ngunits.UnitConverter;
import org.ngs.ngunits.quantity.Angle;


/**
 * Geographic projection. Treats lat and lon as y and x values.
 */
public final class Geographic extends AbstractProjection {

    //-------------------------------------------------------------------------
    // Instance variables
    //-------------------------------------------------------------------------

    /** */
    private Unit<Angle> _units;

    /** */
    private UnitConverter _toRadians;

    /** */
    private UnitConverter _fromRadians;

    /** Cosine of the center latitude */
    private double _cosPhi0;

    //-------------------------------------------------------------------------
    // Constructors
    //-------------------------------------------------------------------------

    /**
     * Construct an Geographic projection.
     * @param ellipsoid the ellipsoid for the projection
     * @param center the center of the projection
     */
    public Geographic (Ellipsoid ellipsoid,
                       Coordinate center,
                       Unit<Angle> units) {
        super(ellipsoid, center);
        _name = "Geographic";
        _units = units;
        _toRadians = _units.getConverterTo(SI.RADIAN);
        _fromRadians = SI.RADIAN.getConverterTo(_units);
        computeParameters();
    }

    //-------------------------------------------------------------------------
    // Instance methods
    //-------------------------------------------------------------------------

    /** */
    public Unit<Angle> getUnits () {
        return _units;
    }

    /** */
    public void setUnits (Unit<Angle> newUnits) {
        _units = newUnits;
        _toRadians = newUnits.getConverterTo(SI.RADIAN);
        _fromRadians = SI.RADIAN.getConverterTo(newUnits);
    }

    /** */
    public boolean equals(Object obj) {
        if (super.equals(obj)) {
            Geographic proj = (Geographic)obj;
            return proj._units.equals(this._units);
        } else {
            return(false);
        }
    }

    public int hashCode() {
        return _units.hashCode();
    }

    //-------------------------------------------------------------------------
    // AbstractProjection implementation
    //-------------------------------------------------------------------------

    /** */
    public Point process (Point point) {
        return point;
    }

    /** */
    public MultiPolygon process (Polygon poly) {
        if (_lineType == LineType.RHUMB) {
            poly = ProjectionUtils.createRhumbPoly(poly);
        } else if (_lineType == LineType.GREATCIRCLE) {
            poly = ProjectionUtils.createGreatCirclePoly(poly);
        }
        return ProjectionUtils.wrap(poly, _lambda0);
    }

    /** */
    public MultiLineString process (LineString line) {
        if (_lineType == LineType.RHUMB) {
            line = ProjectionUtils.createRhumbLine(line);
        } else if (_lineType == LineType.GREATCIRCLE) {
            line = ProjectionUtils.createGreatCircleLine(line);
        }
        return ProjectionUtils.wrap(line, _lambda0);
    }

    /** */
    protected Coordinate forwardPoint (double lon, double lat, Coordinate storage) {
        storage.x = _fromRadians.convert(GeometryUtils.wrap_longitude(lon - _lambda0) * _cosPhi0);
        storage.y = _fromRadians.convert(lat);
        return storage;
    }

    /**
     * Inverse projects a point, ignoring <code>_units</code> and
     * any false easting and/or northing.
     * @param x the x coordinate (in projected units) of the point to be inverse projected
     * @param y the y coordinate (in projected units) of the point to be inverse projected
     * @param storage a place to store the result
     * @return The inverse projected point. Same object as <code>storage</code>.
     */
    protected Coordinate inversePoint (double x, double y, Coordinate storage) {
        storage.x = _lambda0 + (_toRadians.convert(x) / _cosPhi0);
        storage.y = _toRadians.convert(y);
        return storage;
    }

    /**
     * Called to initialize parameters, and to recompute them whenever the
     * ellipsoid or projection center changes.
     */
    protected void computeParameters () {
        _cosPhi0 = StrictMath.cos(_phi0);
    }

    //-------------------------------------------------------------------------
    // Projection implementation
    //-------------------------------------------------------------------------

    /** */
    public ProjectionParameters getParameters () {
        return new ProjectionParameters(_units, SI.METRE);
    }
}
