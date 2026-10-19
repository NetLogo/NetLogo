package org.myworldgis.io.geojson;

import java.io.InputStreamReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import org.locationtech.jts.geom.*;

import org.myworldgis.io.PointZWrapper;
import org.myworldgis.netlogo.VectorDataset.PropertyType;
import org.myworldgis.netlogo.VectorDataset.ShapeType;
import org.nlogo.api.ExtensionException;
import org.nlogo.core.File;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public final class GeoJsonReader implements GeoJsonConstants {

    private static final Map<String, ShapeType> geoJsonStringTypesToShapeTypes;
    static {
        Map<String, ShapeType> tmpMap = new HashMap<String, ShapeType>();
        tmpMap.put("Point", ShapeType.POINT);
        tmpMap.put("MultiPoint", ShapeType.POINT);
        tmpMap.put("LineString", ShapeType.LINE);
        tmpMap.put("MultiLineString", ShapeType.LINE);
        tmpMap.put("Polygon", ShapeType.POLYGON);
        tmpMap.put("MultiPolygon", ShapeType.POLYGON);
        geoJsonStringTypesToShapeTypes = Collections.unmodifiableMap(tmpMap);
    }

    JSONObject geojson;
    GeometryFactory factory;

    ShapeType                 shapeType;
    String                    geojsonShapeType;
    int                       size;
    int                       numProperties;
    Map<String, PropertyType> propertyNamesToDatatypes;
    String[]                  propertyNames;
    PropertyType[]            propertyTypes;
    Geometry[]                geometries;
    boolean                   shouldAddZField = false;
    boolean                   shouldWarnUnusedZ = false;
    Object[][]                propertyValues;
    boolean                   containsDefaultValues;


    public GeoJsonReader(File file, GeometryFactory factory) throws IOException, ParseException, ExtensionException {
        this(new InputStreamReader(file.getInputStream()), factory);
    }

    public GeoJsonReader(Reader dataReader, GeometryFactory factory) throws IOException, ParseException, ExtensionException {
        this.factory = factory;
        this.containsDefaultValues = false;
        this.propertyNamesToDatatypes = new HashMap<String, PropertyType>();

        JSONParser parser = new JSONParser();
        this.geojson = (JSONObject) parser.parse(dataReader);

        String topLevelType = geojson.get("type").toString();

        if (topLevelType.equals("FeatureCollection")) {
            parseFeatureCollection();
        } else if (topLevelType.equals("Feature")) {
            parseSingleFeatureDataset();
        } else if (topLevelType.equals("Point")           ||
                   topLevelType.equals("MultiPoint")      ||
                   topLevelType.equals("LineString")      ||
                   topLevelType.equals("MultiLineString") ||
                   topLevelType.equals("Polygon")         ||
                   topLevelType.equals("MultiPolygon")) {
            parseSingleGeometryDataset();
        } else {
            throw new ExtensionException(topLevelType + " is not a supported GeoJSON type");
        }
    }

    private void extractShapeInfo(JSONObject geometry) throws ExtensionException {
        String geometryTypeString = geometry.get("type").toString();
        this.shapeType = mapStringToShapeType(geometryTypeString);
        this.geojsonShapeType = geometryTypeString;
    }

    private void parseGeometryObject(JSONObject geometry, int featureIndex) throws ExtensionException {
        if (!geometry.get("type").toString().equals(this.geojsonShapeType)) {
            throw new ExtensionException("Only homogenous FeatureCollections are supported");
        }

        JSONArray coordinates = (JSONArray) geometry.get("coordinates");
        this.geometries[featureIndex] = parseCoordinates(coordinates, this.geojsonShapeType);
    }

    private void extractSchemaFromFeatures(JSONArray features) throws ExtensionException {
        for (Object featureObj : features) {
            JSONObject feature = (JSONObject) featureObj;
            parseSchemaOfSingleFeature(feature);
        }
    }

    public void parseSchemaOfSingleFeature(JSONObject feature) throws ExtensionException {
        JSONObject properties = (JSONObject) feature.get("properties");
        for (Object entryObj : properties.entrySet()) {
            @SuppressWarnings("unchecked")
            Map.Entry<String, Object> entry = (Map.Entry<String, Object>) entryObj;

            if (this.propertyNamesToDatatypes.containsKey(entry.getKey())){
                PropertyType existingType = this.propertyNamesToDatatypes.get(entry.getKey());
                PropertyType newType = getPropertyTypeForValue(entry.getValue());
                if (existingType != newType) {
                    throw new ExtensionException("All features properties of the same name must be of the same datatype. "
                    + "The property " + entry.getKey() + "has one value of type of " + existingType.toString()
                    + "and one property type of " + newType.toString());
                }
            } else {
                this.propertyNamesToDatatypes.put(entry.getKey(), getPropertyTypeForValue(entry.getValue()));
            }
        }
    }

    private void finalizeSchema() {
        this.numProperties = this.propertyNamesToDatatypes.size();
        this.propertyNames = new String[numProperties];
        this.propertyTypes = new PropertyType[numProperties];

        int i = 0;
        for (Map.Entry<String, PropertyType> property : this.propertyNamesToDatatypes.entrySet()) {
            this.propertyNames[i] = property.getKey();
            this.propertyTypes[i] = property.getValue();
            i++;
        }
    }

    public void parseFeatureObject(JSONObject feature, int featureIndex) throws ExtensionException {
        JSONObject geometry = (JSONObject) feature.get("geometry");
        parseGeometryObject(geometry, featureIndex);

        Object[] thesePropertyValues = new Object[this.numProperties];
        JSONObject propertiesObject = (JSONObject) feature.get("properties");

        for (int i = 0; i < this.numProperties; i++) {
            PropertyType thisPropertyType = this.propertyTypes[i];
            if (!propertiesObject.containsKey(this.propertyNames[i])) {
                this.containsDefaultValues = true;
                if (thisPropertyType == PropertyType.NUMBER) {
                    thesePropertyValues[i] = 0.0;
                } else {
                    thesePropertyValues[i] = "";
                }
            } else {
                Object thisPropertyValue = propertiesObject.get(this.propertyNames[i]);

                if (thisPropertyType == PropertyType.NUMBER) {
                    thesePropertyValues[i] = ((Number) thisPropertyValue).doubleValue();
                } else {
                    if (thisPropertyValue instanceof JSONObject){
                        thesePropertyValues[i] = ((JSONObject) thisPropertyValue).toJSONString();
                    } else {
                        thesePropertyValues[i] = thisPropertyValue.toString();
                    }
                }
            }
        }
        this.propertyValues[featureIndex] = thesePropertyValues;
    }

    public void parseFeatureCollection() throws ExtensionException {
        JSONArray features = (JSONArray) geojson.get("features");

        if (features.size() < 1) {
            throw new ExtensionException("Each FeatureCollection must have at least one feature.");
        }

        this.size = features.size();
        this.geometries = new Geometry[size];
        this.propertyValues = new Object[size][];

        JSONObject firstFeature = (JSONObject) features.get(0);

        extractShapeInfo((JSONObject) firstFeature.get("geometry"));
        extractSchemaFromFeatures(features);
        finalizeSchema();

        int featureIndex = 0;
        for (Object featureObj : features) {
            JSONObject feature = (JSONObject) featureObj;
            parseFeatureObject(feature, featureIndex);
            featureIndex++;
        }
    }

    public void parseSingleFeatureDataset() throws ExtensionException {
        this.size = 1;
        this.geometries = new Geometry[size];
        this.propertyValues = new Object[size][];

        JSONObject firstFeature = geojson;

        extractShapeInfo((JSONObject) firstFeature.get("geometry"));
        parseSchemaOfSingleFeature(firstFeature);
        finalizeSchema();

        parseFeatureObject(firstFeature, 0);
    }

    public void parseSingleGeometryDataset() throws ExtensionException {
        this.size = 1;
        this.geometries = new Geometry[size];
        this.propertyValues = new Object[size][0];

        extractShapeInfo(geojson);
        finalizeSchema();

        parseGeometryObject(geojson, 0);
        return;
    }

    public int size() {
        return size;
    }

    public ShapeType getShapeType() {
        return shapeType;
    }

    public String[] getPropertyNames() {
        return propertyNames;
    }

    public PropertyType[] getPropertyTypes() {
        return propertyTypes;
    }

    public Geometry[] getGeometries() {
        return geometries;
    }

    public Object[][] getPropertyValues() {
        return propertyValues;
    }

    public boolean getContainsDefaultValues() {
        return containsDefaultValues;
    }

    public boolean getShouldAddZField() {
        return shouldAddZField;
    }

    public boolean getShouldWarnUnusedZ() {
        return shouldWarnUnusedZ;
    }

    private Geometry parseCoordinates(JSONArray coordinates, String geojsonShapeType) throws ExtensionException {
        switch (geojsonShapeType) {
            case "Point":
                Point p = factory.createPoint(JSONPairToCoordinate(coordinates));
                if (coordinates.size() == 3) {
                    this.shouldAddZField = true;
                    return new PointZWrapper(p, ((Number) coordinates.get(2)).doubleValue());
                } else {
                    return p;
                }
            case "MultiPoint":
                Coordinate[] pointCoords = new Coordinate[coordinates.size()];
                for (int i = 0; i < coordinates.size(); i++) {
                    pointCoords[i] = JSONPairToCoordinate((JSONArray) coordinates.get(i));
                }
                Geometry points = factory.createMultiPointFromCoords(pointCoords);
                return points;
            case "LineString":
                return parseSingleLineString(coordinates);
            case "MultiLineString":
                LineString[] subLines = new LineString[coordinates.size()];
                for (int i = 0; i < coordinates.size(); i++) {
                    subLines[i] = parseSingleLineString((JSONArray) coordinates.get(i));
                }
                Geometry lines = factory.createMultiLineString(subLines);
                return lines;
            case "Polygon":
                return parseSingleComplexPolygon(coordinates);
            case "MultiPolygon":
                if (coordinates.size() < 1) {
                    throw new ExtensionException("One of the MultiPolygons has no polygons within it");
                }
                Polygon[] subPolygons = new Polygon[coordinates.size()];
                for (int i = 0; i < coordinates.size(); i++) {
                    subPolygons[i] = parseSingleComplexPolygon((JSONArray) coordinates.get(i));
                }
                Geometry polygons = factory.createMultiPolygon(subPolygons);
                return polygons;
            default:
                throw new ExtensionException(geojsonShapeType + " is not a supported geojson shape type");
        }
    }

    private LineString parseSingleLineString(JSONArray coordinates) throws ExtensionException {
        Coordinate[] linePoints = new Coordinate[coordinates.size()];
                for (int i = 0; i < coordinates.size(); i++) {
                    linePoints[i] = JSONPairToCoordinate((JSONArray) coordinates.get(i));
                }
        LineString line = factory.createLineString(linePoints);
        return line;
    }

    private Polygon parseSingleComplexPolygon(JSONArray coordinates) throws ExtensionException {
        if (coordinates.size() < 1) { throw new ExtensionException("Empty polygon in geojson file");}
        int numRings = coordinates.size();
        int numHoles = numRings - 1;

        JSONArray shellArr = (JSONArray) coordinates.get(0);
        Coordinate[] shellCoords = new Coordinate[shellArr.size()];
        for (int j = 0; j < shellArr.size(); j++) {
            shellCoords[j] = JSONPairToCoordinate((JSONArray)shellArr.get(j));
        }
        LinearRing shell = factory.createLinearRing(shellCoords);

        Coordinate[][] holeCoords = new Coordinate[numHoles][];
        LinearRing[] holeRings = new LinearRing[numHoles];
        for (int i = 0; i < numHoles; i++) {
            JSONArray thisRing = (JSONArray) coordinates.get(i + 1);
            holeCoords[i] = new Coordinate[thisRing.size()];
            for (int j = 0; j < thisRing.size(); j++) {
                holeCoords[i][j] = JSONPairToCoordinate((JSONArray) thisRing.get(j));
            }
            holeRings[i] = factory.createLinearRing(holeCoords[i]);
        }

        Polygon polygon = factory.createPolygon(shell, holeRings);
        return polygon;
    }

    private Coordinate JSONPairToCoordinate(JSONArray arr) {
        if (arr.size() > 2) {
            this.shouldWarnUnusedZ = true;
        }
        return new Coordinate(((Number) arr.get(0)).doubleValue(), ((Number) arr.get(1)).doubleValue());
    }

    private static ShapeType mapStringToShapeType(String str) throws ExtensionException {
        if (geoJsonStringTypesToShapeTypes.containsKey(str)) {
            return geoJsonStringTypesToShapeTypes.get(str);
        } else {
            throw new ExtensionException(str + "is not a supported geojson geometry type");
        }
    }

    private static PropertyType getPropertyTypeForValue(Object obj) throws ExtensionException {
        if (obj instanceof Number) {
            return PropertyType.NUMBER;
        } else {
            return PropertyType.STRING;
        }
    }
}
