// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app

import java.awt.{ Cursor, FileDialog => AwtFileDialog }
import java.awt.event.{ ActionListener, ActionEvent, KeyEvent, FocusEvent, MouseEvent }
import java.io.IOException
import java.util.{ ArrayList, List => JList }

import javax.imageio.ImageIO
import javax.swing.{ JMenuItem, JPopupMenu, JOptionPane }

import org.nlogo.window.{ ButtonWidget, ChooserWidget, CodeEditor,
  EditorColorizer, GUIWorkspace, InputBoxWidget, InterfaceGlobalWidget,
  JobWidget, MonitorWidget, OutputWidget, PlotWidget, SliderWidget,
  ViewWidget, ViewWidgetInterface, Widget, WidgetInfo, WidgetRegistry }
import org.nlogo.api.{ Editable, Exceptions, ModelSection, Version, VersionHistory }
import org.nlogo.awt.{ Fonts, Hierarchy, Images, UserCancelException }
import org.nlogo.core.{ AgentKind, I18N, View => CoreView, Widget => CoreWidget,
  Button => CoreButton, Chooser => CoreChooser, InputBox => CoreInputBox,
  Monitor => CoreMonitor, Output => CoreOutput, Plot => CorePlot, Slider => CoreSlider,
  Switch => CoreSwitch, TextBox => CoreTextBox }
import org.nlogo.log.Logger
import org.nlogo.swing.{ FileDialog => SwingFileDialog }
import org.nlogo.swing.ModalProgressTask
import org.nlogo.window.Events.{ CompileAllEvent, CompileMoreSourceEvent,
  EditWidgetEvent, LoadWidgetsEvent, RemoveConstraintEvent }
import org.nlogo.workspace.Evaluator

import scala.collection.JavaConverters._

