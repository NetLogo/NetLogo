// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.hubnet.client

private class AppletEditor(rows: Int, columns: Int, disableFocusTraversal: Boolean)
        extends org.nlogo.editor.AbstractEditorArea
{
  if(!disableFocusTraversal)
    addFocusTraversalActions()
  private def addFocusTraversalActions() = {
    val transferFocusAction = new javax.swing.AbstractAction() {
      def actionPerformed(e: java.awt.event.ActionEvent) = transferFocus()
    }
    val transferFocusBackwardAction = new javax.swing.AbstractAction() {
      def actionPerformed(e: java.awt.event.ActionEvent) = transferFocusBackward()
    }
    getInputMap.put(
      javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_TAB, 0),
      transferFocusAction)
    getInputMap.put(
      javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_TAB,
                                         java.awt.event.InputEvent.SHIFT_MASK),
     transferFocusBackwardAction)
    getInputMap.put(
      javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ENTER, 0),
      transferFocusAction)
  }
  override def getPreferredScrollableViewportSize(): java.awt.Dimension = {
    val dimension = {
      val sup = super.getPreferredScrollableViewportSize()
      if(sup == null) new java.awt.Dimension(400, 400)
      else sup
    }
    dimension.width =
      if(columns != 0) columns * columnWidth
      else dimension.width
    dimension.height =
      if(rows != 0) rows * rowHeight
      else dimension.height
    dimension
  }
  override def getPreferredSize(): java.awt.Dimension = {
    val dimension = {
      val sup = super.getPreferredSize()
      if(sup == null) new java.awt.Dimension(400, 400)
      else sup
    }
    if(columns != 0)
      dimension.width = StrictMath.max(dimension.width,
                                       columns * columnWidth)
    if(rows != 0)
      dimension.height = StrictMath.max(dimension.height,
                                        rows * rowHeight)
    dimension
  }
  private def columnWidth = getFontMetrics(getFont).charWidth('m')
  private def rowHeight = getFontMetrics(getFont).getHeight
  override def enableBracketMatcher(enabled: Boolean) = {}
}
