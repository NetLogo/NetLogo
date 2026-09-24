// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.interfacetab

import java.awt.{ Color, Component, Dimension, Font }
import java.awt.event.{ ActionEvent, ActionListener, MouseEvent, MouseAdapter }
import java.awt.font.TextAttribute
import java.net.URI
import java.time.format.{ DateTimeFormatter, FormatStyle }
import javax.swing.{ Box, JButton, JLabel }
import javax.swing.border.MatteBorder

import org.nlogo.analytics.Analytics
import org.nlogo.api.{ Advisory, Announcement, Event, Release }
import org.nlogo.core.NetLogoPreferences
import org.nlogo.swing.{ BoxColumn, BoxRow, BrowserLauncher, MouseUtils, PreferredSize, RoundedBorderPanel, Utils,
                         Zoomable, ZoomableBorder }
import org.nlogo.theme.{ InterfaceColors, ThemeSync }

class AnnouncementBanner extends BoxRow with MouseUtils with ThemeSync {

  private val prefKey = "announce.latest-read-id"

  private var announcements = Seq[Announcement]()

  private val annTitle = new JLabel with Zoomable {
    setBaseFont(getFont.deriveFont(18f))
  }

  private val annText = new JLabel with Zoomable {
    setBaseFont(getFont.deriveFont(14f))
  }

  private val textPane = new TextPane(annTitle, annText)

  private val ggGoNext = () => {
    NetLogoPreferences.put(prefKey, announcements.head.id.toString)
    announcements = announcements.tail
    renderData()
  }

  private val simpleXButton   = new XButton(ggGoNext) {
    setVisible(false)
  }

  private val complexXWrapper = new ComplexXWrapper(ggGoNext)

  setHandCursor()
  setBorder(new MatteBorder(1, 0, 1, 0, InterfaceColors.viewBorder()))
  setOpaque(true)

  addMouseListener(new MouseAdapter() {

    override def mouseClicked(e: MouseEvent): Unit = {
      announcements.headOption.foreach {
        ann =>
          Analytics.announcementBannerClicked(ann.id)
          BrowserLauncher.openURI(new URI(s"https://www.netlogo.org/announcements#news-item-${ann.id}"))
          ggGoNext()
      }
    }

    override def mouseEntered(e: MouseEvent): Unit = {
      setSummaryUnderline(true)
    }

    override def mouseExited(e: MouseEvent): Unit = {
      setSummaryUnderline(false)
    }

  })

  add(new BoxRow(Seq(textPane, Box.createHorizontalGlue, simpleXButton, complexXWrapper), 20) {
    setBorder(new ZoomableBorder(0, 20, 0, 22))
  })

  syncTheme()

  def appendData(anns: Seq[Announcement]): Unit = {
    val isDebug      = NetLogoPreferences.get("announce.debug", "false") == "true"
    val latestReadID = if (!isDebug) NetLogoPreferences.get(prefKey, "-1").toInt else -1
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
        annTitle.setBaseFont(annTitle.getBaseFont.deriveFont(Font.BOLD))
        annText.setText(summary.replaceAll("\n", " ").replaceAll("  ", " "))
        setVisible(true)

    }

  }

  override def getPreferredSize: Dimension =
    new Dimension(super.getPreferredSize.width, zoom(66))

  override def getMaximumSize: Dimension =
    new Dimension(Int.MaxValue, zoom(66))

  override def syncTheme(): Unit = {
    renderData()
    textPane.syncTheme()
    complexXWrapper.syncTheme()
  }

}

private class TextPane(title: Component, text: Component) extends BoxColumn(Seq(title, text)) with ThemeSync {

  override def syncTheme(): Unit = {
    title.setForeground(InterfaceColors.widgetText())
    text .setForeground(InterfaceColors.widgetText())
  }

}

private class XButton(dismissItem: () => Unit) extends JButton with MouseUtils with Zoomable {

  private def defaultXColor() = InterfaceColors.announceX()

  private val setXColor = (color: () => Color) => {
    setIcon(Utils.iconScaledWithColor(this, "/images/close-light.png", 15, 15, color))
  }

  setBorderPainted(false)
  setContentAreaFilled(false)
  setOpaque(false)

  addActionListener(
    new ActionListener() {
      override def actionPerformed(e: ActionEvent): Unit = {
        dismissItem()
      }
    }
  )

  addMouseListener(new MouseAdapter() {

    override def mouseEntered(e: MouseEvent): Unit = {
      setXColor(InterfaceColors.announceXHovered)
    }

    override def mouseExited(e: MouseEvent): Unit = {
      setXColor(defaultXColor)
    }

    override def mousePressed(e: MouseEvent): Unit = {
      setXColor(InterfaceColors.announceXPressed)
    }

    override def mouseReleased(e: MouseEvent): Unit = {
      setXColor(defaultXColor)
    }

  })

}

private class ComplexXWrapper(dismissItem: () => Unit)
  extends BoxRow with RoundedBorderPanel with PreferredSize with ThemeSync {

  private def defaultWrapperColor() = InterfaceColors.scrollBarBackground()

  private val complexX = new JLabel with Zoomable {
    setIcon(Utils.iconScaledWithColor(this, "/images/chevron-right.png", 10, 10, () => InterfaceColors.announceX()))
  }

  private val complexXNum = new JLabel with Zoomable {
    setBorder(new ZoomableBorder(0, 0, 0, 3))
    setBaseFont(getFont.deriveFont(14f))
  }

  setBorder(new ZoomableBorder(0, 20, 0, 20))
  setVisible(false)
  setDiameter(40)
  setBorderColor(InterfaceColors.Transparent)

  add(complexXNum)
  add(complexX)

  syncTheme()

  addMouseListener(new MouseAdapter() {

    override def mouseEntered(e: MouseEvent): Unit = {
      setBackgroundColor(new Color(200, 200, 200))
    }

    override def mouseClicked(e: MouseEvent): Unit = {
      dismissItem()
    }

    override def mouseExited(e: MouseEvent): Unit = {
      setBackgroundColor(defaultWrapperColor())
    }

    override def mousePressed(e: MouseEvent): Unit = {
      setBackgroundColor(InterfaceColors.announceXHovered())
    }

    override def mouseReleased(e: MouseEvent): Unit = {
      setBackgroundColor(defaultWrapperColor())
    }

  })

  def setRemaining(num: Int): Unit = {
    complexXNum.setText(num.toString)
  }

  override def getPreferredSize: Dimension =
    new Dimension(super.getPreferredSize.width, zoom(40))

  override def syncTheme(): Unit = {
    complexXNum.setForeground(InterfaceColors.widgetText())
    setBackgroundColor(defaultWrapperColor())
  }

}

private object Util {

  def handleUnderline(shouldUnderline: Boolean, c: Zoomable): Unit = {

    import scala.jdk.CollectionConverters.MapHasAsJava

    val underlineValue = if (shouldUnderline) 1 else -1

    c.setBaseFont(c.getBaseFont.deriveFont(Map(TextAttribute.UNDERLINE -> Int.box(underlineValue)).asJava))

  }

}
