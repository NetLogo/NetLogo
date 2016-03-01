// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app

// pulled this out of CommandLine.java so I could translate it separately to Scala - ST 8/19/10

import org.nlogo.core.{ AgentKind, I18N }
import java.awt.Font

class HistoryPrompt(commandLine: CommandLine) extends javax.swing.JButton {

  locally {
    val icon = new javax.swing.ImageIcon(classOf[HistoryPrompt].getResource("/images/popup.gif"))
    setIcon(icon)
    setDisabledIcon(icon)
  }
  setEnabled(false)
  setOpaque(true) // needed as of quaqua 3.4.1 - ST 10/4/05
  setFont(new Font(org.nlogo.awt.Fonts.platformFont, Font.PLAIN, 9)) // play nice with zoomer
  addMouseListener(
    new java.awt.event.MouseAdapter() {
      override def mousePressed(e: java.awt.event.MouseEvent) {
        if(!e.isPopupTrigger && org.nlogo.awt.Mouse.hasButton1(e))
          doPopupMenu()}})
  // get right appearance on Mac - ST 10/4/05
  putClientProperty("Quaqua.Button.style", "square")

  lazy val isMac = System.getProperty("os.name").startsWith("Mac")

  private var usedInsets = new java.awt.Insets(0, 4, 0, 4)  // ad hoc - ST 11/24/04

  override def getInsets = usedInsets

  override def setFont(font: Font): Unit = {
    if (isMac) {
      // Zoomer sometimes resizes the font to be too small, which causes
      // quaqua to error RG 2/29/16
      if (font.getSize >= 9) {
        usedInsets = new java.awt.Insets(0, 4, 0, 4)
        super.setFont(font)
      } else {
        usedInsets = new java.awt.Insets(0, 0, 0, 0)
      }
    } else {
      super.setFont(font)
    }
  }

  override def getHeight: Int = {
    if (isMac)
      super.getHeight max 14
    else
      super.getHeight
  }

  override def getWidth: Int = {
    if (isMac)
      super.getWidth max 14
    else
      super.getWidth
  }

  override def getMinimumSize: java.awt.Dimension = {
    if (isMac) {
      val s = super.getMinimumSize()
      new java.awt.Dimension(s.getWidth.toInt max 14, s.getHeight.toInt max 14)
    } else {
      super.getMinimumSize
    }
  }

  private def doPopupMenu() {
    val popMenu = new javax.swing.JPopupMenu(I18N.gui.get("tabs.run.commandcenter.history"))
    import collection.JavaConverters._
    for(ex <- commandLine.getExecutionList) {
      val str =
        if(commandLine.agent != null)          // if we're in an agent monitor
          CommandLine.PROMPT + " " + ex.string
        else {
          val prompt = ex.agentClass match {
            case AgentKind.Observer => CommandLine.OBSERVER_PROMPT
            case AgentKind.Turtle   => CommandLine.TURTLE_PROMPT
            case AgentKind.Patch    => CommandLine.PATCH_PROMPT
            case AgentKind.Link     => CommandLine.LINK_PROMPT
          }
          prompt + " " + ex.string
        }
      val item = new javax.swing.JMenuItem(str)
      item.addActionListener(
        new java.awt.event.ActionListener() {
          override def actionPerformed(e: java.awt.event.ActionEvent) {
            commandLine.setExecutionString(ex)
            commandLine.requestFocus()}})
      popMenu.add(item)
    }
    if(commandLine.getExecutionList.isEmpty) {
      val noHistoryItem = new javax.swing.JMenuItem(I18N.gui.get("tabs.run.commandcenter.nohistory"))
      noHistoryItem.setEnabled(false)
      popMenu.add(noHistoryItem)
    }
    else {
      popMenu.add(new javax.swing.JPopupMenu.Separator)
      val hintItem = new javax.swing.JMenuItem(I18N.gui.get("tabs.run.commandcenter.useArrowKeys"))
      hintItem.setEnabled(false)
      popMenu.add(hintItem)
      val clearHistoryItem = new javax.swing.JMenuItem(I18N.gui.get("tabs.run.commandcenter.clearHistory"))
      val clearActionListener = new java.awt.event.ActionListener() {
        override def actionPerformed(e: java.awt.event.ActionEvent) {
          commandLine.clearList() }}
      clearHistoryItem.addActionListener(clearActionListener)
      popMenu.add(new javax.swing.JPopupMenu.Separator)
      popMenu.add(clearHistoryItem)
    }
    popMenu.show(this, getWidth / 2, getHeight / 2)
  }

}
