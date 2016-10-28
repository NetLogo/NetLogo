// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.editor

import javax.swing.event.{ PopupMenuEvent, PopupMenuListener }
import javax.swing.text.{ DefaultCaret, JTextComponent }

/** This class solves a very particular and peculiar problem.
 *  When a JTextComponent launches a popup menu, the caret continues
 *  to process mouse events as normal. This is obviously undesirable
 *  for a number of reasons, and is especially problematic on a mac,
 *  where there is only one mouse button. This class ensures that the
 *  caret (and thus the selection) are frozen at the moment the popup menu
 *  is activated and are restored when it closes. */
class SuspendCaretPopupListener(component: JTextComponent) extends PopupMenuListener {
  var suspendedListener = Option.empty[DefaultCaret]

  def popupMenuCanceled(e: PopupMenuEvent): Unit = { }

  def popupMenuWillBecomeInvisible(e: PopupMenuEvent): Unit = {
    suspendedListener.foreach(component.addMouseListener)
    suspendedListener.foreach(component.addMouseMotionListener)
    suspendedListener = None
    component.setDragEnabled(true)
  }

  def popupMenuWillBecomeVisible(e: PopupMenuEvent): Unit = {
    component.setDragEnabled(false)
    if (! suspendedListener.isDefined) {
      component.getMouseListeners.foreach {
        case caret: DefaultCaret =>
          component.removeMouseListener(caret)
          component.removeMouseMotionListener(caret)
          suspendedListener = Some(caret)
        case _ =>
      }
    }
  }
}
