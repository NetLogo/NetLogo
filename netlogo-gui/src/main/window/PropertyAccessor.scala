// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

case class PropertyAccessor[T](target: Editable, name: String, getter: () => T, setter: (Option[T]) => Unit,
                               changed: () => Unit = () => {})
