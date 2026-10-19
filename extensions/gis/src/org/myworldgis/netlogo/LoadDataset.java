//
// Copyright (c) 2007 Eric Russell. All rights reserved.
//

package org.myworldgis.netlogo;

import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.util.GeometryTransformer;
import java.awt.image.BandedSampleModel;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.text.ParseException;
import java.util.Arrays;

import org.myworldgis.io.PointZWrapper;
import org.myworldgis.io.asciigrid.AsciiGridFileReader;
import org.myworldgis.io.geojson.GeoJsonReader;
import org.myworldgis.io.shapefile.DBaseFileReader;
import org.myworldgis.io.shapefile.ESRIShapeBuffer;
import org.myworldgis.io.shapefile.ESRIShapefileReader;
import org.myworldgis.projection.Ellipsoid;
import org.myworldgis.projection.Geographic;
import org.myworldgis.projection.Projection;
import org.myworldgis.projection.ProjectionFormat;
import org.myworldgis.util.StringUtils;
import org.ngs.ngunits.NonSI;
import org.ngs.ngunits.converter.AbstractUnitConverter;
import org.nlogo.api.Argument;
import org.nlogo.api.Context;
import org.nlogo.api.ExtensionException;
import org.nlogo.core.File;
import org.nlogo.api.LogoException;
import org.nlogo.api.OutputDestination;
import org.nlogo.api.OutputDestinationJ;
import org.nlogo.api.Workspace;
import org.nlogo.core.Syntax;
import org.nlogo.core.SyntaxJ;
import org.nlogo.core.prim._const;
import org.nlogo.nvm.ExtensionContext;
import org.nlogo.api.World;


/**
 *
 */
public final class LoadDataset extends GISExtension.Reporter {

    private static final String ADDED_Z_FIELD = "_Z";

    //--------------------------------------------------------------------------
    // Class methods
    //--------------------------------------------------------------------------

    /** */
    private static Dataset loadShapefile (String shpFilePath,
                                          Projection srcProj,
                                          Projection dstProj) throws ExtensionException, IOException {
        File shpFile = GISExtension.getState().getFile(shpFilePath);
        if (shpFile == null) {
            throw new ExtensionException("shapefile " + shpFilePath + " not found");
        }
        String dbfFilePath = StringUtils.changeFileExtension(shpFilePath, "dbf");
        File dbfFile = GISExtension.getState().getFile(dbfFilePath);
        if (dbfFile == null) {
            throw new ExtensionException("dbf file " + dbfFilePath + " not found");
        }
        return loadShapefile(shpFile.getInputStream(), dbfFile.getInputStream(), shpFilePath, srcProj, dstProj);
    }

