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
import org.nlogo.api.LogoList;
import org.nlogo.api.Syntax;

/**
 * 
 */
public final strictfp class EnvelopeUnion extends GISExtension.Reporter {
    
    //--------------------------------------------------------------------------
    // GISExtension.Reporter implementation
    //--------------------------------------------------------------------------
    
    /** */
    public String getAgentClassString() {
        return "OTPL";
    }

    /** */
    public Syntax getSyntax() {
        return Syntax.reporterSyntax(new int[] { Syntax.TYPE_REPEATABLE | Syntax.TYPE_LIST },
                                     Syntax.TYPE_LIST,
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
