// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.parse

import java.util.Locale

import org.nlogo.core.{ CompilationOperand, FrontEndInterface, ProcedureDefinition, ProcedureSyntax, SourceLocation,
  SourceRewriter, StructureResults, Token, TokenType, TokenizerInterface }
import org.nlogo.core.StructureDeclarations.{ Breed, Identifier, Declaration, Procedure }

import scala.util.matching.Regex

class AstRewriter(val tokenizer: TokenizerInterface, frontEnd: FrontEndInterface, op: CompilationOperand) extends SourceRewriter with NetLogoParser {

  def preserveBody(structureResults: StructureResults, header: String, procedures: String, footer: String): String =
    header + procedures + footer

  override def expandConciseBreeds(): String = {
    val source: String = op.sources("")

    val breeds: Seq[Token] = tokenizer.tokenizeString(source).sliding(4).foldLeft(Seq()) {
      case (acc, Seq(Token(_, _, "BREED"), Token(_, TokenType.OpenBracket, _), token @ Token(_, _, _),
                     Token(_, TokenType.CloseBracket, _))) =>
        acc :+ token

      case (acc, _) =>
        acc
    }

    breeds.foldLeft(RewriteContext(source)) {
      case (ctx, token) =>
        ctx.through(token.start).through(token.end, Some(s"${token.text} a-${token.text}"))
    }.throughEnd.text
  }

  override def renameBreedSingular(breed: String, replacement: String): String = {
    val source: String = op.sources("")

    val result = frontEnd.findDeclarations(source, "").find {
      case Breed(_, Identifier(name, _), _, _, _, _) =>
        name.equalsIgnoreCase(breed)

      case _ =>
        false
    } match {
      case Some(Breed(_, Identifier(_, token), linkBreed, _, _, _)) =>
        val newSource: String = source.substring(0, token.start) + replacement + source.substring(token.end)

        tokenizer.tokenizeWithWhitespace(newSource, "").map { token =>
          if (token.text.equalsIgnoreCase(breed)) {
            replacement
          } else if (token.text.equalsIgnoreCase(s"is-$breed?")) {
            s"is-$replacement?"
          } else {
            if (linkBreed) {
              if (token.text.equalsIgnoreCase(s"create-$breed-from")) {
                s"create-$replacement-from"
              } else if (token.text.equalsIgnoreCase(s"create-$breed-to")) {
                s"create-$replacement-to"
              } else if (token.text.equalsIgnoreCase(s"create-$breed-with")) {
                s"create-$replacement-with"
              } else if (token.text.equalsIgnoreCase(s"in-$breed-from")) {
                s"in-$replacement-from"
              } else if (token.text.equalsIgnoreCase(s"in-$breed-neighbor?")) {
                s"in-$replacement-neighbor"
              } else if (token.text.equalsIgnoreCase(s"in-$breed-neighbors")) {
                s"in-$replacement-neighbors"
              } else if (token.text.equalsIgnoreCase(s"$breed-neighbor?")) {
                s"$replacement-neighbor?"
              } else if (token.text.equalsIgnoreCase(s"$breed-neighbors")) {
                s"$replacement-neighbors"
              } else if (token.text.equalsIgnoreCase(s"$breed-with")) {
                s"$replacement-with"
              } else if (token.text.equalsIgnoreCase(s"out-$breed-neighbor?")) {
                s"out-$replacement-neighbor?"
              } else if (token.text.equalsIgnoreCase(s"out-$breed-neighbors")) {
                s"out-$replacement-neighbors"
              } else if (token.text.equalsIgnoreCase(s"out-$breed-to")) {
                s"out-$replacement-to"
              } else {
                token.text
              }
            } else {
              token.text
            }
          }
        }.mkString

      case _ =>
        source
    }

    result
  }

