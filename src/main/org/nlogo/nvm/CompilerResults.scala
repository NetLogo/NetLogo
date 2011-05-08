package org.nlogo.nvm

case class CompilerResults(procedures: Seq[Procedure], program: org.nlogo.api.Program){
  def proceduresMap = org.nlogo.util.JCL.toJavaMap(procedures.map(proc => (proc.name, proc)).toMap)
  def head = procedures.head
}
