// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.tools

import java.awt.{ Component, Dimension, EventQueue, Font }
import java.awt.event.{ ActionEvent, KeyEvent }
import java.io.IOException
import java.nio.file.Path
import javax.swing.{ AbstractAction, DefaultListModel, Icon, JLabel, JList, ListCellRenderer }
import javax.swing.border.LineBorder
import javax.swing.event.{ ListDataEvent, ListDataListener }

import org.nlogo.api.{ LibraryInfoDownloader, LibraryManager, Version }
import org.nlogo.core.{ I18N, LibraryInfo, LibraryStatus }
import org.nlogo.swing.{ AutomationUtils, BoxColumn, BoxRow, BrowserLauncher, Button, EmptyIcon, FilterableListModel,
                         FocusUtils, HorizontalStrut, Implicits, MaximumHeight, OptionPane, ScrollPane, SwingWorker,
                         TextArea, TextField, Utils, VerticalStrut, Zoomable, ZoomableBorder },
                       Implicits.thunk2documentListener
import org.nlogo.theme.{ InterfaceColors, ThemeSync }
import org.nlogo.workspace.ModelsLibrary

import scala.collection.mutable.Buffer

abstract class LibrariesTab(manager: LibraryManager, getLibraries: => Seq[LibraryInfo], updateStatus: String => Unit,
                            recompile: () => Unit, updateSource: (String => String) => Unit,
                            extPathMappings: Map[String, Path])
  extends BoxColumn(6) with ThemeSync {

  private implicit val i18nPrefix: I18N.Prefix = I18N.Prefix("tools.libraries")

  protected var actionIsInProgress = false

  setBorder(new ZoomableBorder(10, 0, 0, 0))

  private val baseListModel = new DefaultListModel[LibraryInfo]

  private val listModel   = new FilterableListModel(baseListModel, containsLib) {
    addListDataListener(new ListDataListener {
      override def intervalAdded(e: ListDataEvent): Unit = {
        if (canUpdateInRange(e.getIndex0, e.getIndex1))
          updateAllAction.setEnabled(LibraryInfoDownloader.enabled && true)
      }

      override def intervalRemoved(e: ListDataEvent): Unit = {
        updateAllAction.setEnabled(LibraryInfoDownloader.enabled && canUpdate)
      }

      override def contentsChanged(e: ListDataEvent): Unit = {
        updateAllAction.setEnabled(LibraryInfoDownloader.enabled && canUpdate)
      }
    })
  }

  private val libraryList = new JList[LibraryInfo](listModel) with FocusUtils with Zoomable with ThemeSync {
    setCellRenderer(new CellRenderer)
    setSelectedIndex(0)

    addListSelectionListener(_ => updateSidebar())

    override def syncTheme(): Unit = {
      setBackground(InterfaceColors.dialogBackground())
      setFocusColor(InterfaceColors.focus())
    }
  }

  val updateAllAction: AbstractAction = new AbstractAction(I18N.gui("updateAll")) {
    setEnabled(LibraryInfoDownloader.enabled && canUpdate)

    override def actionPerformed(e: ActionEvent): Unit = {
      setEnabled(false)

      val libsToUpdate =
        (0 until listModel.getSize).
          map(listModel.getElementAt).
          filter(_.status == LibraryStatus.CanUpdate)

      numOperatedLibs = libsToUpdate.length
      updateMultipleOperationStatus("installing")

      runAllWorkersAndThen("installing", install, libsToUpdate, multiple = true)(() => finishManagement())
    }
  }

  private val magIcon = new JLabel with Zoomable {
    setIcon(Utils.iconScaledWithColor(this, "/images/find.png", 15, 15, () => InterfaceColors.toolbarImage()))
  }

  private val filterField = new TextField with MaximumHeight

  private val libraryScroll = new ScrollPane(libraryList)

  private val installButton = new Button(I18N.gui("install"), () => {
    def installCheck(lib: LibraryInfo): Boolean =
      lib.isVersionRequirementMet(Version.version) && lib.status != LibraryStatus.UpToDate

    def uninstallCheck(lib: LibraryInfo): Boolean =
      installCheck(lib) && lib.canUninstall

    perform("uninstalling", uninstall, uninstallCheck, true)
    perform("installing", wrappedInstall, installCheck, false)
  }) {
    override def getMaximumSize: Dimension =
      new Dimension(Int.MaxValue, super.getMaximumSize.height)
  }

  private val addToCodeTabButton = new Button(I18N.gui("addToCodeTab"), () => {
    updateSource { source =>
      addLibrariesToSource(source, selectedValues.map(_.codeName).toSet) match {
        case Some(newSource) =>
          newSource

        case _ =>
          new OptionPane(this, I18N.gui.get("common.messages.error"), I18N.gui.get("tools.libraries.failedToAdd"),
                         OptionPane.Options.Ok, OptionPane.Icons.error)

          source
      }
    }

    recompile()
  })

  private val homepageButton = new Button(I18N.gui("homepage"), () => {
    BrowserLauncher.openURI(this, selectedValue.homepage.toURI)
  })

  private val uninstallButton = new Button(I18N.gui("uninstall"), () => {
    perform("uninstalling", uninstall, _.canUninstall, false)
  }) {
    override def getMaximumSize: Dimension =
      new Dimension(Int.MaxValue, super.getMaximumSize.height)
  }

  private val buttonStrut = new HorizontalStrut(6)

  private val info = new TextArea(2, 28) {
    setFocusable(false)
    setLineWrap(true)
    setWrapStyleWord(true)
    setEditable(false)
  }

  private val infoScroll = new ScrollPane(info)

  private val installedVersionLabel  = new JLabel(s"${I18N.gui("installedVersion")}: ") with Zoomable {
    setBaseFont(getFont.deriveFont(Font.BOLD))
  }

  private val latestVersionLabel  = new JLabel(s"${I18N.gui("latestVersion")}: ") with Zoomable {
    setBaseFont(getFont.deriveFont(Font.BOLD))
  }

  private val minNetLogoVersionLabel = new JLabel(s"${I18N.gui("minimumVersion")}: ") with Zoomable {
    setBaseFont(getFont.deriveFont(Font.BOLD))
  }

  private val installedVersion = new JLabel with Zoomable
  private val latestVersion = new JLabel with Zoomable
  private val minNetLogoVersion = new JLabel with Zoomable

  private val nlvPanel = new BoxRow(Seq(minNetLogoVersionLabel, minNetLogoVersion))

  EventQueue.invokeLater(() => getLibraries.foreach(baseListModel.addElement))

  manager.onLibInfoChange { () =>
    EventQueue.invokeLater(() => {
      baseListModel.clear()
      getLibraries.foreach(baseListModel.addElement)
      updateSidebar()
    })
  }

  filterField.getDocument.addDocumentListener(() => listModel.filter(filterField.getText))

  locally {
    val secondRow = new BoxRow(Seq(
      addToCodeTabButton,
      homepageButton
    ), 6) with MaximumHeight

    add(new BoxRow(Seq(magIcon, filterField), 6) with MaximumHeight)
    add(new BoxRow(Seq(
      libraryScroll,
      new BoxColumn(Seq(
        new BoxColumn(Seq(
          new BoxRow(Seq(
            installButton,
            buttonStrut,
            uninstallButton,
          )) {
            override def getMaximumSize: Dimension =
              new Dimension(secondRow.getPreferredSize.width, getPreferredSize.height)
          },
          secondRow
        ), 6),
        new VerticalStrut(6),
        new BoxRow(Seq(installedVersionLabel, installedVersion)),
        new BoxRow(Seq(latestVersionLabel, latestVersion)),
        nlvPanel,
        new VerticalStrut(6),
        infoScroll
      ))
    ), 6))

    updateSidebar()
  }

  protected def statusName: String

  def addLibrariesToSource(source: String, requiredLibs: Set[String]): Option[String]

  private def install(info: LibraryInfo): Unit = {
    if (info.isExtension) {
      manager.installExtension(info)
    } else {
      manager.installPackage(info)
    }
  }

  private def uninstall(info: LibraryInfo): Unit = {
    if (info.isExtension) {
      manager.uninstallExtension(info)
    } else {
      manager.uninstallPackage(info)
    }
  }

  private def canUpdate: Boolean =
    canUpdateInRange(0, listModel.getSize - 1)

  private def canUpdateInRange(index0: Int, index1: Int): Boolean =
    (index0 to index1).map(listModel.getElementAt).exists(_.status == LibraryStatus.CanUpdate)

  private def actionableLibraries: Buffer[LibraryInfo] = {
    selectedValues.filterNot { lib =>
      !lib.isVersionRequirementMet(Version.version) || lib.status == LibraryStatus.UpToDate
    }
  }

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

      installButton.setVisible(!actionIsInProgress && !selectedValues.forall(_.status == LibraryStatus.UpToDate))
      uninstallButton.setVisible(!actionIsInProgress && selectedValues.exists(_.status != LibraryStatus.CanInstall))
      buttonStrut.setVisible(installButton.isVisible && uninstallButton.isVisible)

    } else {
      Seq(installButton, uninstallButton, addToCodeTabButton, homepageButton).foreach(_.setEnabled(false))

      installButton.setVisible(true)
      uninstallButton.setVisible(false)
      buttonStrut.setVisible(false)

      info.setText("")
      info.select(0, 0)
    }

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
    import scala.jdk.CollectionConverters.ListHasAsScala
    libraryList.getSelectedValuesList.asScala
  }

  private def wrappedInstall(lib: LibraryInfo) =
    try {
      install(lib)
    } catch {
      case ex: IOException =>
        new OptionPane(this, I18N.gui.get("common.messages.error"), I18N.gui("downloadFailed", lib.downloadURL),
                       OptionPane.Options.Ok, OptionPane.Icons.error)
    }

  private def containsLib(info: LibraryInfo, text: String): Boolean =
    s"${info.name}${info.shortDescription}".toLowerCase.contains(text.toLowerCase)

  // the `silent` parameter prevents recompilation during a multi-step action such as uninstalling
  // and then reinstalling an extension (Isaac B 9/2/25)
  private def perform(opName: String, fn: LibraryInfo => Unit, checkIsTarget: LibraryInfo => Boolean,
                      silent: Boolean) = {
    if (numSelected == 1) {
      updateSingleOperationStatus(opName, selectedValue.name)
      actionIsInProgress = true
      new Worker(opName, fn, selectedValue, multiple = false, () => {
        actionIsInProgress = false

        if (!silent)
          finishManagement()
      }).execute()
    } else {
      val libs = selectedValues.filter(checkIsTarget).toSeq
      numOperatedLibs = libs.length
      updateMultipleOperationStatus(opName)
      runAllWorkersAndThen(opName, fn, libs, multiple = true)(() => {
        if (!silent)
          finishManagement()
      })
    }
  }

  private var numOperatedLibs = 0

  private def updateMultipleOperationStatus(operation: String) =
    updateStatus(I18N.gui(operation + "Multiple", Int.box(numOperatedLibs), statusName))

  private def updateSingleOperationStatus(operation: String, libName: String) =
    updateStatus(I18N.gui(operation, libName))

  private class CellRenderer extends BoxRow(6) with ListCellRenderer[LibraryInfo] {
    private val upToDateIcon: Icon = Utils.iconScaledWithColor(this, "/images/check.png", 24, 24,
                                                               () => InterfaceColors.checkFilled())
    private val warningIcon: Icon = Utils.iconScaledWithColor(this, "/images/exclamation-triangle.png", 24, 24,
                                                              () => InterfaceColors.warningIcon())
    private val canUpdateIcon: Icon = Utils.iconScaledWithColor(this, "/images/update.png", 24, 24,
                                                                () => InterfaceColors.updateIcon())
    private val noIcon: Icon = new EmptyIcon(this, 24, 24)

    private val iconLabel = new JLabel
    private val descLabel = new JLabel with Zoomable

    private val nameLabel = new JLabel with Zoomable {
      setBaseFont(getFont.deriveFont(14.0f).deriveFont(Font.BOLD))
    }

    setOpaque(true)
    setBorder(new ZoomableBorder(6, 6, 6, 6))

    add(iconLabel)
    add(new BoxColumn(Seq(nameLabel, descLabel), 6))

    override def getListCellRendererComponent(list: JList[? <: LibraryInfo], value: LibraryInfo, index: Int,
                                              isSelected: Boolean, hasFocus: Boolean): Component = {

      iconLabel.setIcon(statusIcon(value.status, value.codeName))
      nameLabel.setText(value.name)
      descLabel.setText(value.shortDescription)

      if (isSelected) {
        setBackground(InterfaceColors.dialogBackgroundSelected())

        nameLabel.setForeground(InterfaceColors.dialogTextSelected())
        descLabel.setForeground(InterfaceColors.dialogTextSelected())
      } else {
        setBackground(InterfaceColors.dialogBackground())

        nameLabel.setForeground(InterfaceColors.dialogText())
        descLabel.setForeground(InterfaceColors.dialogText())
      }

      this
    }

    private def statusIcon(status: LibraryStatus, extName: String): Icon =
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

    private val indices = libraryList.getSelectedIndices

    updateSidebar()

    override def doInBackground() = fn(lib)
    override def onComplete() = {
      manager.reloadMetadata()
      if (multiple && numOperatedLibs > 1) {
        // This happens (gets queued) on the EDT, so there are no shared-state threading issues -- EL 2018-07-01
        numOperatedLibs -= 1
        updateMultipleOperationStatus(operation)
      } else {
        updateStatus(null)
      }
      callback()
      EventQueue.invokeLater(() => libraryList.setSelectedIndices(indices))
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
    filterField.syncTheme()

    libraryScroll.setBackground(InterfaceColors.dialogBackground())

    libraryList.syncTheme()

    installButton.syncTheme()
    addToCodeTabButton.syncTheme()
    homepageButton.syncTheme()
    uninstallButton.syncTheme()

    installedVersionLabel.setForeground(InterfaceColors.dialogText())
    latestVersionLabel.setForeground(InterfaceColors.dialogText())
    minNetLogoVersionLabel.setForeground(InterfaceColors.dialogText())

    installedVersion.setForeground(InterfaceColors.dialogText())
    latestVersion.setForeground(InterfaceColors.dialogText())
    minNetLogoVersion.setForeground(InterfaceColors.dialogText())

    infoScroll.setBorder(new LineBorder(InterfaceColors.textAreaBorderNoneditable()))
    infoScroll.setBackground(InterfaceColors.textAreaBackground())

    info.syncTheme()
  }

  private [app] def searchFor(text: String, expectedSize: Int): Option[Seq[LibraryInfo]] = {
    filterField.requestFocus()

    // make sure all focus-related events are processed (Isaac B 11/6/25)
    if (!AutomationUtils.waitUntil(() => filterField.hasFocus))
      return None

    val queue: EventQueue = getToolkit.getSystemEventQueue

    text.foreach { char =>
      queue.postEvent(new KeyEvent(filterField, KeyEvent.KEY_TYPED, System.currentTimeMillis, 0,
                                   KeyEvent.VK_UNDEFINED, char))
    }

    // wait for the list to update extension visibilities (Isaac B 11/2/25)
    if (AutomationUtils.waitUntil(() => listModel.getSize == expectedSize)) {
      Option((0 until listModel.getSize).map(listModel.getElementAt))
    } else {
      None
    }
  }

  private [app] def testInstall(info: LibraryInfo): Unit = {
    install(info)

    // wait for any resulting events to be processed (Isaac B 11/2/25)
    EventQueue.invokeAndWait(() => {})

    uninstall(info)

    // wait for any resulting events to be processed (Isaac B 11/2/25)
    EventQueue.invokeAndWait(() => {})
  }
}
