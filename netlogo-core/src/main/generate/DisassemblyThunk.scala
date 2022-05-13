// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.generate

import org.nlogo.nvm.Thunk
import org.objectweb.asm, asm.ClassReader, asm.util.TraceClassVisitor

class DisassemblyThunk(bytecode: Array[Byte]) extends Thunk[String] {
  def isBoring(line: String) =
    List("\\s*LINENUMBER.*", "\\s*MAXSTACK.*", "\\s*MAXLOCALS.*").exists(line.matches(_))
  def compute = {
    val sw = new java.io.StringWriter
    new ClassReader(bytecode).accept(new TraceClassVisitor(new java.io.PrintWriter(sw)), 0)
    // (?s) = let dot match newlines. match until blank line (don't include init method)
    """(?s)public final (?:perform|report).*?\n(.*?)\n\s*\n""".r
      .findFirstMatchIn(sw.getBuffer.toString).get.subgroups.head
      .split("\n").filter(!isBoring(_)).mkString("\n")
  }
}
