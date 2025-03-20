// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.interfacetab

import java.awt.{ Color, Component, Dimension, Font, Graphics, Graphics2D, GridBagConstraints, GridBagLayout, Insets, RenderingHints }
import java.awt.event.{ ActionEvent, ActionListener, MouseEvent, MouseAdapter }
import java.awt.font.TextAttribute
import java.net.URI
import java.time.format.{ DateTimeFormatter, FormatStyle }
import java.util.prefs.Preferences
import javax.swing.{ BoxLayout, JButton, JLabel, JPanel }
import javax.swing.border.EmptyBorder
import javax.swing.BorderFactory

import org.nlogo.api.{ Advisory, Announcement, Event, Release }
import org.nlogo.swing.{ BrowserLauncher, MouseUtils, Utils }
import org.nlogo.theme.{ InterfaceColors, ThemeSync }

class AnnouncementBanner extends JPanel with MouseUtils with ThemeSync {

  private val prefs   = Preferences.userRoot.node("/org/nlogo/NetLogo")
  private val prefKey = "announce.latest-read-id"

  private var announcements = Seq[Announcement]()
  private val annTitle      = new JLabel()
  private val annText       = new JLabel()

  private val textPane = new TextPane(annTitle, annText)

  private val ggGoNext = () => {
    prefs.put(prefKey, announcements.head.id.toString)
    announcements = announcements.tail
    renderData()
  }

  private val simpleXButton   = new XButton(ggGoNext)
  private val complexXWrapper = new ComplexXWrapper(ggGoNext)

  locally {

    setHandCursor()

    setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, InterfaceColors.viewBorder))
    setLayout(new GridBagLayout)

    Util.modifyFont(annTitle)(_.deriveFont(24f))
    Util.modifyFont(annText )(_.deriveFont(18f))

    addMouseListener(new MouseAdapter() {

      override def mouseClicked(e: MouseEvent): Unit = {
        announcements.headOption.foreach {
          ann =>
            BrowserLauncher.openURI(new URI(s"https://ccl.northwestern.edu/netlogo/announce.shtml#news-item-${ann.id}"))
        }
      }

      override def mouseEntered(e: MouseEvent): Unit = {
        setSummaryUnderline(true)
      }

      override def mouseExited(e: MouseEvent): Unit = {
        setSummaryUnderline(false)
      }

    })

    syncTheme()
    customLayout()

  }

  private def customLayout(): Unit = {

    val textGBC     = new GridBagConstraints
    textGBC.gridy   = 0
    textGBC.anchor  = GridBagConstraints.WEST
    textGBC.fill    = GridBagConstraints.HORIZONTAL
    textGBC.weightx = 1
    textGBC.insets  = new Insets(0, 20, 0, 20)

    add(textPane, textGBC)

    val xGBC     = new GridBagConstraints
    xGBC.anchor  = GridBagConstraints.EAST
    xGBC.fill    = GridBagConstraints.NONE
    xGBC.weightx = 0
    xGBC.insets  = new Insets(0, 20, 0, 22)

    simpleXButton.setVisible(false)
    add(simpleXButton, xGBC)
    add(complexXWrapper, xGBC)

  }

  def appendData(anns: Seq[Announcement]): Unit = {
    val latestReadID = prefs.get(prefKey, "-1").toInt
    announcements ++= anns.filter(_.id > latestReadID)
    renderData()
  }

  private def setSummaryUnderline(shouldUnderline: Boolean): Unit = {
    Util.handleUnderline(shouldUnderline, annTitle)
    Util.handleUnderline(shouldUnderline, annText)
  }

  private def renderData(): Unit = {

    announcements.headOption.fold {
      setVisible(false)
    } {

      case Announcement(_, title, date, _, annType, summary, _) =>

        val color =
          annType match {
            case Release  => InterfaceColors.announceRelease()
            case Event    => InterfaceColors.announceEvent()
            case Advisory => InterfaceColors.announceAdvisory()
          }

        val dateStr = date.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT))

        val remainingAnnCount = announcements.length - 1
        if (remainingAnnCount > 0) {
          simpleXButton.setVisible(false)
          complexXWrapper.setVisible(true)
          complexXWrapper.setRemaining(remainingAnnCount)
        } else {
          simpleXButton.setVisible(true)
          complexXWrapper.setVisible(false)
        }

        setBackground(color)
        annTitle.setText(s"$title ($dateStr)")
        Util.modifyFont(annTitle)(_.deriveFont(Font.BOLD))
        annText.setText(summary.replaceAll("\n", " ").replaceAll("  ", " "))
        setVisible(true)

    }

  }

  override def getPreferredSize: Dimension =
    new Dimension(super.getPreferredSize.width, 100)

  override def syncTheme(): Unit = {
    renderData()
    textPane.syncTheme()
    simpleXButton.syncTheme()
    complexXWrapper.syncTheme()
  }

}

