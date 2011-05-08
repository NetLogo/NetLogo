//
// Copyright (c) 2000 the National Geographic Society. All rights reserved.
//

package org.myworldgis.projection;

import javax.measure.quantity.Length;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;
import org.myworldgis.util.ExtendedUnits;


/**
 * A class representing a reference ellipsoid for projection. 
 */
public final strictfp class Ellipsoid {
    
    //--------------------------------------------------------------------------
    // Class variables
    //--------------------------------------------------------------------------
    
    /** */
    public static final String NAME_PROPERTY = "name";
    
    /** */
    public static final String ORDINAL_PROPERTY = "ellipsoidOrdinal";
    
    /** */
    public static final String RADIUS_PROPERTY = "semimajorAxisLength";
    
    /** */
    public static final String ECCSQ_PROPERTY = "eccentricitySquared";
    
    /** */
    public final static Ellipsoid AIRY_1830 = new Ellipsoid(true, "Airy 1830", 6377563.396, SI.METRE, 0.00667054); 
    
    /** */
    public final static Ellipsoid AIRY_1849 = new Ellipsoid(true, "Airy Modified 1849", 6377340.189, SI.METRE, 0.00667054); 
    
    /** */
    public final static Ellipsoid AUSTRALIAN_NATIONAL = new Ellipsoid(true, "Australian National", 6378160.0, SI.METRE, 0.006694542); 
    
    /** */
    public final static Ellipsoid BESSEL_1841 = new Ellipsoid(true, "Bessel 1841", 6377397.155, SI.METRE, 0.006674372); 
    
    /** */
    public final static Ellipsoid BESSEL_MODIFIED = new Ellipsoid(true, "Bessel Modified", 6377492.018, SI.METRE, 0.006674372); 
    
    /** */
    public final static Ellipsoid BESSEL_NAMIBIA = new Ellipsoid(true, "Bessel Namibia", 6377483.865, SI.METRE, 0.006674372); 
    
    /** */
    public final static Ellipsoid CLARKE_1858 = new Ellipsoid(true, "Clarke 1858", 20926348.0, ExtendedUnits.FOOT_CLARKE, 0.006785146);
    
    /** */
    public final static Ellipsoid CLARKE_1866 = new Ellipsoid(true, "Clarke 1866", 6378206.4, SI.METRE, 0.006768658); 
    
    /** */
    public final static Ellipsoid CLARKE_1866_MICHIGAN = new Ellipsoid(true, "Clarke 1866 Michigan", 20926631.53, ExtendedUnits.FOOT_MODIFIED_AMERICAN, 0.006768658); 
    
    /** */
    public final static Ellipsoid CLARKE_1880 = new Ellipsoid(true, "Clarke 1880", 6378249.0, SI.METRE, 0.006803511); 
    
    /** */
    public final static Ellipsoid CLARKE_1880_ARC = new Ellipsoid(true, "Clarke 1880 (Arc)", 6378249.145, SI.METRE, 0.006803481); 
    
    /** */
    public final static Ellipsoid CLARKE_1880_BENOIT = new Ellipsoid(true, "Clarke 1880 (Benoit)", 6378300.79, SI.METRE, 0.006803483); 
    
    /** */
    public final static Ellipsoid CLARKE_1880_IGN = new Ellipsoid(true, "Clarke 1880 (IGN)", 6378249.2, SI.METRE, 0.006803488); 
    
    /** */
    public final static Ellipsoid CLARKE_1880_RGS = new Ellipsoid(true, "Clarke 1880 (RGS)", 6378249.145, SI.METRE, 0.006803511); 
    
    /** */
    public final static Ellipsoid CLARKE_1880_SGA_1922 = new Ellipsoid(true, "Clarke 1880 (SGA 1922)", 6378249.2, SI.METRE, 0.006803489); 
    
    /** */
    public final static Ellipsoid EVEREST_1937 = new Ellipsoid(true, "Everest 1830 (1937 Adjustment)", 6377276.345, SI.METRE, 0.006637847);
    
    /** */
    public final static Ellipsoid EVEREST_1967 = new Ellipsoid(true, "Everest 1830 (1967 Definition)", 6377298.556, SI.METRE, 0.006637847);
    
    /** */
    public final static Ellipsoid EVEREST_1975 = new Ellipsoid(true, "Everest 1830 (1975 Definition))", 6377301.243, SI.METRE, 0.006637846);
    
    /** */
    public final static Ellipsoid EVEREST_MODIFIED = new Ellipsoid(true, "Everest 1830 Modified", 6377304.063, SI.METRE, 0.006637847);
    
    /** */
    public final static Ellipsoid FISHER_1960_MERCURY = new Ellipsoid(true, "Fisher 1960 Mercury", 6378166.0, SI.METRE, 0.006693422);
    
    /** */
    public final static Ellipsoid FISCHER_1960_MODIFIED = new Ellipsoid(true, "Fischer 1960 Modified", 6378155.0, SI.METRE, 0.006693422); 
    
    /** */
    public final static Ellipsoid FISHER_1968 = new Ellipsoid(true, "Fisher 1968", 6378150.0, SI.METRE, 0.006693422);
    
    /** */
    public final static Ellipsoid GEM_10C = new Ellipsoid(true, "GEM 10C", 6378137.0, SI.METRE, 0.00669438);
    
    /** */
    public final static Ellipsoid GRS_1967 = new Ellipsoid(true, "GRS 1967", 6378160.0, SI.METRE, 0.006694605);
    
    /** */
    public final static Ellipsoid GRS_1980 = new Ellipsoid(true, "GRS 1980", 6378137.0, SI.METRE, 0.00669438);
    
    /** */
    public final static Ellipsoid HELMERT_1906 = new Ellipsoid(true, "Helmert 1906", 6378200.0, SI.METRE, 0.006693422);
    
    /** */
    public final static Ellipsoid HOUGH = new Ellipsoid(true, "Hough", 6378270.0, SI.METRE, 0.00672267);
    
    /** */
    public final static Ellipsoid INDONESIAN_NATIONAL = new Ellipsoid(true, "Indonesian National", 6378160.0, SI.METRE, 0.006694609);
    
    /** */
    public final static Ellipsoid INTERNATIONAL_1924 = new Ellipsoid(true, "International 1924", 6378388.0, SI.METRE, 0.00672267); 
    
    /** */
    public final static Ellipsoid INTERNATIONAL_1967 = new Ellipsoid(true, "International 1967", 6378160.0, SI.METRE, 0.006694542); 
    
    /** */
    public final static Ellipsoid KRASSOVSKY = new Ellipsoid(true, "Krassovsky 1940", 6378245.0d, SI.METRE, 0.006693422); 
    
    /** */
    public static final Ellipsoid NAD_1927 = new Ellipsoid(true, "NAD 1927", CLARKE_1866.radius, SI.METRE, CLARKE_1866.eccsq);
    
    /** */
    public static final Ellipsoid NAD_1983 = new Ellipsoid(true, "NAD 1983", GRS_1980.radius, SI.METRE, GRS_1980.eccsq);
    
    /** */
    public static final Ellipsoid NWL_10D = new Ellipsoid(true, "NWL 10D", 6378135.0, SI.METRE, 0.006694318);
    
    /** */
    public static final Ellipsoid NWL_9D = new Ellipsoid(true, "NWL 9D", 6378145.0, SI.METRE, 0.006694542);
    
    /** */
    public static final Ellipsoid OSU86F = new Ellipsoid(true, "OSU86F", 6378136.2, SI.METRE, 0.00669438);
    
    /** */
    public static final Ellipsoid OSU91A = new Ellipsoid(true, "OSU91A", 6378136.3, SI.METRE, 0.00669438);
    
    /** */
    public static final Ellipsoid PLESSIS_1817 = new Ellipsoid(true, "Plessis 1817", 6376523.0, SI.METRE, 0.006469544);
    
    /** */
    public final static Ellipsoid SOUTH_AMERICAN_1969 = new Ellipsoid(true, "South American 1969", 6378160.0, SI.METRE, 0.006694542);
    
    /** */
    public static final Ellipsoid STRUVE_1860 = new Ellipsoid(true, "Struve 1860", 6378297.0, SI.METRE, 0.00677436);
    
    /** */
    public static final Ellipsoid WAR_OFFICE = new Ellipsoid(true, "War Office", 6378300.583, SI.METRE, 0.006745343);
    
    /** */
    public final static Ellipsoid WGS_60 = new Ellipsoid(true, "WSG 60", 6378165.0, SI.METRE, 0.006693422); 
    
    /** */
    public final static Ellipsoid WGS_66 = new Ellipsoid(true, "WGS 66", 6378145.0, SI.METRE, 0.006694542); 
    
    /** */
    public final static Ellipsoid WGS_72 = new Ellipsoid(true, "WGS 72", 6378135.0, SI.METRE, 0.006694318); 
    
    /** */
    public final static Ellipsoid WGS_84 = new Ellipsoid(true, "WGS 84", 6378137.0, SI.METRE, 0.00669438); 
    
    /** */
    public final static Ellipsoid SPHERE = new Ellipsoid(true, "Sphere", 6370997.0, SI.METRE, 0.0); 
    
    /** ALL Ellipsoids */
    private static final Ellipsoid[] ELLIPSOIDS = { AIRY_1830,
                                                    AIRY_1849,
                                                    AUSTRALIAN_NATIONAL,
                                                    BESSEL_1841,
                                                    BESSEL_MODIFIED,
                                                    BESSEL_NAMIBIA,
                                                    CLARKE_1858,
                                                    CLARKE_1866,
                                                    CLARKE_1866_MICHIGAN,
                                                    CLARKE_1880,
                                                    CLARKE_1880_ARC,
                                                    CLARKE_1880_BENOIT,
                                                    CLARKE_1880_IGN,
                                                    CLARKE_1880_RGS,
                                                    CLARKE_1880_SGA_1922,
                                                    EVEREST_1937,
                                                    EVEREST_1967,
                                                    EVEREST_1975,
                                                    EVEREST_MODIFIED,
                                                    FISHER_1960_MERCURY,
                                                    FISCHER_1960_MODIFIED,
                                                    FISHER_1968,
                                                    GEM_10C,
                                                    GRS_1967,
                                                    GRS_1980,
                                                    HELMERT_1906,
                                                    HOUGH,
                                                    INDONESIAN_NATIONAL,
                                                    INTERNATIONAL_1924,
                                                    INTERNATIONAL_1967,
                                                    KRASSOVSKY,
                                                    NAD_1927,
                                                    NAD_1983,
                                                    NWL_10D,
                                                    NWL_9D,
                                                    OSU86F,
                                                    OSU91A,
                                                    PLESSIS_1817,
                                                    SOUTH_AMERICAN_1969,
                                                    STRUVE_1860,
                                                    WAR_OFFICE,
                                                    WGS_60,
                                                    WGS_66,
                                                    WGS_72,
                                                    WGS_84,
                                                    SPHERE };
    
    //--------------------------------------------------------------------------
    // Class Methods
    //--------------------------------------------------------------------------
    
    /** */
    public static Ellipsoid[] getAllEllipsoids () {
        Ellipsoid[] result = new Ellipsoid[ELLIPSOIDS.length];
        System.arraycopy(ELLIPSOIDS, 0, result, 0, ELLIPSOIDS.length);
        return(result);
    }
    
    //--------------------------------------------------------------------------
    // Instance variables
    //--------------------------------------------------------------------------
    
    /** The display name for this ellipsoid. */
    private final String _name;
    
    /** The equatorial radius for this ellipsoid, in METERS. */
    public final double radius;

    /** The square of this ellipsoid's eccentricity. */
    public final double eccsq;
    
    //--------------------------------------------------------------------------
    // Constructors
    //--------------------------------------------------------------------------
    
    /**
     * Constructs a new Ellipsoid instance.
     * @param name The name of this ellipsoid.
     * @param radius The length of the semi-major axis for this ellipsoid, in METERS.
     * @param inverseFlattening The inverse of the flattening of this ellipsoid
     */
    public Ellipsoid (String name, double radius, Unit<Length> radiusUnits, double inverseFlattening) {
        this(false, 
             name, 
             radius, 
             radiusUnits, 
             ((2.0 / inverseFlattening) - (1.0 / (inverseFlattening * inverseFlattening))));
    }
    
    /**
     * Constructs a new Ellipsoid instance.
     * @param isPredefined True if this is a predefined ellipsoid, false if it is
     *     a user-defined ellipsoid.
     * @param name The name of this ellipsoid.
     * @param radius The length of the semi-major axis for this ellipsoid, in METERS.
     * @param eccsq The square of the eccentricity for this ellipsoid.
     * @param geoTIFFCode The code for this ellipsoid defined by the GeoTIFF standard
     */
    private Ellipsoid (boolean isPredefined, 
                       String name, 
                       double radius,
                       Unit<Length> radiusUnits,
                       double eccsq) {
        if (name == null) {
            _name = "";
        } else {
            _name = name;
        }
        this.radius = radiusUnits.getConverterTo(SI.METRE).convert(radius);
        this.eccsq = eccsq;
    }
    
    //--------------------------------------------------------------------------
    // Instance methods
    //--------------------------------------------------------------------------
    
    /** */
    public String toString () {
        return _name;
    }
    
    /** */
    public double getInverseFlattening () {
        return 1.0 / (1.0 - StrictMath.sqrt(1.0 - eccsq));
    }
    
    /** */
    public boolean equals (Object obj) {
        return (obj instanceof Ellipsoid) &&
               (((Ellipsoid)obj).radius == this.radius) &&
               (((Ellipsoid)obj).eccsq == this.eccsq);
    }
}
