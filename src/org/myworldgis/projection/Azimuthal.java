//
// Copyright (c) 2000 the National Geographic Society. All rights reserved.
//

package org.myworldgis.projection;

import org.locationtech.jts.geom.Coordinate;
import java.text.ParseException;
import org.ngs.ngunits.Unit;
import org.ngs.ngunits.quantity.Length;

/**
 * Base class of all azimuthal projections.
 */
public abstract class Azimuthal extends HemisphericalProjection {

    //-------------------------------------------------------------------------
    // Instance variables
    //-------------------------------------------------------------------------

    /** sine of the center latitude. */
    protected double _sinPhi0;

    /** cosine of the center latitude. */
    protected double _cosPhi0;

    /** */
    protected Coordinate _hemisphereCenter;

    //-------------------------------------------------------------------------
    // Constructors
    //-------------------------------------------------------------------------

    /**
     * Construct an Azimuthal projection.
     * @param ellipsoid the ellipsoid for the projection
     * @param center the center of the projection
     * @param falseEasting value to add to x coordinate of each projected point, in projected units
     * @param falseNorthing value to add to y coordinate of each projected point, in projected units
     */
    public Azimuthal (Ellipsoid ellipsoid,
                      Coordinate center,
                      Unit<Length> units,
                      double falseEasting,
                      double falseNorthing) {
        super(ellipsoid, center, units, falseEasting, falseNorthing);
    }

    /** */
    public Azimuthal (Ellipsoid ellipsoid, ProjectionParameters parameters)
            throws ParseException {
        super(ellipsoid, parameters);
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

    //-------------------------------------------------------------------------
    // AbstractProjection implementation
    //-------------------------------------------------------------------------

    /**
     * Initialize parameters, and recompute them whenever the ellipsoid or
     * projection center changes.
     */
    protected void computeParameters () {
        _sinPhi0 = StrictMath.sin(_phi0);
        _cosPhi0 = StrictMath.cos(_phi0);
        _hemisphereCenter = new Coordinate(_lambda0, _phi0);
        super.computeParameters();
    }
}
