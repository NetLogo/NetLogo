//
// Copyright (c) 2007 Eric Russell. All rights reserved.
//

package org.myworldgis.netlogo;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.util.GeometryTransformer;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.image.BandedSampleModel;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferDouble;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.awt.image.renderable.ParameterBlock;
import java.text.ParseException;
import javax.media.jai.BorderExtender;
import javax.media.jai.BorderExtenderConstant;
import javax.media.jai.Interpolation;
import javax.media.jai.InterpolationBicubic;
import javax.media.jai.InterpolationBicubic2;
import javax.media.jai.InterpolationBilinear;
import javax.media.jai.InterpolationNearest;
import javax.media.jai.JAI;
import javax.media.jai.KernelJAI;
import javax.media.jai.RenderedOp;
import javax.media.jai.TiledImage;
import org.myworldgis.projection.Projection;
import org.myworldgis.util.RasterUtils;
import org.myworldgis.util.ValueColorModel;
import org.nlogo.api.Argument;
import org.nlogo.api.Context;
import org.nlogo.api.Dump;
import org.nlogo.api.ExtensionException;
import org.nlogo.api.LogoException;
import org.nlogo.core.Syntax;
import org.nlogo.core.SyntaxJ;


/**
 *
 */
public class RasterDataset extends Dataset {

    //--------------------------------------------------------------------------
    // Inner classes
    //--------------------------------------------------------------------------

    /** */
    public static final class New extends GISExtension.Reporter {

        public String getAgentClassString() {
            return "OTPL";
        }

        public Syntax getSyntax() {
            return SyntaxJ.reporterSyntax(new int[] { Syntax.NumberType(),
                                                     Syntax.NumberType(),
                                                     Syntax.ListType() },
                                         Syntax.WildcardType());
        }

        public Object reportInternal (Argument args[], Context context)
                throws ExtensionException, LogoException, ParseException {
            int width = args[0].getIntValue();
            int height = args[1].getIntValue();
            Envelope envelope = EnvelopeLogoListFormat.getInstance().parse(args[2].getList());
            GridDimensions dimensions = new GridDimensions(new Dimension(width, height), envelope);
            DataBuffer data = new DataBufferDouble(width * height);
            BandedSampleModel sampleModel = new BandedSampleModel(data.getDataType(),width, height, 1);
            WritableRaster raster = Raster.createWritableRaster(sampleModel, data, null);
            return new RasterDataset(dimensions, raster);
        }
    }

    /** */
    public static final class GetHeight extends GISExtension.Reporter {

        public String getAgentClassString() {
            return "OTPL";
        }

        public Syntax getSyntax() {
            return SyntaxJ.reporterSyntax(new int[] { Syntax.WildcardType() },
                                         Syntax.NumberType());
        }

        public Object reportInternal (Argument args[], Context context)
                throws ExtensionException, LogoException {
            return Double.valueOf(getDataset(args[0]).getDimensions().getGridHeight());
        }
    }

    /** */
    public static final class GetWidth extends GISExtension.Reporter {

        public String getAgentClassString() {
            return "OTPL";
        }

        public Syntax getSyntax() {
            return SyntaxJ.reporterSyntax(new int[] { Syntax.WildcardType() },
                                         Syntax.NumberType());
        }

        public Object reportInternal (Argument args[], Context context)
                throws ExtensionException, LogoException {
            return Double.valueOf(getDataset(args[0]).getDimensions().getGridWidth());
        }
    }

    /** */
    public static final class GetValue extends GISExtension.Reporter {

        public String getAgentClassString() {
            return "OTPL";
        }

        public Syntax getSyntax() {
            return SyntaxJ.reporterSyntax(new int[] { Syntax.WildcardType(),
                                                     Syntax.NumberType(),
                                                     Syntax.NumberType() },
                                         Syntax.NumberType());
        }

