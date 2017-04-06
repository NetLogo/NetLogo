// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo(UTF8)

package org.nlogo.javafx

import javafx.event.{ Event, EventHandler }
import javafx.beans.value.{ ChangeListener, ObservableValue }

object Utils {
  def handler[T <: Event](f: T => Unit): EventHandler[T] = {
    new EventHandler[T]() {
      override def handle(event: T): Unit = {
        f(event)
      }
    }
  }

  def changeListener[T](f: (ObservableValue[_ <: T], T, T) => Unit): ChangeListener[T] = {
    new ChangeListener[T]() {
      def changed(o: ObservableValue[_ <: T], oldValue: T, newValue: T): Unit = {
        f(o, oldValue, newValue)
      }
    }
  }
}
