// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.generate

import org.objectweb.asm.Opcodes._
import java.lang.reflect.{ Field, Method }
import org.objectweb.asm
import asm.{ ClassReader, Label, MethodVisitor, Type }
import org.nlogo.nvm.Instruction

class MethodRipper(method: Method, instr: Instruction, mvOut: MethodVisitor, bgen: Generator#InstructionGenerator[_], instrUID: Int) {
  private val errorLog = new StringBuilder
  def writeTransformedBytecode() {
    val reader = PrimitiveCache.getClassReader(instr.getClass)
    val extractor = new MethodExtractorClassAdapter
    // When we switched to Java 6, the following line started throwing
    // exceptions until we added SKIP_FRAMES.  It wasn't clear to me
    // which was better, adding EXPAND_FRAMES or SKIP_FRAMES.  But
    // the former is flagged in the ASM doc as being slow, so I guess
    // if we can get away with SKIP_FRAMES, we should. - ST 7/19/12
    reader.accept(extractor, ClassReader.SKIP_FRAMES)
    if (errorLog.length > 0) throw new IllegalStateException(errorLog.toString)
  }
  private class MethodExtractorClassAdapter extends EmptyClassVisitor {
    override def visitMethod(arg0: Int, name: String, descriptor: String, signature: String, exceptions: Array[String]): MethodVisitor = {
      if (name == method.getName && descriptor == Type.getMethodDescriptor(method))
        new MethodTransformerAdapter
      else new EmptyMethodVisitor
    }
  }
  private class MethodTransformerAdapter extends MethodVisitor(ASM5, mvOut) {
    val endOfMethodLabel = new Label
    override def visitFieldInsn(opcode: Int, owner: String, name: String, desc: String) {
      if (owner != Type.getInternalName(instr.getClass))
        mvOut.visitFieldInsn(opcode, owner, name, desc)
      else opcode match {
        case GETFIELD =>
          if (List("workspace", "world").contains(name))
            mvOut.visitFieldInsn(opcode, bgen.fullClassName, name, desc)
          else try {
            // It'd be nice if we could just use Class.getField, but that only finds public stuff. - ST 2/3/09
            def getField(c: Class[_]): Field =
              try { c.getDeclaredField(name) }
              catch { case _: NoSuchFieldException => getField(c.getSuperclass) }
            val field = getField(instr.getClass)
            val accessCode = field.getModifiers
            field.setAccessible(true)
            val obj = field.get(instr)
            bgen.translateGetField(name, instrUID, obj, Type.getType(desc), accessCode)
          } catch {
            case ex: NoSuchFieldException => errorLog.append("MethodRipper says: " + ex)
            case ex: IllegalAccessException => errorLog.append("MethodRipper says: " + ex)
          }
        case PUTFIELD =>
          // I guess this is somewhat of an artificial constraint, since we could just translate the
          // names, and attempt to set the fields.  -- we'd have to be careful though, since some of
          // the fields are being turned into LDC ... instructions, so they'd no longer exist.
          // Let's just leave it as an error until we find a primitive with a strong reason to want
          // to set member fields.  ~Forrest (7/16/2006)
          errorLog.append("MethodRipper says: Java class " + instr.getClass +
            " not allowed to set member fields in a report/perform method!")
        case GETSTATIC =>
          // Why translate static fields?  Well, one reason is because java code like "Turtle.class"
          // gets compiled into fancy JVM code that caches the result of Class.forName() in a static
          // field of the enclosing class.  To make that caching run smoothly, we need to translate
          // those static Class fields over as well. ~Forrest (7/16/2006)
          try {
            val field = instr.getClass.getDeclaredField(name)
            val accessCode = field.getModifiers()
            field.setAccessible(true)
            val obj = field.get(instr)
            bgen.translateGetStatic(name, instrUID, obj, Type.getType(desc), accessCode)
          } catch {
            case ex: NoSuchFieldException => errorLog.append("MethodRipper says: " + ex)
            case ex: IllegalAccessException => errorLog.append("MethodRipper says: " + ex)
          }
        case PUTSTATIC =>
          // See note above for why we want to do this.  ~Forrest (7/16/2006)
          bgen.translatePutStatic(name, instrUID, desc)
        case _ => // do nothing
      }
    }
    override def visitMethodInsn(opcode: Int, owner: String, name: String, desc: String, itf: Boolean) {
      if (name == "displayName") {
        mvOut.visitInsn(POP)
        mvOut.visitLdcInsn(instr.displayName)
      } else if (owner != Type.getInternalName(instr.getClass))
        mvOut.visitMethodInsn(opcode, owner, name, desc, itf)
      else if (opcode == INVOKESTATIC) {
        if (name == "class$") {
          bgen.generateStaticClassMethod("class$")
          // handle things like "Turtle.class"
          mvOut.visitMethodInsn(opcode, bgen.fullClassName, name, desc, itf)
        } else
          // for now, I just want to know if static methods are ever called
          errorLog.append("debug: MethodRipper noticed that class " + instr.getClass() +
            " contained a static method invocation to method '" + name +
            "' in a report_X()/perform_X() method.\n")
        // What we probably want to do is just leave static method calls still pointing to the
        // old _prim class, unless they are other weird special calls like "class" above, or if
        // they are private access.  super.visitMethodInsn(opcode,owner,name,desc);
      } else if (BytecodeUtils.checkClassHasMethod(classOf[Instruction], name, desc))
        // it's probably okay to let them call a method from Instruction. ~Forrest (7/16/2006)
        mvOut.visitMethodInsn(opcode, bgen.fullClassName, name, desc, itf)
      else
        // probably calling helper function inside same class -- we don't allow that
        errorLog.append("MethodRipper says: Java class " + instr.getClass() +
          " not allowed to call method '" + name + "' in a report_X()/perform_X() method.\n")
    }
    override def visitInsn(opcode: Int) {
      // We need to change "returns" to "jump-to-end-method"
      opcode match {
        case RETURN | ARETURN | IRETURN | DRETURN | FRETURN | LRETURN =>
          mvOut.visitJumpInsn(GOTO, endOfMethodLabel)
        case _ => mvOut.visitInsn(opcode)
      }
    }
    // strip out the visitations that we don't want to pass on
    override def visitCode() {}
    override def visitEnd() { visitLabel(endOfMethodLabel) }
    override def visitMaxs(maxStack: Int, maxLocals: Int) {}
    override def visitLocalVariable(name: String, desc: String, signature: String, start: Label, end: Label, index: Int) {}
    override def visitLineNumber(line: Int, start: Label) {}
    override def visitTryCatchBlock(start: Label, end: Label, handler: Label, tpe: String) {
      mvOut.visitTryCatchBlock(start, end, handler, tpe)
    }
  }
}
