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
      fetch().map(ofKlass.cast _)
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

/** Components manages components which are dynamically registered with the Workspace
 *
 *  At the moment, it just holds and returns those components. Perhaps in the future
 *  we could have a more advanced component lifecycle where components could be
 *  initialized, disposed, and made aware of workspace events (like ticks)
 */
trait Components {
  private var componentPairs = List.empty[ComponentPair[_]]

  def addLifecycle[A <: AnyRef](lifecycle: ComponentLifecycle[A]): Unit =
    componentPairs :+= new ComponentPair(lifecycle.klass, Some(lifecycle), None)

  def addComponent[A <: AnyRef](componentClass: Class[A], component: A): Unit =
    componentPairs :+= new ComponentPair(componentClass, None, Some(component))

  def getComponent[A <: AnyRef](componentClass: Class[A]): Option[A] = {
    componentPairs.flatMap(_.fetchClass(componentClass)).headOption
  }

  @throws(classOf[InterruptedException])
  def disposeComponents(): Unit = {
    componentPairs.foreach(_.dispose())
    componentPairs = List.empty[ComponentPair[_]]
  }
}