        public Object reportInternal (Argument args[], Context context)
                throws ExtensionException, LogoException {
            RasterDataset dataset = getDataset(args[0]);
            int col = args[1].getIntValue();
            int row = args[2].getIntValue();
            return Double.valueOf(dataset.getRaster().getSampleDouble(col, row, 0));
        }
    }

    /** */
    public static final class SetValue extends GISExtension.Command {

        public String getAgentClassString() {
            return "OTPL";
        }

        public Syntax getSyntax() {
            return SyntaxJ.commandSyntax(new int[] { Syntax.WildcardType(),
                                                    Syntax.NumberType(),
                                                    Syntax.NumberType(),
                                                    Syntax.NumberType() });
        }

        public void performInternal (Argument args[], Context context)
                throws ExtensionException, LogoException {
            RasterDataset dataset = getDataset(args[0]);
            int col = args[1].getIntValue();
            int row = args[2].getIntValue();
            double value = args[3].getDoubleValue();
            dataset.getRaster().setSample(col, row, 0, value);
        }
    }

    /** */
    public static final class GetMinimum extends GISExtension.Reporter {

        public String getAgentClassString() {
            return "OTPL";
        }

        public Syntax getSyntax() {
            return SyntaxJ.reporterSyntax(new int[] { Syntax.WildcardType() },
                                         Syntax.NumberType());
        }

        public Object reportInternal (Argument args[], Context context)
                throws ExtensionException, LogoException {
            DataBuffer buffer = getDataset(args[0]).getRaster().getDataBuffer();
            double result = Double.MAX_VALUE;
            for (int i = 0; i < buffer.getSize(); i += 1) {
                double d = buffer.getElemDouble(i);
                if ((d < result) && (!Double.isNaN(d))) {
                    result = d;
                }
            }
            return Double.valueOf(result);
        }
    }

    /** */
    public static final class GetMaximum extends GISExtension.Reporter {

        public String getAgentClassString() {
            return "OTPL";
        }

        public Syntax getSyntax() {
            return SyntaxJ.reporterSyntax(new int[] { Syntax.WildcardType() },
                                         Syntax.NumberType());
        }

        public Object reportInternal (Argument args[], Context context)
                throws ExtensionException, LogoException {
            DataBuffer buffer = getDataset(args[0]).getRaster().getDataBuffer();
            double result = -Double.MAX_VALUE;
            for (int i = 0; i < buffer.getSize(); i += 1) {
                double d = buffer.getElemDouble(i);
                if ((d > result) && (!Double.isNaN(d))) {
                    result = d;
                }
            }
            return Double.valueOf(result);
        }
    }

    /** */
    public static final class GetInterpolation extends GISExtension.Reporter {

        public String getAgentClassString() {
            return "OTPL";
        }

        public Syntax getSyntax() {
            return SyntaxJ.reporterSyntax(new int[] { Syntax.WildcardType() },
                                         Syntax.StringType());
        }

        public Object reportInternal (Argument args[], Context context)
                throws ExtensionException, LogoException {
            Interpolation interp = getDataset(args[0]).getInterpolation();
            if (interp instanceof InterpolationNearest) {
                return "NEAREST_NEIGHBOR";
            } else if (interp instanceof InterpolationBilinear) {
                return "BILINEAR";
            } else if (interp instanceof InterpolationBicubic) {
                return "BICUBIC";
            } else if (interp instanceof InterpolationBicubic2) {
                return "BICUBIC_2";
            } else {
                throw new ExtensionException("Unknown interpolation type: " + Dump.logoObject(interp));
            }
        }
    }

    /** */
    public static final class SetInterpolation extends GISExtension.Command {

        public String getAgentClassString() {
            return "OTPL";
        }

        public Syntax getSyntax() {
            return SyntaxJ.commandSyntax(new int[] { Syntax.WildcardType(),
                                                    Syntax.StringType() });
        }

