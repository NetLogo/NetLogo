//
// Copyright (c) 2007 Eric Russell. All rights reserved.
//

package org.myworldgis.wkt;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;


/** 
 * 
 */
public final strictfp class WKTElement {
    
    //--------------------------------------------------------------------------
    // Inner classes
    //--------------------------------------------------------------------------
    
    /** */
    private final String _keyword;
    
    /** */
    private List<Object> _contents;
    
    //--------------------------------------------------------------------------
    // Constructors
    //--------------------------------------------------------------------------
    
    /** */
    public WKTElement (String keyword) {
        _keyword = keyword.toUpperCase();
        _contents = new LinkedList<Object>();
    }

    /** */
    public WKTElement (String keyword, List<Object> contents) {
        _keyword = keyword.toUpperCase();
        _contents = contents;
    }

    /** */
    public WKTElement (String keyword, Object... contents) {
        _keyword = keyword.toUpperCase();
        _contents = Arrays.asList(contents);
    }

    //--------------------------------------------------------------------------
    // Instance methods
    //--------------------------------------------------------------------------
    
    /** */
    public String getKeyword () {
        return _keyword;
    }
    
    /** */
    public void addContent (Object newContent) {
        _contents.add(newContent);
    }
    
    /** */
    public Iterator<Object> iterator () {
        return Collections.unmodifiableCollection(_contents).iterator();
    }
    
    /** */
    public Number nextNumber (boolean required) throws ParseException {
        for (Iterator<Object> iterator = _contents.iterator(); iterator.hasNext();) {
            final Object object = iterator.next();
            if (object instanceof Number) {
                iterator.remove();
                return (Number)object;
            }
        }
        if (required) {
            throw new ParseException("missing required number parameter", 0);
        } else {
            return null;
        }
    }
    
    /** */
    public String nextString (boolean required) throws ParseException {
        for (Iterator<Object> iterator = _contents.iterator(); iterator.hasNext();) {
            final Object object = iterator.next();
            if (object instanceof String) {
                iterator.remove();                
                return (String)object;
            }
        }
        if (required) {
            throw new ParseException("missing required string parameter", 0);
        } else {
            return null;
        }
    }

    /** */
    public WKTElement nextElement (String key, boolean required) throws ParseException {
        for (Iterator<Object> iterator = _contents.iterator(); iterator.hasNext();) {
            final Object object = iterator.next();
            if (object instanceof WKTElement) {
                final WKTElement element = (WKTElement)object;
                if ((element._contents != null) && element._keyword.equals(key)) {
                    iterator.remove();
                    return element;
                }
            }
        }
        if (required) {
            throw new ParseException("missing required element '"+key+"'", 0);
        } else {
            return null;
        }
    }
}