  override def renameBreedPlural(breed: String, replacement: String): String = {
    val source: String = op.sources("")

    frontEnd.findDeclarations(source, "").find {
      case Breed(Identifier(name, _), _, _, _, _, _) =>
        name.equalsIgnoreCase(breed)

      case _ =>
        false
    } match {
      case Some(Breed(Identifier(_, token), _, linkBreed, _, _, _)) =>
        val newSource: String = source.substring(0, token.start) + replacement + source.substring(token.end)

        tokenizer.tokenizeWithWhitespace(newSource, "").map { token =>
          if (token.text.equalsIgnoreCase(breed)) {
            replacement
          } else if (token.text.equalsIgnoreCase(s"$breed-own")) {
            s"$replacement-own"
          } else {
            if (linkBreed) {
              if (token.text.equalsIgnoreCase(s"create-$breed-from")) {
                s"create-$replacement-from"
              } else if (token.text.equalsIgnoreCase(s"create-$breed-to")) {
                s"create-$replacement-to"
              } else if (token.text.equalsIgnoreCase(s"create-$breed-with")) {
                s"create-$replacement-with"
              } else if (token.text.equalsIgnoreCase(s"my-$breed")) {
                s"my-$replacement"
              } else if (token.text.equalsIgnoreCase(s"my-in-$breed")) {
                s"my-in-$replacement"
              } else if (token.text.equalsIgnoreCase(s"my-out-$breed")) {
                s"my-out-$replacement"
              } else {
                token.text
              }
            } else if (token.text.equalsIgnoreCase(s"$breed-at")) {
              s"$replacement-at"
            } else if (token.text.equalsIgnoreCase(s"$breed-here")) {
              s"$replacement-here"
            } else if (token.text.equalsIgnoreCase(s"$breed-on")) {
              s"$replacement-on"
            } else if (token.text.equalsIgnoreCase(s"create-$breed")) {
              s"create-$replacement"
            } else if (token.text.equalsIgnoreCase(s"create-ordered-$breed")) {
              s"create-ordered-$replacement"
            } else if (token.text.equalsIgnoreCase(s"hatch-$breed")) {
              s"hatch-$replacement"
            } else if (token.text.equalsIgnoreCase(s"sprout-$breed")) {
              s"sprout-$replacement"
            } else {
              token.text
            }
          }
        }.mkString

      case _ =>
        source
    }
  }

  override def reorderDeclarations(): String = {
    val source: String = op.sources("")

    val decls: Seq[Declaration] = frontEnd.findDeclarations(source, "")

    if (decls.nonEmpty) {
      val sorted: Seq[Declaration] = decls.sortBy(_.start.start)

      val firstRange: (Declaration, String) = (sorted(0), source.substring(0, sorted(0).end.end))

      val ranges: Seq[(Declaration, String)] = sorted.sliding(2).foldLeft(Seq(firstRange)) {
        case (acc, Seq(one, two)) =>
          acc :+ (two, source.substring(one.end.end, two.end.end))

        case (acc, _) =>
          acc
      }

      val (valid, (invalid, procs)) = ranges.span(!_._1.isInstanceOf[Procedure]) match {
        case (valid, other) =>
          (valid, other.partition(!_._1.isInstanceOf[Procedure]))
      }

      (valid :++ invalid :++ procs).map(_._2).mkString + source.substring(sorted.last.end.end)
    } else {
      source
    }
  }

  private def rewriteSimple(visitor: RewriteFolder): String = {
    basicParse(op)._1.filter(_.procedure.filename == "").foldLeft(RewriteContext(op.sources(""))) {
      case (ctx, proc) =>
        visitor.visitProcedureDefinition(proc)(ctx)
    }.throughEnd.text
  }

  override def remove(command: String): String =
    rewriteSimple(new RemoveVisitor(command))

  override def addCommand(command: String, addition: String): String =
    rewriteSimple(new AddVisitor(command, addition))

  override def replace(primitive: String, replacement: String): String =
    rewriteSimple(new ReplaceVisitor(primitive, replacement))

  def lambdaize(): String = {
    rewrite(new Lambdaizer, preserveBody)
  }

  def runVisitor(folder: PositionalAstFolder[AstEdit]): String = {
    rewrite(folder, preserveBody)
  }

