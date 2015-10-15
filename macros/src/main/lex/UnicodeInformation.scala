// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.lex

import scala.language.experimental.macros
import scala.reflect.macros.blackbox.{ Context => BlackBoxContext}

object UnicodeInformation {
  def letterCharacters: Set[Range] = macro letterCharactersInternal

  // This would be private, but scala macros cannot be private
  def letterCharactersInternal(c: BlackBoxContext): c.Tree = {
    import c.universe._
    val letterChars: List[Range] = (0x0000 to 0xFFFF).foldLeft(List[Range]()) {
      case (r :: rs, i) if Character.isDefined(i) && Character.isLetter(i) =>
        if (r.contains(i - 1)) (r.start to i)::rs else (i to i)::r::rs
      case (Nil, i) if Character.isDefined(i) && Character.isLetter(i) =>
        (i to i)::Nil
      case (l, i) => l
    }
    // We return a Set of Ranges instead of a Set because returning a Set
    // causes the class using this to exceed Java's maximum class size (RG 1/21/2015)
    val ranges = letterChars.map(r => q"(${r.start} to ${r.end})")
    q"Set(..$ranges)"
  }
}
