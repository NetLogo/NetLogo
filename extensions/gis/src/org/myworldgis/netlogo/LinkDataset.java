//
// Copyright (c) 2007 Eric Russell. All rights reserved.
//

package org.myworldgis.netlogo;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import java.util.Arrays;
import java.util.Iterator;
import org.nlogo.api.Agent;
import org.nlogo.api.AgentSet;
import org.nlogo.api.Argument;
import org.nlogo.api.Context;
import org.nlogo.api.ExtensionException;
import org.nlogo.api.Link;
import org.nlogo.api.LogoException;
import org.nlogo.core.Syntax;
import org.nlogo.core.SyntaxJ;
import org.nlogo.api.World;

/**
 *
 */
public final class LinkDataset extends GISExtension.Reporter {

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
        return SyntaxJ.reporterSyntax(new int[] { Syntax.LinksetType() },
                                     Syntax.WildcardType());
    }

    /** */
    public Object reportInternal (Argument args[], Context context)
            throws ExtensionException, LogoException {
        org.nlogo.agent.World world = (org.nlogo.agent.World) context.getAgent().world(); // workaround to weird
        // interface issue. linkBreedsOwnNameAt is in org.nlogo.agent.World but not org.nlogo.api.World, so we
        // need to do a cast here. - James Hovet 1/2021
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
            variableNames[i] = world.linkBreedsOwnNameAt((org.nlogo.agent.AgentSet) breed, i);
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
