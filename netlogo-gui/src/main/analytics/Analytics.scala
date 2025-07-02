// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.analytics

import java.net.URI

import org.matomo.java.tracking.{ MatomoRequest, MatomoTracker, TrackerConfiguration }

import org.nlogo.core.NetLogoPreferences

object Analytics {
  private val config = TrackerConfiguration.builder.apiEndpoint(URI.create("https://ccl.northwestern.edu:8080/matomo.php"))
                                                   .defaultSiteId(1)
                                                   .build()

  private val tracker = new MatomoTracker(config)

  private def wrapRequest(request: MatomoRequest): Unit = {
    if (NetLogoPreferences.getBoolean("sendAnalytics", true)) {
      tracker.sendBulkRequestAsync(request).handle((_, error) => {
        if (error != null)
          println(error)
      })
    }
  }

  def appStart(): Unit = {
    wrapRequest(MatomoRequest.request.actionName("App Start").build())
  }
}
