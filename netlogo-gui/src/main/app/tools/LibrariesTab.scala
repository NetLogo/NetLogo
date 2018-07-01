// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.tools

import java.awt.{ BorderLayout, Color, GridLayout }
import javax.swing.{ JButton, JLabel, JList, JPanel, JScrollPane, JTextField,
  JTextArea, ListCellRenderer, ListModel }

import org.nlogo.core.I18N
import org.nlogo.swing.{ BrowserLauncher, EmptyIcon, FilterableListModel, SwingWorker }
import org.nlogo.swing.Utils.icon

object LibrariesTab {
  val itemHTMLTemplate = """<html>
    |<h3 style="margin: -10px 0">%s
    |<p color="#AAAAAA">%s""".stripMargin
}

class LibrariesTab(category: String, list: ListModel[LibraryInfo],
  install: LibraryInfo => Unit, uninstall: LibraryInfo => Unit,
  updateStatus: String => Unit, updateLists: () => Unit)
extends JPanel(new BorderLayout) {
  import LibrariesTab._

  implicit val i18nPrefix = I18N.Prefix("tools.libraries")

  private val listModel = new FilterableListModel(list, filterFn)

  locally {
    import org.nlogo.swing.Implicits.thunk2documentListener

    val libraryList = new JList[LibraryInfo](listModel)
    libraryList.setCellRenderer(new CellRenderer(libraryList.getCellRenderer))

    val filterField = new JTextField

    val sidebar = new JPanel(new BorderLayout)
    val libraryButtonsPanel = new JPanel(new GridLayout(2,1, 2,2))
    val installationPanel = new JPanel(new GridLayout(1,2, 2,2))

    val installButton = new JButton(I18N.gui("install"))
    val uninstallButton = new JButton(I18N.gui("uninstall"))
    installationPanel.add(installButton)

    val homepageButton = new JButton(I18N.gui("homepage"))
    libraryButtonsPanel.add(installationPanel)
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
    sidebar.add(libraryButtonsPanel, BorderLayout.NORTH)
    sidebar.add(infoScroll, BorderLayout.CENTER)

    add(new JScrollPane(libraryList), BorderLayout.CENTER)
    add(sidebar, BorderLayout.EAST)
    add(filterField, BorderLayout.NORTH)

    libraryList.addListSelectionListener(_ => updateSidebar())
    filterField.getDocument.addDocumentListener(() => listModel.filter(filterField.getText))
    installButton.addActionListener { _ =>
      if (numSelected == 1) {
        updateSingleOperationStatus("installing", selectedValue.name)
        new Worker("installing", install, selectedValue, multiple = false).execute()
      } else {
        numOperatedLibs = numSelected
        updateMultipleOperationStatus("installing")
        selectedValues.map(new Worker("installing", install, _, multiple = true)).foreach(_.execute)
      }
    }
    uninstallButton.addActionListener { _ =>
      if (numSelected == 1) {
        updateSingleOperationStatus("uninstalling", selectedValue.name)
        new Worker("unintalling", uninstall, selectedValue, multiple = false).execute()
      } else {
        updateMultipleOperationStatus("uninstalling")
        val forUninstall = selectedValues.filter(_.status != LibraryStatus.CanInstall)
        numOperatedLibs = forUninstall.length
        forUninstall.map(new Worker("uninstalling", uninstall, _, multiple = true)).foreach(_.execute)
      }
    }
    homepageButton.addActionListener(_ => BrowserLauncher.openURI(this, selectedValue.homepage.toURI))

    libraryList.setSelectedIndex(0)

    def numSelected = libraryList.getSelectedIndices.length
    def selectedValue = libraryList.getSelectedValue
    def selectedValues = {
      import scala.collection.JavaConverters._

      libraryList.getSelectedValuesList.asScala
    }
    def actionableLibraries = selectedValues.filterNot(_.status == LibraryStatus.UpToDate)

    def updateSidebar(): Unit = {
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

      updateInstallationPanel()
    }

    def updateInstallationPanel() = {
      installationPanel.removeAll()
      if (selectedValues.length == 0 || selectedValues.exists(_.status != LibraryStatus.UpToDate))
        installationPanel.add(installButton)
      if (selectedValues.exists(_.status != LibraryStatus.CanInstall))
        installationPanel.add(uninstallButton)
      installationPanel.revalidate()
      installationPanel.repaint()
    }

    def installButtonText: String =
      if (actionableLibraries.forall(_.status == LibraryStatus.CanInstall))
        I18N.gui("install")
      else if (actionableLibraries.forall(_.status == LibraryStatus.CanUpdate))
        I18N.gui("update")
      else
        I18N.gui("update") + " / " + I18N.gui("install")
  }

  def updateAll() = {
    val libsToUpdate =
      (0 until listModel.getSize)
        .map(listModel.getElementAt)
        .filter(_.status == LibraryStatus.CanUpdate)
    numOperatedLibs = libsToUpdate.length
    updateMultipleOperationStatus("installing")
    libsToUpdate.map(new Worker("installing", install, _, multiple = true)).foreach(_.execute)
  }

  private def filterFn(info: LibraryInfo, text: String) =
    (info.name + info.shortDescription).toLowerCase.contains(text.toLowerCase)

  private var numOperatedLibs = 0
  private def updateMultipleOperationStatus(operation: String) =
    updateStatus(I18N.gui(operation + "Multiple", Int.box(numOperatedLibs), category.toLowerCase))

  private def updateSingleOperationStatus(operation: String, libName: String) =
    updateStatus(I18N.gui(operation, libName))

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

  private class Worker(operation: String, fn: LibraryInfo => Unit, lib: LibraryInfo, multiple: Boolean) extends SwingWorker[Any, Any] {
    override def doInBackground() = fn(lib)
    override def onComplete() = {
      updateLists()
      if (multiple && numOperatedLibs > 1) {
        // This happens (gets queued) on the EDT, so there are no shared-state threading issues -- EL 2018-07-01
        numOperatedLibs -= 1
        updateMultipleOperationStatus(operation)
      } else {
        updateStatus(null)
      }
    }
  }
}