class InterfacePanel(val viewWidget: ViewWidgetInterface, workspace: GUIWorkspace)
  extends WidgetPanel(workspace)
  with java.awt.event.KeyListener
  with LoadWidgetsEvent.Handler
  with org.nlogo.window.Events.ExportInterfaceEvent.Handler {

  workspace.setWidgetContainer(this)
  // in 3d don't add the view widget since it's always
  // disabled there's no reason for it to take space 7/5/07
  if (!Version.is3D)
    addWidget(viewWidget.asInstanceOf[Widget], 0, 0, false, false)

  viewWidget.asInstanceOf[Widget].deleteable = false
  addKeyListener(this)
  addMouseListener(this)

  ///

  override def focusGained(e: FocusEvent): Unit = {
    _hasFocus = true
    enableButtonKeys(true)
  }

  override def focusLost(e: FocusEvent): Unit = {
    _hasFocus = false
    enableButtonKeys(false)
  }

  ///

  override protected def doPopup(e: MouseEvent): Unit = {
    val menu = new JPopupMenu()
    Seq(WidgetInfo.button,
      WidgetInfo.slider,
      WidgetInfo.switch,
      WidgetInfo.chooser,
      WidgetInfo.input,
      WidgetInfo.monitor,
      WidgetInfo.plot)
    .map(i => i.displayName -> i.widgetThunk)
    .foreach {
        case (displayName, widgetThunk) =>
          menu.add(new WidgetCreationMenuItem(displayName, widgetThunk(), e.getX, e.getY))
    }

    // add all the widgets
    val outputItem =
        new WidgetCreationMenuItem(I18N.gui.get("tabs.run.widgets.output"),
          CoreOutput(0, 0, 0, 0, 11), e.getX, e.getY)
    if (getOutputWidget != null) {
      outputItem.setEnabled(false)
    }
    menu.add(outputItem)

    menu.add(new WidgetCreationMenuItem(I18N.gui.get("tabs.run.widgets.note"), CoreTextBox(None, fontSize = 11, color = 0), e.getX, e.getY))

    // add extra stuff
    menu.add(new JPopupMenu.Separator())
    menu.add(exportItem)

    menu.show(this, e.getX, e.getY)
  }

  val exportItem: JMenuItem = {
    val exportAction = new ActionListener() {
      def actionPerformed(e: ActionEvent): Unit = {
        try {
          exportInterface()
        } catch  {
          case ex: IOException =>
            JOptionPane.showMessageDialog(InterfacePanel.this, ex.getMessage(),
              I18N.gui.get("common.messages.error"), JOptionPane.ERROR_MESSAGE)
        }
      }
    }
    val exportItem = new JMenuItem(
      I18N.gui.get("menu.file.export.interface"))
    exportItem.addActionListener(exportAction)
    exportItem
  }

  class WidgetCreationMenuItem(val displayName: String, val coreWidget: CoreWidget, x: Int, y: Int)
  extends JMenuItem(displayName) with ActionListener {
    addActionListener(this)

    override def actionPerformed(e: ActionEvent): Unit = {
      val widget = makeWidget(coreWidget)
      val wrapper = addWidget(widget, x, y, true, false)
      revalidate()
      wrapper.selected(true)
      wrapper.foreground()
      wrapper.isNew(true)
      new EditWidgetEvent(null).raise(InterfacePanel.this)
      newWidget.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR))
      wrapper.isNew(false)
      newWidget = null
    }
  }

  // This is used both when loading a model and when the user is making
  // new widgets in the UI.  For most widget types, the same type string
  // is used in both places. - ST 3/17/04
  override def makeWidget(coreWidget: CoreWidget): Widget = {
    val fromRegistry = WidgetRegistry(coreWidget.getClass.getSimpleName)
    if (fromRegistry != null)
      fromRegistry
    else coreWidget match {
      case c: CoreChooser  => new org.nlogo.window.ChooserWidget(workspace)
      case b: CoreButton   => new ButtonWidget(workspace.world.mainRNG)
      case p: CorePlot     => PlotWidget(workspace.plotManager)
      case m: CoreMonitor  => new MonitorWidget(workspace.world.auxRNG)
      case s: CoreSlider =>
        new SliderWidget(workspace.world.auxRNG) {
          override def sourceOffset: Int =
            Evaluator.sourceOffset(AgentKind.Observer, false)
        }
      case i: CoreInputBox =>
        val font = Fonts.monospacedFont
        val textArea = new CodeEditor(1, 20, font, true, null, new EditorColorizer(workspace), I18N.gui.fn)
        val dialogTextArea = new CodeEditor(5, 20, font, false, null, new EditorColorizer(workspace), I18N.gui.fn)
        new InputBoxWidget(textArea, dialogTextArea, workspace, this)
      case _ =>
        throw new IllegalStateException("unknown widget type: " + coreWidget.getClass.getName)
    }
  }

  override private[app] def deleteWidgets(hitList: Seq[WidgetWrapper]): Unit = {
    var needsRecompile: Boolean = false
    for (wrapper <- hitList) {
      removeWidget(wrapper)
      wrapper.widget match {
        // this ensures that the right thing happens if we delete
        // a button or monitor that doesn't compile; we need to remove it
        // from the errors tab - ST 12/17/04
        case jobWidget: JobWidget =>
          jobWidget.innerSource = ""
          new CompileMoreSourceEvent(jobWidget).raise(this)
        case _ =>
      }
      wrapper.widget match {
        case _: InterfaceGlobalWidget => needsRecompile = true
        case _ =>
      }
    }
    setForegroundWrapper()
    revalidate()
    repaint() // you wouldn't think this'd be necessary, but without it
    // the widget didn't visually disappear - ST 6/23/03
    if (needsRecompile) {
      new CompileAllEvent().raise(this)
    }
    loseFocusIfAppropriate()
  }

  override protected def removeWidget(wrapper: WidgetWrapper): Unit = {
    remove(wrapper)

    // if the compile that is associated with this removal (assuming there is one) fails
    // the observer variables and constraints might not get reallocated in which case
    // if we try to add a different widget with the same name we get a constraint violation
    // from the old constraint. yuck.  ev 11/27/07
    new RemoveConstraintEvent(wrapper.widget.displayName).raise(this)

    Logger.logWidgetRemoved(wrapper.widget.classDisplayName,
        wrapper.widget.displayName)
  }

  /// loading and saving

  override def loadWidget(coreWidget: CoreWidget): Widget =
    loadWidget(coreWidget, 0, 0)

  // TODO: consider cleaning up this x and y business
  // it was added for copying/pasting widgets.
  // the regular loadWidget just uses the x and y from the string array
  // it passes in x=0, y=0 and we do a check. ugly, but works for now.
  // paste uses the x and y from the right click location.
  private def loadWidget(coreWidget: CoreWidget, _x: Int, _y: Int): Widget = {
    val x = if (_x == 0) coreWidget.left else _x
    val y = if (_y == 0) coreWidget.top  else _y
    coreWidget match {
      case view: CoreView =>
        // the graphics widget (and the command center) are special cases because
        // they are not recreated at load time, but reused
        viewWidget.asInstanceOf[ViewWidget].load(view)
        // in 3D we don't add the viewWidget to the interface panel
        // so don't worry about all the sizing junk ev 7/5/07
        val parent = viewWidget.asWidget.getParent
        if (parent != null) {
          parent.setSize(viewWidget.asWidget.getSize)
          enforceMinimumAndMaximumWidgetSizes(viewWidget.asWidget)
          parent.setLocation(x, y)
          zoomer.zoomWidgetLocation(
            getWrapper(viewWidget.asWidget),
                  true, true, 1.0, zoomer.zoomFactor)
          zoomer.zoomWidgetSize(
            getWrapper(viewWidget.asWidget),
                  true, true, 1.0, zoomer.zoomFactor)
          zoomer.scaleComponentFont(
            viewWidget.asInstanceOf[ViewWidget].view,
                 zoomFactor, 1.0, false)
        }
        viewWidget.asWidget
      case _ =>
        makeAndLoadWidget(coreWidget, x, y)
    }
  }

  override def getWidgetsForSaving: Seq[CoreWidget] =
    // automatically add the view widget since it isn't in
    // the components list in 3D - ev 7/5/07
    (viewWidget.model +: getComponents.reverse.collect {
      case wrapper: WidgetWrapper => wrapper.widget.model
    }).distinct

  override private[app] def contains(w: Editable): Boolean =
    if (w == viewWidget.asInstanceOf[Widget].getEditable)
      true
    else
      super.contains(w)

  override def handle(e: org.nlogo.window.Events.WidgetRemovedEvent): Unit = {
  }

  def handle(e: org.nlogo.window.Events.ExportInterfaceEvent): Unit = {
    try ImageIO.write(Images.paintToImage(this), "png", e.stream)
    catch {
      case ex: IOException =>
        e.exceptionBox(0) = ex
    }
  }

  @throws(classOf[java.io.IOException])
  private def exportInterface(): Unit = {
    try {
      val exportPath = SwingFileDialog.show(this,
        I18N.gui.get("dialog.interface.export.file"),
        AwtFileDialog.SAVE,
        workspace.guessExportName("interface.png"))
      var exception = Option.empty[IOException]
      val runExport = new Runnable() {
        def run(): Unit = {
          try workspace.exportInterface(exportPath)
          catch {
            case ex: IOException => exception = Some(ex)
          }}}
      ModalProgressTask(Hierarchy.getFrame(this),
        I18N.gui.get("dialog.interface.export.task"), runExport)
      exception.foreach(e => throw e)
    } catch {
      case ex: UserCancelException => Exceptions.ignore(ex)
    }
  }

  def handle(e: LoadWidgetsEvent): Unit = {
    loadWidgets(e.widgets)
  }

  override def removeAllWidgets(): Unit = {
    try {
      val comps = getComponents()
      setVisible(false)
      for (component <- getComponents) {
        component match {
          case w: WidgetWrapper if w.widget != viewWidget =>
            removeWidget(w)
          case _ =>
        }
      }
    } catch {
      case ex: RuntimeException => Exceptions.handle(ex)
    } finally {
      setVisible(false)
    }
  }

  /// buttons

  override def isFocusable: Boolean =
    getComponents.collect {
      case w: WidgetWrapper => w.widget
    }.exists {
      case _: InputBoxWidget => true
      case b: ButtonWidget   =>
        b.actionKey != '\u0000' && b.actionKey != ' '
      case _ => false
    }

  private def findActionButton(key: Char): ButtonWidget = {
    import java.lang.Character.toUpperCase
    getComponents.collect {
      case w: WidgetWrapper => w.widget
    }.collect {
      case b: ButtonWidget if toUpperCase(b.actionKey) == toUpperCase(key) => b
    }.headOption.orNull
  }

  private def enableButtonKeys(enabled: Boolean): Unit =
    getComponents.collect {
      case w: WidgetWrapper => w.widget
    }.foreach {
      case b: ButtonWidget => b.keyEnabled(enabled)
      case _ =>
    }

  def keyTyped(e: KeyEvent): Unit = {
    if (e.getKeyChar() != KeyEvent.CHAR_UNDEFINED &&
      !e.isActionKey &&
    (e.getModifiers & getToolkit.getMenuShortcutKeyMask) == 0) {
      Option(findActionButton(e.getKeyChar)).foreach { button =>
        button.keyTriggered()
      }
    }
  }

  def keyPressed(evt: KeyEvent): Unit = { }

  def keyReleased(evt: KeyEvent): Unit = { }

  override def canAddWidget(widget: String): Boolean = {
    return (widget != "Output" || getOutputWidget == null)
  }
}
