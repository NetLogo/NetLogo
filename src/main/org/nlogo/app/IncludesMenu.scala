// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app

class IncludesMenu(target: CodeTab)
extends org.nlogo.swing.ToolBarMenu("Includes")
with org.nlogo.window.Events.CompiledEvent.Handler
{

  // If we're empty, we have no size, are invisible and don't affect our parent's layout
  private var isEmpty = true

  private var includesTable: Map[String, String] = null

  def handle(e: org.nlogo.window.Events.CompiledEvent) {
    if(e.sourceOwner.isInstanceOf[CodeTab])
      updateVisibility()
  }

  def updateVisibility() {
    isEmpty = target.getIncludesTable.isEmpty
    revalidate()
    super.doLayout()
  }

  override def populate(menu: javax.swing.JPopupMenu) {
    import collection.JavaConverters._
    target.getIncludesTable match {
      case Some(includesTableJ) =>
        includesTable = includesTableJ.asScala.toMap
        val includes = new java.util.ArrayList[String]
        includesTable.keys.foreach(includes.add)
        java.util.Collections.sort(includes, String.CASE_INSENSITIVE_ORDER)
        for(include <- includes.asScala if include.endsWith(".nls") && include.size > 4)
          if(new java.io.File(includesTable(include)).exists) {
            val item = new javax.swing.JMenuItem(include)
            item.addActionListener(
              new java.awt.event.ActionListener() {
                override def actionPerformed(e: java.awt.event.ActionEvent) {
                  menuSelection(include)
                }})
            menu.add(item)
          }
        menu.addSeparator()
      case None =>
        val nullItem = new javax.swing.JMenuItem("<No Includes Defined>")
        nullItem.setEnabled(false)
        menu.add(nullItem)
    }
    menu.add(new javax.swing.JMenuItem(new NewSourceEditorAction))
    menu.add(new javax.swing.JMenuItem(new OpenSourceEditorAction))
  }

  override def getMinimumSize =
    if (isEmpty) new java.awt.Dimension(0, 0)
    else super.getMinimumSize

  override def getPreferredSize =
    if (isEmpty) new java.awt.Dimension(0, 0)
    else super.getPreferredSize

  protected def menuSelection(s: String) {
    App.app.tabs.openTemporaryFile(includesTable(s), true)
  }

  private class OpenSourceEditorAction
  extends javax.swing.AbstractAction("Open Source File...")
  {
    override def actionPerformed(e: java.awt.event.ActionEvent) {
      try {
        val path = org.nlogo.swing.FileDialog.show(
            IncludesMenu.this, "Open: NetLogo Source File",
            java.awt.FileDialog.LOAD, null)
          .replace(java.io.File.separatorChar, '/')
        if(path.endsWith(".nls"))
          App.app.tabs.openTemporaryFile(path, false)
        else
          javax.swing.JOptionPane.showMessageDialog(target, "Filename must end in *.nls")
      }
      catch {
        case ex: org.nlogo.awt.UserCancelException =>
          org.nlogo.util.Exceptions.ignore(ex)
      }
    }
  }

  private class NewSourceEditorAction
  extends javax.swing.AbstractAction("New Source File") {
    override def actionPerformed(e: java.awt.event.ActionEvent) {
      App.app.tabs.newTemporaryFile()
    }
  }

}
