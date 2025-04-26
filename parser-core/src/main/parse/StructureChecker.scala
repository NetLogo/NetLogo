// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.parse

/// Stage #2 of StructureParser:
/// check for duplicate declarations and duplicate names

// (I'm not very happy with the code for this stage, but as long as it's
// well encapsulated, maybe it's good enough. - ST 12/7/12)

import
  org.nlogo.core,
    core.{ BreedIdentifierHandler, I18N, StructureDeclarations, Token, TokenType },
      StructureDeclarations.{ Breed, Declaration, Extensions, Identifier, Includes, Procedure, Variables },
    core.Fail._

import SymbolType._

object StructureChecker {

  def rejectMisplacedConstants(declarations: Seq[Declaration]): Unit = {
    for (declaration <- declarations) {
      declaration match {
        case Variables(_, names) =>
          for (name <- names) {
            if (name.token.tpe == TokenType.Literal) {
              exception(I18N.errors.get("compiler.StructureChecker.variableConstant"), name.token)
            }
          }
        case Procedure(_, _, inputs, _) =>
          for (input <- inputs) {
            if (input.token.tpe == TokenType.Literal) {
              exception(I18N.errors.get("compiler.StructureChecker.inputConstant"), input.token)
            }
          }
        case _ =>
      }
    }
  }

  def rejectDuplicateDeclarations(declarations: Seq[Declaration]): Unit = {

    // O(n^2) -- maybe we should fold instead --ST
    def checkPair(decl1: Declaration, decl2: Declaration): Option[(String, Token)] =
      (decl1, decl2) match {
        case (v1: Variables, v2: Variables) if v1.kind == v2.kind =>
          Some((v1.kind.name, v2.kind.token))
        case (_: Extensions, e: Extensions) =>
          Some(("EXTENSIONS", e.token))
        case (_: Includes, i: Includes) =>
          Some(("INCLUDES", i.token))
        case _ =>
          None
      }

    for {
      Procedure(_, _, inputs, _) <- declarations
      input                      <- inputs
    } {
      checkNotArrow(input)
      cAssert(
        inputs.count(_.name == input.name) == 1,
        duplicateVariableIdentifier(input), input.token)
    }

    for{
      (decl1, decl2) <- allPairs(declarations)
      (kind, token)  <- checkPair(decl1, decl2)
    } exception(redeclarationOf(kind), token)

  }

  def rejectDuplicateNames(declarations: Seq[Declaration], usedNames: SymbolTable): Unit = {

    def checkForInconsistentIDs(usageIdentifier: String, usageType: SymbolType, occ: Occurrence): Unit = {
      cAssert(
        usageDoesNotClashWithOccurence(usageIdentifier, usageType, occ),
        duplicateOf(usageType, occ.identifier.name),
        occ.identifier.token)
    }

    val occurrences = occurrencesFromDeclarations(declarations)

    for { usage@Occurrence(Breed(_, _, _, _), _, _, _) <- occurrences.iterator } {
      checkForBreedPrimsDuplicatingBuiltIn(usage, usedNames)
    }

    val usedNamesAndBreedNames = usedNames ++ breedPrimitives(declarations)

    for { usage <- occurrences.iterator } {
      checkNotArrow(usage.identifier)

      for ((identifier, typeName) <- usedNamesAndBreedNames) {
        checkForInconsistentIDs(identifier, typeName, usage)
      }
    }

    for {
      firstUsage  <- occurrences.iterator if firstUsage.isGlobal
      secondUsage <- occurrences if (secondUsage ne firstUsage)
    } {
      checkForInconsistentIDs(firstUsage.identifier.name, firstUsage.typeOfDeclaration, secondUsage)
    }
  }

  def rejectMissingReport(declarations: Seq[Declaration]): Unit = {
    declarations.foreach(_ match {
      case Procedure(_, true, _, tokens) =>
        if (!tokens.exists(_ match {
          case Token(_, TokenType.Ident, "REPORT") |
               Token(_, TokenType.Ident, "RUN") |
               Token(_, TokenType.Ident, "RUNRESULT") |
               Token(_, TokenType.Ident, "ERROR") => true
          case _ => false
        })) {
          exception("Reporter procedures must report a value", tokens(1))
        }

      case _ =>
    })
  }

  def breedPrimitives(declarations: Seq[Declaration]): SymbolTable = {
    import BreedIdentifierHandler._
    import org.nlogo.core.StructureDeclarations.{ Breed => DeclBreed }

    declarations.foldLeft(SymbolTable.empty) {
      case (table, breed: DeclBreed) =>
        table.addSymbols(breedCommands(breed), SymbolType.BreedCommand)
             .addSymbols(breedReporters(breed), SymbolType.BreedReporter)
      case (table, _) => table
    }
  }

