// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compiler

import org.nlogo.core.{ I18N, Instantiator }
import org.nlogo.compiler.CompilerExceptionThrowers.exception
import org.nlogo.nvm.{Command,Reporter}
import org.nlogo.prim._

/**
 * an AstVisitor that handles the set command. We convert constructs like
 * "_set(var, value)" into more specific things like
 * "_setprocedurevariable(value)" or whatever, where the new set* command
 * knows internally the variable it's setting.
 */
private class SetVisitor extends DefaultAstVisitor {
  private lazy val INVALID_SET =
    I18N.errors.get("compiler.SetVisitor.notSettable")
  override def visitStatement(stmt:Statement) {
    super.visitStatement(stmt)
    if(stmt.command.isInstanceOf[_set]) {
      val rApp = stmt.args.head.asInstanceOf[ReporterApp]
      // it's annoying Scala can't figure out that reporter.getClass is a ReporterClass.
      // lampsvn.epfl.ch/trac/scala/ticket/490 - ST 4/3/08, 8/19/08, 11/1/08
      val newCommandClass = SetVisitor.classes.get(rApp.reporter.getClass.asInstanceOf[SetVisitor.ReporterClass])
        .getOrElse(exception(INVALID_SET,stmt))
      val newCommand = Instantiator.newInstance[Command](newCommandClass,rApp.reporter)
      newCommand.copyMetadataFrom(stmt.command)
      newCommand.tokenLimitingType(rApp.coreReporter.token)
      stmt.command = newCommand
      stmt.removeArgument(0)
    }
  }
}
private object SetVisitor {
  type ReporterClass = Class[_ <: Reporter]
  type CommandClass = Class[_ <: Command]
  val classes = Map[ReporterClass,CommandClass](
         classOf[_letvariable]           -> classOf[_setletvariable],
         classOf[_turtleorlinkvariable]  -> classOf[_setturtleorlinkvariable],
         classOf[_patchvariable]         -> classOf[_setpatchvariable],
         classOf[_observervariable]      -> classOf[_setobservervariable],
         classOf[_linkbreedvariable]     -> classOf[_setlinkbreedvariable],
         classOf[_procedurevariable]     -> classOf[_setprocedurevariable],
         classOf[_turtlevariable]        -> classOf[_setturtlevariable],
         classOf[_breedvariable]         -> classOf[_setbreedvariable],
         classOf[_linkvariable]          -> classOf[_setlinkvariable]
    )
}
