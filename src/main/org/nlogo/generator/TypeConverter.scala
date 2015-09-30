// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.generator

import org.objectweb.asm
import asm.Opcodes._
import asm.{ Label, MethodVisitor }
import org.nlogo.{ api, nvm }
import api.CompilerException
import nvm.{ ArgumentTypeException, Instruction }

private object TypeConverter {
  /**
   * @param typeFrom - what we are converting from
   * @param typeTo - what we are converting to
   * @param mv - ASM MethodVisitor to write bytes to
   * @param firstFreeJVMLocal - this tells us what local variable slots are
   *   free/available to use, to store temporary information.
   * @param instr - we a reference to the instruction so we can throw a proper
   *   exception when something goes wrong.
   * @param argIndex - this is the index of the argument that is being converted
   *   we need this, so we can create ArgumentTypeExceptions pointing to the
   *   correct culprit.
   */
  def generateConversion(typeFrom: Class[_], typeTo: Class[_], mv: MethodVisitor, firstFreeJVMLocal: Int, parentInstr: Instruction, argIndex: Int) {
    if (typeFrom == java.lang.Boolean.TYPE && (typeTo == classOf[Object] || typeTo == classOf[java.lang.Boolean]))
      from_boolean_to_Object(mv)
    else if (typeFrom == java.lang.Double.TYPE && (typeTo == classOf[Object] || typeTo == classOf[java.lang.Double]))
      from_double_to_Object(mv, firstFreeJVMLocal)
    else if (typeFrom == classOf[java.lang.Double] && typeTo == java.lang.Double.TYPE)
      from_Double_to_double(mv)
    else if (typeFrom == classOf[Object] && typeTo == java.lang.Double.TYPE)
      from_Object_to_double(mv, firstFreeJVMLocal, argIndex)
    else if (typeFrom == classOf[java.lang.Boolean] && typeTo == java.lang.Boolean.TYPE)
      from_Boolean_to_boolean(mv)
    else if (typeFrom == classOf[Object] && typeTo == java.lang.Boolean.TYPE)
      from_Object_to_boolean(mv, firstFreeJVMLocal, argIndex)
    else if (typeTo != typeFrom && !typeTo.isAssignableFrom(typeFrom))
      if (typeFrom.isAssignableFrom(typeTo))
        // class typeTo inherits from class typeFrom, so we must cast, to narrow the type.
        // e.g. typeFrom=Agent, typeTo=Turtle
        castObjectToObject(typeTo, mv, firstFreeJVMLocal, argIndex)
      else {
        // conversion from typeFrom to typeTo is impossible.
        val argEx = new ArgumentTypeException(null, parentInstr, argIndex, api.Syntax.getTypeConstant(typeTo), typeFrom)
        val tokenInstr = if (argIndex < parentInstr.args.length) parentInstr.args(argIndex)
        else parentInstr
        val token = tokenInstr.token
        throw new CompilerException(argEx.getMessage, token.startPos, token.endPos, token.fileName)
      }
  }
  // Conversion methods:
  // method fromXtoY assumes that the top jvm stack value is of type X
  // the method generates code to convert the top stack value to type Y
  //   ~Forrest (5/16/2006)
  private def from_Double_to_double(mv: MethodVisitor) {
    mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Double", "doubleValue", "()D", false)
  }
  private def from_Object_to_double(mv: MethodVisitor, firstFreeJVMLocal: Int, argIndex: Int) {
    mv.visitVarInsn(ASTORE, firstFreeJVMLocal)
    val l0 = new Label
    val l1 = new Label
    mv.visitTryCatchBlock(l0, l1, l1, "java/lang/ClassCastException")
    mv.visitLabel(l0)
    mv.visitVarInsn(ALOAD, firstFreeJVMLocal)
    mv.visitTypeInsn(CHECKCAST, "java/lang/Double")
    mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Double", "doubleValue", "()D", false)
    val lEnd = new Label
    mv.visitJumpInsn(GOTO, lEnd)
    mv.visitLabel(l1)
    mv.visitInsn(POP) // throw away the ClassCastException
    mv.visitTypeInsn(NEW, "org/nlogo/nvm/ArgumentTypeException")
    mv.visitInsn(DUP)
    mv.visitVarInsn(ALOAD, 1) // context
    mv.visitVarInsn(ALOAD, 0) // this (GeneratedInstruction)
    mv.visitIntInsn(BIPUSH, argIndex) // index of argument that was wrong type
    mv.visitLdcInsn(api.Syntax.NumberType.toInt)
    mv.visitVarInsn(ALOAD, firstFreeJVMLocal)
    mv.visitMethodInsn(INVOKESPECIAL, "org/nlogo/nvm/ArgumentTypeException", "<init>",
      "(Lorg/nlogo/nvm/Context;Lorg/nlogo/nvm/Instruction;IILjava/lang/Object;)V", false)
    mv.visitInsn(ATHROW)
    mv.visitLabel(lEnd)
  }
  private def from_Boolean_to_boolean(mv: MethodVisitor) {
    mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Boolean", "booleanValue", "()Z", false)
  }
  private def from_Object_to_boolean(mv: MethodVisitor, firstFreeJVMLocal: Int, argIndex: Int) {
    mv.visitVarInsn(ASTORE, firstFreeJVMLocal)
    val l0 = new Label
    val l1 = new Label
    mv.visitTryCatchBlock(l0, l1, l1, "java/lang/ClassCastException")
    mv.visitLabel(l0)
    mv.visitVarInsn(ALOAD, firstFreeJVMLocal)
    mv.visitTypeInsn(CHECKCAST, "java/lang/Boolean")
    mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Boolean", "booleanValue", "()Z", false)
    val lEnd = new Label
    mv.visitJumpInsn(GOTO, lEnd)
    mv.visitLabel(l1)
    mv.visitInsn(POP) // throw away the ClassCastException
    mv.visitTypeInsn(NEW, "org/nlogo/nvm/ArgumentTypeException")
    mv.visitInsn(DUP)
    mv.visitVarInsn(ALOAD, 1) // context
    mv.visitVarInsn(ALOAD, 0) // this (GeneratedInstruction)
    mv.visitIntInsn(BIPUSH, argIndex) // index of argument that was wrong type
    mv.visitLdcInsn(Int.box(api.Syntax.BooleanType))
    mv.visitVarInsn(ALOAD, firstFreeJVMLocal)
    mv.visitMethodInsn(INVOKESPECIAL, "org/nlogo/nvm/ArgumentTypeException", "<init>",
      "(Lorg/nlogo/nvm/Context;Lorg/nlogo/nvm/Instruction;IILjava/lang/Object;)V", false)
    mv.visitInsn(ATHROW)
    mv.visitLabel(lEnd)
  }
  private def from_double_to_Object(mv: MethodVisitor, firstFreeJVMLocal: Int) {
    mv.visitVarInsn(DSTORE, firstFreeJVMLocal)
    mv.visitTypeInsn(NEW, "java/lang/Double")
    mv.visitInsn(DUP)
    mv.visitVarInsn(DLOAD, firstFreeJVMLocal)
    mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Double", "<init>", "(D)V", false)
  }
  private def from_boolean_to_Object(mv: MethodVisitor) {
    // Code, roughly speaking:  bval ? Boolean.TRUE : Boolean.FALSE
    val l1 = new Label
    mv.visitJumpInsn(IFEQ, l1)
    mv.visitFieldInsn(GETSTATIC, "java/lang/Boolean", "TRUE", "Ljava/lang/Boolean;")
    val l2 = new Label
    mv.visitJumpInsn(GOTO, l2)
    mv.visitLabel(l1)
    mv.visitFieldInsn(GETSTATIC, "java/lang/Boolean", "FALSE", "Ljava/lang/Boolean;")
    mv.visitLabel(l2)
  }
  private def castObjectToObject(typeTo: Class[_], mv: MethodVisitor, firstFreeJVMLocal: Int, argIndex: Int) {
    mv.visitVarInsn(ASTORE, firstFreeJVMLocal)
    val l0 = new Label
    val l1 = new Label
    mv.visitTryCatchBlock(l0, l1, l1, "java/lang/ClassCastException")
    mv.visitLabel(l0)
    mv.visitVarInsn(ALOAD, firstFreeJVMLocal)
    mv.visitTypeInsn(CHECKCAST, asm.Type.getInternalName(typeTo))
    val lEnd = new Label()
    mv.visitJumpInsn(GOTO, lEnd)
    mv.visitLabel(l1)
    mv.visitInsn(POP) // throw away the ClassCastException
    mv.visitTypeInsn(NEW, "org/nlogo/nvm/ArgumentTypeException")
    mv.visitInsn(DUP)
    mv.visitVarInsn(ALOAD, 1) // context
    mv.visitVarInsn(ALOAD, 0) // this (GeneratedInstruction)
    mv.visitIntInsn(BIPUSH, argIndex) // index of argument that was wrong type
    mv.visitLdcInsn(Int.box(api.Syntax.getTypeConstant(typeTo)))
    mv.visitVarInsn(ALOAD, firstFreeJVMLocal)
    mv.visitMethodInsn(INVOKESPECIAL, "org/nlogo/nvm/ArgumentTypeException", "<init>",
      "(Lorg/nlogo/nvm/Context;Lorg/nlogo/nvm/Instruction;IILjava/lang/Object;)V", false)
    mv.visitInsn(ATHROW)
    mv.visitLabel(lEnd)
  }
}