  private def checkForBreedPrimsDuplicatingBuiltIn(usage: Occurrence, usedNames: SymbolTable): Unit = {
    usage.declaration match {
      case breed@Breed(_, _, _, _) =>
        val matchedPrimAndType =
          BreedIdentifierHandler.breedCommands(breed).filter(c => usedNames.contains(c.toUpperCase)).map(i => (i, BreedCommand)) ++
        BreedIdentifierHandler.breedReporters(breed).filter(r => usedNames.contains(r.toUpperCase)).map(i => (i, BreedReporter))
        matchedPrimAndType.foreach {
          case (p, st) => exception(breedOverridesBuiltIn(breed, usedNames(p.toUpperCase), p), usage.identifier.token)
        }
      case _ =>
    }
  }

  private def occurrencesFromDeclarations(declarations: Seq[Declaration]): Seq[Occurrence] = {
    val os = declarations.foldLeft(Seq[Occurrence]()) {
      case (occs, decl@Variables(kind, names)) =>
        val vars = kind.name.toUpperCase match {
          case "TURTLES-OWN" => names.map(Occurrence(decl, _, TurtleVariable))
          case "PATCHES-OWN" => names.map(Occurrence(decl, _, PatchVariable))
          case "LINKS-OWN"   => names.map(Occurrence(decl, _, LinkVariable))
          case "GLOBALS" => names.map(Occurrence(decl, _, GlobalVariable))
          case breedName =>
            val name = breedName.stripSuffix("-OWN")
            occs.find(_.identifier.name == name).map { occ =>
              occ.typeOfDeclaration match {
                case LinkBreed   => names.map(Occurrence(decl, _, LinkBreedVariable(name)))
                case TurtleBreed => names.map(Occurrence(decl, _, BreedVariable(name)))
                case _ => Seq()
              }
            }.getOrElse(Seq())
        }
        occs ++ vars
      case (occs , decl@Procedure(name, _, inputs, _)) =>
        (Occurrence(decl, name, ProcedureSymbol) +: inputs.map(Occurrence(decl, _, ProcedureVariable, isGlobal = false))) ++ occs
      case (occs, decl@Breed(plural, singular, _, _)) =>
        val (symType, singularSymType) =
          if (decl.isLinkBreed) (LinkBreed, LinkBreedSingular)
          else (TurtleBreed, TurtleBreedSingular)
        occs ++
          Seq(Occurrence(decl, plural, symType), Occurrence(decl, singular, singularSymType))
      case (occs, _) => occs
    }
    os.sortBy(_.typeOfDeclaration)
  }

  private def usageDoesNotClashWithOccurence(usageIdentifier: String, usageType: SymbolType, occ: Occurrence): Boolean = {
    val identifierNamesAreDifferent = ! usageIdentifier.equalsIgnoreCase(occ.identifier.name)

    identifierNamesAreDifferent || (usageType match {
      case _: SymbolType.Variable => compatibleVariableDeclaration(usageIdentifier, usageType, occ)
      case _ => false
    })
  }

  private def compatibleVariableDeclaration(usageIdentifier: String, usageType: SymbolType, occ: Occurrence): Boolean = {
    (usageType, occ.typeOfDeclaration) match {
      case (TurtleVariable, BreedVariable(_))   | (BreedVariable(_), TurtleVariable)   => true
      case (LinkVariable, LinkBreedVariable(_)) | (LinkBreedVariable(_), LinkVariable) => true
      case (LinkBreedVariable(_), LinkBreedVariable(_)) => true
      case (BreedVariable(_), BreedVariable(_)) => true
      case _ => false
    }
  }

  private def allPairs[T <: AnyRef](xs: Seq[T]): Iterator[(T, T)] =
    for {
      x1 <- xs.iterator
      x2 <- xs
      if x1 ne x2
    }
    yield (x1, x2)

  private def breedOverridesBuiltIn(breed: Breed, duplicatedType: SymbolType, duplicatedName: String): String = {
    val typeName = SymbolType.typeName(duplicatedType)
    I18N.errors.getN("compiler.StructureChecker.breedOverrides", s"[${breed.plural.name} ${breed.singular.name}]", duplicatedName, typeName)
  }

  private def checkNotArrow(ident: Identifier): Unit = {
    cAssert(ident.name != "->", I18N.errors.get("compiler.StructureChecker.invalidArrow"), ident.token)
  }

  private def redeclarationOf(kind: String) =
    I18N.errors.getN("compiler.StructureChecker.redeclaration", kind)

  private def duplicateOf(symbolType: SymbolType, origName: String) = {
    val typeName = SymbolType.typeName(symbolType)
    I18N.errors.getN("compiler.StructureChecker.duplicateType", typeName, origName)
  }

  private def duplicateVariableIdentifier(ident: Identifier) =
    I18N.errors.getN("compiler.StructureChecker.duplicateVariable", ident.name)

  private case class Occurrence(declaration: Declaration, identifier: Identifier, typeOfDeclaration: SymbolType, isGlobal: Boolean = true)

}
