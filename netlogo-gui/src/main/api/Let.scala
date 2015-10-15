// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import java.util.{ Collections, List => JList }

final case class Let(varName: String, start: Int, end: Int, children: JList[Let]) {
  def this() = this(null, -1, -1, Collections.emptyList[Let])
  def this(varName: String, start: Int, end: Int) = this(varName, start, end, Collections.emptyList[Let])
}
