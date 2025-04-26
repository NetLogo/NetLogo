// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compile.optimize

import org.nlogo.core.Dialect
import org.nlogo.prim._
import org.nlogo.compile.api.{ RewritingReporterMunger, Match }

class DialectPatchVariableDouble(dialect: Dialect) extends RewritingReporterMunger {
  val clazz = classOf[_patchvariable]
  def munge(root: Match): Unit = {
    val vn = root.reporter.asInstanceOf[_patchvariable].vn
    if(org.nlogo.api.AgentVariables.isDoublePatchVariable(vn, dialect.is3D)) {
      root.replace(classOf[_patchvariabledouble])
      root.reporter.asInstanceOf[_patchvariabledouble].vn = vn
    }
  }
}
class DialectTurtleVariableDouble(dialect: Dialect) extends RewritingReporterMunger {
  val clazz = classOf[_turtlevariable]
  def munge(root: Match): Unit = {
    val vn = root.reporter.asInstanceOf[_turtlevariable].vn
    if(org.nlogo.api.AgentVariables.isDoubleTurtleVariable(vn, dialect.is3D)) {
      root.replace(classOf[_turtlevariabledouble])
      root.reporter.asInstanceOf[_turtlevariabledouble].vn = vn
    }
  }
}
