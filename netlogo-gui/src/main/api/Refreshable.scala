// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

/** Marker trait used to indicate that the value in this class may be stale and in need of refresh */

trait Refreshable {
  def refresh(): Unit
}
