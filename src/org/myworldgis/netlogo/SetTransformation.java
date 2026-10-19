//
// Copyright (c) 2007 Eric Russell. All rights reserved.
//

package org.myworldgis.netlogo;

import org.locationtech.jts.geom.Envelope;
import java.text.ParseException;
import org.nlogo.api.Argument;
import org.nlogo.api.Context;
import org.nlogo.api.ExtensionException;
import org.nlogo.api.LogoException;
import org.nlogo.core.Syntax;
import org.nlogo.core.SyntaxJ;
import org.nlogo.api.World;


/**
 *
 */
public abstract class SetTransformation {


    //--------------------------------------------------------------------------
    // Inner classes
    //--------------------------------------------------------------------------

    /** */
    public static final class Linked extends GISExtension.Command {

        public String getAgentClassString() {
            return "O";
        }

        public Syntax getSyntax() {
            return SyntaxJ.commandSyntax(new int[] { Syntax.ListType(), Syntax.ListType() });
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
    public static final class Independent extends GISExtension.Command {

        public String getAgentClassString() {
            return "O";
        }

        public Syntax getSyntax() {
            return SyntaxJ.commandSyntax(new int[] { Syntax.ListType(), Syntax.ListType() });
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
    public static final class WorldLinked extends GISExtension.Command {

        public String getAgentClassString() {
            return "O";
        }

        public Syntax getSyntax() {
            return SyntaxJ.commandSyntax(new int[] { Syntax.ListType() });
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
    public static final class WorldIndependent extends GISExtension.Command {

        public String getAgentClassString() {
            return "O";
        }

        public Syntax getSyntax() {
            return SyntaxJ.commandSyntax(new int[] { Syntax.ListType() });
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
