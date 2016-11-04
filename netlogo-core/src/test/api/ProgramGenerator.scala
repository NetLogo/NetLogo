// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import
  org.nlogo.core.{ Token, TokenType }

import
  org.scalacheck.{ Arbitrary, Gen, Shrink }, Gen.{ listOf, oneOf }

import
  org.scalatest.{ FunSuite, prop, PropSpec },
    prop.GeneratorDrivenPropertyChecks

trait ProgramGenerator extends GeneratorDrivenPropertyChecks {
  val breedTypes = Seq("breed", "directed-link-breed", "undirected-link-breed")

  val procedureTypes = Seq("to", "to-report")
  // we don't load extensions in this step, but we use ones that are recognizable as extensions
  val extensionNames: Gen[String] = oneOf("array", "table")
  // we don't need to check for uniqueness at the parse level
  val ident: Gen[String] = oneOf("foo", "bar", "baz", "qux")

  def wellFormedPrograms: Gen[RawProgram] = for {
    inc        <- includes
    ext        <- extensions
    globs      <- globals
    linksOwn   <- agentsOwn("links")
    patchesOwn <- agentsOwn("patches")
    turtlesOwn <- agentsOwn("turtles")
    breeds     <- listOf(breed)
    procedures <- listOf(procedure)
    aComment   <- comment
    elems      <- Gen.someOf(inc, ext, globs, linksOwn, patchesOwn, turtlesOwn, aComment)
  } yield RawProgram(elems ++ breeds ++ procedures)

  def programElement: Gen[ProgramElement] =
    Gen.oneOf(includes, extensions, globals, agentsOwn("links"), agentsOwn("patches"), agentsOwn("turtles"), breed, procedure, comment)

  def extensions: Gen[Extensions] =
    for { idents <- listOf(ident) } yield new Extensions(idents)

  def globals: Gen[Globals] =
    for { idents <- listOf(ident) } yield new Globals(idents)

  def includes: Gen[Includes] =
    for { fileNames <- listOf(ident.map(_ + ".nls")) } yield new Includes(fileNames)

  def agentsOwn(agent: String): Gen[AgentOwns] =
    for { idents <- listOf(ident) } yield AgentOwns(agent, idents)

  def breed: Gen[Breed] =
    for {
      typeOfBreed <- oneOf(breedTypes)
      breedName   <- ident
      breedOwns   <- listOf(ident)
    } yield new Breed(typeOfBreed, breedName + "s", breedName, breedOwns)

  def procedure: Gen[Procedure] =
    for {
      typeOfProc <- oneOf(procedureTypes)
      name       <- ident
      body       <- listOf(ident)
      args       <- listOf(ident)
    } yield Procedure(typeOfProc, name, args, body.mkString(" "))

  def comment: Gen[TopLevelComment] =
    for {
      text <- listOf(ident)
      if text.nonEmpty
    } yield TopLevelComment(text.mkString(" "))

  def onceMangledProgram: Gen[MangledProgram] =
    for {
      program   <- wellFormedPrograms.suchThat(_.nonEmpty)
      elem      <- Gen.oneOf(program.elems)
      corrupter <- Gen.oneOf(FirstSyntaxDeleter, LastSyntaxDeleter)
      if corrupter.appliesTo(elem)
        corruptedElem = Left(corrupter(elem))
      otherElems = (program.elems.toSet - elem).map(Right(_)).toSeq
    } yield MangledProgram(corruptedElem +: otherElems)

  implicit def shrinkProgramElements(implicit shrinkSeq: Shrink[Seq[String]]): Shrink[ProgramElement] =
    Shrink.apply((e: ProgramElement) =>
        e match {
          case Globals(globs)   => shrinkSeq.shrink(globs).map(Globals(_))
          case Includes(files)  => shrinkSeq.shrink(files).map(Includes(_))
          case Extensions(exts) => shrinkSeq.shrink(exts).map(Extensions(_))
          case t: TopLevelComment => Stream.empty
          case AgentOwns(agentType, vars) =>
            shrinkSeq.shrink(vars).map(newVars => AgentOwns(agentType, newVars))
          case Procedure(tpe, name, args, body) =>
            shrinkSeq.shrink(args).flatMap(newArgs =>
                shrinkSeq.shrink(body.split(" ").toSeq).map(newBody =>
                Procedure(tpe, name, newArgs, newBody.mkString(" "))))
          case Breed(tpe, plural, singular, owns) =>
            shrinkSeq.shrink(owns).map(newOwns => Breed(tpe, plural, singular, newOwns))
        })

  implicit def shrinkProgram(implicit shrinkElems: Shrink[Seq[ProgramElement]]): Shrink[RawProgram] =
    Shrink.apply((p: RawProgram) =>
        shrinkElems.shrink(p.elems).map(new RawProgram(_)))

  implicit def shrinkMangledProgram(implicit shrinkProg: Shrink[Seq[Either[MangledProgramElement, ProgramElement]]]): Shrink[MangledProgram] =
    Shrink.apply((m: MangledProgram) => shrinkProg.shrink(m.elems).map(MangledProgram(_)))

  trait Corrupter {
    def appliesTo(e: ProgramElement): Boolean
    def apply(e: ProgramElement): MangledProgramElement
  }

