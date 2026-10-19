//
// Copyright (c) 2006 the National Geographic Society. All rights reserved.
//

package org.myworldgis.projection;

import org.locationtech.jts.geom.Coordinate;
import java.text.ParseException;
import org.myworldgis.util.GeometryUtils;
import org.ngs.ngunits.NonSI;
import org.ngs.ngunits.SI;
import org.ngs.ngunits.Unit;
import org.ngs.ngunits.UnitConverter;
import org.ngs.ngunits.quantity.Length;


/**
 * Abstract base class for projected (i.e., not Geographic) Projections.
 *
 * All parameters, unless otherwise noted, are in RADIANS
 */
public abstract class AbstractProjectedProjection extends AbstractProjection {

    //--------------------------------------------------------------------------
    // Class variables
    //--------------------------------------------------------------------------

    /** */
    public static final String FALSE_EASTING_PROPERTY = "false_easting";

    /** */
    public static final String FALSE_NORTHING_PROPERTY = "false_northing";

    //--------------------------------------------------------------------------
    // Instance variables
    //--------------------------------------------------------------------------

    /** */
    private Unit<Length> _units;

    /** */
    private UnitConverter _toMeters;

    /** */
    private UnitConverter _fromMeters;

    /** Value to be added to the x coordinate of every projected point, in _units */
    private double _falseEasting;

    /** Value to be added to the y coordinate of every projected point, in _units */
    private double _falseNorthing;

    //--------------------------------------------------------------------------
    // Constructors
    //--------------------------------------------------------------------------

    /**
     * Construct an AbstractProjection.
     * @param ellipsoid the ellipsoid for the projection
     * @param center the center of the projection
     * @param units the linear units of the projected data
     * @param falseEasting value to add to x coordinate of each projected point, in projected units
     * @param falseNorthing value to add to y coordinate of each projected point, in projected units
     */
    public AbstractProjectedProjection (Ellipsoid ellipsoid,
                                        Coordinate center,
                                        Unit<Length> units,
                                        double falseEasting,
                                        double falseNorthing) {
        super(ellipsoid, center);
        _units = units;
        _toMeters = _units.getConverterTo(SI.METRE);
        _fromMeters = SI.METRE.getConverterTo(_units);
        _falseEasting = falseEasting;
        _falseNorthing = falseNorthing;
    }

    /** */
    public AbstractProjectedProjection (Ellipsoid ellipsoid,
                                        ProjectionParameters parameters)
            throws ParseException {
        super(ellipsoid,
              parameters.getCenterLongitude(),
              parameters.getCenterLatitude());
        _units = parameters.getLinearUnits();
        _toMeters = _units.getConverterTo(SI.METRE);
        _fromMeters = SI.METRE.getConverterTo(_units);
        _falseEasting = parameters.getLinearParameter(FALSE_EASTING_PROPERTY);
        _falseNorthing = parameters.getLinearParameter(FALSE_NORTHING_PROPERTY);
    }

    //--------------------------------------------------------------------------
    // Abstract instance methods
    //--------------------------------------------------------------------------

    /**
     * Forward projects a point, ignoring <code>_units</code> and
     * any false easting and/or northing.
     * @param lon the longitude of the point to project, in RADIANS
     * @param lat the latitude of the point to project, in RADIANS
     * @param storage a place to store the result
     * @return The projected point in METERS. Same object as <code>storage</code>
     */
    protected abstract Coordinate forwardPointRaw (double lon, double lat, Coordinate storage);

    /**
     * Inverse projects a point, ignoring <code>_units</code> and
     * any false easting and/or northing.
     * @param x the x coordinate (in METERS) of the point to be inverse projected
     * @param y the y coordinate (in METERS) of the point to be inverse projected
     * @param storage a place to store the result
     * @return The inverse projected point. Same object as <code>storage</code>.
     */
    protected abstract Coordinate inversePointRaw (double x, double y, Coordinate storage);

    //--------------------------------------------------------------------------
    // AbstractProjection implementation
    //--------------------------------------------------------------------------

    /** */
    protected Coordinate forwardPoint (double lon, double lat, Coordinate storage) {
        storage = forwardPointRaw(lon, lat, storage);
        storage.x = _fromMeters.convert(storage.x) + _falseEasting;
        storage.y = _fromMeters.convert(storage.y) + _falseNorthing;
        return storage;
    }

    /** */
    protected Coordinate inversePoint (double x, double y, Coordinate storage) {
        storage = inversePointRaw(_toMeters.convert(x - _falseEasting),
                                  _toMeters.convert(y - _falseNorthing),
                                  storage);
        return(storage);
    }

    //--------------------------------------------------------------------------
    // Instance methods
    //--------------------------------------------------------------------------

    /** */
    public Unit<Length> getUnits () {
        return _units;
    }

    /** */
    public void setUnits (Unit<Length> newUnits) {
        double falseEastingM = _toMeters.convert(_falseEasting);
        double falseNorthingM = _toMeters.convert(_falseNorthing);
        _units = newUnits;
        _toMeters = newUnits.getConverterTo(SI.METRE);
        _fromMeters = SI.METRE.getConverterTo(newUnits);
        setFalseEasting(Double.valueOf(_fromMeters.convert(falseEastingM)));
        setFalseNorthing(Double.valueOf(_fromMeters.convert(falseNorthingM)));
    }

    /** */
    public double getFalseEasting () {
        return _falseEasting;
    }

    /** */
    public void setFalseEasting (double newEasting) {
        if (newEasting != _falseEasting) {
            _falseEasting = newEasting;
        }
    }

    /** */
    public double getFalseNorthing () {
        return _falseNorthing;
    }

    /** */
    public void setFalseNorthing (double newNorthing) {
        if (newNorthing != _falseNorthing) {
            _falseNorthing = newNorthing;
        }
    }

    /** */
    public boolean equals(Object obj) {
        if (super.equals(obj)) {
            AbstractProjectedProjection proj = (AbstractProjectedProjection)obj;
            return proj._units.equals(this._units) &&
                   (StrictMath.abs(proj._falseEasting - this._falseEasting) < GeometryUtils.EPSILON) &&
                   (StrictMath.abs(proj._falseNorthing - this._falseNorthing) < GeometryUtils.EPSILON);
        } else {
            return(false);
        }
    }

    //--------------------------------------------------------------------------
    // Projection implementation
    //--------------------------------------------------------------------------

    /** */
    public double getCenterEasting () {
        return _falseEasting;
    }

    /** */
    public double getCenterNorthing () {
        return _falseNorthing;
    }

    /** */
    public ProjectionParameters getParameters () {
        ProjectionParameters result = new ProjectionParameters(NonSI.DEGREE_ANGLE, _units);
        result.addLinearParameter(FALSE_EASTING_PROPERTY, _falseEasting, SI.METRE);
        result.addLinearParameter(FALSE_NORTHING_PROPERTY, _falseNorthing, SI.METRE);
        return result;
    }
}
