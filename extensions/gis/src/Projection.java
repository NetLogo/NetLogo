//
// Copyright (c) 2000 the National Geographic Society. All rights reserved.
//

package org.myworldgis.projection;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.util.GeometryTransformer;
import javax.measure.converter.UnitConverter;
import javax.measure.quantity.Length;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;


/**
 * Projection interface for My World projection library.
 * <p>
 * <h4>Assumptions:</h4>
 * <ul>
 *   <li>At the center point of the projection, North is in the positive
 *       y direction, East is in the positive x direction </li>
 *   <li>No line segment is longer than 180 degrees (pi radians). If you need
 *       to draw a longer line, break it into several segments of less than
 *       180 degrees each. Furthermore no line segment may be longer than 180 
 *       degrees (pi radians) in longitudinal extent.</li>
 *   <li>Polygons are to be filled using the Non-Zero winding rule. That is,
 *       a given point is inside the polygon iff the number of times a line 
 *       drawn from that point to infinity crosses the polygon in a clockwise
 *       direction minus the number of times the line crosses the point in a
 *       counter-clockwise direction is not zero. In general, this means that
 *       clockwise polygons have positive area, and counter-clockwise polygons
 *       have negative area, in keeping with ESRI's conventions for shapefiles.
 *       </li>
 *   <li>For the cylindrical projections, (e.g. Mercator), your polygons should 
 *       not include or touch the poles. This is because a polygon or polyline 
 *       that includes a pole becomes a non-continuous straight line on the map.
 *       "So what about Antarctica", you say, "after all it's a polygon that is 
 *       draped over the South Pole". Well, if you want to see it in a 
 *       cylindrical projection, you will need to "augment" the vertices to turn 
 *       it into a valid x-y polygon.  You could do this by removing the segment 
 *       which crosses the dateline, and instead add two extra edges down along 
 *       both sides of the dateline to very near the south pole and then connect 
 *       these ends back the other way around the world (not across the 
 *       dateline) with a few extra line segments. This way you've removed the 
 *       polar anomaly from the data set.  On the screen, all you see is a 
 *       sliver artifact down along the dateline.</li>
 *</ul>
 * <p> 
 * The projection library deals internally in radians, so keeping your data
 * in radians will save you from wasting a lot of time converting units.
 * <p> 
 * Implementing classes will be thread safe, as long as you don't call 
 * <code>setUnits</code>, <code>setEllipsoid</code> or <code>setCenter</code>.
 */
public interface Projection extends Cloneable {

    //--------------------------------------------------------------------------
    // Inner classes
    //--------------------------------------------------------------------------
    
    /** */
    public enum LineType { STRAIGHT, RHUMB, GREATCIRCLE };
    
    //--------------------------------------------------------------------------
    // Class variables
    //--------------------------------------------------------------------------
    
    /** */
    public static UnitConverter DEGREES_TO_RADIANS = NonSI.DEGREE_ANGLE.getConverterTo(SI.RADIAN);
    
    /** */
    public static UnitConverter RADIANS_TO_DEGREES = SI.RADIAN.getConverterTo(NonSI.DEGREE_ANGLE);
    
    /** */
    public static final Ellipsoid DEFAULT_ELLIPSOID = Ellipsoid.WGS_84;
    
    /** */
    public static final Coordinate DEFAULT_CENTER = new Coordinate(0, 0);
    
    /** */
    public static final Unit<Length> DEFAULT_UNITS = SI.METRE;
    
    //-------------------------------------------------------------------------
    // Instance methods
    //-------------------------------------------------------------------------
    
    /** */
    public LineType getLineType ();
    
    /** */
    public void setLineType (LineType newLineType);
    
    /**
     * Get the ellipsoid used for projection calculations.
     * @return the current ellipsoid.
     */
    public Ellipsoid getEllipsoid ();
    
    /**
     * Set the ellipsoid used for projection calculations.
     * @param newEllipsoid the new ellipsoid
     */
    public void setEllipsoid (Ellipsoid newEllipsoid);
    
    /**
     * Get the center of this projection.
     * @return the center of this projection.
     */
    public Coordinate getCenter ();
    
    /**
     * Set the center of this projection.
     * @param newCenter the new center of this projection.
     */
    public void setCenter (Coordinate newCenter);
    
    /** */
    public double getCenterEasting ();
    
    /** */
    public double getCenterNorthing ();
    
    /** */
    public boolean isRhumbRectangular ();
    
    /** */
    public GeometryTransformer getForwardTransformer ();
    
    /** */
    public GeometryTransformer getInverseTransformer ();
    
    /** */
    public ProjectionParameters getParameters ();
}