  def replaceToken(original: String, replacement: String): String = {
    val source = op.sources("")
    val tokens = tokenizer.tokenizeString(source)
    val buf = new StringBuilder(source)
    var offset = 0
    for (token <- tokens if token.text.equalsIgnoreCase(original)) {
      buf.replace(token.start + offset, token.end + offset, replacement)
      offset += replacement.length - token.text.length
    }
    buf.toString
  }

  def addExtension(newExtension: String): String = {
    rewrite(NoopFolder, declarationReplace("extensions", extensionsRegex, _.extensions.map(_.text), newExtension))
  }

  def removeExtension(extension: String): String = {
    import TokenType.{OpenBracket, CloseBracket, Ident}
    val extValue = extension.toUpperCase(Locale.ENGLISH)
    val source = op.sources("")
    val tokens = tokenizer.tokenizeString(source).to(LazyList)
    val buf = new StringBuilder(source)

    val (beforeExtensions, rest) = tokens.span(t => t.value != "EXTENSIONS" && t.tpe == Ident)
    val endIndex = rest.indexWhere(_.tpe == CloseBracket)
    val extensions = tokens.take(endIndex + 1)
    val afterExtensions = tokens.drop(endIndex + 1)

    extensions.toList match {
      case extKeyword +: Token(_, OpenBracket, _)
            +: Token(_, Ident, extName)
            +: (close @ Token(_, CloseBracket, _))
            +: Nil if extName == extValue =>
        // eat empty lines
        if (beforeExtensions.nonEmpty) {
          buf.replace(beforeExtensions.last.end, close.end, "")
        } else if (afterExtensions.nonEmpty) {
          buf.replace(extKeyword.start, afterExtensions.head.start, "")
        } else {
          buf.replace(extKeyword.start, close.end, "")
        }
      case extSection =>
        extSection.span(t => t.value != extValue || t.tpe != Ident) match {
          case (_ :+ (precedingToken @ Token(_, Ident, _)), extToken +: _) =>
            // Preceding token is an extension. Make whatever comes after the extension come after the preceding
            // extension instead.
            buf.replace(precedingToken.end, extToken.end, "")
          case (_, extToken +: followingToken +: _) =>
            // The token before this one isn't an extension. In a valid file, it must be an open bracket.
            // So preserve spacing before the extension, and move the next token over to where this one starts
            buf.replace(extToken.start, followingToken.start, "")
          case _ => // not found
        }
    }
    buf.toString
  }

  def addGlobal(newGlobal: String): String = {
    rewrite(NoopFolder,
            declarationReplace("globals", globalsRegex, _.program.userGlobals.map(_.toLowerCase), newGlobal))
  }

  def addReporterProcedure(name: String, args: Seq[String], body: String): String = {
    val formattedArgs = if (args.isEmpty) "" else args.mkString(" [ ", " ", " ]")
    val procedureContent =
      s"""|
          |to-report $name$formattedArgs
          |${body}
          |end""".stripMargin
    rewrite(NoopFolder, procedureAdd(procedureContent))
  }

  private val extensionsRegex = new Regex("(?i)(?m)extensions\\s+\\[[^]]*\\]")
  private val globalsRegex = new Regex("(?i)(?m)globals\\s+\\[[^]]*\\]")

  private def declarationReplace(
      declKeyword: String,
      declRegex: Regex,
      declItems: StructureResults => Seq[String],
      addedItem: String)(res: StructureResults, headers: String, procedures: String, footer: String): String = {
    val newDecl =
      declKeyword + " " + (declItems(res) :+ addedItem).distinct.mkString("[", " ", "]")
    val modifiedHeaders = declRegex
      .findFirstMatchIn(headers)
      .map(m => headers.take(m.start) + newDecl + headers.drop(m.end))
      .getOrElse(newDecl + "\n" + headers)
    modifiedHeaders + procedures + footer
  }

  private def procedureAdd(
      content: String)(res: StructureResults, headers: String, procedures: String, footer: String): String = {
    headers + procedures + "\n" + content + "\n" + footer
  }