        public void performInternal (Argument args[], Context context)
                throws ExtensionException, LogoException {
            Object obj = args[0].get();
            if (obj instanceof RasterDataset) {
                RasterDataset dataset = (RasterDataset)obj;
                String newInterpolation = args[1].getString();
                if (newInterpolation.equalsIgnoreCase("NEAREST_NEIGHBOR")) {
                    dataset.setInterpolation(Interpolation.INTERP_NEAREST);
                } else if (newInterpolation.equalsIgnoreCase("BILINEAR")) {
                    dataset.setInterpolation(Interpolation.INTERP_BILINEAR);
                } else if (newInterpolation.equalsIgnoreCase("BICUBIC")) {
                    dataset.setInterpolation(Interpolation.INTERP_BICUBIC);
                } else if (newInterpolation.equalsIgnoreCase("BICUBIC_2")) {
                    dataset.setInterpolation(Interpolation.INTERP_BICUBIC_2);
                } else {
                    throw new ExtensionException("Unknown interpolation type: " + Dump.logoObject(newInterpolation));
                }
            } else {
                throw new ExtensionException("not a RasterDataset: " + obj);
            }
        }
    }

    //--------------------------------------------------------------------------
    // Class methods
    //--------------------------------------------------------------------------

    /** */
    static RasterDataset getDataset (Argument arg)
            throws ExtensionException, LogoException {
        Object obj = arg.get();
        if (obj instanceof RasterDataset) {
            return (RasterDataset)obj;
        } else {
            throw new ExtensionException("not a RasterDataset: " + obj);
        }
    }

    /** */
    static RenderedImage createRendering (RenderedOp op, ColorModel cm) {
        WritableRaster wr = op.copyData();
        TiledImage ti = new TiledImage(wr.getMinX(), wr.getMinY(),
                                       wr.getWidth(), wr.getHeight(),
                                       0, 0,
                                       cm.createCompatibleSampleModel(wr.getWidth(), wr.getHeight()),
                                       cm);
        ti.setData(wr);
        return ti;
    }

    //--------------------------------------------------------------------------
    // Instance variables
    //--------------------------------------------------------------------------

    /** */
    private GridDimensions _dimensions;

    /** */
    private WritableRaster _raster;

    /** */
    private Interpolation _interpolation;

    /** */
    private double[][] _interpArray;

    //--------------------------------------------------------------------------
    // Constructors
    //--------------------------------------------------------------------------

    /** */
    public RasterDataset (GridDimensions dimensions, WritableRaster raster) {
        super("RASTER");
        _dimensions = dimensions;
        _raster = raster;
        _interpolation = Interpolation.getInstance(Interpolation.INTERP_NEAREST);
        _interpArray = new double[_interpolation.getHeight()][_interpolation.getWidth()];
        GISExtension.getState().datasetLoadNotify();
    }

    /** */
    public RasterDataset (WritableRaster raster,
                          GridDimensions srcDimensions,
                          Projection srcProj,
                          Projection dstProj) {
        super("RASTER");
        GeometryFactory factory = GISExtension.getState().factory();
        GeometryTransformer srcToGeog = srcProj.getInverseTransformer();
        GeometryTransformer geogToDst = dstProj.getForwardTransformer();
        int minCol = Integer.MAX_VALUE;
        int maxCol = -1;
        int minRow = Integer.MAX_VALUE;
        int maxRow = -1;
        Envelope newEnvelope = new Envelope();
        for (int col = 0; col <= srcDimensions.getGridWidth(); col += 2) {
            for (int row = 0; row <= srcDimensions.getGridHeight(); row += 2) {
                Point src = factory.createPoint(new Coordinate(srcDimensions.getColumnLeft(col),
                                                               srcDimensions.getRowBottom(row)));
                Point dest = (Point)geogToDst.transform(srcToGeog.transform(src));
                if (!dest.isEmpty()) {
                    if (col < minCol) {
                        minCol = col;
                    }
                    if (col > maxCol) {
                        maxCol = col;
                    }
                    if (row < minRow) {
                        minRow = row;
                    }
                    if (row > maxRow) {
                        maxRow = row;
                    }
                    newEnvelope.expandToInclude(dest.getCoordinate());
                }
            }
        }
        double scale = StrictMath.min((maxCol - minCol) / newEnvelope.getWidth(),
                                      (maxRow - minRow) / newEnvelope.getHeight());
        _dimensions = new GridDimensions(new Dimension((int)(scale * newEnvelope.getWidth()),
                                                                        (int)(scale * newEnvelope.getHeight())),
                                                          newEnvelope);
        ColorModel srcCM = new ValueColorModel(raster);
        BufferedImage img = new BufferedImage(srcCM, raster, false, null);
        RenderedImage dstImage = RasterUtils.reproject(img,
                                                       srcDimensions,
                                                       srcProj,
                                                       _dimensions,
                                                       dstProj,
                                                       factory,
                                                       new double[] { Double.NaN });
        _raster = (WritableRaster)dstImage.getData();
        _interpolation = Interpolation.getInstance(Interpolation.INTERP_NEAREST);
        _interpArray = new double[_interpolation.getHeight()][_interpolation.getWidth()];
        GISExtension.getState().datasetLoadNotify();
    }

