// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.tools

import java.awt.{ BorderLayout, Dimension, GridLayout }
import javax.swing.{ JButton, JLabel, JList, JPanel, JScrollPane, JTextField,
  ListCellRenderer, ListModel }

import org.nlogo.core.I18N
import org.nlogo.swing.{ BrowserLauncher, EmptyIcon, FilterableListModel }
import org.nlogo.swing.Utils.icon

object LibrariesTab {
  val itemHTMLTemplate = """<html>
    |<h3 style="margin: -10px 0">%s
    |<p color="#AAAAAA">%s""".stripMargin
}

class LibrariesTab(list: ListModel[LibraryInfo], install: LibraryInfo => Unit)
extends JPanel(new BorderLayout) {
  import LibrariesTab._

  locally {
    import org.nlogo.swing.Implicits.thunk2documentListener

    implicit val i18nPrefix = I18N.Prefix("tools.libraries")

    val listModel = new FilterableListModel(list, filterFn)
    val libraryList = new JList[LibraryInfo](listModel)
    libraryList.setCellRenderer(new CellRenderer(libraryList.getCellRenderer))

    val filterField = new JTextField

    val sidebar = new JPanel(new BorderLayout)

    val libraryButtonsPanel = new JPanel(new GridLayout(2,1, 2,2))
    val installButton  = new JButton(I18N.gui("install"))
    val homepageButton = new JButton(I18N.gui("homepage"))
    libraryButtonsPanel.add(installButton)
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

    libraryList.addListSelectionListener(_ => updateSidebar(libraryList.getSelectedIndices.length))
    filterField.getDocument.addDocumentListener(() => listModel.filter(filterField.getText))
    installButton.addActionListener(_ => install(selectedValue))
    homepageButton.addActionListener(_ => BrowserLauncher.openURI(this, selectedValue.homepage.toURI))

    libraryList.setSelectedIndex(0)

    def selectedValue = libraryList.getSelectedValue

    def updateSidebar(numSelected: Int): Unit = {
      val infoText = if (numSelected == 1) "<html>" + selectedValue.longDescription else null
      info.setText(infoText)
      installButton.setEnabled(numSelected > 0)
      homepageButton.setEnabled(numSelected == 1)
      val installToolTip = if (numSelected == 1) selectedValue.downloadURL.toString else null
      installButton.setToolTipText(installToolTip)
      val homepageToolTip = if (numSelected == 1) selectedValue.homepage.toString else null
      homepageButton.setToolTipText(homepageToolTip)
    }
  }

  private def filterFn(info: LibraryInfo, text: String) =
    (info.name + info.shortDescription).toLowerCase.contains(text.toLowerCase)

  private class CellRenderer(originalRenderer: ListCellRenderer[_ >: LibraryInfo]) extends ListCellRenderer[LibraryInfo] {
    private val noIcon = new EmptyIcon(32, 32)
    private val upToDateIcon = icon("/images/check.gif", 32, 32)
    private val canUpdateIcon = icon("/images/update.gif", 32, 32)

    override def getListCellRendererComponent(list: JList[_ <: LibraryInfo], value: LibraryInfo, index: Int, isSelected: Boolean, hasFocus: Boolean) = {
      val originalComponent = originalRenderer.getListCellRendererComponent(list, value, index, isSelected, hasFocus)
      originalComponent match {
        case label: JLabel => {
          label.setText(itemHTMLTemplate.format(value.name, value.shortDescription))
          label.setIcon(statusIcon(value.status))
          label.setIconTextGap(0)
          label
        }
        case _ => originalComponent
      }
    }

    private def statusIcon(status: LibraryStatus) = status match {
      case LibraryStatus.UpToDate   => upToDateIcon
      case LibraryStatus.CanUpdate  => canUpdateIcon
      case LibraryStatus.CanInstall => noIcon
    }
  }
}
