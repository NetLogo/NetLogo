// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.nvm

import org.nlogo.api.Program
import collection.JavaConverters._

case class CompilerResults(procedures: Seq[Procedure], program: Program) {
  def proceduresMap = procedures.map(proc => (proc.name, proc)).toMap.asJava
  def head = procedures.head
}
