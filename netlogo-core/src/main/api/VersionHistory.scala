// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

object VersionHistory {
  def olderThan40beta2(version: String) =
    Version.numericValue(version) < Version.numericValue("NetLogo 4.0beta2")

  def olderThan42pre2(version: String) =
    Version.numericValue(version) < Version.numericValue("NetLogo 4.2pre2")
}
