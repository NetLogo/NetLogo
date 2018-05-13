// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.tools

import java.awt.{ BorderLayout, Dimension, GridLayout, Frame }
import javax.swing.{ JButton, JDialog, JLabel, JList, JPanel, JScrollPane, JTextField, ListCellRenderer, ListModel }

import org.nlogo.core.I18N
import org.nlogo.swing.{ BrowserLauncher, EmptyIcon }
import org.nlogo.swing.Utils.icon

class LibrariesDialog(parent: Frame, extensions: ListModel[ExtensionInfo])
extends JDialog(parent, I18N.gui.get("tools.libraries"), false) {

  locally {
    implicit val i18nPrefix = I18N.Prefix("tools.libraries")

    val extensionsList = new JList[ExtensionInfo](extensions)
    extensionsList.setCellRenderer(new CellRenderer(extensionsList.getCellRenderer))

    val sidebar = new JPanel(new BorderLayout)

    val extensionButtonsPanel = new JPanel(new GridLayout(2,1, 2,2))
    extensionButtonsPanel.add(new JButton(I18N.gui("install")))
    val homepageButton = new JButton(I18N.gui("homepage"))
    extensionButtonsPanel.add(homepageButton)

    sidebar.add(extensionButtonsPanel, BorderLayout.NORTH)
    val info = new JLabel {
      override def getMaximumSize   = new Dimension(200, super.getMaximumSize.height)
      override def getPreferredSize = new Dimension(200, super.getPreferredSize.height)
    }
    sidebar.add(info, BorderLayout.CENTER)
    sidebar.add(new JButton(I18N.gui("updateAll")), BorderLayout.SOUTH)

    add(new JScrollPane(extensionsList), BorderLayout.CENTER)
    add(sidebar, BorderLayout.EAST)
    add(new JTextField, BorderLayout.NORTH)
    setSize(500, 300)

    extensionsList.setSelectedIndex(0)
    extensionsList.addListSelectionListener(_ => info.setText("<html>" + extensionsList.getSelectedValue.longDescription))
    homepageButton.addActionListener(_ => BrowserLauncher.openURI(this, extensionsList.getSelectedValue.homepage.toURI))
  }

  private class CellRenderer(originalRenderer: ListCellRenderer[_ >: ExtensionInfo]) extends ListCellRenderer[ExtensionInfo] {
    private val noIcon = new EmptyIcon(32, 32)
    private val upToDateIcon = icon("/images/check.gif", 32, 32)

    override def getListCellRendererComponent(list: JList[_ <: ExtensionInfo], value: ExtensionInfo, index: Int, isSelected: Boolean, hasFocus: Boolean) = {
      val originalComponent = originalRenderer.getListCellRendererComponent(list, value, index, isSelected, hasFocus)
      originalComponent match {
        case label: JLabel => {
          label.setText("""<html><h3 style="margin: -10px 0">""" + value.name + """<p style="margin-bottom: 10px" color="#AAAAAA">""" + value.shortDescription)
          label.setIcon(statusIcon(value.status))
          label.setIconTextGap(0)
          label
        }
        case _ => originalComponent
      }
    }

    private def statusIcon(status: ExtensionStatus) = status match {
      case ExtensionStatus.UpToDate => upToDateIcon
      case _ => noIcon
    }
  }
}
