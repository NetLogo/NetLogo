// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.Dimension

// helper class for auto-resizing widgets, gets rid of icky wrapperOption that was here before (Isaac B 3/31/25)
abstract class WidgetEditPanel(target: Widget & Editable) extends EditPanel(target) {
  private val wrapper: WidgetWrapperInterface = target.getParent.asInstanceOf[WidgetWrapperInterface]

  private val originalSize: Dimension = wrapper.getSize
  private var originalPreferredSize: Dimension = wrapper.getPreferredSize

  override def apply(swapSizes: Boolean): Unit = {
    super.apply(swapSizes)

    resizeWidget(swapSizes)
  }

  override def revert(): Unit = {
    super.revert()

    wrapper.setSize(originalSize)
  }

  private def resizeWidget(swapSizes: Boolean): Unit = {
    // this is kinda kludgy because of the need to deal with the WidgetWrapperInterface rather than
    // with the widget itself, but the alternative is to make a new event just to handle this, but
    // that would be kludgy in itself, and a great deal less simple... - ST 12/22/01
    val prefSize = wrapper.getPreferredSize

    if (wrapper.isNew) {
      val currentSize = wrapper.getSize

      if (prefSize.width != currentSize.width)
        prefSize.width = wrapper.snapToGrid(prefSize.width)

      if (prefSize.height != currentSize.height)
        prefSize.height = wrapper.snapToGrid(prefSize.height)

      wrapper.setSize(prefSize)
    } else if (originalPreferredSize != prefSize) {
      var width = 10000 min (prefSize.width max (if (swapSizes) originalSize.height else originalSize.width))
      var height = 10000 min (if (wrapper.verticallyResizable)
                                prefSize.height max (if (swapSizes) originalSize.width else originalSize.height)
                              else
                                prefSize.height)
      val currentSize = wrapper.getSize

      if (width != currentSize.width)
        width = wrapper.snapToGrid(width)

      if (prefSize.height != currentSize.height)
        height = wrapper.snapToGrid(height)

      wrapper.setSize(width, height)
      originalPreferredSize = wrapper.getPreferredSize
    }

    wrapper.widgetChanged()
  }
}
