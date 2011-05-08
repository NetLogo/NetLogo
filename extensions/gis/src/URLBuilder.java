//
// Copyright (c)2010 by the National Geographic Society. All Rights Reserved.
//

package org.myworldgis.util;

import com.vividsolutions.jts.geom.Envelope;
import java.awt.Dimension;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/** */
public class URLBuilder 
{
    //--------------------------------------------------------------------------
    // Class variables
    //--------------------------------------------------------------------------
    
    /** */
    private static final NumberFormat NUMBER_FORMAT = new DecimalFormat("#########0.#####");

    //--------------------------------------------------------------------------
    // Instance variables
    //--------------------------------------------------------------------------
            
    private String baseUrl;
    
    private Map<String,String> queryVars;

    //--------------------------------------------------------------------------
    // Constructors
    //--------------------------------------------------------------------------
    
    public URLBuilder (String base) {
        try {
            URI uri = new URI(base);
            URI baseUri = new URI(uri.getScheme(), uri.getAuthority(), uri.getPath(), null, null);
            baseUrl = baseUri.toString();
            queryVars = new HashMap<String,String>();
            if ((uri.getQuery() != null) && (uri.getQuery().length() > 0)) {
                String[] queryParts = uri.getQuery().split("&");
                for (int i = 0; i < queryParts.length; i += 1) {
                    String[] queryVar = queryParts[i].split("=");
                    queryVars.put(queryVar[0], queryVar[1]);
                }
            }
        } catch (URISyntaxException e) {
            IllegalArgumentException ex = new IllegalArgumentException();
            ex.initCause(e);
            throw ex;
        }
    }

    //--------------------------------------------------------------------------
    // Instance methods
    //--------------------------------------------------------------------------
    
    public void setParameter (String key, String value) {
        queryVars.put(key, value);
    }
    
    public void setParameter (String key, Dimension value) {
        StringBuffer dim = new StringBuffer();
        dim.append(value.width);
        dim.append(",");
        dim.append(value.height);
        queryVars.put(key, dim.toString());
    }
    
    public void setParameter (String key, Envelope value) {
        StringBuffer bbox = new StringBuffer();
        bbox.append(NUMBER_FORMAT.format(value.getMinX()));
        bbox.append(",");
        bbox.append(NUMBER_FORMAT.format(value.getMinY()));
        bbox.append(",");
        bbox.append(NUMBER_FORMAT.format(value.getMaxX()));
        bbox.append(",");
        bbox.append(NUMBER_FORMAT.format(value.getMaxY()));
        queryVars.put(key, bbox.toString());
    }
    
    public String toString () {
        StringBuffer result = new StringBuffer();
        result.append(baseUrl);
        result.append("?");
        for (Iterator<String> e = queryVars.keySet().iterator(); e.hasNext();) {
            String key = e.next();
            result.append(key);
            result.append("=");
            result.append(queryVars.get(key));
            if (e.hasNext()) {
                result.append("&");
            }
        }
        return StringUtils.encodeURL(result.toString());
    }
}