    //--------------------------------------------------------------------------
    // Instance methods
    //--------------------------------------------------------------------------

    /** */
    public GridDimensions getDimensions () {
        return _dimensions;
    }

    /** */
    public WritableRaster getRaster () {
        return _raster;
    }

    /** */
    public Interpolation getInterpolation () {
        return _interpolation;
    }

    /** */
    public void setInterpolation (int interpolationType) {
        _interpolation = Interpolation.getInstance(interpolationType);
        _interpArray = new double[_interpolation.getHeight()][_interpolation.getWidth()];
    }

    /** */
    public double getValue (Coordinate gisSpLocation) {
        Coordinate gridSpLocation = _dimensions.gisToGrid(gisSpLocation, null);
        if (Double.isNaN(gridSpLocation.x) || Double.isNaN(gridSpLocation.y)) {
            return Double.NaN;
        }
        int rx = (int)StrictMath.floor(gridSpLocation.x);
        int ry = (int)StrictMath.floor(gridSpLocation.y);
        float xfrac = (float)(gridSpLocation.x - rx);
        float yfrac = (float)(gridSpLocation.y - ry);
        for (int iy = 0; iy < _interpArray.length; iy += 1) {
            int sy = ry + iy - _interpolation.getTopPadding();
            if ((sy >= 0) && (sy < _dimensions.getGridHeight())) {
                // NOTE: reverse y axis. I hate raster space!
                sy = _dimensions.getGridHeight() - sy - 1;
                for (int ix = 0; ix < _interpArray[iy].length; ix += 1) {
                    int sx = rx + ix - _interpolation.getLeftPadding();
                    if ((sx >= 0) && (sx < _dimensions.getGridWidth())) {
                        _interpArray[iy][ix] = _raster.getSampleDouble(sx, sy, 0);
                    } else {
                        _interpArray[iy][ix] = Double.NaN;
                    }
                }
            } else {
                for (int ix = 0; ix < _interpArray[iy].length; ix += 1) {
                    _interpArray[iy][ix] = Double.NaN;
                }
            }
        }
        return _interpolation.interpolate(_interpArray, xfrac, yfrac);
    }

