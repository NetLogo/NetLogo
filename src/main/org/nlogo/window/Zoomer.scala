// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

// There's a lot of carelessness here about Component vs.
// Widget that should be cleaned up at some point
//   - ST 8/9/03, 10/14/03

import java.awt.{ Component, Container, Dimension, Font, Point, Rectangle }
import scala.collection.mutable
import scala.annotation.tailrec

class Zoomer(container: Container) {
  private var _zoomFactor = 1: Double
  def zoomFactor = _zoomFactor

  private val sizes     = new mutable.HashMap[Component, Dimension]
  private val locations = new mutable.HashMap[Component, Point]
  private val fonts     = new mutable.HashMap[Component, Font]
  private val sizeZooms, locationZooms, fontZooms = new mutable.HashMap[Component, Double]

  ///

  def zoomWidgets(newZoom: Double) = {
    container.setVisible(false)
    val oldZoom = zoomFactor
    _zoomFactor = newZoom
    container.getComponents.foreach { case wrapper: WidgetWrapperInterface =>
      zoomWidget(wrapper, false, false, oldZoom, zoomFactor)
    }
    container.setVisible(true)
  }

  def zoomWidget(wrapper: WidgetWrapperInterface, newWidget: Boolean, loadingWidget: Boolean,
      oldZoom: Double, newZoom: Double) = if(oldZoom != newZoom) {
    zoomWidgetSize(wrapper, newWidget, loadingWidget, oldZoom, newZoom)
    zoomWidgetLocation(wrapper, newWidget, loadingWidget, oldZoom, newZoom)
    zoomWidgetFont(wrapper, wrapper.widget, newWidget, loadingWidget, oldZoom, newZoom)
  }

  def zoomWidgetSize(wrapper: WidgetWrapperInterface, newWidget: Boolean, loadingWidget: Boolean,
      oldZoom: Double, newZoom: Double) = {
    val component = wrapper.widget
    var originalSize = sizes.getOrElse(component, null)
    var originalZoom = sizeZooms.getOrElse(component, 0: Double)
    if(originalSize == null) {
      originalSize = component.getSize
      originalZoom = oldZoom
      if(!newWidget || loadingWidget) {
        sizes(component) = originalSize
        sizeZooms(component) = originalZoom
      }
    }
    if(!newWidget || loadingWidget)
      wrapper.setSize(zoomSize(originalSize, originalZoom, newZoom))
  }

  def zoomSize(originalSize: Dimension, oldZoom: Double, newZoom: Double) =
    new Dimension(StrictMath.ceil(originalSize.width * newZoom / oldZoom).toInt,
      StrictMath.ceil(originalSize.height * newZoom / oldZoom).toInt)

  def zoomSize(originalSize: Dimension): Dimension = zoomSize(originalSize, 1.0, zoomFactor)

  def zoomWidgetLocation(wrapper: WidgetWrapperInterface, newWidget: Boolean, loadingWidget: Boolean,
      oldZoom: Double, newZoom: Double) = {
    val component = wrapper.widget
    var originalLocation = locations.getOrElse(component, null)
    var originalZoom = locationZooms.getOrElse(component, 0: Double)
    if(originalLocation == null) {
      originalLocation = wrapper.getUnselectedLocation
      originalZoom = oldZoom
      if(!newWidget || loadingWidget) {
        locations(component) = originalLocation
        locationZooms(component) = originalZoom
      }
    }
    if(!newWidget || loadingWidget)
      wrapper.setLocation(zoomLocation(originalLocation, originalZoom, newZoom))
  }

  private def zoomLocation(originalLocation: Point, oldZoom: Double, newZoom: Double) =
    new Point(StrictMath.ceil(originalLocation.x * newZoom / oldZoom).toInt,
      StrictMath.ceil(originalLocation.y * newZoom / oldZoom).toInt)

  /**
   * it may seems redundant to take both wrapper and widget as arguments,
   * but when we are used by CommandCenter, there is no wrapper - ST 7/13/04
   */
  def zoomWidgetFont(wrapper: WidgetWrapperInterface, widget: Widget,
      newWidget: Boolean, loadingWidget: Boolean, oldZoom: Double, newZoom: Double) = {
    val recursive = widget.zoomSubcomponents
    if(!fonts.contains(widget))
      storeComponentFont(widget, recursive, newWidget, loadingWidget, oldZoom)
    scaleComponentFont(widget, newZoom, oldZoom, recursive)
    if(wrapper != null && recursive)
      wrapper.validate()
  }

