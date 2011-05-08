//
// Copyright (c) 2007 Eric Russell. All rights reserved.
//

package org.myworldgis.netlogo;

import org.nlogo.api.Argument;
import org.nlogo.api.Context;
import org.nlogo.api.ExtensionException;
import org.nlogo.api.LogoException;
import org.nlogo.api.Syntax;


/**
 * 
 */
public final strictfp class GetShapeType extends GISExtension.Reporter {
    
    //--------------------------------------------------------------------------
    // GISExtension.Reporter implementation
    //--------------------------------------------------------------------------
    
    /** */
    public String getAgentClassString() {
        return "OTPL";
    }
    
    /** */
    public Syntax getSyntax() {
        return Syntax.reporterSyntax(new int[] { Syntax.TYPE_WILDCARD },
                                     Syntax.TYPE_STRING);
    }
    
    /** */
    public Object reportInternal (Argument args[], Context context) 
            throws ExtensionException , LogoException {
        Object arg = args[0].get();
        if (arg instanceof VectorDataset) {
            return ((VectorDataset)arg).getShapeType().toString();
        } else if (arg instanceof VectorFeature) {
            return ((VectorFeature)arg).getShapeType().toString();
        } else {
            throw new ExtensionException("not a VectorFeature or VectorDataset: " + arg);
        }
    }
}
