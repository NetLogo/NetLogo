// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app
import com.vladsch.flexmark.html.HtmlRenderer
import com.vladsch.flexmark.parser.Parser

import java.awt.{BorderLayout, Dimension, Graphics}
import java.awt.event.{ActionEvent, ActionListener, MouseAdapter, MouseEvent}
import java.util.prefs.Preferences
import javax.swing.{JButton, JEditorPane, JLabel, JOptionPane, JPanel, JScrollPane, SwingConstants}

import org.nlogo.app.infotab.InfoFormatter
import org.nlogo.core.I18N
import org.nlogo.swing.HoverDecoration
import org.nlogo.theme.{InterfaceColors, ThemeSync}

import org.json.simple.parser.JSONParser
import org.json.simple.{JSONArray, JSONObject}

import scala.io.Source


case class JsonObject(eventId: Int, date: String, title: String, fullText: String)

class NotificationBanner() extends JPanel with ThemeSync with HoverDecoration {
  private var jsonObjectList: List[JsonObject] = List() // Initialize here first
  private val jsonUrl = "https://ccl.northwestern.edu/netlogo/announce-test.json"
  // Fetch and populate jsonObjectList in the constructor
  jsonObjectList = parseJsonToList(fetchJsonFromUrl())

  private val initialMessage = getJsonObjectHead.getOrElse("")
  lazy val lastSeenEventIdKey: String = "lastSeenEventId" // The key for the most recently seen event-id
  private var editorPane: JEditorPane = new JEditorPane()
  // Label to display notification messages
  private val messageLabel = new JLabel(" " + initialMessage + "   -  " + I18N.gui.get("dialog.interface.viewMore"))
  // Close button with an "X" icon
  private val closeButton = new JButton("\u2716") // Unicode character for "X"
  setVisible(isShowNeeded()) // Set visibility based on isShowNeeded
  // Initialize the NotificationBanner panel and set layout and appearance
  setLayout(new BorderLayout())
  setPreferredSize(new Dimension(400, 50))

  // Customize the message label
  messageLabel.setHorizontalAlignment(SwingConstants.LEFT)
  add(messageLabel, BorderLayout.CENTER)

  // Configure the close button
  closeButton.setBorderPainted(false)
  closeButton.setContentAreaFilled(false)
  closeButton.setFocusPainted(false)
  //TODO this action needs to get called on the okay button click.
  closeButton.addActionListener(new ActionListener {
    override def actionPerformed(e: ActionEvent): Unit = {
      setVisible(false) // Hide the banner when the close button is pressed
    }
  })
  add(closeButton, BorderLayout.EAST)

  // Add a mouse listener to call showJsonInDialog when the banner is clicked
  addMouseListener(new MouseAdapter {
    override def mouseClicked(e: MouseEvent): Unit = {
      showJsonInDialog() // Call the method on click
    }
  })

  override def paintComponent(g: Graphics) {
    if (isHover)
      setBackground(InterfaceColors.ANNOUNCEMENTS_BANNER_BACKGROUND_HOVER)
    else
      setBackground(InterfaceColors.ANNOUNCEMENTS_BANNER_BACKGROUND)

    super.paintComponent(g)
  }
  def syncTheme(): Unit = {
    setBackground(InterfaceColors.ANNOUNCEMENTS_BANNER_BACKGROUND)
    messageLabel.setForeground(InterfaceColors.ANNOUNCEMENTS_BANNER_TEXT)
    closeButton.setForeground(InterfaceColors.ANNOUNCEMENTS_BANNER_TEXT)
  }

  // Method to fetch JSON content from a URL
  private def fetchJsonFromUrl(): String = {
    try {
      val source = Source.fromURL(jsonUrl)
      val content = source.mkString
      source.close()
      content
    } catch {
      case e: Exception =>
      ""
    }
  }

  // Method to parse JSON content to a list of JsonObject instances
  private def parseJsonToList(jsonContent: String): List[JsonObject] = {
    if(jsonContent.isEmpty){
      return Nil
    }

      val parser = new JSONParser()
      val jsonArray = parser.parse(jsonContent).asInstanceOf[JSONArray]
      jsonArray.toArray.flatMap { obj =>
        val jsonObject = obj.asInstanceOf[JSONObject]
        Option(jsonObject.get("event-id").asInstanceOf[Long].toInt).map { eventId =>
          val title = Option(jsonObject.get("title")).map(_.toString).getOrElse("")
          val fullText = Option(jsonObject.get("fullText")).map(_.toString).getOrElse("")
          val date = Option(jsonObject.get("date")).map(_.toString).getOrElse("")
          JsonObject(eventId, date, title, fullText)
        }
      }.toList.sortBy(_.eventId)(Ordering[Int].reverse)

  }

