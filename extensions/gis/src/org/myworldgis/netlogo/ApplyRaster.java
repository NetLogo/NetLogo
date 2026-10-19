//
// Copyright (c) 2008 Eric Russell. All rights reserved.
//

package org.myworldgis.netlogo;

import org.locationtech.jts.geom.Envelope;
import java.awt.Dimension;
import java.awt.image.WritableRaster;
import org.nlogo.api.AgentException;
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
public class ApplyRaster extends GISExtension.Command {

    //--------------------------------------------------------------------------
    // GISExtension.Command implementation
    //--------------------------------------------------------------------------

    /** */
    public String getAgentClassString() {
        return "O";
    }

    /** */
    public Syntax getSyntax() {
        return SyntaxJ.commandSyntax(new int[] { Syntax.WildcardType(),
                                                Syntax.ReferenceType() });
    }

    /** */
    public void performInternal (Argument args[], Context context)
            throws AgentException, ExtensionException, LogoException {
        RasterDataset dataset = RasterDataset.getDataset(args[0]);
        World world = context.getAgent().world();
        Reference patchVar = ((org.nlogo.nvm.Argument)args[1]).getReference();
        Envelope gisEnvelope = GISExtension.getState().getTransformation().getEnvelope(world);
        Dimension gridSize = new Dimension(world.worldWidth(), world.worldHeight());
        RasterDataset resampledDataset = dataset.resample(new GridDimensions(gridSize, gisEnvelope));
        WritableRaster raster = resampledDataset.getRaster();
        for (int px = world.minPxcor(), ix = 0; px <= world.maxPxcor(); px += 1, ix += 1) {
            for (int py = world.minPycor(), iy = raster.getHeight() - 1; py <= world.maxPycor(); py += 1, iy -= 1) {
                Patch p = world.fastGetPatchAt(px, py);
                p.setVariable(patchVar.vn(), Double.valueOf(raster.getSampleDouble(ix, iy, 0)));
            }
        }
    }
}
