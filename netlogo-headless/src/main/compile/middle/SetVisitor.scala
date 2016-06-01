// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compile
package middle

import org.nlogo.core.Instantiator
import org.nlogo.{ core, nvm, prim },
  core.Fail._,
  prim._

/**
 * an AstVisitor that handles the set command. We convert constructs like
 * "_set(var, value)" into more specific things like
 * "_setprocedurevariable(value)" or whatever, where the new set* command
 * knows internally the variable it's setting.
 */
class SetVisitor extends DefaultAstVisitor {
  private lazy val INVALID_SET =
    core.I18N.errors.get("compiler.SetVisitor.notSettable")
  override def visitStatement(stmt: Statement) {
    super.visitStatement(stmt)
    if(stmt.command.isInstanceOf[_set]) {
      val rApp = stmt.args(0).asInstanceOf[ReporterApp]
      val newCommandClass = SetVisitor.classes.get(rApp.reporter.getClass)
        .getOrElse(exception(INVALID_SET, stmt))
      val newCommand =
        Instantiator.newInstance[nvm.Command](
          newCommandClass, rApp.reporter)
      newCommand.copyMetadataFrom(stmt.command)
      stmt.command = newCommand
      stmt.removeArgument(0)
    }
  }
}

object SetVisitor {
  type ReporterClass = Class[_ <: nvm.Reporter]
  type CommandClass = Class[_ <: nvm.Command]
  // pending resolution of https://issues.scala-lang.org/browse/SI-6723
  // we avoid the `a -> b` syntax in favor of `(a, b)` - ST 1/3/13
  val classes = Map[ReporterClass, CommandClass](
    (classOf[_letvariable]          , classOf[_setletvariable]),
    (classOf[_turtleorlinkvariable] , classOf[_setturtleorlinkvariable]),
    (classOf[_patchvariable]        , classOf[_setpatchvariable]),
    (classOf[_observervariable]     , classOf[_setobservervariable]),
    (classOf[_linkbreedvariable]    , classOf[_setlinkbreedvariable]),
    (classOf[_procedurevariable]    , classOf[_setprocedurevariable]),
    (classOf[_turtlevariable]       , classOf[_setturtlevariable]),
    (classOf[_breedvariable]        , classOf[_setbreedvariable]),
    (classOf[_linkvariable]         , classOf[_setlinkvariable])
  )
}
