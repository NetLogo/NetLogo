// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.generate

// This is where the knowledge of how to custom-bytecode-generate some special primitives resides.
// It used to reside in the individual primitives, but I didn't like that the prims had to depend on
// ASM guts.  So it was a choice between the lesser of two evils -- the old way with the ASM
// dependency, or the new way which is totally not object oriented and splits up the details on each
// prim between the prim classes and here.
//
// The reason this way seems less ugly to me is that the whole concept of each prim being a
// self-contained package of knowledge about itself really breaks down when you get to control
// structures.  It doesn't bother me at all for the compiler to have its own special knowledge about
// how to deal with things like _call/_callreport and short-circuiting prims like _and/_or.
//
// The fact that the list of custom generated prims also includes all of our variadic prims like
// _list is ugly, though.  Those prims really ought to be able to just tell the compiler that they
// are variadic and have the compiler figure out the rest.  I'd like to fix that.
//
// - ST 2/22/08

// The generated code doesn't start with anything on the stack.  To evaluate our arguments, we need
// to call GeneratorAdapter.generateInstruction() ~Forrest (7/20/2006), ST 2/17/08

import org.objectweb.asm
import asm.Label
import asm.Opcodes._
import org.nlogo.nvm.CustomGenerated
import org.nlogo.prim.{ _and, _call, _callreport, _list, _or, _word }

class CustomGenerator(profilingEnabled: Boolean) {

  def generate(instr: CustomGenerated, nlgen: GeneratorAdapter, thisInstrUID: Int, ip: Int) {
    instr match {
      case instr: _and =>
        generateAnd(instr, nlgen, thisInstrUID)
      case instr: _or =>
        generateOr(instr, nlgen, thisInstrUID)
      case instr: _call =>
        generateCall(instr, nlgen, thisInstrUID)
      case instr: _callreport =>
        generateCallReport(instr, nlgen, thisInstrUID, ip)
      case instr: _list =>
        generateList(instr, nlgen, thisInstrUID)
      case instr: _word =>
        generateWord(instr, nlgen, thisInstrUID)
    }
  }

  private def generateAnd(instr: _and, mv: GeneratorAdapter, thisInstrUID: Int) {
    mv.generateArgument(instr, 0, java.lang.Boolean.TYPE, thisInstrUID)
    val l1 = new Label
    mv.visitJumpInsn(IFEQ, l1)
    mv.generateArgument(instr, 1, java.lang.Boolean.TYPE, thisInstrUID)
    val lEnd = new Label
    mv.visitJumpInsn(GOTO, lEnd)
    mv.visitLabel(l1)
    mv.visitInsn(ICONST_0)
    mv.visitLabel(lEnd)
  }

  private def generateOr(instr: _or, mv: GeneratorAdapter, thisInstrUID: Int) {
    mv.generateArgument(instr, 0, java.lang.Boolean.TYPE, thisInstrUID)
    val l1 = new Label
    mv.visitJumpInsn(IFNE, l1)
    mv.generateArgument(instr, 1, java.lang.Boolean.TYPE, thisInstrUID)
    val lEnd = new Label
    mv.visitJumpInsn(GOTO, lEnd)
    mv.visitLabel(l1)
    mv.visitInsn(ICONST_1)
    mv.visitLabel(lEnd)
  }

