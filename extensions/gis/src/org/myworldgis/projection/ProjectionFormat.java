//
// Copyright (c) 2007 Eric Russell. All rights reserved.
//

package org.myworldgis.projection;

import org.locationtech.jts.geom.Coordinate;
import java.io.BufferedReader;
import java.io.IOException;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.Iterator;
import org.myworldgis.wkt.WKTElement;
import org.myworldgis.wkt.WKTFormat;
import org.ngs.ngunits.NonSI;
import org.ngs.ngunits.SI;
import org.ngs.ngunits.Unit;
import org.ngs.ngunits.UnitConverter;
import org.ngs.ngunits.quantity.Angle;
import org.ngs.ngunits.quantity.Length;


/**
 *
 */
public final class ProjectionFormat extends Format {

    //--------------------------------------------------------------------------
    // Class variables
    //--------------------------------------------------------------------------

    /** */
    static final long serialVersionUID = 1L;

    /** */
    private static final ProjectionFormat _instance = new ProjectionFormat();

    //--------------------------------------------------------------------------
    // Class methods
    //--------------------------------------------------------------------------

    /** */
    public static ProjectionFormat getInstance () {
        return _instance;
    }

    /**
     * For testing
     */
    public static void main (String[] args) {
        try {
            Projection proj = _instance.parseProjection("GEOGCS[\"GCS_WGS_1984\",DATUM[\"D_WGS_1984\",SPHEROID[\"WGS_1984\",6378137,298.257223563]],PRIMEM[\"Greenwich\",0],UNIT[\"Degree\",0.017453292519943295]]",
                                                        new ParsePosition(0));
            System.out.println(_instance.format(proj));
            System.out.println(_instance.parseObject(_instance.format(proj)));
            proj = _instance.parseProjection("PROJCS[\"World_Equidistant_Conic\",GEOGCS[\"GCS_WGS_1984\",DATUM[\"D_WGS_1984\",SPHEROID[\"WGS_1984\",6378137,298.257223563]],PRIMEM[\"Greenwich\",0],UNIT[\"Degree\",0.017453292519943295]],PROJECTION[\"Equidistant_Conic\"],PARAMETER[\"False_Easting\",0],PARAMETER[\"False_Northing\",0],PARAMETER[\"Central_Meridian\",0],PARAMETER[\"Standard_Parallel_1\",60],PARAMETER[\"Standard_Parallel_2\",60],PARAMETER[\"Latitude_Of_Origin\",0],UNIT[\"Meter\",1]]",
                    new ParsePosition(0));
            System.out.println(_instance.format(proj));
            System.out.println(_instance.parseObject(_instance.format(proj)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //--------------------------------------------------------------------------
    // Instance methods
    //--------------------------------------------------------------------------

    /** */
    public String format (Projection proj) {
        return WKTFormat.getInstance().formatWKT(toWKT(proj));
    }

    /** */
    public WKTElement toWKT (Projection proj) {
        if (proj instanceof Geographic) {
            WKTElement geogcs = new WKTElement("GEOGCS");
            geogcs.addContent(proj.toString());
            geogcs.addContent(makeDatumElement(proj.getEllipsoid()));
            geogcs.addContent(makePrimeMeridianElement(proj.getCenter().x));
            geogcs.addContent(makeAngularUnitsElement(((Geographic)proj).getUnits()));
            return geogcs;
        } else {
            ProjectionParameters params = proj.getParameters();
            WKTElement projcs = new WKTElement("PROJCS");
            projcs.addContent(proj.toString());
            projcs.addContent(new WKTElement("GEOGCS",
                    "GCS_" + proj.getEllipsoid().toString(),
                    makeDatumElement(proj.getEllipsoid()),
                    makePrimeMeridianElement(0.0),
                    makeAngularUnitsElement(params.getAngularUnits())));
            projcs.addContent(new WKTElement("PROJECTION", proj.toString()));
            for (Iterator<String> names = params.propertyNameIterator(); names.hasNext(); ) {
                String parameterName = names.next();
                projcs.addContent(new WKTElement("PARAMETER",
                        parameterName,
                        params.getParameter(parameterName)));
            }
            projcs.addContent(makeLinearUnitsElement(params.getLinearUnits()));
            return projcs;
        }
    }

    /** */
    private WKTElement makeDatumElement (Ellipsoid ellipsoid) {
        return new WKTElement("DATUM",
                "D_"+ellipsoid.toString(),
                new WKTElement("SPHEROID",
                        ellipsoid.toString(),
                        Double.valueOf(ellipsoid.radius),
                        Double.valueOf(ellipsoid.getInverseFlattening())));
    }

    /** */
    private WKTElement makePrimeMeridianElement (double longitude) {
        return new WKTElement("PRIMEM",
                ((longitude == 0.0) ? "Greenwich" : "Custom"),
                Double.valueOf(Projection.RADIANS_TO_DEGREES.convert(longitude)));
    }

    /** */
    private WKTElement makeAngularUnitsElement (Unit<Angle> units) {
        return new WKTElement("UNIT",
                units.toString(),
                Double.valueOf(units.getConverterTo(SI.RADIAN).convert(1.0)));
    }

    /** */
    private WKTElement makeLinearUnitsElement (Unit<Length> units) {
        return new WKTElement("UNIT",
                units.toString(),
                Double.valueOf(units.getConverterTo(SI.METRE).convert(1.0)));
    }

    /** */
    public Projection parseProjection (BufferedReader in) throws IOException, ParseException {
        StringBuffer wkt = new StringBuffer();
        String line = null;
        while ((line = in.readLine()) != null) {
            wkt.append(line);
        }
        return parseProjection(wkt.toString());
    }

    /** */
    public Projection parseProjection (String text) throws ParseException {
        return parseProjection(text, new ParsePosition(0));
    }

    /** */
    public Projection parseProjection (String text, ParsePosition pos)
            throws ParseException {
        return parseProjection(WKTFormat.getInstance().parseWKT(text, pos));
    }

    /** */
    public Projection parseProjection (WKTElement wkt) throws ParseException {
        if (wkt.getKeyword().equals("GEOGCS")) {
            return parseGeographic(wkt);
        } else if (wkt.getKeyword().equals("PROJCS")) {
            return parseProjected(wkt);
        } else {
            throw new ParseException("only GEOGCS and PROJCS are supported. Try using a tool like QGIS or ArcMap to convert your data to a different projection like WGS84", 0);
        }
    }

    /** */
    private Geographic parseGeographic (WKTElement element)
            throws ParseException {
        WKTElement datumElt = element.nextElement("DATUM", true);
        WKTElement spheriodElt = datumElt.nextElement("SPHEROID", true);
        Ellipsoid ellipsoid = new Ellipsoid(spheriodElt.nextString(false),
                                            spheriodElt.nextNumber(true).doubleValue(),
                                            SI.METRE,
                                            spheriodElt.nextNumber(true).doubleValue());
        WKTElement primeMElement = element.nextElement("PRIMEM", true);
        WKTElement unitsElement = element.nextElement("UNIT", true);
        Unit<Angle> units = SI.RADIAN;
        double conversion = unitsElement.nextNumber(true).doubleValue();
        if (conversion != 1.0) {
            units = units.multiply(conversion);
        }
        UnitConverter centerConverter = NonSI.DEGREE_ANGLE.getConverterTo(units);
        Coordinate center = new Coordinate(centerConverter.convert(primeMElement.nextNumber(true).doubleValue()), 0.0);
        return new Geographic(ellipsoid, center, units);
    }

    /** */
    private Projection parseProjected (WKTElement element)
            throws ParseException {
        WKTElement gcsElement = element.nextElement("GEOGCS", true);
        WKTElement angularUnitsElement = gcsElement.nextElement("UNIT", true);
        Unit<Angle> angularUnits = SI.RADIAN;
        double angularConversion = angularUnitsElement.nextNumber(true).doubleValue();
        if (angularConversion != 1.0) {
            angularUnits = angularUnits.multiply(angularConversion);
        }
        WKTElement datumElt = gcsElement.nextElement("DATUM", true);
        WKTElement spheriodElt = datumElt.nextElement("SPHEROID", true);
        Ellipsoid ellipsoid = new Ellipsoid(spheriodElt.nextString(false),
                                            spheriodElt.nextNumber(true).doubleValue(),
                                            SI.METRE,
                                            spheriodElt.nextNumber(true).doubleValue());
        WKTElement projectedUnitsElement = element.nextElement("UNIT", true);
        Unit<Length> linearUnits = SI.METRE;
        double linearConversion = projectedUnitsElement.nextNumber(true).doubleValue();
        if (linearConversion != 1.0) {
            linearUnits = linearUnits.multiply(linearConversion);
        }
        ProjectionParameters parameters = new ProjectionParameters(angularUnits, linearUnits);
        WKTElement paramElement = null;
        while ((paramElement = element.nextElement("PARAMETER", false)) != null) {
            parameters.addParameter(paramElement.nextString(true),
                                    paramElement.nextNumber(true));
        }
        WKTElement projectionElt = element.nextElement("PROJECTION", true);
        String projectionName = projectionElt.nextString(true);
        if (projectionName.equals(AlbersEqualAreaConic.WKT_NAME)) {
            return new AlbersEqualAreaConic(ellipsoid, parameters);
        } else if (projectionName.equals(AzimuthalEqualArea.WKT_NAME)) {
            return new AzimuthalEqualArea(ellipsoid, parameters);
        } else if (projectionName.equals(AzimuthalEquidistant.WKT_NAME)) {
            return new AzimuthalEquidistant(ellipsoid, parameters);
        } else if (projectionName.equals(CylindricalEqualArea.WKT_NAME)) {
            return new CylindricalEqualArea(ellipsoid, parameters);
        } else if (projectionName.equals(EquidistantConic.WKT_NAME)) {
            return new EquidistantConic(ellipsoid, parameters);
        } else if (projectionName.equals(Gnomonic.WKT_NAME)) {
            return new Gnomonic(ellipsoid, parameters);
        } else if (projectionName.equals(LambertConformalConic.WKT_NAME)) {
            return new LambertConformalConic(ellipsoid, parameters);
        } else if (projectionName.equals(Mercator.WKT_NAME)) {
            return new Mercator(ellipsoid, parameters);
        } else if (projectionName.equals(Miller.WKT_NAME)) {
            return new Miller(ellipsoid, parameters);
        } else if (projectionName.equals(ObliqueMercator.WKT_NAME) ||
                   projectionName.equals(ObliqueMercator.ALTERNATE_WKT_NAME)) {
            return new ObliqueMercator(ellipsoid, parameters);
        } else if (projectionName.equals(Orthographic.WKT_NAME)) {
            return new Orthographic(ellipsoid, parameters);
        } else if (projectionName.equals(Polyconic.WKT_NAME)) {
            return new Polyconic(ellipsoid, parameters);
        } else if (projectionName.equals(Robinson.WKT_NAME)) {
            return new Robinson(ellipsoid, parameters);
        } else if (projectionName.equals(Stereographic.WKT_NAME)) {
            return new Stereographic(ellipsoid, parameters);
        } else if (projectionName.equals(TransverseMercator.WKT_NAME)) {
            return new TransverseMercator(ellipsoid, parameters);
        } else {
            throw new ParseException("unsupported projection '"+projectionName+"'", 0);
        }
    }

    //--------------------------------------------------------------------------
    // Format implementation
    //--------------------------------------------------------------------------

    /** */
    public StringBuffer format (Object obj, StringBuffer buf, FieldPosition pos) {
        buf.append(format((Projection)obj));
        return buf;
    }

    /** */
    public Object parseObject (String str, ParsePosition pos) {
        try {
            return parseProjection(str, pos);
        } catch (ParseException e) {
            pos.setIndex(0);
            pos.setErrorIndex(e.getErrorOffset());
            return null;
        }
    }
}
