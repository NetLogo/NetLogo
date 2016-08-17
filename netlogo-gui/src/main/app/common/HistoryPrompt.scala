// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.common

// pulled this out of CommandLine.java so I could translate it separately to Scala - ST 8/19/10

import java.awt.{ Font, Insets }
import java.awt.event.{ ActionEvent, ActionListener, MouseAdapter, MouseEvent }
import javax.swing.{ JButton, JMenuItem, JPopupMenu, ImageIcon }

import scala.collection.JavaConverters._

import org.nlogo.core.{ AgentKind, I18N }
import org.nlogo.awt.{ Fonts, Mouse }

class HistoryPrompt(commandLine: CommandLine) extends JButton {

  locally {
    val icon = new ImageIcon(classOf[HistoryPrompt].getResource("/images/popup.gif"))
    setIcon(icon)
    setDisabledIcon(icon)
  }
  setEnabled(false)
  setOpaque(true) // needed as of quaqua 3.4.1 - ST 10/4/05
  setFont(new Font(Fonts.platformFont, Font.PLAIN, 9)) // play nice with zoomer
  addMouseListener(
    new MouseAdapter {
      override def mousePressed(e: MouseEvent) {
        if(!e.isPopupTrigger && Mouse.hasButton1(e))
          doPopupMenu()}})
  lazy val isMac = System.getProperty("os.name").startsWith("Mac")

  override def getInsets =  new Insets(0, 4, 0, 4)  // ad hoc - ST 11/24/04

  private def doPopupMenu() {
    val popMenu = new JPopupMenu(I18N.gui.get("tabs.run.commandcenter.history"))
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
      val item = new JMenuItem(str)
      item.addActionListener(
        new ActionListener {
          override def actionPerformed(e: ActionEvent) {
            commandLine.setExecutionString(ex)
            commandLine.requestFocus()}})
      popMenu.add(item)
    }
    if(commandLine.getExecutionList.isEmpty) {
      val noHistoryItem = new JMenuItem(I18N.gui.get("tabs.run.commandcenter.nohistory"))
      noHistoryItem.setEnabled(false)
      popMenu.add(noHistoryItem)
    }
    else {
      popMenu.add(new JPopupMenu.Separator)
      val hintItem = new JMenuItem(I18N.gui.get("tabs.run.commandcenter.useArrowKeys"))
      hintItem.setEnabled(false)
      popMenu.add(hintItem)
      val clearHistoryItem = new JMenuItem(I18N.gui.get("tabs.run.commandcenter.clearHistory"))
      val clearActionListener = new ActionListener {
        override def actionPerformed(e: ActionEvent) {
          commandLine.clearList() }}
      clearHistoryItem.addActionListener(clearActionListener)
      popMenu.add(new JPopupMenu.Separator)
      popMenu.add(clearHistoryItem)
    }
    popMenu.show(this, getWidth / 2, getHeight / 2)
  }

}
