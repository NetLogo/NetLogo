// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.generate

import org.nlogo.core.Syntax
import org.nlogo.nvm.{ Command, CustomGenerated, GeneratorInterface, Instruction, Procedure, Reporter, Thunk }

object Generator {
  val KEPT_INSTRUCTION_PREFIX = "keptinstr"
  // If this is set to true, then it will slow down the compiler IMMENSELY.  What it does, is write
  // out the bytecode of all the GeneratedInstructions to the tmp/Generator folder.
  private val DEBUG_DUMP_CLASS_FILES = false
  // If a generated method exceeds this size in bytes, a warning will be printed to stderr
  private val METHOD_SIZE_WARNING_THRESHOLD = 32768 // half of 64K, which is the JVM limit
  // it seems like we also might want to use ASM's CheckClassAdapter during debugging?
  // (or maybe just leave it on all the time, depending on what the performance impact is?)
  // something to think about - ST 2/2/11
}

class Generator(procedure: Procedure, profilingEnabled: Boolean) extends GeneratorInterface {
  var ip = 0 // kinda ugly we need to track this only to use in one place, in generateCallReport - ST 2/10/09
  def generate() = {
    ip = 0
    procedure.code.map { cmd => val result = recurse(cmd); ip += 1; result }
  }
  private def recurse[A <: Instruction](instr: A): A = // A is Command or Reporter
    if (BytecodeUtils.isRejiggered(instr))
      new InstructionGenerator(instr).generate()
    else {
      instr.args = instr.args.map(recurse(_))
      instr
    }
  private var customClassNumUID = -1
  private def nextCustomClassNumUID(): Int = {
    customClassNumUID += 1
    customClassNumUID
  }
  // At one time in 2009 I'd tried moving the CustomClassLoader instantiation down to the point where
  // it is used, but that broke the Mathematica link for reasons I don't understand. ST 4/16/09
  private val loader = new CustomClassLoader(Thread.currentThread.getContextClassLoader)
  class InstructionGenerator[A <: Instruction](original: A) {
    import org.objectweb.asm
    import asm.Opcodes._
    import asm.{ ClassReader, ClassWriter, Label, Type }
    import asm.util.TraceClassVisitor
    val REPORT_PERFORM_ACCESS_CODES = ACC_PUBLIC + ACC_STRICT + ACC_FINAL
    // this is just used for debugging -- we can get rid of it eventually. it's placed at the end of
    // a method, so that we can measure the length of the bytecode of the generated method, to see
    // if we're getting close to the 64 K limit.  ~Forrest (8/24/2006)
    val debugEndOfMethodLabel = new Label
    val cw = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES)
    var nlgen: GeneratorAdapter = null
    val superClassFullName = original match {
      case _: Command => "org/nlogo/generate/GeneratedCommand"
      case _: Reporter => "org/nlogo/generate/GeneratedReporter"
    }
    // keep track of the "number" of the instruction we are processing useful for uniquely naming
    // fields that were inlined/kept, and used to drop fake linenumbers into the bytecode, to use to
    // tell what instruction was being executed, during error handling later.  ~Forrest (6/5/06)
    var curInstructionUID = 0
    val className = {
      val cName = original.getClass.getName
      val pName = procedure.displayName.toLowerCase.filter(_.isLetterOrDigit).mkString
      "_asm" + "_" + pName + cName.substring(cName.lastIndexOf('.') + 1) + "_" + nextCustomClassNumUID()
    }
    val fullClassName = "org/nlogo/prim/" + className
    def generate(): A = {
      cw.visit(V1_8, ACC_PUBLIC + ACC_SUPER, fullClassName, null, superClassFullName, null)
      cw.visitSource("", null)
      generateConstructor()
      val methodName = original match {
        case _: Command => "perform"
        case _: Reporter => "report"
      }
      val methodDescriptor = original match {
        case _: Command => "(Lorg/nlogo/nvm/Context;)V"
        case _: Reporter => "(Lorg/nlogo/nvm/Context;)Ljava/lang/Object;"
      }
      var mv = cw.visitMethod(REPORT_PERFORM_ACCESS_CODES, methodName, methodDescriptor,
        null, Array("org/nlogo/api/LogoException"))
      mv = new PeepholeOptimizer3B(mv) // order is important for 3B/3
      mv = new PeepholeOptimizer3(mv)
      mv = new PeepholeOptimizer3(mv) // twice, to catch two layers
      mv = new PeepholeOptimizer2(mv)
      mv = new PeepholeOptimizer1(mv)
      nlgen = new GeneratorAdapter(mv, REPORT_PERFORM_ACCESS_CODES, methodName, methodDescriptor, this)
      generateBodyMethod()
      generateInitMethod()
      generateKeptFields()
      val result = finish()
      result.agentClassString = original.agentClassString
      result.asInstanceOf[GeneratedInstruction].original = original
      result.chosenMethod = original.chosenMethod
      result
    }
    def generateConstructor() {
      val constructor = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null)
      constructor.visitCode()
      constructor.visitVarInsn(ALOAD, 0)
      constructor.visitMethodInsn(INVOKESPECIAL, superClassFullName, "<init>", "()V", false)
      constructor.visitInsn(RETURN)
      constructor.visitMaxs(0, 0)
      constructor.visitEnd()
    }
    def generateBodyMethod() {
      nlgen.visitCode()
      original match {
        case _: Command =>
          generateInstruction(original, java.lang.Void.TYPE, 0, null, -1)
          nlgen.visitInsn(RETURN)
        case _: Reporter =>
          generateInstruction(original, classOf[Object], 0, null, 0)
          nlgen.visitInsn(ARETURN)
      }
      nlgen.visitLabel(debugEndOfMethodLabel)
      nlgen.visitMaxs(0, 0)
      nlgen.visitEnd()
    }
    def generateInstruction(instr: Instruction, retTypeWanted: Class[_], parentInstrUID: Int,
                            parentInstr: Instruction, argIndex: Int) {
      curInstructionUID += 1
      // need to save the curInstructionUID value in a local var, since curInstructionUID gets
      // changed as a result of generating the children.
      // keep every instruction in a field of our GeneratedInstruction class for use with error
      // handling later, among other things.
      val thisInstrUID = curInstructionUID
      keepInstruction(instr, thisInstrUID)
      instr match {
        case cg: CustomGenerated =>
          nlgen.markLineNumber(thisInstrUID)
          new CustomGenerator(profilingEnabled).generate(cg, nlgen, thisInstrUID, ip)
          nlgen.markLineNumber(parentInstrUID)
          val actualReturnType = cg.returnType match {
            case Syntax.BooleanType => classOf[Boolean]
            case Syntax.ListType => classOf[org.nlogo.core.LogoList]
            case Syntax.StringType => classOf[String]
            case Syntax.WildcardType => classOf[Object]
            case Syntax.VoidType => java.lang.Void.TYPE
          }
          nlgen.generateConversion(actualReturnType, retTypeWanted, parentInstr, argIndex)
        case _ =>
          MethodSelector.select(instr, retTypeWanted, profilingEnabled) match {
            case None =>
              generateOldStyleCall(instr, retTypeWanted, parentInstrUID, parentInstr, argIndex)
            case Some(bestEvalMethod) =>
              // need to save the curInstructionUID value in a local var, since curInstructionUID gets
              // changed as a result of generating the children.
              // keep every instruction in a field of our GeneratedInstruction class for use with error
              // handling later, among other things.
              val thisInstrUID = curInstructionUID
              keepInstruction(instr, thisInstrUID)
              // libraryCall flag just means we invoke the report_X() or perform_X() method rather than
              // inlining it.
              val paramTypes = bestEvalMethod.getParameterTypes()
              if (paramTypes.length != instr.args.length + 1)
                throw new IllegalStateException((instr, bestEvalMethod).toString)
              for (i <- 0 until instr.args.length) {
                val paramType = paramTypes(i + 1)
                // if it's a Reporter argument, don't evaluate it.
                if (classOf[Reporter].isAssignableFrom(paramType)) {
                  curInstructionUID += 1
                  val newArg = recurse(instr.args(i))
                  instr.args(i) = newArg
                  keepAndLoadReporter(newArg, curInstructionUID)
                } else generateInstruction(instr.args(i), paramType, thisInstrUID, instr, i)
              }
              // pop off the stack into local vars, in backwards order because this is where the
              // inlined code expects to find the args.
              val paramJVMTypes = Type.getArgumentTypes(bestEvalMethod)
              var totalLocalSlots = paramJVMTypes.map(_.getSize).sum
              for (i <- paramJVMTypes.length - 1 to 1 by -1) {
                totalLocalSlots -= paramJVMTypes(i).getSize
                nlgen.visitVarInsn(paramJVMTypes(i).getOpcode(ISTORE),
                  totalLocalSlots + 1)
              }
              // PeepholeOptimizer3 will be looking for this flag.  If it finds it, then it goes on to
              // replace the pattern it's looking for.  If it doesn't find it, it aborts.
              if (new PeepholeSafeChecker(profilingEnabled).isSafe(bestEvalMethod))
                nlgen.visitLabel(PeepholeOptimizer3.PEEPHOLE_FLAG_LABEL)
              // Set the line number to thisInstrUID, for use with error handling.
            nlgen.markLineNumber(thisInstrUID)
            val mripper = new MethodRipper(bestEvalMethod, instr, nlgen, this, thisInstrUID)
            mripper.writeTransformedBytecode()
            // Set the line number to the parent, since any errors that occur
            // during the type conversion are the parent instruction's responsibility
            nlgen.markLineNumber(parentInstrUID)
            nlgen.generateConversion(bestEvalMethod.getReturnType, retTypeWanted, parentInstr, argIndex)
          }
      }
    }
    def generateOldStyleCall(instr: Instruction, retTypeWanted: Class[_], parentInstrUID: Int,
                             parentInstr: Instruction, argIndex: Int) {
      keepAndLoadInstruction(instr, curInstructionUID)
      nlgen.loadContext()
      instr match {
        case _: Reporter =>
          nlgen.visitMethodInsn(INVOKEVIRTUAL, Type.getInternalName(instr.getClass),
            "report", "(Lorg/nlogo/nvm/Context;)Ljava/lang/Object;", false)
          nlgen.markLineNumber(parentInstrUID)
          nlgen.generateConversion(classOf[Object], retTypeWanted, parentInstr, argIndex)
        case _: Command =>
          nlgen.visitMethodInsn(INVOKEVIRTUAL, Type.getInternalName(instr.getClass),
            "perform", "(Lorg/nlogo/nvm/Context;)V", false)
      }
      // now, we want to recursively try to generate all args[] of instr
      instr.args = instr.args.map(recurse(_))
    }
    /**
     * We need the "init()" method of our GeneratedInstruction to call the init() method of the
     * original instruction, so that all of the children on the tree get initialized to the right
     * Workspace.  This is necessary, unless we get to the point where we are doing total bytecode
     * inlining of everything.  (Remember that any Instructions that are called "old-style" with
     * report() need to be inited too...) ~Forrest (3/12/2006)
     */
    def generateInitMethod() {
      val mv = cw.visitMethod(ACC_PUBLIC, "init", "(Lorg/nlogo/nvm/Workspace;)V", null, null)
      mv.visitCode()
      // first invoke super.init()
      mv.visitVarInsn(ALOAD, 0)
      mv.visitVarInsn(ALOAD, 1)
      mv.visitMethodInsn(INVOKESPECIAL, superClassFullName, "init", "(Lorg/nlogo/nvm/Workspace;)V", false)
      // push the original instruction onto the stack...
      val fieldName = Generator.KEPT_INSTRUCTION_PREFIX + "1"
      val descriptor = keptThingsTypes.get(fieldName).getDescriptor
      mv.visitVarInsn(ALOAD, 0)
      mv.visitFieldInsn(GETFIELD, fullClassName, fieldName, descriptor)
      // and call its init() method
      // String cName = Type.getInternalName(original.getClass())
      mv.visitVarInsn(ALOAD, 1)
      mv.visitMethodInsn(INVOKEVIRTUAL, "org/nlogo/nvm/Instruction", "init", "(Lorg/nlogo/nvm/Workspace;)V", false)
      mv.visitInsn(RETURN)
      mv.visitMaxs(0, 0)
      mv.visitEnd()
    }
    def finish(): A = {
      cw.visitEnd()
      val bytecode = cw.toByteArray
      val cName = fullClassName.replace('/', '.')
      val result = loader.loadBytecodeClass(cName, bytecode).newInstance.asInstanceOf[A]
      setAllKeptFields(result)
      result.args   = original.args
      result.copyMetadataFrom(original)

      // disassembly is stored as a thunk, so it's not generated unless used
      result.disassembly = new DisassemblyThunk(bytecode)
      if (debugEndOfMethodLabel.getOffset > Generator.METHOD_SIZE_WARNING_THRESHOLD) {
        System.err.println
        System.err.println("WARNING: method size=" + debugEndOfMethodLabel.getOffset +
          " for " + fullClassName)
      }
      // for debugging, we could dump the created bytecode to a file...
      if (Generator.DEBUG_DUMP_CLASS_FILES) {
        new java.io.File("tmp").mkdir()
        new java.io.File("tmp/Generator").mkdir()
        val fout = new java.io.FileOutputStream("tmp/Generator/" + className + ".class")
        fout.write(bytecode)
        fout.close()
      }
      result
    }
    val keptThings = new java.util.HashMap[String, Object]
    val keptThingsTypes = new java.util.HashMap[String, Type]
    val keptThingsAccessCodes = new java.util.HashMap[String, java.lang.Integer]
    /**
     * returns the field name that the object will be stored in
     */
    def keepInstruction(obj: Instruction, instrUID: Int): String =
      keepInstructionWithType(obj, instrUID, Type.getType(obj.getClass))

    def keepInstructionWithType(obj: Instruction, instrUID: Int, keptType: Type): String = {
      val fieldName = Generator.KEPT_INSTRUCTION_PREFIX + instrUID
      keep(fieldName, obj, keptType, ACC_PUBLIC)
      fieldName
    }

    def loadInstruction(instrUID: Int): Unit = {
      val fieldName = Generator.KEPT_INSTRUCTION_PREFIX + instrUID
      loadKept(fieldName)
    }
    /**
     * Convenience method, for when you want to keep an object, and only load it once.  If you want
     * to be able to load it multiple times, then you should first call "keep" and save the index
     * that is returned.  Then call loadKept(index) whenever you want to load the object.
     */
    def keepAndLoadInstruction(obj: Instruction, instrUID: Int) =
      loadKept(keepInstruction(obj, instrUID))

    def keepAndLoadReporter(obj: Instruction, instrUID: Int) =
      loadKept(keepInstructionWithType(obj, instrUID, Type.getType(classOf[Reporter])))

    def keep(fieldName: String, obj: Object, tpe: Type, accessCode: Int) {
      keptThings.put(fieldName, obj)
      keptThingsTypes.put(fieldName, tpe)
      keptThingsAccessCodes.put(fieldName, Int.box(accessCode))
    }

    def loadKept(fieldName: String): Unit = {
      val descriptor = keptThingsTypes.get(fieldName).getDescriptor
      nlgen.visitVarInsn(ALOAD, 0)
      nlgen.visitFieldInsn(GETFIELD, fullClassName, fieldName, descriptor)
    }

    def remapFieldName(originalName: String, instrUID: Int) =
      "kept" + instrUID + "_" + originalName
    def translateGetField(origFieldName: String, instrUID: Int, obj: Object, tpe: Type, accessCode: Int) {
      // Note: The POPs are generated to cancel out the ALOAD 0 which preceded the GETFIELD
      // instruction.  These ALOAD/POP pairs will then be swept away by a peep-hole optimizer that
      // streamlines the bytecode.
      if (obj.isInstanceOf[String] ||
        List(Type.BOOLEAN_TYPE, Type.CHAR_TYPE, Type.SHORT_TYPE, Type.BYTE_TYPE, Type.DOUBLE_TYPE,
          Type.INT_TYPE, Type.FLOAT_TYPE, Type.LONG_TYPE).contains(tpe)) {
        nlgen.visitInsn(POP)
        nlgen.push(obj)
      } else {
        val fieldName = remapFieldName(origFieldName, instrUID)
        keep(fieldName, obj, tpe, accessCode)
        nlgen.visitFieldInsn(GETFIELD, fullClassName, fieldName, tpe.getDescriptor)
      }
    }
    def translateGetStatic(origFieldName: String, instrUID: Int, obj: Object, tpe: Type, accessCode: Int) {
      val fieldName = remapFieldName(origFieldName, instrUID)
      keep(fieldName, obj, tpe, accessCode)
      nlgen.visitFieldInsn(GETSTATIC, fullClassName, fieldName, tpe.getDescriptor)
    }
    def translatePutStatic(origFieldName: String, instrUID: Int, descriptor: String) {
      nlgen.visitFieldInsn(PUTSTATIC, fullClassName, remapFieldName(origFieldName, instrUID),
        descriptor)
    }
    def generateKeptFields() {
      import collection.JavaConverters._
      for (fieldName <- keptThings.keySet.asScala) {
        val descriptor = keptThingsTypes.get(fieldName).getDescriptor
        var accessCode = keptThingsAccessCodes.get(fieldName).intValue
        // remove "final" flag, so we can set the fields after construction.
        accessCode &= ~ACC_FINAL
        cw.visitField(accessCode, fieldName, descriptor, null, null).visitEnd()
      }
    }
    def setAllKeptFields(resultInstr: Instruction) {
      import collection.JavaConverters._
      for (fieldName <- keptThings.keySet.asScala) {
        val f = resultInstr.getClass.getDeclaredField(fieldName)
        f.setAccessible(true)
        f.set(resultInstr, keptThings.get(fieldName))
      }
    }
    /**
     * This is a kind of a hack, to create the synthetic method that javac creates to deal with code
     * like "Turtle.class" ~Forrest (7/16/2006)
     */
    var staticClassMethodAlreadyGenerated = false
    def generateStaticClassMethod(methodName: String) {
      // we don't want to generate it twice!
      if (staticClassMethodAlreadyGenerated) return
      staticClassMethodAlreadyGenerated = true
      val mv = cw.visitMethod(ACC_STATIC + ACC_SYNTHETIC, "class$",
        "(Ljava/lang/String;)Ljava/lang/Class;", null, null)
      mv.visitCode()
      val (l0, l1, l2) = (new Label, new Label, new Label)
      mv.visitTryCatchBlock(l0, l1, l2, "java/lang/ClassNotFoundException")
      mv.visitLabel(l0)
      mv.visitVarInsn(ALOAD, 0)
      mv.visitMethodInsn(INVOKESTATIC, "java/lang/Class", "forName",
        "(Ljava/lang/String;)Ljava/lang/Class;", false)
      mv.visitLabel(l1)
      mv.visitInsn(ARETURN)
      mv.visitLabel(l2)
      mv.visitVarInsn(ASTORE, 1)
      mv.visitTypeInsn(NEW, "java/lang/NoClassDefFoundError")
      mv.visitInsn(DUP)
      mv.visitMethodInsn(INVOKESPECIAL, "java/lang/NoClassDefFoundError", "<init>", "()V", false)
      mv.visitVarInsn(ALOAD, 1)
      mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/NoClassDefFoundError", "initCause",
        "(Ljava/lang/Throwable;)Ljava/lang/Throwable;", false)
      mv.visitInsn(ATHROW)
      mv.visitMaxs(2, 2)
      mv.visitEnd()
    }
  }
}
