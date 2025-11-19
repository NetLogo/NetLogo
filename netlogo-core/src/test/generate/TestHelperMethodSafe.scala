// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.generate

import java.lang.reflect.Method

import org.nlogo.nvm.Instruction
import org.nlogo.util.AnyFunSuiteEx

import org.objectweb.asm.Opcodes.INVOKESTATIC
import org.objectweb.asm.Type

// MethodRipper won't let inlinable methods call helper methods in the same class.

class TestHelperMethodSafe extends AnyFunSuiteEx with AllPrimitivesTester {
  override def makeVisitor(method: Method) =
    new EmptyMethodVisitor {
      override def visitMethodInsn(opcode: Int, owner: String, name: String, desc: String): Unit = {
        assert(name == "displayName" ||
               owner != Type.getInternalName(method.getDeclaringClass) ||
               opcode == INVOKESTATIC ||
               BytecodeUtils.checkClassHasMethod(classOf[Instruction], name, desc),
               name)
      }
    }
}
