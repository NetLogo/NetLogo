// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.tools

import java.awt.{ BorderLayout, Color, GridLayout }
import java.io.IOException
import javax.swing.{ Action, BorderFactory, DefaultListModel, JButton, JLabel, JList, JOptionPane,
  JPanel, JScrollPane, JTextField, JTextArea, ListCellRenderer, ListModel }
import javax.swing.event.{ ListDataEvent, ListDataListener }

import scala.collection.mutable.Buffer

import org.nlogo.api.LibraryManager
import org.nlogo.core.{ I18N, LibraryInfo, LibraryStatus }
import org.nlogo.swing.{ BrowserLauncher, EmptyIcon, FilterableListModel, RichAction, SwingWorker }
import org.nlogo.swing.Utils.icon

object LibrariesTab {
  val itemHTMLTemplate =
    """<html>
      |<h3 style="margin: -10px 0">%s
      |<p color="#AAAAAA">%s""".stripMargin
}

class LibrariesTab(category: String, manager: LibraryManager, updateStatus: String => Unit)
extends JPanel(new BorderLayout) {

  import LibrariesTab._

  private val libraries   = manager.getExtensionInfos
  private val install     = manager.installExtension _
  private val uninstall   = manager.uninstallExtension _
  private val updateLists = () => manager.reloadMetadata()

  implicit val i18nPrefix = I18N.Prefix("tools.libraries")

  private val baseListModel = new DefaultListModel[LibraryInfo]
  libraries.toArray.foreach(elem => baseListModel.addElement(elem))

  manager.onLibInfoChange {
    libs =>
      baseListModel.clear()
      libs.foreach(elem => baseListModel.addElement(elem))
  }

  private val listModel   = new FilterableListModel(baseListModel, containsLib)
  private val libraryList = new JList[LibraryInfo](listModel)

  val updateAllAction: Action =
    RichAction(I18N.gui("updateAll")) { _ =>
      val libsToUpdate =
        (0 until listModel.getSize).
          map(listModel.getElementAt).
          filter(_.status == LibraryStatus.CanUpdate)
      numOperatedLibs = libsToUpdate.length
      updateMultipleOperationStatus("installing")
      libsToUpdate.map(new Worker("installing", install, _, multiple = true)).foreach(_.execute)
    }

  locally {

    import org.nlogo.swing.Implicits.thunk2documentListener

    libraryList.setCellRenderer(new CellRenderer(libraryList.getCellRenderer))

    val filterField = new JTextField

    val sidebar             = new JPanel(new BorderLayout)
    val libraryButtonsPanel = new JPanel(new GridLayout(2,1, 2,2))
    val installationPanel   = new JPanel(new GridLayout(1,2, 2,2))

    val installButton  = new JButton(I18N.gui("install"))
    val homepageButton = new JButton(I18N.gui("homepage"))

    installationPanel.add(installButton)
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
    infoScroll.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5))

    sidebar.add(libraryButtonsPanel, BorderLayout.NORTH)
    sidebar.add(         infoScroll, BorderLayout.CENTER)

    val magIcon  = new JLabel(icon("/images/magnify.gif", 20, 32))
    val topPanel = new JPanel(new BorderLayout)
    topPanel.add(filterField, BorderLayout.CENTER)
    topPanel.add(    magIcon, BorderLayout.WEST)

    add(new JScrollPane(libraryList), BorderLayout.CENTER)
    add(                     sidebar, BorderLayout.EAST)
    add(                    topPanel, BorderLayout.NORTH)

    val uninstallButton = new JButton(I18N.gui("uninstall"))
    uninstallButton.addActionListener(_ => perform("uninstalling", uninstall,
      lib => lib.status != LibraryStatus.CanInstall && !lib.bundled))

    listModel.addListDataListener(
      new ListDataListener {

        override def intervalAdded(e: ListDataEvent): Unit =
          if (canUpdateInRange(listModel, e.getIndex0, e.getIndex1))
            updateAllAction.setEnabled(true)

        override def intervalRemoved(e: ListDataEvent): Unit = updateAllAction.setEnabled(canUpdate(listModel))
        override def contentsChanged(e: ListDataEvent): Unit = updateAllAction.setEnabled(canUpdate(listModel))

      }
    )

    libraryList.addListSelectionListener(_ => updateSidebar())
    libraryList.setSelectedIndex(0)

    filterField.getDocument.addDocumentListener(() => listModel.filter(filterField.getText))

    installButton.addActionListener(_ => perform("installing", wrappedInstall,
      lib => lib.status != LibraryStatus.UpToDate))

    homepageButton.addActionListener(_ => BrowserLauncher.openURI(this, selectedValue.homepage.toURI))

    updateAllAction.setEnabled(canUpdate(listModel))

    def actionableLibraries = selectedValues.filterNot(_.status == LibraryStatus.UpToDate)

    def canUpdate(model: ListModel[LibraryInfo]) = canUpdateInRange(model, 0, model.getSize - 1)
    def canUpdateInRange(model: ListModel[LibraryInfo], index0: Int, index1: Int) =
      (index0 to index1)
        .map(model.getElementAt)
        .exists(_.status == LibraryStatus.CanUpdate)

    def updateSidebar(): Unit = {

      val infoText = if (numSelected == 1) selectedValue.longDescription else null
      info.setText(infoText)
      info.select(0,0)

      installButton.setText(installButtonText)
      installButton.setEnabled(actionableLibraries.length > 0)

      uninstallButton.setEnabled(selectedValues.filter(_.status != LibraryStatus.CanInstall).exists(!_.bundled))
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

  private def numSelected:   Int         = libraryList.getSelectedIndices.length
  private def selectedValue: LibraryInfo = libraryList.getSelectedValue

  private def selectedValues: Buffer[LibraryInfo] = {
    import scala.collection.JavaConverters._
    libraryList.getSelectedValuesList.asScala
  }

  private def wrappedInstall(lib: LibraryInfo) =
    try {
      install(lib)
    } catch {
      case ex: IOException =>
        JOptionPane.showMessageDialog(
          this
        , I18N.gui("downloadFailed", lib.downloadURL)
        , I18N.gui.get("common.messages.error")
        , JOptionPane.ERROR_MESSAGE
        )
    }

  private def containsLib(info: LibraryInfo, text: String): Boolean =
    s"${info.name}${info.shortDescription}".toLowerCase.contains(text.toLowerCase)

  private def perform(opName: String, fn: LibraryInfo => Unit, checkIsTarget: LibraryInfo => Boolean) = {
    if (numSelected == 1) {
      updateSingleOperationStatus(opName, selectedValue.name)
      new Worker(opName, fn, selectedValue, multiple = false).execute()
    } else {
      val libs = selectedValues.filter(checkIsTarget)
      numOperatedLibs = libs.length
      updateMultipleOperationStatus(opName)
      libs.map(new Worker(opName, fn, _, multiple = true)).foreach(_.execute)
    }
  }

  private var numOperatedLibs = 0
  private def updateMultipleOperationStatus(operation: String) =
    updateStatus(I18N.gui(operation + "Multiple", Int.box(numOperatedLibs), category.toLowerCase))

  private def updateSingleOperationStatus(operation: String, libName: String) =
    updateStatus(I18N.gui(operation, libName))

  private class CellRenderer(originalRenderer: ListCellRenderer[_ >: LibraryInfo]) extends ListCellRenderer[LibraryInfo] {

    private val noIcon        = new EmptyIcon(32, 32)
    private val upToDateIcon  = icon("/images/check.gif", 32, 32)
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
