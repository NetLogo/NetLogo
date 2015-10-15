// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.parse

/// Stage #2 of StructureParser:
/// check for duplicate declarations and duplicate names

// (I'm not very happy with the code for this stage, but as long as it's
// well encapsulated, maybe it's good enough. - ST 12/7/12)

import
  org.nlogo.core,
    core.{ Token, StructureDeclarations },
      StructureDeclarations.{ Breed, Declaration, Extensions, Identifier, Includes, Procedure, Variables },
    core.Fail._

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

  def rejectDuplicateNames(declarations: Seq[Declaration], usedNames: Map[String, String]) {

    def checkForInconsistentIDs(usageIdentifier: String, usageTypeName: String, occ: Occurrence, takenNames: Seq[String]) {
      val identifierNamesAreDifferent = usageIdentifier != occ.identifier.name
      cAssert(
        identifierNamesAreDifferent || invalidBreedVariableDeclaration(usageIdentifier, usageTypeName, occ, takenNames),
        duplicateOf(usageTypeName, occ.identifier.name),
        occ.identifier.token)
    }

    // `linkBreedNames` goes unused.  Is this a bug? --JAB (10/16/14)
    val (linkBreedNames, turtleBreedNames) = linkAndTurtleBreedNames(declarations)
    val occurrences                        = occurrencesFromDeclarations(declarations)

    for {
      firstUsage  <- occurrences.iterator
      secondUsage <- occurrences
    } {

      if (firstUsage.isGlobal && (secondUsage ne firstUsage))
        checkForInconsistentIDs(firstUsage.identifier.name, firstUsage.typeOfDeclaration, secondUsage, turtleBreedNames)

      checkNotTaskVariable(firstUsage.identifier)

      for ((identifier, typeName) <- usedNames) {
        checkForInconsistentIDs(identifier, typeName, firstUsage, turtleBreedNames)
      }

    }

  }

  private def occurrencesFromDeclarations(declarations: Seq[Declaration]): Seq[Occurrence] = {
    declarations.flatMap {
      case decl@Variables(kind, names) =>
        names.map(Occurrence(decl, _, s"${kind.name} variable"))
      case decl@Procedure(name, _, inputs, _) =>
        Occurrence(decl, name, "procedure") +: inputs.map(Occurrence(decl, _, "", isGlobal = false))
      case _ =>
        Seq()
    }
  }

  private def linkAndTurtleBreedNames(declarations: Seq[Declaration]): (Seq[String], Seq[String]) = {
    val (linkBreeds, turtleBreeds) = declarations
      .collect { case breed: Breed => (breed, s"${breed.plural.name}-OWN") }
      .partition(_._1.isLinkBreed)
    (linkBreeds.map(_._2), turtleBreeds.map(_._2))
  }

  private def invalidBreedVariableDeclaration(usageIdentifier: String, usageTypeName: String, occ: Occurrence, turtleBreedNames: Seq[String]): Boolean = {

    def sameBreediness(names: String*) = names.forall(turtleBreedNames.contains)
    def isReserved(names: String*)     = names.exists(Set("TURTLES", "LINKS"))

    val isVariable = usageTypeName.endsWith("variable")
    val ownerName  = usageTypeName.stripSuffix(" variable")

    isVariable && {
      occ.declaration match {
        case Variables(Identifier(breedName, _), _)  =>
          ownerName != breedName && sameBreediness(ownerName, breedName) && !isReserved(ownerName, breedName)
        case _ =>
          false
      }
    }

  }

  private def allPairs[T <: AnyRef](xs: Seq[T]): Iterator[(T, T)] =
    for {
      x1 <- xs.iterator
      x2 <- xs
      if x1 ne x2
    }
    yield (x1, x2)

  private def checkNotTaskVariable(ident: Identifier) {
    cAssert(!ident.name.startsWith("?"),
      "Names beginning with ? are reserved for use as task inputs",
      ident.token)
  }

  private def redeclarationOf(kind: String) =
    s"Redeclaration of $kind"

  private def duplicateOf(typeName: String, origName: String) =
    s"There is already a $typeName called $origName"

  private def duplicateVariableIdentifier(ident: Identifier) =
    s"There is already a local variable called ${ident.name} here"

  private case class Occurrence(declaration: Declaration, identifier: Identifier, typeOfDeclaration: String, isGlobal: Boolean = true)

}
