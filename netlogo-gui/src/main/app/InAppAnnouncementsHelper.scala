package org.nlogo.app

import org.json.simple.parser.{JSONParser, ParseException}
import org.json.simple.{JSONArray, JSONObject}
import org.nlogo.app.common.FindDialog
import org.nlogo.app.infotab.InfoFormatter
import com.vladsch.flexmark.html.HtmlRenderer
import com.vladsch.flexmark.parser.Parser


import java.awt._
import javax.swing._
import java.util.prefs.{Preferences => JPreferences}
import org.nlogo.core.I18N

import java.awt.event.{FocusEvent, FocusListener}
import scala.io.Source
import scala.collection.immutable.List

// JsonObject case class
case class JsonObject(eventId: Int, date: String, title: String, fullText: String)

object InAppAnnouncementsHelper {
  val lastSeenEventIdKey: String = "lastSeenEventId"  // The key for the most recently seen event-id
  private val JsonUrl = "https://ccl.northwestern.edu/netlogo/announce-test.json"

  // Class variable to store jsonObjectList
  private var jsonObjectList: List[JsonObject] = List()

  // Method to parse JSON content to a list of JsonObject instances
  def parseJsonToList(jsonContent: String): List[JsonObject] = {
    try {
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
    } catch {
      case e: ParseException =>
        throw new Exception(s"Error parsing JSON: ${e.getMessage}", e)
      case e: ClassCastException =>
        throw new Exception(s"Error casting JSON objects: ${e.getMessage}", e)
      case e: Exception =>
        throw new Exception(s"General error: ${e.getMessage}", e)
    }
  }

  // Method to fetch JSON content from a URL
  def fetchJsonFromUrl(url: String): String = {
    try {
      val source = Source.fromURL(url)
      val content = source.mkString
      source.close()
      content
    } catch {
      case e: Exception =>
        throw new Exception(s"Error fetching JSON from URL: ${e.getMessage}", e)
    }
  }

  // Method to show JSON content in a dialog
  def showJsonInDialog(): Unit = {

    val prefs = JPreferences.userNodeForPackage(getClass)
    try {
      val jsonContent = fetchJsonFromUrl(JsonUrl)
      jsonObjectList = parseJsonToList(jsonContent) // Populate the class variable
      val formattedString = formatJsonObjectList(jsonObjectList)
      val lastSeenEventId = prefs.getInt(lastSeenEventIdKey, -1)
      println(s"lastSeenEventId: $lastSeenEventId, head title: ${getJsonObjectHead.getOrElse("None")}")

      val html = InfoFormatter.toInnerHtml(formattedString)
      println("voila:  "+ formattedString)
      if (!jsonContent.trim.isEmpty) {
        SwingUtilities.invokeLater(() => {
          val editorPane: JEditorPane = new JEditorPane { self =>
            addFocusListener(new FocusListener {
              def focusGained(fe: FocusEvent): Unit = { FindDialog.watch(self) }
              def focusLost(fe: FocusEvent): Unit = { if (!fe.isTemporary) FindDialog.dontWatch(self) }
            })
            setDragEnabled(false)
            setEditable(false)
            setContentType("text/html")
            setOpaque(false)
            setText(html)
            setCaretPosition(0)
          }

          val scrollPane = new JScrollPane(editorPane)
          scrollPane.setPreferredSize(new Dimension(500, 400))
          val checkbox = new JCheckBox(I18N.gui.get("dialog.interface.newsNotificationDoNotShowAgain"))
          val panel = new JPanel(new BorderLayout())
          panel.add(scrollPane, BorderLayout.CENTER)
          panel.add(checkbox, BorderLayout.SOUTH)
          val options: Array[AnyRef] = Array(I18N.gui.get("common.buttons.ok"))

          JOptionPane.showOptionDialog(
            null, panel, I18N.gui.get("dialog.interface.newsNotificationTitle"),
            JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options(0)
          )

          if (checkbox.isSelected) {
            println(s"Do not show again: ${checkbox.isSelected}")
            prefs.putInt("lastSeenEventId", jsonObjectList.head.eventId)
          }
        })
      }
    } catch {
      case e: Exception => throw new Exception(s"Error in showJsonInDialog: ${e.getMessage}", e)
    }
  }


  // Initialize the Markdown parser and renderer (can be reused for multiple calls)
  val markdownParser = Parser.builder().build()
  val htmlRenderer = HtmlRenderer.builder().build()

  def formatJsonObjectList(jsonObjectList: List[JsonObject]): String = {
    jsonObjectList.map { obj =>
      // Convert fullText from Markdown to HTML
      val fullTextHtml = htmlRenderer.render(markdownParser.parse(obj.fullText))

      // Format title, date, and the converted HTML fullText
      s"<h3>* ${I18N.gui.get("dialog.interface.update")}: ${obj.date} -- ${obj.title}</h3>" +
        s"$fullTextHtml"
//        s"<p>$fullTextHtml</p>"
    }.mkString("<html>", "", "</html>")
  }
//  def formatJsonObjectList(jsonObjects: List[JsonObject]): String = {
//    jsonObjects.map { obj =>
//      s"* ${I18N.gui.get("dialog.interface.update")}: ${obj.date}\n  ${obj.fullText}"
//    }.mkString("\n\n")
//  }
//def formatJsonObjectList(jsonObjectList: List[JsonObject]): String = {
//  jsonObjectList.map { obj =>
//    s"<h3>* ${I18N.gui.get("dialog.interface.update")}: ${obj.date} -- ${obj.title}</h3>" +
//      s"<p>${obj.fullText}</p>"
//  }.mkString("<html>", "<br><br>", "</html>")
//}

  // Static method to get the title of the head of jsonObjectList, with lazy initialization
  def getJsonObjectHead: Option[String] = {
    // Check if jsonObjectList is empty, and fetch the JSON if needed
    if (jsonObjectList.isEmpty) {
      val jsonContent = fetchJsonFromUrl(JsonUrl) // Use the static URL here
      jsonObjectList = parseJsonToList(jsonContent)
    }

    // Return the fullText of the first item, if available
    jsonObjectList.headOption.map(_.title)
  }
}