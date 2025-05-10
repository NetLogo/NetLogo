// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.ide

import java.util.{ List => JList }

import org.nlogo.core.{ CompilerException, Femto, Token, TokenizerInterface, TokenType }

import org.fife.ui.rsyntaxtextarea.{ folding, RSyntaxTextArea },
  folding.{ Fold, FoldParser, FoldType }

import scala.annotation.tailrec
import scala.util.Try

object NetLogoFoldParser {
  val tokenizer: TokenizerInterface =
    Femto.scalaSingleton[TokenizerInterface]("org.nlogo.lex.Tokenizer")

  val namer: (Token => Token) =
    Femto.scalaSingleton[Token => Token]("org.nlogo.parse.Namer0")

  def sections(text: String): Seq[Seq[Token]] = {
    val tokens = tokenizer.tokenizeString(text, "")
      .map(t => Try(namer(t)).recover({
        case c: CompilerException => t
      }).get).buffered

    @tailrec
    def takeUntilCloseBracketOrKeyword(acc: Seq[Token]): Seq[Token] =
      if (! tokens.hasNext) acc
      else tokens.head.tpe match {
        case TokenType.CloseBracket                  => tokens.next() +: acc
        case TokenType.OpenBracket if acc.length > 1 => acc
        case TokenType.Ident if tokens.head.text.equalsIgnoreCase("BREED") => acc
        case TokenType.Keyword | TokenType.Eof       => acc
        case _ => takeUntilCloseBracketOrKeyword(tokens.next() +: acc)
      }

    @tailrec
    def takeUntilEnd(acc: Seq[Token]): Seq[Token] =
      if (! tokens.hasNext) acc
      else tokens.head.tpe match {
        case TokenType.Keyword if (tokens.head.value == "END") => tokens.next() +: acc
        case TokenType.Keyword | TokenType.Eof => acc
        case _ => takeUntilEnd(tokens.next() +: acc)
      }

    @tailrec
    def takeUntilNonComment(acc: Seq[Token]): Seq[Token] =
      if (! tokens.hasNext) acc
      else tokens.head.tpe match {
        case TokenType.Comment => takeUntilNonComment(tokens.next() +: acc)
        case _                 => acc
      }

    def takeUntilEof(acc: Seq[Seq[Token]]): Seq[Seq[Token]] =
      if (! tokens.hasNext || tokens.head.tpe == TokenType.Eof) acc
      else {
        val next = tokens.head.tpe match {
          case TokenType.OpenBracket => takeUntilCloseBracketOrKeyword(Seq(tokens.next()))
          case TokenType.Keyword if tokens.head.value == "TO" || tokens.head.value == "TO-REPORT" =>
            takeUntilEnd(Seq(tokens.next()))
          case TokenType.Keyword =>
            takeUntilCloseBracketOrKeyword(Seq(tokens.next()))
          case TokenType.Comment =>
            takeUntilNonComment(Seq(tokens.next()))
          case TokenType.Ident if tokens.head.text.equalsIgnoreCase("BREED") =>
            takeUntilCloseBracketOrKeyword(Seq(tokens.next()))
          case _ =>
            takeUntilEnd(Seq(tokens.next()))
        }
        takeUntilEof(next.reverse +: acc)
      }

    takeUntilEof(Seq()).reverse
  }
}

import NetLogoFoldParser.sections

class NetLogoFoldParser extends FoldParser {
  override def getFolds(textArea: RSyntaxTextArea): JList[Fold] = {
    import scala.jdk.CollectionConverters.SeqHasAsJava

    val text = textArea.getText
    val parsedSections = sections(text)

    parsedSections
      .filterNot(singleLineDeclaration(text))
      .map { section =>
        val foldType = section.head.tpe match {
          case TokenType.Comment => FoldType.COMMENT
          case _                 => FoldType.CODE
        }
        val fold = new Fold(foldType, textArea, section.head.start)
        fold.setEndOffset(section.last.end)
        fold
      }.toSeq.asJava
  }

  private def singleLineDeclaration(text: String)(toks: Seq[Token]): Boolean =
    !text.slice(toks.head.start, toks.last.end).contains("\n")
}
