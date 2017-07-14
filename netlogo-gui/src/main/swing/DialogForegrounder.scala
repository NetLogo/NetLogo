// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

import java.awt.Dialog
import java.awt.event.{ ComponentEvent, ComponentListener }

// since https://bugs.openjdk.java.net/browse/JDK-8169589 was fixed
// (we got the fix in 8u131) dialogs don't hide other active dialogs,
// which means that newly visible dialogs aren't necessarily at the front
// and are often *behind* a dialogs (assuming the dialog was created by
// another dialog).
// This class is used to pull newly created dialogs to the foreground.
object DialogForegrounder {
  def apply(dialog: Dialog): Unit = {
    dialog.addComponentListener(new DialogForegrounder(dialog))
  }
}

class DialogForegrounder(dialog: Dialog) extends ComponentListener {
  def componentHidden(e: ComponentEvent): Unit = {}
  def componentMoved(e: ComponentEvent): Unit = {}
  def componentResized(e: ComponentEvent): Unit = {}
  def componentShown(e: ComponentEvent): Unit = { dialog.toFront() }
}
