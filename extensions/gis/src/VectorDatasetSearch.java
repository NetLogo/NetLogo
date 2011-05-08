//
// Copyright (c) 2007 Eric Russell. All rights reserved.
//

package org.myworldgis.netlogo;

import java.util.Iterator;
import org.myworldgis.util.StringUtils;
import org.nlogo.api.Argument;
import org.nlogo.api.Context;
import org.nlogo.api.ExtensionException;
import org.nlogo.api.LogoException;
import org.nlogo.api.LogoListBuilder;
import org.nlogo.api.Nobody;
import org.nlogo.api.Syntax;


/**
 * 
 */
public abstract strictfp class VectorDatasetSearch {
    
    //--------------------------------------------------------------------------
    // Inner classes
    //--------------------------------------------------------------------------
    
    /** */
    public static final strictfp class FindOne extends GISExtension.Reporter {
        
        public String getAgentClassString() {
            return "OTPL";
        }
        
        public Syntax getSyntax() {
            return Syntax.reporterSyntax(new int[] { Syntax.TYPE_WILDCARD,
                                                     Syntax.TYPE_STRING,
                                                     Syntax.TYPE_STRING },
                                         Syntax.TYPE_WILDCARD);
        }
        
        @SuppressWarnings("unchecked")
        public Object reportInternal (Argument args[], Context context)
                throws ExtensionException, LogoException {
            VectorDataset dataset = VectorDataset.getDataset(args[0]);
            String propertyName = getPropertyName(dataset, args[1]);
            StringUtils.WildcardMatcher matcher = new StringUtils.WildcardMatcher(args[2].getString());
            for (Iterator<VectorFeature> i = dataset.getFeatures().iterator(); i.hasNext();) {
                VectorFeature feature = i.next();
                Object value = feature.getProperty(propertyName);
                if ((value != null) && matcher.matches(value.toString())) {
                    return feature;
                }
            }
            return Nobody.NOBODY;
        }
    }
    
    /** */
    public static final strictfp class FindAll extends GISExtension.Reporter {

        public String getAgentClassString() {
            return "OTPL";
        }
        
        public Syntax getSyntax() {
            return Syntax.reporterSyntax(new int[] { Syntax.TYPE_WILDCARD,
                                                     Syntax.TYPE_STRING,
                                                     Syntax.TYPE_STRING },
                                         Syntax.TYPE_LIST);
        }
        
        @SuppressWarnings("unchecked")
        public Object reportInternal (Argument args[], Context context)
                throws ExtensionException, LogoException {
            VectorDataset dataset = VectorDataset.getDataset(args[0]);
            String propertyName = getPropertyName(dataset, args[1]);
            StringUtils.WildcardMatcher matcher = new StringUtils.WildcardMatcher(args[2].getString());
            LogoListBuilder result = new LogoListBuilder();
            for (Iterator<VectorFeature> i = dataset.getFeatures().iterator(); i.hasNext();) {
                VectorFeature feature = i.next();
                Object value = feature.getProperty(propertyName);
                if ((value != null) && matcher.matches(value.toString())) {
                    result.add(feature);
                }
            }
            return result.toLogoList();
        }
    }
        
    /** */
    public static final strictfp class FindLessThan extends GISExtension.Reporter {

        public String getAgentClassString() {
            return "OTPL";
        }
        
        public Syntax getSyntax() {
            return Syntax.reporterSyntax(new int[] { Syntax.TYPE_WILDCARD,
                                                     Syntax.TYPE_STRING,
                                                     Syntax.TYPE_STRING | Syntax.TYPE_NUMBER },
                                         Syntax.TYPE_LIST);
        }
        
        @SuppressWarnings("unchecked")
        public Object reportInternal (Argument args[], Context context)
                throws ExtensionException, LogoException {
            VectorDataset dataset = VectorDataset.getDataset(args[0]);
            String propertyName = getPropertyName(dataset, args[1]);
            Comparable max = (Comparable)args[2].get();
            LogoListBuilder result = new LogoListBuilder();
            for (Iterator<VectorFeature> i = dataset.getFeatures().iterator(); i.hasNext();) {
                VectorFeature feature = i.next();
                Object value = feature.getProperty(propertyName);
                if ((value != null) && (max.compareTo(value) > 0)) {
                    result.add(feature);
                }
            }
            return result.toLogoList();
        }
    }
        
    /** */
    public static final strictfp class FindGreaterThan extends GISExtension.Reporter {

        public String getAgentClassString() {
            return "OTPL";
        }
        
        public Syntax getSyntax() {
            return Syntax.reporterSyntax(new int[] { Syntax.TYPE_WILDCARD,
                                                     Syntax.TYPE_STRING,
                                                     Syntax.TYPE_STRING | Syntax.TYPE_NUMBER },
                                         Syntax.TYPE_LIST);
        }
        
        @SuppressWarnings("unchecked")
        public Object reportInternal (Argument args[], Context context)
                throws ExtensionException, LogoException {
            VectorDataset dataset = VectorDataset.getDataset(args[0]);
            String propertyName = getPropertyName(dataset, args[1]);
            Comparable min = (Comparable)args[2].get();
            LogoListBuilder result = new LogoListBuilder();
            for (Iterator<VectorFeature> i = dataset.getFeatures().iterator(); i.hasNext();) {
                VectorFeature feature = i.next();
                Object value = feature.getProperty(propertyName);
                if ((value != null) && (min.compareTo(value) < 0)) {
                    result.add(feature);
                }
            }
            return result.toLogoList();
        }
    }
        
    /** */
    public static final strictfp class FindInRange extends GISExtension.Reporter {

        public String getAgentClassString() {
            return "OTPL";
        }
        
        public Syntax getSyntax() {
            return Syntax.reporterSyntax(new int[] { Syntax.TYPE_WILDCARD,
                                                     Syntax.TYPE_STRING,
                                                     Syntax.TYPE_STRING | Syntax.TYPE_NUMBER,
                                                     Syntax.TYPE_STRING | Syntax.TYPE_NUMBER },
                                         Syntax.TYPE_LIST);
        }
        
        @SuppressWarnings("unchecked")
        public Object reportInternal (Argument args[], Context context)
                throws ExtensionException, LogoException {
            VectorDataset dataset = VectorDataset.getDataset(args[0]);
            String propertyName = getPropertyName(dataset, args[1]);
            Comparable min = (Comparable)args[2].get();
            Comparable max = (Comparable)args[3].get();
            LogoListBuilder result = new LogoListBuilder();
            for (Iterator<VectorFeature> i = dataset.getFeatures().iterator(); i.hasNext();) {
                VectorFeature feature = i.next();
                Object value = feature.getProperty(propertyName);
                if ((value != null) && (min.compareTo(value) < 0) && (max.compareTo(value) > 0)) {
                    result.add(feature);
                }
            }
            return result.toLogoList();
        }
    }
        
    /** */
    public static final strictfp class GetMinimum extends GISExtension.Reporter {

        public String getAgentClassString() {
            return "OTPL";
        }
        
        public Syntax getSyntax() {
            return Syntax.reporterSyntax(new int[] { Syntax.TYPE_WILDCARD,
                                                     Syntax.TYPE_STRING },
                                         Syntax.TYPE_NUMBER | Syntax.TYPE_STRING);
        }
        
        @SuppressWarnings("unchecked")
        public Object reportInternal (Argument args[], Context context)
                throws ExtensionException, LogoException {
            VectorDataset dataset = VectorDataset.getDataset(args[0]);
            String propertyName = getPropertyName(dataset, args[1]);
            Comparable result = null;
            for (Iterator<VectorFeature> i = dataset.getFeatures().iterator(); i.hasNext();) {
                VectorFeature feature = i.next();
                Object value = feature.getProperty(propertyName);
                if ((value != null) && ((result == null) || (result.compareTo(value) > 0))) {
                    if ((value instanceof Number) && Double.isNaN(((Number)value).doubleValue())) {
                        // careful! don't accept NaN values
                        continue;
                    }
                    result = (Comparable)value;
                }
            }
            return result;
        }
    }
        
    /** */
    public static final strictfp class GetMaximum extends GISExtension.Reporter {

        public String getAgentClassString() {
            return "OTPL";
        }
        
        public Syntax getSyntax() {
            return Syntax.reporterSyntax(new int[] { Syntax.TYPE_WILDCARD,
                                                     Syntax.TYPE_STRING },
                                         Syntax.TYPE_NUMBER | Syntax.TYPE_STRING);
        }
        
        @SuppressWarnings("unchecked")
        public Object reportInternal (Argument args[], Context context)
                throws ExtensionException, LogoException {
            VectorDataset dataset = VectorDataset.getDataset(args[0]);
            String propertyName = getPropertyName(dataset, args[1]);
            Comparable result = null;
            for (Iterator<VectorFeature> i = dataset.getFeatures().iterator(); i.hasNext();) {
                VectorFeature feature = i.next();
                Object value = feature.getProperty(propertyName);
                if ((value != null) && ((result == null) || (result.compareTo(value) < 0))) {
                    if ((value instanceof Number) && Double.isNaN(((Number)value).doubleValue())) {
                        // careful! don't accept NaN values
                        continue;
                    }
                    result = (Comparable)value;
                }
            }
            return result;
        }
    }

    //--------------------------------------------------------------------------
    // Class methods
    //--------------------------------------------------------------------------
    
    /** */
    static String getPropertyName (VectorDataset dataset, Argument arg) 
            throws ExtensionException, LogoException {
        String propertyName = arg.getString().toUpperCase();
        if (dataset.isValidPropertyName(propertyName)) {
            return propertyName;
        } else {
            throw new ExtensionException("dataset does not have property: '"+propertyName+"'");
        }
    }
}
