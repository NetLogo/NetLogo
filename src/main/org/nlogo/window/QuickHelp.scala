// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import org.nlogo.util.Utils.getResourceLines

/**
 * This provides context-sensitive help in the procedures tab.  Based on what the user's cursor is
 * on, it should jump to the proper documentation, found in docs/dict/ -- which is just a decomposed
 * version of docs/dictionary.html
 *
 * Maybe it would be best to pull everything out of some new file, maybe xml format, and we could
 * autogenerate the primitives dictionary file from that.
 *
 * ~Forrest (4/26/2007)
 */
object QuickHelp {

  def doHelp(token: String, is3D: Boolean,
             open: String => Unit,
             confirmOpen: () => Boolean) {
    val canonical = token.toLowerCase
    if (is3D && words3D.isDefinedAt(canonical))
      open("docs/dict/" + words3D(canonical))
    else if (words2D.isDefinedAt(canonical))
      open("docs/dict/" + words2D(canonical))
    else if (confirmOpen())
      open("docs/index2.html")
  }

  // entries in 3D dictionary overrides 2D entries ev 10/25/07
  private lazy val words2D = loadHelp("/system/dict.txt")
  private lazy val words3D = loadHelp("/system/dict3d.txt")

  private def loadHelp(path: String): Map[String, String] =
    getResourceLines(path)
      .map(_.trim.split(' '))
      .collect{case Array(anchor, file) => anchor -> file}
      .toMap

}
