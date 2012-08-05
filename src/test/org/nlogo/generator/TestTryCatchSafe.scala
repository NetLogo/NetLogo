// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.generator

import org.scalatest.FunSuite
import java.lang.reflect.Method
import org.objectweb.asm.Opcodes.ATHROW
import org.objectweb.asm.{ ClassReader, Label, Type }
import org.objectweb.asm.commons.EmptyVisitor
import org.nlogo.api.Version
import org.nlogo.nvm.Reporter

// Commands can have any try/catch blocks, but in Reporters use of them is restricted.

class TestTryCatchSafe extends FunSuite {

  type ReporterClass = Class[_ <: Reporter]

  if(Version.useGenerator)
    for(c <- allReporterClasses(new java.io.File("src/main/org/nlogo/prim")))
      test(c.getName) {
        processClass(c)
      }

  // not all primitives are listed in tokens.txt, because some of them are only used internally so
  // they only have an internal name.  so we have to actually look on disk. - ST 2/12/09
  def allReporterClasses(dir: java.io.File): List[ReporterClass] =
    if(dir.getName.startsWith(".")) Nil
    else dir.list().map(new java.io.File(dir, _)).toList.flatMap(fileOrDir =>
      if(fileOrDir.isDirectory()) allReporterClasses(fileOrDir)
      else if(fileOrDir.getName.endsWith(".java")) file2class(fileOrDir)
           else Nil)
  def file2class(f: java.io.File): Option[ReporterClass] = {
    import java.io.File.separatorChar
    val c = Class.forName(f.getAbsolutePath.split(separatorChar).toList
                           .dropWhile(_ != "org")
                           .mkString(".")
                           .replaceAll(".java$", ""))
    if(classOf[org.nlogo.nvm.Reporter].isAssignableFrom(c))
      Some(c.asInstanceOf[ReporterClass])
    else None
  }
  def processClass(c: ReporterClass) {
    val reader = PrimitiveCache.getClassReader(c)
    for(method <- BytecodeUtils.getMethods(c))
      reader.accept(new MethodExtractorClassAdapter(method),
                    ClassReader.SKIP_DEBUG)
  }

  class MethodExtractorClassAdapter(method: Method) extends EmptyVisitor {
    override def visitMethod(arg0: Int, name: String, descriptor: String, signature: String, exceptions: Array[String]) =
      if(name == method.getName && descriptor == Type.getMethodDescriptor(method))
        new TryCatchSafeMethodChecker
      else new EmptyVisitor
    class TryCatchSafeMethodChecker extends EmptyVisitor {
      val handlerLabels = new collection.mutable.HashSet[Label]
      // found: false = looking for an error handler label
      //         true = found handler label, now looking for an ATHROW
      //                to occur before any branching instructions.
      var found = false
      override def visitTryCatchBlock(start: Label, end: Label, handler: Label, tpe: String) { handlerLabels += handler }
      override def visitLabel(label: Label) { if(handlerLabels(label)) found = true }
      override def visitJumpInsn(opcode: Int, label: Label) { assert(!found, method.toString) }
      override def visitInsn(opcode: Int) { if(opcode == ATHROW) found = false }
    }
  }

}
