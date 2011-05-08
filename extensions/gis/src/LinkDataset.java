//
// Copyright (c) 2007 Eric Russell. All rights reserved.
//

package org.myworldgis.netlogo;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import java.util.Arrays;
import java.util.Iterator;
import org.nlogo.api.Agent;
import org.nlogo.api.AgentSet;
import org.nlogo.api.Argument;
import org.nlogo.api.Context;
import org.nlogo.api.ExtensionException;
import org.nlogo.api.Link;
import org.nlogo.api.LogoException;
import org.nlogo.api.Syntax;
import org.nlogo.api.World;

/**
 * 
 */
public final strictfp class LinkDataset extends GISExtension.Reporter {
    
    //--------------------------------------------------------------------------
    // GISExtension.Reporter implementation
    //--------------------------------------------------------------------------
    
    /** */
    public String getAgentClassString() {
        return "OTPL";
    }
    
    // gis:set-world-envelope [ -100 -80 40 60 ]
    // gis:store-dataset gis:link-dataset links "link-test.shp"
    
    /** */
    public Syntax getSyntax() {
        return Syntax.reporterSyntax(new int[] { Syntax.TYPE_LINKSET },
                                     Syntax.TYPE_WILDCARD);
    }
    
    /** */
    public Object reportInternal (Argument args[], Context context) 
            throws ExtensionException, LogoException {
        World world = context.getAgent().world();
        AgentSet links = (AgentSet)args[0].get();
        AgentSet breed = null;
        for (Iterator<Agent> i = links.agents().iterator(); i.hasNext();) {
            Link l = (Link)i.next();
            if (breed == null) {
                breed = l.getBreed();
            } else if (l.getBreed() != breed) {
                breed = world.links();
                break;
            }
        }
        int allLinksVarCount = world.getVariablesArraySize((Link)null, world.links());
        int breedVarCount = world.getVariablesArraySize((Link)null, breed);
        String[] variableNames = new String[breedVarCount];
        for (int i = 0; i < allLinksVarCount; i += 1) {
            variableNames[i] = world.linksOwnNameAt(i);
        }
        for (int i = allLinksVarCount; i < breedVarCount; i += 1) {
            variableNames[i] = world.breedsOwnNameAt(breed, i);
        }
        VectorDataset.PropertyType variableTypes[] = new VectorDataset.PropertyType[breedVarCount];
        Arrays.fill(variableTypes, VectorDataset.PropertyType.NUMBER);
        for (int i = 0; i < variableTypes.length; i += 1) {
            for (Iterator<Agent> j = links.agents().iterator(); j.hasNext();) {
                Object value = j.next().getVariable(i);
                if ((value != null) && (!(value instanceof Number))) {
                    variableTypes[i] = VectorDataset.PropertyType.STRING;
                    break;
                }
            }
        }
        VectorDataset result = new VectorDataset(VectorDataset.ShapeType.LINE, 
                                                 variableNames,
                                                 variableTypes);
        for (Iterator<Agent> i = links.agents().iterator(); i.hasNext();) {
            Link l = (Link)i.next();
            Object[] data = new Object[variableNames.length];
            for (int j = 0; j < variableNames.length; j += 1) {
                Object value = l.getVariable(j);
                if (value instanceof AgentSet) {
                    value = ((AgentSet)value).printName().toLowerCase();
                } else if ((value != null) &&
                           (!(value instanceof Number)) &&
                           (!(value instanceof String))) {
                    value = value.toString();
                }
                data[j] = value;
            }
            Coordinate start = new Coordinate(l.end1().xcor(), l.end1().ycor());
            start = GISExtension.getState().netLogoToGIS(start, start);
            Coordinate end = new Coordinate(l.end2().xcor(), l.end2().ycor());
            end = GISExtension.getState().netLogoToGIS(end, end);
            GeometryFactory f = GISExtension.getState().factory();
            LineString ls = f.createLineString(new Coordinate[] { start, end });
            Geometry geom = f.createMultiLineString(new LineString[] { ls });
            result.add(geom, data);
        }
        return result;
    }
}
