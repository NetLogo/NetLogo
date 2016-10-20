// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.ide

import javax.swing.text.JTextComponent

import org.nlogo.core.TokenType.Ident
import org.nlogo.core._
import org.nlogo.editor.EditorArea

import scala.collection.mutable.Map

object JumpToDeclaration {

  def findTokenContainingPosition(source: String, position: Int): Option[Token] = {
    val iterator = Femto.scalaSingleton[TokenizerInterface]("org.nlogo.lex.Tokenizer").tokenizeString(source)
    iterator.find(p => p.start < position && p.end >= position)
  }

  def jumpToDeclaration(cursorPosition: Int, editorArea: JTextComponent): Unit = {
    val tokenOption = findTokenContainingPosition(editorArea.getText(), cursorPosition)
    for(token <- tokenOption) {
      if(token.tpe != TokenType.Ident || DefaultTokenMapper.allCommandNames.contains(token.text)
        || DefaultTokenMapper.allReporterNames.contains(token.text))
        return
      val t = getDeclaration(token, editorArea.getText)
      t.foreach(token => {
        editorArea.select(token.start, token.end)
      })
    }
  }

  def getDeclaration(token: Token, source: String): Option[Token] = {
    val tokens = Femto.scalaSingleton[TokenizerInterface]("org.nlogo.lex.Tokenizer").tokenizeString(source).filter(t => t.tpe != TokenType.Comment).toSeq
    val (globals, owns) = parseOwns(tokens)
    val indexOfToken = tokens.indexWhere(t => t.start == token.start)
    var stack = 0
    for(i <- indexOfToken to 0 by -1) {
      val tok = tokens(i)
      if (tok.tpe == TokenType.CloseBracket) {
        stack -= 1
      }
      if (tok.tpe == TokenType.OpenBracket) {
        stack += 1
      }
      if(stack >= 0 && tok.text.equalsIgnoreCase(token.text) && i > 0 && tokens(i - 1).text.equalsIgnoreCase("let")) {
        return Some(tok)
      }
      if(tok.text.equalsIgnoreCase("to") || tok.text.equalsIgnoreCase("to-report")) {
        // i + 1 is the function name
        if(i + 2 < tokens.length && tokens(i + 2).tpe == TokenType.OpenBracket) {
          // Arguments found
          var argIndex = i + 3
          while(argIndex < tokens.length && tokens(argIndex).tpe != TokenType.CloseBracket) {
            if(tokens(argIndex).text.equalsIgnoreCase(token.text)) {
              return Some(tokens(argIndex))
            }
            argIndex += 1
          }
        }
        globals.get(token.text.toUpperCase) orElse owns.get(token.text.toUpperCase)
      }
    }
    globals.get(token.text.toUpperCase) orElse owns.get(token.text.toUpperCase)
  }

  def parseOwns(tokens: Seq[Token]): (Map[String, Token], Map[String, Token]) = {
    val globals = Map[String, Token]()
    val owns = Map[String, Token]()
    var i = 0
    while(i < tokens.length) {
      var token = tokens(i)
      if (token.text.equalsIgnoreCase("GLOBALS")) {
        i += 1
        while (token.tpe != TokenType.CloseBracket && i < tokens.length) {
          token = tokens(i)
          if (token.tpe == TokenType.Ident)
            globals += token.text.toUpperCase -> token
          i += 1
        }
      }
      if (token.text.toUpperCase.endsWith("-OWN")) {
        i += 1
        while (token.tpe != TokenType.CloseBracket && i < tokens.length) {
          token = tokens(i)
          if (token.tpe == TokenType.Ident && owns.get(token.text.toUpperCase).isEmpty) {
            owns += token.text.toUpperCase -> token
          }
          i += 1
        }
      }
      i += 1
    }
    (globals, owns)
  }
}
