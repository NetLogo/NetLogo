// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.parse

import org.nlogo.core.{
  CompilationOperand,
  Femto,
  ProcedureDefinition,
  ProcedureSyntax,
  SourceLocation,
  SourceRewriter,
  StructureResults,
  Token,
  TokenType,
  TokenizerInterface
}

import scala.util.matching.Regex

class AstRewriter(val tokenizer: TokenizerInterface, op: CompilationOperand) extends SourceRewriter with NetLogoParser {

  def preserveBody(structureResults: StructureResults, header: String, procedures: String, footer: String): String =
    header + procedures + footer

  def remove(dropCommand: String): String = {
    rewrite(new RemovalVisitor(dropCommand), preserveBody _)
  }

  def addCommand(addCommand: (String, String)): String = {
    rewrite(new AddVisitor(addCommand), preserveBody _)
  }

  def replaceCommand(replaceCommand: (String, String)): String = {
    rewrite(new ReplaceVisitor(replaceCommand), preserveBody _)
  }

  def replaceReporter(replaceReporter: (String, String)): String = {
    rewrite(new ReplaceReporterVisitor(replaceReporter), preserveBody _)
  }

  def customRewrite(className: String): String = {
    rewrite(Femto.get(className), preserveBody _)
  }

  def runVisitor(folder: PositionalAstFolder[AstEdit]): String = {
    rewrite(folder, preserveBody _)
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
    val extValue = extension.toUpperCase
    val source = op.sources("")
    val tokens = tokenizer.tokenizeString(source).toStream
    val buf = new StringBuilder(source)

    def extensions(tokens: Stream[Token]): Seq[Token] = tokens match {
      case Token(_, Ident, "EXTENSIONS") #:: Token(_, OpenBracket, _) #:: rest => {
        val (exts, after) = rest.span(_.tpe == Ident)
        tokens.take(2) ++ exts ++ after.take(1)
      }
      case _ #:: rest   => extensions(rest)
      case Stream.Empty => Seq.empty[Token]
    }

    extensions(tokens) match {
      case extKeyword +: Token(_, OpenBracket, _)
            +: Token(_, Ident, extName)
            +: (close @ Token(_, CloseBracket, _))
            +: Nil if extName == extValue =>
        // eat empty lines
        val end = (close.end until source.length).find(_ == '\n').getOrElse(source.length)
        buf.replace(extKeyword.start, end, "")
      case extSection =>
        extSection.span(_.value != extValue) match {
          case (_ :+ precedingToken, extToken +: _) =>
            // This basically preserves formatting exactly. So
            // extensions [ abc def ] => extensions [ abc ]
            // extensions [abc def] => extension [abc]
            // It just makes whatever comes after the extension come after the preceding extension instead.
            buf.replace(precedingToken.end, extToken.end, "")
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

    val (wsMap, fileHeaders, fileFooters) = trackWhitespace(getSource _, procs)

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
    rewritten.lines.map(eolWhitespace.replaceAllIn(_, "")).mkString("\n")
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
            case (k, v) => k.toUpperCase -> v
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
      .filter(p => op.sources.contains(p.procedure.filename))
      .foldLeft[AstFormat](Formatter.context("", operations, wsMap = wsMap)) {
        case (acc, proc) =>
          formatter.visitProcedureDefinition(proc)(Formatter.context(acc.text, acc.operations, wsMap = wsMap))
      }
    res.text
  }
}
