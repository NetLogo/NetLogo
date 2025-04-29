// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.generate

import org.nlogo.nvm.{ Command, EngineException, Instruction, Reporter, Workspace }

trait GeneratedInstruction extends Instruction {
  var original: Instruction = null

  // Given an enclosing GeneratedInstruction and an exception, use line number information in the stack trace
  // to find the exact original instruction that caused the error.  ~Forrest (10/24/2006)
  override def extractErrorInstruction(ex: EngineException): Instruction = {
    // 4.0 and 4.1RCx both had a bug (#840) where sometimes this code threw a NoSuchFieldException.
    // We're on the eve of 4.1 final, so rather than really mess with this right now, I'm just
    // inserting this code that catches the exception and soldiers on regardless. - ST 8/23/09
    def safelyGetField(id: Int) =
      try { Some(getClass.getField(Generator.KEPT_INSTRUCTION_PREFIX + id)) }
      catch { case _: NoSuchFieldException => None }
    // the GeneratedInstruction itself is the default culprit. ~Forrest 5/22/06
    ex.getStackTrace.dropWhile { elem =>
      !elem.getClassName.containsSlice("_asm_") || elem.getLineNumber <= 0
    }.headOption.flatMap(elem => safelyGetField(elem.getLineNumber)).map(_.get(this).asInstanceOf[Instruction]).getOrElse(this)
  }
  override def dump(indentLevel: Int): String = {
    val buf = new StringBuilder
    buf ++= super.dump(indentLevel)
    buf += '\n'
    val spaceString = List.fill(indentLevel * 2)(' ').mkString
    val bytecode = disassembly.value
    if (bytecode != "") buf ++= spaceString + disassembly.value.replaceAll("\n", "\n" + spaceString)
    buf.toString
  }
}
// this could be done at compile time (I believe) but it isn't speed-critical
// RG 10/21/15
abstract class GeneratedCommand extends Command with GeneratedInstruction {
  override def init(workspace: Workspace): Unit = {
    super.init(workspace)
    switches = original.asInstanceOf[Command].switches
  }
}
abstract class GeneratedReporter extends Reporter with GeneratedInstruction
