//
// Copyright (c) 2007 the National Geographic Society. All rights reserved.
//

package org.myworldgis.util;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.measure.quantity.Area;
import javax.measure.quantity.Duration;
import javax.measure.quantity.Length;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;
import javax.measure.unit.UnitFormat;


/** 
 * 
 */
public final strictfp class ExtendedUnits {

    /**
     * Holds collection of extended units.
     */
    private static HashSet<Unit<?>> INSTANCES = new HashSet<Unit<?>>();
    
    /**
     * Default constructor (prevents this class from being instantiated).
     */
    private ExtendedUnits () { }

    ////////////
    // Length //
    ////////////

    /** */
    public static final Unit<Length> FOOT_MODIFIED_AMERICAN = ext(SI.METRE.times(12.0004584).divide(39.37));
    
    /** */
    public static final Unit<Length> FOOT_CLARKE = ext(SI.METRE.times(12.0).divide(39.370432));
    
    /** */
    public static final Unit<Length> FOOT_INDIAN = ext(SI.METRE.times(12.0).divide(39.370141));
    
    /** */
    public static final Unit<Length> LINK = ext(SI.METRE.times(7.92).divide(39.370432));
    
    /** */
    public static final Unit<Length> LINK_BENOIT = ext(SI.METRE.times(7.92).divide(39.370113));
    
    /** */
    public static final Unit<Length> LINK_SEARS = ext(SI.METRE.times(7.92).divide(39.370147));
    
    /** */
    public static final Unit<Length> CHAIN_BENOIT = ext(LINK_BENOIT.times(100));
    
    /** */
    public static final Unit<Length> CHAIN_SEARS = ext(LINK_SEARS.times(100));
    
    /** */
    public static final Unit<Length> YARD = ext(NonSI.FOOT.times(3));
    
    /** */
    public static final Unit<Length> YARD_SEARS = ext(SI.METRE.times(36).divide(39.370147));
    
    /** */
    public static final Unit<Length> YARD_INDIAN = ext(FOOT_INDIAN.times(3));
    
    /** */
    public static final Unit<Length> FATHOM = ext(YARD.times(2));
    
    //////////
    // Area //
    //////////
    
    /** */
    @SuppressWarnings("unchecked")
    public static final Unit<Area> SQUARE_KILOMETER = (Unit<Area>)ext(SI.KILO(SI.METRE).pow(2));
    
    /** */
    @SuppressWarnings("unchecked")
    public static final Unit<Area> SQUARE_FOOT = (Unit<Area>)ext(NonSI.FOOT.pow(2));
    
    /** */
    public static final Unit<Area> ACRE = ext(SQUARE_FOOT.times(43560));
    
    //////////
    // Time //
    //////////
    
    /** */
    public static final Unit<Duration> MILLISECOND = ext(SI.SECOND.divide(1000));
    
    /////////////////////
    // Collection View //
    /////////////////////
    
    /** */
    public static Set<Unit<?>> getUnits() {
        return Collections.unmodifiableSet(INSTANCES);
    }

    /** */
    @SuppressWarnings("unchecked")
    private static <U extends Unit> U ext (U unit) {
        INSTANCES.add(unit);
        return unit;
    }
    
    
    ////////////
    // Labels //
    ////////////
    
    static {
        // labels for extended units
        UnitFormat.getInstance().label(FOOT_MODIFIED_AMERICAN, "foot_modified_american");
        UnitFormat.getInstance().label(FOOT_CLARKE, "foot_Clarke");
        UnitFormat.getInstance().label(FOOT_INDIAN, "foot_Indian");
        UnitFormat.getInstance().label(LINK, "ln");
        UnitFormat.getInstance().label(LINK_BENOIT, "link_Benoit");
        UnitFormat.getInstance().label(LINK_SEARS, "link_Sears");
        UnitFormat.getInstance().label(CHAIN_BENOIT, "chain_Benoit");
        UnitFormat.getInstance().label(CHAIN_SEARS, "chain_Sears");
        UnitFormat.getInstance().label(YARD, "yd");
        UnitFormat.getInstance().label(YARD_SEARS, "yard_Sears");
        UnitFormat.getInstance().label(YARD_INDIAN, "yard_Indian");
        UnitFormat.getInstance().label(FATHOM, "f");
        UnitFormat.getInstance().label(ACRE, "acre");
        UnitFormat.getInstance().label(MILLISECOND, "msec");
        
        // new labels for built-in units
        UnitFormat.getInstance().label(NonSI.DEGREE_ANGLE, "deg");
        UnitFormat.getInstance().label(NonSI.PERCENT, "Percent");
    }
}
