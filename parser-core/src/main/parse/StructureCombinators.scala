// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.parse

/// Stage #1 of StructureParser

// This knows practically nothing about the rest of NetLogo.
// We use core.{ Token, TokenType } and nothing else.

import org.nlogo.core.{ Token, TokenType, StructureDeclarations },
  StructureDeclarations.{ Breed, Declaration, Extensions, Identifier, Includes, Procedure, Variables }

import scala.collection.BufferedIterator
import scala.util.{ Failure, Success, Try }

object StructureCombinators {
  def parse(tokens: Iterator[Token], filename: String): Either[(String, Token), Seq[Declaration]] = {
    val buffered: BufferedIterator[Token] = tokens.buffered
    val parser = new StructureCombinators(buffered, filename)

    parser.parseProgram().fold(
      ex => Left((ex.getMessage, buffered.headOption.getOrElse(Token.eof(filename)))),
      Right(_)
    )
  }
}

class StructureCombinators(tokens: BufferedIterator[Token], filename: String) {
  def parseProgram(): Try[Seq[Declaration]] = {
    val prefix = skipWhitespace()

    tokens.head match {
      case Token(_, TokenType.Eof, _) =>
        Success(Seq())

      case Token(_, TokenType.Keyword, name: String) =>
        (name match {
          case "__INCLUDES" => parseIncludes(prefix)
          case "EXTENSIONS" => parseExtensions(prefix)
          case "DIRECTED-LINK-BREED" => parseBreed(prefix, true, true)
          case "UNDIRECTED-LINK-BREED" => parseBreed(prefix, true, false)
          case "TO" => parseProcedure(prefix, false)
          case "TO-REPORT" => parseProcedure(prefix, true)
          case _ => parseVariables(prefix)
        }).flatMap(decl => parseProgram().map(decl +: _))

      case Token(_, TokenType.Ident, "BREED") =>
        parseBreed(prefix, false, false).flatMap(decl => parseProgram().map(decl +: _))

      case next =>
        Failure(new Exception("keyword expected"))
    }
  }

  private def parseIncludes(prefix: Seq[Token]): Try[Includes] = {
    val includes = tokens.next()
    val toOpen = prefix ++: includes +: skipWhitespace()

    nextToken(_.tpe == TokenType.OpenBracket, "opening bracket expected").flatMap { open =>
      val toClose = tokensWhile {
        case Token(_, TokenType.Literal, _: String) => true
        case Token(_, TokenType.Whitespace | TokenType.Comment, _) => true
      }

      val names = toClose.filter(_.tpe == TokenType.Literal)

      nextToken(_.tpe == TokenType.CloseBracket, "closing bracket expected").flatMap { close =>
        Success(Includes(includes, names, toOpen :+ open :++ toClose :+ close))
      }
    }
  }

  private def parseExtensions(prefix: Seq[Token]): Try[Extensions] = {
    val extensions = tokens.next()
    val toOpen = prefix ++: extensions +: skipWhitespace()

    nextToken(_.tpe == TokenType.OpenBracket, "opening bracket expected").flatMap { open =>
      val toClose = tokensWhile {
        case Token(_, TokenType.Ident | TokenType.Whitespace | TokenType.Comment, _) => true
      }

      val names = toClose.collect {
        case token @ Token(_, TokenType.Ident, value: String) =>
          Identifier(value, token)
      }

      nextToken(_.tpe == TokenType.CloseBracket, "closing bracket expected").flatMap { close =>
        Success(Extensions(extensions, names, toOpen :+ open :++ toClose :+ close))
      }
    }
  }

  private def parseBreed(prefix: Seq[Token], link: Boolean, directed: Boolean): Try[Breed] = {
    val breed = tokens.next()
    val toOpen = prefix ++: breed +: skipWhitespace()

    nextToken(_.tpe == TokenType.OpenBracket, "opening bracket expected").flatMap { open =>
      val toClose = tokensWhile {
        case Token(_, TokenType.Ident | TokenType.Whitespace | TokenType.Comment, _) => true
      }

      val names = toClose.collect {
        case token @ Token(_, TokenType.Ident, value: String) =>
          Identifier(value, token)
      }

      if (names.isEmpty) {
        Failure(new Exception("identifier expected"))
      } else if (names.size == 1) {
        val breedName = s"${breed.value} \"${names(0).name}\""
        val error = s"Breed declarations must have plural and singular. $breedName has only one name."

        Failure(new Exception(error))
      } else {
        nextToken(_.tpe == TokenType.CloseBracket, "closing bracket expected").flatMap { close =>
          Success(Breed(breed, names(0), names(1), link, directed, toOpen :+ open :++ toClose :+ close))
        }
      }
    }
  }

  private def parseVariables(prefix: Seq[Token]): Try[Variables] = {
    val breed = tokens.next()
    val toOpen = prefix ++: breed +: skipWhitespace()

    nextToken(_.tpe == TokenType.OpenBracket, "opening bracket expected").flatMap { open =>
      val toClose = tokensWhile {
        case Token(_, TokenType.Ident | TokenType.Literal | TokenType.Whitespace | TokenType.Comment, _) => true
      }

      val names = toClose.collect {
        case token @ Token(_, TokenType.Ident | TokenType.Literal, value) =>
          Identifier(value.toString, token)
      }

      nextToken(_.tpe == TokenType.CloseBracket, "closing bracket expected").flatMap { close =>
        val breedIdent = Identifier(breed.value.asInstanceOf[String], breed)

        Success(Variables(breedIdent, names, toOpen :+ open :++ toClose :+ close))
      }
    }
  }

  private def parseProcedure(prefix: Seq[Token], reporter: Boolean): Try[Procedure] = {
    val to = tokens.next()
    val toName = prefix ++: to +: skipWhitespace()

    nextToken(_.tpe == TokenType.Ident, "identifier expected").flatMap { name =>
      val toOpen = skipWhitespace()

      tokens.headOption.filter(_.tpe == TokenType.OpenBracket).fold(Success(Seq())) { open =>
        tokens.next()

        val toClose = tokensWhile {
          case Token(_, TokenType.Ident | TokenType.Literal | TokenType.Whitespace | TokenType.Comment, _) => true
        }

        nextToken(_.tpe == TokenType.CloseBracket, "closing bracket expected").flatMap { close =>
          Success(open +: toClose :+ close)
        }
      }.flatMap { inputs =>
        val inputIdents = inputs.collect {
          case token @ Token(_, TokenType.Ident | TokenType.Literal, value) =>
            Identifier(value.toString, token)
        }

        val body = tokensWhile(_.tpe != TokenType.Keyword)

        nextToken(_.value == "END", "END expected").flatMap { end =>
          val nameIdent = Identifier(name.value.asInstanceOf[String], name)
          val tokens = to +: name +: body :+ end
          val range = toName ++: name +: toOpen :++ inputs :++ body :+ end

          Success(Procedure(nameIdent, reporter, inputIdents, tokens, range))
        }
      }
    }
  }

  private def nextToken(f: PartialFunction[Token, Boolean], error: String): Try[Token] =
    tokens.nextOption.filter(f.lift(_).getOrElse(false)).fold(Failure(new Exception(error)))(Success(_))

  private def tokensWhile(f: PartialFunction[Token, Boolean]): Seq[Token] =
    tokens.headOption.filter(f.lift(_).getOrElse(false)).fold(Seq())(_ => tokens.next() +: tokensWhile(f))

  private def skipWhitespace(): Seq[Token] = {
    tokensWhile {
      case Token(_, TokenType.Whitespace | TokenType.Comment, _) => true
    }
  }
}
