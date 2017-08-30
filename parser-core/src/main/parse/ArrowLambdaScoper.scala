// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.parse

import
  org.nlogo.core.{ Fail, SourceLocation, Token, TokenType }, Fail.exception

import
  org.nlogo.core.prim.{ Lambda, _lambdavariable, _unknownidentifier },
    Lambda.Arguments

object ArrowLambdaScoper {
  def apply(group: BracketGroup, usedNames: SymbolTable): Option[(Arguments, Seq[SyntaxGroup], SymbolTable)] = {
    val containsArrow =
      group.innerGroups.exists {
        case Atom(t) if t.text == "->" => true
        case _ => false
      }

    if (containsArrow)
      gatherArguments(group, usedNames).map {
        case (args, remainder, syms) =>
          val body = remainder.map(traverseAndReplace(args.argumentNames, syms))
          (args, body, syms)
      }
    else
      None
  }

  private def traverseAndReplace(args: Seq[String], syms: SymbolTable)(g: SyntaxGroup): SyntaxGroup = {
    g match {
      case Atom(t @ Token(txt, TokenType.Reporter, _unknownidentifier())) if args.contains(t.text.toUpperCase) =>
        Atom(t.refine(_lambdavariable(txt.toUpperCase)))
      case a: Atom => a
      case ParenGroup(inner, start, end)   => ParenGroup(inner.map(traverseAndReplace(args, syms)), start, end)
      case BracketGroup(inner, start, end) => BracketGroup(inner.map(traverseAndReplace(args, syms)), start, end)
    }
  }

  def gatherArguments(group: BracketGroup, usedNames: SymbolTable): Option[(Arguments, Seq[SyntaxGroup], SymbolTable)] = {
    implicit class RichToken(t: Token) {
      def isArrow = t.text == "->"
      def isOpenBracket = t.tpe == TokenType.OpenBracket
      def isCloseBracket = t.tpe == TokenType.CloseBracket
      // _taskvariable is an accomodation for the autoconverter
      def isAlreadyDefined(usedNames: SymbolTable) =
        usedNames.contains(t.text.toUpperCase) && ! t.value.isInstanceOf[LambdaTokenMapper._taskvariable]
    }

    def gatherBracketedArguments(argGroups: Seq[SyntaxGroup]): (Arguments, SymbolTable) = {
      val (argTokens, symbols) = argGroups.foldLeft((Seq.empty[Token], usedNames)) {
        case ((args, syms), Atom(t)) => gatherSingleArgument(t, syms, args)
        case ((args, syms), other)   => exception(ExpectedVarName, other.start)
      }
      (Lambda.BracketedArguments(argTokens), symbols)
    }

    def gatherSingleArgument(t: Token, scope: SymbolTable, others: Seq[Token] = Seq()): (Seq[Token], SymbolTable) = {
      if (t.isAlreadyDefined(scope))
        SymbolType.alreadyDefinedException(scope(t.text.toUpperCase), t)
      else if (t.isArrow)
        exception(ExpectedVarName, t)
      else if (t.tpe == TokenType.Ident || t.value.isInstanceOf[_unknownidentifier] || t.value.isInstanceOf[LambdaTokenMapper._taskvariable])
        (others :+ t, scope.addSymbol(t.text.toUpperCase, SymbolType.LambdaVariable))
      else
        exception(ExpectedVarName, t)
    }

    def gatherBody(innerGroups: Seq[SyntaxGroup], start: SourceLocation): Seq[SyntaxGroup] = {
      innerGroups.head match {
        case Atom(t) if t.isArrow => innerGroups.tail
        case Atom(t)              =>
          exception(MustBeBracketed, SourceLocation(start.start, t.end, t.filename))
        case _                    => Seq() // ????
      }
    }

    val groups = group.innerGroups
    groups.head match {
      case b@BracketGroup(argGroups, _, _) =>
        val (args, syms) = gatherBracketedArguments(argGroups)
        val body = gatherBody(groups.tail, b.start)
        Some((args, body, syms))
      case Atom(t) if t.isArrow =>
        val body = gatherBody(groups, group.start)
        Some((Lambda.NoArguments(true), body, usedNames))
      case Atom(t) =>
        val (args, syms) = gatherSingleArgument(t, usedNames)
        val body = gatherBody(groups.tail, t.sourceLocation)
        Some((Lambda.UnbracketedArgument(args.head), body, syms))
      case other =>
        exception(MustBeBracketed, other.location)
    }
  }

  val MustBeBracketed = "An anonymous procedures of two or more arguments must enclose its argument list in brackets"
  val ExpectedVarName = "Expected a variable name here"
}
