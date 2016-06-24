// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.parse

/// Stage #2 of StructureParser:
/// check for duplicate declarations and duplicate names

// (I'm not very happy with the code for this stage, but as long as it's
// well encapsulated, maybe it's good enough. - ST 12/7/12)

import
  org.nlogo.core,
    core.{ BreedIdentifierHandler, StructureDeclarations, Token },
      StructureDeclarations.{ Breed, Declaration, Extensions, Identifier, Includes, Procedure, Variables },
    core.Fail._

import SymbolType._

object StructureChecker {

  def rejectDuplicateDeclarations(declarations: Seq[Declaration]) {

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
      checkNotTaskVariable(input)
      cAssert(
        inputs.count(_.name == input.name) == 1,
        duplicateVariableIdentifier(input), input.token)
    }

    for{
      (decl1, decl2) <- allPairs(declarations)
      (kind, token)  <- checkPair(decl1, decl2)
    } exception(redeclarationOf(kind), token)

  }

  def rejectDuplicateNames(declarations: Seq[Declaration], usedNames: SymbolTable) {

    def checkForInconsistentIDs(usageIdentifier: String, usageType: SymbolType, occ: Occurrence) {
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
      checkNotTaskVariable(usage.identifier)

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

  def breedPrimitives(declarations: Seq[Declaration]): SymbolType.SymbolTable = {
    import BreedIdentifierHandler._
    import org.nlogo.core.StructureDeclarations.{ Breed => DeclBreed }

    declarations.foldLeft(SymbolType.emptySymbolTable) {
      case (table, breed: DeclBreed) =>
        table.addSymbols(breedCommands(breed), SymbolType.BreedCommand)
             .addSymbols(breedReporters(breed), SymbolType.BreedReporter)
      case (table, _) => table
    }
  }

  private def checkForBreedPrimsDuplicatingBuiltIn(usage: Occurrence, usedNames: SymbolTable): Unit = {
    usage.declaration match {
      case breed@Breed(_, _, _, _) =>
        val allBreedPrims = BreedIdentifierHandler.breedCommands(breed) ++ BreedIdentifierHandler.breedReporters(breed)
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
        (Occurrence(decl, name, ProcedureSymbol) +: inputs.map(Occurrence(decl, _, LocalVariable, isGlobal = false))) ++ occs
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
    s"Defining a breed [${breed.plural.name} ${breed.singular.name}] redefines $duplicatedName, a $typeName"
  }

  private def checkNotTaskVariable(ident: Identifier) {
    cAssert(!ident.name.startsWith("?"),
      "Names beginning with ? are reserved for use as task inputs",
      ident.token)
  }

  private def redeclarationOf(kind: String) =
    s"Redeclaration of $kind"

  private def duplicateOf(symbolType: SymbolType, origName: String) = {
    val typeName = SymbolType.typeName(symbolType)
    s"There is already a $typeName called $origName"
  }

  private def duplicateVariableIdentifier(ident: Identifier) =
    s"There is already a local variable called ${ident.name} here"

  private case class Occurrence(declaration: Declaration, identifier: Identifier, typeOfDeclaration: SymbolType, isGlobal: Boolean = true)

}