  private def storeComponentFont(component: Component, recursive: Boolean,
      newWidget: Boolean, loadingWidget: Boolean, oldZoom: Double): Unit = {
    // note that we usually ignore newWidget and loadingWidget flags -- we always
    // want to remember what the font size at the 100% zoom level is, because
    // font sizes are so small that rounding error will mess us up unless we
    // always scale from the normal, unzoomed size
    if(!fonts.contains(component) &&
        (!component.isInstanceOf[ViewWidgetInterface] || !newWidget || loadingWidget)) {
      fonts(component) = component.getFont
      fontZooms(component) = oldZoom
    }
    if(recursive) component match {
      case comp: Container => comp.getComponents.foreach { x =>
        storeComponentFont(x, true, newWidget, loadingWidget, oldZoom)
      }
    }
  }

  def scaleComponentFont(component: Component,
      newZoom: Double, oldZoom: Double, recursive: Boolean): Unit = {
    if(!fonts.contains(component))
      storeComponentFont(component, recursive, false, false, oldZoom)
    val originalFont = fonts(component)
    val originalZoom = fontZooms(component)
    component.invalidate()
    component.setFont(component.getFont.deriveFont(
      StrictMath.ceil(originalFont.getSize * newZoom / originalZoom).toFloat))
    if(recursive) component match {
      case comp: Container => comp.getComponents.foreach { x =>
        scaleComponentFont(x, newZoom, oldZoom, true)
      }
    }
  }

  def forgetAllZoomInfo() = {
    sizes.clear()
    sizeZooms.clear()
    locations.clear()
    locationZooms.clear()
    // the graphics window is special since it isn't recreated
    // on every load - ST 10/31/03
    fonts.keySet.foreach { comp =>
      if(!comp.isInstanceOf[View]) {
        fonts -= comp
        fontZooms -= comp
      }
    }
  }

  def updateZoomInfo(component: Component): Unit = component.getParent match {
      case wrapper: WidgetWrapperInterface =>
        val storedSize = sizes.getOrElse(component, null)
        if(storedSize != null &&
            component.getSize != zoomSize(storedSize, sizeZooms(component), zoomFactor)) {
          sizes -= component
          sizeZooms -= component
        }
      
        val storedLocation = locations.getOrElse(component, null)
        if(storedLocation != null &&
            wrapper.getUnselectedLocation !=
              zoomLocation(storedLocation, locationZooms(component), zoomFactor)) {
          locations -= component
          locationZooms -= component
        }
        // except for View, always go from original font
        // size, since at the moment the user can't the change
        // individual font sizes of other widget types
        component match {
          case viewWidget: ViewWidget =>
            fonts -= viewWidget.view
            fontZooms -= viewWidget.view
          case _ =>
        }
      case _ =>
    }

  /// called indirectly from the save() methods of the individual Widget classes

  def getUnzoomedBounds(comp: Component) = {
    val r = comp.getBounds
    val parent = comp.getParent
    if(parent != null) {
      r.x += parent.getLocation.x
      r.y += parent.getLocation.y
    }
    val originalLocation = locations.getOrElse(comp, null)
    val unzoomedLocation =
      if(originalLocation == null)
        zoomLocation(r.getLocation, zoomFactor, 1.0)
      else
        zoomLocation(originalLocation, locationZooms(comp), 1.0)
    val originalSize = sizes.getOrElse(comp, null)
    val unzoomedSize =
      if(originalSize == null)
        zoomSize(r.getSize, zoomFactor, 1.0)
      else
        zoomSize(originalSize, sizeZooms(comp), 1.0)
    new Rectangle(unzoomedLocation, unzoomedSize)
  }

  def getFontForSave(comp: Component) = {
    val size = comp.getFont.getSize
    val originalFont = fonts.getOrElse(comp, null)
    val originalZoom = fontZooms(comp)
    if (originalFont != null)
      StrictMath.ceil(originalFont.getSize / originalZoom).toInt
    else
      StrictMath.ceil(size / zoomFactor).toInt
  }
}
