// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

case class Breed(val name: String,
                 val singular: String,
                 owns: Seq[String] = Seq(),
                 isLinkBreed: Boolean = false,
                 isDirected: Boolean = false) {
  override def toString =
    Seq(name, singular, owns.mkString(" "), isDirected)
      .mkString("Breed(", ", ", ")")
}
