//
// Copyright (c) 2007 Eric Russell. All rights reserved.
//

package org.myworldgis.netlogo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import org.myworldgis.projection.ProjectionFormat;
import org.nlogo.api.Argument;
import org.nlogo.api.Context;
import org.nlogo.api.ExtensionException;
import org.nlogo.api.File;
import org.nlogo.api.LogoException;
import org.nlogo.api.Syntax;


/**
 * 
 */
public final strictfp class LoadCoordinateSystem extends GISExtension.Command {
    
    //--------------------------------------------------------------------------
    // GISExtension.Command implementation
    //--------------------------------------------------------------------------
    
    /** */
    public String getAgentClassString() {
        return "O";
    }

    /** */
    public Syntax getSyntax() {
        return Syntax.commandSyntax(new int[] { Syntax.TYPE_STRING });
    }

    /** */
    public void performInternal (Argument args[], Context context) 
            throws ExtensionException, IOException, LogoException, ParseException {
        String filePath = args[0].getString();
        File prjFile = GISExtension.getState().getFile(filePath);
        if (prjFile == null) {
            throw new ExtensionException("projection file " + filePath + " does not exist");
        }
        BufferedReader in = new BufferedReader(new InputStreamReader(prjFile.getInputStream()));
        try {
            GISExtension.getState().setProjection(ProjectionFormat.getInstance().parseProjection(in),
                                                  context);
        } finally {
            in.close();
        }
    }
}