  /*  Example code, just to show what bytecode is being generated
   public void perform_N (Object o0, Object o1) {
     Object[] args = new Object[procedure.size]
     args[ 0 ] = o0
     args[ 1 ] = o1
     ...
     Activation newActivation =
     new Activation(procedure, context.activation, args, n)
     context.activation = newActivation
     context.ip = 0
     // included only if profiling data collection:
     workspace.profilingTracer().openCallRecord(context, newActivation)
   }
   */
  private def generateCall(instr: _call, mv: GeneratorAdapter, thisInstrUID: Int) {
    mv.keepField("procedure", instr.procedure, thisInstrUID)
    mv.visitTypeInsn(NEW, "org/nlogo/nvm/Activation")
    // stack: Activation
    mv.visitInsn(DUP)
    // stack: Activation Activation
    mv.loadKeptField("procedure", thisInstrUID)
    // stack: Activation Activation Procedure
    mv.visitVarInsn(ALOAD, 1)
    // stack: Activation Activation Procedure Context
    mv.visitFieldInsn(GETFIELD, "org/nlogo/nvm/Context", "activation", "Lorg/nlogo/nvm/Activation;")
    // stack: Activation Activation Procedure Activation
    mv.visitIntInsn(SIPUSH, instr.procedure.size)
    // stack: Activation Activation Procedure Activation Int
    mv.visitTypeInsn(ANEWARRAY, "java/lang/Object")
    // stack: Activation Activation Procedure Activation Args[]
    for (i <- 0 until (instr.procedure.args.size - instr.procedure.localsCount)) {
      mv.visitInsn(DUP)
      // stack: Activation Activation Procedure Activation Args[] Args[]
      mv.push(i)
      // stack: Activation Activation Procedure Activation Args[] i
      mv.generateArgument(instr, i, classOf[Object], thisInstrUID)
      // stack: Activation Activation Procedure Activation Args[] i Arg
      mv.visitInsn(AASTORE)
      // stack: Activation Activation Procedure Activation Args[]
    }
    mv.push(instr.next)
    // stack: Activation Activation Procedure Activation Args[] int
    mv.visitMethodInsn(INVOKESPECIAL, "org/nlogo/nvm/Activation", "<init>", "(Lorg/nlogo/nvm/Procedure;Lorg/nlogo/nvm/Activation;[Ljava/lang/Object;I)V", false)
    // operand stack: Activation
    // if profiling,we'll need an extra Activation on the stack.
    if (profilingEnabled) mv.visitInsn(DUP)
    mv.visitVarInsn(ALOAD, 1)
    // operand stack: [Activation] Activation Context
    mv.visitInsn(SWAP)
    // operand stack: [Activation] Context Activation
    mv.visitFieldInsn(PUTFIELD, "org/nlogo/nvm/Context", "activation", "Lorg/nlogo/nvm/Activation;")
    // operand stack: [Activation]
    mv.visitVarInsn(ALOAD, 1)
    mv.visitInsn(ICONST_0)
    // operand stack: [Activation] Context 0
    mv.visitFieldInsn(PUTFIELD, "org/nlogo/nvm/Context", "ip", "I")
    // operand stack: [Activation]
    if (profilingEnabled) {
      // operand stack: Activation
      mv.visitVarInsn(ALOAD, 0)
      // operand stack: Activation Instruction
      mv.visitFieldInsn(GETFIELD, "org/nlogo/nvm/Instruction", "workspace", "Lorg/nlogo/nvm/Workspace;")
      // operand stack: Activation Workspace
      mv.visitMethodInsn(INVOKEINTERFACE, "org/nlogo/nvm/Workspace", "profilingTracer", "()Lorg/nlogo/nvm/Tracer;", true)
      // operand stack: Activation Tracer
      mv.visitInsn(SWAP)
      // operand stack: Tracer Activation
      mv.visitVarInsn(ALOAD, 1)
      mv.visitInsn(SWAP)
      // operand stack: Tracer Context Activation
      mv.visitMethodInsn(INVOKEVIRTUAL, "org/nlogo/nvm/Tracer", "openCallRecord",
        "(Lorg/nlogo/nvm/Context;Lorg/nlogo/nvm/Activation;)V", false)
    }
  }

