//
// Copyright (c) 2008 Eric Russell. All rights reserved.
//

package org.myworldgis.util;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.util.GeometryTransformer;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.awt.image.renderable.ParameterBlock;
import javax.media.jai.ImageLayout;
import javax.media.jai.InterpolationNearest;
import javax.media.jai.JAI;
import javax.media.jai.RenderedOp;
import javax.media.jai.WarpGrid;
import org.myworldgis.netlogo.GridDimensions;
import org.myworldgis.projection.Projection;


/**
 * 
 */
public final strictfp class RasterUtils {

    //--------------------------------------------------------------------------
    // Class methods
    //--------------------------------------------------------------------------
    
    /** */
    public static RenderedImage reproject (BufferedImage image,
                                           GridDimensions srcDimensions,
                                           Projection srcProj,
                                           GridDimensions dstDimensions,
                                           Projection dstProj,
                                           GeometryFactory factory,
                                           Object missingPixel) {
        GeometryTransformer geogToSrc = srcProj.getForwardTransformer();
        GeometryTransformer dstToGeog = dstProj.getInverseTransformer();
        // We "fudge" the x and y values slightly along the 
        // edges of the grid, to reduce the likelihood that 
        // we'll go outside the projection domain
        double yFudge = (dstDimensions.getCellHeight() * 0.01);
        double xFudge = (dstDimensions.getCellWidth() * 0.01);
        int xNumCells = dstDimensions.getGridWidth();
        int yNumCells = dstDimensions.getGridHeight();
        float[] warpPositions = new float[2 * (xNumCells + 1) * (yNumCells + 1)];
        int index = 0;
        // Note that y grid coordinates are reversed, because 
        // in the raster coordinate system y values INcrease as
        // you go downward, and in the GIS coordinate system 
        // y values DEcrease as you go downward.
        for (int y = 0; y <= yNumCells; y += 1) {
            double dstY = dstDimensions.getRowBottom(yNumCells - y);
            if (y == 0) {
                dstY += yFudge;
            } else if (y == yNumCells) {
                dstY -= yFudge;
            }
            for (int x = 0; x <= xNumCells; x += 1) {
                double dstX = dstDimensions.getColumnLeft(x);
                if (x == 0) {
                    dstX += xFudge;
                } else if (x == xNumCells) {
                    dstX -= xFudge;
                }
                Point dest = factory.createPoint(new Coordinate(dstX, dstY));
                Point src = (Point)geogToSrc.transform(dstToGeog.transform(dest));
                if (!src.isEmpty()) {
                    Coordinate c = srcDimensions.gisToGrid(src.getCoordinate(), null);
                    warpPositions[index++] = (float)c.x;
                    warpPositions[index++] = (float)(srcDimensions.getGridHeight() - c.y);
                } else {
                    warpPositions[index++] = Float.NaN;
                    warpPositions[index++] = Float.NaN;
                }
            }
        }
        
        WarpGrid warp = new WarpGrid(0, 1, xNumCells, 0, 1, yNumCells, warpPositions);
        ParameterBlock pb = new ParameterBlock();
        pb.addSource(image);
        pb.add(warp);
        pb.add(new InterpolationNearest());
        
        ImageLayout layout = new ImageLayout();
        layout.setMinX(0);
        layout.setMinY(0);
        layout.setWidth(dstDimensions.getGridWidth());
        layout.setHeight(dstDimensions.getGridHeight());
        RenderingHints rh = new RenderingHints(JAI.KEY_IMAGE_LAYOUT, layout);
        
        RenderedOp result = JAI.create("warp", pb, rh);
        
        if (missingPixel == null) {
            return result;
        } else {
            ColorModel srcCM = image.getColorModel();
            WritableRaster wr = srcCM.createCompatibleWritableRaster(result.getWidth(), 
                                                                     result.getHeight());
            result.copyData(wr);
            // Copy NaNs from warpPositions to the raster's pixels
            int wpIndex = 0;
            for (int y = 0; y < yNumCells; y += 1) {
                for (int x = 0; x < xNumCells; x += 1) {
                    if (Float.isNaN(warpPositions[wpIndex]) ||
                        Float.isNaN(warpPositions[wpIndex+1])) {
                        wr.setDataElements(x, y, missingPixel);
                    }
                    wpIndex += 2;
                }
                wpIndex += 2;
            }
            return new BufferedImage(srcCM, wr, false, null);
        }
    }
}
