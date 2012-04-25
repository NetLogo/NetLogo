package org.nlogo.prim.etc

import org.nlogo.api.{Syntax, Version}
import Syntax.{RepeatableType, StringType}
import scala.collection.JavaConversions.iterableAsScalaIterable
import org.nlogo.nvm.{Context, Command}

class _logevent extends Command {

  override def syntax = Syntax.commandSyntax(Array(StringType, RepeatableType | StringType), 1, "O---", null, false)

  override def perform(context: Context) {

    if(Version.isLoggingEnabled) {
      val world = workspace.world()
      val evaledStrs = (0 until args.length) map (argEvalString(context, _))
      val (msg, globalFilters) = { val (msgList, filters) = evaledStrs splitAt 1; (msgList.head, filters) }
      val globalsRegex = globalFilters map ("(%s)".format(_)) mkString("(?i)", "|", "")
      val desiredGlobals = world.program.globals zip (world.observer.variables map (_.toString)) filter (_._1 matches globalsRegex)
      org.nlogo.log.Logger.logCustomMessage(msg, desiredGlobals.toSeq: _*)
    }

    context.ip = next

  }

}
