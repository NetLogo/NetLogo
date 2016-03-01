// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.core.{ AgentKind, Reference, Referenceable }
import org.nlogo.nvm.{ Context, Reporter }

class _observervariable(_vn: Int) extends Reporter with Referenceable {

  override def toString =
    super.toString + ":" +
      Option(world).map(_.observerOwnsNameAt(vn)).getOrElse(vn.toString)

  // MethodRipper won't let us call a public method from report_1()
  // so we must keep vn and _vn separate - ST 9/22/12
  def vn = _vn

  def makeReference =
    new Reference(AgentKind.Observer, _vn, this)

  override def report(context: Context): AnyRef =
    world.observer.getVariable(_vn)

  def report_1(context: Context): AnyRef =
    world.observer.getVariable(_vn)

}
