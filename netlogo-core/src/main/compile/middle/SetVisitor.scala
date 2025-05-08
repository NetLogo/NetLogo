// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compile
package middle

import org.nlogo.core.Instantiator
import org.nlogo.{ core, nvm, prim },
  core.{ Program, Fail},
    Fail._,
  prim._
import org.nlogo.compile.api.{ DefaultAstVisitor, ReporterApp, Statement }

/**
 * an AstVisitor that handles the set command. We convert constructs like
 * "_set(var, value)" into more specific things like
 * "_setprocedurevariable(value)" or whatever, where the new set* command
 * knows internally the variable it's setting.
 */
class SetVisitor(program: Program) extends DefaultAstVisitor {
  private lazy val INVALID_SET =
    core.I18N.errors.get("compiler.SetVisitor.notSettable")
  override def visitStatement(stmt: Statement): Unit = {
    super.visitStatement(stmt)
    if(stmt.command.isInstanceOf[_set]) {
      val rApp = stmt.args.head.asInstanceOf[ReporterApp]
      val cmdTransformer = (SetVisitor.classes ++ programClasses)
        .get(rApp.reporter.getClass)
        .getOrElse(exception(INVALID_SET, stmt))
      val newCommand = cmdTransformer(rApp.reporter)
      newCommand.copyMetadataFrom(stmt.command)
      stmt.command = newCommand
      stmt.removeArgument(0)
    }
  }

  val programClasses: Map[SetVisitor.ReporterClass, SetVisitor.CommandTransformer] =
    Map(classOf[_turtleorlinkvariable] -> turtleOrLinkVariable)
  private def turtleOrLinkVariable(rep: nvm.Reporter): nvm.Command = {
    rep match {
      case v: _turtleorlinkvariable =>
        val vnTurtle = program.turtlesOwn.indexOf(v.varName)
        val vnLink   = program.linksOwn.indexOf(v.varName)
        new _setturtleorlinkvariable(v.varName, vnTurtle, vnLink)
      case _                        => exception(INVALID_SET, rep.token)
    }
  }
}

object SetVisitor {
  type ReporterClass = Class[? <: nvm.Reporter]
  type CommandClass = Class[? <: nvm.Command]
  type CommandTransformer = nvm.Reporter => nvm.Command

  def transformer(klass: CommandClass): CommandTransformer = {
    (rep: nvm.Reporter) =>
      Instantiator.newInstance[nvm.Command](klass, rep)
  }

  // pending resolution of https://issues.scala-lang.org/browse/SI-6723
  // we avoid the `a -> b` syntax in favor of `(a, b)` - ST 1/3/13
  val classes = Map[ReporterClass, CommandTransformer](
    (classOf[_letvariable]          , transformer(classOf[_setletvariable])),
    (classOf[_patchvariable]        , transformer(classOf[_setpatchvariable])),
    (classOf[_observervariable]     , transformer(classOf[_setobservervariable])),
    (classOf[_linkbreedvariable]    , transformer(classOf[_setlinkbreedvariable])),
    (classOf[_procedurevariable]    , transformer(classOf[_setprocedurevariable])),
    (classOf[_turtlevariable]       , transformer(classOf[_setturtlevariable])),
    (classOf[_breedvariable]        , transformer(classOf[_setbreedvariable])),
    (classOf[_linkvariable]         , transformer(classOf[_setlinkvariable]))
  )

}
