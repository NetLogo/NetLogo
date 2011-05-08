//
// Copyright (c) 2007 Eric Russell. All rights reserved.
//

package org.myworldgis.netlogo;

import com.vividsolutions.jts.geom.Envelope;
import java.text.ParseException;
import org.nlogo.api.Dump;
import org.nlogo.api.LogoList;
import org.nlogo.api.LogoListBuilder;


/**
 * 
 */
public final strictfp class EnvelopeLogoListFormat {

    //--------------------------------------------------------------------------
    // Class variables
    //--------------------------------------------------------------------------
    
    /** */
    private static final EnvelopeLogoListFormat _instance = new EnvelopeLogoListFormat();
    
    //--------------------------------------------------------------------------
    // Class methods
    //--------------------------------------------------------------------------
    
    /** */
    public static EnvelopeLogoListFormat getInstance () {
        return _instance;
    }

    //--------------------------------------------------------------------------
    // Constructors
    //--------------------------------------------------------------------------
    
    /** */
    private EnvelopeLogoListFormat () { }
    
    //--------------------------------------------------------------------------
    // Instance methods
    //--------------------------------------------------------------------------
    
    /** */
    public Envelope parse (LogoList list) throws ParseException {
        if (list.size() != 4) {
            throw new ParseException("expected a four-element list: " + Dump.logoObject(list), 0);
        }
        Object minX = list.get(0);
        if (!(minX instanceof Number)) {
            throw new ParseException("not a number: " + minX, 0);
        }
        Object maxX = list.get(1);
        if (!(maxX instanceof Number)) {
            throw new ParseException("not a number: " + maxX, 1);
        }
        Object minY = list.get(2);
        if (!(minY instanceof Number)) {
            throw new ParseException("not a number: " + minY, 2);
        }
        Object maxY = list.get(3);
        if (!(maxY instanceof Number)) {
            throw new ParseException("not a number: " + maxY, 3);
        }
        return new Envelope(((Number)minX).doubleValue(),
                            ((Number)maxX).doubleValue(),
                            ((Number)minY).doubleValue(),
                            ((Number)maxY).doubleValue());
        
    }
    
    /** */
    @SuppressWarnings("unchecked")
    public LogoList format (Envelope envelope) {
        LogoListBuilder result = new LogoListBuilder();
        result.add(Double.valueOf(envelope.getMinX()));
        result.add(Double.valueOf(envelope.getMaxX()));
        result.add(Double.valueOf(envelope.getMinY()));
        result.add(Double.valueOf(envelope.getMaxY()));
        return result.toLogoList();
    }
}
