//
// Copyright (c) 2000 the National Geographic Society. All rights reserved.
//

package org.myworldgis.projection;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import org.myworldgis.util.GeometryUtils;
import org.ngs.ngunits.SI;
import org.ngs.ngunits.Unit;
import org.ngs.ngunits.quantity.Length;


/**
 * Base class of all conic projections.
 */
public abstract class Conic extends HemisphericalProjection {

    //-------------------------------------------------------------------------
    // Class variables
    //-------------------------------------------------------------------------

    /** */
    public static final String STANDARD_PARALLEL_1_PROPERTY = "standard_parallel_1";

    /** */
    public static final String STANDARD_PARALLEL_2_PROPERTY = "standard_parallel_2";

    /** North pole constant for our clipping hemisphere center */
    protected static final transient Coordinate NORTH_POLE = new Coordinate(0.0f, GeometryUtils.HALF_PI);

    /** South pole constant for our clipping hemisphere center */
    protected static final transient Coordinate SOUTH_POLE = new Coordinate(0.0f, -GeometryUtils.HALF_PI);

    //-------------------------------------------------------------------------
    // Instance variables
    //-------------------------------------------------------------------------

    /** Latitude of the first standard parallel, in RADIANS */
    protected double _phi1;

    /** Latitude of the second standard parallel, in RADIANS */
    protected double _phi2;

    //-------------------------------------------------------------------------
    // Constructors
    //-------------------------------------------------------------------------

    /**
     * Construct a Conic projection.
     * @param ellipsoid the ellipsoid for the projection
     * @param center the center of the projection
     * @param falseEasting value to add to x coordinate of each projected point, in projected units
     * @param falseNorthing value to add to y coordinate of each projected point, in projected units
     * @param phi1 latitude of the first standard parallel of the projection, in RADIANS
     * @param phi2 latitude of the second standard parallel of the projection, in RADIANS
     */
    public Conic (Ellipsoid ellipsoid,
                  Coordinate center,
                  Unit<Length> units,
                  double falseEasting,
                  double falseNorthing,
                  double phi1,
                  double phi2) {
        super(ellipsoid, center, units, falseEasting, falseNorthing);
        _phi1 = phi1;
        _phi2 = phi2;
        if (StrictMath.abs(_phi2) == StrictMath.abs(_phi1)) {
            // all kinds of mathematical problems result when
            // abs(phi1) == abs(phi2)
            _phi2 += (_phi2 * 0.01);
        }
    }

    /** */
    public Conic (Ellipsoid ellipsoid, ProjectionParameters parameters)
            throws ParseException {
        super(ellipsoid, parameters);
        _phi1 = parameters.getAngularParameter(STANDARD_PARALLEL_1_PROPERTY);
        _phi2 = parameters.getAngularParameter(STANDARD_PARALLEL_2_PROPERTY);
        if (StrictMath.abs(_phi2) == StrictMath.abs(_phi1)) {
            // all kinds of mathematical problems result when
            // abs(phi1) == abs(phi2)
            _phi2 += (_phi2 * 0.01);
        }
    }

    //-------------------------------------------------------------------------
    // Instance methods
    //-------------------------------------------------------------------------

    /** */
    public double getStandardParallel1 () {
        return _phi1;
    }

    /** */
    public void setStandardParallel1 (double newPhi1) {
        if (newPhi1 != _phi1) {
            _phi1 = newPhi1;
            if (_phi2 == -_phi1) {
                _phi2 += (_phi2 * 0.01);
            }
            computeParameters();
        }
    }

    /** */
    public double getStandardParallel2 () {
        return _phi2;
    }

    /** */
    public void setStandardParallel2 (double newPhi2) {
        if (newPhi2 != _phi2) {
            _phi2 = newPhi2;
            if (_phi2 == -_phi1) {
                _phi2 += (_phi2 * 0.01);
            }
            computeParameters();
        }
    }

    /** */
    public boolean equals (Object obj) {
        if (super.equals(obj)) {
            Conic proj = (Conic)obj;
            return (StrictMath.abs(proj._phi1 - this._phi1) < GeometryUtils.EPSILON) &&
                   (StrictMath.abs(proj._phi2 - this._phi2) < GeometryUtils.EPSILON);
        } else {
            return(false);
        }
    }

    //-------------------------------------------------------------------------
    // HemisphericalProjection implementation
    //-------------------------------------------------------------------------

    /**
     * Returns the center of the clipping hemisphere.
     * The center of the clipping hemisphere is the North Pole if the first
     * standard parallel is below the second, the South Pole if the first is
     * below the second.
     * @return the center of the clipping hemisphere
     */
    protected Coordinate getHemisphereCenter () {
        return (_phi0 >= 0.0) ? NORTH_POLE : SOUTH_POLE;
    }

    /**
     * Returns the maximum angular distance from the center of the clipping
     * hemisphere to which polylines & polygons are clipped.
     * Our clipping hemisphere has a radius of 3pi/4, so it's actually larger
     * than a hemisphere, but I don't know the proper name for such a
     * construction.
     * @return the radius of the clipping hemisphere
     */
    protected double getMaxC () {
        return GeometryUtils.THREE_QUARTERS_PI;
    }

    //-------------------------------------------------------------------------
    // Projection implementation
    //-------------------------------------------------------------------------

    /** */
    public MultiPolygon process (Polygon poly) {
        GeometryFactory factory = poly.getFactory();
        MultiPolygon mp = ProjectionUtils.wrap(poly, _lambda0);
        List<Polygon> result = new ArrayList<Polygon>(mp.getNumGeometries());
        for (int i = 0; i < mp.getNumGeometries(); i += 1) {
            MultiPolygon clippedPoly = super.process((Polygon)mp.getGeometryN(i));
            for (int j = 0; j < clippedPoly.getNumGeometries(); j += 1) {
                result.add((Polygon)clippedPoly.getGeometryN(j));
            }
        }
        return factory.createMultiPolygon(result.toArray(new Polygon[result.size()]));
    }

    /** */
    public MultiLineString process (LineString line) {
        GeometryFactory factory = line.getFactory();
        MultiLineString mls = ProjectionUtils.wrap(line, _lambda0);
        List<LineString> result = new ArrayList<LineString>(mls.getNumGeometries());
        for (int i = 0; i < mls.getNumGeometries(); i += 1) {
            MultiLineString clippedLine = super.process((LineString)mls.getGeometryN(i));
            for (int j = 0; j < clippedLine.getNumGeometries(); j += 1) {
                result.add((LineString)clippedLine.getGeometryN(j));
            }
        }
        return factory.createMultiLineString(result.toArray(new LineString[result.size()]));
    }

    /** */
    public ProjectionParameters getParameters () {
        ProjectionParameters result = super.getParameters();
        result.addAngularParameter(STANDARD_PARALLEL_1_PROPERTY, _phi1, SI.RADIAN);
        result.addAngularParameter(STANDARD_PARALLEL_2_PROPERTY, _phi2, SI.RADIAN);
        return result;
    }
}
