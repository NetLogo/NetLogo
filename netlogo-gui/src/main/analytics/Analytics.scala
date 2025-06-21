// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.analytics

import java.net.URI

import org.matomo.java.tracking.{ MatomoRequest, MatomoTracker, TrackerConfiguration }

object Analytics {
  // this endpoint is temporary for local testing purposes (Isaac B 6/20/25)
  private val config = TrackerConfiguration.builder.apiEndpoint(URI.create("http://localhost:8080/matomo.php"))
                                                   .defaultSiteId(1)
                                                   .build()

  private val tracker = new MatomoTracker(config)

  def appStart(): Unit = {
    tracker.sendBulkRequestAsync(MatomoRequest.request.actionName("App Start").build())
  }
}
