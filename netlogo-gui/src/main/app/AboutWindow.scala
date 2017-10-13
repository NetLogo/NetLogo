// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app

import java.awt._
import event._
import javax.swing._
import java.net.URI

import org.nlogo.util.SysInfo
import org.nlogo.api.{ FileIO, APIVersion, TwoDVersion, Version }
import org.nlogo.swing.{ BrowserLauncher, RichAction }
import org.nlogo.swing.Implicits._
import org.nlogo.swing.Utils.icon

class AboutWindow(parent:Frame) extends JDialog(parent,false) {
  private val refreshTimer: Timer = new Timer(2000, () => refreshSystemText())
  private val system: JTextArea = new JTextArea() {
    setFont(new Font(org.nlogo.awt.Fonts.platformMonospacedFont, Font.PLAIN, 12))
    setLineWrap(true)
    setWrapStyleWord(true)
    setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10))
    setDragEnabled(false)
    setEditable(false)
  }
  private var graphicsInfo = ""
  private val staticInfo =
    TwoDVersion.version +
      " (" + Version.buildDate + ")\n" +
      "Extension API version: " + APIVersion.version + "\n" +
      SysInfo.getVMInfoString + "\n" +
      SysInfo.getOSInfoString + "\n" +
      SysInfo.getScalaVersionString + "\n"
  locally {
    setTitle("About NetLogo")
    setResizable(false)
    setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE)

    val label = new JLabel {
      setText(
          "<html>\n"
          + "<center>"
          + "<b>" + Version.versionDropZeroPatch
          + " <font color=\"#666666\">(" + Version.buildDate
          + ")</font>" + "</b><br><br>\n"
          + "<font size=-1><b>web site</b> "
          + "<a href=\"http://ccl.northwestern.edu/netlogo/\">ccl.northwestern.edu/netlogo</a><br><br>"
          + "<font color=\"#333333\">&copy 1999-2016 Uri Wilensky<br><br>"
          + "Please cite as:<br>"
          + "Wilensky, U. 1999. NetLogo. http://ccl.northwestern.edu/netlogo/.<br>"
          + "Center for Connected Learning and Computer-Based Modeling,<br>"
          + "Northwestern University. Evanston, IL."
          + "</center> </html>"
       )
      setHorizontalAlignment(SwingConstants.CENTER)
      addMouseListener(new MouseAdapter {
        override def mouseClicked(e: MouseEvent) {
          BrowserLauncher.openURI(AboutWindow.this, new URI("http://ccl.northwestern.edu/netlogo/"))
        }
      })
    }

    refreshSystemText()

    getContentPane.setLayout(new BorderLayout(0,10))
    val graphic = new JLabel(icon("/images/title.jpg")) {
      setBorder(BorderFactory.createEmptyBorder(10,10,0,10))
    }
    getContentPane.add(graphic,BorderLayout.NORTH)

    val credits = new JTextArea(FileIO.getResourceAsString("/system/about.txt"),15,0){
      setFont(new Font(org.nlogo.awt.Fonts.platformMonospacedFont,Font.PLAIN,12))
      setDragEnabled(false)
      setLineWrap(true)
      setWrapStyleWord(true)
      setEditable(false)
      setBorder(BorderFactory.createEmptyBorder(5,10,5,10))
    }

    val tabs = new JTabbedPane(){
      add("Credits",new JScrollPane(credits){ setPreferredSize(new Dimension(200,230)) })
      add("System",new JScrollPane(system){ setPreferredSize(new Dimension(200,230)) })
    }
    getContentPane.add(label,BorderLayout.CENTER)
    getContentPane.add(tabs,BorderLayout.SOUTH)

    org.nlogo.swing.Utils.addEscKeyAction(this, RichAction{ _ => dispose() } )
    pack()
    org.nlogo.awt.Positioning.center(this,null)

    // Bring the parent frame (the main NetLogo window) to front.
    // Otherwise this will be obscured (sometimes completely) by
    // the front window (e.g. the System Dynamics Modeler) on OS X,
    // because of the way that non-modal dialogs are layered with
    // their parent. Maybe this should be an independent frame and
    // not a dialog...  - AZS 6/18/05
    parent.toFront()

    refreshTimer.start()

    addWindowListener(new WindowAdapter() {
      override def windowClosed(evt:WindowEvent){ refreshTimer.stop() }
    })
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
}
