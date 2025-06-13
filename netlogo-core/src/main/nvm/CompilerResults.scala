// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.nvm

import org.nlogo.core.Program
import scala.collection.immutable.ListMap

case class CompilerResults(procedures: Seq[Procedure], program: Program) {
  def this(proceduresMap: ListMap[Tuple2[String, Option[String]], Procedure], program: Program) =
    this(proceduresMap.values.toSeq.distinct, program)

  def proceduresMap: ListMap[Tuple2[String, Option[String]], Procedure] =
    ListMap(procedures.flatMap(proc => proc.aliases.map(alias => (alias, proc)) :+ ((proc.name, proc.module), proc))*)

  def head = procedures.head
}
