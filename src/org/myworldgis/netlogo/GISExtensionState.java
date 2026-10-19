//
// Copyright (c) 2007 Eric Russell. All rights reserved.
//

package org.myworldgis.netlogo;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import java.awt.Color;
import java.io.IOException;
import org.myworldgis.projection.Projection;
import org.nlogo.api.Context;
import org.nlogo.api.Dump;
import org.nlogo.api.ExtensionException;
import org.nlogo.core.ExtensionObject;
import org.nlogo.core.File;
import org.nlogo.api.Link;
import org.nlogo.api.LogoException;
import org.nlogo.api.OutputDestinationJ;
import org.nlogo.api.Patch;
import org.nlogo.api.Turtle;
import org.nlogo.headless.HeadlessWorkspace;
import org.nlogo.nvm.ExtensionContext;
import org.nlogo.nvm.Workspace;
import org.nlogo.workspace.AbstractWorkspace;
import org.nlogo.workspace.ExtendableWorkspace;
import org.nlogo.workspace.ExtensionManager;


/**
 *
 */
public final class GISExtensionState implements ExtensionObject {

    //--------------------------------------------------------------------------
    // Instance variables
    //--------------------------------------------------------------------------

    /** */
    private final ExtensionManager _em;

    /** */
    private final GeometryFactory _factory;

    /** */
    private Projection _projection;

    /** */
    private int _datasetCount;

    /** */
    private CoordinateTransformation _transformation;

    /** */
    private Color _color;

    /** */
    private Object _nlColor;

    /** */
    private double _coverageSingleCellThreshold;

    /** */
    private double _coverageMultipleCellThreshold;

    private static ExtendableWorkspace _workspace;

    //--------------------------------------------------------------------------
    // Constructors
    //--------------------------------------------------------------------------

