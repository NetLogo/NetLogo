// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.generate

/**
 * This class catches one specific inefficient pattern of bytecode that the
 * inliner generates -- the ALOAD 0 / POP pair.  This bad pair gets
 * generated when we replace GETFIELDs with LDCs when Java primitive
 * types are involved.  e.g., the int "vn" field in _procedurevariable.
 *
 * It's looking for this pattern:
 *    ALOAD 0
 *    POP
 * And replaces it with nothing.
 *
 *  ~Forrest (6/19/2006)
 */

import org.objectweb.asm.{ Label, MethodVisitor }
import org.objectweb.asm.Opcodes.{ ALOAD, POP }

class PeepholeOptimizer2(mv: MethodVisitor) extends AbstractPeepholeOptimizer(mv) {

  private var seenAload0 = false

  // don't restartMatch when encountering a label
  override def visitLabel(label: Label): Unit = {
    mv.visitLabel(label)
  }

  override def restartMatch(): Unit = {
    if (seenAload0) {
      mv.visitVarInsn(ALOAD, 0)
      seenAload0 = false
    }
  }

  override def visitVarInsn(opcode: Int, variable: Int): Unit = {
    restartMatch()
    if (opcode == ALOAD && variable == 0)
      seenAload0 = true
    else
      mv.visitVarInsn(opcode, variable)
  }

  override def visitInsn(opcode: Int): Unit = {
    if (seenAload0 && opcode == POP)
      seenAload0 = false
    else super.visitInsn(opcode)
  }

}
