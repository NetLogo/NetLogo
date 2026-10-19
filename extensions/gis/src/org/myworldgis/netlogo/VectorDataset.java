//
// Copyright (c) 2007 Eric Russell. All rights reserved.
//

package org.myworldgis.netlogo;

import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.prep.PreparedGeometry;
import org.locationtech.jts.geom.prep.PreparedGeometryFactory;
import org.locationtech.jts.index.ItemVisitor;
import org.locationtech.jts.index.SpatialIndex;
import org.locationtech.jts.index.strtree.STRtree;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.nlogo.api.Argument;
import org.nlogo.api.Context;
import org.nlogo.api.ExtensionException;
import org.nlogo.api.LogoException;
import org.nlogo.core.LogoList;
import org.nlogo.api.LogoListBuilder;
import org.nlogo.core.Syntax;
import org.nlogo.core.SyntaxJ;


/**
 *
 */
public final class VectorDataset extends Dataset {

    //--------------------------------------------------------------------------
    // Inner classes
    //--------------------------------------------------------------------------

    /** */
    public static final class GetFeatures extends GISExtension.Reporter {

        public String getAgentClassString() {
            return "OTPL";
        }

        public Syntax getSyntax() {
            return SyntaxJ.reporterSyntax(new int[] { Syntax.WildcardType() },
                                         Syntax.ListType());
        }

        public Object reportInternal (Argument args[], Context context)
                throws ExtensionException, LogoException {
            try {
                return LogoList.fromJava(getDataset(args[0]).getFeatures());
            } catch (ExtensionException e) {
                throw e;
            } catch (Throwable t) {
                ExtensionException e = new ExtensionException("error parsing envelope");
                e.initCause(t);
                throw e;
            }
        }
    }

    /** */
    public static final class GetPropertyNames extends GISExtension.Reporter {

        public String getAgentClassString() {
            return "OTPL";
        }

        public Syntax getSyntax() {
            return SyntaxJ.reporterSyntax(new int[] { Syntax.WildcardType() },
                                         Syntax.ListType());
        }

        @SuppressWarnings("unchecked")
        public Object reportInternal (Argument args[], Context context)
                throws ExtensionException, LogoException {
            Property[] properties = getDataset(args[0]).getProperties();
            LogoListBuilder result = new LogoListBuilder();
            for (int i = 0; i < properties.length; i += 1) {
                result.add(properties[i].getName());
            }
            return result.toLogoList();
        }
    }

    /** */
    public static enum ShapeType { POINT, LINE, POLYGON }

    /** */
    public static enum PropertyType { STRING, NUMBER }

    /** */
    public static final class Property {

        private final String _name;
        private final PropertyType _type;

        public Property (String name, PropertyType type) {
            _name = name.toUpperCase();
            _type = type;
        }

        public String getName () {
            return _name;
        }

        public PropertyType getType () {
            return _type;
        }
    }

    //--------------------------------------------------------------------------
    // Class methods
    //--------------------------------------------------------------------------

    /** */
    static VectorDataset getDataset (Argument arg)
            throws ExtensionException, LogoException {
        Object obj = arg.get();
        if (obj instanceof VectorDataset) {
            return (VectorDataset)obj;
        } else {
            throw new ExtensionException("not a VectorDataset: " + obj);
        }
    }

    //--------------------------------------------------------------------------
    // Instance variables
    //--------------------------------------------------------------------------

    /** */
    private VectorDataset.ShapeType _shapeType;

    /** */
    private Envelope _envelope;

    /** */
    private ArrayList<VectorFeature> _features;

    /** */
    private SpatialIndex _spatialIndex;

    /** */
    private Property[] _properties;

    //--------------------------------------------------------------------------
    // Constructors
    //--------------------------------------------------------------------------

    /** */
    public VectorDataset (VectorDataset.ShapeType shapeType,
                          String[] propertyNames,
                          PropertyType[] propertyTypes) {
        super("VECTOR");
        _shapeType = shapeType;
        _features = new ArrayList<VectorFeature>();
        _properties = new Property[propertyNames.length];
        for (int i = 0; i < propertyNames.length; i += 1) {
            _properties[i] = new Property(propertyNames[i], propertyTypes[i]);
        }
        reindex();
        GISExtension.getState().datasetLoadNotify();
    }

    //--------------------------------------------------------------------------
    // Instance methods
    //--------------------------------------------------------------------------

    /** */
    public VectorDataset.ShapeType getShapeType () {
        return _shapeType;
    }

    /** */
    public Collection<VectorFeature> getFeatures () {
        return Collections.unmodifiableCollection(_features);
    }

    /** */
    public Property[] getProperties () {
        Property[] result = new Property[_properties.length];
        System.arraycopy(_properties, 0, result, 0, _properties.length);
        return result;
    }

    /** */
    public boolean isValidPropertyName (String name) {
        for (int i = 0; i < _properties.length; i += 1) {
            if (_properties[i].getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    /** */
    public List<VectorFeature> intersectingFeatures (Geometry geom) {
        final PreparedGeometry pGeom = PreparedGeometryFactory.prepare(geom);
        final List<VectorFeature> result = new ArrayList<VectorFeature>();
        _spatialIndex.query(geom.getEnvelopeInternal(), new ItemVisitor() {
                public void visitItem (Object item) {
                    VectorFeature feature = (VectorFeature)item;
                    if (pGeom.intersects(feature.getGeometry())) {
                        result.add(feature);
                    }
                }
            });
        return result;
    }

    /** */
    void add (Geometry geometry, Object[] propertyValues) {
        VectorFeature feature = new VectorFeature(_shapeType,
                                                  geometry,
                                                  _properties,
                                                  propertyValues);
        Envelope featureEnvelope = feature.getEnvelope();
        _envelope.expandToInclude(featureEnvelope);
        _features.add(feature);
        _spatialIndex.insert(featureEnvelope, feature);
    }

    /** */
    private void reindex () {
        _envelope = new Envelope();
        _spatialIndex = new STRtree();
        for (int i = 0; i < _features.size(); i += 1) {
            VectorFeature feature = _features.get(i);
            Envelope featureEnvelope = feature.getEnvelope();
            _envelope.expandToInclude(featureEnvelope);
            _spatialIndex.insert(featureEnvelope, feature);
        }
    }

    //--------------------------------------------------------------------------
    // Dataset implementation
    //--------------------------------------------------------------------------

    /** */
    public Envelope getEnvelope () {
        return new Envelope(_envelope);
    }

    //--------------------------------------------------------------------------
    // ExtensionObject implementation
    //--------------------------------------------------------------------------

    /**
     * Returns a string representation of the object.  If readable is
     * true, it should be possible read it as NL code.
     *
     **/
    public String dump (boolean readable, boolean exporting, boolean reference ) {
        return "";
    }

    /** */
    public String getNLTypeName() {
        return "VectorDataset";
    }

    /** */
    public boolean recursivelyEqual (Object obj) {
        if (obj instanceof VectorDataset) {
            VectorDataset vd = (VectorDataset)obj;
            return vd == this;
        } else {
            return false;
        }
    }
}