  def rewrite(visitor: PositionalAstFolder[AstEdit],
              wholeFile: (StructureResults, String, String, String) => String,
              sourceName: String = ""): String = {

    val (procs, structureResults) = basicParse(op)

    def getSource(filename: String): String =
      op.sources
        .get(filename)
        .orElse(IncludeFile(op.compilationEnvironment, filename).map(_._2))
        .getOrElse(throw new Exception("Unable to find file: " + filename))

    val (wsMap, fileHeaders, fileFooters) = trackWhitespace(getSource, procs)

    val procsToRewrite = procs.filter(_.filename == sourceName)

    val edit = procsToRewrite.foldLeft(AstEdit(Map[AstPath, AstFormat.Operation](), wsMap)) {
      case (edit, proc) => visitor.visitProcedureDefinition(proc)(edit)
    }

    val headers = fileHeaders.getOrElse("", "")
    val footer = fileFooters.getOrElse("", "")

    val eolWhitespace = new Regex("\\s+$")
    val rewrittenProcedures = format(edit, procsToRewrite)
    val rewritten =
      wholeFile(structureResults, headers, rewrittenProcedures, footer)
    rewritten.linesIterator.map(eolWhitespace.replaceAllIn(_, "")).mkString("\n")
  }

  def trackWhitespace(
      getSource: String => String,
      procs: Iterable[ProcedureDefinition]): (WhitespaceMap, Map[String, String], Map[String, String]) = {
    val ws = new WhiteSpace.Tracker(getSource, tokenizer)
    var fileHeaders: Map[String, String] = Map()
    var fileFooters: Map[String, String] = Map()
    var procedurePositions: Map[String, Map[String, ProcedureSyntax]] = Map()

    def procedurePosition(file: String, procedureName: String): ProcedureSyntax = {
      if (procedurePositions.isDefinedAt(file))
        procedurePositions(file)(procedureName)
      else {
        val newPositions =
          findProcedurePositions(getSource(file), Some(op.containingProgram.dialect)).map {
            case (k, v) => k.toUpperCase(Locale.ENGLISH) -> v
          }
        procedurePositions = procedurePositions + (file -> newPositions)
        newPositions(procedureName)
      }
    }

    def addTrailingWhitespace(ctx: WhiteSpace.Context): Unit = {
      ctx.lastPosition.foreach {
        case (_, SourceLocation(_, lastIndex, lastFile)) =>
          fileFooters = (fileFooters + (lastFile -> getSource(lastFile).drop(lastIndex)))
      }
    }

    val whiteSpaces =
      procs.foldLeft((WhitespaceMap.empty, WhiteSpace.Context.empty)) {
        case ((whitespaceLog, ctx), proc) =>
          val procSyntax = procedurePosition(proc.filename, proc.procedure.name)
          val newContext =
            if (ctx.lastPosition.map(_._2.filename).contains(proc.filename)) {
              WhiteSpace.Context.empty(ctx.lastPosition)
            } else {
              val procStart = procSyntax.declarationKeyword.start
              fileHeaders = (fileHeaders + (proc.filename -> getSource(proc.filename).slice(0, procStart)))
              WhiteSpace.Context.empty(Some((AstPath(), SourceLocation(procStart, procStart, proc.filename))))
            }
          val r = ws.visitProcedureDefinition(proc)(newContext)
          addTrailingWhitespace(r)
          (whitespaceLog ++ r.whitespaceLog, r)
      }
    if (procs.isEmpty) {
      fileHeaders = fileHeaders + ("" -> getSource(""))
    }
    addTrailingWhitespace(whiteSpaces._2)
    (whiteSpaces._1, fileHeaders, fileFooters)
  }

  def format(edit: AstEdit, procs: Iterable[ProcedureDefinition]): String = {
    import edit.{operations, wsMap}
    val formatter = new Formatter
    val res = procs
      .filter(p => op.sources.contains(p.procedure.filename.getOrElse("")))
      .foldLeft[AstFormat](Formatter.context("", operations, wsMap = wsMap)) {
        case (acc, proc) =>
          formatter.visitProcedureDefinition(proc)(Formatter.context(acc.text, acc.operations, wsMap = wsMap))
      }
    res.text
  }
}
