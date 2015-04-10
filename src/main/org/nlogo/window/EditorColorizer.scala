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

  // Realistically, using a popup here is not what we actually want to do.
  // We should create our own completion widget that works like we'd like.
  // This is a prototype which may doom it to being the production method
  // if it's good enough!  However, if you find that that people are running
  // into weird edge cases, don't try and make this work, but rather build
  // something correct from the ground up, probably something that lets
  // the editor retain focus for key events.  FD - 3/7/15
  class CodeCompletionPopup(editor: EditorArea[_], allTokens: Seq[(String, String)], f: (Int, String) => Unit)
  extends javax.swing.JPopupMenu {
    val PageSize = 30
    var tokenName: String = ""
    var page: Integer = 1

    addMenuKeyListener(new javax.swing.event.MenuKeyListener {
       override def menuKeyTyped(e: javax.swing.event.MenuKeyEvent): Unit = {
         if(e.getKeyChar == java.awt.event.KeyEvent.VK_BACK_SPACE) {
           // Early return when we're deleting and we had nothing!
           if(tokenName.length == 0) {
             setVisible(false)
             return
           }

           val position = editor.getCaretPosition
           if(position != 0) {
             editor.setCaretPosition(position - 1)
             val doc = editor.getDocument.asInstanceOf[javax.swing.text.PlainDocument]
             doc.remove(position - 1, 1)
           }
         } else {
           e.setSource(editor)
           editor.dispatchEvent(e)
         }

         refreshMyself
       }
       override def menuKeyPressed(e: javax.swing.event.MenuKeyEvent) = { }
       override def menuKeyReleased(e: javax.swing.event.MenuKeyEvent) = { }
      })

    class CodeCompletionAction(name: String, position: Int, insert: String) extends javax.swing.text.TextAction(name) {
      override def actionPerformed(e:java.awt.event.ActionEvent): Unit = {
        f(position, insert)
      }
    }

    class PageAction(name: String, increment: Int) extends javax.swing.text.TextAction(name) {
      override def actionPerformed(e:java.awt.event.ActionEvent): Unit = {
        page += increment
        refreshMyself
        setVisible(true)
      }
    }

    def refreshMyself: Unit = {
      removeAll

      val currentToken = Option(editor.getCursorToken)
      tokenName = currentToken.map(_.name).getOrElse("")
      val position = editor.getCaretPosition

      val tokens = allTokens.filter( { token => !(tokenName.length == 0 && token._1.startsWith("_")) }).
                     filter(_._1.startsWith(tokenName)).sortWith(_._1 < _._1)
      if(tokens.isEmpty) {
        add(new javax.swing.JMenuItem("-- No Completions --"))
      } else {
        if(tokens.size > PageSize && page > 1)
          add(new PageAction("... " + ((page - 1) * PageSize) + " more", -1))
        tokens
          .take(page * PageSize)
          .takeRight(Math.min(PageSize, tokens.size - (page - 1) * PageSize))
          .foreach { case (name, source) =>
            add(new CodeCompletionAction(name + " (" + source + ")", position, name.stripPrefix(tokenName)))
        }
        if(tokens.size > page * PageSize)
          add(new PageAction("... " + (tokens.size - page * PageSize) + " more", 1))
      }

      setLocation(editor.modelToView(position).x + editor.getLocationOnScreen.x,
        editor.modelToView(position).y + editor.getLocationOnScreen.y)
    }
  }

  def doCodeCompletion(editor: EditorArea[_]): Unit = {
    val doc = editor.getDocument.asInstanceOf[javax.swing.text.PlainDocument]
    val currentLine = editor.offsetToLine(doc, editor.getCaretPosition);
    val startLineOffset = editor.lineToStartOffset(doc, currentLine);
    val allTokens: Seq[(String, String)] = parser.getCompletions(org.nlogo.app.App.app.tabs.codeTab.text.getText())

    val position = Option(editor.getCursorToken()).map(_.endPos + startLineOffset).getOrElse(editor.getCaretPosition())
    editor.setCaretPosition(position)

    val menu = new CodeCompletionPopup(editor, allTokens, 
      { (position, insert) => doc.insertString(position, insert + " ", null) }
    )

    menu.refreshMyself
    menu.show(editor, editor.modelToView(position).x, editor.modelToView(position).y)
  }
}
