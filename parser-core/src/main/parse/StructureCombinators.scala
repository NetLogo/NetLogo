// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.parse

/// Stage #1 of StructureParser

// This knows practically nothing about the rest of NetLogo.
// We use core.{ Token, TokenType } and nothing else.

// We currently use the parser combinators from the Scala standard library.  It's not an
// industrial-strength implementation, but I'm hopeful it will be good enough for our purposes.  Our
// grammar is simple and NetLogo programs aren't enormous.  If we get into trouble with slowness or
// stack overflows or what have you, I don't think it would be hard to swap in something else (Nomo?
// GLL Combinators? Parboiled? our own hand-rolled code?).

// For background on parser combinators, see the chapter on them in
// the Programming in Scala book.  The combinators used here are:
//   ~   plain sequencing
//   ~!  sequence, don't allow backtracking
//   ~>  sequence, discard result on left
//   <~  sequence, discard result on right
//   |   alternatives
//   ^^  transform results
//
// It's annoying that there's no ~>! (to both disallow backtracking
// and discard input).

import
  org.nlogo.core.{ Token, TokenType, StructureDeclarations },
  StructureDeclarations._

object StructureCombinators {
  def parse(tokens: Iterator[Token]): Either[(String, Token), Seq[Declaration]] = {
    val reader = new SeqReader[Token](tokens.toStream, _.start)
    val combinators = new StructureCombinators
    combinators.program(reader) match {
      case combinators.Success(declarations, _) =>
        Right(declarations)
      case combinators.NoSuccess(msg, rest) =>
        val token =
          if (rest.atEnd)
            if (tokens.hasNext)
              tokens.next()
            else
              Token.Eof
          else
            rest.first
        Left((msg, token))
    }
  }
}

class StructureCombinators
extends scala.util.parsing.combinator.Parsers {

  // specify what kind of input we take
  override type Elem = Token

  // wouldn't be strictly necessary to use phrase() below except that because of
  // https://issues.scala-lang.org/browse/SI-4929 we'll leak a ThreadLocal if we don't. besides,
  // using phrase() may help with error reporting since it adds logic that chooses between multiple
  // possible failures to report by reporting the one that parsed the most input
  // - ST 1/3/13, 6/27/13, 8/20/13

  // top level entry point. output will be a Seq[Declaration]
  def program: Parser[Seq[Declaration]] =
    phrase(
      rep(declaration) ~ (procedures | noProcedures)) ^^ {
        case decs ~ procs =>
          decs ++ procs }

  def declaration: Parser[Declaration] =
    includes | extensions | breed | directedLinkBreed | undirectedLinkBreed |
      variables("GLOBALS") | variables("TURTLES-OWN") | variables("PATCHES-OWN") |
      variables("LINKS-OWN") | breedVariables

  def includes: Parser[Includes] =
    keyword("__INCLUDES") ~! stringList ^^ {
      case token ~ names =>
        Includes(token, names) }

  def extensions: Parser[Extensions] =
    keyword("EXTENSIONS") ~! identifierList ^^ {
      case token ~ names =>
        Extensions(token, names) }

  def variables(key: String): Parser[Variables] =
    keyword(key) ~! identifierList ^^ {
      case keyToken ~ names =>
        Variables(Identifier(key, keyToken), names) }

  def breedVariables: Parser[Variables] =
    acceptMatch("<breed>-own", {
      case token @ Token(_, TokenType.Keyword, value: String)
          if value.endsWith("-OWN") =>
        token }) ~!
      identifierList ^^ {
        case token ~ names =>
          Variables(Identifier(token.value.asInstanceOf[String], token), names) }

  def breed: Parser[Breed] = {
    breedKeyword ~! breedBlock("BREED") ^^ (_._2) // (_._2) drops BREED token
  }

  def breedBlock(breedWord: String): Parser[Breed] = {
    val breedNames: Parser[Breed] = identifier ~ identifier ^^ {
      case plural ~ singular => Breed(plural, singular)
    }
    val singleBreedName: Parser[Nothing] =
      identifier >> (i =>
          failure(s"Breed declarations must have plural and singular. $breedWord [${i.name}] has only one name."))
    (openBracket ~> (breedNames | singleBreedName) <~ closeBracket)
  }

  def directedLinkBreed: Parser[Breed] =
    keyword("DIRECTED-LINK-BREED") ~! breedBlock("DIRECTED-LINK-BREED") ^^ (_._2.copy(isLinkBreed = true, isDirected = true))

  def undirectedLinkBreed: Parser[Breed] =
    keyword("UNDIRECTED-LINK-BREED") ~! breedBlock("UNDIRECTED-LINK-BREED") ^^ (_._2.copy(isLinkBreed = true, isDirected = false))

  def procedures: Parser[Seq[Procedure]] =
    rep1(procedure) <~ (eof | failure("TO or TO-REPORT expected"))

  def noProcedures: Parser[Seq[Procedure]] =
    eof ^^ { case _ => Seq() } | failure("keyword expected")

  def procedure: Parser[Procedure] =
    (keyword("TO") | keyword("TO-REPORT")) ~!
      identifier ~
      opt(identifierList) ~
      rep(nonKeyword) ~
      (keyword("END") | failure("END expected")) ^^ {
        case to ~ name ~ names ~ body ~ end =>
          Procedure(name,
            to.value == "TO-REPORT", names.getOrElse(Seq()),
            to +: name.token +: body :+ end) }

  /// helpers

  def tokenType(expected: String, tpe: TokenType): Parser[Token] =
    acceptMatch(expected, { case t @ Token(_, `tpe`, _) => t })

  def eof: Parser[Token] =
    tokenType("eof", TokenType.Eof)

  def openBracket: Parser[Token] =
    tokenType("opening bracket", TokenType.OpenBracket)

  def closeBracket: Parser[Token] =
    tokenType("closing bracket", TokenType.CloseBracket)

  def identifier: Parser[Identifier] =
    tokenType("identifier", TokenType.Ident) ^^ {
      token =>
        Identifier(token.value.asInstanceOf[String], token)}

  def identifierList: Parser[Seq[Identifier]] =
    openBracket ~> commit(rep(identifier) <~ closeBracket)

  def stringList: Parser[Seq[Token]] =
    openBracket ~> commit(rep(string) <~ closeBracket)

  def string: Parser[Token] =
    acceptMatch("string", {
      case t @ Token(_, TokenType.Literal, value: String) =>
        t })

  def keyword(name: String): Parser[Token] =
    acceptMatch(name, {
      case token @ Token(_, TokenType.Keyword, `name`) =>
        token })

  // kludge: special case because of naming conflict with BREED turtle variable - jrn 8/04/05
  def breedKeyword: Parser[Token] =
    acceptMatch("BREED", {
      case token @ Token(_, TokenType.Ident, "BREED") =>
        token })

  def nonKeyword: Parser[Token] =
    acceptMatch("?", {
      case token @ Token(_, tpe, _)
          if (tpe != TokenType.Keyword && tpe != TokenType.Eof) =>
        token })

}

/// Allows our combinators to take their input from a Seq.

import scala.util.parsing.input

class SeqReader[T](xs: Seq[T], fn: T => Int) extends input.Reader[T] {
  case class Pos(pos: Int) extends input.Position {
    def column = pos
    def line = 0
    def lineContents = ""
  }
  def atEnd = xs.isEmpty
  def first = xs.head
  def rest = new SeqReader(xs.tail, fn)
  def pos =
    if (atEnd) Pos(Int.MaxValue)
    else Pos(fn(xs.head))
}
