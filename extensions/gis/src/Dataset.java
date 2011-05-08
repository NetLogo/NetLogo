//
// Copyright (c) 2007 Eric Russell. All rights reserved.
//

package org.myworldgis.netlogo;

import com.vividsolutions.jts.geom.Envelope;
import org.nlogo.api.Argument;
import org.nlogo.api.Context;
import org.nlogo.api.ExtensionException;
import org.nlogo.api.ExtensionObject;
import org.nlogo.api.LogoException;
import org.nlogo.api.Syntax;


/**
 * 
 */
public abstract strictfp class Dataset implements ExtensionObject {

    //--------------------------------------------------------------------------
    // Inner classes
    //--------------------------------------------------------------------------
    
    /** */
    public static final strictfp class GetDatasetType extends GISExtension.Reporter {
        
        public Syntax getSyntax() {
            return Syntax.reporterSyntax(new int[] { Syntax.TYPE_WILDCARD },
                                         Syntax.TYPE_STRING);
        }
        
        public Object reportInternal (Argument args[], Context context)
                throws ExtensionException, LogoException {
            Object obj = args[0].get();
            if (obj instanceof VectorDataset) {
                return "VECTOR";
            } else if (obj instanceof RasterDataset) {
                return "RASTER";
            } else {
                throw new ExtensionException("not a GIS dataset: " + obj);
            }
        }
    }

    //--------------------------------------------------------------------------
    // Instance variables
    //--------------------------------------------------------------------------
    
    /** */
    private final String _type;
    
    //--------------------------------------------------------------------------
    // Constructors
    //--------------------------------------------------------------------------
    
    /** */
    public Dataset (String type) { 
        _type = type;
    }
    
    //--------------------------------------------------------------------------
    // Abstract instance methods
    //--------------------------------------------------------------------------

    /** */
    public abstract Envelope getEnvelope ();
        
    //--------------------------------------------------------------------------
    // Instance methods
    //--------------------------------------------------------------------------
    
    /** */
    public String getType () {
        return _type;
    }
    
    //--------------------------------------------------------------------------
    // (partial) ExtensionObject implementation
    //--------------------------------------------------------------------------
    
    /** */
    public String getExtensionName () {
        return "gis";
    }
}
