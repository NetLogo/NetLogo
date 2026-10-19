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
import org.nlogo.core.LogoList;
import org.nlogo.core.Syntax;
import org.nlogo.core.SyntaxJ;

/**
 *
 */
public final class EnvelopeUnion extends GISExtension.Reporter {

    //--------------------------------------------------------------------------
    // GISExtension.Reporter implementation
    //--------------------------------------------------------------------------

    /** */
    public String getAgentClassString() {
        return "OTPL";
    }

    /** */
    public Syntax getSyntax() {
        return SyntaxJ.reporterSyntax(new int[] { Syntax.RepeatableType() | Syntax.ListType() },
                                     Syntax.ListType(),
                                     2);
    }

    /** */
    public Object reportInternal (Argument args[], Context context)
            throws ExtensionException, LogoException, ParseException {
        Envelope[] envelopes = new Envelope[args.length];
        for (int i = 0; i < args.length; i += 1) {
            envelopes[i] = EnvelopeLogoListFormat.getInstance().parse(args[i].getList());
        }
        if (envelopes.length == 0) {
            return LogoList.Empty();
        } else if (envelopes.length == 1) {
            return EnvelopeLogoListFormat.getInstance().format(envelopes[0]);
        } else {
            Envelope result = envelopes[0];
            for (int i = 1; i < envelopes.length; i += 1) {
                result.expandToInclude(envelopes[i]);
            }
            return EnvelopeLogoListFormat.getInstance().format(result);
        }
    }
}
