// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core
package prim.etc

abstract class OutputCommand extends Command {
  override def syntax =
    Syntax.commandSyntax(
      right = List(Syntax.WildcardType))
}

case class _type()  extends OutputCommand
case class _print() extends OutputCommand
case class _write() extends OutputCommand
case class _show()  extends OutputCommand

case class _outputtype()  extends OutputCommand
case class _outputprint() extends OutputCommand
case class _outputwrite() extends OutputCommand
case class _outputshow()  extends OutputCommand
