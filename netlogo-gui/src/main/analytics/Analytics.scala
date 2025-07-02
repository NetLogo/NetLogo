// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.analytics

import java.net.URI

import org.matomo.java.tracking.{ MatomoException, MatomoRequest, MatomoRequests, MatomoTracker, TrackerConfiguration }

import org.nlogo.core.{ NetLogoPreferences, Token, TokenType }

import scala.concurrent.{ ExecutionContext, Future }

object Analytics {
  private var sendEnabled = true

  private val config = TrackerConfiguration.builder.apiEndpoint(URI.create("https://ccl.northwestern.edu:8080/matomo.php"))
                                                   .defaultSiteId(1)
                                                   .build()

  private val tracker = new MatomoTracker(config)

  private var startTime = 0L

  refreshPreference()

  def refreshPreference(): Unit = {
    sendEnabled = NetLogoPreferences.getBoolean("sendAnalytics", true)
  }

  private def wrapRequest(request: MatomoRequest, synchronous: Boolean = false): Unit = {
    if (sendEnabled) {
      if (synchronous) {
        try {
          tracker.sendBulkRequest(request)
        } catch {
          case e: MatomoException => println(e)
        }
      } else {
        tracker.sendBulkRequestAsync(request).handle((_, error) => {
          if (error != null)
            println(error)
        })
      }
    }
  }

  def appStart(is3D: Boolean): Unit = {
    startTime = System.currentTimeMillis

    wrapRequest(MatomoRequests.event("Desktop", "App Start", if (is3D) "3D" else "2D", null).build())

    if (System.getProperty("os.name").toLowerCase.contains("mac")) {
      if (System.getProperty("os.arch") == "aarch64") {
        wrapRequest(MatomoRequests.event("Desktop", "App Start Mac", "Silicon", null).build())
      } else {
        wrapRequest(MatomoRequests.event("Desktop", "App Start Mac", "Intel", null).build())
      }
    }
  }

  def appExit(): Unit = {
    val length = (System.currentTimeMillis - startTime) / 60000

    if (length > 0)
      wrapRequest(MatomoRequests.event("Desktop", "App Exit", null, length.toDouble).build(), true)
  }

  def themeChange(theme: String): Unit = {
    wrapRequest(MatomoRequests.event("Desktop", "Theme Change", theme, null).build())
  }

  // figure out how best to use this
  // def preferenceChange(preference: String): Unit = {
  //   wrapRequest(MatomoRequests.event("Desktop", "Preference Change", preference, null).build())
  // }

  def sdmOpen(): Unit = {
    wrapRequest(MatomoRequests.event("Desktop", "SDM Open", null, null).build())
  }

  def bspaceOpen(): Unit = {
    wrapRequest(MatomoRequests.event("Desktop", "BehaviorSpace Open", null, null).build())
  }

  def bspaceRun(): Unit = {
    wrapRequest(MatomoRequests.event("Desktop", "BehaviorSpace Run", null, null).build())
  }

  def threedViewOpen(): Unit = {
    wrapRequest(MatomoRequests.event("Desktop", "3D View Open", null, null).build())
  }

  def turtleShapeEditorOpen(): Unit = {
    wrapRequest(MatomoRequests.event("Desktop", "Turtle Shape Editor Open", null, null).build())
  }

  def turtleShapeEdit(): Unit = {
    wrapRequest(MatomoRequests.event("Desktop", "Turtle Shape Edit", null, null).build())
  }

  def linkShapeEditorOpen(): Unit = {
    wrapRequest(MatomoRequests.event("Desktop", "Link Shape Editor Open", null, null).build())
  }

  def linkShapeEdit(): Unit = {
    wrapRequest(MatomoRequests.event("Desktop", "Link Shape Edit", null, null).build())
  }

  def colorPickerOpen(): Unit = {
    wrapRequest(MatomoRequests.event("Desktop", "Color Picker Open", null, null).build())
  }

  def hubNetEditorOpen(): Unit = {
    wrapRequest(MatomoRequests.event("Desktop", "HubNet Editor Open", null, null).build())
  }

  def hubNetClientOpen(): Unit = {
    wrapRequest(MatomoRequests.event("Desktop", "HubNet Client Open", null, null).build())
  }

  def globalsMonitorOpen(): Unit = {
    wrapRequest(MatomoRequests.event("Desktop", "Globals Monitor Open", null, null).build())
  }

  def turtleMonitorOpen(): Unit = {
    wrapRequest(MatomoRequests.event("Desktop", "Turtle Monitor Open", null, null).build())
  }

  def patchMonitorOpen(): Unit = {
    wrapRequest(MatomoRequests.event("Desktop", "Patch Monitor Open", null, null).build())
  }

  def linkMonitorOpen(): Unit = {
    wrapRequest(MatomoRequests.event("Desktop", "Link Monitor Open", null, null).build())
  }

  def codeHash(hash: Int): Unit = {
    wrapRequest(MatomoRequests.event("Desktop", "Model Code Hash", hash.toString, null).build())
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

      if (prims.nonEmpty) {
        val json = s"{ ${prims.map((k, v) => s"\"$k\": $v").mkString(", ")} }"

        wrapRequest(MatomoRequests.event("Desktop", "Primitive Usage", json, null).build())
      }
    } (using ExecutionContext.global)
  }

  def loadOldSizeWidgets(widgets: Int): Unit = {
    if (widgets > 0)
      wrapRequest(MatomoRequests.event("Desktop", "Load Old Size Widgets", null, widgets.toDouble).build())
  }

  def loadExtension(extension: String): Unit = {
    wrapRequest(MatomoRequests.event("Desktop", "Load Extension", extension, null).build())
  }
}
