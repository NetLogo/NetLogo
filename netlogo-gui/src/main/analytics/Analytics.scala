// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.analytics

import java.net.URI

import org.matomo.java.tracking.{ MatomoRequest, MatomoTracker, TrackerConfiguration }

object Analytics {
  private val config = TrackerConfiguration.builder.apiEndpoint(URI.create("http://129.105.119.160:8080/matomo.php"))
                                                   .defaultSiteId(1)
                                                   .build()

  private val tracker = new MatomoTracker(config)

  def appStart(): Unit = {
    tracker.sendBulkRequestAsync(MatomoRequest.request.actionName("App Start").build())
  }
}
