//
// Copyright (c) 2007 Eric Russell. All rights reserved.
//

package org.myworldgis.projection;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.measure.converter.UnitConverter;
import javax.measure.quantity.Angle;
import javax.measure.quantity.Length;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;


/** */
public final strictfp class ProjectionParameters {
    
    //-------------------------------------------------------------------------
    // Instance variables
    //-------------------------------------------------------------------------
    
    /** */
    private final Map<String,Number> _parameters;
    
    /** */
    private final Unit<Angle> _angularUnits;
    
    /** */
    private final UnitConverter _toRadians;
    
    /** */
    private final Unit<Length> _linearUnits;
    
    /** */
    private final UnitConverter _toMeters;
    
    //-------------------------------------------------------------------------
    // Constructors
    //-------------------------------------------------------------------------
    
    /** */
    public ProjectionParameters (Unit<Angle> angularUnits, 
                                 Unit<Length> linearUnits) {
        _parameters = new HashMap<String,Number>();
        _angularUnits = angularUnits;
        _toRadians = _angularUnits.getConverterTo(SI.RADIAN);
        _linearUnits = linearUnits;
        _toMeters = _linearUnits.getConverterTo(SI.METRE);
    }

    //-------------------------------------------------------------------------
    // Instance methods
    //-------------------------------------------------------------------------
    
    /** */
    public Unit<Angle> getAngularUnits () {
        return _angularUnits;
    }

    /** */
    public Unit<Length> getLinearUnits () {
        return _linearUnits;
    }
    
    /** */
    public Iterator<String> propertyNameIterator () {
        return _parameters.keySet().iterator();
    }
    
    /** */
    public void addParameter (String name, Number value) {
        _parameters.put(name.toLowerCase(), value);
    }
    
    /** */
    public Number getParameter (String name) {
        return _parameters.get(name.toLowerCase());
    }
    
    /** */
    public void addAngularParameter (String name, double angle, Unit<Angle> units) {
        UnitConverter converter = units.getConverterTo(_angularUnits);
        _parameters.put(name.toLowerCase(), Double.valueOf(converter.convert(angle)));
    }
    
    /** */
    public void addLinearParameter (String name, double length, Unit<Length> units) {
        UnitConverter converter = units.getConverterTo(_linearUnits);
        _parameters.put(name.toLowerCase(), Double.valueOf(converter.convert(length)));
    }
    
    /** */
    public void addDimensionlessParameter (String name, double k) {
        _parameters.put(name.toLowerCase(), Double.valueOf(k));
    }
    
    /** Returns the parameter value in RADIANS */
    public double getAngularParameter (String name) throws ParseException {
        Number value = _parameters.get(name.toLowerCase());
        if (value != null) {
            return _toRadians.convert(value.doubleValue());
        } else {
            throw new ParseException("missing required parameter '"+name+"'", 0);
        }
    }
    
    /** Returns the parameter value in RADIANS */
    public double getCenterLongitude () throws ParseException {
        Number value = _parameters.get("longitude_of_center");
        if (value != null) {
            return _toRadians.convert(value.doubleValue());
        } 
        value = _parameters.get("central_meridian");
        if (value != null) {
            return _toRadians.convert(value.doubleValue());
        }
        throw new ParseException("unable to find parameter for center longitude", 0);
    }
    
    /** Returns the parameter value in RADIANS */
    public double getCenterLatitude () throws ParseException {
        Number value = _parameters.get("latitude_of_center");
        if (value != null) {
            return _toRadians.convert(value.doubleValue());
        } 
        value = _parameters.get("latitude_of_origin");
        if (value != null) {
            return _toRadians.convert(value.doubleValue());
        }
        value = _parameters.get("standard_parallel_1");
        if (value != null) {
            return _toRadians.convert(value.doubleValue());
        }
        throw new ParseException("unable to find parameter for center latitude", 0);
    }

    /** Returns the parameter value in METERS */
    public double getLinearParameter (String name) throws ParseException {
        Number value = _parameters.get(name.toLowerCase());
        if (value != null) {
            return _toMeters.convert(value.doubleValue());
        } else {
            throw new ParseException("missing required parameter '"+name+"'", 0);
        }
    }
    
    /** */
    public double getDimensionlessParameter (String name) throws ParseException {
        Number value = _parameters.get(name.toLowerCase());
        if (value != null) {
            return value.doubleValue();
        } else {
            throw new ParseException("missing required parameter '"+name+"'", 0);
        }
    }
}