  /* Example code, just to show what bytecode is being generated
   *
   public void perform_N (Object o0, Object o1) {
     Object[] args = new Object[procedure.size];
     args[ 0 ] = o0
     args[ 1 ] = o1
     ...
     Activation newActivation = new Activation(procedure, context.activation, args n)
     context.activation = newActivation
     context.ip = 0
     // included only if profiling data collection:
     workspace.profilingTracer().openCallRecord(context, newActivation)
   }
   */
  private def generateCallReport(instr: _callreport, mv: GeneratorAdapter, thisInstrUID: Int, ip: Int) {
    mv.keepField("procedure", instr.procedure, thisInstrUID)
    mv.visitTypeInsn(NEW, "org/nlogo/nvm/Activation")
    // stack: Activation
    mv.visitInsn(DUP)
    // stack: Activation Activation
    mv.loadKeptField("procedure", thisInstrUID)
    // stack: Activation Activation Procedure
    mv.visitVarInsn(ALOAD, 1)
    // stack: Activation Activation Procedure Context
    mv.visitFieldInsn(GETFIELD, "org/nlogo/nvm/Context", "activation", "Lorg/nlogo/nvm/Activation;")
    // stack: Activation Activation Procedure Activation
    mv.visitIntInsn(SIPUSH, instr.procedure.size)
    // stack: Activation Activation Procedure Activation Int
    mv.visitTypeInsn(ANEWARRAY, "java/lang/Object")
    // stack: Activation Activation Procedure Activation Args[]
    for (i <- 0 until (instr.procedure.args.size - instr.procedure.localsCount)) {
      mv.visitInsn(DUP)
      // stack: Activation Activation Procedure Activation Args[] Args[]
      mv.push(i)
      // stack: Activation Activation Procedure Activation Args[] i
      mv.generateArgument(instr, i, classOf[Object], thisInstrUID)
      // stack: Activation Activation Procedure Activation Args[] i Arg
      mv.visitInsn(AASTORE)
      // stack: Activation Activation Procedure Activation Args[]
    }
    mv.push(ip)
    // stack: Activation Activation Procedure Activation Args[] int
    mv.visitMethodInsn(INVOKESPECIAL, "org/nlogo/nvm/Activation", "<init>", "(Lorg/nlogo/nvm/Procedure;Lorg/nlogo/nvm/Activation;[Ljava/lang/Object;I)V", false)
    // if profiling,we'll need an extra Activation on the stack.
    if (profilingEnabled) mv.visitInsn(DUP)
    // operand stack: [Activation] Activation
    if (profilingEnabled) {
      mv.visitInsn(DUP)
      // operand stack: Activation Activation Activation
      mv.visitVarInsn(ALOAD, 0)
      // operand stack: Activation Activation Activation Instruction
      mv.visitFieldInsn(GETFIELD, "org/nlogo/nvm/Instruction", "workspace", "Lorg/nlogo/nvm/Workspace;")
      // operand stack: Activation Activation Activation Workspace
      mv.visitMethodInsn(INVOKEINTERFACE, "org/nlogo/nvm/Workspace", "profilingTracer", "()Lorg/nlogo/nvm/Tracer;", true)
      // operand stack: Activation Activation Activation Tracer
      mv.visitInsn(SWAP)
      // operand stack: Activation Activation Tracer Activation
      mv.visitVarInsn(ALOAD, 1)
      mv.visitInsn(SWAP)
      // operand stack: Activation Activation Tracer Context Activation
      mv.visitMethodInsn(INVOKEVIRTUAL, "org/nlogo/nvm/Tracer", "openCallRecord",
        "(Lorg/nlogo/nvm/Context;Lorg/nlogo/nvm/Activation;)V", false)
    }
    // operand stack: [Activation] Activation
    mv.visitVarInsn(ALOAD, 1)
    mv.visitInsn(SWAP)
    mv.visitMethodInsn(INVOKEVIRTUAL, "org/nlogo/nvm/Context", "callReporterProcedure",
      "(Lorg/nlogo/nvm/Activation;)Ljava/lang/Object;", false)
    // operand stack: [Activation] resultObj
    if (profilingEnabled) {
      mv.visitInsn(SWAP)
      mv.visitVarInsn(ALOAD, 0)
      // operand stack: resultObj Activation Instruction
      mv.visitFieldInsn(GETFIELD, "org/nlogo/nvm/Instruction", "workspace", "Lorg/nlogo/nvm/Workspace;")
      // operand stack: resultObj Activation Workspace
      mv.visitMethodInsn(INVOKEINTERFACE, "org/nlogo/nvm/Workspace", "profilingTracer", "()Lorg/nlogo/nvm/Tracer;", true)
      // operand stack: resultObj Activation Tracer
      mv.visitInsn(SWAP)
      // operand stack: resultObj Tracer Activation
      mv.visitVarInsn(ALOAD, 1)
      mv.visitInsn(SWAP)
      // operand stack: resultObj Tracer Context Activation
      mv.visitMethodInsn(INVOKEVIRTUAL, "org/nlogo/nvm/Tracer", "closeCallRecord",
        "(Lorg/nlogo/nvm/Context;Lorg/nlogo/nvm/Activation;)V", false)
    }
    // operand stack: resultObj
    mv.visitInsn(DUP)
    val lSkip = new Label
    mv.visitJumpInsn(IFNONNULL, lSkip)
    mv.visitTypeInsn(NEW, "org/nlogo/nvm/RuntimePrimitiveException")
    mv.visitInsn(DUP)
    mv.visitVarInsn(ALOAD, 1)
    mv.visitVarInsn(ALOAD, 0)
    mv.visitLdcInsn("the " + instr.procedure.name + " procedure failed to report a result")
    mv.visitMethodInsn(INVOKESPECIAL, "org/nlogo/nvm/RuntimePrimitiveException", "<init>",
      "(Lorg/nlogo/api/Context;Lorg/nlogo/nvm/Instruction;Ljava/lang/String;)V", false)
    mv.visitInsn(ATHROW)
    mv.visitLabel(lSkip)
    // operand stack: resultObj
  }

