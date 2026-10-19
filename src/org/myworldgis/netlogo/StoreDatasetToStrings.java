package org.myworldgis.netlogo;

import java.io.IOException;
import java.util.Base64;

import org.myworldgis.io.ByteArrayRandomAccessSink;
import org.myworldgis.io.asciigrid.AsciiGridFileWriter;
import org.myworldgis.io.geojson.GeoJsonWriter;
import org.myworldgis.io.shapefile.DBaseFileWriter;
import org.myworldgis.io.shapefile.ESRIShapeIndexWriter;
import org.myworldgis.io.shapefile.ESRIShapefileWriter;
import org.myworldgis.projection.Projection;
import org.myworldgis.projection.ProjectionFormat;
import org.nlogo.api.Argument;
import org.nlogo.api.Context;
import org.nlogo.api.ExtensionException;
import org.nlogo.api.LogoException;
import org.nlogo.api.LogoListBuilder;
import org.nlogo.core.LogoList;
import org.nlogo.core.Syntax;
import org.nlogo.core.SyntaxJ;


/**
 *
 */
public final class StoreDatasetToStrings extends GISExtension.Reporter {

    //--------------------------------------------------------------------------
    // Class methods
    //--------------------------------------------------------------------------

    /** */
    private static LogoList pair (String extension, String content) {
        LogoListBuilder result = new LogoListBuilder();
        result.add(extension);
        result.add(content);
        return result.toLogoList();
    }

    /** */
    private static String base64 (ByteArrayRandomAccessSink sink) {
        return Base64.getEncoder().encodeToString(sink.toByteArray());
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
        return SyntaxJ.reporterSyntax(new int[] { Syntax.WildcardType(),
                                                  Syntax.StringType() },
                                      Syntax.ListType());
    }

    /** */
    public Object reportInternal (Argument[] args, Context context)
            throws ExtensionException, IOException, LogoException {
        Object arg0 = args[0].get();
        String format = LoadDatasetFromString.normalizeFormat(args[1].getString());
        if (format.equals(ESRIShapefileWriter.SHAPEFILE_EXTENSION)) {
            if (!(arg0 instanceof VectorDataset)) {
                throw new ExtensionException("expected a VectorDataset to store as " + format);
            }
            ByteArrayRandomAccessSink shpSink = new ByteArrayRandomAccessSink();
            ByteArrayRandomAccessSink shxSink = new ByteArrayRandomAccessSink();
            ByteArrayRandomAccessSink dbfSink = new ByteArrayRandomAccessSink();
            StoreDataset.storeShapefile((VectorDataset)arg0, shpSink, shxSink, dbfSink);
            LogoListBuilder result = new LogoListBuilder();
            result.add(pair(ESRIShapefileWriter.SHAPEFILE_EXTENSION, base64(shpSink)));
            result.add(pair(ESRIShapeIndexWriter.SHAPE_INDEX_EXTENSION, base64(shxSink)));
            result.add(pair(DBaseFileWriter.DBASE_FILE_EXTENSION, base64(dbfSink)));
            Projection projection = GISExtension.getState().getProjection();
            if (projection != null) {
                result.add(pair("prj", ProjectionFormat.getInstance().format(projection)));
            }
            return result.toLogoList();
        } else if (format.equals(GeoJsonWriter.GEOJSON_EXTENSION) ||
                   format.equals(GeoJsonWriter.JSON_EXTENSION) ||
                   format.equals(AsciiGridFileWriter.ASCII_GRID_FILE_EXTENSION_1) ||
                   format.equals(AsciiGridFileWriter.ASCII_GRID_FILE_EXTENSION_2)) {
            throw new ExtensionException(format + " produces a single string; use gis:store-dataset-to-string");
        } else {
            throw new ExtensionException("unsupported data format " + args[1].getString());
        }
    }
}
