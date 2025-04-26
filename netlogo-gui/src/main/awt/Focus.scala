// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.awt

import java.awt.Component
import java.awt.event.{ FocusEvent, FocusListener }

object Focus {
  def addNoisyFocusListener(comp: Component): Unit = {
    comp.addFocusListener(
      new FocusListener {
        override def focusGained(fe: FocusEvent): Unit = {
          println(comp.toString + " gained focus at " + System.nanoTime)
          println("oppositeComponent = " + fe.getOppositeComponent)
        }
        override def focusLost(fe: FocusEvent): Unit = {
          println(comp.toString + " lost focus at " + System.nanoTime)
          println("oppositeComponent = " + fe.getOppositeComponent)
        }
      })
  }
}
