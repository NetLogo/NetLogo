//
// Copyright (c) 2006 the National Geographic Society. All rights reserved.
//

package org.myworldgis.util;

import com.vividsolutions.jts.algorithm.CGAlgorithms;
import com.vividsolutions.jts.algorithm.locate.SimplePointInAreaLocator;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateArrays;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.GeometryFilter;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Location;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.TopologyException;
import com.vividsolutions.jts.operation.polygonize.Polygonizer;
import com.vividsolutions.jts.operation.valid.IsValidOp;
import com.vividsolutions.jts.operation.valid.TopologyValidationError;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;


/** 
 * 
 */
public final strictfp class JTSUtils {
    
    //--------------------------------------------------------------------------
    // Class variables
    //--------------------------------------------------------------------------
    
    /** */
    private static final Comparator<Polygon> POLYGON_AREA_COMPARATOR = new Comparator<Polygon>() {
            public int compare (Polygon p1, Polygon p2) {
                double a1 = p1.getArea();
                double a2 = p2.getArea();
                if (a1 < a2) {
                    return(1);
                } else if (a1 > a2) {
                    return(-1);
                } else {
                    return(0);
                }
            }
        };
    
    /** */
    private static final Comparator<Polygon> POLYGON_CONTAINMENT_COMPARATOR = new Comparator<Polygon>() {
            public int compare (Polygon p1, Polygon p2) {
                if (p1.contains(p2)) {
                    return(1);
                } else if (p2.contains(p1)) {
                    return(-1);
                } else {
                    return(0);
                }
            }
        };
    
    //--------------------------------------------------------------------------
    // Class methods
    //--------------------------------------------------------------------------
    
    /**
     * JTS enforces a much stricter standards for the topology of Polygons
     * than the shapefile format does. This method takes a list of polygon
     * rings and does its best to build a valid JTS MultiPolygon out of them
     * by grouping negative-area "hole" rings with their containing positive-
     * area "shell" rings, and by repairing common topology errors such as 
     * self intersections.
     */
    public static MultiPolygon buildPolygonGeometry (LinearRing[] rings, 
                                                     GeometryFactory factory,
                                                     boolean repair) {
        MultiPolygon result = null;
        if (rings.length == 0) {
            // short-circuit for the empty case
            result = factory.createMultiPolygon(null);
        } else if (rings.length == 1) {
            // short-circuit for common, simple case
            result = factory.createMultiPolygon(new Polygon[] { factory.createPolygon(rings[0], null) });
        } else {
            List<LinearRing> shellRingList = new ArrayList<LinearRing>(rings.length);
            List<LinearRing> holeRingList = new ArrayList<LinearRing>(rings.length-1);
            for (int i = 0; i < rings.length; i += 1) {
                LinearRing ring = rings[i];
                if (ring.getNumPoints() > 0) {
                    if (CGAlgorithms.isCCW(ring.getCoordinates())) {
                        holeRingList.add(ring);
                    } else {
                        shellRingList.add(ring);
                    }
                }
            }
            if (shellRingList.size() == 1) {
                // short-circuit for another common, simple case
                Polygon poly = factory.createPolygon(shellRingList.get(0), 
                                                     GeometryFactory.toLinearRingArray(holeRingList));
                result = factory.createMultiPolygon(new Polygon[] { poly });
            } else if (holeRingList.size() == 0) {
                // short-circuit for another common, simple case
                Polygon[] polygons = new Polygon[shellRingList.size()];
                for (int i = 0; i < shellRingList.size(); i += 1) {
                    polygons[i] = factory.createPolygon(shellRingList.get(i), null);
                }
                result = factory.createMultiPolygon(polygons);
            } else {
                // complex case: we have multiple shell rings AND one or more hole rings,
                // so we need to do some work to figure out which shell ring each hole 
                // ring belongs to
                Polygon[] polygons = new Polygon[shellRingList.size()];
                for (int i = 0; i < shellRingList.size(); i += 1) {
                    polygons[i] = factory.createPolygon(shellRingList.get(i), null);
                }
                ArrayList<Polygon> holeContainers = new ArrayList<Polygon>(4);
                for (int i = 0; i < holeRingList.size(); i += 1) {
                    LinearRing hole = holeRingList.get(i);
                    holeContainers.clear();
                    for (int j = 0; j < polygons.length; j += 1) {
                        if (polygons[j].contains(hole)) {
                            holeContainers.add(polygons[j]);
                        }
                    }
                    if (holeContainers.size() > 0) {
                        Collections.sort(holeContainers, POLYGON_CONTAINMENT_COMPARATOR);
                        Polygon container = holeContainers.get(0);
                        LinearRing[] newHoles = new LinearRing[container.getNumInteriorRing()+1];
                        for (int j = 0; j < container.getNumInteriorRing(); j += 1) {
                            newHoles[j] = (LinearRing)container.getInteriorRingN(j);
                        }
                        newHoles[newHoles.length-1] = hole;
                        for (int j = 0; j < polygons.length; j += 1) {
                            if (container == polygons[j]) {
                                polygons[j] = factory.createPolygon((LinearRing)container.getExteriorRing(), newHoles);
                                break;
                            }
                        }
                    }
                }
                result = factory.createMultiPolygon(polygons);
            }
        }
        if (result == null) {
            return factory.createMultiPolygon(null);
        }
        if (repair) {
            result = (MultiPolygon)repair(result);
        }
        return result;
    }
    
    /** */
    public static Geometry repair (Geometry geom) {
        GeometryFactory factory = geom.getFactory();
        if (geom instanceof MultiPolygon) {
            MultiPolygon mp = (MultiPolygon)geom;
            Polygon[] polys = new Polygon[mp.getNumGeometries()];
            for (int i = 0; i < mp.getNumGeometries(); i += 1) {
                polys[i] = repair((Polygon)mp.getGeometryN(i));
            }
            return factory.createMultiPolygon(polys);
        } else if (geom instanceof Polygon) {
            return repair((Polygon)geom);
        } else if (geom.getGeometryType().equals("GeometryCollection")) {
            GeometryCollection gc = (GeometryCollection)geom;
            Geometry[] geoms = new Geometry[gc.getNumGeometries()];
            for (int i = 0; i < gc.getNumGeometries(); i += 1) {
                geoms[i] = repair(gc.getGeometryN(i));
            }
            Thread.dumpStack();
            return factory.createGeometryCollection(geoms);
        } else {
            return(geom);
        }
    }
    
    /** */
    @SuppressWarnings("unchecked")
    public static Polygon repair (Polygon p) {
        GeometryFactory factory = p.getFactory();
        IsValidOp isValidOp = new IsValidOp(p);
        TopologyValidationError err = isValidOp.getValidationError();
        while (err != null) {
            if ((err.getErrorType() == TopologyValidationError.SELF_INTERSECTION) ||
                (err.getErrorType() == TopologyValidationError.RING_SELF_INTERSECTION) ||
                (err.getErrorType() == TopologyValidationError.DISCONNECTED_INTERIOR)) {
                Geometry boundary = p.getBoundary();
                // calling union will re-node the boundary curve to eliminate self-intersections
                // see http://lists.jump-project.org/pipermail/jts-devel/2006-November/001815.html
                boundary = boundary.union(boundary);
                Polygonizer polygonizer = new Polygonizer();
                polygonizer.add(boundary);
                Collection c = polygonizer.getPolygons();
                if (c.size() > 0) {
                    Polygon[] polys = (Polygon[])c.toArray(new Polygon[c.size()]);
                    Arrays.sort(polys, POLYGON_AREA_COMPARATOR);
                    p = polys[0];
                } else {
                    System.err.println("unable to fix polygon: " + err);
                    p = factory.createPolygon(null, null);
                }
            } else if (err.getErrorType() == TopologyValidationError.TOO_FEW_POINTS) {
                LinearRing exterior = (LinearRing)p.getExteriorRing();
                Coordinate[] coords = CoordinateArrays.removeRepeatedPoints(exterior.getCoordinates());
                if (coords.length < 4) {
                    p = factory.createPolygon(null, null);
                } else {
                    exterior = factory.createLinearRing(coords);
                    List<LinearRing> validInteriorRings = new ArrayList<LinearRing>(p.getNumInteriorRing());
                    for (int i = 0; i < p.getNumInteriorRing(); i += 1) {
                        LinearRing s = (LinearRing)p.getInteriorRingN(i);
                        coords = CoordinateArrays.removeRepeatedPoints(s.getCoordinates());
                        if (coords.length >= 4) {
                            validInteriorRings.add(factory.createLinearRing(coords));
                        }
                    }
                    p = factory.createPolygon(exterior, GeometryFactory.toLinearRingArray(validInteriorRings));
                }
            } else {
                System.err.println(err);
                p = factory.createPolygon(null, null);
            }
            isValidOp = new IsValidOp(p);
            err = isValidOp.getValidationError();
        }
        return(p);
    }
    
    /** */
    public static GeometryCollection explodeMultiPolygon (MultiPolygon mp) {
        List<LinearRing> result = new ArrayList<LinearRing>(mp.getNumGeometries()*2);
        for (int i = 0; i < mp.getNumGeometries(); i += 1) {
            Polygon p = (Polygon)mp.getGeometryN(i);
            result.add((LinearRing)p.getExteriorRing());
            for (int j = 0; j < p.getNumInteriorRing(); j += 1) {
                result.add((LinearRing)p.getInteriorRingN(j));
            }
        }   
        return mp.getFactory().createGeometryCollection(GeometryFactory.toGeometryArray(result));
    }
    
    /** */
    public static Geometry flatten (GeometryCollection gc) {
        final List<Point> points = new LinkedList<Point>();
        final List<LineString> lines = new LinkedList<LineString>();
        final List<Polygon> polygons = new LinkedList<Polygon>();
        gc.apply(new GeometryFilter() {
                public void filter (Geometry geom) {
                    if (geom instanceof Point) {
                        points.add((Point)geom);
                    } else if (geom instanceof LineString) {
                        lines.add((LineString)geom);
                    } else if (geom instanceof Polygon) {
                        polygons.add((Polygon)geom);
                    }
                }
            });
        if (!polygons.isEmpty()) {
            return gc.getFactory().createMultiPolygon(GeometryFactory.toPolygonArray(polygons));
        } else if (!lines.isEmpty()) {
            return gc.getFactory().createMultiLineString(GeometryFactory.toLineStringArray(lines));
        } else {
            return gc.getFactory().createMultiPoint(GeometryFactory.toPointArray(points));
        }
    }
    
    /** */
    public static double getSharedAreaRatio (Geometry geom1, Geometry geom2) {
        try {
            return geom1.intersection(geom2).getArea() / geom1.getArea();
        } catch (TopologyException e) {
            // HACK: there appears to be a bug in JTS, but I can't
            // reproduce it consistently. Why should computing the
            // intersection with a MultiPolygon fail when computing
            // the intersection with each of its constituent Polygons
            // succeeds? I have no idea, but it does happen. This 
            // seems to fix the problem, though.
            double result = 0.0;
            if (geom2 instanceof GeometryCollection) {
                GeometryCollection gc = (GeometryCollection)geom2;
                for (int j = 0; j < gc.getNumGeometries(); j += 1) {
                    result += geom1.intersection(gc.getGeometryN(j)).getArea();
                }
                return result / geom1.getArea();
            } else {
                throw e;
            }
        }
    }
    
    /**
     * This is ten times faster than the absolutely correct 
     * version above, and it's only off by an average of 1%.
     * Note that the first argument MUST be rectangular, or
     * your results will be meaningless.
     */
    public static double fastGetSharedAreaRatio (Geometry geom1, Geometry geom2) {
        Envelope env1 = geom1.getEnvelopeInternal();
        if ((SimplePointInAreaLocator.locate(new Coordinate(env1.getMinX(),env1.getMinY()), geom2) == Location.INTERIOR) &&
            (SimplePointInAreaLocator.locate(new Coordinate(env1.getMaxX(),env1.getMaxY()), geom2) == Location.INTERIOR) &&
            (SimplePointInAreaLocator.locate(new Coordinate(env1.getMaxX(),env1.getMinY()), geom2) == Location.INTERIOR) &&
            (SimplePointInAreaLocator.locate(new Coordinate(env1.getMinX(),env1.getMaxY()), geom2) == Location.INTERIOR)) {
            // I suppose it is possible for a valid polygon geometry
            // to contain all four corners and share considerably less
            // than 100% of its area with the envelope in question.
            // But if you're that worried about correctness you 
            // shouldn't be using this method in the first place.
            return 1.0;
        }
        double xInc = env1.getWidth() / 9.0;
        double yInc = env1.getHeight() / 9.0;
        double x = env1.getMinX();
        double y = env1.getMinY();
        double ct = 0;
        for (int i = 0; i < 10; i += 1) {
            y = env1.getMinY();
            for (int j = 0; j < 10; j += 1) {
                if (SimplePointInAreaLocator.locate(new Coordinate(x,y), geom2) == Location.INTERIOR) {
                    ct += 1;
                }
                y += yInc;
            }
            x += xInc;
        }
        return (ct / 100.0);
    }
}
