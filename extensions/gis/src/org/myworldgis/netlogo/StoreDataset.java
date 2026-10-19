//
// Copyright (c) 2007 Eric Russell. All rights reserved.
//

package org.myworldgis.netlogo;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.Writer;
import java.util.Arrays;
import java.util.Iterator;
import org.myworldgis.io.FileRandomAccessSink;
import org.myworldgis.io.RandomAccessSink;
import org.myworldgis.io.asciigrid.AsciiGridFileWriter;
import org.myworldgis.io.geojson.GeoJsonWriter;
import org.myworldgis.io.shapefile.DBaseBuffer;
import org.myworldgis.io.shapefile.DBaseFieldDescriptor;
import org.myworldgis.io.shapefile.DBaseFileWriter;
import org.myworldgis.io.shapefile.ESRIShapeIndexRecord;
import org.myworldgis.io.shapefile.ESRIShapeIndexWriter;
import org.myworldgis.io.shapefile.ESRIShapefileWriter;
import org.myworldgis.projection.Projection;
import org.myworldgis.projection.ProjectionFormat;
import org.myworldgis.util.StringUtils;
import org.ngs.ngunits.converter.AbstractUnitConverter;
import org.nlogo.api.Argument;
import org.nlogo.api.Context;
import org.nlogo.api.ExtensionException;
import org.nlogo.api.LogoException;
import org.nlogo.core.Syntax;
import org.nlogo.core.SyntaxJ;


/**
 *
 */
public final class StoreDataset extends GISExtension.Command {

    //--------------------------------------------------------------------------
    // Class methods
    //--------------------------------------------------------------------------

    /** */
    private static String storeAsciiGrid (RasterDataset dataset, String file)
            throws IOException {
        String ascFile = StringUtils.changeFileExtension(file, AsciiGridFileWriter.ASCII_GRID_FILE_EXTENSION_1);
        storeAsciiGrid(dataset, new FileWriter(new File(ascFile)));
        return ascFile;
    }

    /** */
    static void storeAsciiGrid (RasterDataset dataset, Writer out)
            throws IOException {
        AsciiGridFileWriter asc = new AsciiGridFileWriter(out);
        try {
            GridDimensions dimensions = dataset.getDimensions();
            asc.writeGridInfo(dimensions.getGridSize(), dimensions.getEnvelope(), Double.NaN);
            asc.writeGridData(dataset.getRaster().getDataBuffer());
        } finally {
            asc.close();
        }
    }


    /** */
    private static String storeShapefile (VectorDataset dataset,String file)
            throws IOException {
        String shpFile = StringUtils.changeFileExtension(file, ESRIShapefileWriter.SHAPEFILE_EXTENSION);
        String shxFile = StringUtils.changeFileExtension(file, ESRIShapeIndexWriter.SHAPE_INDEX_EXTENSION);
        String dbfFile = StringUtils.changeFileExtension(file, DBaseFileWriter.DBASE_FILE_EXTENSION);
        storeShapefile(dataset,
                       new FileRandomAccessSink(new RandomAccessFile(shpFile, "rw")),
                       new FileRandomAccessSink(new RandomAccessFile(shxFile, "rw")),
                       new FileRandomAccessSink(new RandomAccessFile(dbfFile, "rw")));
        return shpFile;
    }

    /** */
    static void storeShapefile (VectorDataset dataset,
                                RandomAccessSink shpSink,
                                RandomAccessSink shxSink,
                                RandomAccessSink dbfSink)
            throws IOException {
        ESRIShapefileWriter shp = new ESRIShapefileWriter(shpSink,
                                                          dataset.getEnvelope(),
                                                          esriShapeType(dataset),
                                                          AbstractUnitConverter.IDENTITY,
                                                          GISExtension.getState().factory());
        ESRIShapeIndexWriter shx = new ESRIShapeIndexWriter(shxSink,
                                                            dataset.getEnvelope(),
                                                            esriShapeType(dataset),
                                                            AbstractUnitConverter.IDENTITY,
                                                            GISExtension.getState().factory());
        DBaseFileWriter dbf = new DBaseFileWriter(dbfSink,
                                                  dBaseFieldDescriptors(dataset));
        try {
            int recordIndex = 0;
            VectorDataset.Property[] props = dataset.getProperties();
            for (Iterator<VectorFeature> i = dataset.getFeatures().iterator(); i.hasNext(); ) {
                VectorFeature f = i.next();
                ESRIShapeIndexRecord indexRecord = shp.writeShape(recordIndex, f.getGeometry());
                shx.writeIndexRecord(indexRecord);
                Object[] data = new Object[props.length];
                for (int j = 0; j < props.length; j += 1) {
                    data[j] = f.getProperty(props[j].getName());
                }
                dbf.writeRecord(data);
            }
        } finally {
            if (shp != null) {
                shp.close();
            }
            if (shx != null) {
                shx.close();
            }
            if (dbf != null) {
                dbf.close();
            }
        }
    }


