package org.myworldgis.util;

import org.locationtech.jts.geom.Coordinate;
import org.myworldgis.netlogo.GISExtension;
import org.myworldgis.netlogo.VectorDataset;
import org.myworldgis.netlogo.VectorFeature;
import org.nlogo.agent.TreeAgentSet;
import org.nlogo.agent.Turtle;
import org.nlogo.agent.World;
import org.nlogo.api.AgentSet;
import org.nlogo.api.AgentException;
import org.nlogo.api.Color;
import org.nlogo.api.ExtensionException;
import org.nlogo.core.LogoList;
import org.nlogo.core.Syntax;
import scala.Option;
import scala.Some;
import scala.jdk.javaapi.CollectionConverters;

import java.util.*;

import static java.util.stream.Collectors.toList;

public class VectorFeaturesToTurtlesUtil {
    private static final Map<String, Double> COLOR_NAMES_TO_COLOR_NUMBERS;
    static { Map<String, Double> tmpMap = new HashMap<String, Double>();
        for (int i = 0; i < Color.ColorNames().length; i ++) {
            tmpMap.put(Color.ColorNames()[i], Color.getColorNumberByIndex(i));
        }
        COLOR_NAMES_TO_COLOR_NUMBERS = Collections.unmodifiableMap(tmpMap);
    }

    // The SyntaxJ convenience class doesn't currently support a command primitive that has a blockAgentClassString
    // Therefore, this function abstracts away the messy work of plugging inputs into the Scala base
    // Syntax.commandSyntax procedure to make the proper syntax for an observer primitive that creates a turtle
    // scope inside its command block. - James Hovet 3/15/21
    public static Syntax makeTurtleCreationCommandSyntax(Object[] syntaxTokens) {
        scala.collection.immutable.List<Object> list = CollectionConverters.asScala(Arrays.asList(syntaxTokens)).toList();
        return Syntax.commandSyntax(list, Option.empty(), Option.empty(), "O---", Some.apply("-T--"), false, true);
    }

    public static void UpdatePropertyNameToTurtleVarMappingsWithManualAdditions(Map<String, Integer> propertyNameToTurtleVarIndexMappings, LogoList manualList, List<String> variableNamesList) throws ExtensionException {
        final String improperSyntaxExceptionMessage = "The variable mapping must be of the form: [[\"property-name\" \"turtle-variable-name\"] [\"property-name\" \"turtle-variable-name\"] (etc.)]";

        for (Object pairing : manualList.javaIterable()) {
            if (!(pairing instanceof LogoList)) {
                throw new ExtensionException(improperSyntaxExceptionMessage);
            }

            LogoList pairingList = ((LogoList) pairing);
            if (pairingList.length() != 2) {
                throw new ExtensionException(improperSyntaxExceptionMessage);
            }

            Object firstObj = pairingList.first();
            Object secondObj = pairingList.butFirst().first();

            if (!(firstObj instanceof String) || !(secondObj instanceof String)) {
                throw new ExtensionException(improperSyntaxExceptionMessage);
            }

            String propertyName = ((String) firstObj).toUpperCase();
            String variableName = ((String) secondObj).toUpperCase();

            int index = variableNamesList.indexOf(variableName);
            if (index != -1) {
                propertyNameToTurtleVarIndexMappings.put(propertyName, index);
            } else {
                throw new ExtensionException("There is no variable " + variableName + " defined. use turtles-own or <breeds>-own to define one.");
            }
        }
    }

    public static Turtle CreateTurtleAtGISCoordinate(TreeAgentSet agentSet, Coordinate coordinate, World world, org.nlogo.nvm.Context nvmContext) throws ExtensionException, AgentException {
        // Copied from default turtle initialization code, otherwise all turtles would be black on creation - James Hovet 3/31/21
        Turtle turtle = world.createTurtle(agentSet, nvmContext.job.random.nextInt(14), 0);

        Coordinate nlogoPosition = GISExtension.getState().gisToNetLogo(coordinate, null);

        if (nlogoPosition == null) { // we tried to create a turtle outside of the GIS envelope
            return null;
        }

        // max-pxcor + 0.5 and max-pycor + 0.5 are illegal x and ycor's in NetLogo but can come up when
        // working from GIS to NetLogo. This isn't a problem if world wrapping is on, but if it isn't
        // we need to nudge them back into the legal area.
        if (! world.wrappingAllowedInX() && nlogoPosition.x == world.maxPxcorBoxed() + 0.5) {
            nlogoPosition.x = Math.nextDown(nlogoPosition.x);
        }
        if (! world.wrappingAllowedInY() && nlogoPosition.y == world.maxPycorBoxed() + 0.5) {
            nlogoPosition.y = Math.nextDown(nlogoPosition.y);
        }

        turtle.setTurtleOrLinkVariable("XCOR", nlogoPosition.x);
        turtle.setTurtleOrLinkVariable("YCOR", nlogoPosition.y);

        agentSet.add(turtle);

        return turtle;
    }

