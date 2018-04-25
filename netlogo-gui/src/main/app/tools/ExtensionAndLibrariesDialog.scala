// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.tools

import java.awt.{ BorderLayout, Dimension, GridLayout, Frame }
import javax.swing.{ JButton, JDialog, JLabel, JList, JPanel, JScrollPane, JTextField, ListCellRenderer, SwingConstants }
import javax.swing.event.{ ListSelectionEvent, ListSelectionListener }

import org.nlogo.core.I18N
import org.nlogo.swing.Utils.icon

class ExtensionsAndLibrariesDialog(parent: Frame, extensions: Array[ExtensionInfo])
extends JDialog(parent, I18N.gui.get("tools.extensionsAndLibraries"), false) {
  val extensionsList = new JList[ExtensionInfo](extensions)
  extensionsList.setCellRenderer(CellRenderer)

  val sidebar = new JPanel(new BorderLayout)
  val extensionButtonsPanel = new JPanel(new GridLayout(2,1, 2,2))
  extensionButtonsPanel.add(new JButton("Install"))
  extensionButtonsPanel.add(new JButton("Homepage"))
  sidebar.add(extensionButtonsPanel, BorderLayout.NORTH)
  val info = new JLabel {
      override def getMaximumSize = new Dimension(200, super.getMaximumSize.height)
      override def getPreferredSize = new Dimension(200, super.getPreferredSize.height)
  }
  sidebar.add(info, BorderLayout.CENTER)
  sidebar.add(new JButton("Update All"), BorderLayout.SOUTH)

  extensionsList.addListSelectionListener(new ListSelectionListener {
    override def valueChanged(e: ListSelectionEvent) = info.setText("<html>" + extensionsList.getSelectedValue.longDescription)
  })

  add(new JScrollPane(extensionsList), BorderLayout.CENTER)
  add(sidebar, BorderLayout.EAST)
  add(new JTextField, BorderLayout.NORTH)
  setSize(500, 300)
}

object CellRenderer extends JPanel(new BorderLayout) with ListCellRenderer[ExtensionInfo] {
  val status = new JLabel {
    setHorizontalAlignment(SwingConstants.CENTER)
    val compSize = new Dimension(32, 32)
    override def getMinimumSize   = compSize
    override def getPreferredSize = compSize
    override def getMaximumSize   = compSize
  }
  val text = new JLabel
  add(status, BorderLayout.WEST)
  add(text, BorderLayout.CENTER)
  def getListCellRendererComponent(list: JList[_ <: ExtensionInfo], value: ExtensionInfo, index: Int, isSelected: Boolean, hasFocus: Boolean) = {
    text.setText("""<html><h3 style="margin: -10px 0">""" + value.name + """<p style="margin-bottom: 10px" color="#AAAAAA">""" + value.shortDescription)
    status.setIcon(statusIcon(value.status))
    if (isSelected) {
       setBackground(list.getSelectionBackground)
       setForeground(list.getSelectionForeground)
    } else {
       setBackground(list.getBackground)
       setForeground(list.getForeground)
    }
    setEnabled(list.isEnabled)
    setFont(list.getFont)
    setOpaque(true)
    this
  }

  def statusIcon(s: ExtensionStatus) = {
    if (s == ExtensionStatus.UpToDate)
      icon("/images/check.gif")
    else
      null
  }
}
