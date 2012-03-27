// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.awt

import java.awt.Component
import java.awt.event.{ FocusEvent, FocusListener }

object Focus {
  def addNoisyFocusListener(comp: Component) {
    comp.addFocusListener(
      new FocusListener {
        override def focusGained(fe: FocusEvent) {
          println(comp + " gained focus at " + System.nanoTime)
          println("oppositeComponent = " + fe.getOppositeComponent)
        }
        override def focusLost(fe: FocusEvent) {
          println(comp + " lost focus at " + System.nanoTime)
          println("oppositeComponent = " + fe.getOppositeComponent)
        }
      })
  }
}
