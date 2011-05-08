//
// Copyright (c) 2007 Eric Russell. All rights reserved.
//

package org.myworldgis.netlogo.gui;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.geom.util.GeometryTransformer;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.event.MouseInputAdapter;
import javax.swing.event.MouseInputListener;
import org.myworldgis.io.shapefile.ESRIShapefileReader;
import org.myworldgis.netlogo.GISExtension;
import org.myworldgis.projection.Projection;
import org.myworldgis.projection.ProjectionFormat;


/** 
 * 
 */
public final strictfp class GISDataView extends JComponent {

    //--------------------------------------------------------------------------
    // Inner classes
    //--------------------------------------------------------------------------
    
    /** */
    private static class ProjectedSpaceToPixelSpaceTransform extends AffineTransform {
        
        static final long serialVersionUID = 1L;
        private Point2D _gisCenter;
        private double _scale;
        private Point2D _pixelCenter;
        
        public ProjectedSpaceToPixelSpaceTransform (Point2D gisCenter, 
                                                    double scale, 
                                                    Point2D pixelCenter) {
            _gisCenter = gisCenter;
            _scale = scale;
            _pixelCenter = pixelCenter;
            recomputeTransform();
        }
        
        public Point2D getProjectedCenter () {
            return (Point2D)_gisCenter.clone();
        }
        
        public void setProjectedCenter (Point2D newCenter) {
            _gisCenter.setLocation(newCenter.getX(), newCenter.getY());
            recomputeTransform();
        }
        
        public double getScale () {
            return(_scale);
        }
        
        public void setScale (double newScale) {
            if (newScale != _scale) {
                _scale = newScale;
                recomputeTransform();
            }
        }
        
        public Point2D getPixelCenter () {
            return (Point2D)_pixelCenter.clone();
        }
        
        public void setPixelCenter (Point2D newCenter) {
            _pixelCenter.setLocation(newCenter.getX(), newCenter.getY());
            recomputeTransform();
        }
        
        private void recomputeTransform () {
            setToTranslation(_pixelCenter.getX(), _pixelCenter.getY());
            scale(_scale, -_scale);
            translate(-_gisCenter.getX(), -_gisCenter.getY());
        }
    }
    
    /** */
    private class DragListener extends MouseInputAdapter {
        
        private java.awt.Point _last;
        
        public void mousePressed (MouseEvent evt) {
            _last = evt.getPoint();
        }
        
        public void mouseReleased (MouseEvent evt) {
            _last = null;
        }
        
        public void mouseDragged (MouseEvent evt) {
            if (_last != null) {
                int dx = evt.getX() - _last.x;
                int dy = evt.getY() - _last.y;
                Point2D oldGISCenter = _transform.getProjectedCenter();
                double newX = oldGISCenter.getX() - (dx / _transform.getScale());
                double newY = oldGISCenter.getY() + (dy / _transform.getScale());
                _transform.setProjectedCenter(new Point2D.Double(newX, newY));
                _last = evt.getPoint();
                repaint();
            }
        }
        
        public void mouseMoved (MouseEvent evt) {
            if (_proj != null) {
                try {
                    Point2D p = _transform.inverseTransform(evt.getPoint(), null);
                    System.out.println(_proj.getInverseTransformer().transform(FACTORY.createPoint(new Coordinate(p.getX(), p.getY()))));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }   
    }
    
    /** */
    private class ZoomListener implements MouseWheelListener {
        
        public void mouseWheelMoved (MouseWheelEvent evt) {
            if (evt.getUnitsToScroll() > 0) {
                _transform.setScale(_transform.getScale() * 0.875);
            } else {
                _transform.setScale(_transform.getScale() * 1.142857);
            }
            repaint();
        }
    }
    
    //--------------------------------------------------------------------------
    // Class variables
    //--------------------------------------------------------------------------
    
    /** */
    static final long serialVersionUID = 1L;
    
    /** */
    static final Stroke STROKE = new BasicStroke(0.001f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_BEVEL);
    
    /** */
    static final GeometryFactory FACTORY = new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING));
    
    //--------------------------------------------------------------------------
    // Class methods
    //--------------------------------------------------------------------------
    
    /** */
    public static void main (String[] args) {
        try {
            GISDataView dataView = new GISDataView();
            //Projection proj = ProjectionFormat.getInstance().parseProjection("PROJCS[\"Orthographic\",GEOGCS[\"GCS_WGS_1984\",DATUM[\"D_WGS_1984\",SPHEROID[\"WGS_1984\",6378137,298.257223563]],PRIMEM[\"Greenwich\",0],UNIT[\"Degree\",0.017453292519943295]],PROJECTION[\"Orthographic\"],PARAMETER[\"False_Easting\",0],PARAMETER[\"False_Northing\",0],PARAMETER[\"Longitude_Of_Center\",-98],PARAMETER[\"Latitude_Of_Center\",36],UNIT[\"Meter\",1]]");
            //Projection proj = ProjectionFormat.getInstance().parseProjection("PROJCS[\"World_Equidistant_Conic\",GEOGCS[\"GCS_WGS_1984\",DATUM[\"D_WGS_1984\",SPHEROID[\"WGS_1984\",6378137,298.257223563]],PRIMEM[\"Greenwich\",0],UNIT[\"Degree\",0.017453292519943295]],PROJECTION[\"Equidistant_Conic\"],PARAMETER[\"False_Easting\",0],PARAMETER[\"False_Northing\",0],PARAMETER[\"Central_Meridian\",0],PARAMETER[\"Standard_Parallel_1\",60],PARAMETER[\"Standard_Parallel_2\",60],PARAMETER[\"Latitude_Of_Origin\",0],UNIT[\"Meter\",1]]");
            Projection proj = ProjectionFormat.getInstance().parseProjection(new BufferedReader(new FileReader("c:/java/gis/data/Lambert_Conformal_Conic.prj")));
            dataView.setProjection(proj);
            GeometryTransformer xform = proj.getForwardTransformer();
            Envelope env = new Envelope();
            GeometryFactory factory = null;
            if (GISExtension.getState() != null) {
                factory = GISExtension.getState().factory();
            } else {
                factory = FACTORY;
            }
            ESRIShapefileReader shp = new ESRIShapefileReader(new FileInputStream(new File("data/countries.shp")),
                                                              Projection.DEGREES_TO_RADIANS,
                                                              factory);
            while (true) {
                try {
                    Geometry geom = shp.getNextShape();
                    if (geom == null) {
                        break;
                    } else {
                        Geometry xGeom = xform.transform(geom);
                        env.expandToInclude(xGeom.getEnvelopeInternal());
                        dataView.addGeometry(xGeom);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }                      
            }
            dataView.zoomToEnvelope(env);
            JFrame window = new JFrame("GIS Data View");
            window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            window.getContentPane().add(dataView);
            window.pack();
            window.setVisible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /** */
    private static void addToPath (LineString line, GeneralPath p) {
        Coordinate c = line.getCoordinateN(0);
        p.moveTo((float)c.x, (float)c.y);
        for (int i = 1; i < line.getNumPoints(); i += 1) {
            c = line.getCoordinateN(i);
            p.lineTo((float)c.x, (float)c.y);
        }
    }
    
    //--------------------------------------------------------------------------
    // Instance variables
    //--------------------------------------------------------------------------
    
    /** */
    private List<Shape> _shapes;
    
    /** */
    private ProjectedSpaceToPixelSpaceTransform _transform;
    
    /** */
    private Projection _proj;
    
    //--------------------------------------------------------------------------
    // Constructors
    //--------------------------------------------------------------------------
    
    /** */
    public GISDataView () {
        setOpaque(true);
        setPreferredSize(new Dimension(300, 300));
        setBackground(Color.WHITE);
        _shapes = new ArrayList<Shape>();
        _transform = new ProjectedSpaceToPixelSpaceTransform(new Point2D.Float(0.0f, 0.0f),
                                                             180.0 / 300.0,
                                                             new Point2D.Float(150f, 150f));
        _proj = null;
        MouseInputListener dragListener = new DragListener();
        addMouseListener(dragListener);
        addMouseMotionListener(dragListener);
        addMouseWheelListener(new ZoomListener());
        setSize(getPreferredSize());
    }
    
    //--------------------------------------------------------------------------
    // Instance methods
    //--------------------------------------------------------------------------
    
    /** */
    public void zoomToEnvelope (Envelope env) {
        double cx = env.getMinX() + ((env.getMaxX() - env.getMinX()) / 2.0);
        double cy = env.getMinY() + ((env.getMaxY() - env.getMinY()) / 2.0);
        _transform.setProjectedCenter(new Point2D.Double(Double.isNaN(cx) ? 0.0 : cx, 
                                                         Double.isNaN(cy) ? 0.0 : cy));
        double xScale = getWidth() / env.getWidth();
        if (xScale == 0) {
            xScale = Double.MAX_VALUE;
        }
        double yScale = getHeight() / env.getHeight();
        if (yScale == 0) {
            yScale = Double.MAX_VALUE;
        }
        _transform.setScale(StrictMath.min(xScale, yScale));
    }
    
    /** */
    public void addGeometry (Geometry geom) {
        if (geom instanceof Point) {
            Coordinate c = ((Point)geom).getCoordinate();
            _shapes.add(new Ellipse2D.Double(c.x - 0.01, c.y - 0.01, 0.02, 0.02));
            _shapes.add(new Ellipse2D.Double(c.x - 1000, c.y - 1000, 2000, 2000));
            _shapes.add(new Ellipse2D.Double(c.x - 10000, c.y - 10000, 20000, 20000));
            _shapes.add(new Ellipse2D.Double(c.x - 100000, c.y - 100000, 200000, 200000));
        } else if (geom instanceof LineString) {
            GeneralPath p = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
            addToPath((LineString)geom, p);
            _shapes.add(p);
        } else if (geom instanceof Polygon) {
            GeneralPath p = new GeneralPath(GeneralPath.WIND_NON_ZERO);
            Polygon poly = (Polygon)geom;
            addToPath(poly.getExteriorRing(), p);
            for (int i = 0; i < poly.getNumInteriorRing(); i += 1) {
                addToPath(poly.getInteriorRingN(i), p);
            }
            _shapes.add(p);
        } else if (geom instanceof GeometryCollection) {
            GeometryCollection gc = (GeometryCollection)geom;
            for (int i = 0; i < gc.getNumGeometries(); i += 1) {
                addGeometry(gc.getGeometryN(i));
            }
        }
    }
    
    /** */
    public Projection getProjection () {
        return _proj;
    }
    
    /** */
    public void setProjection (Projection proj) {
        _proj = proj;
    }
    
    /** */
    @SuppressWarnings("deprecation")
    public void reshape (int x, int y, int newWidth, int newHeight) {
        super.reshape(x, y, (newWidth > 0) ? newWidth : 1, (newHeight > 0) ? newHeight : 1);
        Dimension size = getSize();
        Insets insets = getInsets();
        _transform.setPixelCenter(new Point2D.Float(insets.left + ((size.width-(insets.left+insets.right)) / 2.0f),
                                                    insets.top + ((size.height-(insets.top+insets.bottom)) / 2.0f)));
    }
    
    /** */
    public void paintComponent (Graphics g) {
        g.setColor(getBackground());
        g.fillRect(0, 0, getWidth(),getHeight());
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D)g;
        g2d.setColor(Color.BLACK);
        g2d.setStroke(STROKE);
        g2d.setTransform(_transform);
        for (int i = 0; i < _shapes.size(); i += 1) {
            g2d.draw(_shapes.get(i));
        }
    }
}
