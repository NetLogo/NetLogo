// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compiler

// For each source file, input is Tokens, output is a Results -- which is mostly just a Program and
// some Procedures.

// Each source file is handled in three stages, each represented as a separate trait.
// 1. StructureCombinators parses input tokens, returns a Seq[Declaration].
// 2. DuplicateChecker checks the Seq[Declaration] for duplicates.
// 3. ResultsBuilder converts the Seq[Declaration] to a StructureParser.Results.
// By splitting it this way, we get separation of concerns between the (clean) mechanics of parsing
// and the (messy) mechanics of building the data structures that the rest of the compiler and the
// engine will use.

// Note that when parsing starts, we don't necessarily have all our source files yet.  Some of them
// will be discovered as we parse, through __include declarations.  (Included files might themselves
// include further files.)

import org.nlogo.{ api, nvm }
import api.{ Token, TokenType }
import Fail._

object StructureParser {

  val emptyResults =
    Results(program = api.Program.empty)
  case class Results(
    program: api.Program,
    procedures: Compiler.ProceduresMap = nvm.CompilerInterface.NoProcedures,
    tokens: Map[nvm.Procedure, Iterable[Token]] = Map(),
    includes: Seq[Token] = Seq(),
    extensions: Seq[Token] = Seq())

  /// main entry point.  handles gritty extensions stuff and includes stuff.

  def parseAll(
      tokenizer: api.TokenizerInterface,
      source: String, displayName: Option[String], program: api.Program, subprogram: Boolean,
      oldProcedures: Compiler.ProceduresMap, extensionManager: api.ExtensionManager): Results = {
    if(!subprogram)
      extensionManager.startFullCompilation()
    val sources = Seq((source, ""))
    val oldResults = Results(program, oldProcedures)
    def parseOne(source: String, fileName: String, previousResults: Results) =
      new StructureParser(tokenizer.tokenize(source, fileName), displayName, previousResults)
        .parse(subprogram)
    val firstResults =
      sources.foldLeft(oldResults){
        case (results, (source, fileName)) =>
          parseOne(source, fileName, results)
      }
    val results =
      Iterator.iterate(firstResults){results =>
        assert(!subprogram)
        val path = extensionManager.resolvePath(results.includes.head.value.asInstanceOf[String])
        cAssert(path.endsWith(".nls"),
          "Included files must end with .nls",
          results.includes.head)
        val newResults =
          parseOne(extensionManager.getSource(path), path, results)
        newResults.copy(includes = newResults.includes.filterNot(_ == results.includes.head))
      }.dropWhile(_.includes.nonEmpty).next
    if(!subprogram) {
      for(token <- results.extensions)
        extensionManager.importExtension(
          token.name.toLowerCase, new api.ErrorSource(token))
      extensionManager.finishFullCompilation()
    }
    results
  }

}

/// common data structures used by all three stages. not used outside StructureParser.

// we retain tokens so we can report error locations.

trait StructureDeclarations {
  sealed trait Declaration
  case class Includes(token: Token, names: Seq[Token])
      extends Declaration
  case class Extensions(token: Token, names: Seq[Identifier])
      extends Declaration
  case class Breed(plural: Identifier, singular: Identifier, isLinkBreed: Boolean = false, isDirected: Boolean = false)
      extends Declaration
  case class Variables(kind: Identifier, names: Seq[Identifier])
      extends Declaration
  case class Procedure(name: Identifier, isReporter: Boolean, inputs: Seq[Identifier], tokens: Seq[Token])
      extends Declaration
  case class Identifier(name: String, token: Token)
}

/// main entry point for each source file. knits stages together. throws CompilerException

