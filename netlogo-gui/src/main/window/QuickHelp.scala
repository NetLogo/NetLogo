// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.Component
import java.nio.file.Path

import org.nlogo.api.{ FileIO, Version }
import org.nlogo.core.I18N
import org.nlogo.swing.{ BrowserLauncher, OptionPane }

object QuickHelp {
  /**
   * This action is to provide context-sensitive help in the procedures
   * tab.  Based on what their cursor is on, it should jump the user
   * to the proper documentation, found in docs/dict/
   * -- which is just a decomposed version of docs/dictionary.html
   * <p/>
   * Maybe it would be best to pull everything out of some new file,
   * maybe xml format, and we could autogenerate the primitives dictionary file
   * from that.
   * <p/>
   * ~Forrest (4/26/2007)
   */
  private val QUICKHELPWORDS_PATH = "/system/dict.txt"
  private val QUICKHELPWORDS_PATH3D = "/system/dict3d.txt"

  private lazy val quickHelpWords = loadHelp(QUICKHELPWORDS_PATH)
  // if we're not in 3D don't load the 3d dictionary words
  // cause we don't need 'em and they'll override the 2d
  // dictionary ev 10/25/07
  private lazy val quickHelpWords3d =
    if (Version.is3D)
      loadHelp(QUICKHELPWORDS_PATH3D)
    else
      Map[String, String]()

  private def loadHelp(path: String): Map[String, String] = {
    FileIO.getResourceAsStringArray(path).map(line => {
      (line.substring(0, line.indexOf(' ')), line.substring(line.indexOf(' ') + 1))
    }).toMap
  }

  private def docPath(docName: String): Path =
    BrowserLauncher.docPath(docName)

  private def openDictionary(comp: Component, word: String, words: Map[String, String]) {
    BrowserLauncher.openPath(comp, docPath(s"dict/${words.get(word)}"), null)
  }

  def doHelp(comp: Component, token: String) {
    if (token == null)
      return

    val tokenLower = token.toLowerCase

    // if there is an entry in the 3D dictionary then it overrides
    // the 2D entry ev 10/25/07
    if (quickHelpWords3d.contains(tokenLower))
      openDictionary(comp, tokenLower, quickHelpWords3d)
    else if (quickHelpWords.contains(tokenLower))
      openDictionary(comp, tokenLower, quickHelpWords)
    else {
      if (new OptionPane(comp, I18N.gui.get("common.netlogo"),
                         I18N.gui.getN("tabs.code.rightclick.quickhelp.notfound", tokenLower.toUpperCase),
                         OptionPane.Options.OK, OptionPane.Icons.ERROR).getSelectedIndex == 0)
        BrowserLauncher.openPath(comp, docPath("index2.html"), null)
    }
  }
}
