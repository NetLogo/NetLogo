// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.codetab

import java.util.{ Timer, TimerTask }

import org.nlogo.analytics.Analytics
import org.nlogo.app.common.AppTabsInterface
import org.nlogo.core.{ Token, TokenType }
import org.nlogo.editor.EditorMenu
import org.nlogo.window.{ Events => WindowEvents, GUIWorkspace }

import scala.util.hashing.MurmurHash3

// This is THE Code tab.  Certain settings and things that are only accessible here.
// Other Code tabs come and go.

class MainCodeTab(workspace: GUIWorkspace, tabs: AppTabsInterface, editorMenu: EditorMenu)
extends CodeTab(workspace, tabs)
with WindowEvents.LoadModelEvent.Handler
{
  private var lastHash: Option[Int] = None

  new Timer().scheduleAtFixedRate(new TimerTask {
    override def run(): Unit = {
      maybeSendAnalytics()
    }
  }, 300000, 300000)

  override def editorConfiguration =
    super.editorConfiguration.withMenu(editorMenu)

  override def dirty_=(b: Boolean) = {
    super.dirty_=(b)
    if (b) {
      new WindowEvents.DirtyEvent(None).raise(this)
    }
  }

  def handle(e: WindowEvents.LoadModelEvent): Unit = {
    innerSource = e.model.code
    compile()
    maybeSendAnalytics()
  }

  private def maybeSendAnalytics(): Unit = {
    val text = getText

    if (text.nonEmpty) {
      val hash = MurmurHash3.stringHash(text)

      if (!lastHash.contains(hash)) {
        lastHash = Option(hash)

        Analytics.codeHash(hash)

        val tokens: Seq[Token] = workspace.tokenizeForColorization(text).toSeq

        Analytics.primUsage(tokens)
        Analytics.keywordUsage(tokens)
        Analytics.includeExtensions(findExtensions(tokens))
      }
    }
  }

  private def findExtensions(tokens: Seq[Token]): Seq[String] = {
    val MaxSendableExtNames = 50

    tokens
      .dropWhile {
        case Token(_, TokenType.Keyword, "EXTENSIONS") =>
          false

        case _ =>
          true
      }.drop(2) // First token is `extensions`; second is `[`. --Jason B. (11/24/26)
      .takeWhile(_.tpe == TokenType.Ident)
      .take(MaxSendableExtNames)
      .map(_.text)
  }
}
