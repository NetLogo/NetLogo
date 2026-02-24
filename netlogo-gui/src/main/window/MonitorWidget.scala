// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.{ Color, Component, Dimension, EventQueue, Font, Graphics, GridBagConstraints, GridBagLayout, Insets,
                  Point }
import java.awt.datatransfer.StringSelection
import java.awt.event.{ ActionEvent, MouseEvent }
import javax.swing.{ AbstractAction, JLabel, JPanel }

import org.nlogo.api.{ CompilerServices, Dump, MersenneTwisterFast }
import org.nlogo.core.{ AgentKind, AgentKindJ, I18N, Monitor => CoreMonitor, Widget => CoreWidget }
import org.nlogo.editor.Colorizer
import org.nlogo.nvm.Procedure
import org.nlogo.swing.{ MenuItem, PopupMenu, RoundedBorderPanel }
import org.nlogo.theme.{ InterfaceColors, ThemeSync }
import org.nlogo.window.Events.{ AddJobEvent, EditWidgetEvent,
  RuntimeErrorEvent, PeriodicUpdateEvent, JobRemovedEvent, RemoveJobEvent }

object MonitorWidget {
  private val MinWidth = 50
  private val DefaultDecimalPlaces = 17
  private val DefaultFontSize = 11

  trait ToMonitorModel { self: Widget & Component =>
    def decimalPlaces: Int
    def units: String
    def fontSize: Int
    def innerSource: String
    def name: String

    override def model: CoreWidget = {
      val b       = getUnzoomedBounds
      val display = name.potentiallyEmptyStringToOption
      val src     = innerSource.potentiallyEmptyStringToOption

      CoreMonitor(display = display,
        x = b.x, y = b.y, width = b.width, height = b.height,
        oldSize = _oldSize,
        source   = src, precision = decimalPlaces,
        fontSize = fontSize, units = if (units.isEmpty) None else Option(units))
    }
  }
}

import MonitorWidget._

