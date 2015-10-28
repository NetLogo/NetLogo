// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app

// pulled this out of CommandLine.java so I could translate it separately to Scala - ST 8/19/10

import org.nlogo.core.AgentKind
import org.nlogo.core.I18N

class HistoryPrompt(commandLine: CommandLine) extends javax.swing.JButton {

  locally {
    val icon = new javax.swing.ImageIcon(
      classOf[HistoryPrompt].getResource("/images/popup.gif"))
    setIcon(icon)
    setDisabledIcon(icon)
  }
  setEnabled(false)
  setOpaque(true) // needed as of quaqua 3.4.1 - ST 10/4/05
  addMouseListener(
    new java.awt.event.MouseAdapter() {
      override def mousePressed(e: java.awt.event.MouseEvent) {
        if(!e.isPopupTrigger && org.nlogo.awt.Mouse.hasButton1(e))
          doPopupMenu()}})
  // get right appearance on Mac - ST 10/4/05
  putClientProperty("Quaqua.Button.style", "square")

  override def getInsets = new java.awt.Insets(0, 4, 0, 4)  // ad hoc - ST 11/24/04

  private def doPopupMenu() {
    val popMenu = new javax.swing.JPopupMenu(I18N.gui.get("tabs.run.commandcenter.history"))
    import collection.JavaConverters._
    for(ex <- commandLine.getExecutionList.asScala) {
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
