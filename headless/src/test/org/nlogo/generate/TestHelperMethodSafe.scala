// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.generate

import org.scalatest.FunSuite
import java.lang.reflect.Method
import org.objectweb.asm.Opcodes.INVOKESTATIC
import org.objectweb.asm.Type
import org.nlogo.nvm.Instruction

// MethodRipper won't let inlinable methods call helper methods in the same class.

class TestHelperMethodSafe extends FunSuite with AllPrimitivesTester {
  override def makeVisitor(method: Method) =
    new EmptyMethodVisitor {
      override def visitMethodInsn(opcode: Int, owner: String, name: String, desc: String) {
        assert(name == "displayName" ||
               owner != Type.getInternalName(method.getDeclaringClass) ||
               opcode == INVOKESTATIC ||
               BytecodeUtils.checkClassHasMethod(classOf[Instruction], name, desc),
               name)
      }
    }
}
