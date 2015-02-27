// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.Color
import org.nlogo.api.{ ParserServices, Token, TokenType, Version }
import org.nlogo.editor.{ EditorArea, Colorizer }
import org.nlogo.swing.BrowserLauncher.openURL
import org.nlogo.swing.Implicits._
import collection.JavaConverters._
import org.nlogo.awt.EventQueue

class EditorColorizer(parser: ParserServices) extends Colorizer[TokenType] {

  // cache last studied line, so we don't retokenize the same string over and over again when the
  // user isn't even doing anything
  private var lastLine = ""
  private var lastColors = Array[Color]()

  def reset() {
    lastLine = ""
    lastColors = Array()
  }

  def getCharacterColors(line: String): Array[Color] =
    if (line == lastLine)
      lastColors
    else {
      val tokens = tokenizeForColorization(line)
      val result = Array.fill(line.size)(SyntaxColors.DEFAULT_COLOR)
      for (tok <- tokens) {
        // "breed" can be either a keyword or a turtle variable, which means we can't reliably
        // colorize it correctly; so as a kludge we colorize it as a keyword if it's right at the
        // beginning of the line (position 0) - ST 7/11/06
        val color = getTokenColor(
          if (tok.tpe == TokenType.VARIABLE &&
              tok.startPos == 0 &&
              tok.name.equalsIgnoreCase("BREED"))
            TokenType.KEYWORD
          else
            tok.tpe
        )
        for (j <- tok.startPos until tok.endPos)
          // guard against any bugs in tokenization causing out-of-bounds positions
          if(result.isDefinedAt(j))
            result(j) = color
      }
      lastColors = result
      lastLine = line
      result
    }

  // This is used for bracket matching and word selection (double clicking) and not for
  // colorization, so we don't need to bother with the TYPE_KEYWORD hack for "breed" here.
  // - ST 7/11/06
  def getCharacterTokenTypes(line: String): java.util.List[TokenType] = {
    val result = new Array[TokenType](line.size)
    val tokens = tokenizeForColorization(line)
    for {tok <- tokens; j <- tok.startPos until tok.endPos}
      // guard against any bugs in tokenization causing out-of-bounds positions
      if (result.isDefinedAt(j))
        result(j) = tok.tpe
    result.toIndexedSeq.asJava
  }

  def isMatch(token1: TokenType, token2: TokenType) =
    (token1, token2) == ((TokenType.OPEN_PAREN, TokenType.CLOSE_PAREN)) ||
    (token1, token2) == ((TokenType.OPEN_BRACKET, TokenType.CLOSE_BRACKET))

  def isOpener(token: TokenType) =
    token == TokenType.OPEN_PAREN || token == TokenType.OPEN_BRACKET

  def isCloser(token: TokenType) =
    token == TokenType.CLOSE_PAREN || token == TokenType.CLOSE_BRACKET

  def tokenizeForColorization(line: String): Seq[Token] =
    parser.tokenizeForColorization(line)

  ///

  private def getTokenColor(tpe: TokenType) =
    tpe match {
      case TokenType.CONSTANT =>
        SyntaxColors.CONSTANT_COLOR
      case TokenType.COMMAND =>
        SyntaxColors.COMMAND_COLOR
      case TokenType.REPORTER =>
        SyntaxColors.REPORTER_COLOR
      case TokenType.VARIABLE =>
        SyntaxColors.REPORTER_COLOR
      case TokenType.KEYWORD =>
        SyntaxColors.KEYWORD_COLOR
      case TokenType.COMMENT =>
        SyntaxColors.COMMENT_COLOR
      case _ =>
        SyntaxColors.DEFAULT_COLOR
    }

  def getTokenStringAtPosition(text: String, position: Int): String =
    Option(getTokenAtPosition(text, position)).map(_.name).orNull

  def getTokenAtPosition(text: String, position: Int): Token =
    parser.getTokenAtPosition(text, position)

  def doHelp(comp: java.awt.Component, name: String) {
    def confirmOpen(): Boolean =
      0 == javax.swing.JOptionPane.showConfirmDialog(
        comp, name.toUpperCase + " could not be found in the NetLogo Dictionary.\n" +
        "Would you like to open the full NetLogo Dictionary?",
        "NetLogo", javax.swing.JOptionPane.YES_NO_OPTION)
    if (name != null)
      QuickHelp.doHelp(name, Version.is3D,
                       openURL(comp, _, true),
                       confirmOpen _)
  }

  // When all we have is a name, go find a definition
  override def raiseJumpToDefinitionEvent(comp: java.awt.Component, name: String): Unit = {
     Events.JumpToDefinitionEvent(name).raise(comp)
  }

  def jumpToDefinition(editor: EditorArea[_], name: String): Boolean = {
    val procsTable = parser.findProcedurePositions(editor.getText)
    val found = procsTable.contains(name)
    if (found) {
      val namePos = procsTable(name)._3
      val endPos = procsTable(name)._4
      editor.select(endPos, endPos)
      EventQueue.invokeLater { () =>
        editor.select(namePos, namePos + name.size)
      }
    }
    found
  }


  class CodeCompletionPopup(tokens: Seq[(String, String)], f: String => Unit) extends javax.swing.JPopupMenu {
    class CodeCompletionAction(name: String) extends javax.swing.text.TextAction(name) {
      override def actionPerformed(e:java.awt.event.ActionEvent): Unit = {
        f(name)
      }
    }
    if(tokens.isEmpty) {
      add(new javax.swing.JMenuItem("-- No Completions --"))
    } else {
      tokens.foreach { case (name, source) =>
        add(new CodeCompletionAction(name))
      }
    }
  }

  def doCodeCompletion(editor: EditorArea[_]): Unit = {
    val currentToken = editor.getCursorToken
    val tokenName = currentToken.name.toLowerCase
    val doc = editor.getDocument.asInstanceOf[javax.swing.text.PlainDocument]
    val currentLine = editor.offsetToLine(doc, editor.getCaretPosition);
    val startLineOffset = editor.lineToStartOffset(doc, currentLine);
    val tokens: Seq[(String, String)] = parser.getCompletions(org.nlogo.app.App.app.tabs.codeTab.text.getText(),
      editor.getCursorToken().name)

    System.out.println(tokens.filter(_._1.startsWith(tokenName)).sortWith(_._1 < _._1))

    val position = editor.getCursorToken().endPos + startLineOffset
    editor.setCaretPosition(position)

    val menu = new CodeCompletionPopup(tokens.filter(_._1.startsWith(tokenName)).sortWith(_._1 < _._1),
      { name => doc.insertString(position, name.stripPrefix(tokenName), null) }
    )
    System.out.println(editor.modelToView(editor.getCursorToken().endPos + startLineOffset))

    menu.show(editor, editor.modelToView(position).x, editor.modelToView(position).y)
  }
}
