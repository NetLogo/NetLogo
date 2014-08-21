// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app

import org.nlogo.swing.Implicits._
import org.nlogo.window.{EditorColorizer, EditDialogFactoryInterface, GUIWorkspace}
import org.nlogo.window.Events._
import org.nlogo.app.Events._
import org.nlogo.swing.RichAction
import org.nlogo.api.I18N

class Tabs(val workspace: GUIWorkspace,
           val reviewTab: org.nlogo.window.ReviewTabInterface,
           monitorManager: AgentMonitorManager,
           dialogFactory: EditDialogFactoryInterface) extends javax.swing.JTabbedPane
  with javax.swing.event.ChangeListener with org.nlogo.window.Event.LinkParent
  with LoadBeginEventHandler with RuntimeErrorEventHandler with CompiledEventHandler
  with JumpToDefinitionEventHandler{

  locally{
    setOpaque(false)
    setFocusable(false)
    addChangeListener(this)
  }

  var tabsMenu: org.nlogo.swing.TabsMenu = null

  val interfaceTab = new InterfaceTab(workspace, monitorManager, dialogFactory)
  val infoTab = new InfoTab(workspace.attachModelDir(_))
  val codeTab = new MainCodeTab(workspace)

  var previousTab: java.awt.Component = interfaceTab
  var currentTab: java.awt.Component = interfaceTab

  def init(moreTabs: (String, java.awt.Component) *) {
    addTab(I18N.gui.get("tabs.run"), interfaceTab)
    addTab(I18N.gui.get("tabs.info"), infoTab)
    addTab(I18N.gui.get("tabs.code"), codeTab)
    for((name, tab) <- moreTabs)
      addTab(name, tab)
    tabsMenu = new org.nlogo.swing.TabsMenu(I18N.gui.get("menu.tabs"), this)
  }

  def showReviewTab(selectReviewTab: Boolean = true) {
    if (indexOfComponent(reviewTab) == -1) {
      addTab("Review", reviewTab)
      addMenuItem(getTabCount() - 1, "Review")
      reviewTab.recordingEnabled = true
    }
    org.nlogo.window.Event.rehash()
    if (selectReviewTab) {
      setSelectedComponent(reviewTab)
      org.nlogo.awt.EventQueue.invokeLater(() => requestFocus())
    }
  }

  def stateChanged(e: javax.swing.event.ChangeEvent) {
    previousTab = currentTab
    currentTab = getSelectedComponent
    currentTab.requestFocus()
    new Events.SwitchedTabsEvent(previousTab, currentTab).raise(this)
  }

  override def requestFocus() { currentTab.requestFocus() }
  def handle(e: LoadBeginEvent) { setSelectedComponent(interfaceTab) }
  def handle(e: RuntimeErrorEvent) {
    if(!e.jobOwner.isInstanceOf[org.nlogo.window.MonitorWidget])
      if(e.sourceOwner == codeTab)
        highlightRuntimeError(codeTab, e)
      else if(e.sourceOwner.isInstanceOf[org.nlogo.window.ExternalFileInterface]) {
        val filename = e.sourceOwner.asInstanceOf[org.nlogo.window.ExternalFileInterface].getFileName
        val tab = getTabWithFilename(filename).getOrElse{
          openTemporaryFile(filename, true)
          getTabWithFilename(filename).get
        }
        highlightRuntimeError(tab, e)
      }
  }

  def highlightRuntimeError(tab: CodeTab, e: RuntimeErrorEvent) {
    setSelectedComponent(tab)
    // the use of invokeLater here is a desperate attempt to work around the Mac bug where sometimes
    // the selection happens and sometime it doesn't - ST 8/28/04
    org.nlogo.awt.EventQueue.invokeLater( () => tab.select(e.pos, e.pos + e.length) )
  }

  val errorColor = java.awt.Color.RED

  def handle(e: CompiledEvent) {
    def clearErrors() {
      for(i <- 0 until getTabCount)
        if(getComponentAt(i).isInstanceOf[CodeTab])
          setForegroundAt(i, null)
    }
    def recolorTab(component: java.awt.Component, hasError: Boolean) {
      setForegroundAt(indexOfComponent(component), if(hasError) errorColor else null)
    }

    // recolor tabs
    if(e.sourceOwner.isInstanceOf[CodeTab]) {
      val tab = e.sourceOwner.asInstanceOf[CodeTab]
      if(e.error != null) setSelectedComponent(tab)
      // on null error, clear all errors, as we only get one event for all the files
      if(e.error == null) clearErrors() else recolorTab(tab, e.error != null)
      // I don't really know why this is necessary when you delete a slider (by using the menu
      // item *not* the button) which causes an error in the Code tab the focus gets lost,
      // so request the focus by a known component 7/18/07
      requestFocus()
    }
    if(e.sourceOwner.isInstanceOf[org.nlogo.window.ExternalFileInterface]) {
      val filename = e.sourceOwner.asInstanceOf[org.nlogo.window.ExternalFileInterface].getFileName
      var tab = getTabWithFilename(filename)
      if(! tab.isDefined && e.error != null) {
        openTemporaryFile(filename, true)
        tab = getTabWithFilename(filename)
      }
      if(e.error != null) setSelectedComponent(tab.get)
      recolorTab(tab.get, e.error != null)
      requestFocus()
    }
    if((e.sourceOwner.isInstanceOf[org.nlogo.window.JobWidget] &&
        !e.sourceOwner.asInstanceOf[org.nlogo.window.JobWidget].isCommandCenter
        || e.sourceOwner == null // i'm assuming this is only true when
        // we've deleted that last widget. not a great sol'n - AZS 5/16/05
      )) {
      if(e.error != null) setSelectedIndex(0)
      recolorTab(interfaceTab, e.error != null)
    }
  }


  def handle(e: JumpToDefinitionEvent) {
    if(e.name!="") {
      val found = new EditorColorizer(workspace).jumpToDefinition(codeTab.text, e.name)
      if (found)
        setSelectedComponent(codeTab)
      else
        javax.swing.JOptionPane.showMessageDialog(
          currentTab, e.name.toUpperCase + " could not be found in your code.", "Netlogo",
          javax.swing.JOptionPane.ERROR_MESSAGE)
    }
  }

  def openTemporaryFile(filename: String, fileMustExist: Boolean) {
    getTabWithFilename(filename) match {
      case Some(tab) => setSelectedComponent(tab)
      case _ => addNewTab(filename, fileMustExist)
    }
  }

  def getSource(filename: String): String = getTabWithFilename(filename).map(_.innerSource).orNull

  private def getTabWithFilename(name: String): Option[TemporaryCodeTab] =
    // start at 3 because 0, 1, and 2 are the permanent tabs
    (3 until getTabCount)
      .map(getComponentAt)
      .collect{case tab: TemporaryCodeTab => tab}
      .find(_.filename == name)

  def newTemporaryFile() { addNewTab(TemporaryCodeTab.NewFile, false) }

  def addNewTab(name: String, fileMustExist: Boolean) {
    val tab = new TemporaryCodeTab(workspace, this, name, fileMustExist, codeTab.smartTabbingEnabled)
    addTab(stripPath(name), tab)
    addMenuItem(getTabCount() - 1, stripPath(name))
    org.nlogo.window.Event.rehash()
    tab.includesMenu.updateVisibility()
    setSelectedComponent(tab)
    // if I just call requestFocus the tab never gets the focus request because it's not yet
    // visible.  There might be a more swing appropriate way to do this but I can't figure it out
    // (if you know it feel free to fix) ev 7/24/07
    org.nlogo.awt.EventQueue.invokeLater( () => requestFocus() )
  }

  def saveExternalFiles() {
    (3 until getTabCount)
      .map(getComponentAt)
      .collect{case tab: TemporaryCodeTab => tab}
      .foreach(_.doSave())
  }

  def saveTemporaryFile(tab: TemporaryCodeTab, filename: String) {
    val index = getIndexOfComponent(tab)
    setTitleAt(index, stripPath(filename))
    tabsMenu.getItem(index).setText(filename)
  }

  def getIndexOfComponent(tab: CodeTab): Int =
    (0 until getTabCount).find(n => getComponentAt(n) == tab).get

  def closeTemporaryFile(tab: TemporaryCodeTab) {
    val index = getIndexOfComponent(tab)
    remove(tab)
    removeMenuItem(index)
  }

  def removeMenuItem(index: Int) {
    // first remove all the menu items after this one...
    for(i <- tabsMenu.getItemCount() - 1 to index by -1) tabsMenu.remove(i)
    // then add the ones that still exist back, there might be an easier way to do this by simply
    // changing the actions in the other menu items but this seemed like more straight forward code.
    for(i <- index until getTabCount) addMenuItem(i, getTitleAt(i))
  }

  def addMenuItem(i: Int, name: String) {
    tabsMenu.addMenuItem(name, ('1' + i).toChar, RichAction { _ => Tabs.this.setSelectedIndex(i) })
  }

  private def stripPath(filename: String): String =
    filename.substring(filename.lastIndexOf(System.getProperty("file.separator")) + 1, filename.length)

  val printAction = RichAction("print-current-tab") { _ =>
    currentTab match {
      case printable: org.nlogo.swing.Printable =>
        try org.nlogo.swing.PrinterManager.print(printable, workspace.modelNameForDisplay)
        catch {
          case abortEx: java.awt.print.PrinterAbortException => org.nlogo.util.Exceptions.ignore(abortEx)
        }
    }
  }

  override def processMouseMotionEvent(e: java.awt.event.MouseEvent) {
    // do nothing.  mouse moves are for some reason causing doLayout to be called in the tabbed
    // components on windows and linux (but not Mac) in java 6 it never did this before and I don't
    // see any reason why it needs to. It's causing flickering in the info tabs on the affected
    // platforms ev 2/2/09
  }

  /// LinkComponent stuff

  val linkComponents = new collection.mutable.ArrayBuffer[AnyRef]
  def addLinkComponent(c: AnyRef) { linkComponents += c }
  def getLinkChildren = linkComponents.toArray
}
