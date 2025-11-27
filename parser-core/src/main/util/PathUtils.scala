// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.util

object PathUtils {
  // replace single backslashes (eg. Windows path separator) with forward slashes, making sure
  // not to replace valid escaped backslashes (Isaac B 11/26/25)
  def standardize(path: String): String = {
    """(\\{1,2})+""".r.replaceAllIn(path, _.subgroups.fold("") {
      case (acc, "\\") =>
        acc + "/"

      case (acc, str) =>
        acc + str
    })
  }
}
