// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.ide

import javax.swing.text.JTextComponent

import org.nlogo.core._

import scala.collection.mutable.Map

object JumpToDeclaration {

  def findTokenContainingPosition(source: String, position: Int): Option[Token] = {
    val iterator = Femto.scalaSingleton[TokenizerInterface]("org.nlogo.lex.Tokenizer").tokenizeString(source)
    iterator.find(p => p.start < position && p.end >= position)
  }

  def jumpToDeclaration(cursorPosition: Int, editorArea: JTextComponent): Unit = {
    findTokenContainingPosition(editorArea.getText(), cursorPosition).foreach { token =>
      if (token.tpe == TokenType.Ident && !DefaultTokenMapper.allCommandNames.contains(token.text) &&
          !DefaultTokenMapper.allReporterNames.contains(token.text)) {
        val t = getDeclaration(token, editorArea.getText)
        t.foreach(token => {
          editorArea.select(token.start, token.end)
        })
      }
    }
  }

  def getDeclaration(token: Token, source: String): Option[Token] = {
    val tokens = Femto.scalaSingleton[TokenizerInterface]("org.nlogo.lex.Tokenizer").tokenizeString(source).filter(t => t.tpe != TokenType.Comment).toSeq
    val (globals, owns, functions) = parseOuterScope(tokens)
    var stack = 0
    tokens.takeWhile(_.start != token.start).zipWithIndex.foldRight(Option[Token](null)) {
      // if we already found the result, skip the remaining elements (Isaac B 4/25/25)
      case (_, Some(t)) =>
        Some(t)

      case ((tok, i), None) =>
        if (tok.tpe == TokenType.CloseBracket) {
          stack -= 1
        } else if (tok.tpe == TokenType.OpenBracket) {
          stack += 1
        }

        if (stack >= 0 && tok.text.equalsIgnoreCase(token.text) && i > 0 && tokens(i - 1).text.equalsIgnoreCase("let")) {
          Some(tok)
        } else if (tok.text.equalsIgnoreCase("to") || tok.text.equalsIgnoreCase("to-report")) {
          // i + 1 is the function name
          if (i + 2 < tokens.length && tokens(i + 2).tpe == TokenType.OpenBracket) {
            tokens.drop(3).dropWhile { t =>
              t.tpe != TokenType.CloseBracket && !t.text.equalsIgnoreCase(token.text)
            }.headOption.filter(_.tpe != TokenType.CloseBracket)
             .orElse(globals.get(token.text.toUpperCase) orElse owns.get(token.text.toUpperCase) orElse
                     functions.get(token.text.toUpperCase))
          } else {
            globals.get(token.text.toUpperCase) orElse owns.get(token.text.toUpperCase) orElse functions.get(token.text.toUpperCase)
          }
        } else {
          None
        }
    }.orElse(globals.get(token.text.toUpperCase) orElse owns.get(token.text.toUpperCase) orElse functions.get(token.text.toUpperCase))
  }

  /**
    * Parsed the variable that are visible in the whole scope of the code.
    * @param tokens list of all the tokens in the editor
    * @return tuple of globals, owns, functions. Each value is a map
    *         from token text to token.
    */
  def parseOuterScope(tokens: Seq[Token]): (Map[String, Token], Map[String, Token], Map[String, Token]) = {
    val globals = Map[String, Token]()
    val owns = Map[String, Token]()
    val functions = Map[String, Token]()
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
      } else if (token.text.toUpperCase.endsWith("-OWN")) {
        i += 1
        while (token.tpe != TokenType.CloseBracket && i < tokens.length) {
          token = tokens(i)
          if (token.tpe == TokenType.Ident && owns.get(token.text.toUpperCase).isEmpty) {
            owns += token.text.toUpperCase -> token
          }
          i += 1
        }
      } else if (token.text.equalsIgnoreCase("to") || token.text.equalsIgnoreCase("to-report")) {
        i += 1
        if(i < tokens.length) {
          token = tokens(i)
          functions += token.text.toUpperCase -> token
        }
      } else {
        i += 1
      }
    }
    (globals, owns, functions)
  }
}
