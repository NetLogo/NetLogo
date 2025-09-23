// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.analytics

import java.net.URI

import org.matomo.java.tracking.{ MatomoException, MatomoRequest, MatomoRequests, MatomoTracker, TrackerConfiguration }

import org.nlogo.core.{ NetLogoPreferences, Token, TokenType }

import scala.concurrent.{ ExecutionContext, Future }

object Analytics {
  private var sendEnabled = false

  private val category: String = {
    if (System.getProperty("org.nlogo.release") == "true") {
      "Desktop"
    } else {
      "Desktop (Devel)"
    }
  }

  private val config = TrackerConfiguration.builder
                                           .apiEndpoint(URI.create("https://ccl.northwestern.edu:8080/matomo.php"))
                                           .defaultSiteId(1)
                                           .build()

  private val tracker = new MatomoTracker(config)

  private var startTime = 0L

  private def wrapRequest(request: MatomoRequest, synchronous: Boolean = false): Unit = {
    if (sendEnabled) {
      if (synchronous) {
        try {
          new Thread {
            override def run(): Unit = {
              tracker.sendBulkRequest(request)
            }
          }.join(4000)
        } catch {
          case e: MatomoException => println(e)
          case _: InterruptedException =>
        }
      } else {
        tracker.sendBulkRequestAsync(request).handle((_, error) => {
          if (error != null)
            println(error)
        })
      }
    }
  }

  // non-recursively builds a simple subset of JSON to avoid unnecessary
  // dependencies or object structures (Isaac B 7/2/25)
  private def buildJson(properties: Map[String, Any]): String = {
    properties.map {
      case (key, value: String) => s"\"$key\": \"$value\""
      case (key, value: Double) => s"\"$key\": $value"
      case (key, value: Int) => s"\"$key\": $value"
      case (key, value: Boolean) => s"\"$key\": $value"
      case (key, value) => s"\"$key\": \"null\""
    }.mkString("{ ", ", ", " }")
  }

  def appStart(version: String, is3D: Boolean): Unit = {
    startTime = System.currentTimeMillis

    val json = buildJson(
      Map(
        "version" -> version,
        "threed" -> is3D,
        "os" -> System.getProperty("os.name"),
        "arch" -> System.getProperty("os.arch")
      )
    )

    wrapRequest(MatomoRequests.event(category, "App Start", json, null).build())
  }

  def appExit(): Unit = {
    val length = (System.currentTimeMillis - startTime) / 60000

    if (length > 0)
      wrapRequest(MatomoRequests.event(category, "App Exit", null, length.toDouble).build(), true)
  }

  def preferenceChange(name: String, value: String): Unit = {
    val json = buildJson(
      Map(
        "name" -> name,
        "value" -> value
      )
    )

    wrapRequest(MatomoRequests.event(category, "Preference Change", json, null).build())
  }

  def sdmOpen(): Unit = {
    wrapRequest(MatomoRequests.event(category, "SDM Open", null, null).build())
  }

  def bspaceOpen(): Unit = {
    wrapRequest(MatomoRequests.event(category, "BehaviorSpace Open", null, null).build())
  }

  def bspaceRun(): Unit = {
    wrapRequest(MatomoRequests.event(category, "BehaviorSpace Run", null, null).build())
  }

  def threedViewOpen(): Unit = {
    wrapRequest(MatomoRequests.event(category, "3D View Open", null, null).build())
  }

  def turtleShapeEditorOpen(): Unit = {
    wrapRequest(MatomoRequests.event(category, "Turtle Shape Editor Open", null, null).build())
  }

  def turtleShapeEdit(): Unit = {
    wrapRequest(MatomoRequests.event(category, "Turtle Shape Edit", null, null).build())
  }

  def linkShapeEditorOpen(): Unit = {
    wrapRequest(MatomoRequests.event(category, "Link Shape Editor Open", null, null).build())
  }

  def linkShapeEdit(): Unit = {
    wrapRequest(MatomoRequests.event(category, "Link Shape Edit", null, null).build())
  }

  def colorPickerOpen(): Unit = {
    wrapRequest(MatomoRequests.event(category, "Color Picker Open", null, null).build())
  }

  def hubNetEditorOpen(): Unit = {
    wrapRequest(MatomoRequests.event(category, "HubNet Editor Open", null, null).build())
  }

  def hubNetClientOpen(): Unit = {
    wrapRequest(MatomoRequests.event(category, "HubNet Client Open", null, null).build())
  }

  def globalsMonitorOpen(): Unit = {
    wrapRequest(MatomoRequests.event(category, "Globals Monitor Open", null, null).build())
  }

  def turtleMonitorOpen(): Unit = {
    wrapRequest(MatomoRequests.event(category, "Turtle Monitor Open", null, null).build())
  }

  def patchMonitorOpen(): Unit = {
    wrapRequest(MatomoRequests.event(category, "Patch Monitor Open", null, null).build())
  }

  def linkMonitorOpen(): Unit = {
    wrapRequest(MatomoRequests.event(category, "Link Monitor Open", null, null).build())
  }

  def codeHash(hash: Int): Unit = {
    wrapRequest(MatomoRequests.event(category, "Model Code Hash", hash.toString, null).build())
  }

  def primUsage(tokens: Iterator[Token]): Unit = {
    Future {
      val prims = tokens.foldLeft(Map[String, Int]()) {
        case (map, token) =>
          if (token.tpe == TokenType.Command || token.tpe == TokenType.Reporter) {
            val name = token.text.toLowerCase

            map + ((name, map.getOrElse(name, 0) + 1))
          } else {
            map
          }
      }

      if (prims.nonEmpty)
        wrapRequest(MatomoRequests.event(category, "Primitive Usage", buildJson(prims), null).build())
    } (using ExecutionContext.global)
  }

  def keywordUsage(tokens: Iterator[Token]): Unit = {
    Future {
      val keywords = tokens.foldLeft(Map[String, Int]()) {
        case (map, token) =>
          if (token.tpe == TokenType.Keyword) {
            val name = token.text.toLowerCase

            map + ((name, map.getOrElse(name, 0) + 1))
          } else {
            map
          }
      }

      if (keywords.nonEmpty)
        wrapRequest(MatomoRequests.event(category, "Keyword Usage", buildJson(keywords), null).build())
    } (using ExecutionContext.global)
  }

  def includeExtensions(extensions: Array[String]): Unit = {
    extensions.foreach { extension =>
      wrapRequest(MatomoRequests.event(category, "Include Extension", extension, null).build())
    }
  }

  def loadOldSizeWidgets(widgets: Int): Unit = {
    if (widgets > 0)
      wrapRequest(MatomoRequests.event(category, "Load Old Size Widgets", null, widgets.toDouble).build())
  }

  def modelingCommonsOpen(): Unit = {
    wrapRequest(MatomoRequests.event(category, "Modeling Commons Open", null, null).build())
  }

  def modelingCommonsUpload(): Unit = {
    wrapRequest(MatomoRequests.event(category, "Modeling Commons Upload", null, null).build())
  }

  def saveAsNetLogoWeb(): Unit = {
    wrapRequest(MatomoRequests.event(category, "Save as NetLogo Web", null, null).build())
  }

  def refreshPreference(): Unit = {
    sendEnabled = NetLogoPreferences.getBoolean("sendAnalytics", false)
  }
}
