// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.tools

import java.awt.{ Component, EventQueue, Font, Toolkit }
import java.awt.event.KeyEvent
import java.io.IOException
import java.nio.file.Path
import java.util.Locale
import javax.swing.{ Action, DefaultListModel, Icon, JLabel, JList, ListCellRenderer, ListModel }
import javax.swing.border.LineBorder
import javax.swing.event.{ AncestorEvent, AncestorListener, ListDataEvent, ListDataListener }

import org.nlogo.api.{ LibraryInfoDownloader, LibraryManager, Version }
import org.nlogo.core.{ I18N, LibraryInfo, LibraryStatus, Token, TokenType }
import org.nlogo.swing.{ AutomationUtils, BoxColumn, BoxRow, BrowserLauncher, Button, FilterableListModel,
                         HorizontalStrut, MaximumHeight, OptionPane, RichAction, ScrollPane, SwingWorker, TextArea,
                         TextField, Utils, VerticalStrut, Zoomable, ZoomableBorder }
import org.nlogo.theme.{ InterfaceColors, ThemeSync }
import org.nlogo.workspace.ModelsLibrary

import scala.collection.mutable.Buffer

object LibrariesTab {
  // this may look overly complex, but when rewriting the user's source code, we need to be absolutely sure
  // that we're doing the right thing, otherwise we might break things. the following code tokenizes the source,
  // preserving all whitespace and comments. it then tries to find an existing valid extensions directive. if it
  // can't find an extensions directive regardless of its validity, it adds a new extensions directive to the top
  // of the source. if it finds an extensions directive that is in valid, it rejects the operation. if it finds a
  // valid extensions directive, it adds any new extensions from the specified list to that directive.
  // (Isaac B 7/25/25)
  def addExtsToSource(source: String, requiredExts: Set[String],
                      tokenizeSource: String => Iterator[Token]): Option[String] = {
    val (prefix, rest) = tokenizeSource(source).span(token => token.tpe != TokenType.Keyword ||
                                                              token.text.toUpperCase(Locale.US) != "EXTENSIONS")

    if (rest.nextOption.isDefined) {
      val (beforeList, list) = rest.span(token => token.tpe == TokenType.Comment ||
                                                  token.tpe == TokenType.Whitespace)

      if (list.nextOption.exists(_.tpe == TokenType.OpenBracket)) {
        val (contents, suffix) = list.span(token => token.tpe == TokenType.Ident ||
                                                    token.tpe == TokenType.Comment ||
                                                    token.tpe == TokenType.Whitespace)

        if (suffix.nextOption.exists(_.tpe == TokenType.CloseBracket)) {
          val existing = contents.toSeq
          val extNames = requiredExts -- existing.filter(_.tpe == TokenType.Ident)
                                                 .map(_.text.toLowerCase(Locale.US)).toSet

          val newExts = {
            if (extNames.isEmpty) {
              existing.map(_.text).mkString
            } else {
              extNames.toSeq.sorted.mkString(" ", " ", " ") + existing.map(_.text).mkString.stripLeading
            }
          }

          Some(prefix.map(_.text).mkString + "extensions" + beforeList.map(_.text).mkString + "[" + newExts + "]" +
               suffix.map(_.text).mkString)
        } else {
          None
        }
      } else {
        None
      }
    } else {
      Some(s"extensions [ ${requiredExts.toSeq.sorted.mkString(" ")} ]\n$source")
    }
  }
}