class StructureParser(
  tokens: Seq[Token],
  displayName: Option[String],
  oldResults: StructureParser.Results)
    extends StructureCombinators with DuplicateChecker with ResultsBuilder {

  def parse(subprogram: Boolean): StructureParser.Results = {
    val reader = new SeqReader[Token](tokens, _.startPos)
    try program(reader) match {
      case Success(declarations, _) =>
        rejectDuplicateDeclarations(declarations)
        rejectDuplicateNames(declarations, usedNames(oldResults.program, oldResults.procedures))
        buildResults(declarations, displayName,
          if (subprogram) StructureParser.emptyResults.copy(program = oldResults.program)
          else oldResults,
          subprogram)
      case NoSuccess(msg, rest) =>
        exception(msg, rest.first)
    }
    // avoid leaking ThreadLocals due to some deprecated stuff in
    // Scala 2.10 that will be removed in Scala 2.11.  see
    // https://issues.scala-lang.org/browse/SI-4929 and
    // https://github.com/scala/scala/commit/dce6b34c38a6d774961ca6f9fd50b11300ecddd6
    // - ST 1/3/13
    finally {
      val field = getClass.getDeclaredField("scala$util$parsing$combinator$Parsers$$lastNoSuccessVar")
      field.setAccessible(true)
      val field2 = classOf[scala.util.DynamicVariable[_]].getDeclaredField("tl")
      field2.setAccessible(true)
      field2.get(field.get(this)).asInstanceOf[java.lang.ThreadLocal[_]].remove()
      field.set(this, null)
    }
  }

  def usedNames(program: api.Program, procedures: Compiler.ProceduresMap): Map[String, String] =
    program.usedNames ++
      procedures.keys.map(_ -> "procedure")

}

/// Stage #1: parsing

// The parsing stuff knows practically nothing about the rest of NetLogo.
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

trait StructureCombinators
    extends scala.util.parsing.combinator.Parsers
    with StructureDeclarations {

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
      case token @ Token(_, TokenType.KEYWORD, value: String)
          if value.endsWith("-OWN") =>
        token }) ~!
      identifierList ^^ {
        case token ~ names =>
          Variables(Identifier(token.value.asInstanceOf[String], token), names) }

  // kludge: special case because of naming conflict with BREED turtle variable - jrn 8/04/05
  def breed: Parser[Breed] =
    agentVariable("BREED") ~! openBracket ~> identifier ~ opt(identifier) <~ closeBracket ^^ {
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
    tokenType("opening bracket", TokenType.OPEN_BRACKET)

  def closeBracket: Parser[Token] =
    tokenType("closing bracket", TokenType.CLOSE_BRACKET)

  def identifier: Parser[Identifier] =
    tokenType("identifier", TokenType.IDENT) ^^ {
      token =>
        Identifier(token.value.asInstanceOf[String], token)}

  def identifierList: Parser[Seq[Identifier]] =
    openBracket ~> rep(identifier) <~ closeBracket

  def stringList: Parser[Seq[Token]] =
    openBracket ~> rep(string) <~ closeBracket

  def string: Parser[Token] =
    acceptMatch("string", {
      case t @ Token(_, TokenType.CONSTANT, value: String) =>
        t })

  def keyword(name: String): Parser[Token] =
    acceptMatch(name, {
      case token @ Token(_, TokenType.KEYWORD, `name`) =>
        token })

  def agentVariable(name: String): Parser[Token] =
    acceptMatch(name, {
      case token @ Token(_, TokenType.VARIABLE, `name`) =>
        token })

  def nonKeyword: Parser[Token] =
    acceptMatch("?", {
      case token @ Token(_, tpe, _)
          if (tpe != TokenType.KEYWORD) =>
        token })

}

/// Stage #2: check for duplicate declarations and duplicate names

// (I'm not very happy with the code for this stage, but as long as it's
// well encapsulated, maybe it's good enough. - ST 12/7/12)

trait DuplicateChecker extends StructureDeclarations {

