//
// Copyright (c) 2007 Eric Russell. All rights reserved.
//

package org.myworldgis.netlogo;

import org.locationtech.jts.geom.Envelope;
import java.awt.Dimension;
import java.awt.image.BandedSampleModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferDouble;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import org.nlogo.api.Argument;
import org.nlogo.api.Context;
import org.nlogo.api.ExtensionException;
import org.nlogo.api.LogoException;
import org.nlogo.api.Patch;
import org.nlogo.core.Reference;
import org.nlogo.core.Syntax;
import org.nlogo.core.SyntaxJ;
import org.nlogo.api.World;
import org.nlogo.prim._reference;


/**
 *
 */
public final class PatchDataset extends GISExtension.Reporter {

    //--------------------------------------------------------------------------
    // GISExtension.Reporter implementation
    //--------------------------------------------------------------------------

    public String getAgentClassString() {
        return "O";
    }

    public Syntax getSyntax() {
        return SyntaxJ.reporterSyntax(new int[] { Syntax.ReferenceType() },
                                     Syntax.WildcardType());
    }

    public Object reportInternal (Argument args[], Context context)
            throws ExtensionException, LogoException {
        Reference patchVar = ((org.nlogo.nvm.Argument)args[0]).getReference();
        World world = context.getAgent().world();
        int width = world.worldWidth();
        int height = world.worldHeight();
        Envelope envelope = GISExtension.getState().getTransformation().getEnvelope(world);
        GridDimensions dimensions = new GridDimensions(new Dimension(width, height), envelope);
        DataBuffer data = new DataBufferDouble(width * height);
        BandedSampleModel sampleModel = new BandedSampleModel(data.getDataType(),width, height, 1);
        WritableRaster raster = Raster.createWritableRaster(sampleModel, data, null);
        for (int px = world.minPxcor(), ix = 0; px <= world.maxPxcor(); px += 1, ix += 1) {
            for (int py = world.minPycor(), iy = raster.getHeight() - 1; py <= world.maxPycor(); py += 1, iy -= 1) {
                Patch p = world.fastGetPatchAt(px, py);
                Object value = p.getVariable(patchVar.vn());
                if (value instanceof Number) {
                    raster.setSample(ix, iy, 0, ((Number)value).doubleValue());
                } else {
                    raster.setSample(ix, iy, 0, Double.NaN);
                }
            }
        }
        return new RasterDataset(dimensions, raster);
    }
}