private class TextPane(title: Component, text: Component) extends JPanel with ThemeSync {

  setOpaque(false)
  add(title)
  add(text)
  setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS))

  override def syncTheme(): Unit = {
    title.setForeground(InterfaceColors.widgetText)
    text .setForeground(InterfaceColors.widgetText)
  }

}

private class XButton(dismissItem: () => Unit) extends JButton with MouseUtils with ThemeSync {

  private def defaultXColor() = InterfaceColors.toolbarControlFocus

  private val setXColor = (color: Color) => setIcon(Utils.iconScaledWithColor("/images/close-light.png", 20, 20, color))

  setBorderPainted(false)
  setContentAreaFilled(false)
  setOpaque(false)
  syncTheme()

  addActionListener(
    new ActionListener() {
      override def actionPerformed(e: ActionEvent): Unit = {
        dismissItem()
      }
    }
  )

  addMouseListener(new MouseAdapter() {

    override def mouseEntered(e: MouseEvent): Unit = {
      setXColor(InterfaceColors.secondaryButtonBackgroundHover)
    }

    override def mouseExited(e: MouseEvent): Unit = {
      setXColor(defaultXColor())
    }

    override def mousePressed(e: MouseEvent): Unit = {
      setXColor(InterfaceColors.toolbarControlFocus)
    }

    override def mouseReleased(e: MouseEvent): Unit = {
      setXColor(defaultXColor())
    }

  })

  override def syncTheme(): Unit = {
    setXColor(defaultXColor())
  }

}

private class ComplexXWrapper(dismissItem: () => Unit) extends JPanel with MouseUtils with ThemeSync {

  private def defaultWrapperColor() = InterfaceColors.scrollBarBackground

  private val complexX    = new JLabel
  private val complexXNum = new JLabel

  setOpaque(false)
  setBorder(new EmptyBorder(2, 12, 2, 12))

  Util.modifyFont(complexXNum)(_.deriveFont(20f))
  complexXNum.setBorder(new EmptyBorder(0, 0, 0, 7))

  complexX.setIcon(Utils.iconScaledWithColor("/images/chevron-right.png", 10, 10, InterfaceColors.toolbarControlFocus))

  val complexGBC = new GridBagConstraints()
  setVisible(false)
  add(complexXNum, complexGBC)
  add(complexX   , complexGBC)

  syncTheme()

  addMouseListener(new MouseAdapter() {

    override def mouseEntered(e: MouseEvent): Unit = {
      setBackground(new Color(200, 200, 200))
    }

    override def mouseClicked(e: MouseEvent): Unit = {
      dismissItem()
    }

    override def mouseExited(e: MouseEvent): Unit = {
      setBackground(defaultWrapperColor())
    }

    override def mousePressed(e: MouseEvent): Unit = {
      setBackground(InterfaceColors.secondaryButtonBackgroundHover)
    }

    override def mouseReleased(e: MouseEvent): Unit = {
      setBackground(defaultWrapperColor())
    }

  })

  def setRemaining(num: Int): Unit = {
    complexXNum.setText(num.toString)
  }

  override def syncTheme(): Unit = {
    complexXNum.setForeground(InterfaceColors.widgetText)
    setBackground(defaultWrapperColor())
  }

  override protected def paintComponent(g: Graphics): Unit = {
    super.paintComponent(g)
    val g2 = g.asInstanceOf[Graphics2D]
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
    g2.setColor(getBackground())
    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 50, 50)
  }

}

private object Util {

  def modifyFont(elem: Component)(f: (Font) => Font): Unit = {
    elem.setFont(f(elem.getFont))
  }

  def handleUnderline(shouldUnderline: Boolean, c: Component): Unit = {

    import scala.collection.JavaConverters.mapAsJavaMapConverter

    val underlineValue = if (shouldUnderline) 1 else -1
    Util.modifyFont(c) {
      _.deriveFont(Map(TextAttribute.UNDERLINE -> Int.box(underlineValue)).asJava)
    }

  }

}
