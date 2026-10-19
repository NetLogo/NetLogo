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
import org.nlogo.core.Nobody$;
import org.nlogo.core.Syntax;
import org.nlogo.core.SyntaxJ;


/**
 *
 */
public abstract class VectorDatasetSearch {

    //--------------------------------------------------------------------------
    // Inner classes
    //--------------------------------------------------------------------------

    /** */
    public static final class FindOne extends GISExtension.Reporter {

        public String getAgentClassString() {
            return "OTPL";
        }

        public Syntax getSyntax() {
            return SyntaxJ.reporterSyntax(new int[] { Syntax.WildcardType(),
                                                     Syntax.StringType(),
                                                     Syntax.StringType() | Syntax.NumberType()},
                                         Syntax.WildcardType());
        }

        @SuppressWarnings("unchecked")
        public Object reportInternal (Argument args[], Context context)
                throws ExtensionException, LogoException {
            VectorDataset dataset = VectorDataset.getDataset(args[0]);
            String propertyName = getPropertyName(dataset, args[1]);
            if (args[2].get() instanceof String) {
                StringUtils.WildcardMatcher matcher = new StringUtils.WildcardMatcher(args[2].getString());
                for (Iterator<VectorFeature> i = dataset.getFeatures().iterator(); i.hasNext(); ) {
                    VectorFeature feature = i.next();
                    Object value = feature.getProperty(propertyName);
                    if ((value != null) && matcher.matches(value.toString())) {
                        return feature;
                    }
                }
            } else {
                Double number = args[2].getDouble();
                for (Iterator<VectorFeature> i = dataset.getFeatures().iterator(); i.hasNext();) {
                    VectorFeature feature = i.next();
                    Object value = feature.getProperty(propertyName);
                    if ((value != null) && value.equals(number)) {
                        return feature;
                    }
                }
            }
            return Nobody$.MODULE$;
        }
    }

    /** */
    public static final class FindAll extends GISExtension.Reporter {

        public String getAgentClassString() {
            return "OTPL";
        }

        public Syntax getSyntax() {
            return SyntaxJ.reporterSyntax(new int[] { Syntax.WildcardType(),
                                                     Syntax.StringType(),
                                                     Syntax.StringType() | Syntax.NumberType() },
                                         Syntax.ListType());
        }

        @SuppressWarnings("unchecked")
        public Object reportInternal (Argument args[], Context context)
                throws ExtensionException, LogoException {
            VectorDataset dataset = VectorDataset.getDataset(args[0]);
            String propertyName = getPropertyName(dataset, args[1]);
            LogoListBuilder result = new LogoListBuilder();
            if (args[2].get() instanceof String) {
                StringUtils.WildcardMatcher matcher = new StringUtils.WildcardMatcher(args[2].getString());
                for (Iterator<VectorFeature> i = dataset.getFeatures().iterator(); i.hasNext(); ) {
                    VectorFeature feature = i.next();
                    Object value = feature.getProperty(propertyName);
                    if ((value != null) && matcher.matches(value.toString())) {
                        result.add(feature);
                    }
                }
            } else {
                Double number = args[2].getDouble();
                for (Iterator<VectorFeature> i = dataset.getFeatures().iterator(); i.hasNext(); ) {
                    VectorFeature feature = i.next();
                    Object value = feature.getProperty(propertyName);
                    if ((value != null) && value.equals(number)) {
                        result.add(feature);
                    }
                }
            }
            return result.toLogoList();
        }
    }

    /** */
    public static final class FindLessThan extends GISExtension.Reporter {

        public String getAgentClassString() {
            return "OTPL";
        }

        public Syntax getSyntax() {
            return SyntaxJ.reporterSyntax(new int[] { Syntax.WildcardType(),
                                                     Syntax.StringType(),
                                                     Syntax.StringType() | Syntax.NumberType() },
                                         Syntax.ListType());
        }

