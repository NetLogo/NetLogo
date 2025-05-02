// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.parse

import org.nlogo.core,
  core.{ CompilerException, Fail, FrontEndProcedure, I18N, StructureResults, Program, Syntax, Token, TokenType }

/// Stage #3 of StructureParser

object StructureConverter {

  import org.nlogo.core.Fail.exception
  import core.{Library, DefineLibrary}
  import core.StructureDeclarations.{Library => LibraryDecl, DefineLibrary => DefineLibraryDecl, _}

  def convert(declarations: Seq[Declaration],
              displayName: Option[String],
              oldResults: StructureResults,
              subprogram: Boolean): StructureResults = {
    val ls = declarations.collect {
      case l: LibraryDecl =>
        val maybeAlias = l.options.map((x) => x match {
          case LibraryAlias(name, _) => Some(name)
        }).find(_.isDefined).flatten
        Library(l.name, maybeAlias, l.token)
    }
    val dls = declarations.collect {
      case dl: DefineLibraryDecl =>
        val exportedNames = dl.exportSpecs.map((x) => x match {
          case SimpleExport(name) => Some(name)
        }).flatten
        DefineLibrary(dl.name, dl.version, exportedNames, dl.token)
    }
    val is = declarations.collect {
      case i: Includes =>
        i.names
    }.flatten
    val ps = declarations.collect {
      case p: Procedure =>
        buildProcedure(p, displayName)
    }
    ps.foreach(_._1.topLevel = subprogram)
    if (dls.size > 1) {
      exception(I18N.errors.get("compiler.StructureParser.libraryMultipleDefines"), dls(1).token)
    }
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
        }.flatten,
      libraries = oldResults.libraries ++ ls,
      defineLibrary = dls.headOption)
  }

  def buildProcedure(p: Procedure, displayName: Option[String]): (FrontEndProcedure, Iterable[Token]) = {
    val proc = new RawProcedure(p, displayName)
    (proc, p.tokens.drop(2).init :+
      new Token("", TokenType.Eof, "")(p.tokens.last.sourceLocation))
  }

  private def checkForDuplicates(identifiers: Seq[Identifier], current: Set[String], typeName: String): Unit = {
    identifiers.foldLeft(current) {
      case (names, i) =>
        if (names.exists(_ == i.name))
          Fail.exception(I18N.errors.getN("compiler.StructureChecker.duplicateType", typeName, i.name), i.token)

        names + i.name
    }
  }

  def updateProgram(program: Program, declarations: Seq[Declaration]): Program = {
    def updateVariables(program: Program): Program =
      declarations.foldLeft(program) {
        case (program, Variables(Identifier("GLOBALS", _), identifiers)) =>
          program.copy(userGlobals = program.userGlobals ++ identifiers.map(_.name))
        case (program, Variables(Identifier("TURTLES-OWN", _), identifiers)) =>
          checkForDuplicates(identifiers, program.turtleVars.keySet, SymbolType.typeName(SymbolType.TurtleVariable))
          program.breeds.foreach {
            case (name, breed) =>
              checkForDuplicates(identifiers, breed.owns.toSet,
                                 SymbolType.typeName(SymbolType.BreedVariable(name)))
          }
          program.copy(turtleVars = program.turtleVars ++ identifiers.map(i => i.name -> Syntax.WildcardType))
        case (program, Variables(Identifier("PATCHES-OWN", _), identifiers)) =>
          checkForDuplicates(identifiers, program.patchVars.keySet, SymbolType.typeName(SymbolType.PatchVariable))
          program.copy(patchVars = program.patchVars ++ identifiers.map(i => i.name -> Syntax.WildcardType))
        case (program, Variables(Identifier("LINKS-OWN", _), identifiers)) =>
          checkForDuplicates(identifiers, program.linkVars.keySet, SymbolType.typeName(SymbolType.LinkVariable))
          program.linkBreeds.foreach {
            case (name, breed) =>
              checkForDuplicates(identifiers, breed.owns.toSet,
                                 SymbolType.typeName(SymbolType.LinkBreedVariable(name)))
          }
          program.copy(linkVars = program.linkVars ++ identifiers.map(i => i.name -> Syntax.WildcardType))
        case (program, Variables(Identifier(breedOwn, tok), identifiers)) =>
          updateBreedVariables(program, breedOwn.stripSuffix("-OWN"), identifiers, tok)
        case (program, _) =>
          program
      }
    def updateBreeds(program: Program): Program =
      declarations.foldLeft(program) {
        case (program, Breed(plural, singular, isLinkBreed, isDirected)) =>
          val breed = core.Breed(plural.name, singular.name, plural.token.text, singular.token.text, isLinkBreed = isLinkBreed, isDirected = isDirected)
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

  def updateBreedVariables(program: Program, breedName: String, newOwns: Seq[Identifier], tok: Token): Program = {
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
        val result = ListMap(keys.map { k => (k, newMapInWrongOrder(k))}.toSeq*)
        assert(keys sameElements result.keys.toSeq)
        result
      }
      // if we had lenses this wouldn't need to be so repetitious - ST 7/15/12
      if (program.linkBreeds.isDefinedAt(breedName)) {
        val breed = program.linkBreeds(breedName)
        checkForDuplicates(newOwns, program.linkVars.keySet, SymbolType.typeName(SymbolType.LinkVariable))
        checkForDuplicates(newOwns, breed.owns.toSet, SymbolType.typeName(SymbolType.LinkBreedVariable(breedName)))
        program.copy(linkBreeds =
          orderPreservingUpdate(
            program.linkBreeds,
            breed.copy(owns = breed.owns ++ newOwns.map(_.name))))
      } else {
        val breed = program.breeds(breedName)
        checkForDuplicates(newOwns, program.turtleVars.keySet, SymbolType.typeName(SymbolType.TurtleVariable))
        checkForDuplicates(newOwns, breed.owns.toSet, SymbolType.typeName(SymbolType.BreedVariable(breedName)))
        program.copy(breeds =
          orderPreservingUpdate(
            program.breeds,
            breed.copy(owns = breed.owns ++ newOwns.map(_.name))))
      }
    } else {
      throw new CompilerException(
        I18N.errors.getN("compiler.StructureConverter.noBreed", breedName), tok.start, tok.end, tok.filename)
    }
  }
}
