// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.nvm.{ Command, Context, EditorWorkspace }

class _magicopen(_name: Option[String]) extends Command {
  switches = true

  override def perform(context: Context): Unit = {
    val openName = _name.getOrElse(argEvalString(context, 0))
    workspace.asInstanceOf[EditorWorkspace].magicOpen(openName)
    context.ip = next
  }

}
