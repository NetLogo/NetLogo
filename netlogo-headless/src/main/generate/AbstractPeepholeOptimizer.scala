// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.generate

import
  org.objectweb.asm.{ Label, MethodVisitor, Opcodes },
    Opcodes.{ ASM5, INVOKEINTERFACE }

abstract class AbstractPeepholeOptimizer(mv: MethodVisitor) extends MethodVisitor(ASM5, mv) {

  def restartMatch() // abstract

  // the rest of the methods are all the same. we just insert a call to restartMatch()

  override def visitJumpInsn(opcode: Int, label: Label) {
    restartMatch()
    mv.visitJumpInsn(opcode, label)
  }
  override def visitLabel(label: Label) {
    restartMatch()
    mv.visitLabel(label)
  }
  override def visitInsn(opcode: Int) {
    restartMatch()
    mv.visitInsn(opcode)
  }
  override def visitIntInsn(opcode: Int, operand: Int) {
    restartMatch()
    mv.visitIntInsn(opcode, operand)
  }
  override def visitVarInsn(opcode: Int, variable: Int) {
    restartMatch()
    mv.visitVarInsn(opcode, variable)
  }
  override def visitTypeInsn(opcode: Int, desc: String) {
    restartMatch()
    mv.visitTypeInsn(opcode, desc)
  }
  override def visitFieldInsn(opcode: Int, owner: String, name: String, desc: String) {
    restartMatch()
    mv.visitFieldInsn(opcode, owner, name, desc)
  }
  @Deprecated
  override def visitMethodInsn(opcode: Int, owner: String, name: String, desc: String) {
     restartMatch()
     mv.visitMethodInsn(opcode, owner, name, desc, opcode == INVOKEINTERFACE)
   }
  override def visitMethodInsn(opcode: Int, owner: String, name: String, desc: String, itf: Boolean) {
    restartMatch()
    mv.visitMethodInsn(opcode, owner, name, desc, itf)
  }
  override def visitLdcInsn(cst: Object) {
    restartMatch()
    mv.visitLdcInsn(cst)
  }
  override def visitIincInsn(variable: Int, increment: Int) {
    restartMatch()
    mv.visitIincInsn(variable, increment)
  }
  override def visitLookupSwitchInsn(dflt: Label, keys: Array[Int], labels: Array[Label]) {
    restartMatch()
    mv.visitLookupSwitchInsn(dflt, keys, labels)
  }
  override def visitMultiANewArrayInsn(desc: String, dims: Int) {
    restartMatch()
    mv.visitMultiANewArrayInsn(desc, dims)
  }
  override def visitTryCatchBlock(start: Label, end: Label, handler: Label, tpe: String) {
    restartMatch()
    mv.visitTryCatchBlock(start, end, handler, tpe)
  }
  override def visitLocalVariable(name: String, desc: String, signature: String, start: Label, end: Label, index: Int) {
    restartMatch()
    mv.visitLocalVariable(name, desc, signature, start, end, index)
  }
  override def visitLineNumber(line: Int, start: Label) {
    restartMatch()
    mv.visitLineNumber(line, start)
  }
  override def visitMaxs(maxStack: Int, maxLocals: Int) {
    restartMatch()
    mv.visitMaxs(maxStack, maxLocals)
  }
  override def visitEnd() {
    restartMatch()
    mv.visitEnd()
  }
}
