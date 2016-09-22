// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.nvm

import org.nlogo.core.Program
import scala.collection.immutable.ListMap

case class CompilerResults(proceduresMap: ListMap[String, Procedure], program: Program) {
  def head = proceduresMap.values.head
}
