// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.tools

import java.awt.{ BorderLayout, Dimension, GridLayout }
import javax.swing.{ JButton, JLabel, JList, JPanel, JScrollPane, JTextField,
  ListCellRenderer, ListModel }

import org.nlogo.core.I18N
import org.nlogo.swing.{ BrowserLauncher, EmptyIcon }
import org.nlogo.swing.Utils.icon

class LibrariesTab(list: ListModel[LibraryInfo]) extends JPanel(new BorderLayout) {
  locally {
    implicit val i18nPrefix = I18N.Prefix("tools.libraries")

    val libraryList = new JList[LibraryInfo](list)
    libraryList.setCellRenderer(new CellRenderer(libraryList.getCellRenderer))

    val filterField = new JTextField

    val sidebar = new JPanel(new BorderLayout)

    val libraryButtonsPanel = new JPanel(new GridLayout(2,1, 2,2))
    libraryButtonsPanel.add(new JButton(I18N.gui("install")))
    val homepageButton = new JButton(I18N.gui("homepage"))
    libraryButtonsPanel.add(homepageButton)

    sidebar.add(libraryButtonsPanel, BorderLayout.NORTH)
    val info = new JLabel
    info.setMaximumSize(new Dimension(200, super.getMaximumSize.height))
    info.setPreferredSize(new Dimension(200, super.getPreferredSize.height))
    sidebar.add(info, BorderLayout.CENTER)
    sidebar.add(new JButton(I18N.gui("updateAll")), BorderLayout.SOUTH)

    add(new JScrollPane(libraryList), BorderLayout.CENTER)
    add(sidebar, BorderLayout.EAST)
    add(filterField, BorderLayout.NORTH)

    libraryList.setSelectedIndex(0)
    libraryList.addListSelectionListener(_ =>
      if (!libraryList.isSelectionEmpty)
        info.setText("<html>" + libraryList.getSelectedValue.longDescription))
    homepageButton.addActionListener(_ => BrowserLauncher.openURI(this, libraryList.getSelectedValue.homepage.toURI))
  }

  private class CellRenderer(originalRenderer: ListCellRenderer[_ >: LibraryInfo]) extends ListCellRenderer[LibraryInfo] {
    private val noIcon = new EmptyIcon(32, 32)
    private val upToDateIcon = icon("/images/check.gif", 32, 32)

    override def getListCellRendererComponent(list: JList[_ <: LibraryInfo], value: LibraryInfo, index: Int, isSelected: Boolean, hasFocus: Boolean) = {
      val originalComponent = originalRenderer.getListCellRendererComponent(list, value, index, isSelected, hasFocus)
      originalComponent match {
        case label: JLabel => {
          label.setText("""<html><h3 style="margin: -10px 0">""" + value.name + """<p color="#AAAAAA">""" + value.shortDescription)
          label.setIcon(statusIcon(value.status))
          label.setIconTextGap(0)
          label
        }
        case _ => originalComponent
      }
    }

    private def statusIcon(status: LibraryStatus) = status match {
      case LibraryStatus.UpToDate => upToDateIcon
      case _ => noIcon
    }
  }
}
