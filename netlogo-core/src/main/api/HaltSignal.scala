// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

trait HaltSignal {
  // haltAll indicates that the halt is global (all jobs) as opposed to local
  // (the job raising the HaltSignal)
  def haltAll: Boolean
}
