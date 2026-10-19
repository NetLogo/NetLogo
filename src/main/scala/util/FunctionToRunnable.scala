package org.nlogo.extensions.vid.util

import javafx.beans.value.{ ChangeListener, ObservableValue }

object FunctionToCallback {
  def function2ChangeListener[T](f: (T, T) => Unit): ChangeListener[T] =
    new ChangeListener[T] {
      def changed(obs: ObservableValue[? <: T], oldVal: T, newVal: T) = f(oldVal, newVal)
    }
}
