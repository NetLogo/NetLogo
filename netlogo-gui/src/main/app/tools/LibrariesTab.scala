// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.tools

import java.awt.{ BorderLayout, Component, Dimension, FlowLayout, GridBagConstraints, GridBagLayout, GridLayout,
                  Insets }
import java.awt.font.TextAttribute
import java.io.IOException
import java.nio.file.Path
import javax.swing.{ Action, Box, DefaultListModel, JLabel, JList, JPanel, ListCellRenderer, ListModel }
import javax.swing.border.LineBorder
import javax.swing.event.{ AncestorEvent, AncestorListener, ListDataEvent, ListDataListener }

import java.util.Collections

import scala.collection.mutable.Buffer

import org.nlogo.api.{ LibraryInfoDownloader, LibraryManager, Version }
import org.nlogo.awt.EventQueue
import org.nlogo.core.{ I18N, LibraryInfo, LibraryStatus }
import org.nlogo.swing.{ BrowserLauncher, Button, EmptyIcon, FilterableListModel, OptionPane, RichAction, ScrollPane,
                         SwingWorker, TextArea, TextField, Transparent, Utils }
import org.nlogo.theme.{ InterfaceColors, ThemeSync }
import org.nlogo.workspace.ModelsLibrary

object LibrariesTab {
  val itemHTMLTemplate =
    """<html>
      |<h3 style="margin: -10px 0">%s
      |<p color="#AAAAAA">%s""".stripMargin

  def addExtsToSource(source: String, requiredExts: Set[String]): String = {

    // We have to be careful here.  I'd love to do clever things, but the extensions
    // directive can be multiline and have comments in it.  --JAB (3/6/19)
    val ExtRegex = """(?s)(?i)(^|.*\n)(\s*extensions\s*(?:;?.*?\n)?\[)(.*?\].*)""".r

    val newExtsBasis = requiredExts.toSeq.sorted.mkString(" ")

    source match {
      case ExtRegex(prefix, extDirective, suffix) =>
        val newExtsStr = if (newExtsBasis.length > 0) s"$newExtsBasis " else ""
        s"$prefix$extDirective$newExtsStr$suffix"
      case _ =>
        s"extensions [$newExtsBasis]\n$source"
    }

  }

}