    /** */
    static Dataset loadShapefile (InputStream shpStream,
                                  InputStream dbfStream,
                                  String sourceName,
                                  Projection srcProj,
                                  Projection dstProj) throws ExtensionException, IOException {
        GeometryTransformer inverse = null;
        GeometryTransformer forward = null;
        boolean reproject = false;
        if ((srcProj != null) &&
            (dstProj != null) &&
            (!srcProj.equals(dstProj))) {
            inverse = srcProj.getInverseTransformer();
            forward = dstProj.getForwardTransformer();
            reproject = true;
        }
        ESRIShapefileReader shp = null;
        DBaseFileReader dbf = null;
        try {
            shp = new ESRIShapefileReader(shpStream,
                                          AbstractUnitConverter.IDENTITY,
                                          GISExtension.getState().factory());
            dbf = new DBaseFileReader(dbfStream);

            VectorDataset.ShapeType shapeType = null;
            boolean shouldAddZField = false;
            boolean shouldWarnPartiallySupportedZ = false;
            switch (shp.getShapeType()) {
                case ESRIShapefileReader.SHAPE_TYPE_POINT:
                case ESRIShapefileReader.SHAPE_TYPE_MULTIPOINT:
                    shapeType = VectorDataset.ShapeType.POINT;
                    break;
                case ESRIShapefileReader.SHAPE_TYPE_POINTZ:
                    shapeType = VectorDataset.ShapeType.POINT;
                    shouldAddZField = true;
                    break;
                case ESRIShapefileReader.SHAPE_TYPE_MULTIPOINTZ:
                    shouldWarnPartiallySupportedZ = true;
                    shapeType = VectorDataset.ShapeType.POINT;
                    break;
                case ESRIShapefileReader.SHAPE_TYPE_POLYLINE:
                    shapeType = VectorDataset.ShapeType.LINE;
                    break;
                case ESRIShapefileReader.SHAPE_TYPE_POLYLINEZ:
                    shouldWarnPartiallySupportedZ = true;
                    shapeType = VectorDataset.ShapeType.LINE;
                    break;
                case ESRIShapefileReader.SHAPE_TYPE_POLYGON:
                    shapeType = VectorDataset.ShapeType.POLYGON;
                    break;
                case ESRIShapefileReader.SHAPE_TYPE_POLYGONZ:
                    shouldWarnPartiallySupportedZ = true;
                    shapeType = VectorDataset.ShapeType.POLYGON;
                    break;
                default:
                    throw new IOException("unsupported shape type " + shp.getShapeType());
            }

            if (shouldWarnPartiallySupportedZ) {
                GISExtension.getState().displayWarning("The shapefile " + sourceName + " contains MultiPointZ, PolyLineZ, or PolygonZ features. "
                        + "Upon import, the Z information from these features will be stripped out and they will be "
                        + "treated as 2D Point, Line, and Polygon features.");
            }

            String[] propertyNames = new String[dbf.getFieldCount() + (shouldAddZField ? 1 : 0)];
            VectorDataset.PropertyType[] propertyTypes = new VectorDataset.PropertyType[propertyNames.length];
            for (int i = 0; i < dbf.getFieldCount(); i += 1) {
                propertyNames[i] = dbf.getFieldName(i);
                if (dbf.getFieldDataType(i) == Syntax.NumberType()) {
                    propertyTypes[i] = VectorDataset.PropertyType.NUMBER;
                } else {
                    propertyTypes[i] = VectorDataset.PropertyType.STRING;
                }
            }
            if (shouldAddZField) {
                propertyNames[propertyNames.length - 1] = ADDED_Z_FIELD;
                propertyTypes[propertyTypes.length - 1] = VectorDataset.PropertyType.NUMBER;
            }

            VectorDataset result = new VectorDataset(shapeType, propertyNames, propertyTypes);
            while (true) {
                Geometry shape = shp.getNextShape();
                if (shape == null) {
                    break;
                }

                double z = 0.0;
                if (shape instanceof PointZWrapper) {
                    z = ((PointZWrapper) shape).getZ();
                    shape = ((PointZWrapper) shape).getPoint();
                }

                if (reproject) {
                    shape = forward.transform(inverse.transform(shape));
                }

                if (shouldAddZField) {
                    Object[] propertyValues = new Object[propertyNames.length];
                    System.arraycopy(dbf.getNextRecord(), 0, propertyValues, 0, propertyValues.length - 1);
                    propertyValues[propertyValues.length - 1] = z;
                    result.add(shape, propertyValues);
                } else {
                    result.add(shape, dbf.getNextRecord());
                }
            }

            return result;
        } finally {
            if (shp != null) {
                try { shp.close(); } catch (IOException e) {
                    // who's bright idea was it to allow close()
                    // to throw an exception, anyway?
                }
            }
            if (dbf != null) {
                try { dbf.close(); } catch (IOException e) { }
            }
        }
    }