  // Method to show JSON content in a dialog
  private def showJsonInDialog(): Unit = {

    val prefs = Preferences.userNodeForPackage(getClass)
    try {
      val jsonContent = fetchJsonFromUrl()
      jsonObjectList = parseJsonToList(jsonContent) // Populate the class variable
      val formattedString = formatJsonObjectList(jsonObjectList)
      if(!isShowNeeded()){
        return
      }
      val lastSeenEventId = prefs.getInt(lastSeenEventIdKey, -1); // Returns -1 if "event-id" is not found
      if(jsonObjectList.head.eventId > lastSeenEventId){
        println(s"Show this ${jsonObjectList.head.eventId}")
      }
      else {
        println(s"Don't show this  ${jsonObjectList.head.eventId}")
        return
      }
      val html = InfoFormatter.toInnerHtml(formattedString)

      if (!jsonContent.trim.isEmpty) {
          editorPane = new JEditorPane {
            self =>

            setDragEnabled(false)
            setEditable(false)
            setContentType("text/html")
            setOpaque(true)
            setBackground(InterfaceColors.CODE_BACKGROUND) // Set the background color)
            setForeground(InterfaceColors.DEFAULT_COLOR) //Set the font color
            setText(html)
            setCaretPosition(0)
          }

          val scrollPane = new JScrollPane(editorPane)
          scrollPane.setPreferredSize(new Dimension(500, 400))
          val panel = new JPanel(new BorderLayout())
          panel.add(scrollPane, BorderLayout.CENTER)
          val options: Array[AnyRef] = Array(I18N.gui.get("common.buttons.ok"))

          val result = JOptionPane.showOptionDialog(
            null, panel, I18N.gui.get("dialog.interface.newsNotificationTitle"),
            JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options(0)
          )

          // Check if OK was clicked (index 0 in options array) and hide the NotificationBanner
          if (result == 0) {
            setVisible(false) // Hide NotificationBanner
            prefs.putInt("lastSeenEventId", jsonObjectList.head.eventId)
          }
      }
    } catch {
      case e: Exception =>{
        org.nlogo.swing.OptionDialog.showCustom(this, I18N.gui.get("error.dialog.unknown"), e.getMessage, null)
      }
    }
  }


  // Lazily initialize the Markdown parser and renderer (can be reused for multiple calls)
  private lazy val markdownParser = Parser.builder().build()
  private lazy val htmlRenderer = HtmlRenderer.builder().build()

  private def formatJsonObjectList(jsonObjectList: List[JsonObject]): String = {
    jsonObjectList.map { obj =>
      // Convert fullText from Markdown to HTML
      val fullTextHtml = htmlRenderer.render(markdownParser.parse(obj.fullText))

      // Format title, date, and the converted HTML fullText
      s"<h3>* ${I18N.gui.get("dialog.interface.update")}: ${obj.date} -- ${obj.title}</h3>" +
        s"$fullTextHtml"
    }.mkString("<html>", "", "</html>")
  }

  private def getJsonObjectHead: Option[String] = {
      val jsonContent = fetchJsonFromUrl()
      jsonObjectList = parseJsonToList(jsonContent)

    // Return the title of the first item if jsonObjectList is not null and has elements
    jsonObjectList match {
      case Nil =>
        println("jsonObjectList is empty or Nil; hiding NotificationBanner.")
        this.setVisible(false) // Hide the NotificationBanner panel
        None // Return None if jsonObjectList is null or empty
      case list =>
        this.setVisible(true) // Ensure the NotificationBanner is visible
        list.headOption.map(_.title) // Return the title of the head element if available
    }
  }

  private def isShowNeeded(): Boolean = {
    val prefs = Preferences.userNodeForPackage(getClass)
    val jsonContent = fetchJsonFromUrl()
    jsonObjectList = parseJsonToList(jsonContent) // Populate the class variable
    val lastSeenEventId = prefs.getInt(lastSeenEventIdKey, -1); // Returns -1 if "event-id" is not found
    //return true if jsonObjectList is non-empty and eventId of the first element is> lastSeenEventId; otherwise return false
    jsonObjectList.nonEmpty && jsonObjectList.head.eventId > lastSeenEventId
  }
}
