//
// Copyright (c) 2007 Eric Russell. All rights reserved.
//

package org.myworldgis.netlogo;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import java.util.Iterator;
import org.nlogo.api.Agent;
import org.nlogo.api.AgentSet;
import org.nlogo.api.Argument;
import org.nlogo.api.Context;
import org.nlogo.api.ExtensionException;
import org.nlogo.api.LogoException;
import org.nlogo.api.Syntax;
import org.nlogo.api.World;

/**
 * 
 */
public abstract strictfp class GetEnvelope {
    

    //--------------------------------------------------------------------------
    // Inner classes
    //--------------------------------------------------------------------------
    
    /** */
    public static final strictfp class OfObject extends GISExtension.Reporter {
    
        /** */
        public String getAgentClassString() {
            return "OTPL";
        }
        
        /** */
        public Syntax getSyntax() {
            return Syntax.reporterSyntax(new int[] { Syntax.TYPE_WILDCARD },
                                         Syntax.TYPE_LIST);
        }
    
        /** */
        public Object reportInternal (Argument args[], Context context) 
                throws ExtensionException , LogoException {
            Object arg = args[0].get();
            if (arg instanceof Dataset) {
                return EnvelopeLogoListFormat.getInstance().format(((Dataset)arg).getEnvelope());
            } else if (arg instanceof VectorFeature) {
                return EnvelopeLogoListFormat.getInstance().format(((VectorFeature)arg).getGeometry().getEnvelopeInternal());
            } else if (arg instanceof Agent) {
                Geometry geom = GISExtension.getState().agentGeometry((Agent)arg);
                return EnvelopeLogoListFormat.getInstance().format(geom.getEnvelopeInternal());
            } else if (arg instanceof AgentSet) {
                Envelope env = new Envelope();
                for (Iterator<Agent> i = ((AgentSet)arg).agents().iterator(); i.hasNext();) {
                    env.expandToInclude(GISExtension.getState().agentGeometry(i.next()).getEnvelopeInternal());
                }
                return EnvelopeLogoListFormat.getInstance().format(env);
            } else {
                throw new ExtensionException("not a RasterDataset, VectorDataset, VectorFeature, Agent, or Agentset: " + arg);
            }
        }
    }
    

    /** */
    public static final strictfp class OfWorld extends GISExtension.Reporter {
    
        /** */
        public String getAgentClassString() {
            return "OTPL";
        }
        
        /** */
        public Syntax getSyntax() {
            return Syntax.reporterSyntax(new int[] { }, Syntax.TYPE_LIST);
        }
    
        /** */
        public Object reportInternal (Argument args[], Context context) 
                throws ExtensionException , LogoException {
            World world = context.getAgent().world();
            Envelope envelope = GISExtension.getState().getTransformation().getEnvelope(world);
            return EnvelopeLogoListFormat.getInstance().format(envelope);
        }
    }
}
