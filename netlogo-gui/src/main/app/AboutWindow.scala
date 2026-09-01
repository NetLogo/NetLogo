// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app

import java.awt.{ Cursor, Dimension, Frame }
import java.awt.event.{ WindowAdapter, WindowEvent }
import javax.swing.{ JDialog, JEditorPane, JLabel, Timer, WindowConstants }
import javax.swing.border.LineBorder

import org.nlogo.api.{ APIVersion, FileIO, Version }
import org.nlogo.awt.Positioning
import org.nlogo.core.I18N
import org.nlogo.editor.EditorConfiguration
import org.nlogo.swing.{ BoxAlign, BoxColumn, BoxRow, RichAction, ScrollPane, SyncZoom, TabbedPane, TextArea, Utils,
                         WindowAutomator, ZoomableBorder, ZoomActions }
import org.nlogo.theme.{ DarkTheme, InterfaceColors, ThemeSync }
import org.nlogo.util.SysInfo

class AboutWindow(parent: Frame)
  extends JDialog(parent, I18N.gui.get("dialog.about"), false) with ZoomActions with ThemeSync {

  WindowAutomator.automate(this)

  private val refreshTimer: Timer = new Timer(2000, _ => refreshSystemText())
  private val system = new TextArea(0, 0, "") {
    setFont(EditorConfiguration.getMonospacedFont)
    setLineWrap(true)
    setWrapStyleWord(true)
    setBorder(new ZoomableBorder(5, 10, 5, 10))
    setDragEnabled(false)
    setEditable(false)
    setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR))

    override def syncTheme(): Unit = {
      super.syncTheme()

      setCaretColor(InterfaceColors.Transparent)
    }
  }
  private var graphicsInfo = ""
  private val staticInfo =
    Version.version +
      " (" + Version.buildDate + ")\n" +
      "Extension API version: " + APIVersion.version + "\n" +
      SysInfo.getVMInfoString + "\n" +
      SysInfo.getOSInfoString + "\n" +
      SysInfo.getScalaVersionString + "\n"

  private val graphic = new JLabel {
    setBorder(new ZoomableBorder(10, 10, 0, 10))
  }

  private val citationText =
    s"""|<html>
        |<center>
        |<b>${Version.versionDropZeroPatch}
        | (${Version.buildDate})
        |</b><br><br>
        |<font size=-1><b>web site</b></font>
        |<a href="https://www.netlogo.org">netlogo.org</a><br><br>
        |&copy 1999-${Version.buildDate.takeRight(4)} Uri Wilensky<br><br>
        |Please cite as:<br>
        |Wilensky, U. 1999. NetLogo. http://ccl.northwestern.edu/netlogo/.<br>
        |Center for Connected Learning and Computer-Based Modeling,<br>
        |Northwestern University. Evanston, IL.
        |</center> </html>""".stripMargin

  private val label = new JEditorPane("text/html", citationText) with ThemeSync {
    setEditable(false)
    setDragEnabled(false)
    setCaretColor(InterfaceColors.Transparent)

    override def syncTheme(): Unit = {
      setBackground(InterfaceColors.dialogBackground())
      setForeground(InterfaceColors.dialogText())
    }
  }

  private val credits = new TextArea(15, 0, FileIO.getResourceAsString("/system/about.txt")) {
    setFont(EditorConfiguration.getMonospacedFont)
    setDragEnabled(false)
    setLineWrap(true)
    setWrapStyleWord(true)
    setEditable(false)
    setBorder(new ZoomableBorder(5, 10, 5, 10))
    setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR))

    override def syncTheme(): Unit = {
      super.syncTheme()

      setCaretColor(InterfaceColors.Transparent)
    }
  }

  private val creditsScrollPane = new ScrollPane(credits) {
    setPreferredSize(new Dimension(200, 230))
  }

  private val systemScrollPane = new ScrollPane(system) {
    setPreferredSize(new Dimension(200, 230))
  }

  private val tabs = new TabbedPane {
    add(I18N.gui.get("dialog.about.credits"), creditsScrollPane)
    add(I18N.gui.get("dialog.about.system"), systemScrollPane)
  }

  private val contents = new BoxColumn(Seq(
    new BoxRow(graphic, BoxAlign.Center),
    label,
    tabs
  ), 10) with SyncZoom {
    setOpaque(true)

    override def zoom(oldZoom: Float): Unit = {
      super.zoom(oldZoom)

      setIcon()
      pack()
    }
  }

  setResizable(false)
  setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE)

  refreshSystemText()

  setContentPane(contents)

  contents.syncZoom()

  syncTheme()

  Utils.addEscKeyAction(this, RichAction{ _ => dispose() } )
  pack()
  Positioning.center(this,null)

  // Bring the parent frame (the main NetLogo window) to front.
  // Otherwise this will be obscured (sometimes completely) by
  // the front window (e.g. the System Dynamics Modeler) on OS X,
  // because of the way that non-modal dialogs are layered with
  // their parent. Maybe this should be an independent frame and
  // not a dialog...  - AZS 6/18/05
  parent.toFront()

  refreshTimer.start()

  addWindowListener(new WindowAdapter {
    override def windowClosed(e: WindowEvent): Unit = {
      refreshTimer.stop()
    }
  })

  override def getPreferredSize: Dimension =
    new Dimension(graphic.getPreferredSize.width, super.getPreferredSize.height)

  private def refreshSystemText(): Unit = {
    val newGraphicsInfo = SysInfo.getMemoryInfoString + "\n\n" +
            SysInfo.getJOGLInfoString + "\n" +SysInfo.getGLInfoString + "\n"
    if (!newGraphicsInfo.equals(graphicsInfo)) {
      val start = system.getSelectionStart()
      val end = system.getSelectionEnd()
      system.setText(staticInfo
              + SysInfo.getMemoryInfoString + "\n\n"
              + SysInfo.getJOGLInfoString + "\n"
              + SysInfo.getGLInfoString + "\n")
      graphicsInfo = newGraphicsInfo
      system.setSelectionStart(start)
      system.setSelectionEnd(end)
    }
  }

  private def setIcon(): Unit = {
    val width: Int = Utils.zoom(600)
    val height: Int = Utils.zoom(231)

    if (InterfaceColors.getTheme == DarkTheme) {
      graphic.setIcon(Utils.iconScaled("/images/banner-dark-versionless.png", width, height))
    } else {
      graphic.setIcon(Utils.iconScaled("/images/banner-versionless.png", width, height))
    }
  }

  override def syncTheme(): Unit = {
    contents.setBackground(InterfaceColors.dialogBackground())

    label.syncTheme()
    credits.syncTheme()
    system.syncTheme()

    creditsScrollPane.setBorder(new LineBorder(InterfaceColors.textAreaBorderNoneditable()))
    creditsScrollPane.setBackground(InterfaceColors.textAreaBackground())

    systemScrollPane.setBorder(new LineBorder(InterfaceColors.textAreaBorderNoneditable()))
    systemScrollPane.setBackground(InterfaceColors.textAreaBackground())

    tabs.syncTheme()

    setIcon()
  }
}
