//
// Copyright (c) 2007 Eric Russell. All rights reserved.
//

package org.myworldgis.netlogo;

import org.locationtech.jts.geom.Coordinate;
import org.nlogo.api.Argument;
import org.nlogo.api.Context;
import org.nlogo.api.ExtensionException;
import org.nlogo.core.ExtensionObject;
import org.nlogo.api.LogoException;
import org.nlogo.api.LogoListBuilder;
import org.nlogo.core.Syntax;
import org.nlogo.core.SyntaxJ;


/**
 *
 */
public final class Vertex implements ExtensionObject {

    //--------------------------------------------------------------------------
    // Inner classes
    //--------------------------------------------------------------------------

    /** */
    @SuppressWarnings("unchecked")
    public static final class GetLocation extends GISExtension.Reporter {

        private final Coordinate _temp = new Coordinate();

        public Syntax getSyntax() {
            return SyntaxJ.reporterSyntax(new int[] { Syntax.WildcardType() },
                                         Syntax.ListType());
        }

        public String getAgentClassString() {
            return "OTPL";
        }

        public Object reportInternal (Argument args[], Context context)
                throws ExtensionException, LogoException {
            Object arg0 = args[0].get();
            if (!(arg0 instanceof Vertex)) {
                throw new ExtensionException("not a Vertex: " + arg0);
            }
            LogoListBuilder result = new LogoListBuilder();
            Vertex v = (Vertex)arg0;
            if (v.getCoordinate() != null) {
                Coordinate c = GISExtension.getState().gisToNetLogo(v.getCoordinate(), _temp);
                if (c != null) {
                    result.add(Double.valueOf(c.x));
                    result.add(Double.valueOf(c.y));
                }
            }
            return result.toLogoList();
        }
    }

    //--------------------------------------------------------------------------
    // Instance variables
    //--------------------------------------------------------------------------

    /** */
    private final Coordinate _coordinate;

    //--------------------------------------------------------------------------
    // Constructors
    //--------------------------------------------------------------------------

    /** */
    public Vertex (Coordinate coordinate) {
        _coordinate = coordinate;
    }

    //--------------------------------------------------------------------------
    // Instance methods
    //--------------------------------------------------------------------------

    /** */
    public Coordinate getCoordinate () {
        return _coordinate;
    }

    //--------------------------------------------------------------------------
    // ExtensionObject implementation
    //--------------------------------------------------------------------------

    /**
     * Returns a string representation of the object.  If readable is
     * true, it should be possible read it as NL code.
     */
    public String dump (boolean readable, boolean exporting, boolean reference) {
        return "";
    }

    /** */
    public String getExtensionName () {
        return "gis";
    }

    /** */
    public String getNLTypeName() {
        return "Vertex";
    }

    /** */
    public boolean recursivelyEqual (Object obj) {
        if (obj instanceof VectorDataset) {
            Vertex v = (Vertex)obj;
            return v == this;
        } else {
            return false;
        }
    }
}
