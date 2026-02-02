// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.analytics

import scala.concurrent.{ ExecutionContext, Future }

import org.nlogo.core.{ Token, TokenType }

import AnalyticsEventType._

object Analytics {

  private given ExecutionContext = ExecutionContext.global

  private var startTime = 0L

  def appStart(version: String, is3D: Boolean): Unit = {
    startTime = System.currentTimeMillis()
    val payload =
      Map(
        "version" -> version
      , "is3D"    -> is3D
      , "os"      -> System.getProperty("os.name")
      , "arch"    -> System.getProperty("os.arch")
      )
    AnalyticsSender(AppStart, payload)
  }

  def appExit(): Future[Unit] = {
    val length = (System.currentTimeMillis() - startTime) / 60000d
    AnalyticsSender(AppExit, Map("appMinutes" -> length.round.toInt)).map {
      _ => AnalyticsSender.shutdown()
    }
  }

  def preferenceChange(name: String, origValue: String): Unit = {
    val value   = if (name == "logDirectory") "" else origValue
    val payload = Map("name"  -> name, "value" -> value)
    AnalyticsSender(PreferenceChange, payload)
  }

  def sdmOpen(): Unit = {
    AnalyticsSender(SDMOpen)
  }

  def bspaceOpen(): Unit = {
    AnalyticsSender(BehaviorSpaceOpen)
  }

  def bspaceRun(table: String, spreadsheet: String, stats: String, lists: String): Unit = {
    val payload =
      Map(
        "usedTable"       -> table
      , "usedSpreadsheet" -> spreadsheet
      , "usedStats"       -> stats
      , "usedLists"       -> lists
      ).view
       .mapValues(_.trim.nonEmpty)
       .toMap
    AnalyticsSender(BehaviorSpaceRun, payload)
  }

  def threedViewOpen(): Unit = {
    AnalyticsSender(Open3DView)
  }

  def turtleShapeEditorOpen(): Unit = {
    AnalyticsSender(TurtleShapeEditorOpen)
  }

  def turtleShapeEdit(): Unit = {
    AnalyticsSender(TurtleShapeEdit)
  }

  def linkShapeEditorOpen(): Unit = {
    AnalyticsSender(LinkShapeEditorOpen)
  }

  def linkShapeEdit(): Unit = {
    AnalyticsSender(LinkShapeEdit)
  }

  def colorPickerOpen(): Unit = {
    AnalyticsSender(ColorPickerOpen)
  }

  def hubNetEditorOpen(): Unit = {
    AnalyticsSender(HubNetEditorOpen)
  }

  def hubNetClientOpen(): Unit = {
    AnalyticsSender(HubNetClientOpen)
  }

  def globalsMonitorOpen(): Unit = {
    AnalyticsSender(GlobalsMonitorOpen)
  }

  def turtleMonitorOpen(): Unit = {
    AnalyticsSender(TurtleMonitorOpen)
  }

  def patchMonitorOpen(): Unit = {
    AnalyticsSender(PatchMonitorOpen)
  }

  def linkMonitorOpen(): Unit = {
    AnalyticsSender(LinkMonitorOpen)
  }

  def codeHash(hash: Int): Unit = {
    AnalyticsSender(ModelCodeHash, Map("hash" -> hash))
  }

  def primUsage(tokens: Iterator[Token]): Unit = {
    Future {
      tokens.foldLeft(Map[String, Int]()) {
        case (map, token) =>
          if (token.tpe == TokenType.Command || token.tpe == TokenType.Reporter) {
            val name = token.text.toLowerCase
            map + ((name, map.getOrElse(name, 0) + 1))
          } else {
            map
          }
      }
    }.filter(_.nonEmpty).flatMap(AnalyticsSender(PrimitiveUsage, _))
  }

  def keywordUsage(tokens: Iterator[Token]): Unit = {
    Future {
      tokens.foldLeft(Map[String, Int]()) {
        case (map, token) =>
          if (token.tpe == TokenType.Keyword) {
            val name = token.text.toLowerCase
            map + ((name, map.getOrElse(name, 0) + 1))
          } else {
            map
          }
      }
    }.filter(_.nonEmpty)
     .flatMap(AnalyticsSender(KeywordUsage, _))
  }

  def includeExtensions(extensions: Array[String]): Unit = {
    extensions.foreach {
      extension =>
        AnalyticsSender(IncludeExtension, Map("name" -> extension))
    }
  }

  def loadOldSizeWidgets(widgets: Int): Unit = {
    if (widgets > 0) {
      AnalyticsSender(LoadOldSizeWidgets, Map("numWidgets" -> widgets.toDouble))
    }
  }

  def modelingCommonsOpen(): Unit = {
    AnalyticsSender(ModelingCommonsOpen)
  }

  def modelingCommonsUpload(): Unit = {
    AnalyticsSender(ModelingCommonsUpload)
  }

  def saveAsNetLogoWeb(): Unit = {
    AnalyticsSender(SaveAsNetLogoWeb)
  }

  def previewCommandsOpen(): Unit = {
    AnalyticsSender(PreviewCommandsOpen)
  }

  def refreshPreference(): Unit = {
    AnalyticsSender.refreshPreference()
  }

}
