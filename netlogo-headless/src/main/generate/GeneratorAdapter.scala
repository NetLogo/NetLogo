// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.generate

import org.objectweb.asm.Opcodes._
import org.nlogo.nvm.Instruction
import org.objectweb.asm.{ Label, MethodVisitor, Type }

class GeneratorAdapter(mv: MethodVisitor, access: Int, name: String, desc: String, igen: Generator#InstructionGenerator[_])
    extends org.objectweb.asm.commons.GeneratorAdapter(ASM5, mv, access, name, desc) {
  // We need to know what the lowest JVM local variable that we can play with is.  var 0 = "this",
  // and var 1 = "context", so we can't mess with these.  It could be in the future, we'll want to
  // reserve more JVM local variables e.g. to store NetLogo local procedure variables in them But
  // for now, we aren't reserving anything, and var 2 is the first free var.  ~Forrest (6/22/2006)
  private val FIRST_FREE_JVM_LOCAL = 2
  def keepField(fieldName: String, obj: AnyRef, thisInstrUID: Int) {
    igen.keep(igen.remapFieldName(fieldName, thisInstrUID),
      obj, Type.getType(obj.getClass),
      ACC_PRIVATE) // could be public, too, whatever ~Forrest (7/21/2006)
  }
  def loadKeptField(fieldName: String, thisInstrUID: Int) {
    igen.loadKept(igen.remapFieldName(fieldName, thisInstrUID))
  }
  def generateArgument(instr: Instruction, argIndex: Int, retTypeWanted: Class[_], thisInstrUID: Int) {
    igen.generateInstruction(instr.args(argIndex), retTypeWanted, thisInstrUID, instr, argIndex)
    markLineNumber(thisInstrUID)
  }
  /* Line numbers are not referring to actual line numbers, but instead to Instruction numbers, as
   * ordered by the curInstructionUID field in InstructionGenerator.  They are used later when a
   * runtime error occurs, for determining which instruction, and hence token, is to blame.
   * ~Forrest (7/20/2006) */
  private var lastMarkedLineNum = -1
  def markLineNumber(lineNum: Int) {
    if (lineNum != lastMarkedLineNum) {
      lastMarkedLineNum = lineNum
      val lineLabel = new Label
      visitLabel(lineLabel)
      // bypass this's visitLineNumber, because it purposefully throws
      // an error -- instead, we call super's method.
      super.visitLineNumber(lineNum, lineLabel)
    }
  }
  override def visitLineNumber(line: Int, start: Label) {
    throw new IllegalStateException("Don't write line numbers to the NetLogoGeneratorAdapter -- line number info " +
      "is reserved for runtime error handling uses.")
  }
  def generateConversion(typeFrom: Class[_], typeTo: Class[_], parentInstr: Instruction, argIndex: Int) {
    TypeConverter.generateConversion(typeFrom, typeTo, this, FIRST_FREE_JVM_LOCAL, parentInstr, argIndex)
  }
  /**
   * Note that using "push" can create more efficient code than using "visitLdcInsn()", because when
   * possible it creates ICONST_1, or DCONST_0, etc.
   * @param obj can be any boxed primitive type, or a String.
   */
  def push(obj: AnyRef) {
    obj match {
      case b: java.lang.Boolean => push(b.booleanValue)
      case d: java.lang.Double => push(d.doubleValue)
      case f: java.lang.Float => push(f.floatValue)
      case l: java.lang.Long => push(l.longValue)
      case i: Number => push(i.intValue) // Integer, Short, or Byte
      case s: String => super.push(s)
    }
  }
  // We need to override the methods that LocalVariableSorter was working with, because the local
  // variable bytecode we generate gets broken when the variables are renumbered.  However, we still
  // subclass GeneratorAdapter, because it has lots of convenient methods in it.  ~Forrest
  override def visitVarInsn(opcode: Int, variable: Int) { mv.visitVarInsn(opcode, variable) }
  override def visitIincInsn(variable: Int, increment: Int) { mv.visitIincInsn(variable, increment) }
  override def visitMaxs(maxStack: Int, maxLocals: Int) { mv.visitMaxs(maxStack, maxLocals) }
  override def visitLocalVariable(name: String, desc: String, signature: String, start: Label, end: Label, index: Int) {
    mv.visitLocalVariable(name, desc, signature, start, end, index)
  }
  // see note above about LocalVariableSorter incompatibility
  override def newLocal(tpe: Type): Int = { throw new IllegalStateException }
  override def loadArg(arg: Int) { throw new IllegalStateException }
  override def loadArgs(arg: Int, count: Int) { throw new IllegalStateException }
  override def loadArgs() { throw new IllegalStateException }
  override def loadArgArray() { throw new IllegalStateException }
  override def storeArg(arg: Int) { throw new IllegalStateException }
  override def getLocalType(local: Int): Type = { throw new IllegalStateException }
  override def loadLocal(local: Int) { throw new IllegalStateException }
  override def storeLocal(local: Int) { throw new IllegalStateException }
  /**
   * Generates the instruction to load the given local variable on the stack.
   * @param index a local variable index.
   * @param tpe the type of this local variable.
   */
  override def loadLocal(index: Int, tpe: Type) { mv.visitVarInsn(tpe.getOpcode(ILOAD), index) }
  /**
   * Generates the instruction to store the top stack value in the given local variable.
   * @param index a local variable index.
   * @param tpe the type of this local variable.
   */
  override def storeLocal(index: Int, tpe: Type) { mv.visitVarInsn(tpe.getOpcode(ISTORE), index) }
  override def loadThis() { mv.visitVarInsn(ALOAD, 0) }
  def loadContext() { mv.visitVarInsn(ALOAD, 1) }
}
