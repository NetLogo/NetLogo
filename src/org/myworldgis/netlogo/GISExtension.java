//
// Copyright (c) 2007 Eric Russell. All rights reserved.
//

package org.myworldgis.netlogo;

import java.io.IOException;
import java.text.ParseException;
import org.nlogo.api.AgentException;
import org.nlogo.api.Argument;
import org.nlogo.api.Context;
import org.nlogo.api.DefaultClassManager;
import org.nlogo.api.ExtensionException;
import org.nlogo.api.ExtensionManager;
import org.nlogo.core.ExtensionObject;
import org.nlogo.api.LogoException;
import org.nlogo.api.PrimitiveManager;


/**
 *
 */
public final class GISExtension extends DefaultClassManager {

    //--------------------------------------------------------------------------
    // Inner classes
    //--------------------------------------------------------------------------

    /** */
    public static abstract class Reporter implements org.nlogo.api.Reporter {

        public abstract Object reportInternal (Argument args[], Context context)
                throws AgentException, ExtensionException, IOException, LogoException, ParseException;

        public Object report (Argument args[], Context context)
                throws ExtensionException, LogoException {
            try {
                return reportInternal(args, context);
            } catch (ExtensionException e) {
                throw e;
            } catch (LogoException e) {
                throw e;
            } catch (Throwable t) {
                t.printStackTrace();
                ExtensionException e = new ExtensionException(t.getMessage());
                e.setStackTrace(t.getStackTrace());
                throw e;
            }
        }
    }

    /** */
    public static abstract class Command implements org.nlogo.api.Command {

        public abstract void performInternal (Argument args[], Context context)
                throws AgentException, ExtensionException, IOException, LogoException, ParseException;

        public void perform (Argument args[], Context context)
                throws ExtensionException, LogoException {
            try {
                performInternal(args, context);
            } catch (ExtensionException e) {
                throw e;
            } catch (LogoException e) {
                throw e;
            } catch (Throwable t) {
                t.printStackTrace();
                ExtensionException e = new ExtensionException(t.getMessage());
                e.setStackTrace(t.getStackTrace());
                throw e;
            }
        }
    }

    //--------------------------------------------------------------------------
    // Class variables
    //--------------------------------------------------------------------------

    /** */
    public static final Double MISSING_VALUE = Double.valueOf(Double.NaN);

    /** */
    private static GISExtensionState _state;

    //--------------------------------------------------------------------------
    // Class methods
    //--------------------------------------------------------------------------

    /** */
    public static GISExtensionState getState () {
        return _state;
    }

    //--------------------------------------------------------------------------
    // DefaultClassManager implementation
    //--------------------------------------------------------------------------

    /** */
    public void runOnce (ExtensionManager em) throws ExtensionException {
        super.runOnce(em);
        _state = new GISExtensionState(em);
    }

