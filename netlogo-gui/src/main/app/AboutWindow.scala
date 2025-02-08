// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app

import java.awt.{ BorderLayout, Dimension, Font, Frame }
import java.awt.event.{ WindowAdapter, WindowEvent, MouseAdapter, MouseEvent }
import java.net.URI
import javax.swing.{ JDialog, JLabel, SwingConstants, Timer, WindowConstants }
import javax.swing.border.{ EmptyBorder, LineBorder }

import org.nlogo.api.{ APIVersion, FileIO, Version }
import org.nlogo.awt.{ Fonts, Positioning }
import org.nlogo.core.I18N
import org.nlogo.swing.{ BrowserLauncher, RichAction, ScrollPane, TabbedPane, TextArea, Utils }
import org.nlogo.theme.{ InterfaceColors, ThemeSync }
import org.nlogo.util.SysInfo

class AboutWindow(parent: Frame) extends JDialog(parent, I18N.gui.get("dialog.about"), false) with ThemeSync {
  private val refreshTimer: Timer = new Timer(2000, _ => refreshSystemText())
  private val system = new TextArea(0, 0, "") {
    setFont(new Font(Fonts.platformMonospacedFont, Font.PLAIN, 12))
    setLineWrap(true)
    setWrapStyleWord(true)
    setBorder(new EmptyBorder(5, 10, 5, 10))
    setDragEnabled(false)
    setEditable(false)
  }
  private var graphicsInfo = ""
  private val staticInfo =
    Version.version +
      " (" + Version.buildDate + ")\n" +
      "Extension API version: " + APIVersion.version + "\n" +
      SysInfo.getVMInfoString + "\n" +
      SysInfo.getOSInfoString + "\n" +
      SysInfo.getScalaVersionString + "\n"
  private val label = new JLabel {
    val year = Version.buildDate.takeRight(4)
    setText(
      s"""|<html>
          |<center>
          |<b>${Version.versionDropZeroPatch}
          | (${Version.buildDate})
          |</b><br><br>
          |<font size=-1><b>web site</b>
          |<a href="http://ccl.northwestern.edu/netlogo/">ccl.northwestern.edu/netlogo</a><br><br>
          |&copy 1999-${year} Uri Wilensky<br><br>
          |Please cite as:<br>
          |Wilensky, U. 1999. NetLogo. http://ccl.northwestern.edu/netlogo/.<br>
          |Center for Connected Learning and Computer-Based Modeling,<br>
          |Northwestern University. Evanston, IL.
          |</center> </html>""".stripMargin
    )
    setHorizontalAlignment(SwingConstants.CENTER)
    addMouseListener(new MouseAdapter {
      override def mouseClicked(e: MouseEvent) {
        BrowserLauncher.openURI(AboutWindow.this, new URI("http://ccl.northwestern.edu/netlogo/"))
      }
    })
  }

  private val credits = new TextArea(15, 0, FileIO.getResourceAsString("/system/about.txt")) {
    setFont(new Font(Fonts.platformMonospacedFont, Font.PLAIN, 12))
    setDragEnabled(false)
    setLineWrap(true)
    setWrapStyleWord(true)
    setEditable(false)
    setBorder(new EmptyBorder(5, 10, 5, 10))
  }

  private val creditsScrollPane = new ScrollPane(credits) {
    setPreferredSize(new Dimension(200, 230))
  }

  private val systemScrollPane = new ScrollPane(system) {
    setPreferredSize(new Dimension(200, 230))
  }

  private val tabs = new TabbedPane {
    add("Credits", creditsScrollPane)
    add("System", systemScrollPane)
  }

  locally {
    setResizable(false)
    setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE)

    refreshSystemText()

    getContentPane.setLayout(new BorderLayout(0, 10))
    val graphic = new JLabel(Utils.iconScaled("/images/title.png", 600, 97)) {
      setBorder(new EmptyBorder(10,10,0,10))
    }
    getContentPane.add(graphic, BorderLayout.NORTH)

    getContentPane.add(label, BorderLayout.CENTER)
    getContentPane.add(tabs, BorderLayout.SOUTH)

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
      override def windowClosed(e: WindowEvent) {
        refreshTimer.stop()
      }
    })

    syncTheme()
  }

  private def refreshSystemText() {
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

  def syncTheme(): Unit = {
    getContentPane.setBackground(InterfaceColors.DIALOG_BACKGROUND)

    label.setForeground(InterfaceColors.TOOLBAR_TEXT)

    credits.syncTheme()
    system.syncTheme()

    creditsScrollPane.setBorder(new LineBorder(InterfaceColors.TEXT_AREA_BORDER_NONEDITABLE))
    creditsScrollPane.setBackground(InterfaceColors.TEXT_AREA_BACKGROUND)

    systemScrollPane.setBorder(new LineBorder(InterfaceColors.TEXT_AREA_BORDER_NONEDITABLE))
    systemScrollPane.setBackground(InterfaceColors.TEXT_AREA_BACKGROUND)

    tabs.syncTheme()
  }
}
