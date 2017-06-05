// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.parse

import org.nlogo.core.{ Fail, Token, TokenType }, Fail.exception
import org.nlogo.core.prim.{ _lambdavariable, _unknownidentifier }

import scala.annotation.tailrec

object ArrowLambdaScoper {
  def apply(toks: Seq[Token], usedNames: SymbolTable): Option[(Seq[String], Seq[Token], SymbolTable)] = {
    if (toks.exists(_.text == "->")
      && toks.headOption.exists(_.tpe == TokenType.OpenBracket)
      && toks.lastOption.exists(_.tpe == TokenType.CloseBracket)) {
        val unbracketedToks = toks.drop(1).dropRight(1)
        gatherArguments(unbracketedToks, usedNames).map {
          case (args, remainder, syms) =>
            val body = remainder.map {
              case t @ Token(txt, TokenType.Reporter, _unknownidentifier()) if args.contains(t.text.toUpperCase)=>
                t.refine(_lambdavariable(txt.toUpperCase))
              case t => t
            }
            (args, body, syms)
        }
    } else None
  }

  def gatherArguments(toks: Seq[Token], usedNames: SymbolTable): Option[(Seq[String], Seq[Token], SymbolTable)] = {
    implicit class RichToken(t: Token) {
      def isArrow = t.text == "->"
      def isOpenBracket = t.tpe == TokenType.OpenBracket
      def isCloseBracket = t.tpe == TokenType.CloseBracket
      // _taskvariable is an accomodation for the autoconverter
      def isAlreadyDefined(usedNames: SymbolTable) =
        usedNames.contains(t.text.toUpperCase) && ! t.value.isInstanceOf[LambdaTokenMapper._taskvariable]
    }

    @tailrec
    def gatherArgumentToCloseBracket(acc: Seq[String], toks: Seq[Token], usedNames: SymbolTable): (Seq[String], Seq[Token], SymbolTable) = {
      val tok = toks.head
      if (tok.isCloseBracket)
        (acc, toks.tail.tail, usedNames)
      else if (tok.isAlreadyDefined(usedNames))
        SymbolType.alreadyDefinedException(usedNames(tok.text.toUpperCase), tok)
      else if (tok.isArrow || tok.tpe != TokenType.Reporter)
        exception(s"Expected a variable name here", tok)
      else
        gatherArgumentToCloseBracket(acc :+ tok.text.toUpperCase, toks.tail, usedNames.addSymbols(Seq(tok.text.toUpperCase), SymbolType.LambdaVariable))
    }

    def gatherUnbracketedArgument(toks: Seq[Token], usedNames: SymbolTable): Option[(Seq[String], Seq[Token], SymbolTable)] = {
      val tok = toks.head
      if (tok.isArrow)
        Some((Seq(), toks.tail, usedNames))
      else if (tok.isOpenBracket)
        None
      else if (tok.isAlreadyDefined(usedNames))
        gatherSymbolsToOpenBracket(Seq(tok), toks.tail, usedNames)
      else
        gatherArgumentToArrow(tok, toks.tail, usedNames.addSymbols(Seq(tok.text.toUpperCase), SymbolType.LambdaVariable))
    }

    def gatherArgumentToArrow(arg: Token, toks: Seq[Token], usedNames: SymbolTable): Option[(Seq[String], Seq[Token], SymbolTable)] = {
      val tok = toks.head
      if (tok.isArrow) {
        if (arg.tpe == TokenType.Ident || arg.value.isInstanceOf[_unknownidentifier] || arg.value.isInstanceOf[LambdaTokenMapper._taskvariable])
          Some((Seq(arg.text.toUpperCase), toks.tail, usedNames))
        else
          exception(s"Expected a variable name here", arg)
      } else if (tok.tpe == TokenType.OpenBracket)
        None
      else
        gatherSymbolsToOpenBracket(Seq(arg, tok), toks.tail, usedNames)
    }

    @tailrec
    def gatherSymbolsToOpenBracket(gatheredSymbols: Seq[Token], toks: Seq[Token], usedNames: SymbolTable): Option[(Seq[String], Seq[Token], SymbolTable)] = {
      val tok = toks.head
      if (tok.isArrow) {
        if (gatheredSymbols.length > 1)
          exception("An anonymous procedures of two or more arguments must enclose its argument list in brackets", gatheredSymbols.head.start, gatheredSymbols.last.end, tok.filename)
        else {
          val initTok = gatheredSymbols.head
          SymbolType.alreadyDefinedException(usedNames(initTok.text.toUpperCase), initTok)
        }
      } else if (tok.isOpenBracket)
        None
      else
        gatherSymbolsToOpenBracket(gatheredSymbols :+ tok, toks.tail, usedNames)
    }

    if (toks.head.isOpenBracket)
      Some(gatherArgumentToCloseBracket(Seq(), toks.tail, usedNames))
    else
      gatherUnbracketedArgument(toks, usedNames)
  }
}
