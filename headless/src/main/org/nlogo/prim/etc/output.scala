// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.api.{ Syntax, OutputDestination }
import org.nlogo.nvm.{ Command, Context }

class _print extends Command {
  override def syntax =
    Syntax.commandSyntax(Array(Syntax.WildcardType))
  override def perform(context: Context) {
    workspace.outputObject(
      args(0).report(context), null, true, false,
      OutputDestination.Normal)
    context.ip = next
  }
}

class _show extends Command {
  override def syntax =
    Syntax.commandSyntax(Array(Syntax.WildcardType))
  override def perform(context: Context) {
    workspace.outputObject(
      args(0).report(context), context.agent,
      true, true, OutputDestination.Normal)
    context.ip = next
  }
}

class _type extends Command {
  override def syntax =
    Syntax.commandSyntax(Array(Syntax.WildcardType))
  override def perform(context: Context) {
    workspace.outputObject(
      args(0).report(context), null, false, false,
      OutputDestination.Normal)
    context.ip = next
  }
}

class _write extends Command {
  override def syntax =
    Syntax.commandSyntax(Array(Syntax.ReadableType))
  override def perform(context: Context) {
    workspace.outputObject(
      args(0).report(context), null, false, true,
      OutputDestination.Normal)
    context.ip = next
  }
}

class _outputprint extends Command {
  override def syntax =
    Syntax.commandSyntax(Array(Syntax.WildcardType))
  override def perform(context: Context) {
    workspace.outputObject(
      args(0).report(context), null, true, false,
      OutputDestination.OutputArea)
    context.ip = next
  }
}

class _outputshow extends Command {
  override def syntax =
    Syntax.commandSyntax(Array(Syntax.WildcardType))
  override def perform(context: Context) {
    workspace.outputObject(
      args(0).report(context), context.agent,
      true, true, OutputDestination.OutputArea)
    context.ip = next
  }
}

class _outputtype extends Command {
  override def syntax =
    Syntax.commandSyntax(Array(Syntax.WildcardType))
  override def perform(context: Context) {
    workspace.outputObject(
      args(0).report(context), null, false, false,
      OutputDestination.OutputArea)
    context.ip = next
  }
}

class _outputwrite extends Command {
  override def syntax =
    Syntax.commandSyntax(Array(Syntax.ReadableType))
  override def perform(context: Context) {
    workspace.outputObject(
      args(0).report(context), null, false, true,
      OutputDestination.OutputArea)
    context.ip = next
  }
}