// this tab can be converted back to a `JTabbedPane` once other libraries are added, like code modules or models.
// -JeremyB April 2019
class LibrariesTab( category:        String
                  , manager:         LibraryManager
                  , updateStatus:    String => Unit
                  , recompile:       () => Unit
                  , tokenizeSource:  String => Iterator[Token]
                  , updateSource:    ((String) => String) => Unit
                  , extPathMappings: Map[String, Path]
                  ) extends BoxColumn(6) with ThemeSync {

  import LibrariesTab._

  private val libraries = manager.getExtensionInfos ++ manager.getPackageInfos

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

  private val updateLists = () => manager.reloadMetadata()

  implicit val i18nPrefix: org.nlogo.core.I18N.Prefix = I18N.Prefix("tools.libraries")

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

  private val renderer = new CellRenderer

  private val listModel   = new FilterableListModel(baseListModel, containsLib)
  private val libraryList = new JList[LibraryInfo](listModel) with Zoomable {
    setCellRenderer(renderer)
  }

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

  private val magIcon = new JLabel {
    setIcon(Utils.iconScaledWithColor("/images/find.png", 15, 15, () => InterfaceColors.toolbarImage()))
  }

  private val filterField = new TextField with MaximumHeight

  private val libraryScroll = new ScrollPane(libraryList)

  private val installButton = new Button(I18N.gui("install"), () => {
    val installCheck = (lib: LibraryInfo) =>
      lib.isVersionRequirementMet(Version.version) && lib.status != LibraryStatus.UpToDate
    val uninstallCheck = (lib: LibraryInfo) => installCheck(lib) && lib.canUninstall
    perform("uninstalling", uninstall, uninstallCheck, true)
    perform("installing", wrappedInstall, installCheck, false)
  })

  private val addToCodeTabButton = new Button(I18N.gui("addToCodeTab"), () => {
    updateSource { source =>
      addExtsToSource(source, selectedValues.map(_.codeName).toSet, tokenizeSource) match {
        case Some(newSource) =>
          newSource

        case _ =>
          new OptionPane(this, I18N.gui.get("common.messages.error"), I18N.gui.get("tools.libraries.failedToAdd"),
                         OptionPane.Options.Ok, OptionPane.Icons.Error)

          source
      }
    }

    recompile()
  })

  private val homepageButton = new Button(I18N.gui("homepage"), () => {
    BrowserLauncher.openURI(LibrariesTab.this, selectedValue.homepage.toURI)
  })

  private val uninstallButton = new Button(I18N.gui("uninstall"), () => {
    perform("uninstalling", uninstall, _.canUninstall, false)
  })

  private val uninstallPanel = new BoxRow(Seq(new HorizontalStrut(6), uninstallButton))

  private val info = new TextArea(2, 28)
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

  private val installedVersion = new JLabel
  private val latestVersion = new JLabel
  private val minNetLogoVersion = new JLabel

  private val nlvPanel = new BoxRow(Seq(minNetLogoVersionLabel, minNetLogoVersion))

  locally {

    import org.nlogo.swing.Implicits.thunk2documentListener

    info.setLineWrap(true)
    info.setWrapStyleWord(true)
    info.setEditable(false)

    add(new BoxRow(Seq(magIcon, filterField), 6))
    add(new BoxRow(Seq(
      libraryScroll,
      new BoxColumn(Seq(
        new BoxRow(Seq(
          new BoxRow(Seq(
            installButton,
            uninstallPanel,
          )),
          addToCodeTabButton,
          homepageButton
        ), 6) with MaximumHeight,
        new VerticalStrut(6),
        new BoxRow(Seq(installedVersionLabel, installedVersion)),
        new BoxRow(Seq(latestVersionLabel, latestVersion)),
        nlvPanel,
        new VerticalStrut(6),
        infoScroll
      ))
    ), 6))

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
    installButton.setVisible(!actionIsInProgress && !selectedValues.forall(_.status == LibraryStatus.UpToDate))
    uninstallPanel.setVisible(!actionIsInProgress && selectedValues.exists(_.status != LibraryStatus.CanInstall))
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
                       OptionPane.Options.Ok, OptionPane.Icons.Error)
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
    updateStatus(I18N.gui(operation + "Multiple", Int.box(numOperatedLibs), category.toLowerCase))

  private def updateSingleOperationStatus(operation: String, libName: String) =
    updateStatus(I18N.gui(operation, libName))

  private class CellRenderer extends BoxRow(6) with ListCellRenderer[LibraryInfo] {
    private val upToDateIcon: Icon = Utils.iconScaledWithColor("/images/check.png", 24, 24,
                                                               () => InterfaceColors.checkFilled())
    private val warningIcon: Icon = Utils.iconScaledWithColor("/images/exclamation-triangle.png", 24, 24,
                                                              () => InterfaceColors.warningIcon())
    private val canUpdateIcon: Icon = Utils.iconScaledWithColor("/images/update.png", 24, 24,
                                                                () => InterfaceColors.updateIcon())

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
          case LibraryStatus.CanInstall => null
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
      updateLists()
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
    libraryList.setBackground(InterfaceColors.dialogBackground())

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

    val queue: EventQueue = Toolkit.getDefaultToolkit.getSystemEventQueue

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
