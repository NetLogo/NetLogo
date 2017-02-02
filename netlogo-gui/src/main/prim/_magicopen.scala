// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.nvm.{ Command, Context, EditorWorkspace }
import org.nlogo.api.LogoException
import org.nlogo.core.Syntax

class _magicopen(_name: Option[String]) extends Command {
  private[this] val name = _name

  switches = true

  override def perform(context: Context): Unit = {
    val openName = name.getOrElse(argEvalString(context, 0))
    workspace.asInstanceOf[EditorWorkspace].magicOpen(openName)
    context.ip = next
  }

}