    /** */
    public static int esriShapeType (VectorDataset dataset) {
        switch (dataset.getShapeType()) {
            case POINT:
                for (Iterator<VectorFeature> i = dataset.getFeatures().iterator(); i.hasNext(); ) {
                    if (i.next().getGeometry().getNumPoints() != 1) {
                        return ESRIShapefileWriter.SHAPE_TYPE_MULTIPOINT;
                    }
                }
                return ESRIShapefileWriter.SHAPE_TYPE_POINT;
            case LINE:
                return ESRIShapefileWriter.SHAPE_TYPE_POLYLINE;
            case POLYGON:
                return ESRIShapefileWriter.SHAPE_TYPE_POLYGON;
            default:
                return ESRIShapefileWriter.SHAPE_TYPE_NULL;
        }
    }

    /** */
    private static DBaseFieldDescriptor[] dBaseFieldDescriptors (VectorDataset dataset) {
        VectorDataset.Property[] props = dataset.getProperties();
        String[] fieldNames = new String[props.length];
        char[] fieldTypes = new char[props.length];
        for (int i = 0; i < props.length; i += 1) {
            fieldNames[i] = props[i].getName();
            switch (props[i].getType()) {
                case STRING:
                    fieldTypes[i] = DBaseFieldDescriptor.FIELD_TYPE_CHARACTER;
                    break;
                case NUMBER:
                    fieldTypes[i] = DBaseFieldDescriptor.FIELD_TYPE_NUMBER;
                    break;
                default:
                    throw new RuntimeException("this can't happen");
            }
        }
        fieldNames = DBaseFieldDescriptor.makeLegalFieldNames(fieldNames);
        int[] fieldLengths = new int[props.length];
        Arrays.fill(fieldLengths, 0);
        int[] fieldDecimals = new int[props.length];
        Arrays.fill(fieldDecimals, 0);
        for (Iterator<VectorFeature> i = dataset.getFeatures().iterator(); i.hasNext(); ) {
            VectorFeature f = i.next();
            for (int j = 0; j < props.length; j += 1) {
                Object value = f.getProperty(props[j].getName());
                if (value != null) {
                    switch (props[j].getType()) {
                        case STRING:
                            int byteLength = DBaseBuffer.getBytes(value.toString()).length;
                            fieldLengths[j] = Math.max(fieldLengths[j], byteLength);
                            fieldDecimals[j] = 0;
                            break;
                        case NUMBER:
                            String valueStr = DBaseBuffer.DECIMAL_FORMAT.format((Number)value);
                            fieldLengths[j] = Math.max(fieldLengths[j], valueStr.length());
                            int decIndex = valueStr.indexOf('.');
                            if (decIndex >= 0) {
                                fieldDecimals[j] = Math.max(fieldDecimals[j], valueStr.length() - decIndex - 1);
                            }
                            break;
                    }
                }
            }
        }
        DBaseFieldDescriptor[] result = new DBaseFieldDescriptor[fieldNames.length];
        for (int i = 0; i < result.length; i += 1) {
            result[i] = new DBaseFieldDescriptor(fieldTypes[i],
                                                 fieldNames[i],
                                                 fieldLengths[i],
                                                 fieldDecimals[i]);
        }
        return result;
    }

    /** */
    private static void storeProjection (Projection projection,
                                         String prjFile) throws IOException {
        FileWriter prj = new FileWriter(new File(prjFile));
        try {
            prj.write(ProjectionFormat.getInstance().format(projection));
        } finally {
            prj.close();
        }
    }

    private static String storeGeoJsonFile(VectorDataset dataset, String filename) throws IOException{
        GeoJsonWriter geojson = new GeoJsonWriter(new RandomAccessFile(filename, "rw"),
                                                  dataset);
        return filename;
    }

    //--------------------------------------------------------------------------
    // GISExtension.Command implementation
    //--------------------------------------------------------------------------

    /** */
    public String getAgentClassString() {
        return "OTPL";
    }

    /** */
    public Syntax getSyntax() {
        return SyntaxJ.commandSyntax(new int[] { Syntax.WildcardType(),
                                                Syntax.StringType() });
    }

    /** */
    public void performInternal (Argument args[], Context context)
            throws ExtensionException, IOException, LogoException {
        Object arg0 = args[0].get();
        String fileName = args[1].getString();
        String dataFile = context.attachCurrentDirectory(fileName);
        boolean geojsonMode = StringUtils.hasFileExtension(fileName, GeoJsonWriter.GEOJSON_EXTENSION ) ||
                              StringUtils.hasFileExtension(fileName, GeoJsonWriter.JSON_EXTENSION);
        if (arg0 instanceof RasterDataset) {
            dataFile = storeAsciiGrid((RasterDataset)arg0, dataFile);
        } else if (arg0 instanceof VectorDataset) {
            if (geojsonMode) {
                dataFile = storeGeoJsonFile((VectorDataset) arg0, dataFile);
            } else {
                dataFile = storeShapefile((VectorDataset)arg0, dataFile);
            }
        } else {
            throw new ExtensionException("not a dataset " + arg0);
        }
        if (!geojsonMode) {
            String prjFile = StringUtils.changeFileExtension(dataFile, "prj");
            if (GISExtension.getState().getProjection() != null) {
                storeProjection(GISExtension.getState().getProjection(), prjFile);
            }
        }
    }
}
