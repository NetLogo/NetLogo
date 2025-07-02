// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.analytics

import java.net.URI

import org.matomo.java.tracking.{ MatomoRequest, MatomoRequests, MatomoTracker, TrackerConfiguration }

import org.nlogo.core.NetLogoPreferences

object Analytics {
  private var sendEnabled = true

  private val config = TrackerConfiguration.builder.apiEndpoint(URI.create("https://ccl.northwestern.edu:8080/matomo.php"))
                                                   .defaultSiteId(1)
                                                   .build()

  private val tracker = new MatomoTracker(config)

  refreshPreference()

  def refreshPreference(): Unit = {
    sendEnabled = NetLogoPreferences.getBoolean("sendAnalytics", true)
  }

  private def wrapRequest(request: MatomoRequest): Unit = {
    if (sendEnabled) {
      tracker.sendBulkRequestAsync(request).handle((_, error) => {
        if (error != null)
          println(error)
      })
    }
  }

  def appStart(): Unit = {
    wrapRequest(MatomoRequests.event("Desktop", "App Start", null, null).build())
  }

  def themeChange(theme: String): Unit = {
    wrapRequest(MatomoRequests.event("Desktop", "Theme Change", theme, null).build())
  }
}
