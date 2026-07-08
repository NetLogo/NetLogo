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
  org.nlogo.core.{ I18N, Token, TokenType, StructureDeclarations },
  StructureDeclarations._

object StructureCombinators {
  def parse(tokens: Iterator[Token], filename: String): Either[(String, Token), Seq[Declaration]] = {
    val reader = new SeqReader[Token](tokens.to(LazyList), _.start)
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
              Token.eof(filename)
          else
            rest.first
        Left((msg, token))
      case combinators.Error(msg, rest) =>
        val token =
          if (rest.atEnd)
            if (tokens.hasNext)
              tokens.next()
            else
              Token.eof(filename)
          else
            rest.first
        Left((msg, token))
      case combinators.Failure(msg, rest) =>
        val token =
          if (rest.atEnd)
            if (tokens.hasNext)
              tokens.next()
            else
              Token.eof(filename)
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
    rep(declaration | procedure) <~ (eof | failure("keyword expected"))

  def declaration: Parser[Declaration] =
    `export` | `import` | includes | extensions | breed | directedLinkBreed | undirectedLinkBreed |
      variables("GLOBALS") | variables("TURTLES-OWN") | variables("PATCHES-OWN") |
      variables("LINKS-OWN") | breedVariables

  def includes: Parser[Includes] =
    keyword("__INCLUDES") ~! stringList ^^ {
      case token ~ (names ~ closeBracket) =>
        Includes(token, names, closeBracket) }

  def `export`: Parser[Export] =
    keyword("EXPORT") ~! identifierList ^^ {
      case keyword ~ (exportedNames ~ _) =>
        Export(exportedNames, keyword)
    }

  def `import`: Parser[Import] = importSimple | importIdentifiers

  def importSimple: Parser[Import] =
    keyword("IMPORT") ~ rep1sep(plainIdentifier, colon) ~ opt(exactIdentifier("AS") ~> identifier) ^^ {
      case keyword ~ components ~ pathAliasOption =>
        Import(components.map(_.text.toUpperCase), pathAliasOption.map(_.name), Map(), keyword)
    }

  def importIdentifiers: Parser[Import] =
    keyword("IMPORT") ~ importedIdentifierList ~ exactIdentifier("FROM") ~ rep1sep(plainIdentifier, colon) ^^ {
      case keyword ~ importList ~ _ ~ components =>
        Import(components.map(_.text.toUpperCase), None, importList, keyword)
    }

  def renamableIdentifier: Parser[(String, String)] =
    identifier ^^ (x => (x.name, x.name)) |
    openParen ~>! identifier ~ exactIdentifier("AS") ~ identifier <~ closeParen ^^ {
      case from ~ _ ~ to => (from.name, to.name)
    }

  def importedIdentifierList: Parser[Map[String, String]] = {
    def hasDuplicates(xs: List[String], found: Set[String] = Set()): Boolean = {
      !xs.isEmpty && (found(xs.head) || hasDuplicates(xs.tail, found + xs.head))
    }

    val parser = openBracket ~> commit(rep(renamableIdentifier) <~ closeBracket) ^^ {
      case xs => {
        val (src, dst) = xs.unzip

        if (hasDuplicates(src) || hasDuplicates(dst)) {
          Left(I18N.errors.get("compiler.StructureCombinators.duplicateIdentifiers"))
        } else {
          Right(xs.toMap)
        }
      }
    }

    parser.flatMap(_.fold(err, success))
  }

  def extensions: Parser[Extensions] =
    keyword("EXTENSIONS") ~! identifierList ^^ {
      case token ~ (names ~ closeBracket) =>
        Extensions(token, names, closeBracket) }

  def variables(key: String): Parser[Variables] =
    keyword(key) ~! identifierList ^^ {
      case keyToken ~ (names ~ closeBracket) =>
        Variables(Identifier(key, keyToken), names, keyToken, closeBracket) }

  def breedVariables: Parser[Variables] =
    acceptMatch("<breed>-own", {
      case token @ Token(_, TokenType.Keyword, value: String)
          if value.endsWith("-OWN") =>
        token }) ~!
      identifierList ^^ {
        case token ~ (names ~ closeBracket) =>
          Variables(Identifier(token.value.asInstanceOf[String], token), names, token, closeBracket) }

  def breed: Parser[Breed] = {
    breedKeyword ~! breedBlock("BREED") ^^ {
      case keyword ~ (plural ~ singular ~ closeBracket) =>
        Breed(plural, singular, false, false, keyword, closeBracket)
    }
  }

  def breedBlock(breedWord: String): Parser[Identifier ~ Identifier ~ Token] = {
    val breedNames: Parser[Identifier ~ Identifier] = identifier ~ identifier
    val singleBreedName: Parser[Nothing] =
      identifier >> (i =>
          failure(s"Breed declarations must have plural and singular. $breedWord [${i.name}] has only one name."))
    (openBracket ~> (breedNames | singleBreedName) ~ closeBracket)
  }

  def directedLinkBreed: Parser[Breed] =
    keyword("DIRECTED-LINK-BREED") ~! breedBlock("DIRECTED-LINK-BREED") ^^ {
      case keyword ~ (plural ~ singular ~ closeBracket) =>
        Breed(plural, singular, true, true, keyword, closeBracket)
    }

  def undirectedLinkBreed: Parser[Breed] =
    keyword("UNDIRECTED-LINK-BREED") ~! breedBlock("UNDIRECTED-LINK-BREED") ^^ {
      case keyword ~ (plural ~ singular ~ closeBracket) =>
        Breed(plural, singular, true, false, keyword, closeBracket)
    }

  def procedure: Parser[Procedure] =
    (keyword("TO") | keyword("TO-REPORT")) ~!
      identifier ~
      opt(identifierList) ~
      rep(identifierToken | nonKeyword) ~
      (keyword("END") | failure("END expected")) ^^ {
        case to ~ name ~ Some(names ~ closeBracket) ~ body ~ end =>
          Procedure(name, to.value == "TO-REPORT", names, to +: name.token +: body :+ end)

        case to ~ name ~ None ~ body ~ end =>
          Procedure(name, to.value == "TO-REPORT", Seq(), to +: name.token +: body :+ end)
      }

  /// helpers

  def tokenType(expected: String, tpe: TokenType): Parser[Token] =
    acceptMatch(expected, { case t @ Token(_, `tpe`, _) => t })

  def eof: Parser[Token] =
    tokenType("eof", TokenType.Eof)

  def openBracket: Parser[Token] =
    tokenType("opening bracket", TokenType.OpenBracket)

  def closeBracket: Parser[Token] =
    tokenType("closing bracket", TokenType.CloseBracket)

  def openParen: Parser[Token] =
    tokenType("opening paren", TokenType.OpenParen)

  def closeParen: Parser[Token] =
    tokenType("closing paren", TokenType.CloseParen)

  def colon: Parser[Token] =
    tokenType("colon", TokenType.Colon)

  def plainIdentifier: Parser[Token] =
    tokenType("identifier", TokenType.Ident)

  def anyKeyword: Parser[Token] =
    tokenType("keyword", TokenType.Keyword)

  def exactIdentifier(text: String): Parser[Token] =
    acceptMatch("identifier", {
      case t @ Token(_, TokenType.Ident, `text`) =>
        t })

  def identifierToken: Parser[Token] =
    chainl1(plainIdentifier, plainIdentifier | literalToken | anyKeyword, colon ^^ {_ => {
      case (p, x) => {
        val newText = p.text + ":" + x.text
        val newValue = p.value.asInstanceOf[String] + ":" + x.value
        val newSourcelocation = p.sourceLocation.copy(end = x.sourceLocation.end)

        p.copy(text = newText, value = newValue)(newSourcelocation) }}})

  def identifier: Parser[Identifier] =
    identifierToken ^^ (x => Identifier(x.value.toString, x))

  def literalToken: Parser[Token] =
    tokenType("literal", TokenType.Literal)

  def literal: Parser[Identifier] =
    literalToken ^^ (token => Identifier(token.value.toString, token))

  def identifierList: Parser[Seq[Identifier] ~ Token] =
    openBracket ~> commit(rep(identifier | literal) ~ closeBracket)

  def plainIdentifierList: Parser[Seq[Token]] =
    openBracket ~> commit(rep(plainIdentifier | literalToken) <~ closeBracket)

  def stringList: Parser[Seq[Token] ~ Token] =
    openBracket ~> commit(rep(string) ~ closeBracket)

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