    /** */
    public void load (PrimitiveManager primitiveManager) {

        primitiveManager.addPrimitive("set-transformation", new SetTransformation.Linked());
        primitiveManager.addPrimitive("set-transformation-ds", new SetTransformation.Independent());
        primitiveManager.addPrimitive("set-world-envelope", new SetTransformation.WorldLinked());
        primitiveManager.addPrimitive("set-world-envelope-ds", new SetTransformation.WorldIndependent());
        primitiveManager.addPrimitive("world-envelope", new GetEnvelope.OfWorld());
        primitiveManager.addPrimitive("envelope-of", new GetEnvelope.OfObject());
        primitiveManager.addPrimitive("envelope-union-of", new EnvelopeUnion());
        primitiveManager.addPrimitive("load-coordinate-system", new LoadCoordinateSystem());
        primitiveManager.addPrimitive("set-coordinate-system", new SetCoordinateSystem());

        primitiveManager.addPrimitive("load-dataset", new LoadDataset());
        primitiveManager.addPrimitive("load-dataset-from-string", new LoadDatasetFromString());
        primitiveManager.addPrimitive("store-dataset", new StoreDataset());
        primitiveManager.addPrimitive("store-dataset-to-string", new StoreDatasetToString());
        primitiveManager.addPrimitive("store-dataset-to-strings", new StoreDatasetToStrings());
        primitiveManager.addPrimitive("type-of", new Dataset.GetDatasetType());
        primitiveManager.addPrimitive("patch-dataset", new PatchDataset());
        primitiveManager.addPrimitive("turtle-dataset", new TurtleDataset());
        primitiveManager.addPrimitive("link-dataset", new LinkDataset());
        primitiveManager.addPrimitive("create-turtles-from-points", new CreateTurtlesFromPoints.TurtlesFromPointsAutomatic());
        primitiveManager.addPrimitive("create-turtles-from-points-manual", new CreateTurtlesFromPoints.TurtlesFromPointsManual());
        primitiveManager.addPrimitive("create-turtles-inside-polygon", new CreateTurtlesInsidePolygon.TurtlesInsidePolygonAutomatic());
        primitiveManager.addPrimitive("create-turtles-inside-polygon-manual", new CreateTurtlesInsidePolygon.TurtlesInsidePolygonManual());

        primitiveManager.addPrimitive("shape-type-of", new GetShapeType());
        primitiveManager.addPrimitive("property-names", new VectorDataset.GetPropertyNames());
        primitiveManager.addPrimitive("feature-list-of", new VectorDataset.GetFeatures());
        primitiveManager.addPrimitive("vertex-lists-of", new VectorFeature.GetVertexLists());
        primitiveManager.addPrimitive("centroid-of", new VectorFeature.GetCentroid());
        primitiveManager.addPrimitive("random-point-inside", new VectorFeature.GetRandomPointInside());
        primitiveManager.addPrimitive("location-of", new Vertex.GetLocation());
        primitiveManager.addPrimitive("project-lat-lon", new ProjectLatLon.ProjectWGS84());
        primitiveManager.addPrimitive("project-lat-lon-from-ellipsoid", new ProjectLatLon.ProjectFromEllipsoid());
        primitiveManager.addPrimitive("property-value", new VectorFeature.GetProperty());
        primitiveManager.addPrimitive("set-property-value", new VectorFeature.SetProperty());
        primitiveManager.addPrimitive("find-features", new VectorDatasetSearch.FindAll());
        primitiveManager.addPrimitive("find-one-feature", new VectorDatasetSearch.FindOne());
        primitiveManager.addPrimitive("find-less-than", new VectorDatasetSearch.FindLessThan());
        primitiveManager.addPrimitive("find-greater-than", new VectorDatasetSearch.FindGreaterThan());
        primitiveManager.addPrimitive("find-range", new VectorDatasetSearch.FindInRange());
        primitiveManager.addPrimitive("property-minimum", new VectorDatasetSearch.GetMinimum());
        primitiveManager.addPrimitive("property-maximum", new VectorDatasetSearch.GetMaximum());
        primitiveManager.addPrimitive("apply-coverage", new ApplyCoverage.SinglePolygonField());
        primitiveManager.addPrimitive("apply-coverages", new ApplyCoverage.MultiplePolygonFields());
        primitiveManager.addPrimitive("coverage-minimum-threshold", new ApplyCoverage.GetCoverageMinimumThreshold());
        primitiveManager.addPrimitive("set-coverage-minimum-threshold", new ApplyCoverage.SetCoverageMinimumThreshold());
        primitiveManager.addPrimitive("coverage-maximum-threshold", new ApplyCoverage.GetCoverageMaximumThreshold());
        primitiveManager.addPrimitive("set-coverage-maximum-threshold", new ApplyCoverage.SetCoverageMaximumThreshold());
        primitiveManager.addPrimitive("intersects?", new SpatialRelationship.IntersectionTest());
        primitiveManager.addPrimitive("contains?", new SpatialRelationship.ContainsTest());
        primitiveManager.addPrimitive("contained-by?", new SpatialRelationship.ContainedByTest());
        primitiveManager.addPrimitive("have-relationship?", new SpatialRelationship.GeneralTest());
        primitiveManager.addPrimitive("relationship-of", new SpatialRelationship.GetRelationship());
        primitiveManager.addPrimitive("intersecting", new SpatialRelationship.Intersecting());

        primitiveManager.addPrimitive("width-of", new RasterDataset.GetWidth());
        primitiveManager.addPrimitive("height-of", new RasterDataset.GetHeight());
        primitiveManager.addPrimitive("raster-value", new RasterDataset.GetValue());
        primitiveManager.addPrimitive("set-raster-value", new RasterDataset.SetValue());
        primitiveManager.addPrimitive("minimum-of", new RasterDataset.GetMinimum());
        primitiveManager.addPrimitive("maximum-of", new RasterDataset.GetMaximum());
        primitiveManager.addPrimitive("sampling-method-of", new RasterDataset.GetInterpolation());
        primitiveManager.addPrimitive("set-sampling-method", new RasterDataset.SetInterpolation());
        primitiveManager.addPrimitive("raster-sample", new RasterDatasetMath.GetSample());
        primitiveManager.addPrimitive("raster-world-envelope", new RasterDatasetMath.GetWorldEnvelope());
        primitiveManager.addPrimitive("create-raster", new RasterDataset.New());
        primitiveManager.addPrimitive("resample", new RasterDatasetMath.Resample());
        primitiveManager.addPrimitive("convolve", new RasterDatasetMath.Convolve());
        primitiveManager.addPrimitive("apply-raster", new ApplyRaster());

        primitiveManager.addPrimitive("drawing-color", new Painting.GetColor());
        primitiveManager.addPrimitive("set-drawing-color", new Painting.SetColor());
        primitiveManager.addPrimitive("draw", new Painting.DrawVector());
        primitiveManager.addPrimitive("fill", new Painting.FillVector());
        primitiveManager.addPrimitive("paint", new Painting.PaintRaster());

        primitiveManager.addPrimitive("import-wms-drawing", new LoadWMSImage());

        primitiveManager.addPrimitive("myworld-layers", new MyWorld.GetLayers());
        primitiveManager.addPrimitive("myworld-get-dataset", new MyWorld.GetDataset());
        primitiveManager.addPrimitive("myworld-send-dataset", new MyWorld.PutDataset());
    }

    /** */
    public ExtensionObject readExtensionObject (ExtensionManager em, String typeName, String value)
            throws ExtensionException {

        return null;
        //return new LogoArray ( (LogoList) em.readFromString( "[" + value + "]" ) );
    }

}