    public static List<String> getVariableNamesListForBreed(World world, AgentSet agentSet) {
        int allTurtlesVarCount = world.getVariablesArraySize((org.nlogo.api.Turtle) null, world.turtles());
        int breedVarCount = world.getVariablesArraySize((org.nlogo.api.Turtle) null, agentSet);
        String[] variableNames = new String[breedVarCount];
        for (int i = 0; i < allTurtlesVarCount; i += 1) {
            variableNames[i] = world.turtlesOwnNameAt(i).toUpperCase();
        }
        for (int i = allTurtlesVarCount; i < breedVarCount; i += 1) {
            variableNames[i] = world.breedsOwnNameAt(agentSet, i).toUpperCase();
        }
        return Arrays.asList(variableNames);
    }


    public static Map<String, Integer> getAutomaticPropertyNameToTurtleVarIndexMappings(List<String> variableNamesList, VectorDataset.Property[] properties) {
        return getAutomaticPropertyNameToTurtleVarIndexMappings(variableNamesList, Arrays.stream(properties).map(VectorDataset.Property::getName).collect(toList()));
    }

    public static Map<String, Integer> getAutomaticPropertyNameToTurtleVarIndexMappings(List<String> variableNamesList, List<String> propertyNamesList) {
        HashMap<String, Integer> propertyNameToTurtleVarIndex = new HashMap<String, Integer>();
        for (String propertyName : propertyNamesList) {
            int index = variableNamesList.indexOf(propertyName.toUpperCase().replace(' ', '-'));
            if (index != -1) {
                propertyNameToTurtleVarIndex.put(propertyName.toUpperCase(), index);
            }
        }
        return propertyNameToTurtleVarIndex;
    }

    public static void setTurtleVariablesToVectorFeatureProperties(Turtle turtle,
                                                            VectorFeature feature,
                                                            Map<String, Integer> propertyNameToTurtleVarIndex) throws AgentException, ExtensionException {
        for (Map.Entry<String, Integer> entry : propertyNameToTurtleVarIndex.entrySet()) {
            World world = turtle.world();
            Integer variableIndex = entry.getValue();
            String variableName = entry.getKey();
            Object valueToSetTo = feature.getProperty(variableName);

            if ((valueToSetTo != null)
                    && variableIndex != world.turtlesOwnIndexOf("BREED")
                    && variableIndex != world.turtlesOwnIndexOf("XCOR")
                    && variableIndex != world.turtlesOwnIndexOf("YCOR")) {

                if ((variableIndex == world.turtlesOwnIndexOf("COLOR") || variableIndex == world.turtlesOwnIndexOf("LABEL-COLOR")) && valueToSetTo instanceof String) {
                    String colorName = (String) valueToSetTo;
                    if (COLOR_NAMES_TO_COLOR_NUMBERS.containsKey(colorName)) {
                        valueToSetTo = COLOR_NAMES_TO_COLOR_NUMBERS.get(colorName);
                    } else {
                        throw new ExtensionException(colorName + " is not a supported color name. Only the default hues or netlogo color number representations are supported. see https://ccl.northwestern.edu/netlogo/docs/programming.html#colors for a list of default colors and a table of color number representations.");
                    }
                }

                if (variableIndex == world.turtlesOwnIndexOf("HIDDEN?") && valueToSetTo instanceof String) {
                    String stringValue = (String) valueToSetTo;
                    if (stringValue.equalsIgnoreCase("true") || Double.parseDouble(stringValue) == 1) {
                        valueToSetTo = true;
                    } else if (stringValue.equalsIgnoreCase("false") || Double.parseDouble(stringValue) == 0) {
                        valueToSetTo = false;
                    } else {
                        throw new ExtensionException(stringValue + " is not a supported boolean value. Only true/false or 0/1 are accepted");
                    }
                }

                if (variableIndex == world.turtlesOwnIndexOf("HIDDEN?") && valueToSetTo instanceof Number) {
                    Double doubleValue = (Double) valueToSetTo;
                    if (doubleValue == 1) {
                        valueToSetTo = true;
                    } else if (doubleValue == 0) {
                        valueToSetTo = false;
                    } else {
                        throw new ExtensionException(doubleValue.toString() + " is not a supported boolean value. Only true/false or 0/1 are accepted");
                    }
                }

                turtle.setTurtleVariable(variableIndex, valueToSetTo);
            }
        }
    }

}
