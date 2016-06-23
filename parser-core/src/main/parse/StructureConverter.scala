// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.parse

import org.nlogo.core,
  core.{ CompilerException, FrontEndProcedure, I18N, StructureResults, Program, Syntax, Token, TokenType }

/// Stage #3 of StructureParser

object StructureConverter {

  import core.StructureDeclarations._

  def convert(declarations: Seq[Declaration],
              displayName: Option[String],
              oldResults: StructureResults,
              subprogram: Boolean): StructureResults = {
    val is = declarations.collect {
      case i: Includes =>
        i.names
    }.flatten
    val ps = declarations.collect {
      case p: Procedure =>
        buildProcedure(p, displayName)
    }
    ps.foreach(_._1.topLevel = subprogram)
    StructureResults(
      program =
        updateProgram(oldResults.program, declarations),
      procedures = oldResults.procedures ++
        ps.map { case (pp, _) => pp.name -> pp},
      procedureTokens = oldResults.procedureTokens ++ ps.map {
        case (p, toks) => p.name -> toks
      },
      includes = oldResults.includes ++ is,
      includedSources = oldResults.includedSources,
      extensions = oldResults.extensions ++
        declarations.collect {
          case e: Extensions =>
            e.names.map(_.token)
        }.flatten)
  }

  def buildProcedure(p: Procedure, displayName: Option[String]): (FrontEndProcedure, Iterable[Token]) = {
    val proc = new RawProcedure(p, displayName)
    (proc, p.tokens.drop(2).init :+
      new Token("", TokenType.Eof, "")(p.tokens.last.start, p.tokens.last.end, p.tokens.last.filename))
  }

  def updateProgram(program: Program, declarations: Seq[Declaration]): Program = {
    def updateVariables(program: Program): Program =
      declarations.foldLeft(program) {
        case (program, Variables(Identifier("GLOBALS", _), identifiers)) =>
          program.copy(userGlobals = program.userGlobals ++ identifiers.map(_.name))
        case (program, Variables(Identifier("TURTLES-OWN", _), identifiers)) =>
          program.copy(turtleVars = program.turtleVars ++ identifiers.map(i => i.name -> Syntax.WildcardType))
        case (program, Variables(Identifier("PATCHES-OWN", _), identifiers)) =>
          program.copy(patchVars = program.patchVars ++ identifiers.map(i => i.name -> Syntax.WildcardType))
        case (program, Variables(Identifier("LINKS-OWN", _), identifiers)) =>
          program.copy(linkVars = program.linkVars ++ identifiers.map(i => i.name -> Syntax.WildcardType))
        case (program, Variables(Identifier(breedOwn, tok), identifiers)) =>
          updateBreedVariables(program, breedOwn.stripSuffix("-OWN"), identifiers.map(_.name), tok)
        case (program, _) =>
          program
      }
    def updateBreeds(program: Program): Program =
      declarations.foldLeft(program) {
        case (program, Breed(plural, singular, isLinkBreed, isDirected)) =>
          val breed = core.Breed(plural.name, singular.map(_.name).getOrElse("TURTLE"),
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

  def updateBreedVariables(program: Program, breedName: String, newOwns: Seq[String], tok: Token): Program = {
    if ((program.breeds.keySet ++ program.linkBreeds.keySet).contains(breedName)) {
      import collection.immutable.ListMap
      type BreedMap = ListMap[String, core.Breed]
      // a bit of unpleasantness here is that (as I only belatedly discovered) ListMap.updated
      // doesn't preserve the ordering of existing keys, which is bad for us because we need
      // to preserve the declaration order of breeds because later in Renderer it's going to
      // determine the z-ordering of turtles in the view.  so we resort to a bit of ugliness
      // here: remember the order the keys were in, then after we've updated the map, restore
      // the original order. - ST 7/14/12
      def orderPreservingUpdate(breedMap: BreedMap, breed: core.Breed): BreedMap = {
        val keys = breedMap.keys.toSeq
        val newMapInWrongOrder = breedMap.updated(breed.name, breed)
        val result = ListMap(keys.map { k => (k, newMapInWrongOrder(k))}.toSeq: _*)
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
    } else
      throw new CompilerException(
        I18N.errors.getN("compiler.StructureConverter.noBreed", breedName), tok.start, tok.end, tok.filename)
  }
}
