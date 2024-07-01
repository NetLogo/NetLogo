// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core

case class XMLElement(val name: String, val attributes: Map[String, String], val text: String,
                      val children: List[XMLElement])
