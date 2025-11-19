// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.generate

import java.lang.reflect.Method

import org.nlogo.nvm.Reporter
import org.nlogo.util.AnyFunSuiteEx

import org.objectweb.asm.Opcodes.ATHROW
import org.objectweb.asm.{ Label, MethodVisitor }

// Commands can have any try/catch blocks, but in Reporters use of them is restricted.

class TestTryCatchSafe extends AnyFunSuiteEx with AllPrimitivesTester {
  override def filter(c: Class[?]) =
    classOf[Reporter].isAssignableFrom(c)
  override def makeVisitor(method: Method): MethodVisitor =
    new EmptyMethodVisitor {
      val handlerLabels = new collection.mutable.HashSet[Label]
      // found: false = looking for an error handler label
      //         true = found handler label, now looking for an ATHROW
      //                to occur before any branching instructions.
      private var found = false
      override def visitTryCatchBlock(start: Label, end: Label, handler: Label, tpe: String): Unit = {
        handlerLabels += handler }
      override def visitLabel(label: Label): Unit = {
        if(handlerLabels(label)) found = true }
      override def visitJumpInsn(opcode: Int, label: Label): Unit = {
        assert(!found, method.toString) }
      override def visitInsn(opcode: Int): Unit = {
        if(opcode == ATHROW) found = false }
    }
}
