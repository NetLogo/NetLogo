//
// Copyright (c) 2007 Eric Russell. All rights reserved.
//

package org.myworldgis.util;

import java.awt.Color;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.color.ICC_ColorSpace;
import java.awt.color.ICC_Profile;
import java.awt.image.BandedSampleModel;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.SampleModel;


/**
 * This color model is to be used ONLY for DRAWING. Do not use this color
 * model for images passed to JAI for transformation.
 */
public final strictfp class RangeColorModel extends ColorModel {
    
    //--------------------------------------------------------------------------
    // Instance variables
    //--------------------------------------------------------------------------
    
    /** */
    private final double _valueMin;
    
    /** */
    private final double _valueScale;
    
    /** */
    private final int _redMin;
    
    /** */
    private final int _redRange;
    
    /** */
    private final int _greenMin;
    
    /** */
    private final int _greenRange;
    
    /** */
    private final int _blueMin;
    
    /** */
    private final int _blueRange;
    
    //--------------------------------------------------------------------------
    // Instance variables
    //--------------------------------------------------------------------------
    
    /** */
    public RangeColorModel (Raster raster) {
        this(raster, Color.BLACK, Color.WHITE);
    }   
    
    /** */
    public RangeColorModel (Raster raster,
                            Color minColor,
                            Color maxColor) {
        super(DataBuffer.getDataTypeSize(DataBuffer.TYPE_DOUBLE), 
              new int[] { DataBuffer.getDataTypeSize(DataBuffer.TYPE_BYTE),
                          DataBuffer.getDataTypeSize(DataBuffer.TYPE_BYTE),
                          DataBuffer.getDataTypeSize(DataBuffer.TYPE_BYTE),
                          DataBuffer.getDataTypeSize(DataBuffer.TYPE_BYTE) },
              new ICC_ColorSpace(ICC_Profile.getInstance(ColorSpace.CS_sRGB)), 
              true,
              false,
              Transparency.BITMASK, 
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
        _valueMin = min;
        _valueScale = 1.0 / (max - min);
        _redMin = minColor.getRed();
        _redRange = maxColor.getRed() - minColor.getRed();
        _greenMin = minColor.getGreen();
        _greenRange = maxColor.getGreen() - minColor.getGreen();
        _blueMin = minColor.getBlue();
        _blueRange = maxColor.getBlue() - minColor.getBlue();
    }

    //--------------------------------------------------------------------------
    // Instance methods
    //--------------------------------------------------------------------------
    
    /** */
    public int getAlpha (Object pixel) {
        double value = ((double[])pixel)[0];
        if (Double.isNaN(value)) {
            return 0;
        } else {
            return 0xff;
        }
    }
    
    /** */
    public int getRed (Object pixel) {
        double value = ((double[])pixel)[0];
        if (Double.isNaN(value)) {
            return 0;
        } else {
            return (int)StrictMath.round(((value - _valueMin) * _valueScale) * _redRange) + _redMin;
        }
    }
    
    /** */
    public int getGreen (Object pixel) {
        double value = ((double[])pixel)[0];
        if (Double.isNaN(value)) {
            return 0;
        } else {
            return (int)StrictMath.round(((value - _valueMin) * _valueScale) * _greenRange) + _greenMin;
        }
    }
    
    /** */
    public int getBlue (Object pixel) {
        double value = ((double[])pixel)[0];
        if (Double.isNaN(value)) {
            return 0;
        } else {
            return (int)StrictMath.round(((value - _valueMin) * _valueScale) * _blueRange) + _blueMin;
        }
    }
    
    /** */
    public boolean isCompatibleRaster (Raster raster) {
        return (raster.getDataBuffer().getDataType() == DataBuffer.TYPE_DOUBLE) &&
               (raster.getNumBands() == 1);
    }
    
    /** */
    public boolean isCompatibleSampleModel (SampleModel sm) {
        return (sm.getDataType() == DataBuffer.TYPE_DOUBLE) &&
               (sm.getNumBands() == 1);
    }
    
    /** */
    public SampleModel createCompatibleSampleModel (int w, int h) {
        return new BandedSampleModel(DataBuffer.TYPE_DOUBLE, w, h, 1);
    }
    
    //--------------------------------------------------------------------------
    // ColorModel implementation
    //--------------------------------------------------------------------------

    /** */
    public int getRed (int pixel) { 
        throw new IllegalArgumentException("pixel values not representable as int");
    }

    /** */
    public int getGreen (int pixel)  { 
        throw new IllegalArgumentException("pixel values not representable as int");
    }

    /** */
    public int getBlue (int pixel)  { 
        throw new IllegalArgumentException("pixel values not representable as int");
    }

    /** */
    public int getAlpha (int pixel)  { 
        throw new IllegalArgumentException("pixel values not representable as int");
    }
}
