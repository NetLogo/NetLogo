// (C) 2011 Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.generator

import java.lang.reflect.Method
import org.nlogo.nvm.{ Command, CustomGenerated, Instruction, Reporter }

private object BytecodeUtils {

  // want each unrejiggered prim encountered to print a warning? - ST 2/6/09
  private val DEBUG_LOG_NOT_REJIGGERED = false

  // recursively descends args
  def isEntirelyRejiggered(instr: Instruction): Boolean =
    isRejiggered(instr) && instr.args.forall(isEntirelyRejiggered)

  def isRejiggered(instr: Instruction): Boolean =
    instr.isInstanceOf[CustomGenerated] || {
      val result = getMethods(instr.getClass).nonEmpty
      if (!result && DEBUG_LOG_NOT_REJIGGERED)
        println("debug: not rejiggered: " + instr.getClass.getName)
      result
    }

  def getUnrejiggeredMethod(i: Instruction): Method = {
    val name = i match { case _: Command => "perform"; case _: Reporter => "report" }
    i.getClass.getMethods.find(_.getName == name).get
  }

  def getMethods(instrClass: Class[_], profilingEnabled: Boolean = false): List[Method] = {
    // If profiling is enabled, we search first for profiling methods and use them.  If you
    // provide ANY profiling methods, you need to make sure that you provide one for each
    // perform_/report_ variant.
    val allMethods = instrClass.getMethods.toList
    val profilingMethods =
      if (profilingEnabled)
        allMethods.filter(m => m.getName.startsWith("profiling_report") ||
          m.getName.startsWith("profiling_perform"))
      else Nil
    if (!profilingMethods.isEmpty)
      profilingMethods
    else allMethods.filter(m => m.getName.startsWith("report_") ||
      m.getName.startsWith("perform_"))
  }

}
