// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core

object ExternalResource {
  abstract class Location

  case class Existing(name: String) extends Location
  case class New(path: String) extends Location
  case object None extends Location
}

class ExternalResource(val name: String, val resourceType: String, val data: String) {}