        @SuppressWarnings("unchecked")
        public Object reportInternal (Argument args[], Context context)
                throws ExtensionException, LogoException {
            VectorDataset dataset = VectorDataset.getDataset(args[0]);
            String propertyName = getPropertyName(dataset, args[1]);
            Comparable<Object> max = (Comparable<Object>)args[2].get();
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
    public static final class FindGreaterThan extends GISExtension.Reporter {

        public String getAgentClassString() {
            return "OTPL";
        }

        public Syntax getSyntax() {
            return SyntaxJ.reporterSyntax(new int[] { Syntax.WildcardType(),
                                                     Syntax.StringType(),
                                                     Syntax.StringType() | Syntax.NumberType() },
                                         Syntax.ListType());
        }

        @SuppressWarnings("unchecked")
        public Object reportInternal (Argument args[], Context context)
                throws ExtensionException, LogoException {
            VectorDataset dataset = VectorDataset.getDataset(args[0]);
            String propertyName = getPropertyName(dataset, args[1]);
            Comparable<Object> min = (Comparable<Object>)args[2].get();
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
    public static final class FindInRange extends GISExtension.Reporter {

        public String getAgentClassString() {
            return "OTPL";
        }

        public Syntax getSyntax() {
            return SyntaxJ.reporterSyntax(new int[] { Syntax.WildcardType(),
                                                     Syntax.StringType(),
                                                     Syntax.StringType() | Syntax.NumberType(),
                                                     Syntax.StringType() | Syntax.NumberType() },
                                         Syntax.ListType());
        }

        @SuppressWarnings("unchecked")
        public Object reportInternal (Argument args[], Context context)
                throws ExtensionException, LogoException {
            VectorDataset dataset = VectorDataset.getDataset(args[0]);
            String propertyName = getPropertyName(dataset, args[1]);
            Comparable<Object> min = (Comparable<Object>)args[2].get();
            Comparable<Object> max = (Comparable<Object>)args[3].get();
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
    public static final class GetMinimum extends GISExtension.Reporter {

        public String getAgentClassString() {
            return "OTPL";
        }

        public Syntax getSyntax() {
            return SyntaxJ.reporterSyntax(new int[] { Syntax.WildcardType(),
                                                     Syntax.StringType() },
                                         Syntax.NumberType() | Syntax.StringType());
        }

        @SuppressWarnings("unchecked")
        public Object reportInternal (Argument args[], Context context)
                throws ExtensionException, LogoException {
            VectorDataset dataset = VectorDataset.getDataset(args[0]);
            String propertyName = getPropertyName(dataset, args[1]);
            Comparable<Object> result = null;
            for (Iterator<VectorFeature> i = dataset.getFeatures().iterator(); i.hasNext();) {
                VectorFeature feature = i.next();
                Object value = feature.getProperty(propertyName);
                if ((value != null) && ((result == null) || (result.compareTo(value) > 0))) {
                    if ((value instanceof Number) && Double.isNaN(((Number)value).doubleValue())) {
                        // careful! don't accept NaN values
                        continue;
                    }
                    result = (Comparable<Object>)value;
                }
            }
            return result;
        }
    }

    /** */
    public static final class GetMaximum extends GISExtension.Reporter {

        public String getAgentClassString() {
            return "OTPL";
        }

        public Syntax getSyntax() {
            return SyntaxJ.reporterSyntax(new int[] { Syntax.WildcardType(),
                                                     Syntax.StringType() },
                                         Syntax.NumberType() | Syntax.StringType());
        }

        @SuppressWarnings("unchecked")
        public Object reportInternal (Argument args[], Context context)
                throws ExtensionException, LogoException {
            VectorDataset dataset = VectorDataset.getDataset(args[0]);
            String propertyName = getPropertyName(dataset, args[1]);
            Comparable<Object> result = null;
            for (Iterator<VectorFeature> i = dataset.getFeatures().iterator(); i.hasNext();) {
                VectorFeature feature = i.next();
                Object value = feature.getProperty(propertyName);
                if ((value != null) && ((result == null) || (result.compareTo(value) < 0))) {
                    if ((value instanceof Number) && Double.isNaN(((Number)value).doubleValue())) {
                        // careful! don't accept NaN values
                        continue;
                    }
                    result = (Comparable<Object>)value;
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
