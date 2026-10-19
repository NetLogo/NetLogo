//
// Copyright (c) 2007 Eric Russell. All rights reserved.
//

package org.myworldgis.netlogo;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import java.util.Arrays;
import java.util.Iterator;
import org.nlogo.api.Agent;
import org.nlogo.api.AgentSet;
import org.nlogo.api.Argument;
import org.nlogo.api.Context;
import org.nlogo.api.ExtensionException;
import org.nlogo.api.LogoException;
import org.nlogo.core.Syntax;
import org.nlogo.core.SyntaxJ;
import org.nlogo.api.Turtle;
import org.nlogo.api.World;

/**
 *
 */
public final class TurtleDataset extends GISExtension.Reporter {

    //--------------------------------------------------------------------------
    // GISExtension.Reporter implementation
    //--------------------------------------------------------------------------

    /** */
    public String getAgentClassString() {
        return "OTPL";
    }

    /** */
    public Syntax getSyntax() {
        return SyntaxJ.reporterSyntax(new int[] { Syntax.TurtlesetType() },
                                     Syntax.WildcardType());
    }

    /** */
    public Object reportInternal (Argument args[], Context context)
            throws ExtensionException, LogoException {
        World world = context.getAgent().world() ;
        AgentSet turtles = args[0].getAgentSet() ;
        AgentSet breed = null;
        for (Iterator<Agent> i = turtles.agents().iterator(); i.hasNext();)
        {
            Turtle t = (Turtle) i.next() ;
            if (breed == null) {
                breed = t.getBreed();
            } else if (t.getBreed() != breed) {
                breed = world.turtles();
                break;
            }
        }
        int allTurtlesVarCount = world.getVariablesArraySize((Turtle)null, world.turtles());
        int breedVarCount = world.getVariablesArraySize((Turtle)null, breed);
        String[] variableNames = new String[breedVarCount];
        for (int i = 0; i < allTurtlesVarCount; i += 1) {
            variableNames[i] = world.turtlesOwnNameAt(i);
        }
        for (int i = allTurtlesVarCount; i < breedVarCount; i += 1) {
            variableNames[i] = world.breedsOwnNameAt(breed, i);
        }
        VectorDataset.PropertyType variableTypes[] = new VectorDataset.PropertyType[breedVarCount];
        Arrays.fill(variableTypes, VectorDataset.PropertyType.NUMBER);
        for (int i = 0; i < variableTypes.length; i += 1) {
            for (Iterator<Agent> j = turtles.agents().iterator(); j.hasNext();) {
                Object value = ((Turtle)j.next()).getVariable(i);
                if ((value != null) && (!(value instanceof Number))) {
                    variableTypes[i] = VectorDataset.PropertyType.STRING;
                    break;
                }
            }
        }
        VectorDataset result = new VectorDataset(VectorDataset.ShapeType.POINT,
                                                 variableNames,
                                                 variableTypes);
        for (Iterator<Agent> i = turtles.agents().iterator(); i.hasNext();) {
            Turtle t = (Turtle)i.next();
            Object[] data = new Object[variableNames.length];
            for (int j = 0; j < variableNames.length; j += 1) {
                Object value = t.getVariable(j);
                if (value instanceof AgentSet) {
                    value = ((AgentSet)value).printName().toLowerCase();
                } else if ((value != null) &&
                           (!(value instanceof Number)) &&
                           (!(value instanceof String))) {
                    value = value.toString();
                }
                data[j] = value;
            }
            Coordinate loc = new Coordinate(t.xcor(), t.ycor());
            loc = GISExtension.getState().netLogoToGIS(loc, loc);
            Geometry geom = GISExtension.getState().factory().createPoint(loc);
            result.add(geom, data);
        }
        return result;
    }
}
