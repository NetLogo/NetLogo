package org.myworldgis.netlogo;

import org.locationtech.jts.geom.Coordinate;
import org.myworldgis.util.VectorFeaturesToTurtlesUtil;
import org.nlogo.agent.AgentSet;
import org.nlogo.agent.TreeAgentSet;
import org.nlogo.agent.Turtle;
import org.nlogo.agent.World;
import org.nlogo.api.AgentException;
import org.nlogo.api.Argument;
import org.nlogo.api.ExtensionException;
import org.nlogo.core.LogoList;
import org.nlogo.core.Syntax;
import org.nlogo.nvm.AssemblerAssistant;
import org.nlogo.nvm.CustomAssembled;
import org.nlogo.nvm.ExtensionContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class CreateTurtlesInsidePolygon {

    private static abstract class TurtlesInsidePolygon extends GISExtension.Command implements CustomAssembled {

        protected abstract Map<String, Integer> getPropertyNameToTurtleVarIndex(List<String> variableNamesList, List<String> properties, Argument[] args) throws ExtensionException;

        public void performInternal (Argument args[],org.nlogo.api.Context context)
            throws ExtensionException, AgentException {
            ExtensionContext eContext = (ExtensionContext) context;
            org.nlogo.nvm.Context nvmContext = eContext.nvmContext();
            World world = (org.nlogo.agent.World) context.world();

            if (!(args[0].get() instanceof VectorFeature)) {
                throw new ExtensionException("Not a VectorFeature");
            }

            VectorFeature vectorFeature = (VectorFeature) args[0].get();
            if (vectorFeature.getShapeType() != VectorDataset.ShapeType.POLYGON) {
                throw new ExtensionException("Not a polygon feature");
            }

            org.nlogo.api.AgentSet agentSetCandidate = args[1].getAgentSet();
            if (agentSetCandidate.printName() == null) {
                throw new ExtensionException("Expected breed, received non-breed turtleset");
            }

            TreeAgentSet breedAgentSet;
            if (agentSetCandidate.printName().equalsIgnoreCase("turtles")) {
                breedAgentSet = world.turtles();
            } else {
                breedAgentSet = world.getBreed(agentSetCandidate.printName());
            }

            int numToMake = args[2].getIntValue();

            List<String> variableNamesList = VectorFeaturesToTurtlesUtil.getVariableNamesListForBreed(world, breedAgentSet);
            List<String> propertyNamesList = Arrays.asList(vectorFeature.getPropertyNames());

            Map<String, Integer> propertyNameToTurtleVarIndex = getPropertyNameToTurtleVarIndex(variableNamesList, propertyNamesList, args);

            TreeAgentSet createdAgentSet = new TreeAgentSet(breedAgentSet.kind(), "temp_agentset_for_create_turtles_inside_polygon");
            for (int i = 0; i < numToMake; i++){
                Coordinate coord = vectorFeature.getRandomPointInsidePolygon(context.getRNG());
                Turtle turtle = VectorFeaturesToTurtlesUtil.CreateTurtleAtGISCoordinate(breedAgentSet, coord, world, nvmContext);
                if (turtle == null) {
                    GISExtension.getState().displayWarning("Tried to create turtle outside GIS envelope at: " + coord.toString()
                            + ". Try setting your GIS world envelope with gis:set-world-envelope (gis:envelope-union-of "
                            + "(gis:envelope-of your-first-dataset) (gis:envelope-of your-second-dataset) etc.))");
                    continue;
                }
                VectorFeaturesToTurtlesUtil.setTurtleVariablesToVectorFeatureProperties(turtle, vectorFeature, propertyNameToTurtleVarIndex);
                createdAgentSet.add(turtle);
            }

            nvmContext.runExclusiveJob(createdAgentSet, nvmContext.ip + 1);
        }

        public void assemble (AssemblerAssistant assemblerAssistant){
            assemblerAssistant.block();
            assemblerAssistant.done();
        }
    }

    public static class TurtlesInsidePolygonAutomatic extends TurtlesInsidePolygon {

        public Syntax getSyntax() {
            return VectorFeaturesToTurtlesUtil.makeTurtleCreationCommandSyntax(new Object[]{Syntax.WildcardType(), Syntax.TurtlesetType(), Syntax.NumberType(), Syntax.CommandBlockType() | Syntax.OptionalType()});
        }

        public Map<String, Integer> getPropertyNameToTurtleVarIndex(List<String> variableNamesList, List<String> properties, Argument[] args) {
            return VectorFeaturesToTurtlesUtil.getAutomaticPropertyNameToTurtleVarIndexMappings(variableNamesList, properties);
        }
    }

    public static class TurtlesInsidePolygonManual extends TurtlesInsidePolygon {

        public Syntax getSyntax() {
            return VectorFeaturesToTurtlesUtil.makeTurtleCreationCommandSyntax(new Object[]{Syntax.WildcardType(), Syntax.TurtlesetType(), Syntax.NumberType(), Syntax.ListType(), Syntax.CommandBlockType() | Syntax.OptionalType()});
        }

        public Map<String, Integer> getPropertyNameToTurtleVarIndex(List<String> variableNamesList, List<String> properties, Argument[] args) throws ExtensionException {
            Map<String, Integer> propertyNameToTurtleVarIndexMappings = VectorFeaturesToTurtlesUtil.getAutomaticPropertyNameToTurtleVarIndexMappings(variableNamesList, properties);
            LogoList manualList = args[3].getList();
            VectorFeaturesToTurtlesUtil.UpdatePropertyNameToTurtleVarMappingsWithManualAdditions(propertyNameToTurtleVarIndexMappings, manualList, variableNamesList);
            return propertyNameToTurtleVarIndexMappings;
        }
    }
}