    /** */
    public double getValue (Envelope gisSpEnvelope) {
        Coordinate gisBL = new Coordinate(gisSpEnvelope.getMinX(), gisSpEnvelope.getMinY());
        Coordinate gridBL = _dimensions.gisToGrid(gisBL, gisBL);
        Coordinate gisTR = new Coordinate(gisSpEnvelope.getMaxX(), gisSpEnvelope.getMaxY());
        Coordinate gridTR = _dimensions.gisToGrid(gisTR, gisTR);
        int minY, maxY;
        if (Double.isNaN(gridBL.y) && Double.isNaN(gridTR.y)) {
            return GISExtension.MISSING_VALUE;
        } else if (Double.isNaN(gridBL.y)) {
            minY = 0;
            maxY = (int)StrictMath.ceil(gridTR.y);
        } else if (Double.isNaN(gridTR.y)) {
            minY = (int)StrictMath.floor(gridBL.y);
            maxY = _dimensions.getGridHeight();
        } else {
            minY = (int)StrictMath.floor(gridBL.y);
            maxY = (int)StrictMath.ceil(gridTR.y);
        }
        int minX, maxX;
        if (Double.isNaN(gridBL.x) && Double.isNaN(gridTR.x)) {
            return GISExtension.MISSING_VALUE;
        } else if (Double.isNaN(gridBL.x)) {
            minX = 0;
            maxX = (int)StrictMath.ceil(gridTR.x);
        } else if (Double.isNaN(gridTR.x)) {
            minX = (int)StrictMath.floor(gridBL.x);
            maxX = _dimensions.getGridWidth();
        } else {
            minX = (int)StrictMath.floor(gridBL.x);
            maxX = (int)StrictMath.ceil(gridTR.x);
        }
        double sum = 0.0;
        int count = 0;
        for (int y = minY; y < maxY; y += 1) {
            for (int x = minX; x < maxX; x += 1) {
                sum += _raster.getSampleDouble(x, _dimensions.getGridHeight() - y - 1, 0);
                count += 1;
            }
        }
        if (count > 0) {
            return Double.valueOf(sum / count);
        } else {
            return GISExtension.MISSING_VALUE;
        }
    }

    /** */
    public RasterDataset resample (GridDimensions toDimensions) {
        // Short circuit if possible
        if (toDimensions.equals(_dimensions)) {
            return this;
        }

        // Compute the bounds of the new raster in local raster coordinates.
        double targetLeft = (toDimensions.getLeft() - _dimensions.getLeft()) / _dimensions.getCellWidth();
        double targetRight = (toDimensions.getRight() - _dimensions.getLeft()) / _dimensions.getCellWidth();
        double targetBottom = (toDimensions.getBottom() - _dimensions.getBottom()) / _dimensions.getCellHeight();
        double targetTop = (toDimensions.getTop() - _dimensions.getBottom()) / _dimensions.getCellHeight();

        ColorModel srcCM = new ValueColorModel(_raster);
        RenderedImage srcImg = new BufferedImage(srcCM, _raster, false, null);

        // If any of the bounds of the new raster lie outside the bounds
        // of the current raster, extend the current raster with NaN's
        // to cover all of the requested area
        Insets border = new Insets(0, 0, 0, 0);
        BorderExtender borderExtender = new BorderExtenderConstant(new double[] { Double.NaN });
        if (targetLeft < 0.0) {
            border.left = -(int)StrictMath.floor(targetLeft);
        }
        if (targetRight > _dimensions.getGridWidth()) {
            border.right = (int)StrictMath.ceil(targetRight) - _dimensions.getGridWidth();
        }
        if (targetBottom < 0.0) {
            border.bottom = -(int)StrictMath.floor(targetBottom);
        }
        if (targetTop > _dimensions.getGridHeight()) {
            border.top = (int)StrictMath.ceil(targetTop) - _dimensions.getGridHeight();
        }

        if ((border.left > 0) || (border.right > 0) || (border.bottom > 0) || (border.top > 0)) {
            ParameterBlock pb = new ParameterBlock();
            pb.addSource(srcImg);
            pb.add(Integer.valueOf(border.left));
            pb.add(Integer.valueOf(border.right));
            pb.add(Integer.valueOf(border.top));
            pb.add(Integer.valueOf(border.bottom));
            pb.add(borderExtender);
            //HACK- work around for bug in JAI
            srcImg = createRendering(JAI.create("border", pb), srcCM);
        }

        double flippedTopY = (_dimensions.getGridHeight() + border.top + border.bottom) - targetTop;

        if ((targetLeft > 0.0) ||
            (targetRight < _dimensions.getGridWidth()) ||
            (targetBottom > 0.0) ||
            (targetTop < _dimensions.getGridHeight())) {

            float left = Math.max((float)targetLeft, 0f);
            float bottom = Math.max((float)flippedTopY, 0f);
            float right = Math.min((float)(targetRight - targetLeft), _dimensions.getGridWidth()-1);
            float top = Math.min((float)(targetTop - targetBottom), _dimensions.getGridHeight()-1);

            ParameterBlock pb = new ParameterBlock();
            pb.addSource(srcImg);
            pb.add(Float.valueOf(left));
            pb.add(Float.valueOf(bottom));
            pb.add(Float.valueOf(right));
            pb.add(Float.valueOf(top));
            pb.add(borderExtender);
            //HACK- work around for bug in JAI
            srcImg = createRendering(JAI.create("crop", pb), srcCM);
        }

        double scaleX = toDimensions.getGridWidth() / (double)srcImg.getWidth();
        double scaleY = toDimensions.getGridHeight() / (double)srcImg.getHeight();

        // Remember, translation values are in the RESAMPLED raster's coordinate
        // system,not the current raster's coordinate system.
        double transX = (_dimensions.getLeft() - toDimensions.getLeft()) / toDimensions.getCellWidth();
        double transY = (_dimensions.getTop() - toDimensions.getBottom()) / toDimensions.getCellHeight();
        transY = toDimensions.getGridHeight() - transY;

        ParameterBlock pb = new ParameterBlock();
        pb.addSource(srcImg);
        pb.add(Float.valueOf((float)scaleX));
        pb.add(Float.valueOf((float)scaleY));
        pb.add(Float.valueOf((float)transX));
        pb.add(Float.valueOf((float)transY));
        pb.add(_interpolation);
        RenderingHints hints = new RenderingHints(JAI.KEY_BORDER_EXTENDER, borderExtender);
        RenderedOp dstImg = JAI.create("scale", pb, hints);

        WritableRaster wr = srcCM.createCompatibleWritableRaster(dstImg.getWidth(), dstImg.getHeight());
        dstImg.copyData(wr);
        return new RasterDataset(toDimensions, wr);
    }

