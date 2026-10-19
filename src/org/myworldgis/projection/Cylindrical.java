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
import java.text.ParseException;
import org.ngs.ngunits.Unit;
import org.ngs.ngunits.quantity.Length;


/**
 * Base class of all cylindrical projections.
 */
public abstract class Cylindrical extends AbstractProjectedProjection {

    //-------------------------------------------------------------------------
    // Constructors
    //-------------------------------------------------------------------------

    /**
     * Construct a Cylindrical projection.
     * @param ellipsoid the ellipsoid for the projection
     * @param center the center of the projection
     */
    public Cylindrical (Ellipsoid ellipsoid,
                        Coordinate center,
                        Unit<Length> units,
                        double falseEasting,
                        double falseNorthing) {
        super(ellipsoid, center, units, falseEasting, falseNorthing);
    }

    /** */
    public Cylindrical (Ellipsoid ellipsoid, ProjectionParameters parameters)
            throws ParseException {
        super(ellipsoid, parameters);
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
        if ((_lineType == LineType.RHUMB) && (!(this instanceof Mercator))) {
            poly = ProjectionUtils.createRhumbPoly(poly);
        } else if (_lineType == LineType.GREATCIRCLE) {
            poly = ProjectionUtils.createGreatCirclePoly(poly);
        }
        return ProjectionUtils.wrap(poly, _lambda0);
    }

    /** */
    public MultiLineString process (LineString line) {
        if ((_lineType == LineType.RHUMB) && (!(this instanceof Mercator))) {
            line = ProjectionUtils.createRhumbLine(line);
        } else if (_lineType == LineType.GREATCIRCLE) {
            line = ProjectionUtils.createGreatCircleLine(line);
        }
        return ProjectionUtils.wrap(line, _lambda0);
    }

    /**
     * Initialize parameters, and recompute them whenever the ellipsoid or
     * projection center changes.
     */
    protected void computeParameters () { }
}
