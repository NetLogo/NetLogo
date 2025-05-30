// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.lex

import scala.quoted.*

object UnicodeInformation {

  inline def letterCharacters: Set[Range] = ${ letterCharactersCode }

  // We return a Set of Ranges instead of a Set because returning a Set
  // causes the class using this to exceed Java's maximum class size (RG 1/21/2015)
  //
  // Can confirm that this still applies in Scala 3's macro system. --Jason B. (5/9/25)
  private def letterCharactersCode(using Quotes): Expr[Set[Range]] =
    '{
      (0x0000 `to` 0xFFFF).foldLeft(List[Range]()) {
        case (r :: rs, i) if Character.isDefined(i) && Character.isLetter(i) =>
          if (r.contains(i - 1)) (r.start `to` i)::rs else (i `to` i)::r::rs
        case (Nil, i) if Character.isDefined(i) && Character.isLetter(i) =>
          (i to i)::Nil
        case (l, i) => l
      }.toSet
    }

}