    /** */
    public RasterDataset convolve (KernelJAI kernel) {
        ColorModel srcCM = new ValueColorModel(_raster);
        Object srcImg = new BufferedImage(srcCM, _raster, false, null);
        BorderExtender borderExtender = new BorderExtenderConstant(new double[] { Double.NaN });
        ParameterBlock pb = new ParameterBlock();
        pb.addSource(srcImg);
        pb.add(kernel);
        RenderingHints hints = new RenderingHints(JAI.KEY_BORDER_EXTENDER, borderExtender);
        RenderedOp dstImg = JAI.create("convolve", pb, hints);
        WritableRaster wr = srcCM.createCompatibleWritableRaster(dstImg.getWidth(), dstImg.getHeight());
        dstImg.copyData(wr);
        return new RasterDataset(_dimensions, wr);
    }

    //--------------------------------------------------------------------------
    // Dataset implementation
    //--------------------------------------------------------------------------

    /** */
    public Envelope getEnvelope () {
        return _dimensions.getEnvelope();
    }

    //--------------------------------------------------------------------------
    // ExtensionObject implementation
    //--------------------------------------------------------------------------

    /**
     * Returns a string representation of the object.  If readable is
     * true, it should be possible read it as NL code.
     *
     **/
    public String dump (boolean readable, boolean exporting, boolean reference ) {
        return "";
    }

    /** */
    public String getNLTypeName() {
        return "RasterDataset";
    }

    /** */
    public boolean recursivelyEqual (Object obj) {
        if (obj instanceof VectorDataset) {
            RasterDataset rd = (RasterDataset)obj;
            return rd == this;
        } else {
            return false;
        }
    }
}
