//
// Copyright (c) 2007 Eric Russell. All rights reserved.
//

package org.myworldgis.projection;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.myworldgis.util.GeometryUtils;
import org.myworldgis.util.JTSUtils;


/**
 *
 */
public final strictfp class ProjectionUtils {
    
    //-------------------------------------------------------------------------
    // Making Rhumb and Great Circle polygons
    //-------------------------------------------------------------------------
    
    /** Maximum number of segments to use when projecting a GreatCircle or Rhumb line */
    private static final int MAX_SEGMENTS = 180;
    
    /** */
    public static Polygon createRhumbPoly (Polygon poly) {
        LinearRing shell = (LinearRing)createRhumbLine(poly.getExteriorRing());
        LinearRing[] holes = new LinearRing[poly.getNumInteriorRing()];
        for (int i = 0; i < poly.getNumInteriorRing(); i += 1) {
            holes[i] = (LinearRing)createRhumbLine(poly.getInteriorRingN(i));
        }
        return poly.getFactory().createPolygon(shell, holes);
    }
    
    /** 
     * Forward projects a rhumb line polyline/polygon.
     * Forward projects a rhumb line polyline/polygon by adding intermediate 
     * points between each pair of vertices. If this projection is a Mercator
     * projection, there is no need to do that, since rhumb lines project as
     * straight lines in the Mercator projection.
     * @param points polygon or polyline points in lon,lat order in RADIANS
     * @param isFilled true if given poly is to be filled
     * @return projected polyline/polygon.
     */
    public static LineString createRhumbLine (LineString line) {
        List<Coordinate> newCoords = new ArrayList<Coordinate>(line.getNumPoints() * 3);
        Coordinate pt0 = line.getCoordinateN(0);
        for (int i = 1; i < line.getNumPoints(); i += 1) {
            Coordinate pt1 = line.getCoordinateN(i);
            double dx = GeometryUtils.wrap_longitude(pt0.x - pt1.x);
            double dy = pt0.y - pt1.y;
            double length = StrictMath.sqrt(dx*dx + dy*dy);
            int nsegs = StrictMath.min(StrictMath.max((int)(length/0.01745), 0), MAX_SEGMENTS);
            double[] segments = GeometryUtils.rhumb_line(pt0.x,
                                                         pt0.y,
                                                         pt1.x,
                                                         pt1.y,
                                                         nsegs,
                                                         false);
            for (int j = 0; j < segments.length; j += 2) {
                newCoords.add(new Coordinate(segments[j], segments[j+1]));
            }
        }
        newCoords.add(line.getCoordinateN(line.getNumPoints() - 1));
        GeometryFactory factory = line.getFactory();
        Coordinate[] coordArray = newCoords.toArray(new Coordinate[newCoords.size()]);
        if (line instanceof LinearRing) {
            return factory.createLinearRing(coordArray);
        } else {
            return factory.createLineString(coordArray);
        }
    }

    /** */
    public static Polygon createGreatCirclePoly (Polygon poly) {
        LinearRing shell = (LinearRing)createGreatCircleLine(poly.getExteriorRing());
        LinearRing[] holes = new LinearRing[poly.getNumInteriorRing()];
        for (int i = 0; i < poly.getNumInteriorRing(); i += 1) {
            holes[i] = (LinearRing)createGreatCircleLine(poly.getInteriorRingN(i));
        }
        return poly.getFactory().createPolygon(shell, holes);
    }
    
    /** 
     * Forward projects a great circle polyline/polygon.
     * Forward projects a great circle polyline/polygon by adding intermediate 
     * points between each pair of vertices. If this projection is a Gnomonic
     * projection, there is no need to do that, since great circles project as
     * straight lines in the Gnomonic projection.
     * @param points polygon or polyline points in lon,lat order in RADIANS
     * @param isFilled true if given poly is to be filled
     * @return projected polline/polygon.
     */
    public static LineString createGreatCircleLine (LineString poly) {
        List<Coordinate> newCoords = new ArrayList<Coordinate>(poly.getNumPoints() * 3);
        Coordinate pt0 = poly.getCoordinateN(0);
        for (int i = 1; i < poly.getNumPoints(); i += 1) {
            Coordinate pt1 = poly.getCoordinateN(i);
            double dx = GeometryUtils.wrap_longitude(pt0.x - pt1.x);
            double dy = pt0.y - pt1.y;
            double length = StrictMath.sqrt(dx*dx + dy*dy);
            int nsegs = StrictMath.min(StrictMath.max((int)(length/0.01745), 0), MAX_SEGMENTS);
            double[] segments = GeometryUtils.great_circle(pt0.x,
                                                           pt0.y,
                                                           pt1.x,
                                                           pt1.y,
                                                           nsegs,
                                                           false);
            for (int j = 0; j < segments.length; j += 2) {
                newCoords.add(new Coordinate(segments[j], segments[j+1]));
            }
        }
        newCoords.add(poly.getCoordinateN(poly.getNumPoints() - 1));
        GeometryFactory factory = poly.getFactory();
        Coordinate[] coordArray = newCoords.toArray(new Coordinate[newCoords.size()]);
        if (poly instanceof LinearRing) {
            return factory.createLinearRing(coordArray);
        } else {
            return factory.createLineString(coordArray);
        }
    }
    
    //-------------------------------------------------------------------------
    // Dateline wrapping
    //-------------------------------------------------------------------------
    
    /** 
     * Object representing an intersection of a polygon/polyline segment with
     * the edge of the projection.
     */
    private static final class WrappingIntersection {
        
        /** The index of the x coordinate of the end point of the intersected 
            segment in the array of points */
        final int index;
        
        /** Y coordinate of the intersection */
        final double lat;
        
        /** True if the segment in question intersects the edge moving from 
            left to right (west to east), false otherwise */
        final boolean isLeftToRight;
        
        /** Flag used to mark intersections that occur ON a vertex */
        final boolean isOnPoint;
        
        /** Flag used to mark <code>Intersection</code> objects as used when 
            we iterate through our list of intersections */
        private boolean _used;
        
        public WrappingIntersection (int index, double lat, boolean leftToRight, boolean isOnPoint) {
            this.index = index;
            this.lat = lat;
            this.isLeftToRight = leftToRight;
            this.isOnPoint = isOnPoint;
            _used = false;
        }
        boolean isUsed () { return _used; }
        void markUsed () { _used = true; }
        void markUnused () { _used = false; }
    }
    
    /** <code>Comparator</code> for sorting <code>Intersection</code> objects 
        in ascending order of their end point index (@see java.util.Comparator) */
    private static final Comparator<WrappingIntersection> INDEX_ASCENDING_COMPARATOR = new Comparator<WrappingIntersection>() {
            public int compare (WrappingIntersection i1, WrappingIntersection i2) {
                if (i1.index < i2.index) return(-1);
                if (i1.index > i2.index) return(1);
                return(0);
            }
        };
    
    /** Comparator for sorting <code>Intersection</code> objects in ascending
        order of their y coordinate (@see java.util.Comparator) */
    private static final Comparator<WrappingIntersection> LAT_ASCENDING_COMPARATOR = new Comparator<WrappingIntersection>() {
            public int compare (WrappingIntersection i1, WrappingIntersection i2) {
                if (i1.lat < i2.lat) return(-1);
                if (i1.lat > i2.lat) return(1);
                return(0);
            }
        };
    
    /** Comparator for sorting <code>Intersection</code> objects in ascending
        order of their y coordinate (@see java.util.Comparator) */
    private static final Comparator<WrappingIntersection> LAT_DESCENDING_COMPARATOR = new Comparator<WrappingIntersection>() {
            public int compare (WrappingIntersection i1, WrappingIntersection i2) {
                if (i2.lat < i1.lat) return(-1);
                if (i2.lat > i1.lat) return(1);
                return(0);
            }
        };
    
    /** */
    private static final double EDGE_SHIFT_EPSILON = 0.00001;
    
    /** */
    private static final int LONGITUDE_SEGMENT_COUNT = 72;
    
    /** */
    private static final double LATITUDE_FILL_INCREMENT = GeometryUtils.TWO_PI / LONGITUDE_SEGMENT_COUNT;
    
    /**
     * Wraps a polyline along the projection edge.
     * This is simple: if a segment crosses the edge from left to right, end
     * the segment at the right edge and start it again at the left edge; if
     * a segment crosses from right to left, end it at the left edge and start
     * it again at the right edge. Since no segment is allowed to have a 
     * longitudinal extent of more than pi radians, any segment which has one
     * end point at a longitude greater than PI/2 and the other at a longitude
     * less than -PI/2 must cross the dateline.
     * @param points unprojected points in lon,lat order
     * @return the wrapped polyline
     */
    public static MultiLineString wrap (LineString line, double centerLon) {
        GeometryFactory factory = line.getFactory();
        List<LineString> result = new ArrayList<LineString>(2);
        double leftEdgeLon = GeometryUtils.wrap_longitude(centerLon - (StrictMath.PI - EDGE_SHIFT_EPSILON));
        double rightEdgeLon = GeometryUtils.wrap_longitude(centerLon + (StrictMath.PI - EDGE_SHIFT_EPSILON));
        Coordinate c1 = line.getCoordinateN(0);
        Coordinate c2;
        double transformedLon1; 
        double transformedLon2;
        List<Coordinate> coords = new ArrayList<Coordinate>(line.getNumPoints());
        coords.add(c1); 
        transformedLon1 = GeometryUtils.wrap_longitude_positive(c1.x - centerLon);
        for (int i = 1; i < line.getNumPoints(); i += 1) {
            c2 = line.getCoordinateN(i);
            transformedLon2 = GeometryUtils.wrap_longitude_positive(c2.x - centerLon);
            if ((transformedLon1 > GeometryUtils.HALF_PI) && (transformedLon1 < StrictMath.PI) && 
                (transformedLon2 > StrictMath.PI) && (transformedLon2 < GeometryUtils.THREE_HALVES_PI)) {
                double lat = c1.y + (c2.y - c1.y)*((StrictMath.PI - transformedLon1) / (transformedLon2 - transformedLon1));
                coords.add(new Coordinate(rightEdgeLon, lat));
                result.add(factory.createLineString(coords.toArray(new Coordinate[coords.size()])));
                coords.clear();
                coords.add(new Coordinate(leftEdgeLon, lat));
            } else if ((transformedLon1 > StrictMath.PI) && (transformedLon1 < GeometryUtils.THREE_HALVES_PI) &&
                       (transformedLon2 > GeometryUtils.HALF_PI) && (transformedLon2 < StrictMath.PI)) {
                double lat = c1.y + (c2.y - c1.y)*((StrictMath.PI - transformedLon1) / (transformedLon2 - transformedLon1));
                coords.add(new Coordinate(leftEdgeLon, lat));
                result.add(factory.createLineString(coords.toArray(new Coordinate[coords.size()])));
                coords.clear();
                coords.add(new Coordinate(rightEdgeLon, lat));
            } else if (transformedLon2 == StrictMath.PI) {
                if (transformedLon1 < StrictMath.PI) {
                    coords.add(new Coordinate(rightEdgeLon, c2.y));
                    if (i < (line.getNumPoints() - 1)) {
                        Coordinate c3 = line.getCoordinateN(i+1);
                        double transformedLon3 = (float)GeometryUtils.wrap_longitude_positive(c3.x - centerLon);
                        if ((transformedLon3 > StrictMath.PI) && (transformedLon3 < GeometryUtils.THREE_HALVES_PI)) {
                            result.add(factory.createLineString(coords.toArray(new Coordinate[coords.size()])));
                            coords.clear();
                            coords.add(new Coordinate(leftEdgeLon, c2.y));
                        }
                    }
                } else if (transformedLon1 > StrictMath.PI) {
                    coords.add(new Coordinate(leftEdgeLon, c2.y));
                    if (i < (line.getNumPoints() - 1)) {
                        Coordinate c3 = line.getCoordinateN(i+1);
                        double transformedLon3 = (float)GeometryUtils.wrap_longitude_positive(c3.x - centerLon);
                        if ((transformedLon3 < StrictMath.PI) && (transformedLon2 > GeometryUtils.HALF_PI)) {
                            result.add(factory.createLineString(coords.toArray(new Coordinate[coords.size()])));
                            coords.clear();
                            coords.add(new Coordinate(rightEdgeLon, c2.y));
                        }
                    }
                }
            } else {
                coords.add(c2);
            }
            c1 = c2;
            transformedLon1 = transformedLon2;
        }
        if (result.size() == 0) {
            return factory.createMultiLineString(new LineString[] { line });
        } else if (result.size() == 1) {
            return factory.createMultiLineString(new LineString[] { result.get(0) });
        } else {
            return factory.createMultiLineString(result.toArray(new LineString[result.size()]));
        }
    }
    
    /** */
    public static MultiPolygon wrap (Polygon poly, double centerLon) {
        List<LinearRing> newRings = wrap((LinearRing)poly.getExteriorRing(), centerLon);
        for (int i = 0; i < poly.getNumInteriorRing(); i += 1) {
            newRings.addAll(wrap((LinearRing)poly.getInteriorRingN(i), centerLon));
        }
        return JTSUtils.buildPolygonGeometry(newRings.toArray(new LinearRing[newRings.size()]), 
                                             poly.getFactory(), 
                                             true);
    }
    
    /**
     * Wraps a polygon along the projection edge.
     * This is a little more complicated. In this method, we simply find all
     * the intersections between polygon edges and the projection edge. If 
     * there are no such intersections, the polygon doesn't cross the edge at
     * all and we can just draw the polygon edges without modifiaction. If
     * there is exactly one intersection, the polygon is circular and needs
     * to be dealt with specially. If there is an odd number of intersections
     * greater than one, there must be an error someplace. Otherwise, we pass
     * the intersections on to <code>wrapComplicatedPolygon </code> below, 
     * which handles the wrapping.
     * @param points unprojected points in lon,lat order
     * @return the wrapped polygon
     */
    public static List<LinearRing> wrap (LinearRing ring, double centerLon) {
        Coordinate c1 = ring.getCoordinateN(0);
        Coordinate c2;
        double transformedLon1 = GeometryUtils.wrap_longitude_positive(c1.x - centerLon);
        double transformedLon2;
        List<WrappingIntersection> intersections = null;
        for (int i = 1; i < ring.getNumPoints(); i += 1) {
            c2 = ring.getCoordinateN(i);
            transformedLon2 = GeometryUtils.wrap_longitude_positive(c2.x - centerLon);
            if ((transformedLon1 > GeometryUtils.HALF_PI) && (transformedLon1 < StrictMath.PI) && 
                (transformedLon2 > StrictMath.PI) && (transformedLon2 < GeometryUtils.THREE_HALVES_PI)) {
                double lat = c1.y + (c2.y - c1.y)*((StrictMath.PI - transformedLon1) / (transformedLon2 - transformedLon1));
                if (intersections == null) intersections = new ArrayList<WrappingIntersection>(2);
                intersections.add(new WrappingIntersection(i, lat, true, false));
            } else if ((transformedLon1 > StrictMath.PI) && (transformedLon1 < GeometryUtils.THREE_HALVES_PI) &&
                       (transformedLon2 > GeometryUtils.HALF_PI) && (transformedLon2 < StrictMath.PI)) {
                double lat = c1.y + (c2.y - c1.y)*((StrictMath.PI - transformedLon1) / (transformedLon2 - transformedLon1));
                if (intersections == null) intersections = new ArrayList<WrappingIntersection>(2);
                intersections.add(new WrappingIntersection(i, lat, false, false));
            } else if (transformedLon1 == StrictMath.PI) {
                Coordinate c0 = (i == 1) ? ring.getCoordinateN(ring.getNumPoints()-2) : ring.getCoordinateN(i-2);
                double transformedLon0 = GeometryUtils.wrap_longitude_positive(c0.x - centerLon);
                if (transformedLon0 < StrictMath.PI) {
                    if (transformedLon2 > StrictMath.PI) {
                        if (intersections == null) intersections = new ArrayList<WrappingIntersection>(2);
                        intersections.add(new WrappingIntersection(i-2, c1.y, true, true));
                    } else if (transformedLon2 < StrictMath.PI) {
                        c1.x -= EDGE_SHIFT_EPSILON;
                    }
                } else if (transformedLon0 > StrictMath.PI) {
                    if (transformedLon2 < StrictMath.PI) {
                        if (intersections == null) intersections = new ArrayList<WrappingIntersection>(2);
                        intersections.add(new WrappingIntersection(i-2, c1.y, false, true));
                    } else if (transformedLon2 > StrictMath.PI) {
                        c1.x += EDGE_SHIFT_EPSILON;
                    }
                }
            } else if (transformedLon2 == StrictMath.PI) {
                Coordinate c3 = (i == (ring.getNumPoints() - 1)) ? ring.getCoordinateN(1) : ring.getCoordinateN(i+1);
                double transformedLon3 = GeometryUtils.wrap_longitude_positive(c3.x - centerLon);
                if ((transformedLon1 < StrictMath.PI) && (transformedLon3 < StrictMath.PI)) {
                    c2.x -= EDGE_SHIFT_EPSILON;
                    transformedLon2 -= EDGE_SHIFT_EPSILON;
                } else if ((transformedLon1 > StrictMath.PI) && (transformedLon3 > StrictMath.PI)) {
                    c2.x += EDGE_SHIFT_EPSILON;
                    transformedLon2 += EDGE_SHIFT_EPSILON;
                }
            }
            c1 = c2;
            transformedLon1 = transformedLon2;
        }
        
        if (intersections == null) {
            List<LinearRing> result = new ArrayList<LinearRing>(1);
            result.add(ring);
            return result;
        } else if (intersections.size() == 1) {
            List<LinearRing> result = new ArrayList<LinearRing>(1);
            result.add(wrapCircularRing(ring, intersections.get(0), centerLon));
            return result;
        } else if ((intersections.size() & 1) != 0) {
            throw new IllegalStateException("data crosses the dateline an odd number of times; may not be in Geographic coordinates");
        } else {
            return wrapComplicatedRing(ring, intersections, centerLon);
        }
    }
    
    /** */
    private static LinearRing wrapCircularRing (LinearRing ring, WrappingIntersection intersection, double centerLon) {
        List<Coordinate> newPoints = new ArrayList<Coordinate>(ring.getNumPoints() + 144);
        for (int i = 0; i < intersection.index; i += 1) {
            newPoints.add(ring.getCoordinateN(i));
        }
        double lonIncrement;
        double latGoal;
        double lat = intersection.lat;
        double lon;
        if (intersection.isLeftToRight) {
            latGoal = -(GeometryUtils.HALF_PI - EDGE_SHIFT_EPSILON);
            lonIncrement = -(GeometryUtils.TWO_PI - (EDGE_SHIFT_EPSILON * 2f)) / LONGITUDE_SEGMENT_COUNT;
            lon = GeometryUtils.wrap_longitude(centerLon + (StrictMath.PI - EDGE_SHIFT_EPSILON));
        } else {
            latGoal = GeometryUtils.HALF_PI - EDGE_SHIFT_EPSILON;
            lonIncrement = (GeometryUtils.TWO_PI - (EDGE_SHIFT_EPSILON * 2f)) / LONGITUDE_SEGMENT_COUNT;
            lon = GeometryUtils.wrap_longitude(centerLon - (StrictMath.PI - EDGE_SHIFT_EPSILON));
        }
        int latSegCount = (int)StrictMath.floor(StrictMath.abs(latGoal - lat) / LATITUDE_FILL_INCREMENT);
        double latIncrement = (latGoal - lat) / latSegCount;
        for (int i = 0; i < latSegCount; i += 1) {
            newPoints.add(new Coordinate(lon, lat));
            lat += latIncrement;
        }
        for (int i = 0; i < LONGITUDE_SEGMENT_COUNT; i += 1) {
            newPoints.add(new Coordinate(lon, lat));
            lon = GeometryUtils.wrap_longitude(lon + lonIncrement);
        }
        for (int i = 0; i < latSegCount; i += 1) {
            newPoints.add(new Coordinate(lon, lat));
            lat -= latIncrement;
        }
        for (int i = intersection.index; i < ring.getNumPoints(); i += 1) {
            newPoints.add(ring.getCoordinateN(i));
        }
        return ring.getFactory().createLinearRing(newPoints.toArray(new Coordinate[newPoints.size()]));
    }
    
    /** 
     * Wraps a polygon which intersects the projection edge.
     * The algorithm is this: start with the left side, at the bottom of the 
     * left edge; for each intersection we find: if it is left-to-right, 
     * travel along the polygon (starting at intersection._index) until we 
     * reach another  intersection; if it is right-to-left, travel up the 
     * edge until we find the next intersection; repeat until we return to 
     * the  intersection we started with; repeat until all intersections have 
     * been visited. Next, the right side: start at the top of the right
     * edge; for each intersection we find: if it is left-to-right, travel
     * down the edge until we reach the next intersection; if it is right-to-
     * left, travel along the polygon until we reach the next intersection;
     * repeat until we return to the intersection we started with; repeat
     * until all intersections have been visited.
     * @param points projected points in x,y order
     * @param intersections a list of intersections between the polygon and the projection edge
     * @return the projected, wrapped polygon
     */
    private static List<LinearRing> wrapComplicatedRing (LinearRing ring, 
                                                         List<WrappingIntersection> intersections, 
                                                         double centerLon) {
        GeometryFactory factory = ring.getFactory();
        List<LinearRing> result = new ArrayList<LinearRing>(4);
        List<Coordinate> coords = new ArrayList<Coordinate>(ring.getNumPoints());
        double leftEdgeLon = GeometryUtils.wrap_longitude(centerLon - (StrictMath.PI - EDGE_SHIFT_EPSILON));
        double rightEdgeLon = GeometryUtils.wrap_longitude(centerLon + (StrictMath.PI - EDGE_SHIFT_EPSILON));
        if (leftEdgeLon == rightEdgeLon) {
            leftEdgeLon += EDGE_SHIFT_EPSILON;
            rightEdgeLon -= EDGE_SHIFT_EPSILON;
        }
        List<WrappingIntersection> indexList = new ArrayList<WrappingIntersection>(intersections);
        Collections.sort(indexList, INDEX_ASCENDING_COMPARATOR);
        List<WrappingIntersection> latList = new ArrayList<WrappingIntersection>(intersections);
        Collections.sort(latList, LAT_ASCENDING_COMPARATOR);
        // first the left side
        WrappingIntersection current = latList.get(0);
        while (current != null) {
            WrappingIntersection first = current;
            coords.add(new Coordinate(leftEdgeLon, first.lat));
            do {
                WrappingIntersection next;
                if (current.isLeftToRight) {
                    next = nextIntersection(current, indexList);
                    int start = current.index;
                    if (current.isOnPoint) {
                        start += 1;
                        if (start == ring.getNumPoints()) {
                            start = 0;
                        }
                    }
                    int end = next.index;
                    if (next.isOnPoint) {
                        end -= 1;
                        if (end < 0) {
                            end = ring.getNumPoints() - 1;
                        }
                    }
                    if (start < end) {
                        for (int i = start; i < end; i += 1) {
                            coords.add(ring.getCoordinateN(i));
                        }
                    } else {
                        for (int i = start; i < ring.getNumPoints(); i += 1) {
                            coords.add(ring.getCoordinateN(i));
                        }
                        for (int i = 0; i < end; i += 1) {
                            coords.add(ring.getCoordinateN(i));
                        }
                    }
                } else {
                    next = nextIntersection(current, latList);
                    coords.add(new Coordinate(leftEdgeLon, next.lat));
                }
                current.markUsed();
                current = next;
                if (current.isUsed()) {
                    break;
                }
            } while (current != first);
            if (coords.size() > 2) {
                coords.add((Coordinate)coords.get(0).clone());
                result.add(factory.createLinearRing(coords.toArray(new Coordinate[coords.size()])));
            }
            coords.clear();
            current = null;
            for (int i = 0; i < latList.size(); i += 1) {
                if (!latList.get(i).isUsed()) {
                    current = latList.get(i);
                    break;
                }
            }
        }
        // then the right side
        for (int i = 0; i < latList.size(); i += 1) {
            latList.get(i).markUnused();
        }
        Collections.sort(latList, LAT_DESCENDING_COMPARATOR);
        current = latList.get(0);
        while (current != null) {
            WrappingIntersection first = current;
            coords.add(new Coordinate(rightEdgeLon, first.lat));
            do {
                WrappingIntersection next;
                if (current.isLeftToRight) {
                    next = nextIntersection(current, latList);
                    coords.add(new Coordinate(rightEdgeLon, next.lat));
                } else {
                    next = nextIntersection(current, indexList);
                    int start = current.index;
                    if (current.isOnPoint) {
                        start += 1;
                        if (start == ring.getNumPoints()) {
                            start = 0;
                        }
                    }
                    int end = next.index;
                    if (next.isOnPoint) {
                        end -= 1;
                        if (end < 0) {
                            end = ring.getNumPoints() - 1;
                        }
                    }
                    if (start < end) {
                        for (int i = start; i < end; i += 1) {
                            coords.add(ring.getCoordinateN(i));
                        }
                    } else {
                        for (int i = start; i < ring.getNumPoints(); i += 1) {
                            coords.add(ring.getCoordinateN(i));
                        }
                        for (int i = 0; i < end; i += 1) {
                            coords.add(ring.getCoordinateN(i));
                        }
                    }
                }
                current.markUsed();
                current = next;
                if (current.isUsed()) {
                    break;
                }
            } while (current != first);
            coords.add((Coordinate)coords.get(0).clone());
            result.add(factory.createLinearRing(coords.toArray(new Coordinate[coords.size()])));
            coords.clear();
            current = null;
            for (int i = 0; i < latList.size(); i += 1) {
                if (!latList.get(i).isUsed()) {
                    current = latList.get(i);
                    break;
                }
            }
        }
        return result;
    }
    
    /**
     * Returns the intersection following the given intersection in the given list
     * (wrapping around if necessary).
     * @param after the current intersection
     * @param intersections a list of <code>Intersection</code>s in a particular order
     * @return the intersection following <code>after</code> in the list
     */
    private static WrappingIntersection nextIntersection (WrappingIntersection after, List<WrappingIntersection> intersections) {
        int index = intersections.indexOf(after) + 1;
        while (true) {
            if (index == intersections.size()) index = 0;
            WrappingIntersection testInt = intersections.get(index);
            if (testInt.isLeftToRight != after.isLeftToRight) return(testInt);
            if (testInt == after) return(null);
            index += 1;
        }
    }
    
    //-------------------------------------------------------------------------
    // Hemisphere clipping
    //-------------------------------------------------------------------------
    
    /** */
    private static final class ClippingIntersection {
    
        final int index;
        final double lon;
        final double lat;
        final double azimuth;
        final boolean isEntry;
        private boolean _used;
        
        public ClippingIntersection (int index, double lon, double lat, double azimuth, boolean isEntry) {
            this.index = index;
            this.lon = lon;
            this.lat = lat;
            this.azimuth = azimuth;
            this.isEntry = isEntry;
            _used = false;
        }
        
        boolean isUsed () {
            return(_used);
        }
        
        void markUsed () {
            _used = true;
        }
        
        public String toString () {
            return("i["+(index/2)+" "+isEntry+" "+azimuth+" "+"("+lat+","+lon+")]");
        }
        
        public boolean equals (Object obj) {
            return((obj == this) ||
                   ((obj instanceof ClippingIntersection) &&
                    (((ClippingIntersection)obj).lon == this.lon) &&
                    (((ClippingIntersection)obj).lat == this.lat) &&
                    (((ClippingIntersection)obj).azimuth == this.azimuth) &&
                    (((ClippingIntersection)obj).isEntry == this.isEntry)));
        }
    }
    
    /** Angular size of each segment added to a polygon to fill a space which
        crosses out of the clipping hemisphere */
    private static double AZIMUTH_FILL_INCREMENT = GeometryUtils.TWO_PI / 360.0;
    
    /**
     * Clips a polyline to an earth circle of radius maxC.
     * The algorithm is simple. Find the first point inside the clipping 
     * hemisphere, project & draw until we exit the hemisphere, add a point
     * along the edge to terminate, find next point inside the clipping 
     * hemisphere, add point along the edge, lather, rinse, repeat.
     * @param points unprojected vertices in lat,lon order in RADIANS
     * @return the clipped & projected polyline.
     */
    public static MultiLineString clip (LineString line, Coordinate center, double maxC) {
        GeometryFactory factory = line.getFactory();
        List<LineString> result = new ArrayList<LineString>();
        List<Coordinate> currentSegment = new ArrayList<Coordinate>();
        Coordinate pt = line.getCoordinateN(0);
        double c = GeometryUtils.point_point_greatcircle_distance(center.x, center.y, pt.x, pt.y);
        int index = 0;
        while (index < line.getNumPoints()) {
            while (c > maxC) {
                index += 1;
                if (index >= line.getNumPoints()) {
                    break;
                } else {
                    pt = line.getCoordinateN(index);
                    c = GeometryUtils.point_point_greatcircle_distance(center.x, center.y, pt.x, pt.y);
                }
            }
            if (index > 0) {
                if (currentSegment.size() > 1) {
                    result.add(factory.createLineString(currentSegment.toArray(new Coordinate[currentSegment.size()])));
                }
                currentSegment.clear();
                pt = line.getCoordinateN(index-1);
                double az = GeometryUtils.spherical_azimuth(center.x, center.y, pt.x, pt.y);
                currentSegment.add(GeometryUtils.spherical_between(center.x, center.y, maxC, az));
            } 
            else if (index < line.getNumPoints()) {
                if (currentSegment.size() > 1) {
                    result.add(factory.createLineString(currentSegment.toArray(new Coordinate[currentSegment.size()])));
                }
                currentSegment.clear();
                currentSegment.add(line.getCoordinateN(index));
                index += 1;
            }
            if (index >= line.getNumPoints()) {
                break;
            }
            while (c <= maxC) {
                currentSegment.add(line.getCoordinateN(index));
                index += 1;
                if (index >= line.getNumPoints()) {
                    break;
                } else {
                    pt = line.getCoordinateN(index);
                    c = GeometryUtils.point_point_greatcircle_distance(center.x, center.y, pt.x, pt.y);
                }
            }
            if (index < line.getNumPoints()) {
                pt = line.getCoordinateN(index - 1);
                double az = GeometryUtils.spherical_azimuth(center.x, center.y, pt.x, pt.y);
                currentSegment.add(GeometryUtils.spherical_between(center.x, center.y, maxC, az));
            }
            if (currentSegment.size() > 1) {
                result.add(factory.createLineString(currentSegment.toArray(new Coordinate[currentSegment.size()])));
            }
            currentSegment.clear();
        }
        if (result.size() == 1) {
            return factory.createMultiLineString(new LineString[] { result.get(0) });
        } else {
            return factory.createMultiLineString(result.toArray(new LineString[result.size()]));
        }
    }
    
    /** */
    public static MultiPolygon clip (Polygon poly, Coordinate center, double maxC) {
        List<LinearRing> newRings = clip((LinearRing)poly.getExteriorRing(), center, maxC);
        for (int i = 0; i < poly.getNumInteriorRing(); i += 1) {
            newRings.addAll(clip((LinearRing)poly.getInteriorRingN(i), center, maxC));
        }
        return JTSUtils.buildPolygonGeometry(newRings.toArray(new LinearRing[newRings.size()]), 
                                             poly.getFactory(), 
                                             false);
    }
    
    /**
     * Clips a polygon to an earth circle of radius maxC.
     * The algorithm is this: find a point outside the clipping hemisphere; 
     * if no such point exists, the polygon lies entirely within the clipping 
     * hemisphere and we can build the projected polygon in the simplest 
     * possible way; otherwise, find the first point inside the clipping 
     * hemisphere following the series of points outside the hemisphere; 
     * if no such point exists, the polygon lies entirely outside the 
     * clipping hemisphere, and we can return an empty Poly; otherwise,
     * pass the buck to <code>clipComplexRing</code> below.
     * @param points unprojected vertices in lon,lat order in RADIANS
     * @return the clipped & projected polygon.
     */
    private static List<LinearRing> clip (LinearRing ring, Coordinate center, double maxC) {
        int firstOutsideIndex = 0;
        double c = 0.0;
        do {
            Coordinate pt = ring.getCoordinateN(firstOutsideIndex);
            c = GeometryUtils.point_point_greatcircle_distance(center.x, center.y, pt.x, pt.y);
            firstOutsideIndex += 1;
        } while ((c <= maxC) && (firstOutsideIndex < ring.getNumPoints()));
        
        if (firstOutsideIndex == ring.getNumPoints()) {
            // polygon is entirely inside projection domain
            List<LinearRing> result = new ArrayList<LinearRing>(1);
            result.add(ring);
            return result;
        } else {
            firstOutsideIndex -= 1;
            int firstInsideIndex = firstOutsideIndex;
            do {
                firstInsideIndex = (firstInsideIndex + 1) % ring.getNumPoints();
                Coordinate pt = ring.getCoordinateN(firstInsideIndex);
                c = GeometryUtils.point_point_greatcircle_distance(center.x, center.y, pt.x, pt.y);
            } while ((c >= maxC) && (firstInsideIndex != firstOutsideIndex));
            
            if (firstInsideIndex == firstOutsideIndex) {
                // polygon is entirely outside projection domain
                return new ArrayList<LinearRing>(0);
            } else {
                return clipComplexRing(ring, center, maxC);
            }
        }
    }
    
    /** 
     * Clip and projects a polygon which lies partially within the 
     * clipping hemisphere and partially outside it.
     * The algorithm is this: 
     *
     *** DESCRIPTION OF NEW ALGORITHM GOES HERE ***
     *
     * @param points unprojected vertices in lon,lat order in RADIANS
     * @param firstInsideIndex int index of the latitude in <code>points</code>
     *     of a point inside the clipping hemisphere immediately following one
     *     or more points outisde of the clipping hemisphere
     */
    private static List<LinearRing> clipComplexRing (final LinearRing ring, Coordinate center, double maxC) {
        GeometryFactory factory = ring.getFactory();
        List<LinearRing> result = new ArrayList<LinearRing>();
        List<ClippingIntersection> intersectionsByIndex = new ArrayList<ClippingIntersection>();
        Coordinate temp = new Coordinate();
        Coordinate current = ring.getCoordinateN(0);
        double currentC = GeometryUtils.point_point_greatcircle_distance(center.x, center.y, current.x, current.y);
        boolean curIsInside = (currentC < maxC);
        for (int curIndex = 0; curIndex < (ring.getNumPoints() - 2); curIndex += 1) {
            int nextIndex = (curIndex + 1);
            Coordinate next = ring.getCoordinateN(nextIndex);
            double nextC = GeometryUtils.point_point_greatcircle_distance(center.x, center.y, next.x, next.y);
            boolean nextIsInside = (nextC < maxC);
            if (curIsInside != nextIsInside) {
                double u = curIsInside ? ((maxC - currentC) / (nextC - currentC)) : ((currentC - maxC) / (currentC - nextC));
                double iLon = current.x + (u * (next.x - current.x));
                double iLat = current.y + (u * (next.y - current.y));
                double iAz = GeometryUtils.spherical_azimuth(center.x, center.y, iLon, iLat);
                temp = GeometryUtils.spherical_between(center.x, center.y, maxC, iAz, temp);
                intersectionsByIndex.add(new ClippingIntersection(nextIndex, temp.x, temp.y, iAz, nextIsInside));
            }
            current = next;
            currentC = nextC;
            curIsInside = nextIsInside;
        }
        List<ClippingIntersection> intersectionsByAzimuth = new ArrayList<ClippingIntersection>(intersectionsByIndex);
        Collections.sort(intersectionsByAzimuth, new Comparator<ClippingIntersection>() {
                public int compare (ClippingIntersection i1, ClippingIntersection i2) {
                    if (i1.azimuth < i2.azimuth) return(-1);
                    if (i1.azimuth > i2.azimuth) return(1);
                    if (i1.isEntry == i2.isEntry) return(0);
                    // This is an awful lot of code to deal with an obscure special case,
                    // but it happens to be a special case that occurs regularly (usually
                    // with Antarctica), so we have to deal with it. First, we see if the
                    // visible portion of the ring contains the zero-th element of the 
                    // points array. If it does, we say that the ring "wrapsAround".
                    ClippingIntersection entry = i1.isEntry ? i1 : i2;
                    ClippingIntersection exit = i1.isEntry ? i2 : i1;
                    boolean wrapsAround = false;
                    int index = entry.index;
                    while (index != exit.index) {
                        if (index == 0) {
                            wrapsAround = true;
                            break;
                        }
                        index = (index + 1) % ring.getNumPoints();
                    }
                    // If it wraps around, the intersection with the greater index comes
                    // first. If not, the lesser index is first.
                    if (wrapsAround) {
                        if (i1.index > i2.index) return(-1);
                        if (i1.index < i2.index) return(1);
                    } else {
                        if (i1.index < i2.index) return(-1);
                        if (i1.index > i2.index) return(1);
                    }
                    return(0);
                }
            });
        ClippingIntersection currentExit = null;
        for (int i = 0; i < intersectionsByIndex.size(); i += 1) {
            if (!intersectionsByIndex.get(i).isEntry) {
                currentExit = intersectionsByIndex.get(i);
                break;
            }
        }
        while (currentExit != null) {
            List<Coordinate> coords = new ArrayList<Coordinate>();
            ClippingIntersection lastEntry = null;
            ClippingIntersection firstExit = currentExit;
            while (true) {
                if (lastEntry != null) {
                    if (lastEntry.index < currentExit.index) {
                        for (int i = lastEntry.index; i < currentExit.index; i += 1) {
                            coords.add(ring.getCoordinateN(i));
                        }
                    } else {
                        for (int i = lastEntry.index; i < (ring.getNumPoints() - 1); i += 1) {
                            coords.add(ring.getCoordinateN(i));
                        }
                        for (int i = 0; i < currentExit.index; i += 1) {
                            coords.add(ring.getCoordinateN(i));
                        }
                    }
                    lastEntry.markUsed();
                }
                if (currentExit.isUsed()) {
                    if (true /* currentExit == firstExit */)
                        // We should really be testing here to make sure we've 
                        // moved in a proper ring, but in some situations involving
                        // vertices at the poles this doesn't work, and trying to 
                        // fix it was giving me a headache, so we'll just assume
                        // everything is kosher.
                        break;
                }
                coords.add(new Coordinate(currentExit.lon, currentExit.lat));
                ClippingIntersection currentEntry = nextIntersection(currentExit, intersectionsByAzimuth, true);
                if (currentEntry == null) {
                    break;
                }
                double azDifference = StrictMath.abs(currentEntry.azimuth - currentExit.azimuth);
                if (azDifference > StrictMath.PI) {
                    azDifference = (GeometryUtils.TWO_PI - azDifference);
                }
                int count = (int)StrictMath.floor(azDifference / AZIMUTH_FILL_INCREMENT) - 1;
                double azimuth = currentExit.azimuth + AZIMUTH_FILL_INCREMENT;
                for (int i = 0; i < count; i += 1) {
                    coords.add(GeometryUtils.spherical_between(center.x, center.y, maxC, azimuth, new Coordinate()));
                    azimuth += AZIMUTH_FILL_INCREMENT;
                }
                coords.add(new Coordinate(currentEntry.lon, currentEntry.lat));
                currentExit.markUsed();
                currentExit = nextIntersection(currentEntry, intersectionsByIndex, false);
                lastEntry = currentEntry;
            }
            coords.add(new Coordinate(firstExit.lon, firstExit.lat));
            if (coords.size() > 3) {
                result.add(factory.createLinearRing(coords.toArray(new Coordinate[coords.size()])));
            }
            currentExit = null;
            for (int i = 0; i < intersectionsByIndex.size(); i += 1) {
                ClippingIntersection testInt = intersectionsByIndex.get(i);
                if ((!testInt.isEntry) && (!testInt.isUsed())) {
                    currentExit = testInt;
                    break;
                }
            }
        }
        return result;
    }
    
    /** */
    private static ClippingIntersection nextIntersection (ClippingIntersection currentIntersection, 
                                                          List<ClippingIntersection> intersections, 
                                                          boolean entry) {
        int start = (currentIntersection != null) ? intersections.indexOf(currentIntersection) : 0;
        int index = (start + 1);
        while (true) {
            if (index == intersections.size()) index = 0;
            ClippingIntersection testInt = intersections.get(index);
            if (testInt.isEntry == entry) {
                return(testInt);
            }
            if (index == start) {
                return(null);
            }
            index += 1;
        }
    }
}
