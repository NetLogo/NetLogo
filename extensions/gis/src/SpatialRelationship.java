//
// Copyright (c) 2007 Eric Russell. All rights reserved.
//

package org.myworldgis.netlogo;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.prep.PreparedGeometry;
import com.vividsolutions.jts.geom.prep.PreparedGeometryFactory;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.myworldgis.util.JTSUtils;
import org.nlogo.api.Agent;
import org.nlogo.api.AgentSet;
import org.nlogo.api.Argument;
import org.nlogo.api.Context;
import org.nlogo.api.ExtensionException;
import org.nlogo.api.LogoException;
import org.nlogo.api.LogoList;
import org.nlogo.api.Syntax;


/**
 * 
 */
public abstract strictfp class SpatialRelationship {
    

    //--------------------------------------------------------------------------
    // Inner classes
    //--------------------------------------------------------------------------

    /** */
    private static abstract strictfp class DefaultTest extends GISExtension.Reporter {
        
        public String getAgentClassString() {
            return "OTPL";
        }
        
        public Syntax getSyntax() {
            return Syntax.reporterSyntax(new int[] { Syntax.TYPE_WILDCARD, Syntax.TYPE_WILDCARD },
                                         Syntax.TYPE_BOOLEAN);
        }
        
        public Object reportInternal (Argument args[], Context context) 
                throws ExtensionException, LogoException {
            Geometry geom0 = getGeometry(args[0].get());
            Geometry geom1 = getGeometry(args[1].get());
            if (relates(geom0, geom1)) {
                return Boolean.TRUE;
            } else {
                return Boolean.FALSE;
            }
        }
        
        protected abstract boolean relates (Geometry agentGeom, Geometry featureGeom);
    }
    
    /** */
    public static final strictfp class IntersectionTest extends DefaultTest {
        protected boolean relates (Geometry geom0, Geometry geom1) {
            return geom0.intersects(geom1);
        }
    }
    
    /** */
    public static final strictfp class ContainsTest extends DefaultTest {
        protected boolean relates (Geometry geom0, Geometry geom1) {
            return geom0.covers(geom1);
        }
    }
    
    /** */
    public static final strictfp class ContainedByTest extends DefaultTest {
        protected boolean relates (Geometry geom0, Geometry geom1) {
            return geom0.coveredBy(geom1);
        }
    }
    
    /** */
    public static final strictfp class GeneralTest extends GISExtension.Reporter {
        
        public String getAgentClassString() {
            return "OTPL";
        }
        
        public Syntax getSyntax() {
            return Syntax.reporterSyntax(new int[] { Syntax.TYPE_WILDCARD, 
                                                     Syntax.TYPE_WILDCARD,
                                                     Syntax.TYPE_STRING },
                                         Syntax.TYPE_BOOLEAN);
        }

        public Object reportInternal (Argument args[], Context context) 
                throws ExtensionException, LogoException {
            Geometry geom0 = getGeometry(args[0].get());
            Geometry geom1 = getGeometry(args[1].get());
            String pattern = args[2].getString();
            if (geom0.relate(geom1).matches(pattern)) {
                return Boolean.TRUE;
            } else {
                return Boolean.FALSE;
            }
        }
    }
    
    /** */
    public static final strictfp class GetRelationship extends GISExtension.Reporter {
        
        public String getAgentClassString() {
            return "OTPL";
        }
        
        public Syntax getSyntax() {
            return Syntax.reporterSyntax(new int[] { Syntax.TYPE_WILDCARD, 
                                                     Syntax.TYPE_WILDCARD },
                                         Syntax.TYPE_STRING);
        }

        public Object reportInternal (Argument args[], Context context) 
                throws ExtensionException, LogoException {
            Geometry geom0 = getGeometry(args[0].get());
            Geometry geom1 = getGeometry(args[1].get());
            return geom0.relate(geom1).toString();
        }
    }
    
    /** */
    public static final strictfp class Intersecting extends GISExtension.Reporter {
        
        public String getAgentClassString() {
            return "OTPL";
        }
        
        public Syntax getSyntax() {
            return Syntax.reporterSyntax(Syntax.TYPE_PATCHSET, 
                                         new int[] { Syntax.TYPE_WILDCARD },
                                         Syntax.TYPE_PATCHSET,
                                         Syntax.NORMAL_PRECEDENCE);
        }

        public Object reportInternal (Argument args[], Context context) 
                throws ExtensionException, LogoException {
            AgentSet set = (AgentSet)args[0].get();
            PreparedGeometry pGeom = PreparedGeometryFactory.prepare(getGeometry(args[1].get()));
            List<org.nlogo.agent.Agent> agents = new LinkedList<org.nlogo.agent.Agent>();
            for (Iterator<Agent> i = set.agents().iterator(); i.hasNext();) {
                Agent agent = i.next();
                if (pGeom.intersects(GISExtension.getState().agentGeometry(agent))) {
                    agents.add((org.nlogo.agent.Agent)agent);
                }
            }
            return new org.nlogo.agent.ArrayAgentSet( ((org.nlogo.agent.AgentSet)set).type(),
                                                     agents.toArray(new org.nlogo.agent.Agent[agents.size()]),
                                                     (org.nlogo.agent.World)context.getAgent().world());
        }
    }
    
    //--------------------------------------------------------------------------
    // Class methods
    //--------------------------------------------------------------------------
    
    /** */
    @SuppressWarnings("unchecked")
    static Geometry getGeometry (Object arg) throws ExtensionException {
        if (arg instanceof VectorDataset) {
            Collection<VectorFeature> features = ((VectorDataset)arg).getFeatures();            
            Geometry[] geoms = new Geometry[features.size()];
            int gIndex = 0;
            for (Iterator<VectorFeature> i = features.iterator(); i.hasNext();) {
                geoms[gIndex++] = i.next().getGeometry();
            }
            return JTSUtils.flatten(GISExtension.getState().factory().createGeometryCollection(geoms)); 
        } else if (arg instanceof VectorFeature) {
            return ((VectorFeature)arg).getGeometry();
        } else if (arg instanceof Agent) {
            return GISExtension.getState().agentGeometry((Agent)arg);
        } else if (arg instanceof AgentSet) {
            AgentSet set = (AgentSet)arg;
            Geometry[] geoms = new Geometry[set.count()];
            int gIndex = 0;
            for (Iterator<Agent> i = set.agents().iterator(); i.hasNext();) {
                geoms[gIndex++] = GISExtension.getState().agentGeometry(i.next());
            }
            return JTSUtils.flatten(GISExtension.getState().factory().createGeometryCollection(geoms));
        } else if (arg instanceof LogoList) {
            LogoList list = (LogoList)arg;
            Geometry[] geoms = new Geometry[list.size()];
            int gIndex = 0;
            for (Iterator i = list.iterator(); i.hasNext();) {
                geoms[gIndex++] = getGeometry(i.next());
            }
            return JTSUtils.flatten(GISExtension.getState().factory().createGeometryCollection(geoms));
        } else {
            throw new ExtensionException("not a VectorFeature, Agent, AgentSet, or List: " + arg);
        }
    }
}
