//
// Copyright (c) 2008 Eric Russell. All rights reserved.
//

package org.myworldgis.netlogo;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.util.GeometryTransformer;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jts.io.WKTWriter;
import java.awt.image.BandedSampleModel;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.Writer;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.myworldgis.io.asciigrid.AsciiGridFileReader;
import org.myworldgis.io.asciigrid.AsciiGridFileWriter;
import org.myworldgis.io.shapefile.ESRIShapefileWriter;
import org.myworldgis.projection.Projection;
import org.myworldgis.projection.ProjectionFormat;
import org.myworldgis.util.StringUtils;
import org.nlogo.api.Argument;
import org.nlogo.api.Context;
import org.nlogo.api.ExtensionException;
import org.nlogo.api.LogoException;
import org.nlogo.api.LogoListBuilder;
import org.nlogo.api.Syntax;

/** 
 * 
 */
public abstract strictfp class MyWorld {
    
    //-------------------------------------------------------------------------
    // Class variables
    //-------------------------------------------------------------------------
    
    /** */
    public static final int MYWORLD_PORT = 8328;
    
    /** */
    private static final ProjectionFormat PROJ_FORMAT = new ProjectionFormat();
    
    //--------------------------------------------------------------------------
    // Inner classes
    //--------------------------------------------------------------------------
    
    /** */
    public static final strictfp class GetLayers extends GISExtension.Reporter {
        
        public String getAgentClassString() {
            return "O";
        }
        
        public Syntax getSyntax() {
            return Syntax.reporterSyntax(new int[] { }, Syntax.TYPE_LIST);
        }
        
        @SuppressWarnings("unchecked")
        public Object reportInternal (Argument args[], Context context)
                throws ExtensionException, IOException, LogoException {
            LogoListBuilder result = new LogoListBuilder();
            Socket socket = null;
            try {
                socket = new Socket(InetAddress.getLocalHost(), MYWORLD_PORT);
                Writer out = new OutputStreamWriter(socket.getOutputStream());
                out.write("GET LIST\n");
                out.flush();
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                while (true) {
                    LogoListBuilder layerSpec = new LogoListBuilder();
                    String typeAndName = in.readLine();
                    String projection = in.readLine();
                    String properties = in.readLine();
                    if ((typeAndName == null) || (projection == null) || (properties == null)) {
                        break;
                    }
                    int space = typeAndName.indexOf(' ');
                    String type = typeAndName.substring(0, space);
                    String name = typeAndName.substring(space + 1);
                    layerSpec.add(type);
                    layerSpec.add(name);
                    layerSpec.add(projection);
                    LogoListBuilder propertyNames = new LogoListBuilder();
                    StringReader propsIn = new StringReader(properties);
                    String propName = null;
                    while ((propName = StringUtils.readDelimited(propsIn, ' ')) != null) {
                        propertyNames.add(propName);
                    }
                    layerSpec.add(propertyNames.toLogoList());
                    result.add(layerSpec.toLogoList());
                }
                return result.toLogoList();
            } finally {
                if (socket != null) {
                    try { socket.close(); } catch (IOException e) { }
                }
            } 
        }
    }
    
    /** */
    public static final strictfp class GetDataset extends GISExtension.Reporter {
        
        public String getAgentClassString() {
            return "O";
        }
        
        public Syntax getSyntax() {
            return Syntax.reporterSyntax(new int[] { Syntax.TYPE_NUMBER + Syntax.TYPE_REPEATABLE }, 
                                         Syntax.TYPE_WILDCARD, 
                                         1);
        }
        
        @SuppressWarnings("unchecked")
        public Object reportInternal (Argument args[], Context context)
                throws ExtensionException, IOException, LogoException {
            Socket socket = null;
            try {
                socket = new Socket(InetAddress.getLocalHost(), MYWORLD_PORT);
                Writer out = new OutputStreamWriter(socket.getOutputStream());
                out.write("GET LAYER");
                for (int i = 0; i < args.length; i += 1) {
                    out.write(' ');
                    out.write(Integer.toString(args[i].getIntValue()));
                }   
                out.write("\n");
                out.flush();
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String type = in.readLine();
                if (StringUtils.startsWithIgnoreCase(type, "VECTOR")) {
                    return readVectorDataset(in);
                } else if (StringUtils.startsWithIgnoreCase(type, "RASTER")) {
                    return readRasterDataset(in);
                } else if (StringUtils.startsWithIgnoreCase(type, "ERROR")) {
                    throw new ExtensionException(type.substring(type.indexOf(' ')+1));
                } else {
                    throw new ExtensionException("unrecognized response type: " + type);
                }
            } finally {
                if (socket != null) {
                    try { socket.close(); } catch (IOException e) { }
                }
            }
        }
    }
    
    
    /** */
    public static final strictfp class PutDataset extends GISExtension.Command {
        
        public String getAgentClassString() {
            return "O";
        }
        
        public Syntax getSyntax() {
            return Syntax.commandSyntax(new int[] { Syntax.TYPE_WILDCARD,
                                                    Syntax.TYPE_NUMBER + Syntax.TYPE_REPEATABLE },
                                        1);
        }
        
        @SuppressWarnings("unchecked")
        public void performInternal (Argument args[], Context context)
                throws ExtensionException, IOException, LogoException {
            Object arg0 = args[0].get();
            Socket socket = null;
            try {
                socket = new Socket(InetAddress.getLocalHost(), MYWORLD_PORT);
                Writer out = new OutputStreamWriter(socket.getOutputStream());
                out.write("PUT LAYER");
                for (int i = 1; i < args.length; i += 1) {
                    out.write(' ');
                    out.write(Integer.toString(args[i].getIntValue()));
                }   
                out.write("\n");
                out.flush();
                if (arg0 instanceof VectorDataset) {
                    writeVectorDataset((VectorDataset)arg0, out);
                } else if (arg0 instanceof RasterDataset) {
                    writeRasterDataset((RasterDataset)arg0, out);
                } else {
                    throw new ExtensionException("not a GIS dataset: " + arg0);
                }
                out.flush();

                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String response = in.readLine();
                if (StringUtils.startsWithIgnoreCase(response, "ERROR")) {
                    int space = response.indexOf(' ');
                    throw new IOException(response.substring(space + 1));
                }
            } finally {
                if (socket != null) {
                    try { socket.close(); } catch (IOException e) { }
                }
            } 
        }
    }
    
    //--------------------------------------------------------------------------
    // Class methods
    //--------------------------------------------------------------------------
    
    /** */
    private static VectorDataset readVectorDataset (BufferedReader in)
            throws IOException {
        Projection dstProj = GISExtension.getState().getProjection();
        Projection srcProj = null;
        try {
            srcProj = PROJ_FORMAT.parseProjection(in.readLine());
        } catch (java.text.ParseException e) {
            e.printStackTrace();
        }
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
        VectorDataset.ShapeType shapeType = null;
        String shapeTypeName = in.readLine();
        if (StringUtils.startsWithIgnoreCase(shapeTypeName, "point") ||
            StringUtils.startsWithIgnoreCase(shapeTypeName, "multipoint")) {
            shapeType = VectorDataset.ShapeType.POINT;
        } else if (StringUtils.startsWithIgnoreCase(shapeTypeName, "polyline")) {
            shapeType = VectorDataset.ShapeType.LINE;
        } else if (StringUtils.startsWithIgnoreCase(shapeTypeName, "polygon")) {
            shapeType = VectorDataset.ShapeType.POLYGON;
        }
        List<String> nameList = new LinkedList<String>();
        StringReader propertyNamesIn = new StringReader(in.readLine());
        String propName = null;
        while ((propName = StringUtils.readDelimited(propertyNamesIn, ' ')) != null) {
            nameList.add(propName);
        }
        String[] propertyNames = nameList.toArray(new String[nameList.size()]);
        List<VectorDataset.PropertyType> typeList = new LinkedList<VectorDataset.PropertyType>();
        StringReader propertyTypesIn = new StringReader(in.readLine());
        String typeName = null;
        while ((typeName = StringUtils.readDelimited(propertyTypesIn, ' ')) != null) {
            typeList.add(Enum.valueOf(VectorDataset.PropertyType.class, typeName));
        }
        VectorDataset.PropertyType[] propertyTypes = typeList.toArray(new VectorDataset.PropertyType[typeList.size()]);
        VectorDataset result = new VectorDataset(shapeType, propertyNames, propertyTypes);
        WKTReader gIn = new WKTReader(GISExtension.getState().factory());
        while (true) {
            String dataLine = in.readLine();
            String geomLine = in.readLine();
            if ((dataLine == null) ||
                (geomLine == null) ||
                (dataLine.length() + geomLine.length() == 0)) {
                break;
            }
            Object[] data = new Object[propertyNames.length];
            StringReader dataIn = new StringReader(dataLine);
            String datum = null;
            int index = 0;
            while ((datum = StringUtils.readDelimited(dataIn, ' ')) != null) {
                Object value = datum;
                if (propertyTypes[index] == VectorDataset.PropertyType.NUMBER) {
                    try {
                        value = Double.valueOf(datum); 
                    } catch (NumberFormatException e) {
                        value = null;
                    }
                }
                data[index++] = value;
            }
            try {
                Geometry geom = gIn.read(geomLine);
                if (reproject) {
                    geom = forward.transform(inverse.transform(geom));
                }
                result.add(geom, data);
            } catch (com.vividsolutions.jts.io.ParseException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    /** */
    private static void writeVectorDataset (VectorDataset dataset, Writer out) 
            throws IOException {
        out.write("VECTOR\n");
        if (GISExtension.getState().getProjection() != null) {
            out.write(PROJ_FORMAT.format(GISExtension.getState().getProjection()));
        }
        out.write('\n');
        switch (StoreDataset.esriShapeType(dataset)) {
            case ESRIShapefileWriter.SHAPE_TYPE_POINT:
                out.write("POINT");
                break;
            case ESRIShapefileWriter.SHAPE_TYPE_MULTIPOINT:
                out.write("MULTIPOINT");
                break;
            case ESRIShapefileWriter.SHAPE_TYPE_POLYLINE:
                out.write("POLYLINE");
                break;
            case ESRIShapefileWriter.SHAPE_TYPE_POLYGON:
                out.write("POLYGON");
                break;
            default:
                out.write("NULL");
                break;
        }
        out.write('\n');
        VectorDataset.Property[] properties = dataset.getProperties();
        for (int i = 0; i < properties.length; i += 1) {
            if (i > 0) {
                out.write(' ');
            }
            StringUtils.writeDelimited(properties[i].getName(), ' ', out);
        }
        out.write('\n');
        for (int i = 0; i < properties.length; i += 1) {
            if (i > 0) {
                out.write(' ');
            }
            out.write(properties[i].getType().name());
        }
        out.write('\n');
        WKTWriter gOut = new WKTWriter();
        for (Iterator<VectorFeature> i = dataset.getFeatures().iterator(); i.hasNext(); ) {
            VectorFeature feature = i.next();
            // write record
            for (int j = 0; j < properties.length; j += 1) {
                Object datum = feature.getProperty(properties[j].getName());
                String value = (datum != null) ? datum.toString() : "";
                if (j > 0) {
                    out.write(' ');
                }
                StringUtils.writeDelimited(value, ' ', out);
            }
            out.write('\n');
            // write geometry
            gOut.write(feature.getGeometry(), out);
            out.write('\n');
        }
        out.write('\n');
        out.write('\n');
    }
    
    /** */
    private static RasterDataset readRasterDataset (BufferedReader in)
            throws IOException {
        Projection dstProj = GISExtension.getState().getProjection();
        Projection srcProj = null;
        try {
            srcProj = PROJ_FORMAT.parseProjection(in.readLine());
        } catch (java.text.ParseException e) {
            e.printStackTrace();
        }
        AsciiGridFileReader asc = new AsciiGridFileReader(in);
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
    }
    
    /** */
    private static void writeRasterDataset (RasterDataset dataset, Writer out) 
            throws IOException {
        out.write("RASTER\n");
        if (GISExtension.getState().getProjection() != null) {
            out.write(PROJ_FORMAT.format(GISExtension.getState().getProjection()));
        }
        out.write('\n');
        AsciiGridFileWriter asc = new AsciiGridFileWriter(out);
        GridDimensions dimensions = dataset.getDimensions();
        asc.writeGridInfo(dimensions.getGridSize(), dimensions.getEnvelope(), Double.NaN);
        asc.writeGridData(dataset.getRaster().getDataBuffer());
        out.write('\n');
    }
}
