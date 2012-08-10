// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

case class Breed(name: String,
                 singular: String,
                 owns: Seq[String] = Seq(),
                 isDirected: Boolean = false) {
  override def toString =
    Seq(name, singular, owns.mkString(" "), isDirected)
      .mkString("Breed(", ", ", ")")
}
