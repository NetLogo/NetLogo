package org.nlogo.agent

sealed trait Directedness

object Directedness {
  case object Directed extends Directedness
  case object Undirected extends Directedness
  case object Undetermined extends Directedness
}
