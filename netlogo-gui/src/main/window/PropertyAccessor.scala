// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

case class PropertyAccessor[T](target: Editable, name: String, getter: () => T, setter: (T) => Unit,
                               changed: () => Unit = () => {})
