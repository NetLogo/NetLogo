//
// Copyright (c) 2008 Eric Russell. All rights reserved.
//

package org.myworldgis.netlogo;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import java.awt.Dimension;
import java.text.ParseException;
import java.util.Iterator;
import javax.media.jai.KernelJAI;
import org.nlogo.api.Argument;
import org.nlogo.api.Context;
import org.nlogo.api.ExtensionException;
import org.nlogo.api.LogoException;
import org.nlogo.api.LogoList;
import org.nlogo.api.Patch;
import org.nlogo.api.Syntax;
import org.nlogo.api.Turtle;
import org.nlogo.api.World;


/** 
 * 
 */
public abstract strictfp class RasterDatasetMath {

    //--------------------------------------------------------------------------
    // Inner classes
    //--------------------------------------------------------------------------
    
    /** */
    public static final strictfp class GetSample extends GISExtension.Reporter {
        
        public String getAgentClassString() {
            return "OTPL";
        }
        
        public Syntax getSyntax() {
            return Syntax.reporterSyntax(new int[] { Syntax.TYPE_WILDCARD, 
                                                     Syntax.TYPE_WILDCARD },
                                         Syntax.TYPE_NUMBER);
        }
        
        public Object reportInternal (Argument args[], Context context)
                throws ExtensionException, LogoException {
            RasterDataset dataset = RasterDataset.getDataset(args[0]);
            Object arg1 = args[1].get();
            if (arg1 instanceof LogoList) {
                LogoList list = (LogoList)arg1;
                if (list.size() == 2) {
                    // List of length 2: a point in NetLogo space                    
                    Coordinate loc = new Coordinate(((Number)list.get(0)).doubleValue(),
                                                    ((Number)list.get(1)).doubleValue());
                    loc = GISExtension.getState().netLogoToGIS(loc, loc);
                    return Double.valueOf(dataset.getValue(loc));
                } else if (list.size() == 4) {
                    // List of length 4: an envelope in GIS space
                    return Double.valueOf(dataset.getValue(new Envelope(((Number)list.get(0)).doubleValue(),
                                                                    ((Number)list.get(1)).doubleValue(),
                                                                    ((Number)list.get(2)).doubleValue(),
                                                                    ((Number)list.get(3)).doubleValue())));
                } else {
                    throw new ExtensionException("list argument must have 2 elements (for a point), or 4 elements (for an envelope)");
                }
            } else if (arg1 instanceof Patch) {
                Patch patch = (Patch)arg1;
                Coordinate bl = GISExtension.getState().netLogoToGIS(new Coordinate(patch.pxcor()-0.5,
                                                                                    patch.pycor()-0.5), 
                                                                     null);
                Coordinate tr = GISExtension.getState().netLogoToGIS(new Coordinate(patch.pxcor()+0.5, 
                                                                                    patch.pycor()+0.5), 
                                                                     null);
                return Double.valueOf(dataset.getValue(new Envelope(tr, bl)));
            } else if (arg1 instanceof Turtle) {
                Turtle turtle = (Turtle)arg1;
                Coordinate loc = new Coordinate(turtle.xcor(), turtle.ycor());
                loc = GISExtension.getState().netLogoToGIS(loc, loc);
                return Double.valueOf(dataset.getValue(loc));
            } else if (arg1 instanceof Vertex) {
                Vertex vertex = (Vertex)arg1;
                return Double.valueOf(dataset.getValue(vertex.getCoordinate()));
            } else {
                throw new ExtensionException("not a list, patch, turtle, or Vertex: " + arg1);
            }
        }
    }

    /** */
    public static final strictfp class Resample extends GISExtension.Reporter {

        public String getAgentClassString() {
            return "OTPL";
        }
        
        public Syntax getSyntax() {
            return Syntax.reporterSyntax(new int[] { Syntax.TYPE_WILDCARD,
                                                     Syntax.TYPE_LIST,
                                                     Syntax.TYPE_NUMBER,
                                                     Syntax.TYPE_NUMBER },
                                         Syntax.TYPE_WILDCARD);
        }
        
        public Object reportInternal (Argument args[], Context context)
                throws ExtensionException, LogoException, ParseException {
            RasterDataset dataset = RasterDataset.getDataset(args[0]);
            Envelope envelope = EnvelopeLogoListFormat.getInstance().parse(args[1].getList());
            int width = args[2].getIntValue();
            int height = args[3].getIntValue();
            return dataset.resample(new GridDimensions(new Dimension(width, height), envelope));
        }
    }
    
    /** */
    public static final strictfp class GetWorldEnvelope extends GISExtension.Reporter {

        public String getAgentClassString() {
            return "OTPL";
        }
        
        public Syntax getSyntax() {
            return Syntax.reporterSyntax(new int[] { Syntax.TYPE_WILDCARD,
                                                     Syntax.TYPE_NUMBER,
                                                     Syntax.TYPE_NUMBER },
                                         Syntax.TYPE_LIST);
        }
        
        public Object reportInternal (Argument args[], Context context)
                throws ExtensionException, LogoException {
            RasterDataset dataset = RasterDataset.getDataset(args[0]);
            int leftCol = args[1].getIntValue();
            int topRow = dataset.getDimensions().getGridHeight() - args[2].getIntValue() - 1;
            World world = context.getAgent().world();
            int width = world.maxPxcor() - world.minPxcor();
            int height = world.maxPycor() - world.minPycor();
            Envelope envelope = new Envelope(dataset.getDimensions().getColumnLeft(leftCol),
                                             dataset.getDimensions().getColumnRight(leftCol+width),
                                             dataset.getDimensions().getRowBottom(topRow-height),
                                             dataset.getDimensions().getRowTop(topRow));
            return EnvelopeLogoListFormat.getInstance().format(envelope);
        }
    }
    
    /** */
    public static final strictfp class Convolve extends GISExtension.Reporter {
        
        public String getAgentClassString() {
            return "OTPL";
        }
        
        public Syntax getSyntax() {
            return Syntax.reporterSyntax(new int[] { Syntax.TYPE_WILDCARD,
                                                     Syntax.TYPE_NUMBER,
                                                     Syntax.TYPE_NUMBER,
                                                     Syntax.TYPE_LIST,
                                                     Syntax.TYPE_NUMBER,
                                                     Syntax.TYPE_NUMBER },
                                         Syntax.TYPE_WILDCARD);
        }
        
        @SuppressWarnings("unchecked")
        public Object reportInternal (Argument args[], Context context)
                throws ExtensionException, LogoException {
            RasterDataset dataset = RasterDataset.getDataset(args[0]);
            int kernelRows = args[1].getIntValue();
            int kernelColumns = args[2].getIntValue();
            LogoList matrixElements = args[3].getList();
            if (matrixElements.size() != (kernelRows * kernelColumns)) {
                throw new ExtensionException("Convolution matrix is "+kernelRows+" by "+kernelColumns+
                                             ", so it must have exactly "+(kernelRows*kernelColumns)+
                                             " elements (currently has "+matrixElements.size()+")");
            }
            float[] data = new float[matrixElements.size()];
            int row = kernelRows - 1;
            int col = 0;
            for (Iterator iterator = matrixElements.iterator(); iterator.hasNext();) {
                // NOTE: reverse order of the rows, since image coordinate system 
                // (where the convolution is computed) has the reversed y axis
                data[row*kernelColumns + col] = ((Number)iterator.next()).floatValue();
                col += 1;
                if (col >= kernelColumns) {
                    col = 0;
                    row -= 1;
                }
            }
            int kernelCenterRow = kernelRows - args[4].getIntValue() - 1;
            int kernelCenterColumn = args[5].getIntValue();
            KernelJAI kernel = new KernelJAI(kernelColumns, 
                                             kernelRows, 
                                             kernelCenterColumn, 
                                             kernelCenterRow, 
                                             data);
            return dataset.convolve(kernel);
        }
    }
}