  /* Example code,just to show what bytecode is being generated
   public LogoList report_N (Object o0,Object o1) {
     LogoListBuilder list = new LogoListBuilder()
     list.add(args[0])
     list.add(args[1])
     //...
     return list.toLogoList()
   }
   */
  private def generateList(instr: _list, mv: GeneratorAdapter, thisInstrUID: Int) {
    mv.visitTypeInsn(NEW, "org/nlogo/api/LogoListBuilder")
    mv.visitInsn(DUP)
    mv.visitMethodInsn(INVOKESPECIAL, "org/nlogo/api/LogoListBuilder", "<init>", "()V", false)
    for (i <- 0 until instr.args.length) {
      mv.visitInsn(DUP)
      // recursively generate each argument
      mv.generateArgument(instr, i, classOf[Object], thisInstrUID)
      mv.markLineNumber(thisInstrUID)
      mv.visitMethodInsn(INVOKEVIRTUAL, "org/nlogo/api/LogoListBuilder", "add", "(Ljava/lang/Object;)V", false)
    }
    mv.visitMethodInsn(INVOKEVIRTUAL, "org/nlogo/api/LogoListBuilder", "toLogoList", "()Lorg/nlogo/core/LogoList;", false)
  }

  /* Example code,just to show what bytecode is being generated
   *
   public String report_N(Object o0,Object o1)
   {
   StringBuilder result = new StringBuilder()
   result.append(Dump.logoObject(o0))
   result.append(Dump.logoObject(o1))
   //...
   return result.toString()
   }*/
  private def generateWord(instr: _word, mv: GeneratorAdapter, thisInstrUID: Int) {
    mv.visitTypeInsn(NEW, "java/lang/StringBuilder")
    mv.visitInsn(DUP)
    mv.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V", false)
    // operand stack: SB
    for (i <- 0 until instr.args.length) {
      mv.generateArgument(instr, i, classOf[Object], thisInstrUID)
      // operand stack: SB OBJ
      mv.visitMethodInsn(INVOKESTATIC, "org/nlogo/api/Dump", "logoObject",
        "(Ljava/lang/Object;)Ljava/lang/String;", false)
      // operand stack: SB STR
      mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append",
        "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false)
      // operand stack: SB
    }
    mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString",
      "()Ljava/lang/String;", false)
  }

  protected def generateArgsAccess(mv: GeneratorAdapter): Unit = {
    if (argsIsField)
      mv.visitFieldInsn(GETFIELD, "org/nlogo/nvm/Activation", "args", "[Ljava/lang/Object;")
    else
      mv.visitMethodInsn(INVOKEVIRTUAL, "org/nlogo/nvm/Activation", "args", "()[Ljava/lang/Object;", false)
  }

  // This is a field in NetLogo Desktop, but a method in NetLogo Headless.
  // We don't want to sacrifice speed in NetLogo-Desktop
  protected lazy val argsIsField = try {
    classOf[org.nlogo.nvm.Activation].getField("args")
    true
  }
  catch {
    case e: NoSuchFieldException => false
  }
}
