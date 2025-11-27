// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.util

object PathUtils {
  private val isWindows = System.getProperty("os.name").toLowerCase.startsWith("win")

  // replace single backslashes (eg. Windows path separator) with forward slashes, making sure
  // not to replace valid escaped backslashes (Isaac B 11/26/25)
  def standardize(path: String): String = {
    if (isWindows) {
      path.replace("\\", "/")
    } else {
      path
    }
  }
}
