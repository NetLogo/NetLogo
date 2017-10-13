// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.workspace

trait ComponentLifecycle[A <: AnyRef] {
  def klass: Class[A]
  def create(): Option[A]
  def dispose(a: A): Unit
}
