// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.editor

import java.awt.Color

import org.nlogo.core.{ Femto, I18N, Token, TokenizerInterface }
import org.nlogo.swing.{ MenuItem, PopupMenu }
import org.nlogo.theme.InterfaceColors

class ShowUsage(editorArea: AdvancedEditorArea, colorizer: Colorizer) extends PopupMenu {
  setBackground(InterfaceColors.codeBackground())

  locally {
    val tokenizer: TokenizerInterface = Femto.scalaSingleton[TokenizerInterface]("org.nlogo.lex.Tokenizer")

    val matches: Seq[Token] = editorArea.getTokenAtCaret match {
      case Some(text) =>
        val caret: Int = editorArea.getCaretPosition

        tokenizer.tokenizeString(editorArea.getText).filter { token =>
          token.text.toLowerCase == text.toLowerCase && (token.start > caret || token.end < caret)
        }.toSeq

      case _ =>
        Seq()
    }

    if (matches.isEmpty) {
      add(new MenuItem(I18N.gui.get("tabs.code.rightclick.showUsage.noUsages"))).setEnabled(false)
    } else {
      val lines: Seq[(Token, Line)] = matches.zip(editorArea.getLinesForOffsets(matches.map(_.start)))
                                        .distinctBy(_._2.number)

      val lineChars: Int = lines.map(_._2.number.toString.size).max

      lines.foreach {
        case (token, Line(number, text)) =>
          val numberFormatted: String = textColor(padStart(number.toString, lineChars), InterfaceColors.defaultColor())
          val textFormatted: String = text.zip(colorizer.getCharacterColors(text)).map(charColor).mkString

          add(new MenuItem(s"<html>$numberFormatted&nbsp$textFormatted</html>", () => {
            editorArea.select(token.start, token.end)
          }) {
            setFont(EditorConfiguration.getCodeFont)
          })
      }
    }
  }

  show(editorArea, editorArea.getCaretX, editorArea.getCaretY)

  private def charColor(char: Char, color: Color): String =
    textColor(char.toString, color)

  private def textColor(text: String, color: Color): String = {
    s"<font color=\"rgb(${color.getRed}, ${color.getGreen}, ${color.getBlue})\">$text</font>"
  }

  private def padStart(text: String, length: Int): String =
    s"${"&nbsp;" * (length - text.size).max(0)}$text"
}
