//
// Copyright (c) 2007 Eric Russell. All rights reserved.
//

package org.myworldgis.netlogo;

import com.vividsolutions.jts.geom.Envelope;
import java.text.ParseException;
import org.nlogo.api.Argument;
import org.nlogo.api.Context;
import org.nlogo.api.ExtensionException;
import org.nlogo.api.LogoException;
import org.nlogo.api.Syntax;
import org.nlogo.api.World;


/**
 * 
 */
public abstract strictfp class SetTransformation {
    

    //--------------------------------------------------------------------------
    // Inner classes
    //--------------------------------------------------------------------------
    
    /** */
    public static final strictfp class Linked extends GISExtension.Command {
        
        public String getAgentClassString() {
            return "O";
        }
        
        public Syntax getSyntax() {
            return Syntax.commandSyntax(new int[] { Syntax.TYPE_LIST, Syntax.TYPE_LIST });
        }
        
        public void performInternal (Argument args[], Context context) 
                throws ExtensionException, LogoException, ParseException {
            GISExtension.getState().setTransformation(new CoordinateTransformation(
                    EnvelopeLogoListFormat.getInstance().parse(args[0].getList()),
                    EnvelopeLogoListFormat.getInstance().parse(args[1].getList()),
                    true));
        }
    }
    
    /** */
    public static final strictfp class Independent extends GISExtension.Command {
        
        public String getAgentClassString() {
            return "O";
        }
        
        public Syntax getSyntax() {
            return Syntax.commandSyntax(new int[] { Syntax.TYPE_LIST, Syntax.TYPE_LIST });
        }
        
        public void performInternal (Argument args[], Context context) 
                throws ExtensionException, LogoException, ParseException {
            GISExtension.getState().setTransformation(new CoordinateTransformation(
                    EnvelopeLogoListFormat.getInstance().parse(args[0].getList()),
                    EnvelopeLogoListFormat.getInstance().parse(args[1].getList()),
                    false));
        }
    }
    
    /** */
    public static final strictfp class WorldLinked extends GISExtension.Command {

        public String getAgentClassString() {
            return "O";
        }
        
        public Syntax getSyntax() {
            return Syntax.commandSyntax(new int[] { Syntax.TYPE_LIST });
        }
        
        public void performInternal (Argument args[], Context context) 
                throws ExtensionException, LogoException, ParseException {
            World w = context.getAgent().world();
            GISExtension.getState().setTransformation(new CoordinateTransformation(
                    EnvelopeLogoListFormat.getInstance().parse(args[0].getList()),
                    new Envelope(w.minPxcor(), w.maxPxcor(), w.minPycor(), w.maxPycor()),
                    true));
        }
    }
    
    /** */
    public static final strictfp class WorldIndependent extends GISExtension.Command {

        public String getAgentClassString() {
            return "O";
        }
        
        public Syntax getSyntax() {
            return Syntax.commandSyntax(new int[] { Syntax.TYPE_LIST });
        }
        
        public void performInternal (Argument args[], Context context) 
                throws ExtensionException, LogoException, ParseException {
            World w = context.getAgent().world();
            GISExtension.getState().setTransformation(new CoordinateTransformation(
                    EnvelopeLogoListFormat.getInstance().parse(args[0].getList()),
                    new Envelope(w.minPxcor(), w.maxPxcor(), w.minPycor(), w.maxPycor()),
                    false));
        }
    }
}
