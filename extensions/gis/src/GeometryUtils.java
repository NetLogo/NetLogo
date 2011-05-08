//
// Copyright (c) 2000 the National Geographic Society. All rights reserved.
//

package org.myworldgis.util;

import com.vividsolutions.jts.geom.Coordinate;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;


/** 
 * Utility functions & constants for geometric operations
 * <p>
 * The spherical_azimuth, spherical_between, earth_circle, and great_circle 
 * methods are based on the methods of the same names in the OpenMap projection 
 * library (see the <a href="http://openmap.bbn.com">OpenMap web page</a> 
 * for details).
 */
public final strictfp class GeometryUtils {

    //--------------------------------------------------------------------------
    // Class variables
    //--------------------------------------------------------------------------
    
    /** 2 * StrictMath.PI */
    public static final double TWO_PI = StrictMath.PI * 2.0;
    
    /** 3 * StrictMath.PI / 2 */
    public static final double THREE_HALVES_PI = (3.0 * StrictMath.PI) / 2.0;
    
    /** (3 * StrictMath.PI) / 4 */
    public static final double THREE_QUARTERS_PI = (3.0 * StrictMath.PI) / 4.0;
    
    /** (5 * StrictMath.PI) / 8 */
    public static final double FIVE_EIGHTHS_PI = (5.0 * StrictMath.PI) / 8.0;
    
    /** StrictMath.PI / 2 */
    public static final double HALF_PI = StrictMath.PI / 2.0;
    
    /** StrictMath.PI / 3 */
    public static final double THIRD_PI = StrictMath.PI / 3.0;
    
    /** StrictMath.PI / 4 */
    public static final double QUARTER_PI = StrictMath.PI / 4.0;
    
    /** StrictMath.PI / 6 */
    public static final double SIXTH_PI = StrictMath.PI / 6.0;
    
    /** StrictMath.PI / 18 */
    public static final double EIGHTEENTH_PI = StrictMath.PI / 18.0;
    
    /** An even-odd winding rule for determining the interior of a path */
    public static final int WIND_EVEN_ODD = PathIterator.WIND_EVEN_ODD;
    
    /** A non-zero winding rule for determining the interior of a path */
    public static final int WIND_NON_ZERO = PathIterator.WIND_NON_ZERO;
    
    /** Default epsilon for geometric computations (1 meter at the equator in radians) */
    public static final double EPSILON = 1.567855942887398E-7;
    
    /** */
    public static final Rectangle2D EMPTY_RECT = new Rectangle2D.Double(0, 0, -1, -1);
    
    /** */
    public static final Rectangle2D INFINITE_RECT = new Rectangle2D.Double(-Double.MAX_VALUE/2.0, -Double.MAX_VALUE/2.0, Double.MAX_VALUE, Double.MAX_VALUE);
    
    //--------------------------------------------------------------------------
    // General distance methods
    //--------------------------------------------------------------------------
    
    /**
     * Euclidean distance between two points on a plane.
     */
    public static double straight_length (double x1, double y1, double x2, double y2) {
        return(point_point_straight_distance(y1, x1, y2, x2));
    }
    
    /**
     * Length of a great circle connecting two points on a sphere. Coordinates are in RADIANS.
     */
    public static double greatcircle_length (double lon1, double lat1, double lon2, double lat2) {
        return(point_point_greatcircle_distance(lon1, lat1, lon2, lat2));
    }
    
    /**
     * Length of a rhumb line connecting two points on a sphere. Coordinates are in RADIANS.
     */
    public static double rhumbline_length (double lon1, double lat1, double lon2, double lat2) {
        return(point_point_rhumbline_distance(lon1, lat1, lon2, lat2));
    }
    
    //--------------------------------------------------------------------------
    // Straight (Euclidean) distance methods
    //--------------------------------------------------------------------------
    
    /** 
     * Compute the squared Euclidean distance between two points on a plane. 
     * @param x1 x coordinate of the first point
     * @param y1 y coordinate of the first point
     * @param x2 x coordinate of the second point
     * @param y2 y coordinate of the second point
     * @return the square of the distance between the two points
     */
    public static double point_point_straight_distance_sq (double x1, double y1, double x2, double y2) {
        return(Point2D.distanceSq(x1, y1, x2, y2));
    }
    
    /** 
     * Compute the Euclidean distance between two points on a plane. 
     * @param x1 x coordinate of the first point
     * @param y1 y coordinate of the first point
     * @param x2 x coordinate of the second point
     * @param y2 y coordinate of the second point
     * @return distance between the two points
     */
    public static double point_point_straight_distance (double x1, double y1, double x2, double y2) {
        return(Point2D.distance(x1, y1, x2, y2));
    }
    
    /** 
     * Compute the square of the minimum distance between a line segment 
     * and a point on the plane.
     * @param x x coordinate of the point
     * @param y y coordinate of the point
     * @param x1 x coordinate of the first endpoint of the segment
     * @param y1 y coordinate of the first endpoint of the segment
     * @param x2 x coordinate of the second endpoint of the segment
     * @param y2 y coordinate of the second endpoint of the segment
     * @return the square of the minimum distance
     */
    public static double point_line_straight_distance_sq (double x, double y, double x1, double y1, double x2, double y2) {
        return(Line2D.ptSegDistSq(x1, y1, x2, y2, x, y));
    }
    
    /** 
     * Compute the minimum distance between a line segment and a point on 
     * the plane. 
     * @param x x coordinate of the point
     * @param y y coordinate of the point
     * @param x1 x coordinate of the first endpoint of the segment
     * @param y1 y coordinate of the first endpoint of the segment
     * @param x2 x coordinate of the second endpoint of the segment
     * @param y2 y coordinate of the second endpoint of the segment
     * @return minimum distance between the point and line segment
     */
    public static double point_line_straight_distance (double x, double y, double x1, double y1, double x2, double y2) {
        return(Line2D.ptSegDist(x1, y1, x2, y2, x, y));
    }
    
    /** 
     * Compute the square of the minimum distance between two line segments on 
     * the plane.
     * @param x1 x coordinate of the first endpoint of the first segment
     * @param y1 y coordinate of the first endpoint of the first segment
     * @param x2 x coordinate of the second endpoint of the first segment
     * @param y2 y coordinate of the second endpoint of the first segment
     * @param x3 x coordinate of the first endpoint of the second segment
     * @param y3 y coordinate of the first endpoint of the second segment
     * @param x5 x coordinate of the second endpoint of the second segment
     * @param y4 y coordinate of the second endpoint of the second segment
     * @return the square of the minimum distance
     */
    public static double line_line_straight_distance_sq (double x1, double y1, double x2, double y2, double x3, double y3, double x4, double y4) {
        if (intersects(x1, y1, x2, y2, x3, y3, x4, y4)) {
            return(0.0f);
        } else {
            double dist_1_sq = point_line_straight_distance_sq(x1, y1, x3, y3, x4, y4);
            double dist_2_sq = point_line_straight_distance_sq(x2, y2, x3, y3, x4, y4);
            double dist_3_sq = point_line_straight_distance_sq(x3, y3, x1, y1, x2, y2);
            double dist_4_sq = point_line_straight_distance_sq(x4, y4, x1, y1, x2, y2);
            return(StrictMath.min(StrictMath.min(dist_1_sq, dist_2_sq), StrictMath.min(dist_3_sq, dist_4_sq)));
        }
    }
    
    /** 
     * Compute the minimum distance between two line segments on the plane.
     * @param x1 x coordinate of the first endpoint of the first segment
     * @param y1 y coordinate of the first endpoint of the first segment
     * @param x2 x coordinate of the second endpoint of the first segment
     * @param y2 y coordinate of the second endpoint of the first segment
     * @param x3 x coordinate of the first endpoint of the second segment
     * @param y3 y coordinate of the first endpoint of the second segment
     * @param x5 x coordinate of the second endpoint of the second segment
     * @param y4 y coordinate of the second endpoint of the second segment
     * @return the minimum distance
     */
    public static double line_line_straight_distance (double x1, double y1, double x2, double y2, double x3, double y3, double x4, double y4) {
        return(StrictMath.sqrt(line_line_straight_distance_sq(x1, y1, x2, y2, x3, y3, x4, y4)));
    }
    
    /**
     * Compute the square of the minimum distance between a point and a 
     * polygon on the plane.
     * @param x the x coordinate of the point
     * @param y the y coordinate of the point
     * @param poly points of the polygon, in (x, y) order
     * @return the square of the minimum distance
     */
    public static double point_poly_straight_distance_sq (double x, double y, double[] poly) {
        double distance_sq, min_distance_sq = Double.MAX_VALUE;
        for (int i = 2; i < poly.length; i += 2) {
            distance_sq = point_line_straight_distance_sq(x, y, poly[i - 2], poly[i - 1], poly[i], poly[i + 1]);
            if (distance_sq < min_distance_sq) {
                min_distance_sq = distance_sq;
            }
        }
        return(min_distance_sq);
    }
    
    /**
     * Compute the minimum distance between a point and a polygon on the plane.
     * @param x the x coordinate of the point
     * @param y the y coordinate of the point
     * @param poly points of the polygon, in (x, y) order
     * @return the minimum distance
     */
    public static double point_poly_straight_distance (double x, double y, double[] poly) {
        return(StrictMath.sqrt(point_poly_straight_distance_sq(x, y, poly)));
    }
    
    /**
     * Compute the square of the minimum distance between a line segment and a 
     * polygon on the plane.
     * @param x1 the x coordinate of the first endpoint of the segment
     * @param y1 the y coordinate of the first endpoint of the segment
     * @param x2 the x coordinate of the second endpoint of the segment
     * @param y2 the y coordinate of the second endpoint of the segment
     * @param poly points of the polygon, in (x, y) order
     * @return the square of the minimum distance
     */
    public static double line_poly_straight_distance_sq (double x1, double y1, double x2, double y2, double[] poly) {
        double distance_sq, min_distance_sq = Double.MAX_VALUE;
        double x3 = poly[0], y3 = poly[1], x4, y4;
        for (int i = 2; i < poly.length; i += 2) {
            x4 = poly[i];
            y4 = poly[i+1];
            distance_sq = line_line_straight_distance_sq(x1, y1, x2, y2, x3, y3, x4, y4);
            if (distance_sq < min_distance_sq) min_distance_sq = distance_sq;
            x3 = x4;
            y3 = y4;
        }
        return(min_distance_sq);
    }
    
    /**
     * Compute the minimum distance between a line segment and a polygon on the 
     * plane.
     * @param x1 the x coordinate of the first endpoint of the segment
     * @param y1 the y coordinate of the first endpoint of the segment
     * @param x2 the x coordinate of the second endpoint of the segment
     * @param y2 the y coordinate of the second endpoint of the segment
     * @param poly points of the polygon, in (x, y) order
     * @return the minimum distance
     */
    public static double line_poly_straight_distance (double x1, double y1, double x2, double y2, double[] poly) {
        return(StrictMath.sqrt(line_poly_straight_distance_sq(x1, y1, x2, y2, poly)));
    }
    
    /**
     * Compute the square of the minimum distance between a two polygons on the
     * plane. Uses a brute-force O(m * n) algorithm, testing each combination 
     * of segments.
     * @param poly1 points of the first polygon, in (x, y) order
     * @param poly2 points of the second polygon, in (x, y) order
     * @return square of the the minimum distance
     */
    public static double poly_poly_straight_distance_sq (double[] poly1, double[] poly2) {
        double distance_sq, min_distance_sq = Double.MAX_VALUE;
        double x1 = poly1[0], y1 = poly1[1], x2, y2;
        for (int i = 2; i < poly1.length; i += 2) {
            x2 = poly1[i];
            y2 = poly1[i+1];
            double x3 = poly2[0], y3 = poly2[1], x4, y4;
            for (int j = 2; j < poly2.length; j += 2) {
                x4 = poly2[j];
                y4 = poly2[j+1];
                distance_sq = line_line_straight_distance_sq(x1, y1, x2, y2, x3, y3, x4, y4);
                if (distance_sq < min_distance_sq) {
                    min_distance_sq = distance_sq;
                }
                x3 = x4;
                y3 = y4;
            }
            x1 = x2;
            y1 = y2;
        }
        return(min_distance_sq);
    }
    
    /**
     * Compute the minimum distance between a two polygons on the plane. Uses a 
     * brute-force O(m * n) algorithm, testing each combination of segments.
     * @param poly1 points of the first polygon, in (x, y) order
     * @param poly2 points of the second polygon, in (x, y) order
     * @return the minimum distance
     */
    public static double poly_poly_straight_distance (double[] poly1, double[] poly2) {
        return(StrictMath.sqrt(poly_poly_straight_distance_sq(poly1, poly2)));
    }
    
    //--------------------------------------------------------------------------
    // Great circle distance methods
    //--------------------------------------------------------------------------
    
    /** 
     * Compute the length of a great circle connecting two points, computed
     * using the <a href="http://www.census.gov/cgi-bin/geo/gisfaq?Q5.1">Haversine Formula</a>.
     * @param lon1 longitude of the first point, in radians
     * @param lat1 latitude of the first point, in radians
     * @param lon2 longitude of the first point, in radians
     * @param lat2 latitude of the first point, in radians
     * @return the angular distance, in radians
     */
    public static double point_point_greatcircle_distance (double lon1, double lat1, double lon2, double lat2) {
        double dlat = StrictMath.sin((lat2 - lat1) / 2.0);
        double dlon = StrictMath.sin((lon2 - lon1) / 2.0);
        double a = (dlat * dlat) + (StrictMath.cos(lat1) * StrictMath.cos(lat2)) * (dlon * dlon);
        return(2.0 * StrictMath.atan2(StrictMath.sqrt(a), StrictMath.sqrt(1.0 - a)));
    }
    
    /**
     * Calculate the minimum distance between a point and the great circle 
     * segment joining two other points, p1 and p2. Formula for perpendicular 
     * distance from a point to a great circle thanks to
     * <A HREF="http://forum.swarthmore.edu/dr.math/problems/bellamy.5.24.00.html">Dr. StrictMath</A>.
     * The test to see whether to calculate perpendicular distance or distance 
     * to an end point is of my own invention. I define two planes whose normal 
     * vector is parallel to (p1 -> p2), one at p1 and one at p2. If the point 
     * lies between those two planes, I calculate the perpendicular distance to 
     * the great circle; if it is on the point1 side, I calculate the distance 
     * to point1, and if it is on the point2 side, I calculate distance to 
     * point2.
     * @param lon longitude of the point, in radians
     * @param lat latitude of the point, in radians
     * @param lon1 longitude of the first endpoint of the segment, in radians
     * @param lat1 latitude of the first endpoint of the segment, in radians
     * @param lon2 longitude of the second endpoint of the segment, in radians
     * @param lat2 latitude of the second endpoint of the segment, in radians
     * @param the minimum distance
     */
    public static double point_line_greatcircle_distance (double lon, double lat, double lon1, double lat1, double lon2, double lat2) {
        // Convert geodetic (lat, lon) coordinates to geocentric (x, y, z)
        double px = StrictMath.cos(lat) * StrictMath.cos(lon);
        double py = StrictMath.cos(lat) * StrictMath.sin(lon);
        double pz = StrictMath.sin(lat);
        double p1x = StrictMath.cos(lat1) * StrictMath.cos(lon1);
        double p1y = StrictMath.cos(lat1) * StrictMath.sin(lon1);
        double p1z = StrictMath.sin(lat1);
        double p2x = StrictMath.cos(lat2) * StrictMath.cos(lon2);
        double p2y = StrictMath.cos(lat2) * StrictMath.sin(lon2);
        double p2z = StrictMath.sin(lat2);
        
        // Calculate normal vector for our two planes
        double n1x = p2x - p1x;
        double n1y = p2y - p1y;
        double n1z = p2z - p1z;
        double n1magnitude = StrictMath.sqrt((n1x * n1x) + (n1y * n1y) + (n1z * n1z));
        n1x /= n1magnitude;
        n1z /= n1magnitude;
        n1y /= n1magnitude;
        
        // caluclate values for v using the vector equation for our two planes, found at
        // http://members.tripod.com/~Paul_Kirby/vector/Vplane.html
        
        // plane1
        double v1 = ((px - p1x) * n1x) + ((py - p1y) * n1y) + ((pz - p1z) * n1z);
        
        // plane2
        double v2 = ((px - p2x) * n1x) + ((py - p2y) * n1y) + ((pz - p2z) * n1z);
        
        if ((v1 < 0) && (v2 < 0)) {
            // Point is on point1 side of the planes
            return(point_point_greatcircle_distance(lon, lat, lon1, lat1));
        } else if ((v1 > 0) && (v2 > 0)) {
            // point is on point2 side of the planes
            return(point_point_greatcircle_distance(lon, lat, lon2, lat2));
        } else {
            // point is between the two planes
            
            // Calculate normal to plane formed by p1, p2, and origin
            double n2x = (p1y * p2z) - (p1z * p2y);
            double n2y = (p1z * p2x) - (p1x * p2z);
            double n2z = (p1x * p2y) - (p1y * p2x);
            
            // angle between (origin -> p) and normal vector is 90 degrees off from
            // angle between p and the great circle defined by p1 and p2.
            double angle = StrictMath.abs(angle_between(px, py, pz, n2x, n2y, n2z) - HALF_PI);
            
            // Now we have to know if p lies in the same hemisphere as p1 and p2.
            // If not, we need to subtract angle from pi to get the true distance.
            // Calculate a normal vector from the origin to halfway between p1 and p2
            double n3x = ((p2x - p1x) / 2) + p1x;
            double n3y = ((p2y - p1y) / 2) + p1y;
            double n3z = ((p2z - p1z) / 2) + p1z;
            double n3magnitude = StrictMath.sqrt((n3x * n3x) + (n3y * n3y) + (n3z * n3z));
            n3x /= n3magnitude;
            n3y /= n3magnitude;
            n3z /= n3magnitude;
            
            // caluclate value for p using the vector equation for a plane, as above.
            // if v3 < 0, p lies on opposite side of globe than p1 and p2
            double v3 = (px * n3x) + (py * n3y) + (pz * n3z);
            return((v3 < 0) ? (StrictMath.PI - angle) : angle);
        }
    }
    
    /** 
     * Compute the minimum distance between two great circle segments 
     * @param lon1 the longitude of the first endpoint of the first segment, in radians
     * @param lat1 the latitude of the first endpoint of the first segment, in radians
     * @param lon2 the longitude of the second endpoint of the first segment, in radians
     * @param lat2 the latitude of the second endpoint of the first segment, in radians
     * @param lon3 the longitude of the first endpoint of the second segment, in radians
     * @param lat3 the latitude of the first endpoint of the second segment, in radians
     * @param lon4 the longitude of the second endpoint of the second segment, in radians
     * @param lat4 the latitude of the second endpoint of the second segment, in radians
     * @return the minimum angular distance, in radians
     */
    public static double line_line_greatcircle_distance (double lon1, double lat1, double lon2, double lat2, double lon3, double lat3, double lon4, double lat4) {
        if ((lat1 == lat2) && (lon1 == lon2) && (lat3 == lat4) && (lon3 == lon4)) {
            return(point_point_greatcircle_distance(lon1, lat1, lon3, lat3));
        } else if ((lat1 == lat2) && (lon1 == lon2)) {
            return(point_line_greatcircle_distance(lon1, lat1, lon3, lat3, lon4, lat4));
        } else if ((lat3 == lat4) && (lon3 == lon4)) {
            return(point_line_greatcircle_distance(lon3, lat3, lon1, lat1, lon2, lat2));
        } else {
            double dist_1 = point_line_greatcircle_distance(lon1, lat1, lon3, lat3, lon4, lat4);
            double dist_2 = point_line_greatcircle_distance(lon2, lat2, lon3, lat3, lon4, lat4);
            double dist_3 = point_line_greatcircle_distance(lon3, lat3, lon1, lat1, lon2, lat2);
            double dist_4 = point_line_greatcircle_distance(lon4, lat4, lon1, lat1, lon2, lat2);
            return(StrictMath.min(StrictMath.min(dist_1, dist_2), StrictMath.min(dist_3, dist_4)));
        }
    }
    
    /**
     * Compute the the minimum distance between a point and a great circle polygon.
     * @param lon the longitude of the point, in radians
     * @param lat the latitude of the point, in radians
     * @param poly the points of the polygon in (lon, lat) order, in radians
     * @return the minimum distance
     */
    public static double point_poly_greatcircle_distance (double lon, double lat, double[] poly) {
        double distance, min_distance = Double.MAX_VALUE;
        double lon1 = poly[0], lat1 = poly[1], lon2, lat2;
        for (int i = 2; i < poly.length; i += 2) {
            lon2 = poly[i];
            lat2 = poly[i+1];
            distance = point_line_greatcircle_distance(lon, lat, lon1, lat1, lon2, lat2);
            if (distance < min_distance) min_distance = distance;
            lon1 = lon2;
            lat1 = lat2;
        }
        return(min_distance);
    }
    
    /**
     * Compute the the minimum distance between a great circle segment and a 
     * great circle polygon. 
     * @param lon1 the longitude of the first enpoint of the segment, in radians
     * @param lat1 the latitude of the first enpoint of the segment, in radians
     * @param lon2 the longitude of the second enpoint of the segment, in radians
     * @param lat2 the latitude of the second enpoint of the segment, in radians
     * @param poly the points of the polygon in (lon, lat) order, in radians
     * @return the minimum distance
     */
    public static double line_poly_greatcircle_distance (double lon1, double lat1, double lon2, double lat2, double[] poly) {
        double distance, min_distance = Double.MAX_VALUE;
        double lon3 = poly[0], lat3 = poly[1], lon4, lat4;
        for (int i = 2; i < poly.length; i += 2) {
            lon4 = poly[i];
            lat4 = poly[i+1];
            distance = line_line_greatcircle_distance(lon1, lat1, lon2, lat2, lon3, lat3, lon4, lat4);
            if (distance < min_distance) {
                min_distance = distance;
            }
            lon3 = lon4;
            lat3 = lat4;
        }
        return(min_distance);
    }
    
    /**
     * Compute the the minimum distance between two great circle polygons. 
     * @param poly1 the points of the first polygon in (lon, lat) order
     * @param poly2 the points of the second polygon in (lon, lat) order
     * @return the minimum distance
     */
    public static double poly_poly_greatcircle_distance (double[] poly1, double[] poly2) {
        double distance, min_distance = Double.MAX_VALUE;
        double lon1 = poly1[0], lat1 = poly1[1], lon2, lat2;
        for (int i = 2; i < poly1.length; i += 2) {
            lon2 = poly1[i];
            lat2 = poly1[i+1];
            double lon3 = poly2[0], lat3 = poly2[1], lon4, lat4;
            for (int j = 2; j < poly2.length; j += 2) {
                lon4 = poly2[j];
                lat4 = poly2[j+1];
                distance = line_line_greatcircle_distance(lon1, lat1, lon2, lat2, lon3, lat3, lon4, lat4);
                if (distance < min_distance) {
                    min_distance = distance;
                }
                lon3 = lon4;
                lat3 = lat4;
            }
            lon1 = lon2;
            lat1 = lat2;
        }
        return(min_distance);
    }
    
    //--------------------------------------------------------------------------
    // Rhumbline distance methods
    //--------------------------------------------------------------------------
    
    /** 
     * Compute length of a rhumb line (line of constant azimuth) joining the 
     * two given points on a sphere. 
     * @param lon1 longitude of the first point, in radians
     * @param lat1 latitude of the first point, in radians
     * @param lon2 longitude of the first point, in radians
     * @param lat2 latitude of the first point, in radians
     * @return the length of the rhumbline, in radians
     */
    public static double point_point_rhumbline_distance (double lon1, double lat1, double lon2, double lat2) {
        double dlat = lat2 - lat1;
        double dlon = lon2 - lon1;
        double q;
        if (StrictMath.abs(dlat) < 0.001) {
            q = StrictMath.cos(lat1);
        } else {
            q = dlat / StrictMath.log(StrictMath.tan((lat2 / 2.0) + QUARTER_PI) / StrictMath.tan((lat1 / 2.0) + QUARTER_PI));
        }
        return(StrictMath.sqrt((dlat * dlat) + ((q * q) * (dlon * dlon))));
    }
    
    //--------------------------------------------------------------------------
    // Interior angle methods
    //--------------------------------------------------------------------------
    
    /**
     * Compute the interior angle formed by the great cirlce segments joining 
     * point 1 to point 2 and point 2 to point 3. Formula from Bevis, M. and 
     * Cambareri, G., "Computing the Area of a Spherical Polygon of Arbitrary 
     * Shape", StrictMathematical Geology, 19, 335-346, 1987
     * @param lon1 longitude of the first endpoint, in radians
     * @param lat1 latitude of the first endpoint, in radians
     * @param lon2 longitude of the second, shared endpoint, in radians
     * @param lat2 latitude of the second, shared endpoint, in radians
     * @param lon3 longitude of the third endpoint, in radians
     * @param lat3 latitude of the third endpoint, in radians
     * @return the interior angle, in the range [0, 2pi)
     */
    public static double interior_angle (double lon1, double lat1, double lon2, double lat2, double lon3, double lat3) {
        double cosLat2 = StrictMath.cos(lat2);
        double sinLat2 = StrictMath.sin(lat2);
        double t1 = StrictMath.sin(lon1 - lon2) * StrictMath.cos(lat1);
        double b1 = (StrictMath.sin(lat1) * cosLat2) - (StrictMath.cos(lat1) * sinLat2 * StrictMath.cos(lon1 - lon2));
        double phiPrime1 = StrictMath.atan2(t1, b1);
        double t2 = StrictMath.sin(lon3 - lon2) * StrictMath.cos(lat3);
        double b2 = (StrictMath.sin(lat3) * cosLat2) - (StrictMath.cos(lat3) * sinLat2 * StrictMath.cos(lon3 - lon2));
        double phiPrime2 = StrictMath.atan2(t2, b2);
        double angle = phiPrime1 - phiPrime2;
        return((angle < 0.0) ? (angle + TWO_PI) : angle);
    }
    
    /** 
     * Calculate the angle between two vectors in three dimensions.
     * @param x1 x element of the first vector
     * @param y1 y element of the first vector
     * @param z1 z element of the first vector
     * @param x2 x element of the second vector
     * @param y2 y element of the second vector
     * @param z2 z element of the second vector
     * @return the interior angle, in the range [0, pi)
     */
    public static double angle_between (double x1, double y1, double z1, double x2, double y2, double z2) {
        double magnitude1 = StrictMath.sqrt((x1 * x1) + (y1 * y1) + (z1 * z1));
        double magnitude2 = StrictMath.sqrt((x2 * x2) + (y2 * y2) + (z2 * z2));
        double dotProduct = ((x1 * x2) + (y1 * y2) + (z1 * z2));
        return(StrictMath.acos(dotProduct / (magnitude1 * magnitude2)));
    }
    
    //--------------------------------------------------------------------------
    // North Pole transform methods
    //--------------------------------------------------------------------------
    
    /** 
     * Tranform the given point to a new system where the former north pole lies at
     * the point alpha(latitude),beta(longitude) in the new system.
     * @param phi latitude of the point to be transformed
     * @param lambda longitude of the point to be transformed
     * @param alpha latitude of the north pole in the new coordinate system
     * @param beta longitude of the north pole in the new coordinate system
     * @param lambda0 meridian of longiutde in the new system that passes 
     *     through the origin
     * @param storage an object to store the result in
     * @return the object passed in storage
     */
    public static Coordinate north_pole_transform (double phi, double lambda, double alpha, double beta, double lambda0, Coordinate storage) {
        storage.y = StrictMath.asin((StrictMath.sin(alpha)*StrictMath.sin(phi)) - (StrictMath.cos(alpha)*StrictMath.cos(phi)*StrictMath.cos(lambda - lambda0)));
        storage.x = StrictMath.atan2(StrictMath.cos(phi)*StrictMath.sin(lambda - lambda0), (StrictMath.sin(alpha)*StrictMath.cos(phi)*StrictMath.cos(lambda - lambda0)) + (StrictMath.cos(alpha)*StrictMath.sin(phi))) + beta;
        return storage;
    }
    
    /** */
    public static Coordinate inverse_north_pole (double phiPrime, double lambdaPrime, double alpha, double beta, double lambda0, Coordinate storage) {
        storage.y = StrictMath.asin((StrictMath.sin(alpha)*StrictMath.sin(phiPrime))+(StrictMath.cos(alpha)*StrictMath.cos(phiPrime)*StrictMath.cos(lambdaPrime-beta))); 
        storage.x = StrictMath.atan2(StrictMath.cos(phiPrime)*StrictMath.sin(lambdaPrime-beta), (StrictMath.sin(alpha)*StrictMath.cos(phiPrime)*StrictMath.cos(lambdaPrime - beta))-(StrictMath.cos(alpha)*StrictMath.sin(phiPrime))) + lambda0;
        return storage;
    }
    
    //--------------------------------------------------------------------------
    // Line segment intersection methods
    //--------------------------------------------------------------------------
    
    /**
     * Tests four points to see if a line from the first to the second would
     * intersect a line from the third to the fourth. Stolen from
     * <a href="http://support.microsoft.com/support/kb/articles/Q121/9/60.asp">Microsoft</a>
     * @param x1 the x coordinate of the first endpoint of the first segment
     * @param y1 the y coordinate of the first endpoint of the first segment
     * @param x2 the x coordinate of the second endpoint of the first segment
     * @param y2 the y coordinate of the second endpoint of the first segment
     * @param x3 the x coordinate of the first endpoint of the second segment
     * @param y3 the y coordinate of the first endpoint of the second segment
     * @param x4 the x coordinate of the second endpoint of the second segment
     * @param y4 the y coordinate of the second endpoint of the second segment
     * @return true if the segments intersect, false otherwise
     */    
    public static boolean intersects (double x1, double y1, double x2, double y2, double x3, double y3, double x4, double y4) {
        return(((x1 == x3) && (y1 == y3)) ||
               ((x1 == x4) && (y1 == y4)) ||
               ((x2 == x3) && (y2 == y3)) ||
               ((x2 == x4) && (y2 == y4)) ||
               (((CCW(x1, y1, x2, y2, x3, y3) * CCW(x1, y1, x2, y2, x4, y4)) <= 0) &&
                ((CCW(x3, y3, x4, y4, x1, y1) * CCW(x3, y3, x4, y4, x2, y2)) <= 0)));
    }
    
    /**
     * Test three points to see if, in drawing a line from the first to the 
     * second to the third, you would be moving counter-clockwise (hence CCW). 
     * Stolen from <a href="http://support.microsoft.com/support/kb/articles/Q121/9/60.asp">Microsoft</a>
     * @param x1 the x coordinate of the first point
     * @param y1 the y coordinate of the first point
     * @param x2 the x coordinate of the second point
     * @param y2 the y coordinate of the second point
     * @param x3 the x coordinate of the third point
     * @param y3 the y coordinate of the third point
     * @return 1 if moving counter-clockwise, -1 if moving clockwise.
     */        
    private static int CCW (double x1, double y1, double x2, double y2, double x3, double y3) {
        double dx1 = x2 - x1;
        double dx2 = x3 - x1;
        double dy1 = y2 - y1;
        double dy2 = y3 - y1;
        return(((dx1 * dy2) > (dy1 * dx2)) ? 1 : -1);
    }
    
    //-------------------------------------------------------------------------
    // General mathematical methods
    //-------------------------------------------------------------------------
    
    /** */
    public static double sinh (double z) {
        return((StrictMath.pow(StrictMath.E, z) - StrictMath.pow(StrictMath.E, -z)) / 2.0);
    }
    
    /** */
    public static double asinh (double z) {
        return(StrictMath.log(z + StrictMath.sqrt(1.0 + z*z)));
    }
    
    /** */
    public static double cosh (double z) {
        return((StrictMath.pow(StrictMath.E, z) + StrictMath.pow(StrictMath.E, -z)) / 2.0);
    }
    
    /** */
    public static double acosh (double z) {
        return(StrictMath.log(z + ((z + 1.0) / StrictMath.sqrt((z - 1.0) / (z + 1.0)))));
    }
    
    /** */
    public static double tanh (double z) {
        double eToTheZ = StrictMath.pow(StrictMath.E, z);
        double eToTheMinusZ = StrictMath.pow(StrictMath.E, -z);
        return((eToTheZ - eToTheMinusZ) / (eToTheZ + eToTheMinusZ));
    }
    
    /** */
    public static double atanh (double z) {
        return(StrictMath.log((1.0 + z)*StrictMath.sqrt(1.0 / (1.0 - z*z))));
    }
    
    /**
     * Return sign of number.
     * @param x number whose sign is desired
     * @return sign -1, 1
     */
    public static int sign (double x) {
        return((x < 0.0d) ? -1 : 1);
    }
    
    //-------------------------------------------------------------------------
    // Normalizing latitude and longitude values
    //-------------------------------------------------------------------------
    
    /** 
     * Ensure value is in the valid range for latitude ([-pi, pi])
     * @param lat the latitude
     * @return normalized latitude
     */
    public static double clamp_latitude (double lat) {
        if (lat > StrictMath.PI) {
            return StrictMath.PI;
        } else if (lat < -StrictMath.PI) {
            return -StrictMath.PI;
        } else {
            return lat;
        }
    }
    
    /**
     * Wrap longitude to range [-pi, pi] by adding or subtracting 2pi.
     * @param lon longitude in RADIANS
     * @return wrapped longitude in RADIANS
     */
    public static double wrap_longitude (double lon) {
        if ((lon < -TWO_PI*10) || (lon > TWO_PI*10)) {
            return(Double.NaN);
        }
        while (lon > StrictMath.PI) { lon -= TWO_PI; }
        while (lon < -StrictMath.PI) { lon += TWO_PI; }
        return lon;
    }
    
    /**
     * Wrap longitude to range [0, 2pi] by adding or subtracting 2pi.
     * @param lon longitude in RADIANS
     * @return wrapped longitude in RADIANS
     */
    public static double wrap_longitude_positive (double lon) {
        if ((lon < -TWO_PI*10) || (lon > TWO_PI*10)) {
            return(Double.NaN);
        }
        while (lon < 0.0) { lon += TWO_PI; }
        while (lon > TWO_PI) { lon -= TWO_PI;}
        return lon;
    }
    
    /**
     * Compute the width of the span of longitude from left to right
     * (i.e., west to east), accounting for dateline-crossing.
     * @param left the longitude at the left side of the span, in RADIANS
     * @param right the longitude at the right side of the span, in RADIANS
     * @return the width of the span, in RADIANS
     */
    public static double longitude_span_width (double left, double right) {
        return((left < right) ? (right - left) : ((StrictMath.PI - right) + (left + StrictMath.PI)));
    }
    
    //-------------------------------------------------------------------------
    // Azimuth methods
    //-------------------------------------------------------------------------
    
    /**
     * Calculate spherical azimuth between two points.
     * <p>
     * Computes the azimuth `Az' east of north from phi1, lambda0
     * bearing toward phi and lambda. (5-4b).  (-PI &lt;= Az &lt;= PI).
     * @param phi1 latitude in radians of start point
     * @param lambda0 longitude in radians of start point
     * @param phi latitude in radians of end point
     * @param lambda longitude in radians of end point
     * @return azimuth east of north `Az'
     */
    public static double spherical_azimuth (double lambda0, 
                                            double phi1, 
                                            double lambda, 
                                            double phi) {
        double ldiff = lambda - lambda0;
        double cosphi = StrictMath.cos(phi);
        return(StrictMath.atan2(cosphi*StrictMath.sin(ldiff), StrictMath.cos(phi1)*StrictMath.sin(phi) - StrictMath.sin(phi1)*cosphi*StrictMath.cos(ldiff)));
    }
    
    /**
     * Calculate point at azimuth and distance from another point.
     * <p>
     * Returns a Coordinate at arc distance `c' in direction `Az'
     * from start point.
     * @param phi1 latitude in radians of start point
     * @param lambda0 longitude in radians of start point
     * @param c arc radius in radians (0 &lt; c &lt;= PI)
     * @param Az azimuth (direction) east of north (-PI &lt;= Az &lt; PI)
     * @return the computed point
     */
    public static Coordinate spherical_between (double lambda0, 
                                                double phi1, 
                                                double c, 
                                                double Az) {
        return spherical_between(lambda0, phi1, c, Az, new Coordinate());
    }
    
    /**
     * Calculate point at azimuth and distance from another point.
     * <p>
     * Returns a Coordinate at arc distance `c' in direction `Az'
     * from start point.
     * @param phi1 latitude in radians of start point
     * @param lambda0 longitude in radians of start point
     * @param c arc radius in radians (0 &lt; c &lt;= PI)
     * @param Az azimuth (direction) east of north (-PI &lt;= Az &lt; PI)
     * @return the computed point
     */
    public static Coordinate spherical_between (double lambda0, 
                                                double phi1, 
                                                double c, 
                                                double Az,
                                                Coordinate storage) {
        double cosphi1 = StrictMath.cos(phi1);
        double sinphi1 = StrictMath.sin(phi1);
        double cosAz = StrictMath.cos(Az);
        double sinAz = StrictMath.sin(Az);
        double sinc = StrictMath.sin(c);
        double cosc = StrictMath.cos(c);
        storage.y = StrictMath.asin(sinphi1*cosc + cosphi1*sinc*cosAz);
        storage.x = StrictMath.atan2(sinc*sinAz, cosphi1*cosc - sinphi1*sinc*cosAz) + lambda0;
        return storage;
    }
    
    //-------------------------------------------------------------------------
    // Line generation methods
    //-------------------------------------------------------------------------
    
    /**
     * Calculate earth circle in the sphere.
     * <p>
     * Returns n double lon,lat pairs at arc distance c from point at
     * phi1,lambda0.
     * @param lambda0 longitude in radians of center point
     * @param phi1 latitude in radians of center point
     * @param c arc radius in radians (0 &lt; c &lt; PI)
     * @param n number of points along circle edge to calculate
     * @return radian lon,lat pairs along earth circle
     */
    public static Coordinate[] earth_circle (double lambda0, double phi1, double c, int n) {
        double Az, cosAz, sinAz;
        double cosphi1 = StrictMath.cos(phi1);
        double sinphi1 = StrictMath.sin(phi1);
        double sinc = StrictMath.sin(c);
        double cosc = StrictMath.cos(c);
        Coordinate[] result = new Coordinate[n];
        double inc = TWO_PI/(n-1);
        Az = -StrictMath.PI;
        // generate the points in clockwise order (conforming to internal standard)
        for (int i = 0; i < n; i += 1) {
            cosAz = StrictMath.cos(Az);
            sinAz = StrictMath.sin(Az);
            result[i] = new Coordinate((float)wrap_longitude(StrictMath.atan2(sinc*sinAz, cosphi1*cosc - sinphi1*sinc*cosAz) + lambda0),
                                       (float)StrictMath.asin(sinphi1*cosc + cosphi1*sinc*cosAz));
            Az += inc;
        }
        return result;
    }
    
    /**
     * Generate points along a great circle between two points on the sphere.
     * <p>
     * Folds all computation (distance, azimuth, points between) into
     * one function for optimization. returns npts pairs of lon,lat
     * on great circle between geodetic points.
     * @param lambda0 longitude in radians of start point
     * @param phi1 latitude in radians of start point
     * @param lambda longitude in radians of end point
     * @param phi latitude in radians of end point
     * @param nsegs number of segments
     * @param include_last include end point as last point in returned points
     * @return nsegs+1 radian lon,lat pairs
     */
    public static double[] great_circle (double lambda0, 
                                         double phi1, 
                                         double lambda,
                                         double phi, 
                                         int nsegs, 
                                         boolean include_last) {
        // calculate a bunch of stuff for later use
        double cosphi = StrictMath.cos(phi);
        double cosphi1 = StrictMath.cos(phi1);
        double sinphi1 = StrictMath.sin(phi1);
        double ldiff = lambda - lambda0;
        double p2diff = StrictMath.sin(((phi-phi1)/2));
        double l2diff = StrictMath.sin((ldiff)/2);
        // calculate spherical distance
        double c = 2.0f * StrictMath.asin(StrictMath.sqrt(p2diff*p2diff + cosphi1*cosphi*l2diff*l2diff));
        // calculate spherical azimuth
        double Az = StrictMath.atan2(cosphi*StrictMath.sin(ldiff), cosphi1*StrictMath.sin(phi) - sinphi1*cosphi*StrictMath.cos(ldiff));
        double cosAz = StrictMath.cos(Az);
        double sinAz = StrictMath.sin(Az);
        // calculate distance increment
        double increment = c / (include_last ? nsegs : (nsegs + 1));
        double pointC = increment;
        // generate the great circle line
        double[] result = new double[(nsegs + 1) * 2];
        result[0] = lambda0;
        result[1] = phi1;
        for (int i = 2; i < result.length; i += 2) {
            // partial constants
            double sinc = StrictMath.sin(pointC);
            double cosc = StrictMath.cos(pointC);
            // generate new point
            result[i] = (StrictMath.atan2(sinc*sinAz, cosphi1*cosc - sinphi1*sinc*cosAz) + lambda0);
            result[i+1] = StrictMath.asin(sinphi1*cosc + cosphi1*sinc*cosAz);
            // increment distance
            pointC += increment;
        }
        return(result);
    }
    
    /**
     * Generate points along a rhumb line between two points on the sphere.
     * <p>
     * Formula from Snyder, John P. (1987). "Map Projections -- 
     * A Working Manual". US Geological Survey Professional 
     * Paper 1395, US Government Printing Office, Washington, 
     * DC. pp. 46-47
     * @param phi1 latitude in radians of start point
     * @param lambda0 longitude in radians of start point
     * @param phi latitude in radians of end point
     * @param lambda longitude in radians of end point
     * @param nsegs number of segments
     * @param include_last include end point as last point in returned points
     * @return nsegs+1 radian lon,lat pairs
     */
    public static double[] rhumb_line (double lambda1,
                                       double phi1,
                                       double lambda2,
                                       double phi2,
                                       int nsegs,
                                       boolean include_last) {
        double[] result = new double[(nsegs + 1) * 2];
        result[0] = lambda1;
        result[1] = phi1;
        if (((lambda1 < -HALF_PI) && (lambda2 > HALF_PI)) ||
            ((lambda1 > HALF_PI) && (lambda2 < -HALF_PI))) {
            lambda1 = wrap_longitude_positive(lambda1);
            lambda2 = wrap_longitude_positive(lambda2);
        }
        if (phi1 == phi2) {
            double dLambda = (lambda2 - lambda1) / (include_last ? nsegs : (nsegs + 1));
            double lambda = lambda1 + dLambda;
            for (int i = 2; i < result.length; i += 2) {
                result[i] = wrap_longitude(lambda);
                result[i+1] = phi1;
                lambda += dLambda;
            }
        } else if (lambda1 == lambda2) {
            double dPhi = (phi2 - phi1) / (include_last ? nsegs : (nsegs + 1));
            double phi = phi1 + dPhi;
            for (int i = 2; i < result.length; i += 2) {
                result[i] = wrap_longitude(lambda1);
                result[i+1] = phi;
                phi += dPhi;
            }
        } else {
            double y1 = StrictMath.log(StrictMath.tan(QUARTER_PI + (phi1 / 2.0)));
            double y2 = StrictMath.log(StrictMath.tan(QUARTER_PI + (phi2 / 2.0)));
            double m = (lambda2 - lambda1) / (y2 - y1);
            double lonInc = (lambda2 - lambda1) / (include_last ? nsegs : (nsegs + 1));
            double lon = lambda1 + lonInc;
            for (int i = 2; i < result.length; i += 2) {
                double y = y1 + (lon - lambda1)/m;
                result[i] = wrap_longitude(lon);
                result[i+1] = (HALF_PI - 2.0 * StrictMath.atan(StrictMath.pow(StrictMath.E, -y)));
                lon += lonInc;
            }
        }
        return(result);
    }
}
