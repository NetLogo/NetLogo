package org.myworldgis.io.geojson;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Iterator;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.MultiPoint;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.myworldgis.netlogo.VectorDataset;
import org.myworldgis.netlogo.VectorFeature;

@SuppressWarnings("unchecked")
public final class GeoJsonWriter implements GeoJsonConstants {

    private RandomAccessFile file;
    private VectorDataset dataset;
    private JSONObject root;
    private JSONArray features;

    public GeoJsonWriter(RandomAccessFile file, VectorDataset dataset) throws IOException {
        this(dataset);
        this.file = file;
        writeToFile();
    }

    public GeoJsonWriter(VectorDataset dataset) throws IOException {
        this.dataset = dataset;
        this.root = new JSONObject();
        this.features = new JSONArray();

        setupJsonObject();
        processVectorFeatures();
        this.root.put("features", this.features);
    }

    public String getJsonString() {
        return root.toJSONString();
    }

    private void setupJsonObject() {
        this.root.put("type", "FeatureCollection");
    }

    private void processVectorFeatures() throws IOException {
        VectorDataset.Property[] propSchema = dataset.getProperties();
        for (Iterator<VectorFeature> i = dataset.getFeatures().iterator(); i.hasNext(); ) {
            VectorFeature f = i.next();

            JSONObject thisFeatureJson = new JSONObject();
            thisFeatureJson.put("type", "Feature");
            thisFeatureJson.put("geometry", createGeometryObject(f.getGeometry()));
            thisFeatureJson.put("properties", createPropertiesObject(f, propSchema));
            this.features.add(thisFeatureJson);
        }
    }

    private JSONObject createPropertiesObject(VectorFeature f, VectorDataset.Property[] propSchema) {
        JSONObject props = new JSONObject();
        for (VectorDataset.Property prop : propSchema) {
            String propName = prop.getName();
            props.put(propName, f.getProperty(propName));
        }
        return props;
    }

    private void writeToFile() throws IOException {
        this.file.setLength(0);
        this.file.writeBytes(getJsonString());
    }

    private JSONObject createGeometryObject(Geometry geom) {
        if (geom instanceof Point) {
            return createGeometryObject((Point) geom);
        } else if (geom instanceof MultiPoint) {
            return createGeometryObject((MultiPoint) geom);
        } else if (geom instanceof LineString) {
            return createGeometryObject((LineString) geom);
        } else if (geom instanceof MultiLineString) {
            return createGeometryObject((MultiLineString) geom);
        } else if (geom instanceof Polygon) {
            return createGeometryObject((Polygon) geom);
        } else if (geom instanceof MultiPolygon) {
            return createGeometryObject((MultiPolygon) geom);
        } else {
            return new JSONObject();
        }
    }

    private JSONObject createGeometryObject(Point point) {
        JSONObject out = new JSONObject();
        out.put("type", "Point");
        out.put("coordinates", pointToJSONArray(point));
        return out;
    }

    private JSONObject createGeometryObject(MultiPoint multiPoint) {
        JSONObject out = new JSONObject();
        out.put("type", "MultiPoint");

        JSONArray coordsArray = new JSONArray();
        int numPoints = multiPoint.getNumGeometries();
        for (int i = 0; i < numPoints ; i++) {
            Point thisPoint = (Point) multiPoint.getGeometryN(i);
            coordsArray.add(pointToJSONArray(thisPoint));
        }
        out.put("coordinates", coordsArray);

        return out;
    }

    private JSONObject createGeometryObject(LineString lineString) {
        JSONObject out = new JSONObject();
        out.put("type", "LineString");
        out.put("coordinates", lineStringToJSONArray(lineString));
        return out;
    }

    private JSONObject createGeometryObject(Polygon polygon) {
        JSONObject out = new JSONObject();
        out.put("type", "Polygon");
        out.put("coordinates", polygonToJSONArray(polygon));
        return out;
    }

    private JSONObject createGeometryObject(MultiPolygon multiPolygon) {
        JSONObject out = new JSONObject();
        out.put("type", "MultiPolygon");

        JSONArray coordsArray = new JSONArray();
        int numPolygons = multiPolygon.getNumGeometries();
        for (int i = 0; i < numPolygons; i++) {
            Polygon thisPolygon = (Polygon) multiPolygon.getGeometryN(i);
            coordsArray.add(polygonToJSONArray(thisPolygon));
        }
        out.put("coordinates", coordsArray);

        return out;
    }

    private JSONObject createGeometryObject(MultiLineString multiLineString) {
        JSONObject out = new JSONObject();
        out.put("type", "MultiLineString");

        JSONArray coordsArray = new JSONArray();
        int numLineStrings = multiLineString.getNumGeometries();
        for (int i = 0; i < numLineStrings; i++) {
            LineString thisLineString = (LineString) multiLineString.getGeometryN(i);
            coordsArray.add(lineStringToJSONArray(thisLineString));
        }
        out.put("coordinates", coordsArray);

        return out;
    }

    //--------------------------------------------------------------------------
    // Geometry to JSON Utils
    //--------------------------------------------------------------------------

    private static JSONArray coordToJSONArray(Coordinate coord) {
        JSONArray arr = new JSONArray();
        arr.add(coord.x);
        arr.add(coord.y);
        return arr;
    }

    private static JSONArray pointToJSONArray(Point p) {
        return coordToJSONArray(p.getCoordinate());
    }

    public static JSONArray lineStringToJSONArray(LineString lineString) {
        JSONArray arr = new JSONArray();
        int numPoints = lineString.getNumPoints();
        for (int i = 0; i < numPoints; i++) {
            arr.add(coordToJSONArray(lineString.getCoordinateN(i)));
        }
        return arr;
    }

    public static JSONArray polygonToJSONArray(Polygon poly) {
        JSONArray arr = new JSONArray();
        arr.add(lineStringToJSONArray(poly.getExteriorRing()));
        int numInteriorRings = poly.getNumInteriorRing();
        for (int i = 0; i < numInteriorRings; i ++) {
            arr.add(lineStringToJSONArray(poly.getInteriorRingN(i)));
        }
        return arr;
    }
}
