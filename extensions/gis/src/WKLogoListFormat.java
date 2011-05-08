//
// Copyright (c) 2007 Eric Russell. All rights reserved.
//

package org.myworldgis.netlogo;

import java.text.ParseException;
import java.util.Iterator;
import org.myworldgis.projection.Projection;
import org.myworldgis.projection.ProjectionFormat;
import org.myworldgis.wkt.WKTElement;
import org.nlogo.api.LogoList;
import org.nlogo.api.LogoListBuilder;


/**
 * 
 */
public final strictfp class WKLogoListFormat {

    //--------------------------------------------------------------------------
    // Class variables
    //--------------------------------------------------------------------------
    
    /** */
    private static final WKLogoListFormat _instance = new WKLogoListFormat();
    
    //--------------------------------------------------------------------------
    // Class methods
    //--------------------------------------------------------------------------
    
    /** */
    public static WKLogoListFormat getInstance () {
        return _instance;
    }

    //--------------------------------------------------------------------------
    // Constructors
    //--------------------------------------------------------------------------
    
    /** */
    private WKLogoListFormat () { }
    
    //--------------------------------------------------------------------------
    // Instance methods
    //--------------------------------------------------------------------------
    
    /** */
    public Projection parseProjection (LogoList wkList) throws ParseException {
        return ProjectionFormat.getInstance().parseProjection(parseWKT(wkList));
    }
    
    /** */
    @SuppressWarnings("unchecked")
    public WKTElement parseWKT (LogoList wkList) throws ParseException {
        WKTElement result = new WKTElement((String)wkList.first());
        for (Iterator iterator = wkList.listIterator(1); iterator.hasNext();) {
            Object elt = iterator.next();
            if (elt instanceof LogoList) {
                result.addContent(parseWKT((LogoList)elt));
            } else {
                result.addContent(elt);
            }
        }
        return result;
    }
    
    /** */
    public LogoList format (Projection proj) {
        return format(ProjectionFormat.getInstance().toWKT(proj));
    }
    
    /** */
    @SuppressWarnings("unchecked")
    public LogoList format (WKTElement wkt) {
        LogoListBuilder result = new LogoListBuilder();
        result.add(wkt.getKeyword());
        for (Iterator iterator = wkt.iterator(); iterator.hasNext();) {
            Object elt = iterator.next();
            if (elt instanceof WKTElement) {
                result.add(format((WKTElement)elt));
            } else {
                result.add(elt);
            }
        }
        return result.toLogoList();
    }
}
