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
            val body = remainder.drop(1).map {
              case t @ Token(txt, TokenType.Reporter, _unknownidentifier()) if args.contains(t.text.toUpperCase)=>
                t.refine(_lambdavariable(txt.toUpperCase))
              case t => t
            }
            (args, body, syms)
        }
    } else None
  }

  def gatherArguments(toks: Seq[Token], usedNames: SymbolTable): Option[(Seq[String], Seq[Token], SymbolTable)] = {
    @tailrec
    def gatherArgument(acc: Seq[String], toks: Seq[Token], usedNames: SymbolTable): (Seq[String], Seq[Token], SymbolTable) = {
      val tok = toks.head
      if (tok.tpe == TokenType.CloseBracket)
        (acc, toks.tail, usedNames)
      else if (usedNames.contains(tok.text.toUpperCase))
        SymbolType.alreadyDefinedException(usedNames(tok.text.toUpperCase), tok)
      else if (tok.text == "->" || tok.tpe != TokenType.Reporter)
        exception(s"Expected a variable name here", tok)
      else
        gatherArgument(acc :+ tok.text.toUpperCase, toks.tail, usedNames.addSymbols(Seq(tok.text.toUpperCase), SymbolType.LambdaVariable))
    }
    if (toks.head.tpe == TokenType.OpenBracket)
      Some(gatherArgument(Seq(), toks.tail, usedNames))
    else
      None // if we decide to allow [x -> ...] as opposed to only [[x] -> ...], this branch will become relevant
  }
}
