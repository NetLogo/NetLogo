//
// Copyright (c) 2007 Eric Russell. All rights reserved.
//

package org.myworldgis.netlogo;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.Iterator;
import org.myworldgis.util.RangeColorModel;
import org.nlogo.api.Argument;
import org.nlogo.api.Context;
import org.nlogo.api.Dump;
import org.nlogo.api.ExtensionException;
import org.nlogo.api.LogoException;
import org.nlogo.api.Syntax;
import org.nlogo.api.World;


/**
 * 
 */
public abstract strictfp class Painting extends GISExtension.Command {
    
    //--------------------------------------------------------------------------
    // Inner classes
    //--------------------------------------------------------------------------
    
    /** */
    public static final strictfp class GetColor extends GISExtension.Reporter {
        
        public String getAgentClassString() {
            return "OTPL";
        }
        
        public Syntax getSyntax() {
            return Syntax.reporterSyntax(new int[] { }, Syntax.TYPE_NUMBER | Syntax.TYPE_LIST );
        }
        
        public Object reportInternal (Argument args[], Context context)
                throws ExtensionException, LogoException {
            return GISExtension.getState().getNetLogoColor();
        }
    }
    
    /** */
    public static final strictfp class SetColor extends GISExtension.Command {
        
        public String getAgentClassString() {
            return "OTLP";
        }

        public Syntax getSyntax() {
            return Syntax.commandSyntax(new int[] { Syntax.TYPE_NUMBER | Syntax.TYPE_LIST });
        }
        
        public void performInternal (Argument args[], Context context) 
                throws ExtensionException , LogoException {
            GISExtension.getState().setNetLogoColor(args[0].get());
        }
    }
    
    /** */
    private static abstract strictfp class VectorCommand extends GISExtension.Command {
        
        public String getAgentClassString() {
            return "OTLP";
        }
    
        public Syntax getSyntax() {
            return Syntax.commandSyntax(new int[] { Syntax.TYPE_WILDCARD, Syntax.TYPE_NUMBER });
        }
        
        public void performInternal (Argument args[], Context context) 
                throws ExtensionException , LogoException {
            Object arg = args[0].get();
            double thickness = args[1].getDoubleValue();
            BufferedImage drawing = context.getDrawing();
            Graphics2D g = (Graphics2D)drawing.getGraphics();
            try {
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); 
                g.setColor(GISExtension.getState().getColor());
                g.setTransform(getTransform(context.getAgent().world(),
                                            drawing.getWidth(),
                                            drawing.getHeight()));
                if (arg instanceof VectorDataset) {
                    for (Iterator<VectorFeature> i = ((VectorDataset)arg).getFeatures().iterator(); i.hasNext();) {
                        paint(i.next().getGeometry(), g, thickness);
                    }
                } else if (arg instanceof VectorFeature) {
                    paint(((VectorFeature)arg).getGeometry(), g, thickness);
                } else {
                    throw new ExtensionException("not a VectorDataset or VectorFeature: " + arg);
                }
            } finally {
                g.dispose();
            }
        }
        
        protected abstract void paint (Geometry geom, Graphics2D g, double thickness);
    }
    
    /** */
    public static final strictfp class DrawVector extends VectorCommand {
        
        protected void paint (Geometry geom, Graphics2D g, double thickness) {
            AffineTransform t = g.getTransform();
            double unitsPerPixel = 1.0 / StrictMath.max(t.getScaleX(), t.getScaleY());
            float segmentRadius = (float)(unitsPerPixel * thickness);
            g.setStroke(new BasicStroke(segmentRadius, 
                                        BasicStroke.CAP_BUTT,
                                        BasicStroke.JOIN_BEVEL)); 
            g.draw(toShape(geom, segmentRadius));
        }
    }
    
    /** */
    public static final strictfp class FillVector extends VectorCommand {

        protected void paint (Geometry geom, Graphics2D g, double thickness) {
            AffineTransform t = g.getTransform();
            double unitsPerPixel = 1.0 / StrictMath.max(t.getScaleX(), t.getScaleY());
            float segmentRadius = (float)(unitsPerPixel * thickness);
            g.setStroke(new BasicStroke(segmentRadius, 
                                        BasicStroke.CAP_BUTT,
                                        BasicStroke.JOIN_BEVEL)); 
            g.fill(toShape(geom, segmentRadius));
        }
    }
    
    /** */
    public static final strictfp class PaintRaster extends GISExtension.Command {

        /** */
        public String getAgentClassString() {
            return "OTLP";
        }

        /** */
        public Syntax getSyntax() {
            return Syntax.commandSyntax(new int[] { Syntax.TYPE_WILDCARD, Syntax.TYPE_NUMBER });
        }

        /** */
        public void performInternal (Argument args[], Context context) 
                throws ExtensionException , LogoException {
            Object arg = args[0].get();
            if (! (arg instanceof RasterDataset)) {
                throw new ExtensionException("not a RasterDataset: " + Dump.logoObject(arg));
            }
            int transparency = args[1].getIntValue();
            if ((transparency < 0) || (transparency > 255)) {
                throw new ExtensionException("transparency must be between 0 and 255");
            }
            BufferedImage drawing = context.getDrawing();
            Graphics2D g = (Graphics2D)drawing.getGraphics();
            g.setColor(GISExtension.getState().getColor());
            g.setTransform(getTransform(context.getAgent().world(),
                                        drawing.getWidth(),
                                        drawing.getHeight()));
            try {
                RasterDataset dataset = (RasterDataset)arg;
                BufferedImage bimg = new BufferedImage(new RangeColorModel(dataset.getRaster()),
                                                       dataset.getRaster(),
                                                       false,
                                                       null);
                // Work-around for JDK Bug #4723021: copy the image to a
                // BufferedImage with an ARGB color model WITHOUT SCALING 
                // before we try drawing, or the Java rendering pipeline 
                // will fail with an ImagingOpException when we try and 
                // scale the image.
                // See http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4723021
                BufferedImage img = new BufferedImage(bimg.getWidth(), bimg.getHeight(), BufferedImage.TYPE_INT_ARGB);
                Graphics2D gImg = img.createGraphics();
                try {
                    gImg.drawImage(bimg, 0, 0, null);
                } finally {
                    gImg.dispose();
                }
                if (transparency > 0) {
                    // It would be much more efficient to do this by making a new 
                    // BufferedImage that re-uses the source image's Raster, with a 
                    // custom ColorModel that decorates the source's ColorModel and 
                    // adds/modifies the alpha channel. But the code is much more 
                    // simple this way, and it's fast enough for now.
                    int alpha = 255 - transparency;
                    int width = img.getWidth();
                    int height = img.getHeight();
                    BufferedImage tImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                    gImg = tImg.createGraphics();
                    try {
                        gImg.drawRenderedImage(img, new AffineTransform());
                    } finally {
                        gImg.dispose();
                    }
                    WritableRaster alphaRaster = tImg.getAlphaRaster();
                    int[] pixel = { alpha };
                    for (int x = 0; x < width; x += 1) {
                        for (int y = 0; y < height; y += 1) {
                            alphaRaster.setPixel(x, y, pixel);
                        }
                    }
                    img = tImg;
                }
                // Draw our "compatible" image as we would have liked to have drawn bimg
                GridDimensions dimensions = dataset.getDimensions();
                AffineTransform xform = new AffineTransform(dimensions.getCellWidth(),
                                                            0.0,
                                                            0.0,
                                                            -dimensions.getCellHeight(),
                                                            dimensions.getLeft(),
                                                            dimensions.getTop());
                g.drawRenderedImage(img, xform);
            } finally {
                g.dispose();
            }
        }
    }
    
    /** */
    private static final strictfp class LineStringPathIterator implements PathIterator {
        
        private LineString _line;
        private int _position;
        
        public LineStringPathIterator (LineString ls) {
            _line = ls;
            _position = 0;
        }
        
        public int getWindingRule() {
            return WIND_NON_ZERO;
        }

        public boolean isDone() {
            return _position >= _line.getNumPoints();
        }
        
        public void next() {
            _position += 1;
        }
            
        public int currentSegment (float[] coords) {
            Coordinate c = _line.getCoordinateN(_position);
            coords[0] = (float)c.x;
            coords[1] = (float)c.y;
            return (_position == 0) ? SEG_MOVETO : SEG_LINETO;
        }
        
        public int currentSegment(double[] coords) {
            Coordinate c = _line.getCoordinateN(_position);
            coords[0] = c.x;
            coords[1] = c.y;
            return (_position == 0) ? SEG_MOVETO : SEG_LINETO;
        }
    }

    //--------------------------------------------------------------------------
    // Class methods
    //--------------------------------------------------------------------------
    
    /** */
    static AffineTransform getTransform (World world, 
                                         double drawingWidth, 
                                         double drawingHeight) 
            throws ExtensionException {
        double worldCenterX = world.minPxcor() + (world.worldWidth() * 0.5);
        double worldCenterY = world.minPycor() + (world.worldHeight() * 0.5);
        double sx = drawingWidth / (double)world.worldWidth();
        double sy = drawingHeight / (double)world.worldHeight();
        double tx = StrictMath.ceil(drawingWidth * 0.5) + (sx * 0.5);
        double ty = StrictMath.ceil(drawingHeight * 0.5) - (sy * 0.5);
        AffineTransform result = AffineTransform.getTranslateInstance(tx, ty);
        result.scale(sx, -sy);
        result.translate(-worldCenterX, -worldCenterY);
        result.concatenate(GISExtension.getState().getTransformation().getGISToNetLogoTransform());
        return result;
    }
    
    /** */
    static java.awt.Shape toShape (Geometry geom, float pointRadius) {
        GeneralPath result = new GeneralPath(GeneralPath.WIND_NON_ZERO);
        if (geom instanceof Point) {
            Coordinate c = ((Point)geom).getCoordinate();
            if (c != null) {
                result.append(new Ellipse2D.Double(c.x - pointRadius, 
                                                   c.y - pointRadius, 
                                                   pointRadius * 2, 
                                                   pointRadius * 2), 
                              false);
            }
        } else if (geom instanceof LineString) {
            result.append(new LineStringPathIterator((LineString)geom), false);
        } else if (geom instanceof Polygon) {
            Polygon poly = (Polygon)geom;
            result.append(new LineStringPathIterator(poly.getExteriorRing()), false);
            for (int i = 0; i < poly.getNumInteriorRing(); i += 1) {
                result.append(new LineStringPathIterator(poly.getInteriorRingN(i)), false);
            }
        } else if (geom instanceof GeometryCollection) {
            GeometryCollection gc = (GeometryCollection)geom;
            for (int i = 0; i < gc.getNumGeometries(); i += 1) {
                result.append(toShape(gc.getGeometryN(i), pointRadius), false);
            }
        }
        return result;
    }
}
