//
// Copyright (c)2006 by the National Geographic Society. All Rights Reserved.
// 

package org.myworldgis.util;

import com.vividsolutions.jts.geom.Envelope;
import java.awt.Dimension;

/** */
public class WMSUtils {
    
    
    //--------------------------------------------------------------------------
    // Inner classes
    //--------------------------------------------------------------------------

    public enum ImageFormat {
        
        JPEG("image/jpeg"),
        PNG("image/png"),
        GIF("image/gif");
        
        private final String _mimeType;
        
        ImageFormat (String mimeType) {
            _mimeType = mimeType;
        }
        
        public String mimeType () {
            return _mimeType;
        }
    }
    
    //--------------------------------------------------------------------------
    // Class methods
    //--------------------------------------------------------------------------
    

    /** */
    public static String makeGetMapURL (String serverURL,
                                        Envelope viewBounds,
                                        Dimension pixelDimensions,
                                        String srs,
                                        String layers,
                                        ImageFormat imageFormat) {
        URLBuilder url = new URLBuilder(serverURL);
        url.setParameter("Service", "wms");
        url.setParameter("Version", "1.1.1");
        url.setParameter("Request", "GetMap");
        url.setParameter("SRS", srs);
        url.setParameter("Layers", layers);
        url.setParameter("BBOX", viewBounds);
        // TerraServer restricts sizes to between 50-2000, 
        // so we'll just enforce that for everyone.
        url.setParameter("Width", Integer.toString(Math.min(Math.max(pixelDimensions.width, 50), 2000)));
        url.setParameter("Height", Integer.toString(Math.min(Math.max(pixelDimensions.height, 50), 2000)));
        url.setParameter("Format", imageFormat.mimeType());
        url.setParameter("Styles", "");
        url.setParameter("Transparent", "TRUE");
        return(url.toString());
    }
}
