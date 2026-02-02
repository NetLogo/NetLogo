// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.Component
import java.net.URI
import java.nio.file.{ Files, Path, Paths }
import java.util.Locale

import org.nlogo.api.Version
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

  private val docsRoot: String = System.getProperty("netlogo.docs.dir")

  // if we're not in 3D don't load the 3d dictionary words
  // cause we don't need 'em and they'll override the 2d
  // dictionary ev 10/25/07
  private lazy val quickHelpWords: Map[String, Entry] =
    loadHelp(false)

  private lazy val quickHelpWords3d: Map[String, Entry] = {
    if (Version.is3D) {
      loadHelp(true)
    } else {
      Map()
    }
  }

  def docPath(anchor: Option[String]): Path =
    Paths.get(docsRoot, "NetLogo_User_Manual.pdf" + anchor.fold("")("#" + _))

  private def loadHelp(threed: Boolean): Map[String, Entry] = {
    Files.readString(Paths.get(docsRoot, "manual-links.csv")).split('\n').flatMap { line =>
      val split = line.split(',')

      Entry.parse(split.tail, threed).map((split.head, _))
    }.toMap
  }

  private def openDictionary(comp: Component, word: String, words: Map[String, Entry]): Unit = {
    BrowserLauncher.tryOpenURI(
      comp,
      new URI(s"https://docs.netlogo.org/${Version.versionNumberNo3D}/${words(word).docsUrl}"),
      docPath(Some(words(word).pdfAnchor))
    )
  }

  def doHelp(comp: Component, token: String): Unit = {
    if (token == null)
      return

    val tokenLower = token.toLowerCase

    // if there is an entry in the 3D dictionary then it overrides
    // the 2D entry ev 10/25/07
    if (quickHelpWords3d.contains(tokenLower)) {
      openDictionary(comp, tokenLower, quickHelpWords3d)
    } else if (quickHelpWords.contains(tokenLower)) {
      openDictionary(comp, tokenLower, quickHelpWords)
    } else {
      if (new OptionPane(comp, I18N.gui.get("common.netlogo"),
                         I18N.gui.getN("tabs.code.rightclick.quickhelp.notfound",
                         tokenLower.toUpperCase(Locale.ENGLISH)), OptionPane.Options.OkCancel,
                         OptionPane.Icons.Error).getSelectedIndex == 0)
        BrowserLauncher.tryOpenURI(comp,
          new URI(s"https://docs.netlogo.org/${Version.versionNumberNo3D}/dictionary.html"),
          docPath(Some("dictionary-netlogo-dictionary")))
    }
  }

  private object Entry {
    def parse(els: Array[String], threed: Boolean): Option[Entry] = {
      if (els.size == 3 && (els(0) == "3d") == threed) {
        Some(Entry(els(1), els(2)))
      } else {
        None
      }
    }
  }

  private case class Entry(docsUrl: String, pdfAnchor: String)
}
