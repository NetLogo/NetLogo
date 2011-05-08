//
// Copyright (c) 2007 Eric Russell. All rights reserved.
//

package org.myworldgis.netlogo;

import com.vividsolutions.jts.algorithm.CentroidArea;
import com.vividsolutions.jts.algorithm.CentroidLine;
import com.vividsolutions.jts.algorithm.CentroidPoint;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryComponentFilter;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.nlogo.api.Argument;
import org.nlogo.api.Context;
import org.nlogo.api.ExtensionException;
import org.nlogo.api.ExtensionObject;
import org.nlogo.api.LogoException;
import org.nlogo.api.LogoList;
import org.nlogo.api.LogoListBuilder;
import org.nlogo.api.Nobody;
import org.nlogo.api.Syntax;


/**
 * 
 */
public final strictfp class VectorFeature implements ExtensionObject {

    //--------------------------------------------------------------------------
    // Inner classes
    //--------------------------------------------------------------------------
    
    /** */
    public static final strictfp class GetProperty extends GISExtension.Reporter {
        
        public String getAgentClassString() {
            return "OTPL";
        }
        
        public Syntax getSyntax() {
            return Syntax.reporterSyntax(new int[] { Syntax.TYPE_WILDCARD,
                                                     Syntax.TYPE_STRING },
                                         Syntax.TYPE_READABLE);
        }
        
        public Object reportInternal (Argument args[], Context context)
                throws ExtensionException, LogoException {
            VectorFeature feature = getFeature(args[0]);
            String key = args[1].getString().toUpperCase();
            if (feature.hasProperty(key)) {
                Object result = feature.getProperty(key);
                if (result == null) {
                    return Nobody.NOBODY;
                } else {
                    return result;
                }
            } else {
                throw new ExtensionException("feature does not have property '" + key + "'");
            }
        }
    }
    
    /** */
    public static final strictfp class GetVertexLists extends GISExtension.Reporter {

        public String getAgentClassString() {
            return "OTPL";
        }
        
        public Syntax getSyntax() {
            return Syntax.reporterSyntax(new int[] { Syntax.TYPE_WILDCARD },
                                         Syntax.TYPE_LIST);
        }
        
        @SuppressWarnings("unchecked")
        public Object reportInternal (Argument args[], Context context)
                throws ExtensionException, LogoException {
            VectorFeature feature = getFeature(args[0]);
            final LogoListBuilder result = new LogoListBuilder();
            feature.getGeometry().apply(new GeometryComponentFilter() {
                    public void filter (Geometry geom) {
                        if (geom instanceof Point) {
                            LogoListBuilder list = new LogoListBuilder() ;
                            list.add(new Vertex(((Point)geom).getCoordinate()));
                            result.add(list.toLogoList());
                        } else if (geom instanceof LineString) {
                            LineString ls = (LineString)geom;
                            LogoListBuilder list = new LogoListBuilder();
                            for (int i = 0; i < ls.getNumPoints(); i += 1) {
                                list.add(new Vertex(ls.getCoordinateN(i)));
                            }
                            result.add(list.toLogoList());
                        }   
                    }
                });
            return result.toLogoList();
        }
    }
    
    /** */
    public static final strictfp class GetCentroid extends GISExtension.Reporter {

        public String getAgentClassString() {
            return "OTPL";
        }
        
        public Syntax getSyntax() {
            return Syntax.reporterSyntax(new int[] { Syntax.TYPE_WILDCARD },
                                         Syntax.TYPE_WILDCARD);
        }
        
        @SuppressWarnings("unchecked")
        public Object reportInternal (Argument args[], Context context)
                throws ExtensionException, LogoException {
            VectorFeature feature = getFeature(args[0]);
            switch (feature.getShapeType()) {
                case POINT:
                    CentroidPoint cp = new CentroidPoint();
                    cp.add(feature.getGeometry());
                    return new Vertex(cp.getCentroid());
                case LINE:
                    CentroidLine cl = new CentroidLine();
                    cl.add(feature.getGeometry());
                    return new Vertex(cl.getCentroid());
                case POLYGON:
                    CentroidArea ca = new CentroidArea();
                    ca.add(feature.getGeometry());
                    return new Vertex(ca.getCentroid());
                default:
                    throw new ExtensionException("invalid shape type");
            }
        }
    }

    //--------------------------------------------------------------------------
    // Class methods
    //--------------------------------------------------------------------------
    
    /** */
    static VectorFeature getFeature (Argument arg) 
            throws ExtensionException, LogoException {
        Object obj = arg.get();
        if (obj instanceof VectorFeature) {
            return (VectorFeature)obj;
        } else {
            throw new ExtensionException("not a VectorFeature: " + obj);
        }
    }
    
    //--------------------------------------------------------------------------
    // Instance variables
    //--------------------------------------------------------------------------
    
    /** */
    private VectorDataset.ShapeType _shapeType;
    
    /** */
    private Geometry _geometry;
    
    /** */
    private Map<String,Object> _properties;
    
    //--------------------------------------------------------------------------
    // Constructors
    //--------------------------------------------------------------------------
    
    /** */
    public VectorFeature (VectorDataset.ShapeType shapeType,
                          Geometry geometry,
                          VectorDataset.Property[] properties,
                          Object[] propertyValues) {
        _shapeType= shapeType;
        _geometry = geometry;
        _properties = new HashMap<String,Object>(properties.length);
        for (int i = 0; i < properties.length; i += 1) {
            _properties.put(properties[i].getName(), propertyValues[i]);   
        }
    }
    
    //--------------------------------------------------------------------------
    // Instance methods
    //--------------------------------------------------------------------------
    
    /** */
    public VectorDataset.ShapeType getShapeType () {
        return _shapeType;
    }
    
    /** */
    public Envelope getEnvelope () {
        return _geometry.getEnvelopeInternal();
    }
    
    /** */
    public Geometry getGeometry () {
        return _geometry;
    }
    
    /** */
    public boolean hasProperty (String name) {
        return _properties.containsKey(name);
    }
    
    /** */
    public Object getProperty (String name) {
        return _properties.get(name.toUpperCase());
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
        StringBuffer buffer = new StringBuffer();
        for (Iterator<String> i = _properties.keySet().iterator(); i.hasNext();) {
            String propertyName = i.next();
            buffer.append("[\"");
            buffer.append(propertyName);
            buffer.append("\":\"");
            buffer.append(_properties.get(propertyName));
            buffer.append("\"]");
        }
        return buffer.toString();
    }

    /** */
    public String getExtensionName () {
        return "gis";
    }

    /** */
    public String getNLTypeName() {
        return "VectorFeature";
    }
    
    /** */
    public boolean recursivelyEqual (Object obj) {
        if (obj instanceof VectorFeature) {
            VectorFeature vf = (VectorFeature)obj;
            return vf == this;
        } else {
            return false;
        }
    }
}
