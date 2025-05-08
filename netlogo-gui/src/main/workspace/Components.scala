// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.workspace


class ComponentPair[A <: AnyRef](
  klass:         Class[A],
  lifecycle:     Option[ComponentLifecycle[A]],
  var component: Option[A] = None) {
  def init(): Unit = {
    component = lifecycle.flatMap(_.create())
  }

  def fetch(): Option[A] = {
    component orElse {
      init()
      component
    }
  }

  def fetchClass[B](ofKlass: Class[B]): Option[B] = {
    if (klass == ofKlass)
      fetch().map(ofKlass.cast)
    else
      None
  }

  def dispose(): Unit = {
    for {
      lc   <- lifecycle
      comp <- component
    } lc.dispose(comp)

    component = None
  }
}
