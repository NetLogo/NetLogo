// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.util

object PathUtils {
  // no-op because file paths are irrelevant in NetLogo Web (Isaac B 11/30/25)
  def standardize(path: String): String =
    path
}
