// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.parse0

/// Stage #2 of StructureParser:
/// check for duplicate declarations and duplicate names

// (I'm not very happy with the code for this stage, but as long as it's
// well encapsulated, maybe it's good enough. - ST 12/7/12)

import org.nlogo.api.Token, Fail._

object StructureChecker {

  import StructureDeclarations._

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
        "There is already a " +  used._2 + " called " + occ.identifier.name,
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
