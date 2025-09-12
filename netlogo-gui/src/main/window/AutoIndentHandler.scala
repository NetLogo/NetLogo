// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import org.nlogo.editor.EditorArea

trait AutoIndentHandler extends EditorArea with Events.AutoIndentEvent.Handler {
  override def handle(e: Events.AutoIndentEvent): Unit = {
    setIndenter(e.smart)
  }
}
