//
// Copyright (c) 2007 Eric Russell. All rights reserved.
//

package org.myworldgis.netlogo;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import java.awt.geom.AffineTransform;
import org.nlogo.api.ExtensionObject;


/**
 * 
 */
public final strictfp class CoordinateTransformation implements ExtensionObject {
    
    //--------------------------------------------------------------------------
    // Instance variables
    //--------------------------------------------------------------------------
    
    /** */
    private final Envelope _gisEnvelope;
    
    /** */
    private final Envelope _netLogoEnvelope;
    
    /** */
    private final Coordinate _gisSpaceCenter;
    
    /** */
    private final Coordinate _netLogoSpaceCenter;
    
    /** NetLogo units per GIS units */
    private final double _scaleX;

    /** NetLogo units per GIS units */
    private final double _scaleY;
    
    //--------------------------------------------------------------------------
    // Constructors
    //--------------------------------------------------------------------------
    
    /** */
    public CoordinateTransformation (Envelope gisSpaceEnvelope,
                                     Envelope netLogoSpaceEnvelope,
                                     boolean equalizeScales) {
        _gisEnvelope = new Envelope(gisSpaceEnvelope);
        _netLogoEnvelope = new Envelope(netLogoSpaceEnvelope);
        _netLogoEnvelope.expandBy(0.5);
        double sx = _netLogoEnvelope.getWidth() / _gisEnvelope.getWidth();
        double sy = _netLogoEnvelope.getHeight() / _gisEnvelope.getHeight();
        if (equalizeScales) {
            _scaleX = StrictMath.min(sx, sy);
            _scaleY = _scaleX;
        } else {
            _scaleX = sx;
            _scaleY = sy;
        }
        _gisSpaceCenter = new Coordinate(_gisEnvelope.getMinX() + (_gisEnvelope.getWidth() / 2.0),
                                        _gisEnvelope.getMinY() + (_gisEnvelope.getHeight() / 2.0));
        _netLogoSpaceCenter = new Coordinate(_netLogoEnvelope.getMinX() + (_netLogoEnvelope.getWidth() / 2.0),
                                             _netLogoEnvelope.getMinY() + (_netLogoEnvelope.getHeight() / 2.0));
    }
    
    //--------------------------------------------------------------------------
    // Instance methods
    //--------------------------------------------------------------------------
    
    /** */
    public AffineTransform getNetLogoToGISTransform () {
        AffineTransform result = AffineTransform.getTranslateInstance(_gisSpaceCenter.x,
                                                                      _gisSpaceCenter.y);
        result.scale(1.0 / _scaleX, 1.0 / _scaleY);
        result.translate(-_netLogoSpaceCenter.x, -_netLogoSpaceCenter.y);
        return result;
    }

    /** */
    public AffineTransform getGISToNetLogoTransform () {
        AffineTransform result = AffineTransform.getTranslateInstance(_netLogoSpaceCenter.x, 
                                                                      _netLogoSpaceCenter.y);
        result.scale(_scaleX, _scaleY);
        result.translate(-_gisSpaceCenter.x, -_gisSpaceCenter.y);
        return result;
    }
    
    /** */
    public Coordinate netLogoToGIS (Coordinate pt, Coordinate storage) {
        if (storage == null) {
            storage = new Coordinate();
        }
        storage.x = ((pt.x - _netLogoSpaceCenter.x) / _scaleX) + _gisSpaceCenter.x;
        storage.y = ((pt.y - _netLogoSpaceCenter.y) / _scaleY) + _gisSpaceCenter.y;
        return storage;
    }
    
    /** */
    public Coordinate gisToNetLogo (Coordinate pt, Coordinate storage) {
        if (storage == null) {
            storage = new Coordinate();
        }
        storage.x = ((pt.x - _gisSpaceCenter.x) * _scaleX) + _netLogoSpaceCenter.x;
        storage.y = ((pt.y - _gisSpaceCenter.y) * _scaleY) + _netLogoSpaceCenter.y;
        if (_netLogoEnvelope.contains(storage)) {
            return storage;
        } else {
            return null;
        }
    }
    
    /** */
    public Envelope getEnvelope (org.nlogo.api.World world) {
        Coordinate bottomLeft = new Coordinate(world.minPxcor() - 0.5, world.minPycor() - 0.5);
        Coordinate topRight = new Coordinate(world.maxPxcor() + 0.5, world.maxPycor() + 0.5);
        netLogoToGIS(bottomLeft, bottomLeft); 
        netLogoToGIS(topRight, topRight);
        return new Envelope(bottomLeft.x, topRight.x, bottomLeft.y, topRight.y);
    }
    
    //--------------------------------------------------------------------------
    // ExtensionObject implementation
    //--------------------------------------------------------------------------

    /** */
    public String dump( boolean readable, boolean exporting , boolean reference ) {
        return "";
    }

    /** */
    public String getExtensionName () {
        return "gis";
    }

    /** */
    public String getNLTypeName () {
        return "CT";
    }

    /** */
    public boolean recursivelyEqual (Object o) {
        if (o instanceof CoordinateTransformation) {
            CoordinateTransformation cs = (CoordinateTransformation) o;
            return cs._gisSpaceCenter.equals(_gisSpaceCenter) &&
                   cs._netLogoSpaceCenter.equals(_netLogoSpaceCenter) &&
                   (cs._scaleX == _scaleX) &&
                   (cs._scaleY == _scaleY);
        } else {
            return false;
        }
    }
}

