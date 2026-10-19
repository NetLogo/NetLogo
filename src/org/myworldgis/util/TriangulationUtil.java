package org.myworldgis.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Polygon;

import org.nlogo.api.ExtensionException;
import org.tinfour.common.IConstraint;
import org.tinfour.common.IIncrementalTin;
import org.tinfour.common.PolygonConstraint;
import org.tinfour.common.Vertex;
import org.tinfour.semivirtual.SemiVirtualIncrementalTin;
import org.tinfour.utils.TriangleCollector;

public class TriangulationUtil {
    private static Coordinate tinfourVertexToJTSCoordinate(Vertex vert) {
        return new Coordinate(vert.x, vert.y);
    }

    private static Polygon tinfourVertsToJTSPolygon(Vertex[] verts, GeometryFactory factory) {
        Coordinate[] coords = new Coordinate[4];
        for (int i = 0; i < 3; i++) {
            coords[i] = tinfourVertexToJTSCoordinate(verts[i]);
        }
        // in JTS poygons, the first and last vertex are identical -James Hovet 3/8/21
        coords[3] = tinfourVertexToJTSCoordinate(verts[0]);
        return factory.createPolygon(coords);
    }

    // Consumes triangle verts from the tinfour triangulation and turns them into JTS Polygon triangles within a
    // GeometryCollection. - James Hovet 3/8/21
    private static class GeometryCollectionBuilder implements Consumer<Vertex[]> {
        GeometryFactory factory;
        Geometry geom;
        List<Geometry> geometries;

        GeometryCollectionBuilder(Geometry _geom) {
            this.geom = _geom;
            this.factory = geom.getFactory();
            geometries = new ArrayList<Geometry>();
        }

        public GeometryCollection getCollection() {
            Geometry[] geometriesArr = new Geometry[geometries.size()];
            geometriesArr = geometries.toArray(geometriesArr);
            return new GeometryCollection(geometriesArr, factory);
        }

        @Override
        public void accept(Vertex[] verts) {
            Polygon poly = tinfourVertsToJTSPolygon(verts, factory);
            geometries.add(poly);
        }

    }

    public static PolygonConstraint constraintFromLineString(LineString lineString) {
        CoordinateSequence seq = lineString.getCoordinateSequence();
        Vertex[] exteriorVertices = new Vertex[seq.size() - 1];
        for (int i = 0; i < exteriorVertices.length; i++) {
            Coordinate c = seq.getCoordinate(i);
            exteriorVertices[exteriorVertices.length - i - 1] = new Vertex(c.x, c.y, 0);
        }
        PolygonConstraint polygonConstraint = new PolygonConstraint(Arrays.asList(exteriorVertices));
        polygonConstraint.complete();
        return polygonConstraint;
    }

    public static Geometry triangulate(Geometry geom) throws ExtensionException {
        List<IConstraint> constraints = new ArrayList<>();

        double nominalPointSpacing = geom.getEnvelopeInternal().maxExtent();

        for (int n = 0; n < geom.getNumGeometries(); n++) {
            Polygon thisPolygon = (Polygon) geom.getGeometryN(n);

            PolygonConstraint outerConstraint = constraintFromLineString(thisPolygon.getExteriorRing());
            constraints.add(outerConstraint);
            nominalPointSpacing = Math.min(nominalPointSpacing, outerConstraint.getNominalPointSpacing());

            for (int interiorRing = 0; interiorRing < thisPolygon.getNumInteriorRing(); interiorRing++) {
                PolygonConstraint thisHoleConstraint = constraintFromLineString(thisPolygon.getInteriorRingN(interiorRing));
                constraints.add(thisHoleConstraint);
                nominalPointSpacing = Math.min(nominalPointSpacing, thisHoleConstraint.getNominalPointSpacing());
            }
        }

        // This library requires a "nominal point spacing" constant in order to function and unfortunatly, doesn't fail
        // gracefully when the chosen constant is to large or too small. Thankfully it can be within a few orders of
        // magnitude of the "ideal" and still function, so we can just start at an upper bound generated from the
        // point spacing of the input polygons and try a few until one works or we realize that the input is
        // nonsensical. In my testing, all sane/real-world datasets needed a maximum of one extra attempt,
        // and I had to create a dataset with an outlandish precision of 10^-17 to reach a point where we give up.
        // -James Hovet 3/8/21

        nominalPointSpacing /= 1000.0;
        IIncrementalTin tin = new SemiVirtualIncrementalTin(nominalPointSpacing);
        int triangulationAttempts = 0;
        while (triangulationAttempts < 5) {
            try {
                tin.addConstraints(constraints, true);
                break;
            } catch (Exception e) {
                tin.dispose();
                nominalPointSpacing /= 1000.0;
                tin = new SemiVirtualIncrementalTin(nominalPointSpacing);
            }
            triangulationAttempts += 1;
        }

        if (triangulationAttempts >= 5) {
            throw new ExtensionException("This polygon is too dense and/or complex to generate a point within it. Try simplifying the vector dataset with a tool like QGIS/GRASS v.generalize: https://docs.qgis.org/latest/en/docs/training_manual/processing/generalize.html");
        }

        GeometryCollectionBuilder geometryCollectionBuilder = new GeometryCollectionBuilder(geom);
        TriangleCollector.visitTrianglesConstrained(tin, geometryCollectionBuilder);
        tin.dispose();

        return geometryCollectionBuilder.getCollection();
    }

}
