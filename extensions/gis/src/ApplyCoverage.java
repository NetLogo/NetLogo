//
// Copyright (c) 2007 Eric Russell. All rights reserved.
//

package org.myworldgis.netlogo;

import com.vividsolutions.jts.geom.Geometry;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import org.myworldgis.util.JTSUtils;
import org.nlogo.api.AgentException;
import org.nlogo.api.Argument;
import org.nlogo.api.Context;
import org.nlogo.api.ExtensionException;
import org.nlogo.api.LogoException;
import org.nlogo.api.LogoList;
import org.nlogo.api.Patch;
import org.nlogo.api.Syntax;
import org.nlogo.api.World;
import org.nlogo.prim._reference;


/**
 * 
 */
public abstract strictfp class ApplyCoverage {
    
    //--------------------------------------------------------------------------
    // Inner classes
    //--------------------------------------------------------------------------
    
    /** */
    public static final strictfp class SinglePolygonField extends GISExtension.Command {
        
        public String getAgentClassString() {
            return "O";
        }
        
        public Syntax getSyntax() {
            return Syntax.commandSyntax(new int[] { Syntax.TYPE_WILDCARD, 
                                                    Syntax.TYPE_STRING, 
                                                    Syntax.TYPE_REFERENCE });
        }
        
        public void performInternal (Argument args[], Context context) 
                throws AgentException, ExtensionException, LogoException {
            Object arg0 = args[0].get();
            if (!(arg0 instanceof VectorDataset)) {
                throw new ExtensionException("not a VectorDataset: " + arg0);
            }
            applyCoverages(context.getAgent().world(),
                           (VectorDataset)arg0,
                           new String[] { args[1].getString() },
                           new _reference[] { (_reference)((org.nlogo.nvm.Argument)args[2]).getReporter() });
        }
    }
    
    /** */
    @SuppressWarnings("unchecked")
    public static final strictfp class MultiplePolygonFields extends GISExtension.Command {

        public String getAgentClassString() {
            return "O";
        }
        
        public Syntax getSyntax() {
            
            // This doesn't work because NetLogo keeps trying to evaluate
            // my references to patch variables in the Observer context 
            // rather than passing them along without evaluating them. 
            // Removing the TYPE_REPEATABLE from the third argument will 
            // fix that problem, but then you can no longer pass the 
            // command a variable number of patch variable references, 
            // which kind of defeats the whole purpose. Since I don't know 
            // anything about how the NetLogo compiler works, debugging this 
            // problem would take way more time than I have available, but 
            // if someone else were willing to take a look, I'd really 
            // appreciate it. - ER 12/13/07
            
            return Syntax.commandSyntax(new int[] { Syntax.TYPE_WILDCARD, 
                                                    Syntax.TYPE_LIST, 
                                                    Syntax.TYPE_REFERENCE | Syntax.TYPE_REPEATABLE },
                                        3);
        }
        
        public void performInternal (Argument args[], Context context) 
                throws AgentException, ExtensionException, LogoException {
            Object arg0 = args[0].get();
            if (!(arg0 instanceof VectorDataset)) {
                throw new ExtensionException("not a VectorDataset: " + arg0);
            }
            int propertyCount = args.length - 2;
            LogoList arg1 = args[1].getList();
            if (arg1.size() != propertyCount) {
                throw new ExtensionException("number of properties must match the number of patch variables");
            }
            String[] properties = new String[arg1.size()];
            int idx = 0;
            for (Iterator i = arg1.iterator(); i.hasNext();) {
                properties[idx++] = (String)i.next();
            }
            _reference[] variables = new _reference[propertyCount];
            for (int i = 0; i < propertyCount; i += 1) {
                variables[i] = (_reference)((org.nlogo.nvm.Argument)args[i+2]).getReporter();
            }
            applyCoverages(context.getAgent().world(),
                           (VectorDataset)arg0,
                           properties,
                           variables);
        }
    }
    
    /** */
    public static final strictfp class GetCoverageMinimumThreshold extends GISExtension.Reporter {
        
        public String getAgentClassString() {
            return "O";
        }
        
        public Syntax getSyntax() {
            return Syntax.reporterSyntax(new int[] { }, Syntax.TYPE_NUMBER);
        }
        
        public Object reportInternal (Argument args[], Context context) 
                throws AgentException, ExtensionException, LogoException {
            return new Double(GISExtension.getState().getCoverageSingleCellThreshold());
        }
    }
    
    /** */
    public static final strictfp class SetCoverageMinimumThreshold extends GISExtension.Command {
        
        public String getAgentClassString() {
            return "O";
        }
        
        public Syntax getSyntax() {
            return Syntax.commandSyntax(new int[] { Syntax.TYPE_NUMBER });
        }
        
        public void performInternal (Argument args[], Context context) 
                throws AgentException, ExtensionException, LogoException {
            GISExtension.getState().setCoverageSingleCellThreshold(args[0].getDoubleValue());
        }
    }
    
    /** */
    public static final strictfp class GetCoverageMaximumThreshold extends GISExtension.Reporter {
        
        public String getAgentClassString() {
            return "O";
        }
        
        public Syntax getSyntax() {
            return Syntax.reporterSyntax(new int[] { }, Syntax.TYPE_NUMBER);
        }
        
        public Object reportInternal (Argument args[], Context context) 
                throws AgentException, ExtensionException, LogoException {
            return new Double(GISExtension.getState().getCoverageMultipleCellThreshold());
        }
    }
    
    /** */
    public static final strictfp class SetCoverageMaximumThreshold extends GISExtension.Command {
        
        public String getAgentClassString() {
            return "O";
        }
        
        public Syntax getSyntax() {
            return Syntax.commandSyntax(new int[] { Syntax.TYPE_NUMBER });
        }
        
        public void performInternal (Argument args[], Context context) 
                throws AgentException, ExtensionException, LogoException {
            GISExtension.getState().setCoverageMultipleCellThreshold(args[0].getDoubleValue());
        }
    }
    
    /** */
    private static final strictfp class ValueRecord {
        
        private Object _value;
        private double _areaRatio;
        
        public ValueRecord (Object value, double areaRatio) {
            _value = value;
            _areaRatio = areaRatio;
        }
        
        public Object getValue () {
            return _value;
        }
        
        public double getWeight () {
            return _areaRatio;
        }
    }
    
    //--------------------------------------------------------------------------
    // Class methods
    //--------------------------------------------------------------------------
    
    /** */
    static void applyCoverages (World world,
                                VectorDataset dataset,
                                String[] properties,
                                _reference[] variables) 
            throws AgentException, ExtensionException , LogoException {
        for (int i = 0; i < properties.length; i += 1) {
            if (!dataset.isValidPropertyName(properties[i])) {
                throw new ExtensionException(properties[i] + " is not a valid property name");
            }
        }
        double singleCellThreshold = GISExtension.getState().getCoverageSingleCellThreshold();
        for (int px = world.minPxcor(); px <= world.maxPxcor(); px += 1) {
            for (int py = world.minPycor(); py <= world.maxPycor(); py += 1) {
                Patch p = world.fastGetPatchAt(px, py);
                Geometry patchGeometry = GISExtension.getState().agentGeometry(p);
                List<VectorFeature> features = dataset.intersectingFeatures(patchGeometry);
                if (features.size() > 1) {
                    Object[] values = aggregatePropertyValues(patchGeometry, properties, features);
                    for (int i = 0; i < properties.length; i += 1) {
                        p.setVariable(variables[i].reference.vn(), values[i]);
                    }
                } else if ((features.size() == 1) &&
                           (JTSUtils.fastGetSharedAreaRatio(patchGeometry, features.get(0).getGeometry()) > singleCellThreshold)) {
                    for (int i = 0; i < properties.length; i += 1) {
                        p.setVariable(variables[i].reference.vn(), features.get(0).getProperty(properties[i]));
                    }
                } else {
                    for (int i = 0; i < properties.length; i += 1) {
                        p.setVariable(variables[i].reference.vn(), GISExtension.MISSING_VALUE);
                    }
                }
            }
        }
    }
    
    /** */
    static Object[] aggregatePropertyValues (Geometry patchGeometry,
                                             String[] propertyNames, 
                                             List<VectorFeature> features) {
        int maxAreaIndex = -1;
        double maxAreaRatio = 0.0;
        ValueRecord[][] records = new ValueRecord[propertyNames.length][features.size()];
        boolean[] categoricalProperty = new boolean[propertyNames.length];
        Arrays.fill(categoricalProperty, false);
        for (int i = 0; i < features.size(); i += 1) {
            VectorFeature feature = features.get(i);
            Geometry geom = feature.getGeometry();
            double areaRatio = JTSUtils.fastGetSharedAreaRatio(patchGeometry, geom);
            if (areaRatio > maxAreaRatio) {
                maxAreaIndex = i;
                maxAreaRatio = areaRatio;
            }
            for (int j = 0; j < propertyNames.length; j += 1) {
                Object value = feature.getProperty(propertyNames[j]);
                if (value instanceof String) {
                    categoricalProperty[j] = true;
                }
                records[j][i] = new ValueRecord(value, areaRatio);
            }
        }
        if (maxAreaRatio == 0.0) {
            for (int i = 0; i < features.size(); i += 1) {
                VectorFeature feature = features.get(i);
                Geometry geom = feature.getGeometry();
                double areaRatio = JTSUtils.getSharedAreaRatio(patchGeometry, geom);
                if (areaRatio > maxAreaRatio) {
                    maxAreaIndex = i;
                    maxAreaRatio = areaRatio;
                }
                for (int j = 0; j < propertyNames.length; j += 1) {
                    Object value = feature.getProperty(propertyNames[j]);
                    if (value instanceof String) {
                        categoricalProperty[j] = true;
                    }
                    records[j][i] = new ValueRecord(value, areaRatio);
                }
            }
        }
        Object[] result = new Object[propertyNames.length];
        if (maxAreaRatio == 0.0) {
            for (int j = 0; j < propertyNames.length; j += 1) {
                result[j] = GISExtension.MISSING_VALUE;
            }
        } else if (maxAreaRatio >= GISExtension.getState().getCoverageMultipleCellThreshold()) {
            for (int j = 0; j < propertyNames.length; j += 1) {
                result[j] = records[j][maxAreaIndex].getValue();
            }
        } else {
            for (int j = 0; j < propertyNames.length; j += 1) {
                if (categoricalProperty[j]) {
                    result[j] = majority(records[j]);
                } else {
                    result[j] = weightedAverage(records[j]);
                }
            }
        }
        return result;
    }
    
    
    /** */
    static Object majority (ValueRecord[] records) {
        HashMap<Object,Double> map = new HashMap<Object,Double>();
        double maxWeight = 0.0;
        Object maxWeightValue = null;
        for (int i = 0; i < records.length; i += 1) {
            Object value = records[i].getValue();
            if (value != null) {
                Double weight = map.get(value);
                if (weight == null) {
                    weight = Double.valueOf(records[i].getWeight());
                } else {
                    weight = Double.valueOf(weight.doubleValue() + records[i].getWeight());
                }
                if (weight.doubleValue() > maxWeight) {
                    maxWeight = weight.doubleValue();
                    maxWeightValue = value;
                }
                map.put(value, weight);
            }
        }
        return maxWeightValue;
    }
    
    /** */
    static Object weightedAverage (ValueRecord[] records) {
        double totalValue = 0.0;
        double totalWeight = 0.0;
        for (int i = 0; i < records.length; i += 1) {
            Number n = (Number)records[i].getValue();
            if (n != null) {
                double value = n.doubleValue();
                if ((!Double.isNaN(value)) && (!Double.isInfinite(value))) {
                    double weight = records[i].getWeight();
                    totalValue += (value * weight);
                    totalWeight += weight;
                }
            }
        }
        return Double.valueOf(totalValue / totalWeight);
    }
}
