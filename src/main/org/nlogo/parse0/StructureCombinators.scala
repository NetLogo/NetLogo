// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.parse0

/// Stage #1 of StructureParser

// This knows practically nothing about the rest of NetLogo.
// We use api.{ Token, TokenType } and nothing else.

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

import org.nlogo.api.{ Token, TokenType }
import StructureDeclarations._

object StructureCombinators {
  def parse(tokens: Iterator[Token]): Either[(String, Token), Seq[Declaration]] = {
    val reader = new SeqReader[Token](tokens.toStream, _.start)
    val combinators = new StructureCombinators
    try combinators.program(reader) match {
      case combinators.Success(declarations, _) =>
        Right(declarations)
      case combinators.NoSuccess(msg, rest) =>
        Left((msg, rest.first))
    }
    finally combinators.cleanup()
  }
}

class StructureCombinators
extends scala.util.parsing.combinator.Parsers with Cleanup {

  // specify what kind of input we take
  override type Elem = Token

  // top level entry point. output will be a Seq[Declaration]
  def program: Parser[Seq[Declaration]] =
    rep(declaration) ~ rep(procedure) <~ (eof | failure("keyword expected")) ^^ {
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

  def breed: Parser[Breed] =
    breedKeyword ~! openBracket ~> identifier ~ opt(identifier) <~ closeBracket ^^ {
      case plural ~ singularOption =>
        Breed(plural, singularOption.getOrElse(Identifier("TURTLE", plural.token))) }

  def directedLinkBreed: Parser[Breed] =
    keyword("DIRECTED-LINK-BREED") ~! openBracket ~> identifier ~ identifier <~ closeBracket ^^ {
      case plural ~ singular =>
        Breed(plural, singular, isLinkBreed = true, isDirected = true) }

  def undirectedLinkBreed: Parser[Breed] =
    keyword("UNDIRECTED-LINK-BREED") ~! openBracket ~> identifier ~ identifier <~ closeBracket ^^ {
      case plural ~ singular =>
        Breed(plural, singular, isLinkBreed = true, isDirected = false) }

  def procedure: Parser[Procedure] =
    (keyword("TO") | keyword("TO-REPORT")) ~!
      identifier ~
      formals ~
      rep(nonKeyword) ~
      keyword("END") ^^ {
        case to ~ name ~ names ~ body ~ end =>
          Procedure(name,
            to.value == "TO-REPORT", names,
            to +: name.token +: body :+ end) }

  def formals: Parser[Seq[Identifier]] =
    opt(openBracket ~! rep(identifier) <~ closeBracket) ^^ {
      case Some(_ ~ names) =>
        names
      case _ =>
        Seq()
    }

  /// helpers

  def tokenType(expected: String, tpe: TokenType): Parser[Token] =
    acceptMatch(expected, { case t @ Token(_, `tpe`, _) => t })

  def eof: Parser[Token] =
    tokenType("eof", TokenType.EOF)

  def openBracket: Parser[Token] =
    tokenType("opening bracket", TokenType.OpenBracket)

  def closeBracket: Parser[Token] =
    tokenType("closing bracket", TokenType.CloseBracket)

  def identifier: Parser[Identifier] =
    tokenType("identifier", TokenType.Ident) ^^ {
      token =>
        Identifier(token.value.asInstanceOf[String], token)}

  def identifierList: Parser[Seq[Identifier]] =
    openBracket ~> rep(identifier) <~ closeBracket

  def stringList: Parser[Seq[Token]] =
    openBracket ~> rep(string) <~ closeBracket

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
          if (tpe != TokenType.Keyword) =>
        token })

}

// avoid leaking ThreadLocals due to some deprecated stuff in
// Scala 2.10 that will be removed in Scala 2.11.  see
// https://issues.scala-lang.org/browse/SI-4929 and
// https://github.com/scala/scala/commit/dce6b34c38a6d774961ca6f9fd50b11300ecddd6
// - ST 1/3/13
trait Cleanup extends scala.util.parsing.combinator.Parsers {
  def cleanup() {
    val field = getClass.getDeclaredField(
      "scala$util$parsing$combinator$Parsers$$lastNoSuccessVar")
    field.setAccessible(true)
    val field2 = classOf[scala.util.DynamicVariable[_]].getDeclaredField("tl")
    require(field2 != null)
    field2.setAccessible(true)
    field2.get(field.get(this)).asInstanceOf[java.lang.ThreadLocal[_]].remove()
    field.set(this, null)
  }
}

/// Allows our combinators to take their input from a Seq.

class SeqReader[T](xs: Seq[T], fn: T => Int)
    extends scala.util.parsing.input.Reader[T] {
  case class Pos(pos: Int) extends scala.util.parsing.input.Position {
    def column = pos
    def line = 0
    def lineContents = ""
  }
  def atEnd = xs.isEmpty
  def first = xs.head
  def rest = new SeqReader(xs.tail, fn)
  def pos = Pos(fn(xs.head))
}