class MonitorWidget(random: MersenneTwisterFast, compiler: CompilerServices, colorizer: Colorizer)
    extends JobWidget(random)
    with Editable
    with ToMonitorModel
    with RuntimeErrorEvent.Handler
    with PeriodicUpdateEvent.Handler
    with JobRemovedEvent.Handler
    with java.awt.event.MouseListener {

  private class ValuePanel(label: JLabel) extends JPanel(new GridBagLayout) with RoundedBorderPanel with ThemeSync {
    locally {
      val c = new GridBagConstraints

      c.weightx = 1
      c.anchor = GridBagConstraints.WEST
      c.insets = new Insets(0, zoom(6), 0, zoom(6))

      add(label, c)
    }

    override def paintComponent(g: Graphics): Unit = {
      setDiameter(zoom(6))

      super.paintComponent(g)
    }

    override def syncTheme(): Unit = {
      setBackgroundColor(InterfaceColors.displayAreaBackground())
      setBorderColor(InterfaceColors.monitorBorder())
    }
  }

  private var jobRunning: Boolean = false
  private var hasError: Boolean = false
  private var _name: String = ""
  private var _value: Option[AnyRef] = Option.empty[AnyRef]
  private var valueString: String = ""
  private var _decimalPlaces: Int = DefaultDecimalPlaces

  // This is the same as the button so right-click on a monitor w/ error
  // brings up the popup menu not the edit dialog. ev 1/4/06
  private var lastMousePressedWasPopupTrigger: Boolean = false;

  private var _fontSize = DefaultFontSize

  private val nameLabel = new JLabel(I18N.gui.get("edit.monitor.previewName"))
  private lazy val valueLabel = new JLabel
  private val valuePanel = new ValuePanel(valueLabel)
  private val unitsLabel = new JLabel

  unitsLabel.setVisible(false)

  addMouseListener(this)

  setLayout(new GridBagLayout)

  initGUI()

  override def initGUI(): Unit = {
    removeAll()

    val c = new GridBagConstraints

    c.gridx = 0
    c.gridwidth = 2
    c.weightx = 1
    c.anchor = GridBagConstraints.NORTHWEST
    c.insets = {
      if (_oldSize) {
        new Insets(zoom(3), zoom(6), 0, zoom(6))
      } else {
        new Insets(zoom(6), zoom(8), zoom(6), zoom(8))
      }
    }

    add(nameLabel, c)

    nameLabel.setFont(nameLabel.getFont.deriveFont(_boldState))

    c.gridwidth = 1
    c.fill = GridBagConstraints.BOTH
    c.weighty = 1
    c.insets = {
      if (_oldSize) {
        new Insets(0, zoom(6), zoom(6), zoom(6))
      } else {
        new Insets(0, zoom(8), zoom(8), zoom(8))
      }
    }

    add(valuePanel, c)

    c.gridx = 1
    c.fill = GridBagConstraints.NONE
    c.weightx = 0
    c.weighty = 0
    c.insets = {
      if (_oldSize) {
        new Insets(0, 0, zoom(6), zoom(6))
      } else {
        new Insets(0, 0, zoom(8), zoom(8))
      }
    }

    add(unitsLabel, c)

    unitsLabel.setFont(unitsLabel.getFont.deriveFont(_boldState))
  }

  def setDisplayName(name: String): Unit = {
    _name = name
    chooseDisplayName()
    repaint()
  }

  def name: String = _name

  def setFontSize(size: Int): Unit = {
    _fontSize = size
    // If we are zoomed, we need to zoom the input font size and then
    // set that as our widget font
    val newFontSize =
      if (originalFont != null) {
        val zoomDiff = getFont.getSize - originalFont.getSize
        zoomDiff + size
      } else
        size

    setFont(getFont.deriveFont(newFontSize.toFloat))

    if (originalFont != null)
      this.originalFont = originalFont.deriveFont(size.toFloat)

    // These should reset the cached values in the Zoomer, but
    // after one finishes a property widget edit, it appears to
    // still cache the min height. -- CLB
    resetZoomInfo()
    resetSizeInfo()
  }

  override def setFont(f: Font): Unit = {
    if (isZoomed || getFont == null) {
      super.setFont(f)

      valueLabel.setFont(f)
    } else {
      val newFont = getFont.deriveFont(fontSize.toFloat)

      super.setFont(newFont)

      valueLabel.setFont(newFont)
    }

    revalidate()
    repaint()
  }

  def fontSize: Int = _fontSize

  def units: String = unitsLabel.getText
  def setUnits(value: String): Unit = {
    unitsLabel.setText(value.trim)
    unitsLabel.setVisible(value.trim.nonEmpty)
    revalidate()
    repaint()
  }

  override def classDisplayName: String =
    I18N.gui.get("tabs.run.widgets.monitor")

  override def editPanel: EditPanel = new MonitorEditPanel(this, compiler, colorizer)

  override def getEditable: Option[Editable] = Some(this)

  override def kind: AgentKind = AgentKindJ.Observer

  override def ownsPrimaryJobs: Boolean =
    false

  override def procedure_=(procedure: Procedure): Unit = {
    super.procedure = procedure
    halt()
    if (procedure != null) {
      hasError = false
      new AddJobEvent(this, agents, procedure).raise(this)
      jobRunning = true
    }
    syncTheme()
    repaint()
  }

  def value: AnyRef = _value.orNull

  def value(value: AnyRef): Unit = {
    this._value = Option(value);
    val newString = Dump.logoObject(value)
    if (newString != valueString) {
      valueString = newString
      valueLabel.setText(valueString)
      repaint()
    }
  }

  def chooseDisplayName(): Unit = {
    if (name == null || name == "") {
      if (getSourceName == "") {
        displayName(I18N.gui.get("edit.monitor.previewName"))
      } else {
        displayName(getSourceName)
      }
    } else {
      displayName(name)
    }

    nameLabel.setText(displayName)

    if (nameLabel.getPreferredSize.width > nameLabel.getWidth) {
      nameLabel.setToolTipText(nameLabel.getText)
    } else {
      nameLabel.setToolTipText(null)
    }
  }

  // behold the mighty regular expression
  private def getSourceName: String =
    innerSource.trim.replaceAll("\\s+", " ")

  override def removeNotify(): Unit = {
    // This is a little kludgy.  Normally removeNotify would run on the
    // event thread, but in an applet context, when the applet
    // shuts down, removeNotify can run on some other thread. But
    // actually this stuff doesn't need to happen in the applet,
    // so we can just skip it in that context. - ST 10/12/03, 10/16/03
    if (EventQueue.isDispatchThread)
      halt()

    super.removeNotify()
  }

  override def suppressRecompiles(suppressRecompiles: Boolean): Unit = {
    if (innerSource.trim == "")
      recompilePending(false)

    super.suppressRecompiles(suppressRecompiles)
  }

  override def hasContextMenu: Boolean =
    true

  override def populateContextMenu(menu: PopupMenu, p: Point): Unit = {
    menu.add(new MenuItem(new AbstractAction(I18N.gui.get("tabs.run.widget.copytext")) {
      def actionPerformed(e: ActionEvent): Unit = {
        getToolkit.getSystemClipboard.setContents(new StringSelection(valueLabel.getText), null)
      }
    }))
  }

  override def syncTheme(): Unit = {
    setBackgroundColor(InterfaceColors.monitorBackground())

    if (anyErrors) {
      nameLabel.setForeground(Color.RED)
    } else {
      nameLabel.setForeground(InterfaceColors.widgetText())
    }

    valueLabel.setForeground(InterfaceColors.displayAreaText())
    unitsLabel.setForeground(InterfaceColors.widgetText())

    valuePanel.syncTheme()
  }

  override def getMinimumSize: Dimension = {
    if (_oldSize) {
      new Dimension(MinWidth, (fontSize * 4) + 1)
    } else {
      new Dimension(100, 60)
    }
  }

  override def getPreferredSize: Dimension = {
    if (_oldSize) {
      new Dimension(100, getMinimumSize.height)
    } else {
      new Dimension(100, 60)
    }
  }

  def decimalPlaces: Int = _decimalPlaces

  def setDecimalPlaces(decimalPlaces: Int): Unit = {
    if (decimalPlaces != _decimalPlaces) {
      _decimalPlaces = decimalPlaces
      setWrapSource(innerSource)
    }
  }

  override def innerSource_=(innerSource: String): Unit = {
    super.innerSource = innerSource
    chooseDisplayName()
  }

  def setWrapSource(innerSource: String): Unit = {
    if (innerSource.trim == "") {
      source("", "", "")
      halt()
    } else {
      source(
        "to __monitor [] __observercode loop [ __updatemonitor __monitorprecision (",
          innerSource,
          "\n) " + decimalPlaces + " ] end")
    }
    chooseDisplayName()
  }

  def wrapSource: String = innerSource

  def handle(e: RuntimeErrorEvent): Unit =
    if (this == e.jobOwner) {
      hasError = true
      halt()
      syncTheme()
      repaint()
    }

  def handle(e: PeriodicUpdateEvent): Unit = {
    if (!jobRunning && procedure != null) {
      hasError = false
      jobRunning = true
      new AddJobEvent(this, agents, procedure).raise(this)
    }

    syncTheme()
    repaint()
  }

  def handle(e: JobRemovedEvent): Unit =
    if (e.owner == this) {
      jobRunning = false
      value(if (hasError) I18N.gui.get("tabs.run.widgets.monitor.notApplicable") else "")
    }

  def halt(): Unit = {
    new RemoveJobEvent(this).raise(this)
  }

  override def load(model: CoreWidget): Unit = {
    model match {
      case monitor: CoreMonitor =>
        setUnits(monitor.units.getOrElse(""))
        setDisplayName(monitor.display.getOrElse(""))
        _decimalPlaces = monitor.precision
        setFontSize(monitor.fontSize)

        monitor.source.foreach(setWrapSource)

        oldSize(monitor.oldSize)
        setSize(monitor.width, monitor.height)
        chooseDisplayName()

      case _ =>
    }
  }

  def mouseClicked(e: MouseEvent): Unit = {
    if (e.getButton == MouseEvent.BUTTON1 && !e.isPopupTrigger && error().isDefined &&
        !lastMousePressedWasPopupTrigger)
      new EditWidgetEvent(widgetContainer.orNull, this).raise(this)
  }

  def mousePressed(e: MouseEvent): Unit = {
    lastMousePressedWasPopupTrigger = e.isPopupTrigger
  }

  def mouseReleased(e: MouseEvent): Unit = { }

  def mouseEntered(e: MouseEvent): Unit = { }

  def mouseExited(e: MouseEvent): Unit = { }
}
