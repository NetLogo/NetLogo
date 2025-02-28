// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

// There's a lot of carelessness here about Component vs.
// Widget that should be cleaned up at some point
//   - ST 8/9/03, 10/14/03

import java.awt.{ Component, Container, Dimension, Font, Point, Rectangle }

import scala.collection.mutable.Map

class Zoomer(container: Container) {
  private var _zoomFactor = 1.0

  def zoomFactor: Double =
    _zoomFactor

  private val sizes = Map[Component, Dimension]()
  private val sizeZooms = Map[Component, Double]()
  private val locations = Map[Component, Point]()
  private val locationZooms = Map[Component, Double]()
  private val fonts = Map[Component, Font]()
  private val fontZooms = Map[Component, Double]()

  ///

  def zoomWidgets(newZoom: Double) {
    for (component <- container.getComponents) {
      component match {
        case w: WidgetWrapperInterface => zoomWidget(w, false, false, _zoomFactor, newZoom)
        case _ =>
      }
    }

    _zoomFactor = newZoom
  }

  def zoomWidget(wrapper: WidgetWrapperInterface, newWidget: Boolean, loadingWidget: Boolean, oldZoom: Double,
                 newZoom: Double) {
    if (oldZoom != newZoom) {
      zoomWidgetSize(wrapper, newWidget, loadingWidget, oldZoom, newZoom)
      zoomWidgetLocation(wrapper, newWidget, loadingWidget, oldZoom, newZoom)
      zoomWidgetFont(wrapper, wrapper.widget, newWidget, loadingWidget, oldZoom, newZoom)
    }
  }

  def zoomWidgetSize(wrapper: WidgetWrapperInterface, newWidget: Boolean, loadingWidget: Boolean, oldZoom: Double,
                     newZoom: Double) {
    val component = wrapper.widget
    var originalSize = sizes.get(component)
    var originalZoom = sizeZooms.get(component)

    if (originalSize.isEmpty) {
      originalSize = Some(component.getSize)
      originalZoom = Some(oldZoom)

      if (!newWidget || loadingWidget) {
        sizes += ((component, originalSize.get))
        sizeZooms += ((component, originalZoom.get))
      }
    }

    if (!newWidget || loadingWidget)
      wrapper.setSize(zoomSize(originalSize.get, originalZoom.get, newZoom))

    wrapper.widget.setZoomFactor(newZoom)
  }

  def zoomSize(originalSize: Dimension, oldZoom: Double, newZoom: Double): Dimension =
    new Dimension((originalSize.width * newZoom / oldZoom).ceil.toInt,
                  (originalSize.height * newZoom / oldZoom).ceil.toInt)

  def zoomSize(originalSize: Dimension): Dimension =
    zoomSize(originalSize, 1.0, zoomFactor)

  def zoomWidgetLocation(wrapper: WidgetWrapperInterface, newWidget: Boolean, loadingWidget: Boolean, oldZoom: Double,
                         newZoom: Double) {
    val component = wrapper.widget
    var originalLocation = locations.get(component)
    var originalZoom = locationZooms.get(component)

    if (originalLocation.isEmpty) {
      originalLocation = Some(wrapper.getUnselectedLocation)
      originalZoom = Some(oldZoom)

      if (!newWidget || loadingWidget) {
        locations += ((component, originalLocation.get))
        locationZooms += ((component, originalZoom.get))
      }
    }

    if (!newWidget || loadingWidget)
      wrapper.setLocation(zoomLocation(originalLocation.get, originalZoom.get, newZoom))
  }

  private def zoomLocation(originalLocation: Point, oldZoom: Double, newZoom: Double): Point =
    new Point((originalLocation.x * newZoom / oldZoom).ceil.toInt, (originalLocation.y * newZoom / oldZoom).ceil.toInt)

