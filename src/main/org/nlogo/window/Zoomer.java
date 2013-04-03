// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window;

// There's a lot of carelessness here about Component vs.
// Widget that should be cleaned up at some point
//   - ST 8/9/03, 10/14/03

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public strictfp class Zoomer {

  private final java.awt.Container container;

  private double zoomFactor = 1.0;

  public double zoomFactor() {
    return zoomFactor;
  }

  private final Map<java.awt.Component, java.awt.Dimension> sizes =
      new HashMap<java.awt.Component, java.awt.Dimension>();
  private final Map<java.awt.Component, Double> sizeZooms =
      new HashMap<java.awt.Component, Double>();
  private final Map<java.awt.Component, java.awt.Point> locations =
      new HashMap<java.awt.Component, java.awt.Point>();
  private final Map<java.awt.Component, Double> locationZooms =
      new HashMap<java.awt.Component, Double>();
  private final Map<java.awt.Component, java.awt.Font> fonts =
      new HashMap<java.awt.Component, java.awt.Font>();
  private final Map<java.awt.Component, Double> fontZooms =
      new HashMap<java.awt.Component, Double>();

  public Zoomer(java.awt.Container container) {
    this.container = container;
  }

  ///

  public void zoomWidgets(double newZoom) {
    container.setVisible(false);
    double oldZoom = zoomFactor;
    zoomFactor = newZoom;
    java.awt.Component[] comps = container.getComponents();
    for (int i = 0; i < comps.length; i++) {
      if (comps[i] instanceof WidgetWrapperInterface) {
        WidgetWrapperInterface wrapper = (WidgetWrapperInterface) comps[i];
        zoomWidget(wrapper, false, false, oldZoom, zoomFactor);
      }
    }
    container.setVisible(true);
  }

  public void zoomWidget(WidgetWrapperInterface wrapper, boolean newWidget, boolean loadingWidget,
                         double oldZoom, double newZoom) {
    if (oldZoom == newZoom) {
      return;
    }
    zoomWidgetSize(wrapper, newWidget, loadingWidget, oldZoom, newZoom);
    zoomWidgetLocation(wrapper, newWidget, loadingWidget, oldZoom, newZoom);
    zoomWidgetFont(wrapper, wrapper.widget(), newWidget, loadingWidget, oldZoom, newZoom);
  }

  public void zoomWidgetSize(WidgetWrapperInterface wrapper, boolean newWidget, boolean loadingWidget,
                      double oldZoom, double newZoom) {
    java.awt.Component component = wrapper.widget();
    java.awt.Dimension originalSize = sizes.get(component);
    Double originalZoom = sizeZooms.get(component);
    if (originalSize == null) {
      originalSize = component.getSize();
      originalZoom = Double.valueOf(oldZoom);
      if (!newWidget || loadingWidget) {
        sizes.put(component, originalSize);
        sizeZooms.put(component, originalZoom);
      }
    }
    if (!newWidget || loadingWidget) {
      wrapper.setSize(zoomSize(originalSize, originalZoom.doubleValue(), newZoom));
    }
  }

  public java.awt.Dimension zoomSize(java.awt.Dimension originalSize, double oldZoom, double newZoom) {
    return new java.awt.Dimension
        ((int) StrictMath.ceil(originalSize.width * newZoom / oldZoom),
            (int) StrictMath.ceil(originalSize.height * newZoom / oldZoom));
  }

  public java.awt.Dimension zoomSize(java.awt.Dimension originalSize) {
    return zoomSize(originalSize, 1.0, zoomFactor);
  }

  public void zoomWidgetLocation(WidgetWrapperInterface wrapper, boolean newWidget, boolean loadingWidget,
                                 double oldZoom, double newZoom) {
    java.awt.Component component = wrapper.widget();
    java.awt.Point originalLocation = locations.get(component);
    Double originalZoom = locationZooms.get(component);
    if (originalLocation == null) {
      originalLocation = wrapper.getUnselectedLocation();
      originalZoom = Double.valueOf(oldZoom);
      if (!newWidget || loadingWidget) {
        locations.put(component, originalLocation);
        locationZooms.put(component, originalZoom);
      }
    }
    if (!newWidget || loadingWidget) {
      wrapper.setLocation
          (zoomLocation
              (originalLocation, originalZoom.doubleValue(), newZoom));
    }
  }

  private java.awt.Point zoomLocation(java.awt.Point originalLocation,
                                      double oldZoom, double newZoom) {
    return new java.awt.Point
        ((int) StrictMath.ceil(originalLocation.x * newZoom / oldZoom),
            (int) StrictMath.ceil(originalLocation.y * newZoom / oldZoom));
  }

  /**
   * it may seems redundant to take both wrapper and widget as arguments,
   * but when we are used by CommandCenter, there is no wrapper - ST 7/13/04
   */
  public void zoomWidgetFont(WidgetWrapperInterface wrapper, Widget widget,
                             boolean newWidget, boolean loadingWidget,
                             double oldZoom, double newZoom) {
    boolean recursive = widget.zoomSubcomponents();
    if (fonts.get(widget) == null) {
      storeComponentFont(widget, recursive, newWidget, loadingWidget, oldZoom);
    }
    scaleComponentFont(widget, newZoom, oldZoom, recursive);
    if (wrapper != null && recursive) {
      wrapper.validate();
    }
  }

  private void storeComponentFont(java.awt.Component component, boolean recursive,
                                  boolean newWidget, boolean loadingWidget, double oldZoom) {
    // note that we usually ignore newWidget and loadingWidget flags -- we always
    // want to remember what the font size at the 100% zoom level is, because
    // font sizes are so small that rounding error will mess us up unless we
    // always scale from the normal, unzoomed size
    if ((!(component instanceof ViewWidgetInterface) ||
         !newWidget ||
         loadingWidget) && fonts.get(component) == null) {
      fonts.put(component, component.getFont());
      fontZooms.put(component, Double.valueOf(oldZoom));
    }
    if (recursive && component instanceof java.awt.Container) {
      java.awt.Component[] comps =
          ((java.awt.Container) component).getComponents();
      for (int i = 0; i < comps.length; i++) {
        storeComponentFont(comps[i], true, newWidget, loadingWidget, oldZoom);
      }
    }
  }

  public void scaleComponentFont(java.awt.Component component, double newZoom, double oldZoom,
                                 boolean recursive) {
    if (fonts.get(component) == null) {
      storeComponentFont(component, recursive, false, false, oldZoom);
    }
    java.awt.Font originalFont = fonts.get(component);
    double originalZoom = fontZooms.get(component).doubleValue();
    component.invalidate();
    component.setFont
        (component.getFont().deriveFont
            ((float) StrictMath.ceil
                (originalFont.getSize() * newZoom / originalZoom)));
    if (recursive && component instanceof java.awt.Container) {
      java.awt.Component[] comps =
          ((java.awt.Container) component).getComponents();
      for (int i = 0; i < comps.length; i++) {
        scaleComponentFont(comps[i], newZoom, oldZoom, true);
      }
    }
  }

  public void forgetAllZoomInfo() {
    sizes.clear();
    sizeZooms.clear();
    locations.clear();
    locationZooms.clear();
    // the graphics window is special since it isn't recreated
    // on every load - ST 10/31/03
    for (Iterator<java.awt.Component> comps = fonts.keySet().iterator();
         comps.hasNext();) {
      java.awt.Component comp = comps.next();
      if (!(comp instanceof View)) {
        comps.remove();
        fontZooms.remove(comp);
      }
    }
  }

  public void updateZoomInfo(java.awt.Component component) {
    java.awt.Container parent = component.getParent();
    if (parent instanceof WidgetWrapperInterface) {
      WidgetWrapperInterface wrapper = (WidgetWrapperInterface) parent;

      java.awt.Dimension storedSize = sizes.get(component);
      if (storedSize != null &&
          !component.getSize().equals
              (zoomSize(storedSize,
                  sizeZooms.get(component).doubleValue(),
                  zoomFactor))) {
        sizes.remove(component);
        sizeZooms.remove(component);
      }

      java.awt.Point storedLocation = locations.get(component);
      if (storedLocation != null &&
          !wrapper.getUnselectedLocation().equals
              (zoomLocation(storedLocation,
                  locationZooms.get(component).doubleValue(),
                  zoomFactor))) {
        locations.remove(component);
        locationZooms.remove(component);
      }
      // except for View, always go from original font
      // size, since at the moment the user can't the change
      // individual font sizes of other widget types
      if (component instanceof ViewWidget) {
        fonts.remove
            (((ViewWidget) component).view);
        fontZooms.remove
            (((ViewWidget) component).view);
      }
    }
  }

  /// called indirectly from the save() methods of the individual Widget classes

  public java.awt.Rectangle getUnzoomedBounds(java.awt.Component comp) {
    java.awt.Rectangle r = comp.getBounds();
    java.awt.Component parent = comp.getParent();
    if (parent != null) {
      r.x += comp.getParent().getLocation().x;
      r.y += comp.getParent().getLocation().y;
    }
    java.awt.Point unzoomedLocation;
    java.awt.Point originalLocation = locations.get(comp);
    if (originalLocation == null) {
      unzoomedLocation = zoomLocation(r.getLocation(), zoomFactor, 1.0);
    } else {
      unzoomedLocation =
          zoomLocation
              (originalLocation,
                  locationZooms.get(comp).doubleValue(),
                  1.0);
    }
    java.awt.Dimension unzoomedSize;
    {
      java.awt.Dimension originalSize = sizes.get(comp);
      if (originalSize == null) {
        unzoomedSize = zoomSize(r.getSize(), zoomFactor, 1.0);
      } else {
        unzoomedSize =
            zoomSize(originalSize,
                sizeZooms.get(comp).doubleValue(),
                1.0);
      }
    }
    return new java.awt.Rectangle(unzoomedLocation, unzoomedSize);
  }

  int getFontForSave(java.awt.Component comp) {
    int size = comp.getFont().getSize();
    java.awt.Font originalFont = fonts.get(comp);
    Double originalZoom = fontZooms.get(comp);
    if (originalFont != null) {
      size = (int) StrictMath.ceil(originalFont.getSize() / originalZoom.doubleValue());
    } else {
      size = (int) StrictMath.ceil(size / zoomFactor);
    }
    return size;
  }

}
