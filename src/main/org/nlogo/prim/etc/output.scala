// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.{ api, nvm }

object OutputCommand {
  case class Options(
    withOwner: Boolean = false,
    addNewline: Boolean = false,
    readable: Boolean = false,
    destination: api.OutputDestination = api.OutputDestination.Normal)
  val typeOptions  = Options()
  val printOptions = Options(addNewline = true)
  val writeOptions = Options(readable = true)
  val showOptions  = Options(withOwner = true, addNewline = true, readable = true)
}

import OutputCommand._

abstract class OutputCommand(options: Options) extends nvm.Command {
  override def syntax =
    api.Syntax.commandSyntax(Array(api.Syntax.WildcardType))
  override def perform(context: nvm.Context) {
    workspace.outputObject(
      args(0).report(context),
      owner = if (options.withOwner) context.agent else null,
      addNewline = options.addNewline, readable = options.readable,
      destination = options.destination)
    context.ip = next
  }
}

abstract class OutputAreaCommand(options: Options)
    extends OutputCommand(options.copy(destination = api.OutputDestination.OutputArea))

class _type  extends OutputCommand(typeOptions)
class _print extends OutputCommand(printOptions)
class _write extends OutputCommand(writeOptions)
class _show  extends OutputCommand(showOptions)

class _outputtype  extends OutputAreaCommand(typeOptions)
class _outputprint extends OutputAreaCommand(printOptions)
class _outputwrite extends OutputAreaCommand(writeOptions)
class _outputshow  extends OutputAreaCommand(showOptions)