    private static VectorDataset loadGeoJson (String geojsonFilePath,
                                              Projection dstProj) throws ExtensionException, IOException {
        File geojsonFile = GISExtension.getState().getFile(geojsonFilePath);
        if (geojsonFile == null){
            throw new ExtensionException("Geojson file " + geojsonFilePath + " not found");
        }
        try {
            return loadGeoJson(new InputStreamReader(geojsonFile.getInputStream()), geojsonFilePath, dstProj);
        } finally {
            try { geojsonFile.close(true); } catch (IOException e) { }
        }
    }

    /** */
    static VectorDataset loadGeoJson (Reader dataReader,
                                      String sourceName,
                                      Projection dstProj) throws ExtensionException, IOException {
        Projection srcProj = new Geographic(Ellipsoid.WGS_84, Projection.DEFAULT_CENTER, NonSI.DEGREE_ANGLE);
        GeometryTransformer inverse = srcProj.getInverseTransformer();
        GeometryTransformer forward = null;
        boolean reproject = false;
        if ((dstProj != null) &&
            (!srcProj.equals(dstProj))) {
            forward = dstProj.getForwardTransformer();
            reproject = true;
        }

        GeoJsonReader reader;
        try {
            reader = new GeoJsonReader(dataReader, GISExtension.getState().factory());
        } catch (org.json.simple.parser.ParseException e){
            throw new ExtensionException("Error parsing " + sourceName);
        }

        String[] propertyNames;
        VectorDataset.PropertyType[] propertyTypes;

        if (reader.getShouldAddZField()) {
            propertyNames = new String[reader.getPropertyNames().length + 1];
            propertyTypes = new VectorDataset.PropertyType[propertyNames.length];
            System.arraycopy(reader.getPropertyNames(), 0, propertyNames, 0, reader.getPropertyNames().length);
            System.arraycopy(reader.getPropertyTypes(), 0, propertyTypes, 0, reader.getPropertyTypes().length);
            propertyNames[propertyNames.length - 1] = ADDED_Z_FIELD;
            propertyTypes[propertyTypes.length - 1] = VectorDataset.PropertyType.NUMBER;
        } else {
            propertyNames = reader.getPropertyNames();
            propertyTypes = reader.getPropertyTypes();
        }

        if (reader.getContainsDefaultValues()) {
            GISExtension.getState().displayWarning("Warning: Not all the features in " + sourceName + " have the same set of properties. "
                    + "Default values (0 for numbers and \"\" for strings) will be supplied where there are missing entries.");
        }

        if (reader.getShouldWarnUnusedZ() && !reader.getShouldAddZField()) {
            GISExtension.getState().displayWarning("The file " + sourceName + " contains non-single-point Z values in some features. "
                    + "Upon import, the Z information from these features will be stripped out and they will be "
                    + "treated as 2D features.");
        }

        VectorDataset result = new VectorDataset(reader.getShapeType(), propertyNames, propertyTypes);

        Geometry[] geometries = reader.getGeometries();
        Object[][] propertyValues = reader.getPropertyValues();
        for (int i = 0; i < reader.size(); i++) {
            double z = 0.0;
            boolean shouldAddZ = false;
            if (geometries[i] instanceof PointZWrapper) {
                shouldAddZ = true;
                z = ((PointZWrapper) geometries[i]).getZ();
                geometries[i] = ((PointZWrapper) geometries[i]).getPoint();
            }

            if (reproject) {
                geometries[i] = forward.transform(inverse.transform(geometries[i]));
            }

            if (shouldAddZ) {
                Object[] newValues = new Object[propertyTypes.length];
                System.arraycopy(propertyValues[i], 0, newValues, 0, propertyValues[i].length);
                newValues[newValues.length - 1] = z;
                propertyValues[i] = newValues;
            }
            result.add(geometries[i], propertyValues[i]);
        }

        return result;
    }

    /** */
    private static RasterDataset loadAsciiGrid (String ascFilePath,
                                                Projection srcProj,
                                                Projection dstProj) throws ExtensionException, IOException {
        File ascFile = GISExtension.getState().getFile(ascFilePath);
        if (ascFile == null) {
            throw new ExtensionException("ascii file " + ascFilePath + " not found");
        }
        return loadAsciiGrid(new BufferedReader(new InputStreamReader(ascFile.getInputStream())), srcProj, dstProj);
    }