    /** */
    public GISExtensionState (org.nlogo.api.ExtensionManager em) {
        _em = (ExtensionManager)em;
        _factory = new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING));
        _projection = null;
        _datasetCount = 0;
        _transformation = null;
        setNetLogoColor(org.nlogo.api.Color.BoxedBlack());
        _coverageSingleCellThreshold = 0.1;
        _coverageMultipleCellThreshold = 0.33;
        _workspace = _em.workspace();
    }

    //--------------------------------------------------------------------------
    // Instance methods
    //--------------------------------------------------------------------------

    public void displayWarning(String warning) {
        if (_workspace instanceof HeadlessWorkspace) {
            _workspace.warningMessage(warning);
        } else if (_workspace instanceof AbstractWorkspace) {
            AbstractWorkspace ws = (AbstractWorkspace) _workspace;
            ws.outputObject(warning, null, true, false, OutputDestinationJ.NORMAL());
        }
    }

    public void displayWarning(String warning, Object owner) {
        if (_workspace instanceof HeadlessWorkspace) {
            _workspace.warningMessage(warning);
        } else if (_workspace instanceof AbstractWorkspace) {
            AbstractWorkspace ws = (AbstractWorkspace) _workspace;
            try {
                ws.outputObject(warning, owner, true, false, OutputDestinationJ.NORMAL());
            } catch (LogoException e) {
                displayWarning(warning);
            }
        }
    }

    /** */
    public File getFile (String path) {
        try {
            String fullPath = _em.workspace().fileManager().attachPrefix(path);
            if (_em.workspace().fileManager().fileExists(fullPath)) {
                return _em.workspace().fileManager().getFile(fullPath);
            }
        } catch (IOException e) { }
        return null;
    }

    /** */
    public GeometryFactory factory () {
        return _factory;
    }

    /** */
    public Projection getProjection () {
        return _projection;
    }

    /** */
    public void setProjection (Projection newProjection, Context c) {
        if ((_datasetCount > 0) && (c != null)) {
            Workspace ws = ((ExtensionContext)c).workspace();
            try {
                ws.outputObject("GIS Extension Warning: datasets previously loaded will not be re-projected to match the new projection.",
                                c.getAgent(),
                                true,
                                false,
                                OutputDestinationJ.NORMAL()) ;
            } catch (LogoException e) { }
        }
        _projection = newProjection;
        _datasetCount = 0;

    }

    /** */
    public void datasetLoadNotify () {
        _datasetCount += 1;
    }

    private static final ExtensionException noCoordinateTransformationSetException = new ExtensionException("you must define a coordinate transformation before using any other GIS features. You can use gis:set-transformation directly, or use `gis:set-world-envelope gis:envelope-of dataset` to automatically frame up a given dataset.");

    /** */
    public Coordinate netLogoToGIS (Coordinate pt, Coordinate storage)
            throws ExtensionException {
        if (_transformation == null) {
            throw noCoordinateTransformationSetException;
        } else {
            return _transformation.netLogoToGIS(pt, storage);
        }
    }

    /** */
    public Coordinate gisToNetLogo (Coordinate pt, Coordinate storage)
            throws ExtensionException {
        if (_transformation == null) {
            throw noCoordinateTransformationSetException;
        } else {
            return _transformation.gisToNetLogo(pt, storage);
        }
    }

    /** */
    public CoordinateTransformation getTransformation ()
            throws ExtensionException  {
        if (_transformation == null) {
            throw noCoordinateTransformationSetException;
        } else {
            return _transformation;
        }
    }

    /** */
    public boolean isTransformationSet () {
        return (_transformation != null);
    }

    /** */
    public void setTransformation (CoordinateTransformation newTransformation) {
        _transformation = newTransformation;
    }

    /** */
    public Geometry agentGeometry (org.nlogo.api.Agent agent) throws ExtensionException {
        if (agent instanceof Turtle) {
            Turtle t = (Turtle)agent;
            return _factory.createPoint(netLogoToGIS(new Coordinate(t.xcor(), t.ycor()), null));
        } else if (agent instanceof Patch) {
            Patch p = (Patch)agent;
            Coordinate bl = netLogoToGIS(new Coordinate(p.pxcor()-0.5, p.pycor()-0.5), null);
            Coordinate tr = netLogoToGIS(new Coordinate(p.pxcor()+0.5, p.pycor()+0.5), null);
            return _factory.toGeometry(new Envelope(bl, tr));
        } else if (agent instanceof Link) {
            Turtle t1 = ((Link)agent).end1();
            Turtle t2 = ((Link)agent).end2();
            Coordinate c1 = netLogoToGIS(new Coordinate(t1.xcor(), t1.ycor()), null);
            Coordinate c2 = netLogoToGIS(new Coordinate(t2.xcor(), t2.ycor()), null);
            return _factory.createLineString(new Coordinate[] { c1, c2 });
        } else {
            throw new ExtensionException("unrecognized agent type: " + Dump.logoObject(agent));
        }
    }

    /** */
    public Color getColor () {
        return _color;
    }

    /** */
    public Object getNetLogoColor () {
        return _nlColor;
    }

    /** */
    public void setColor (Color newColor) {
        _color = newColor;
        _nlColor = org.nlogo.api.Color.getRGBListByARGB((0xff << 24) +
                                                        (newColor.getRed() << 16) +
                                                        (newColor.getGreen() << 8) +
                                                        (newColor.getBlue()));
    }

    /** */
    public void setNetLogoColor (Object newColor) {
        _nlColor = newColor;
        _color = org.nlogo.api.Color.getColor(newColor);
    }

    /** */
    public double getCoverageSingleCellThreshold () {
        return _coverageSingleCellThreshold;
    }

    /** */
    public void setCoverageSingleCellThreshold (double newThreshold) {
        _coverageSingleCellThreshold = newThreshold;
    }

    /** */
    public double getCoverageMultipleCellThreshold () {
        return _coverageMultipleCellThreshold;
    }

    /** */
    public void setCoverageMultipleCellThreshold (double newThreshold) {
        _coverageMultipleCellThreshold = newThreshold;
    }

    //--------------------------------------------------------------------------
    // ExtensionObject implementation
    //--------------------------------------------------------------------------

    /**
     * Returns a string representation of the object.  If readable is
     * true, it should be possible read it as NL code.
     *
     **/
    public String dump (boolean readable, boolean exporting, boolean reference) {
        return "";
    }

    /** */
    public String getExtensionName () {
        return "gis";
    }

    /** */
    public String getNLTypeName() {
        return "State";
    }

    /** */
    public boolean recursivelyEqual (Object obj) {
        if (obj instanceof VectorDataset) {
            GISExtensionState s = (GISExtensionState)obj;
            return s == this;
        } else {
            return false;
        }
    }
}
