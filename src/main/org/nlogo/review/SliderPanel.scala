// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.review

import org.nlogo.window.AbstractSliderWidget
import org.nlogo.mirror.ModelRun
import org.nlogo.window.Widget.LoadHelper
import java.awt.event.MouseWheelListener

class SliderPanel(
  panelBounds: java.awt.Rectangle,
  run: ModelRun,
  index: Int)
  extends AbstractSliderWidget {

  setBounds(panelBounds)
  
  def load(strings: Seq[String], helper: LoadHelper): Object = ???
  def save: String = ???

  def removeAllListeners() {
    // this needs to be called after `vertical` has been set
    getMouseListeners.foreach(removeMouseListener)
    getMouseWheelListeners.foreach(removeMouseWheelListener)
    getComponents.foreach { c =>
      c.getMouseListeners.foreach(c.removeMouseListener)
      c.getMouseWheelListeners.foreach(c.removeMouseWheelListener)
      c.getMouseMotionListeners.foreach(c.removeMouseMotionListener)
    }
  }
}