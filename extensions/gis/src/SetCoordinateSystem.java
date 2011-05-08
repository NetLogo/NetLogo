//
// Copyright (c) 2007 Eric Russell. All rights reserved.
//

package org.myworldgis.netlogo;

import java.text.ParseException;
import org.myworldgis.projection.ProjectionFormat;
import org.nlogo.api.Argument;
import org.nlogo.api.Context;
import org.nlogo.api.ExtensionException;
import org.nlogo.api.LogoException;
import org.nlogo.api.LogoList;
import org.nlogo.api.Syntax;


/**
 * 
 */
public final strictfp class SetCoordinateSystem extends GISExtension.Command {
    
    //--------------------------------------------------------------------------
    // GISExtension.Command implementation
    //--------------------------------------------------------------------------
    
    /** */
    public String getAgentClassString() {
        return "O";
    }

    /** */
    public Syntax getSyntax() {
        return Syntax.commandSyntax(new int[] { Syntax.TYPE_LIST | Syntax.TYPE_STRING });
    }

    /** */
    public void performInternal (Argument args[], Context context) 
            throws ExtensionException, LogoException, ParseException {
        Object projArg = args[0].get();
        if (projArg instanceof String) {
            GISExtension.getState().setProjection(ProjectionFormat.getInstance().parseProjection((String)projArg),
                                                  context);
        } else if (projArg instanceof LogoList) {
            GISExtension.getState().setProjection(WKLogoListFormat.getInstance().parseProjection((LogoList)projArg),
                                                  context);
        } else {
            throw new ExtensionException("invalid projection argument: " + projArg);
        }
    }
}
