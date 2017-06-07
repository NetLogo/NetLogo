// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo(UTF8)

package org.nlogo.javafx

import javafx.application.Platform
import javafx.event.{ Event, EventHandler }
import javafx.beans.value.{ ChangeListener, ObservableValue }
import javafx.beans.property.{ DoubleProperty, SimpleDoubleProperty }

import org.nlogo.internalapi.Monitorable

object Utils {
  def runLater(f: () => Unit): Unit = {
    Platform.runLater(new Runnable() {
      override def run(): Unit = { f() }
    })
  }
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

  def changeListener[T](f: T => Unit): ChangeListener[T] = {
    new ChangeListener[T]() {
      def changed(o: ObservableValue[_ <: T], oldValue: T, newValue: T): Unit = {
        f(newValue)
      }
    }
  }

  implicit class RichDoubleMonitorable(m: Monitorable[Double]) {
    def valueProperty: DoubleProperty = {
      val p = new SimpleDoubleProperty(m.defaultValue)
      m.onUpdate({ updated => p.setValue(updated) })
      p
    }
  }


}
