// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.editor

import javax.swing.{ JScrollPane, JViewport, SwingUtilities }
import javax.swing.text.{ EditorKit, JTextComponent }

import org.nlogo.swing.{ ScrollableTextComponent, UserAction }, UserAction.MenuAction

trait AbstractEditorArea extends ScrollableTextComponent {
  def configuration: EditorConfiguration

  def enableBracketMatcher(enable: Boolean): Unit

  def setIndenter(i: Indenter): Unit

  def setSelection(s: Boolean): Unit

  def selectNormal(): Unit
  def selectError(start: Int, end: Int): Unit

  def resetUndoHistory(): Unit

  def additionalMenuActions: Seq[MenuAction] = Seq()

  def undoAction: MenuAction
  def redoAction: MenuAction

  def containingViewport: Option[JViewport] = {
    SwingUtilities.getAncestorOfClass(classOf[JViewport], this) match {
      case j: JViewport => Some(j)
      case _ => None
    }
  }

  // These methods are used primarily by the input widget.
  // If we can find a way to get rid of them, we should. RG 10/27/16
  def getEditorKit(): EditorKit
  def setEditorKit(kit: EditorKit): Unit
  def getEditorKitForContentType(contentType: String): EditorKit

  override def scrollPane: Option[JScrollPane] = configuration.scrollPaneGetter()
}
