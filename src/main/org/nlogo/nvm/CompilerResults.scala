// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.nvm

import org.nlogo.api.Program
import collection.immutable.ListMap
import collection.JavaConverters._

case class CompilerResults(procedures: Seq[Procedure], program: Program) {
  def proceduresMap =
    ListMap(procedures.map(proc => (proc.name, proc)): _*)
  def head = procedures.head
}
