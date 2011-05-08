//
// Copyright (c) 2007 Eric Russell. All rights reserved.
//

package org.myworldgis.util;

import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.color.ICC_ColorSpace;
import java.awt.color.ICC_Profile;
import java.awt.image.BandedSampleModel;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferDouble;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;


/**
 * This color model is only be used for performing transformations on a 
 * dataset's raster. It is NOT CAPABLE of drawing.
 */
public final class ValueColorModel extends ColorModel {
    
    //--------------------------------------------------------------------------
    // Instance variables
    //--------------------------------------------------------------------------
    
    /** */
    private final double _min;
    
    /** */
    private final double _scale;

    //--------------------------------------------------------------------------
    // Instance variables
    //--------------------------------------------------------------------------
    
    /** */
    public ValueColorModel (Raster raster) {
        super(DataBuffer.getDataTypeSize(DataBuffer.TYPE_DOUBLE), 
              new int[] { DataBuffer.getDataTypeSize(DataBuffer.TYPE_DOUBLE) },
              new ICC_ColorSpace(ICC_Profile.getInstance(ColorSpace.CS_GRAY)), 
              false,
              false,
              Transparency.OPAQUE, 
              DataBuffer.TYPE_DOUBLE);
        double min = Double.MAX_VALUE;
        double max = -Double.MAX_VALUE;
        DataBuffer buffer = raster.getDataBuffer();
        for (int i = 0; i < buffer.getSize(); i += 1) {
            double d = buffer.getElemDouble(i);
            if (d < min) {
                min = d;
            }
            if (d > max) {
                max = d;
            }
        }
        _min = min;
        _scale = 1.0 / (max - min);
    }

    //--------------------------------------------------------------------------
    // Instance methods
    //--------------------------------------------------------------------------
    
    /** */
    public boolean isCompatibleRaster (Raster raster) {
        return (raster.getDataBuffer().getDataType() == DataBuffer.TYPE_DOUBLE) &&
               (raster.getNumBands() == 1);
    }
    
    /** */
    public float[] getNormalizedComponents (Object pixel,
                                            float[] normComponents,
                                            int normOffset) {
        if (normComponents == null) {
            normComponents = new float[normOffset+1];
        }
        double value = ((double[])pixel)[0];
        if (Double.isNaN(value)) {
            normComponents[normOffset] = 0.0f;
        } else {
            ColorSpace cs = getColorSpace();
            float min = cs.getMinValue(0);
            float max = cs.getMaxValue(0);
            normComponents[normOffset] = (float)((((value - _min) * _scale) * (max - min)) + min);
        }   
        return normComponents;
    }
    
    /** */
    public Object getDataElements (int rgb, Object pixel) {
        if (pixel == null) {
            pixel = new double[1];
        }
        double[] dpix = (double[])pixel;
        if ((rgb >> 24) == 0) {
            dpix[0] = Double.NaN;
        } else {
            dpix[0] = (((rgb & 0xFF) / 255.0) / _scale) + _min;
        }
        return pixel;
    }
    
    /** */
    public WritableRaster createCompatibleWritableRaster (int w, int h) {
        return Raster.createWritableRaster(createCompatibleSampleModel(w, h), 
                                           new DataBufferDouble(w * h),
                                           null);
    }
    
    /** */
    public SampleModel createCompatibleSampleModel (int w, int h) {
        return new BandedSampleModel(DataBuffer.TYPE_DOUBLE, w, h, 1);
    }

    /** */
    public boolean isCompatibleSampleModel (SampleModel sm) {
        return (sm.getDataType() == DataBuffer.TYPE_DOUBLE) &&
               (sm.getNumBands() == 1);
    }
    
    //--------------------------------------------------------------------------
    // ColorModel implementation
    //--------------------------------------------------------------------------

    /** */
    public int getRed (int pixel) { 
        throw new IllegalArgumentException("values not representable as int");
    }

    /** */
    public int getGreen (int pixel)  { 
        throw new IllegalArgumentException("values not representable as int");
    }

    /** */
    public int getBlue (int pixel)  { 
        throw new IllegalArgumentException("values not representable as int");
    }

    /** */
    public int getAlpha (int pixel)  { 
        throw new IllegalArgumentException("values not representable as int");
    }
}