  /**
   * it may seems redundant to take both wrapper and widget as arguments,
   * but when we are used by CommandCenter, there is no wrapper - ST 7/13/04
   */
  def zoomWidgetFont(wrapper: WidgetWrapperInterface, widget: Widget, newWidget: Boolean, loadingWidget: Boolean,
                     oldZoom: Double, newZoom: Double) {
    if (!fonts.contains(widget))
      storeComponentFont(widget, true, newWidget, loadingWidget, oldZoom)

    scaleComponentFont(widget, newZoom, oldZoom, true)

    if (wrapper != null)
      wrapper.validate()
  }

  private def storeComponentFont(component: Component, recursive: Boolean, newWidget: Boolean, loadingWidget: Boolean,
                                 oldZoom: Double) {
    // note that we usually ignore newWidget and loadingWidget flags -- we always
    // want to remember what the font size at the 100% zoom level is, because
    // font sizes are so small that rounding error will mess us up unless we
    // always scale from the normal, unzoomed size
    if ((!(component.isInstanceOf[ViewWidgetInterface]) || !newWidget || loadingWidget) &&
        !fonts.contains(component)) {
      fonts += ((component, component.getFont))
      fontZooms += ((component, oldZoom))
    }

    if (recursive) {
      component match {
        case c: Container =>
          for (component <- c.getComponents)
            storeComponentFont(component, true, newWidget, loadingWidget, oldZoom)
        case _ =>
      }
    }
  }

  def scaleComponentFont(component: Component, newZoom: Double, oldZoom: Double, recursive: Boolean) {
    if (!fonts.contains(component))
      storeComponentFont(component, recursive, false, false, oldZoom)

    component.invalidate()
    component.setFont(component.getFont.deriveFont(
      (fonts(component).getSize * newZoom / fontZooms(component)).ceil.toFloat))

    if (recursive) {
      component match {
        case c: Container =>
          for (component <- c.getComponents)
            scaleComponentFont(component, newZoom, oldZoom, true)
        case _ =>
      }
    }
  }

  def forgetAllZoomInfo() {
    sizes.clear()
    sizeZooms.clear()
    locations.clear()
    locationZooms.clear()

    // the graphics window is special since it isn't recreated
    // on every load - ST 10/31/03
    for (component <- fonts.keys) {
      if (!component.isInstanceOf[View]) {
        fonts.remove(component)
        fontZooms.remove(component)
      }
    }
  }

  def updateZoomInfo(component: Component) {
    component.getParent match {
      case wrapper: WidgetWrapperInterface =>
        sizes.get(component).foreach(size =>
          if (component.getSize != zoomSize(size, sizeZooms(component), zoomFactor)) {
            sizes -= component
            sizeZooms -= component
          }
        )

        locations.get(component).foreach(location =>
          if (wrapper.getUnselectedLocation != zoomLocation(location, locationZooms(component), zoomFactor)) {
            locations -= component
            locationZooms -= component
          }
        )

        // except for View, always go from original font
        // size, since at the moment the user can't the change
        // individual font sizes of other widget types
        component match {
          case v: ViewWidget =>
            fonts.remove(v.view)
            fontZooms.remove(v.view)
          case _ =>
        }

      case _ =>
    }
  }

  /// called indirectly from the save() methods of the individual Widget classes

  def getUnzoomedBounds(component: Component): Rectangle = {
    val r = component.getBounds

    if (component.getParent != null) {
      r.x += component.getParent.getLocation.x
      r.y += component.getParent.getLocation.y
    }

    val unzoomedLocation =
      locations.get(component) match {
        case Some(location) => zoomLocation(location, locationZooms(component), 1.0)
        case None => zoomLocation(r.getLocation, zoomFactor, 1.0)
      }

    val unzoomedSize =
      sizes.get(component) match {
        case Some(size) => zoomSize(size, sizeZooms(component), 1.0)
        case None => zoomSize(r.getSize, zoomFactor, 1.0)
      }

    new Rectangle(unzoomedLocation, unzoomedSize)
  }

  def getFontForSave(component: Component): Int = {
    fonts.get(component) match {
      case Some(font) => (font.getSize / fontZooms(component)).ceil.toInt
      case None => (component.getFont.getSize / zoomFactor).ceil.toInt
    }
  }
}