  def rejectDuplicateDeclarations(declarations: Seq[Declaration]) {
    for {
      Procedure(_, _, inputs, _) <- declarations
      input <- inputs
    } cAssert(
      inputs.count(_.name == input.name) == 1,
      "There is already a local variable called " + input.name + " here", input.token)
    // O(n^2) -- maybe we should fold instead
    def checkPair(decl1: Declaration, decl2: Declaration): Option[(String, Token)] =
      (decl1, decl2) match {
        case (v1: Variables, v2: Variables)
            if v1.kind == v2.kind =>
          Some((v1.kind.name, v2.kind.token))
        case (_: Extensions, e: Extensions) =>
          Some(("EXTENSIONS", e.token))
        case (_: Includes, i: Includes) =>
          Some(("INCLUDES", i.token))
        case _ =>
          None
      }
    for{
      (decl1, decl2) <- allPairs(declarations)
      (kind, token) <- checkPair(decl1, decl2)
    } exception("Redeclaration of " + kind, token)
  }

  def rejectDuplicateNames(declarations: Seq[Declaration], usedNames: Map[String, String]) {
    type Used = (String, String)
    case class Occurrence(declaration: Declaration, identifier: Identifier)
    val (linkBreedNames, turtleBreedNames) = {
      val (linkBreeds, turtleBreeds) =
        declarations
          .collect{case breed: Breed => breed}
          .partition(_.isLinkBreed)
      (linkBreeds.map(_.plural.name).map(_ + "-OWN"),
        turtleBreeds.map(_.plural.name).map(_ + "-OWN"))
    }
    val occurrences: Seq[Occurrence] =
      declarations.flatMap{
        case decl @ Breed(plural, singular, _, _) =>
          if (singular.token ne plural.token)
            Seq(Occurrence(decl, plural), Occurrence(decl, singular))
          else
            Seq(Occurrence(decl, plural))
        case decl @ Variables(_, names) =>
          names.map(Occurrence(decl, _))
        case decl @ Procedure(name, _, _, _) =>
          Seq(Occurrence(decl, name))
        case _ =>
          Seq()
      }
    def displayName(decl: Declaration) =
      decl match {
        case _: Breed =>
          "breed"
        case Variables(kind, _) =>
          kind.name + " variable"
        case _: Procedure =>
          "procedure"
        case _ =>
          throw new IllegalArgumentException(decl.toString)
      }
    def check(used: Used, occ: Occurrence) {
      def isBreedVariableException =
        if (!used._2.endsWith(" variable"))
          false
        else
          (used._2.dropRight(" variable".size), occ.declaration) match {
            case (name1, Variables(kind2, _))
                if (name1 != kind2.name &&
                  turtleBreedNames.contains(name1) == turtleBreedNames.contains(kind2.name) &&
                  Set(name1, kind2.name).intersect(Set("TURTLES", "LINKS")).isEmpty) =>
              true
            case _ =>
              false
          }
      cAssert(
        used._1 != occ.identifier.name || isBreedVariableException,
        "You already defined " + occ.identifier.name + " as a " + used._2,
        occ.identifier.token)
    }
    // O(n^2) -- maybe we should fold instead
    for((o1, o2) <- allPairs(occurrences))
      check((o1.identifier.name, displayName(o1.declaration)), o2)
    for(used <- usedNames; o <- occurrences)
      check(used, o)
  }

  def allPairs[T](xs: Seq[T]): Iterator[(T, T)] =
    for {
      rest <- xs.tails
      x1 <- rest.headOption.toSeq
      x2 <- rest.tail
    } yield (x1, x2)

}

/// Stage #3: build results

trait ResultsBuilder extends StructureDeclarations {

  def buildResults(declarations: Seq[Declaration],
      displayName: Option[String],
      oldResults: StructureParser.Results,
      subprogram: Boolean): StructureParser.Results = {
    val is = declarations.collect{
      case i: Includes =>
        i.names}.flatten
    val ps = declarations.collect{
      case p: Procedure =>
        buildProcedure(p, displayName)}
    ps.foreach(_._1.topLevel = subprogram)
    StructureParser.Results(
      program =
        updateProgram(oldResults.program, declarations),
      procedures =
        oldResults.procedures ++
          ps.map{case (pp, _) => pp.name -> pp},
      tokens = oldResults.tokens ++ ps,
      includes = oldResults.includes ++ is,
      extensions = oldResults.extensions ++
        declarations.collect{
          case e: Extensions =>
            e.names.map(_.token)}.flatten)
  }

