// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.generate

import java.lang.reflect.Method
import org.nlogo.nvm.{ Command, CustomGenerated, Instruction, Reporter }
import org.objectweb.asm.Type

object BytecodeUtils {

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
    val name = i match {
      case _: Command => "perform"
      case _: Reporter => "report"
    }
    // if report() has been overridden to have a more specific type than Object,
    // then both the override and the original will be included in the result
    // of getMethods. we definitely want the override since the generator may
    // be able to take advantage of its knowledge of the more specific type.
    val candidates = i.getClass.getMethods.filter(_.getName == name)
    if (name == "perform") {
      assert(candidates.size == 1)
      candidates.head
    }
    else {
      assert(candidates.size <= 2)
      candidates.find(_.getReturnType != classOf[AnyRef])
        .getOrElse(candidates.head)
    }
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

  def checkClassHasMethod(c: Class[_], name: String, descriptor: String): Boolean =
    c != null &&
      (c.getDeclaredMethods.exists(method => method.getName == name &&
        Type.getMethodDescriptor(method) == descriptor)
        || checkClassHasMethod(c.getSuperclass, name, descriptor))

}
