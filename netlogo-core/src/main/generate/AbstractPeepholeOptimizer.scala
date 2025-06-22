// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.generate

import
  org.objectweb.asm.{ Label, MethodVisitor, Opcodes },
    Opcodes.{ ASM5, INVOKEINTERFACE }

abstract class AbstractPeepholeOptimizer(mv: MethodVisitor) extends MethodVisitor(ASM5, mv) {

  def restartMatch(): Unit // abstract

  // the rest of the methods are all the same. we just insert a call to restartMatch()

  override def visitJumpInsn(opcode: Int, label: Label): Unit = {
    restartMatch()
    mv.visitJumpInsn(opcode, label)
  }
  override def visitLabel(label: Label): Unit = {
    restartMatch()
    mv.visitLabel(label)
  }
  override def visitInsn(opcode: Int): Unit = {
    restartMatch()
    mv.visitInsn(opcode)
  }
  override def visitIntInsn(opcode: Int, operand: Int): Unit = {
    restartMatch()
    mv.visitIntInsn(opcode, operand)
  }
  override def visitVarInsn(opcode: Int, variable: Int): Unit = {
    restartMatch()
    mv.visitVarInsn(opcode, variable)
  }
  override def visitTypeInsn(opcode: Int, desc: String): Unit = {
    restartMatch()
    mv.visitTypeInsn(opcode, desc)
  }
  override def visitFieldInsn(opcode: Int, owner: String, name: String, desc: String): Unit = {
    restartMatch()
    mv.visitFieldInsn(opcode, owner, name, desc)
  }
  @deprecated("Do not create additional uses of this method.", since = "1.0")
  override def visitMethodInsn(opcode: Int, owner: String, name: String, desc: String): Unit = {
     restartMatch()
     mv.visitMethodInsn(opcode, owner, name, desc, opcode == INVOKEINTERFACE)
   }
  override def visitMethodInsn(opcode: Int, owner: String, name: String, desc: String, itf: Boolean): Unit = {
    restartMatch()
    mv.visitMethodInsn(opcode, owner, name, desc, itf)
  }
  override def visitLdcInsn(cst: Object): Unit = {
    restartMatch()
    mv.visitLdcInsn(cst)
  }
  override def visitIincInsn(variable: Int, increment: Int): Unit = {
    restartMatch()
    mv.visitIincInsn(variable, increment)
  }
  override def visitLookupSwitchInsn(dflt: Label, keys: Array[Int], labels: Array[Label]): Unit = {
    restartMatch()
    mv.visitLookupSwitchInsn(dflt, keys, labels)
  }
  override def visitMultiANewArrayInsn(desc: String, dims: Int): Unit = {
    restartMatch()
    mv.visitMultiANewArrayInsn(desc, dims)
  }
  override def visitTryCatchBlock(start: Label, end: Label, handler: Label, tpe: String): Unit = {
    restartMatch()
    mv.visitTryCatchBlock(start, end, handler, tpe)
  }
  override def visitLocalVariable(name: String, desc: String, signature: String, start: Label, end: Label, index: Int): Unit = {
    restartMatch()
    mv.visitLocalVariable(name, desc, signature, start, end, index)
  }
  override def visitLineNumber(line: Int, start: Label): Unit = {
    restartMatch()
    mv.visitLineNumber(line, start)
  }
  override def visitMaxs(maxStack: Int, maxLocals: Int): Unit = {
    restartMatch()
    mv.visitMaxs(maxStack, maxLocals)
  }
  override def visitEnd(): Unit = {
    restartMatch()
    mv.visitEnd()
  }
}