  def buildProcedure(p: Procedure, displayName: Option[String]): (nvm.Procedure, Iterable[Token]) = {
    val proc = new nvm.Procedure(
      p.isReporter, p.tokens.tail.head.value.asInstanceOf[String],
      p.tokens.tail.head, displayName, null)
    proc.args = Vector(p.inputs.map(_.name): _*)
    (proc, p.tokens.drop(2).init :+ Token.eof)
  }

  def updateProgram(program: api.Program, declarations: Seq[Declaration]): api.Program = {
    def updateVariables(program: api.Program): api.Program =
      declarations.foldLeft(program){
        case (program, Variables(Identifier("GLOBALS", _), identifiers)) =>
          program.copy(userGlobals = program.userGlobals ++ identifiers.map(_.name))
        case (program, Variables(Identifier("TURTLES-OWN", _), identifiers)) =>
          program.copy(turtlesOwn = program.turtlesOwn ++ identifiers.map(_.name))
        case (program, Variables(Identifier("PATCHES-OWN", _), identifiers)) =>
          program.copy(patchesOwn = program.patchesOwn ++ identifiers.map(_.name))
        case (program, Variables(Identifier("LINKS-OWN", _), identifiers)) =>
          program.copy(linksOwn = program.linksOwn ++ identifiers.map(_.name))
        case (program, Variables(Identifier(breedOwn, _), identifiers)) =>
          updateBreedVariables(program, breedOwn.dropRight(4), identifiers.map(_.name))
        case (program, _) =>
          program
      }
    def updateBreeds(program: api.Program): api.Program =
      declarations.foldLeft(program){
        case (program, Breed(plural, singular, isLinkBreed, isDirected)) =>
          val breed = api.Breed(plural.name, singular.name,
            isLinkBreed = isLinkBreed, isDirected = isDirected)
          if (isLinkBreed)
            program.copy(
              linkBreeds = program.linkBreeds.updated(breed.name, breed))
          else
            program.copy(
              breeds = program.breeds.updated(breed.name, breed))
        case (program, _) =>
          program
      }
    updateVariables(updateBreeds(program))
  }

  def updateBreedVariables(program: api.Program, breedName: String, newOwns: Seq[String]): api.Program = {
    import collection.immutable.ListMap
    type BreedMap = ListMap[String, api.Breed]
    // a bit of unpleasantness here is that (as I only belatedly discovered) ListMap.updated
    // doesn't preserve the ordering of existing keys, which is bad for us because we need
    // to preserve the declaration order of breeds because later in Renderer it's going to
    // determine the z-ordering of turtles in the view.  so we resort to a bit of ugliness
    // here: remember the order the keys were in, then after we've updated the map, restore
    // the original order. - ST 7/14/12
    def orderPreservingUpdate(breedMap: BreedMap, breed: api.Breed): BreedMap = {
      val keys = breedMap.keys.toSeq
      val newMapInWrongOrder = breedMap.updated(breed.name, breed)
      val result = ListMap(keys.map{k => (k, newMapInWrongOrder(k))}.toSeq: _*)
      assert(keys sameElements result.keys.toSeq)
      result
    }
    // if we had lenses this wouldn't need to be so repetitious - ST 7/15/12
    if (program.linkBreeds.isDefinedAt(breedName))
      program.copy(linkBreeds =
        orderPreservingUpdate(
          program.linkBreeds,
          program.linkBreeds(breedName).copy(owns = newOwns)))
    else
      program.copy(breeds =
        orderPreservingUpdate(
          program.breeds,
          program.breeds(breedName).copy(owns = newOwns)))
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