class LibrariesTab( category:        String
                  , manager:         LibraryManager
                  , updateStatus:    String => Unit
                  , recompile:       () => Unit
                  , updateSource:    ((String) => String) => Unit
                  , extPathMappings: Map[String, Path]
                  ) extends JPanel(new BorderLayout) with ThemeSync {

  import LibrariesTab._

  private val libraries       = manager.getExtensionInfos
  private val install         = manager.installExtension _
  private val uninstall       = manager.uninstallExtension _
  private val updateLists     = () => manager.reloadMetadata()

  implicit val i18nPrefix = I18N.Prefix("tools.libraries")

  private val baseListModel = new DefaultListModel[LibraryInfo]

  EventQueue.invokeLater { () => libraries.toArray.foreach(elem => baseListModel.addElement(elem)) }

  manager.onLibInfoChange {
    libs =>
      EventQueue.invokeLater {
        () =>
          baseListModel.clear()
          libs.foreach(elem => baseListModel.addElement(elem))
          updateSidebar()
      }
  }

  private val listModel   = new FilterableListModel(baseListModel, containsLib)
  private val libraryList = new JList[LibraryInfo](listModel)

  private var actionIsInProgress = false

  val updateAllAction: Action =
    RichAction(I18N.gui("updateAll")) { _ =>

      updateAllAction.setEnabled(false)

      val libsToUpdate =
        (0 until listModel.getSize).
          map(listModel.getElementAt).
          filter(_.status == LibraryStatus.CanUpdate)

      numOperatedLibs = libsToUpdate.length
      updateMultipleOperationStatus("installing")

      runAllWorkersAndThen("installing", install, libsToUpdate, multiple = true)(() => finishManagement())

    }

  private val topPanel = new JPanel(new GridBagLayout)
  private val magIcon = new JLabel
  private val filterField = new TextField

  private val libraryScroll = new ScrollPane(libraryList)

  private val sidebar             = Box.createVerticalBox()
  private val libraryButtonsPanel = new JPanel(new GridLayout(3, 1, 2, 2)) with Transparent
  private val installationPanel   = new JPanel(new GridLayout(1, 2, 2, 2)) with Transparent

  private val installButton = new Button(I18N.gui("install"), () => {
    val installCheck = (lib: LibraryInfo) =>
      lib.isVersionRequirementMet(Version.version) && lib.status != LibraryStatus.UpToDate
    val uninstallCheck = (lib: LibraryInfo) => installCheck(lib) && lib.canUninstall
    perform("uninstalling", uninstall, uninstallCheck)
    perform("installing", wrappedInstall, installCheck)
  })

  private val addToCodeTabButton = new Button(I18N.gui("addToCodeTab"), () => {
    updateSource(addExtsToSource(_, selectedValues.map(_.codeName).toSet))
    recompile()
  })

  private val homepageButton = new Button(I18N.gui("homepage"), () => {
    BrowserLauncher.openURI(LibrariesTab.this, selectedValue.homepage.toURI)
  })

  private val uninstallButton = new Button(I18N.gui("uninstall"), () => {
    perform("uninstalling", uninstall, _.canUninstall)
  })

  private val info = new TextArea(2, 28)
  private val infoScroll = new ScrollPane(info)

  private val installedVersionLabel  = new JLabel(s"${I18N.gui("installedVersion")}: ")
  private val latestVersionLabel  = new JLabel(s"${I18N.gui("latestVersion")}: ")
  private val minNetLogoVersionLabel = new JLabel(s"${I18N.gui("minimumVersion")}: ")

  private val installedVersion = new JLabel
  private val latestVersion = new JLabel
  private val minNetLogoVersion = new JLabel

  private val nlvPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0)) with Transparent

  locally {

    import org.nlogo.swing.Implicits.thunk2documentListener

    def embolden(l: JLabel) =
      l.setFont(l.getFont.deriveFont(Collections.singletonMap(TextAttribute.WEIGHT, TextAttribute.WEIGHT_BOLD)))

    libraryList.setCellRenderer(new CellRenderer(libraryList.getCellRenderer))

    installationPanel.add(installButton)

    libraryButtonsPanel.add(installationPanel)
    libraryButtonsPanel.add(addToCodeTabButton)
    libraryButtonsPanel.add(homepageButton)

    embolden(installedVersionLabel)
    embolden(latestVersionLabel)
    embolden(minNetLogoVersionLabel)

    val ivPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0)) with Transparent

    ivPanel.add(installedVersionLabel)
    ivPanel.add(installedVersion)
    ivPanel.setMaximumSize(new Dimension(Short.MaxValue, 20))

    val lvPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0)) with Transparent

    lvPanel.add(latestVersionLabel)
    lvPanel.add(latestVersion)
    lvPanel.setMaximumSize(new Dimension(Short.MaxValue, 20))

    nlvPanel.add(minNetLogoVersionLabel)
    nlvPanel.add(minNetLogoVersion)
    nlvPanel.setMaximumSize(new Dimension(Short.MaxValue, 20))
    nlvPanel.setVisible(true)

    libraryButtonsPanel.setMaximumSize(new Dimension(Short.MaxValue, 20))

    info.setLineWrap(true)
    info.setWrapStyleWord(true)
    info.setEditable(false)

    libraryButtonsPanel.setAlignmentX(Component.LEFT_ALIGNMENT)
    ivPanel.setAlignmentX(Component.LEFT_ALIGNMENT)
    lvPanel.setAlignmentX(Component.LEFT_ALIGNMENT)
    nlvPanel.setAlignmentX(Component.LEFT_ALIGNMENT)
    infoScroll.setAlignmentX(Component.LEFT_ALIGNMENT)

    val d = new Dimension(5, 5)
    sidebar.add(libraryButtonsPanel)
    sidebar.add(new Box.Filler(d, d, d))
    sidebar.add(ivPanel)
    sidebar.add(lvPanel)
    sidebar.add(nlvPanel)
    sidebar.add(new Box.Filler(d, d, d))
    sidebar.add(infoScroll)

    val c = new GridBagConstraints

    c.insets = new Insets(6, 6, 6, 6)

    topPanel.add(magIcon, c)

    c.insets = new Insets(6, 0, 6, 6)
    c.fill = GridBagConstraints.HORIZONTAL
    c.weightx = 1

    topPanel.add(filterField, c)

    add(libraryScroll, BorderLayout.CENTER)
    add(sidebar, BorderLayout.EAST)
    add(topPanel, BorderLayout.NORTH)

    listModel.addListDataListener(
      new ListDataListener {

        override def intervalAdded(e: ListDataEvent): Unit =
          if (canUpdateInRange(listModel, e.getIndex0, e.getIndex1))
            updateAllAction.setEnabled(LibraryInfoDownloader.enabled && true)

        override def intervalRemoved(e: ListDataEvent): Unit =
          updateAllAction.setEnabled(LibraryInfoDownloader.enabled && canUpdate(listModel))

        override def contentsChanged(e: ListDataEvent): Unit =
          updateAllAction.setEnabled(LibraryInfoDownloader.enabled && canUpdate(listModel))

      }
    )

    libraryList.addListSelectionListener(_ => updateSidebar())
    libraryList.setSelectedIndex(0)

    filterField.getDocument.addDocumentListener(() => listModel.filter(filterField.getText))

    updateAllAction.setEnabled(LibraryInfoDownloader.enabled && canUpdate(listModel))

    updateSidebar()

    def canUpdate(model: ListModel[LibraryInfo]) = canUpdateInRange(model, 0, model.getSize - 1)
    def canUpdateInRange(model: ListModel[LibraryInfo], index0: Int, index1: Int) =
      (index0 to index1)
        .map(model.getElementAt)
        .exists(_.status == LibraryStatus.CanUpdate)

  }

  this.addAncestorListener(new AncestorListener {
    override def ancestorAdded  (e: AncestorEvent): Unit = {}
    override def ancestorMoved  (e: AncestorEvent): Unit = {}
    override def ancestorRemoved(e: AncestorEvent): Unit = {
      libraryList.setSelectedIndex(0)
      filterField.setText("")
    }
  })

  private def actionableLibraries = selectedValues.filterNot((lib) => !lib.isVersionRequirementMet(Version.version) || lib.status == LibraryStatus.UpToDate)

  private def updateSidebar(): Unit = {

    if (selectedValue != null) { // It's `null` when the download fails --JAB (3/6/19)

      installedVersion .setText(selectedValue.installedVersionOpt.getOrElse("N/A"))
      latestVersion    .setText(selectedValue.version)
      minNetLogoVersion.setText(selectedValue.minNetLogoVersion.getOrElse(""))

      nlvPanel.setVisible(numSelected == 1 && !selectedValue.minNetLogoVersion.isEmpty)

      val infoText = if (numSelected != 1) {
        null
      } else {
        if (selectedValue.isVersionRequirementMet(Version.version)) {
          selectedValue.longDescription
        } else {
          s"${I18N.gui("unmetMinimumVersion", selectedValue.name, selectedValue.minNetLogoVersion.get)}\n\n${selectedValue.longDescription}"
        }
      }
      info.setText(infoText)
      info.select(0, 0)

      addToCodeTabButton.setEnabled(selectedValues.forall(_.status != LibraryStatus.CanInstall))

      installButton.setText(installButtonText)
      installButton.setEnabled(LibraryInfoDownloader.enabled && actionableLibraries.length > 0)

      uninstallButton.setEnabled(LibraryInfoDownloader.enabled && selectedValues.filter(_.status != LibraryStatus.CanInstall).exists(!_.bundled))
      homepageButton.setEnabled(numSelected == 1)

      val installToolTip = if (numSelected == 1) selectedValue.downloadURL.toString else null
      installButton.setToolTipText(installToolTip)

      val homepageToolTip = if (numSelected == 1) selectedValue.homepage.toString else null
      homepageButton.setToolTipText(homepageToolTip)

      updateInstallationPanel()

    } else {
      Seq(installButton, uninstallButton, addToCodeTabButton, homepageButton).foreach(_.setEnabled(false))
      info.setText("")
      info.select(0, 0)
    }

  }

  private def updateInstallationPanel() = {

    installationPanel.removeAll()
    if (!actionIsInProgress) {
      if (selectedValues.length == 0 || selectedValues.exists(_.status != LibraryStatus.UpToDate))
        installationPanel.add(installButton)
      if (selectedValues.exists(_.status != LibraryStatus.CanInstall))
        installationPanel.add(uninstallButton)
    }

    installationPanel.revalidate()
    installationPanel.repaint()

  }

  private def installButtonText: String =
    if (actionableLibraries.forall(_.status == LibraryStatus.CanInstall)) {
      I18N.gui("install")
    } else if (actionableLibraries.forall(_.status == LibraryStatus.CanUpdate)) {
      I18N.gui("update")
    } else {
      I18N.gui("update") + " / " + I18N.gui("install")
    }

  private def finishManagement(): Unit = {
    updateSidebar()
    recompile()
    ModelsLibrary.rootNode = None
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
        new OptionPane(this, I18N.gui.get("common.messages.error"), I18N.gui("downloadFailed", lib.downloadURL),
                       OptionPane.Options.Ok, OptionPane.Icons.Error)
    }

  private def containsLib(info: LibraryInfo, text: String): Boolean =
    s"${info.name}${info.shortDescription}".toLowerCase.contains(text.toLowerCase)

  private def perform(opName: String, fn: LibraryInfo => Unit, checkIsTarget: LibraryInfo => Boolean) = {
    if (numSelected == 1) {
      updateSingleOperationStatus(opName, selectedValue.name)
      actionIsInProgress = true
      new Worker(opName, fn, selectedValue, multiple = false, { () => actionIsInProgress = false; finishManagement() }).execute()
    } else {
      val libs = selectedValues.filter(checkIsTarget)
      numOperatedLibs = libs.length
      updateMultipleOperationStatus(opName)
      runAllWorkersAndThen(opName, fn, libs, multiple = true)(() => finishManagement())
    }
  }

  private var numOperatedLibs = 0
  private def updateMultipleOperationStatus(operation: String) =
    updateStatus(I18N.gui(operation + "Multiple", Int.box(numOperatedLibs), category.toLowerCase))

  private def updateSingleOperationStatus(operation: String, libName: String) =
    updateStatus(I18N.gui(operation, libName))

  private class CellRenderer(originalRenderer: ListCellRenderer[_ >: LibraryInfo]) extends ListCellRenderer[LibraryInfo] {

    private val noIcon        = new EmptyIcon(32, 32)
    private val upToDateIcon  = Utils.iconScaledWithColor("/images/check.png", 20, 20, InterfaceColors.checkFilled)
    private val warningIcon   = Utils.iconScaledWithColor("/images/exclamation-triangle.png", 20, 20,
                                                          InterfaceColors.warningIcon)
    private val canUpdateIcon = Utils.iconScaledWithColor("/images/update.png", 20, 20, InterfaceColors.updateIcon)

    override def getListCellRendererComponent(list: JList[_ <: LibraryInfo], value: LibraryInfo, index: Int, isSelected: Boolean, hasFocus: Boolean) = {
      val originalComponent = originalRenderer.getListCellRendererComponent(list, value, index, isSelected, hasFocus)
      val newComponent =
        originalComponent match {
          case label: JLabel =>
            label.setText(itemHTMLTemplate.format(value.name, value.shortDescription))
            label.setIcon(statusIcon(value.status, value.codeName))
            label.setIconTextGap(6)
            label
          case _ => originalComponent
        }

      if (isSelected) {
        newComponent.setBackground(InterfaceColors.dialogBackgroundSelected)
        newComponent.setForeground(InterfaceColors.dialogTextSelected)
      } else {
        newComponent.setBackground(InterfaceColors.dialogBackground)
        newComponent.setForeground(InterfaceColors.dialogText)
      }

      newComponent
    }

    private def statusIcon(status: LibraryStatus, extName: String) =
      if (!extPathMappings.contains(extName)) {
        status match {
          case LibraryStatus.UpToDate   => upToDateIcon
          case LibraryStatus.CanUpdate  => canUpdateIcon
          case LibraryStatus.CanInstall => noIcon
        }
      } else {
        warningIcon
      }
  }

  private class Worker( operation: String, fn: LibraryInfo => Unit
                      , lib: LibraryInfo, multiple: Boolean
                      , callback: () => Unit = () => ()) extends SwingWorker[Any, Any] {

    updateSidebar()

    override def doInBackground() = fn(lib)
    override def onComplete() = {
      val indices = libraryList.getSelectedIndices
      updateLists()
      if (multiple && numOperatedLibs > 1) {
        // This happens (gets queued) on the EDT, so there are no shared-state threading issues -- EL 2018-07-01
        numOperatedLibs -= 1
        updateMultipleOperationStatus(operation)
      } else {
        updateStatus(null)
      }
      libraryList.setSelectedIndices(indices)
      callback()
    }

  }

  // Intended as JavaScript's `Promise.all` --JAB (3/2/19)
  private def runAllWorkersAndThen(operation: String, task: LibraryInfo => Unit, libs: Seq[LibraryInfo], multiple: Boolean)
                                  (callback: () => Unit): Unit = {

    var numRemaining = numOperatedLibs
    val cb = {
      () =>
        numRemaining -= 1
        if (numRemaining == 0) {
          actionIsInProgress = false
          callback()
        }
    }

    actionIsInProgress = true
    libs.map(new Worker(operation, task, _, multiple = true, cb)).foreach(_.execute)

  }

  override def syncTheme(): Unit = {
    setBackground(InterfaceColors.dialogBackground)

    topPanel.setBackground(InterfaceColors.dialogBackground)

    magIcon.setIcon(Utils.iconScaledWithColor("/images/find.png", 15, 15, InterfaceColors.toolbarImage))

    filterField.syncTheme()

    libraryScroll.setBackground(InterfaceColors.dialogBackground)
    libraryList.setBackground(InterfaceColors.dialogBackground)

    installButton.syncTheme()
    addToCodeTabButton.syncTheme()
    homepageButton.syncTheme()
    uninstallButton.syncTheme()

    installedVersionLabel.setForeground(InterfaceColors.dialogText)
    latestVersionLabel.setForeground(InterfaceColors.dialogText)
    minNetLogoVersionLabel.setForeground(InterfaceColors.dialogText)

    installedVersion.setForeground(InterfaceColors.dialogText)
    latestVersion.setForeground(InterfaceColors.dialogText)
    minNetLogoVersion.setForeground(InterfaceColors.dialogText)

    infoScroll.setBorder(new LineBorder(InterfaceColors.textAreaBorderNoneditable))
    infoScroll.setBackground(InterfaceColors.textAreaBackground)

    info.syncTheme()
  }
}