  object FirstSyntaxDeleter extends Corrupter {
    def appliesTo(e: ProgramElement): Boolean = e match {
      case t: TopLevelComment => false
      case _ => true
    }

    def apply(e: ProgramElement): MangledProgramElement = DeleteFirstSyntax(e)

    case class DeleteFirstSyntax(originalElement: ProgramElement) extends MangledProgramElement {
      def mangledText = originalElement match {
        case Breed(tpe, plural, singular, owns) if owns.nonEmpty => s"[ $plural $singular ] $plural-own " + owns.mkString("[ ", " ", " ]")
        case Breed(tpe, plural, singular, _) => s"[ $plural $singular ]"
        case BracketedElement(keyword, elems) => elems.mkString("[ ", " ", " ]")
        case p@Procedure(procType, _, _, _) => p.programText.replaceAllLiterally(procType, "")
      }
    }
  }

  object LastSyntaxDeleter extends Corrupter {
    def appliesTo(e: ProgramElement): Boolean = e match {
      case t: TopLevelComment => false
      case _ => true
    }

    def apply(e: ProgramElement): MangledProgramElement = DeleteLastSyntax(e)

    case class DeleteLastSyntax(originalElement: ProgramElement) extends MangledProgramElement {
      def mangledText = originalElement match {
        case p: Procedure => originalText.replaceAllLiterally("end", "")
        case Breed(tpe, plural, singular, owns) if owns.nonEmpty => s"$tpe [ $plural $singular ] $plural-own " + owns.mkString("[ ", " ", "")
        case Breed(tpe, plural, singular, _) => s"$tpe [ $plural $singular"
        case o            => originalText.replaceAllLiterally("]", "")
      }
    }
  }

  trait MangledProgramElement {
    def originalElement: ProgramElement
    def originalText:    String = originalElement.programText
    def mangledText:     String
  }

  case class MangledProgram(elems: Seq[Either[MangledProgramElement, ProgramElement]]) {
    def sortedElements = elems.sortBy {
      case Left(mangled) if mangled.originalElement.isInstanceOf[Procedure] => 2
      case Right(_: Procedure) => 2
      case _ => 1
    }

    def statementCount =
      originalElements.map {
        case b: Breed if b.owns.nonEmpty => 2
        case _ => 1
      }.sum

    def originalElements: Seq[ProgramElement] =
      elems.map {
        case Right(e) => e
        case Left(m) => m.originalElement
      }

    def originalProgramText =
      sortedElements.map {
        case Left(m)  => m.originalText
        case Right(e) => e.programText
      }.mkString("\n")

    def invalidProgramText: String =
      sortedElements.map {
        case Left(m)  => m.mangledText
        case Right(e) => e.programText
      }.mkString("\n")
  }

  trait ProgramElement {
    def programText: String
  }

  object BracketedElement {
    def unapply(be: BracketedElement): Option[(String, Seq[String])] =
      Some((be.keyword, be.contents))
  }

  trait BracketedElement extends ProgramElement {
    def keyword: String
    def contents: Seq[String]
    val programText = keyword + contents.mkString(" [ ", " ", " ]")
  }

  case class Includes(files: Seq[String]) extends BracketedElement {
    def keyword = "__includes"
    def contents = files.map(i => s""""$i"""")
  }

  case class Extensions(exts: Seq[String]) extends BracketedElement {
    def keyword = "extensions"
    def contents = exts
  }

  case class Globals(globs: Seq[String]) extends BracketedElement {
    def keyword = "globals"
    def contents = globs
  }

  case class AgentOwns(agentType: String, vars: Seq[String]) extends BracketedElement {
    def keyword = s"$agentType-own"
    def contents = vars
  }

  case class TopLevelComment(comment: String) extends ProgramElement {
    def programText = "; " + comment
  }

  case class Breed(tpe: String, plural: String, singular: String, owns: Seq[String]) extends ProgramElement {
    val breedDecl = s"$tpe [ $plural $singular ]"
    val ownsDecl  = s"$plural-own " + owns.mkString("[ ", " ", " ]")

    val programText = if (owns.isEmpty) breedDecl else breedDecl + "\n" + ownsDecl
  }

  case class Procedure(tpe: String, name: String, args: Seq[String], body: String) extends ProgramElement {
    val argString = if (args.isEmpty) "" else args.mkString("[ ", " ", " ]")
    val programText = s"${tpe} ${name} ${argString} ${body} end"
  }

  case class RawProgram(elems: Seq[ProgramElement]) {
    def nonEmpty = elems.nonEmpty
    def isEmpty  = elems.isEmpty
    def orderedDeclarations: Seq[ProgramElement] = {
      val headerDecls = elems.collect {
        case e@(_: AgentOwns | _: Globals | _:Breed | _:Includes | _:Extensions | _: TopLevelComment) => e
      }
      val procDecls = elems.collect { case p: Procedure => p }
      headerDecls ++ procDecls
    }

    def statementCount =
      elems.map {
        case b: Breed if b.owns.nonEmpty => 2
        case _ => 1
      }.sum

    def programText =
      orderedDeclarations.map(_.programText).mkString("\n")
  }
}
