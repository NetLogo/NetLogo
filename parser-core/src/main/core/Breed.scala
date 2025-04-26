// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core

case class Breed(name: String,
                 singular: String,
                 originalName: String,
                 originalSingular: String,
                 owns: Seq[String] = Seq(),
                 isLinkBreed: Boolean = false,
                 isDirected: Boolean = false) {

  override def toString =
    Seq[Any](name, singular, owns.mkString(" "), isDirected)
      .mkString("Breed(", ", ", ")")
}
