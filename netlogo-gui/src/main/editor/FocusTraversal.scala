// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.editor

import java.awt.Component
import java.awt.event.{ ActionEvent, FocusEvent, FocusListener, MouseEvent, MouseListener }
import javax.swing.{ AbstractAction, JPopupMenu }
import javax.swing.text.JTextComponent

trait FocusTraversable extends JTextComponent {
  def selectionActive: Boolean
}

// To understand the behavior of this class, edit the code in a plot pen
// and use tab to switch back and forth between the various code fields.
// The expected behavior is that when focus leaves the text
// component, any selection in that component is removed.
// Additionally, when the text component *gains* focus, all
// text is highlighted.
class FocusTraversalListener(textComponent: FocusTraversable)
  extends FocusListener
  with MouseListener {
  private var mouseEvent = false

  def focusGained(fe: FocusEvent): Unit = {
    if (!mouseEvent && textComponent.selectionActive) {
      // this is like selectAll(), but it leaves the
      // caret at the beginning rather than the start;
      // this prevents the enclosing scrollpane from
      // scrolling to the end to make the caret
      // visible; it's nicer to keep the scroll at the
      // start - ST 12/20/04
      textComponent.setCaretPosition(textComponent.getText().length)
      textComponent.moveCaretPosition(0)
    }
  }

  def focusLost(fe: FocusEvent): Unit = {
    // On Windows (and perhaps Linux? not sure), putting
    // the focus elsewhere leaves the text selected in the
    // now-unfocused field.  This causes the text to be drawn
    // in different colors even though the selection isn't
    // visible.  I suppose we could make HighlightView smarter
    // about that, but instead let's just force the Mac-like
    // behavior and be done with it for now - ST 11/3/03
    if (!fe.isTemporary) {
      textComponent.select(0, 0)
    }
    mouseEvent = fe.isTemporary
  }

  def mousePressed(me: MouseEvent) = {
    mouseEvent = true
  }
  def mouseClicked(me: MouseEvent) = { }
  def mouseReleased(me: MouseEvent) = { }
  def mouseEntered(me: MouseEvent) = { }
  def mouseExited(me: MouseEvent) = { }
}


class TransferFocusAction extends AbstractAction {
  def actionPerformed(e: ActionEvent): Unit = {
    e.getSource match {
      case c: Component => c.transferFocus()
      case _ =>
    }
  }
}

class TransferFocusBackwardAction extends AbstractAction {
  def actionPerformed(e: ActionEvent): Unit = {
    e.getSource match {
      case c: Component => c.transferFocusBackward()
      case _ =>
    }
  }
}
