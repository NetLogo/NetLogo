package org.myworldgis.netlogo;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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
import org.nlogo.core.Syntax;
import org.nlogo.core.SyntaxJ;


/**
 *
 */
public final class StoreDatasetToString extends GISExtension.Reporter {

    //--------------------------------------------------------------------------
    // Class methods
    //--------------------------------------------------------------------------

    // base name used for the files inside a shapefile zip; the loader finds
    // the ".shp" by extension, so the name only matters if the zip is unpacked
    static final String ZIP_ENTRY_BASE_NAME = "dataset";

    /** */
    private static void addZipEntry (ZipOutputStream zip, String extension, byte[] content)
            throws IOException {
        zip.putNextEntry(new ZipEntry(ZIP_ENTRY_BASE_NAME + "." + extension));
        zip.write(content);
        zip.closeEntry();
    }

    /** */
    private static String storeShapefileToZipString (VectorDataset dataset)
            throws ExtensionException, IOException {
        ByteArrayRandomAccessSink shpSink = new ByteArrayRandomAccessSink();
        ByteArrayRandomAccessSink shxSink = new ByteArrayRandomAccessSink();
        ByteArrayRandomAccessSink dbfSink = new ByteArrayRandomAccessSink();
        StoreDataset.storeShapefile(dataset, shpSink, shxSink, dbfSink);
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        ZipOutputStream zip = new ZipOutputStream(bytes);
        try {
            addZipEntry(zip, ESRIShapefileWriter.SHAPEFILE_EXTENSION, shpSink.toByteArray());
            addZipEntry(zip, ESRIShapeIndexWriter.SHAPE_INDEX_EXTENSION, shxSink.toByteArray());
            addZipEntry(zip, DBaseFileWriter.DBASE_FILE_EXTENSION, dbfSink.toByteArray());
            Projection projection = GISExtension.getState().getProjection();
            if (projection != null) {
                addZipEntry(zip, "prj",
                            ProjectionFormat.getInstance().format(projection).getBytes(StandardCharsets.UTF_8));
            }
        } finally {
            zip.close();
        }
        return Base64.getEncoder().encodeToString(bytes.toByteArray());
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
                                      Syntax.StringType());
    }

    /** */
    public Object reportInternal (Argument[] args, Context context)
            throws ExtensionException, IOException, LogoException {
        Object arg0 = args[0].get();
        String format = LoadDatasetFromString.normalizeFormat(args[1].getString());
        if (format.equals(GeoJsonWriter.GEOJSON_EXTENSION) ||
            format.equals(GeoJsonWriter.JSON_EXTENSION)) {
            if (!(arg0 instanceof VectorDataset)) {
                throw new ExtensionException("expected a VectorDataset to store as " + format);
            }
            return new GeoJsonWriter((VectorDataset)arg0).getJsonString();
        } else if (format.equals(AsciiGridFileWriter.ASCII_GRID_FILE_EXTENSION_1) ||
                   format.equals(AsciiGridFileWriter.ASCII_GRID_FILE_EXTENSION_2)) {
            if (!(arg0 instanceof RasterDataset)) {
                throw new ExtensionException("expected a RasterDataset to store as " + format);
            }
            StringWriter out = new StringWriter();
            StoreDataset.storeAsciiGrid((RasterDataset)arg0, out);
            return out.toString();
        } else if (format.equals(ESRIShapefileWriter.SHAPEFILE_EXTENSION)) {
            if (!(arg0 instanceof VectorDataset)) {
                throw new ExtensionException("expected a VectorDataset to store as " + format);
            }
            return storeShapefileToZipString((VectorDataset)arg0);
        } else {
            throw new ExtensionException("unsupported data format " + args[1].getString());
        }
    }
}