    /** */
    static RasterDataset loadAsciiGrid (BufferedReader in,
                                        Projection srcProj,
                                        Projection dstProj) throws ExtensionException, IOException {
        AsciiGridFileReader asc = null;
        try {
            asc = new AsciiGridFileReader(in);
            GridDimensions dimensions = new GridDimensions(asc.getSize(), asc.getEnvelope());
            DataBuffer data = asc.getData();
            BandedSampleModel sampleModel = new BandedSampleModel(data.getDataType(),
                                                                  dimensions.getGridWidth(),
                                                                  dimensions.getGridHeight(),
                                                                  1);
            WritableRaster raster = Raster.createWritableRaster(sampleModel, data, null);
            if ((srcProj != null) &&
                (dstProj != null) &&
                (!srcProj.equals(dstProj))) {
                return new RasterDataset(raster, dimensions, srcProj, dstProj);
            } else {
                return new RasterDataset(dimensions, raster);
            }
        } finally {
            if (asc != null) {
                try { asc.close(); } catch (IOException e) { }
            }
        }
    }

    //--------------------------------------------------------------------------
    // GISExtension.Reporter implementation
    //--------------------------------------------------------------------------

    /** */
    public String getAgentClassString() {
        return "OTPL";
    }

    /** */
    public Syntax getSyntax() {
        return SyntaxJ.reporterSyntax(new int[] { Syntax.StringType() },
                                     Syntax.WildcardType());
    }

    /** */
    public Object reportInternal (Argument[] args, Context context)
            throws ExtensionException, IOException, LogoException, ParseException {
        String dataFilePath = args[0].getString();
        Projection netLogoProjection = GISExtension.getState().getProjection();
        Projection datasetProjection = null;
        File prjFile = GISExtension.getState().getFile(StringUtils.changeFileExtension(dataFilePath, "prj"));
        if (prjFile != null) {
            BufferedReader prjReader = new BufferedReader(new InputStreamReader(prjFile.getInputStream()));
            try {
                datasetProjection = ProjectionFormat.getInstance().parseProjection(prjReader);
            } finally {
                prjReader.close();
            }
        }
        String extension = StringUtils.getFileExtension(dataFilePath);
        Dataset result = null;
        if (extension.equalsIgnoreCase(ESRIShapefileReader.SHAPEFILE_EXTENSION)) {
            result = loadShapefile(dataFilePath, datasetProjection, netLogoProjection);
        } else if (extension.equalsIgnoreCase(AsciiGridFileReader.ASCII_GRID_FILE_EXTENSION_1) ||
                   extension.equalsIgnoreCase(AsciiGridFileReader.ASCII_GRID_FILE_EXTENSION_2)) {
            result = loadAsciiGrid(dataFilePath, datasetProjection, netLogoProjection);
        } else if (extension.equalsIgnoreCase(GeoJsonReader.GEOJSON_EXTENSION) ||
                   extension.equalsIgnoreCase(GeoJsonReader.JSON_EXTENSION)){
            result = loadGeoJson(dataFilePath, netLogoProjection);
        } else {
            throw new ExtensionException("unsupported file type "+extension);
        }
        setDefaultTransformationIfUnset(result, context);
        return result;
    }

    // If the transformation hasn't been set yet, set it to map the given
    // dataset's envelope to the NetLogo world using constant scale.
    static void setDefaultTransformationIfUnset (Dataset result, Context context) {
        if (!GISExtension.getState().isTransformationSet()) {
            World w = context.getAgent().world();
            GISExtension.getState().setTransformation(new CoordinateTransformation
                                                      (result.getEnvelope(),
                                                       new Envelope(w.minPxcor(), w.maxPxcor(), w.minPycor(), w.maxPycor()),
                                                       true));
        }
    }
}
