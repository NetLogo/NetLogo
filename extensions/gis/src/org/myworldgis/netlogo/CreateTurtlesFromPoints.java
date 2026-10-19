package org.myworldgis.netlogo;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.myworldgis.util.VectorFeaturesToTurtlesUtil;
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

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class CreateTurtlesFromPoints {

    private static abstract class TurtlesFromPoints extends GISExtension.Command implements CustomAssembled {

        protected abstract Map<String, Integer> getPropertyNameToTurtleVarIndex(List<String> variableNamesList, VectorDataset.Property[] properties, Argument[] args) throws ExtensionException;

        public void performInternal(Argument args[], org.nlogo.api.Context context)
                throws ExtensionException, AgentException {
            ExtensionContext eContext = (ExtensionContext) context;
            org.nlogo.nvm.Context nvmContext = eContext.nvmContext();
            World world = (org.nlogo.agent.World) context.world();

            if (!(args[0].get() instanceof VectorDataset)) {
                throw new ExtensionException("Not a VectorDataset");
            }

            VectorDataset dataset = (VectorDataset) args[0].get();
            if (dataset.getShapeType() != VectorDataset.ShapeType.POINT) {
                throw new ExtensionException("Not a point dataset");
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

            List<String> variableNamesList = VectorFeaturesToTurtlesUtil.getVariableNamesListForBreed(world, breedAgentSet);
            VectorDataset.Property[] properties = dataset.getProperties();

            Map<String, Integer> propertyNameToTurtleVarIndex = getPropertyNameToTurtleVarIndex(variableNamesList, properties, args);

            Collection<VectorFeature> features = dataset.getFeatures();
            for (VectorFeature feature : features) {
                Geometry geom = feature.getGeometry();
                for (int subPointIndex = 0; subPointIndex < geom.getNumGeometries(); subPointIndex++) {
                    Geometry thisPoint = geom.getGeometryN(subPointIndex);
                    Coordinate coord = thisPoint.getCoordinate();
                    if (coord == null) {
                        GISExtension.getState().displayWarning("Tried to create turtle at a position that doesn't exist. "
                                + "This can happen if you are, say, using an orthographic projection and the point at which "
                                + "you are trying to create a turtle is on the other side of the globe.");
                        continue;
                    }
                    Turtle turtle = VectorFeaturesToTurtlesUtil.CreateTurtleAtGISCoordinate(breedAgentSet, thisPoint.getCoordinate(), world, nvmContext);
                    if (turtle == null) {
                        GISExtension.getState().displayWarning("Tried to create turtle outside GIS envelope at: " + thisPoint.getCoordinate().toString()
                                + ". Try setting your GIS world envelope with gis:set-world-envelope (gis:envelope-union-of "
                                + "(gis:envelope-of your-first-dataset) (gis:envelope-of your-second-dataset) etc.))");
                        continue;
                    }
                    VectorFeaturesToTurtlesUtil.setTurtleVariablesToVectorFeatureProperties(turtle, feature, propertyNameToTurtleVarIndex);
                }
            }

            nvmContext.runExclusiveJob(breedAgentSet, nvmContext.ip + 1);
        }

        public void assemble(AssemblerAssistant assemblerAssistant) {
            assemblerAssistant.block();
            assemblerAssistant.done();
        }

    }

    public static class TurtlesFromPointsAutomatic extends TurtlesFromPoints {

        public Syntax getSyntax() {
            return VectorFeaturesToTurtlesUtil.makeTurtleCreationCommandSyntax(new Object[]{Syntax.WildcardType(), Syntax.TurtlesetType(), Syntax.CommandBlockType() | Syntax.OptionalType()});
        }

        public Map<String, Integer> getPropertyNameToTurtleVarIndex(List<String> variableNamesList, VectorDataset.Property[] properties, Argument[] args) {
            return VectorFeaturesToTurtlesUtil.getAutomaticPropertyNameToTurtleVarIndexMappings(variableNamesList, properties);
        }
    }

    public static class TurtlesFromPointsManual extends TurtlesFromPoints {

        public Syntax getSyntax() {
            return VectorFeaturesToTurtlesUtil.makeTurtleCreationCommandSyntax(new Object[]{Syntax.WildcardType(), Syntax.TurtlesetType(), Syntax.ListType(), Syntax.CommandBlockType() | Syntax.OptionalType()});
        }

        public Map<String, Integer> getPropertyNameToTurtleVarIndex(List<String> variableNamesList, VectorDataset.Property[] properties, Argument[] args) throws ExtensionException {
            Map<String, Integer> propertyNameToTurtleVarIndexMappings = VectorFeaturesToTurtlesUtil.getAutomaticPropertyNameToTurtleVarIndexMappings(variableNamesList, properties);
            LogoList manualList = args[2].getList();
            VectorFeaturesToTurtlesUtil.UpdatePropertyNameToTurtleVarMappingsWithManualAdditions(propertyNameToTurtleVarIndexMappings, manualList, variableNamesList);
            return propertyNameToTurtleVarIndexMappings;
        }
    }
}
