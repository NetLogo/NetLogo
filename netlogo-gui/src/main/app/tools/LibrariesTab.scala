// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.tools

import java.awt.{ BorderLayout, Color, GridLayout }
import javax.swing.{ JButton, JLabel, JList, JPanel, JScrollPane, JTextField,
  JTextArea, ListCellRenderer, ListModel }

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

  private val listModel = new FilterableListModel(list, filterFn)

  locally {
    import org.nlogo.swing.Implicits.thunk2documentListener

    implicit val i18nPrefix = I18N.Prefix("tools.libraries")

    val libraryList = new JList[LibraryInfo](listModel)
    libraryList.setCellRenderer(new CellRenderer(libraryList.getCellRenderer))

    val filterField = new JTextField

    val sidebar = new JPanel(new BorderLayout)

    val libraryButtonsPanel = new JPanel(new GridLayout(2,1, 2,2))
    val installButton  = new JButton(I18N.gui("install"))
    val homepageButton = new JButton(I18N.gui("homepage"))
    libraryButtonsPanel.add(installButton)
    libraryButtonsPanel.add(homepageButton)

    val info = new JTextArea(2, 20)
    info.setLineWrap(true)
    info.setWrapStyleWord(true)
    info.setBackground(new Color(0,0,0,0))
    info.setOpaque(false)
    info.setEditable(false)
    val infoScroll = new JScrollPane(info)
    infoScroll.getViewport.setOpaque(false)
    infoScroll.setViewportBorder(null)
    infoScroll.setBorder(null)
    val updateAllButton = new JButton(I18N.gui("updateAll"))
    sidebar.add(libraryButtonsPanel, BorderLayout.NORTH)
    sidebar.add(infoScroll, BorderLayout.CENTER)
    sidebar.add(updateAllButton, BorderLayout.SOUTH)

    add(new JScrollPane(libraryList), BorderLayout.CENTER)
    add(sidebar, BorderLayout.EAST)
    add(filterField, BorderLayout.NORTH)

    libraryList.addListSelectionListener(_ => updateSidebar(libraryList.getSelectedIndices.length))
    filterField.getDocument.addDocumentListener(() => listModel.filter(filterField.getText))
    installButton.addActionListener(_ => install(selectedValue))
    homepageButton.addActionListener(_ => BrowserLauncher.openURI(this, selectedValue.homepage.toURI))
    updateAllButton.addActionListener(_ => updateAll())

    libraryList.setSelectedIndex(0)

    def selectedValue = libraryList.getSelectedValue
    def selectedValues = {
      import scala.collection.JavaConverters._

      libraryList.getSelectedValuesList.asScala
    }
    def actionableLibraries = selectedValues.filterNot(_.status == LibraryStatus.UpToDate)

    def updateSidebar(numSelected: Int): Unit = {
      val infoText = if (numSelected == 1) selectedValue.longDescription else null
      info.setText(infoText)
      info.select(0,0)
      installButton.setText(installButtonText)

      installButton.setEnabled(actionableLibraries.length > 0)
      homepageButton.setEnabled(numSelected == 1)

      val installToolTip = if (numSelected == 1) selectedValue.downloadURL.toString else null
      installButton.setToolTipText(installToolTip)
      val homepageToolTip = if (numSelected == 1) selectedValue.homepage.toString else null
      homepageButton.setToolTipText(homepageToolTip)
    }

    def installButtonText: String =
      if (actionableLibraries.forall(_.status == LibraryStatus.CanInstall))
        I18N.gui("install")
      else if (actionableLibraries.forall(_.status == LibraryStatus.CanUpdate))
        I18N.gui("update")
      else
        I18N.gui("update") + " / " + I18N.gui("install")
  }

  private def updateAll() = {
    (0 until listModel.getSize).foreach { i =>
      val lib = listModel.getElementAt(i)
      if (lib.status == LibraryStatus.CanUpdate)
        install(lib)
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
