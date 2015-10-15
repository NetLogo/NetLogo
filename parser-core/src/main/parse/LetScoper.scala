// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.parse

// A disquisition on how "let" is handled in the compiler.
//
// Well, it's messy. Probably a better solution is possible.  But what we have is,
// FrontEnd.parseProcedure calls LetNamer, then Namer, then LetScoper.  All three
// classes (yes, including Namer!) do something special with "let".  All of this
// happens before ExpressionParser (the class that actually makes ASTs).
// After ExpressionParser, there are two more passes on let's, LetReducer and LetVerifier,
// which are covered at the end.
//
// WHAT HAPPENS:
//
// 1) LetNamer performs a single, simple transform. We look for a "LET" token followed by an identifier,
// and change the value slot of the identifier token to an instance of _letname.
//
// 2) Namer's main job is to recognize identifiers of all kinds, replacing
// Ident tokens with Command and Reporter tokens, as appropriate.  If Namer
// doesn't recognize an identifier, it assumes that identifier must be
// a reference to a let variable, so Namer makes a _letvariable Reporter token.
//
// 3) Finally, LetScoper cleans up after Namer, connecting each _letvariable reference to the
// corresponding _let where that variable was defined. (Or, it will complain if you try to refer to
// a nonexistent variable, or a variable that exists but isn't in your current scope.)
// It uses bracket tokens to determine where scopes begin and end.
//
// WHY IT HAPPENS THAT WAY:
//
// Q: Why is LetNamer distinct from Namer?
//
// A: Because Namer only examines one token at a time. In LetNamer, we need
//    a two-token window, to identify the identifier tokens that come after
//    "LET" tokens.  (An alternative solution would have been to give all of Namer
//    a two-token window, but then only use that window in one place. It's not
//    clear if that would be worse or better. In general, more phases multiplies
//    entities but reduces coupling.  We went with more phases.)
//
// Q: What is this `_letname` thing that got added?
//
// A: When the input is `Token(_let _ident:FOO ...)`, we need to prevent Namer from replacing the
//    _ident token with something else. We do that by replacing _ident with an instance of a dummy
//    _letname prim. (We considered just dropping the _ident token, but dropping tokens from the
//    token stream just seemed like a bizarre thing to be doing this early in compilation.
//    In general, we want to preserve the lexical structure of the source code, and only start
//    dropping and abstracting in later phases once we start making ASTs.)
//
// Q: Because LetScoper precedes ExpressionParser, it operates on a linear stream
//    of tokens, forcing it to look for bracket tokens in order to determine the
//    structure of the code. Why not put LetScoper after ExpressionParser?
//    ExpressionParser outputs structured ASTs, so we wouldn't need separate
//    bracket-based code to determine where scopes begin and end.
//
// A: We actually tried that and failed. We no longer remember the exact details,
//    but at a high level, what happened was, we couldn't parse correctly
//    if all identifiers weren't resolved.  (We might have been able to parse
//    correct code correctly, but the full task of parsing includes issuing
//    appropriate error messages for incorrect code, and that was the tough
//    part.)  Anyway, there is actually something elegant, arguably anyway,
//    about completing all identifier resolution before doing any parsing
//    (in the ExpressionParser sense).
//
// Q: So is the new LetScoper code any improvement on the same stuff in 5.x?
//
// A: Actually yes.  The old code kept track of scopes using token numbers in the
//    Let object.  Both aspects were gross (the use of numbers at all, and the
//    extra slots they were stored in).  In the new code, it all happens via
//    nice recursive code.
//
// Q: Is the code below the best possible way to code this general solution
//    approach?
//
// A: Almost certainly not, but we need to move on since we only have 4 hours/
//    week to work on this. #pragmatism
//
// Q: What's the deal with LetVerifier and LetReducer?
//
// A: LetNamer, Namer, and LetScoper allow let to be parsed, but don't really deal
//    with the semantics of let. That's LetVerifier and LetReducer. LetVerifier
//    checks that no `let x x` is allowed. LetReducer pulls the (at that point) redundant
//    _letname prim out of the AST, which makes life easier for the rest of the Compiler.
//    These operations are easiest to do on ASTs. The reduction step *could* be moved into
//    LetScoper, but it would result in having to change the syntax of the _let primitive,
//    seems awkward and confusing.

import org.nlogo.core,
  core.{ Token, TokenType, Let, I18N },
  core.Fail._

import scala.annotation.tailrec

class LetScoper(usedNames: Map[String, String]) {

  // Environment encapsulates state, namely, what variables are currently in scope.
  // Each List[Let] is a scope; we have a List[List[Let]] because scopes nest.
  // We push() to start a new scope when we see an open square bracket, then pop()
  // when we reach the corresponding close bracket.
  object Environment {
    private var lets = List[List[Let]]()
    def push(): Unit =
      lets +:= Nil
    def add(name: String): Let = {
      val let = Let(name)
      lets = (let +: lets.head) +: lets.tail
      let
    }
    def get(name: String): Option[Let] =
      lets.flatten.find(_.name == name)
    def pop(): Unit =
      lets = lets.tail
    def used: Map[String, String] =
      lets.flatten
        .map(_.name -> "local variable here")
        .toMap
  }

  // begin with an initial empty scope, for the top level of the procedure
  Environment.push()

  // We require a BufferedIterator so we can peek ahead one token
  // the tailrec annotation prevents StackOverflowErrors, when compiling the "Continental Divide" Model
  @tailrec final def apply(tokens: BufferedIterator[Token], accTokens: Seq[Token]=Seq()): Iterator[Token] =
    if (tokens.hasNext)
      apply(tokens, accTokens :+ next(tokens))
    else
      accTokens.iterator

  // we look for this in the input:
  //   Command(_let) Reporter(_letname) ...
  //     make Let from _letname, stuff into _let
  //   Reporter(_letvariable)
  //     stuff Let into _letvariable

  def next(iter: BufferedIterator[Token]): Token =
    iter.next() match {
      case t @ Token(_, TokenType.OpenBracket, _) =>
        Environment.push()
        t
      case t @ Token(_, TokenType.CloseBracket, _) =>
        Environment.pop()
        t
      case t @ Token(_, TokenType.Command, l: core.prim._let) =>
        val nameToken = iter.head
        val name = nameToken.text.toUpperCase
        for (displayName <- (usedNames ++ Environment.used).get(name))
          exception("There is already a " + displayName + " called " + name, nameToken)
        cAssert(nameToken.tpe == TokenType.Reporter,
           "Expected variable name here", nameToken)
        cAssert(!name.startsWith("?"),
          "Names beginning with ? are reserved for use as task inputs", nameToken)
        val let = Environment.add(name)
        t.refine(l.copy(let = let))
      case t @ Token(_, TokenType.Reporter, l: core.prim._letvariable) =>
        Environment.get(t.text.toUpperCase) match {
          case Some(let) =>
            t.refine(l.copy(let = let))
          case None =>
            val msg = I18N.errors.getN(
              "compiler.LetVariable.notDefined", t.text.toUpperCase)
            exception(msg, t)
        }
      case t =>
        t
    }

}
