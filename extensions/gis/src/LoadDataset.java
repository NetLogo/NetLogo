//
// Copyright (c) 2007 Eric Russell. All rights reserved.
//

package org.myworldgis.netlogo;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.util.GeometryTransformer;
import java.awt.image.BandedSampleModel;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import javax.measure.converter.UnitConverter;
import org.myworldgis.io.asciigrid.AsciiGridFileReader;
import org.myworldgis.io.shapefile.DBaseFileReader;
import org.myworldgis.io.shapefile.ESRIShapefileReader;
import org.myworldgis.projection.Projection;
import org.myworldgis.projection.ProjectionFormat;
import org.myworldgis.util.StringUtils;
import org.nlogo.api.Argument;
import org.nlogo.api.Context;
import org.nlogo.api.ExtensionException;
import org.nlogo.api.File;
import org.nlogo.api.LogoException;
import org.nlogo.api.Syntax;
import org.nlogo.api.World;


/**
 * 
 */
public final strictfp class LoadDataset extends GISExtension.Reporter {
    
    //--------------------------------------------------------------------------
    // Class methods
    //--------------------------------------------------------------------------
    
    /** */
    private static Dataset loadShapefile (String shpFilePath,
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
            File shpFile = GISExtension.getState().getFile(shpFilePath);
            if (shpFile == null) {
                throw new ExtensionException("shapefile " + shpFilePath + " not found");
            }
            shp = new ESRIShapefileReader(shpFile.getInputStream(), 
                                          UnitConverter.IDENTITY,
                                          GISExtension.getState().factory());
            String dbfFilePath = StringUtils.changeFileExtension(shpFilePath, "dbf");
            File dbfFile = GISExtension.getState().getFile(dbfFilePath);
            if (dbfFile == null) {
                throw new ExtensionException("dbf file " + dbfFilePath + " not found");
            }
            dbf = new DBaseFileReader(dbfFile.getInputStream());
            
            VectorDataset.ShapeType shapeType = null;
            switch (shp.getShapeType()) {
                case ESRIShapefileReader.SHAPE_TYPE_POINT:
                case ESRIShapefileReader.SHAPE_TYPE_MULTIPOINT:
                    shapeType = VectorDataset.ShapeType.POINT;
                    break;
                case ESRIShapefileReader.SHAPE_TYPE_POLYLINE:
                    shapeType = VectorDataset.ShapeType.LINE;
                    break;
                case ESRIShapefileReader.SHAPE_TYPE_POLYGON:
                    shapeType = VectorDataset.ShapeType.POLYGON;
                    break;
                default:
                    throw new IOException("unsupported shape type " + shp.getShapeType());
            }
            String[] propertyNames = new String[dbf.getFieldCount()];
            VectorDataset.PropertyType[] propertyTypes = new VectorDataset.PropertyType[propertyNames.length];
            for (int i = 0; i < dbf.getFieldCount(); i += 1) {
                propertyNames[i] = dbf.getFieldName(i);
                if (dbf.getFieldDataType(i) == Syntax.TYPE_NUMBER) {
                    propertyTypes[i] = VectorDataset.PropertyType.NUMBER;
                } else {
                    propertyTypes[i] = VectorDataset.PropertyType.STRING;
                }
            }
            VectorDataset result = new VectorDataset(shapeType, propertyNames, propertyTypes);
            while (true) {
                Geometry shape = shp.getNextShape();
                if (shape == null) {
                    break;
                } else if (reproject) {
                    shape = forward.transform(inverse.transform(shape));
                }
                result.add(shape, dbf.getNextRecord());
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
    
    /** */
    private static RasterDataset loadAsciiGrid (String ascFilePath,
                                                Projection srcProj,
                                                Projection dstProj) throws ExtensionException, IOException {
        AsciiGridFileReader asc = null;
        try {
            File ascFile = GISExtension.getState().getFile(ascFilePath);
            if (ascFile == null) {
                throw new ExtensionException("ascii file " + ascFilePath + " not found");
            }
            asc = new AsciiGridFileReader(new BufferedReader(new InputStreamReader(ascFile.getInputStream())));
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
        return Syntax.reporterSyntax(new int[] { Syntax.TYPE_STRING },
                                     Syntax.TYPE_WILDCARD);
    }
    
    /** */
    public Object reportInternal (Argument args[], Context context) 
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
        } else {
            throw new ExtensionException("unsupported file type "+extension);
        }
        // If the transformation hasn't been set yet, set it to map this
        // dataset's envelope to the NetLogo world using constant scale.
        if (!GISExtension.getState().isTransformationSet()) {
            World w = context.getAgent().world();
            GISExtension.getState().setTransformation(new CoordinateTransformation
                                                      (result.getEnvelope(),
                                                       new Envelope(w.minPxcor(), w.maxPxcor(), w.minPycor(), w.maxPycor()),
                                                       true));
        }
        return result;
    }
}
