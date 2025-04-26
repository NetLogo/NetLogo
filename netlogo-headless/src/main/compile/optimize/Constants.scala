// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compile
package optimize

import org.nlogo.prim._const
import org.nlogo.compile.api.{ Match, RewritingReporterMunger }

object Constants extends RewritingReporterMunger {
  val clazz = classOf[_const]
  def munge(root: Match): Unit = {
    root.replace(Literals.makeLiteralReporter(root.reporter.asInstanceOf[_const].value))
  }
}
