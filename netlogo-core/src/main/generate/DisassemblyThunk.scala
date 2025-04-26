// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.generate

import org.nlogo.nvm.Thunk
import org.objectweb.asm, asm.ClassReader, asm.util.TraceClassVisitor

class DisassemblyThunk(bytecode: Array[Byte]) extends Thunk[String] {
  def isBoring(line: String) =
    List("\\s*LINENUMBER.*", "\\s*MAXSTACK.*", "\\s*MAXLOCALS.*").exists(line.matches(_))
  def compute() = {
    val sw = new java.io.StringWriter
    new ClassReader(bytecode).accept(new TraceClassVisitor(new java.io.PrintWriter(sw)), 0)
    val codeString = sw.getBuffer.toString
    // (?s) = let dot match newlines. match until blank line (don't include init method)
    val firstMatch = """(?s)public final strictfp (?:perform|report).*?\n(.*?)\n\s*\n""".r
      .findFirstMatchIn(codeString)
    firstMatch.get.subgroups.head
      .split("\n").filter(!isBoring(_)).mkString("\n")
  }
}
