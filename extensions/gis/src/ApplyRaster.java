//
// Copyright (c) 2008 Eric Russell. All rights reserved.
//

package org.myworldgis.netlogo;

import com.vividsolutions.jts.geom.Envelope;
import java.awt.Dimension;
import java.awt.image.WritableRaster;
import org.nlogo.api.AgentException;
import org.nlogo.api.Argument;
import org.nlogo.api.Context;
import org.nlogo.api.ExtensionException;
import org.nlogo.api.LogoException;
import org.nlogo.api.Patch;
import org.nlogo.api.Syntax;
import org.nlogo.api.World;
import org.nlogo.prim._reference;


/**
 * 
 */
public strictfp class ApplyRaster extends GISExtension.Command {
    
    //--------------------------------------------------------------------------
    // GISExtension.Command implementation
    //--------------------------------------------------------------------------
    
    /** */
    public String getAgentClassString() {
        return "O";
    }

    /** */
    public Syntax getSyntax() {
        return Syntax.commandSyntax(new int[] { Syntax.TYPE_WILDCARD, 
                                                Syntax.TYPE_REFERENCE });
    }

    /** */
    public void performInternal (Argument args[], Context context) 
            throws AgentException, ExtensionException, LogoException {
        RasterDataset dataset = RasterDataset.getDataset(args[0]);
        World world = context.getAgent().world();
        _reference patchVar = (_reference)((org.nlogo.nvm.Argument)args[1]).getReporter();
        Envelope gisEnvelope = GISExtension.getState().getTransformation().getEnvelope(world);
        Dimension gridSize = new Dimension(world.worldWidth(), world.worldHeight());
        RasterDataset resampledDataset = dataset.resample(new GridDimensions(gridSize, gisEnvelope));
        WritableRaster raster = resampledDataset.getRaster();
        for (int px = world.minPxcor(), ix = 0; px <= world.maxPxcor(); px += 1, ix += 1) {
            for (int py = world.minPycor(), iy = raster.getHeight() - 1; py <= world.maxPycor(); py += 1, iy -= 1) {
                Patch p = world.fastGetPatchAt(px, py);
                p.setVariable(patchVar.reference.vn(), Double.valueOf(raster.getSampleDouble(ix, iy, 0)));
            }
        }
    }
}
