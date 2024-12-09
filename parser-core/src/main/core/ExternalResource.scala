// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core

object ExternalResource {

  sealed trait Location

  case class Existing(name: String) extends Location
  case class New     (path: String) extends Location

}

case class ExternalResource(name: String, resourceType: String, data: String)
