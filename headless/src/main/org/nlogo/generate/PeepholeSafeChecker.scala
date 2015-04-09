// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.generate

import java.lang.reflect.Method
import org.objectweb.asm.{ ClassReader, MethodVisitor, Type }
import org.objectweb.asm.Opcodes.{ ALOAD, DLOAD, FLOAD, ILOAD, LLOAD }

/**
 * A report_* or perform_* method of a _prim is Peephole-Safe
 * if it references each argument at most once.
 * A method with no arguments is trivially PeepholeSafe.
 * This class maintains a table of the Peephole-Safe status
 * of each report_* and perform_* method, by checking them
 * the first time a prim's class is used for inlining.
 *
 *  Examples:
 *  A _prim with this report method is PeepholeSafe:
 *  double report_1(Context context, double arg0, double arg1)
 *  {
 *       return arg0 + arg1;
 *  }
 *  A _prim with this report method is NOT PeepholeSafe:
 *  double report_1(Context context, double arg0, double arg1)
 *  {
 *      if (arg1 != 0.0)
 *      {
 *              return arg0 / arg1;
 *      ...
 *  }
 *  PeepholeSafety actually refers to safety with regard to just one
 *  type of peephole optimization, the redundant STORE/LOAD pattern,
 *  which is found in {@link PeepholeOptimizer3}.
 *  The other PeepholeOptimizers will be run regardless.
 */

class PeepholeSafeChecker(profilingEnabled: Boolean = false) {
  // we have to synchronize because we could be compiling stuff on multiple threads
  def isSafe(m: Method): Boolean = synchronized {
    val hashKey = getHashKey(m)
    if (!methodSafeTable.contains(hashKey))
      processClass(m.getDeclaringClass)
    methodSafeTable(hashKey)
  }
  private val methodSafeTable = new collection.mutable.HashMap[String, Boolean]
  private def getHashKey(m: Method): String =
    m.getDeclaringClass.getName + "." + m.getName
  private def processClass(c: Class[_]) {
    val reader = PrimitiveCache.getClassReader(c)
    for (m <- BytecodeUtils.getMethods(c, profilingEnabled))
      reader.accept(new MethodExtractorClassAdapter(m), ClassReader.SKIP_DEBUG)
  }
  private class MethodExtractorClassAdapter(method: Method) extends EmptyClassVisitor {
    override def visitMethod(arg0: Int, name: String, descriptor: String, signature: String, exceptions: Array[String]): MethodVisitor =
      if (name == method.getName && descriptor == Type.getMethodDescriptor(method))
        new PeepholeSafeMethodChecker(method)
      else new EmptyMethodVisitor
  }
  private class PeepholeSafeMethodChecker(method: Method) extends EmptyMethodVisitor {
    private var thisMethodFailedTest = false
    val paramLocalsCount = Type.getArgumentTypes(method).map(_.getSize).sum
    val alreadyLoaded = Array.fill(paramLocalsCount)(false)
    override def visitVarInsn(opcode: Int, variable: Int) {
      val check =
        !thisMethodFailedTest &&
          List(ILOAD, DLOAD, LLOAD, FLOAD, ALOAD).contains(opcode) &&
          variable >= 2 && variable <= paramLocalsCount
      if (check) {
        if (alreadyLoaded(variable - 2)) {
          methodSafeTable(getHashKey(method)) = false
          thisMethodFailedTest = true
        } else alreadyLoaded(variable - 2) = true
      }
    }
    override def visitEnd() {
      if (!thisMethodFailedTest)
        methodSafeTable(getHashKey(method)) = true
    }
  }
}
