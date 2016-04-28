// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.generate

import org.scalatest.FunSuite
import org.objectweb.asm.ClassReader

class TestStrictFP extends FunSuite {

  test("allMethodsStrictFP") {
    // We need to maintain this list of abstract classes - probably we can generate it by running
    // some sort of "grep" for abstract classes in the "src" folder...  I guess it really could be
    // any classes -- not just abstract ones.  but the abstract ones are the ones we're worried
    // about checking.  ~Forrest (12/6/2006)
    val classesToCheck = List("org/nlogo/agent/Agent.class",
                              "org/nlogo/agent/AgentSet.class")
    for(c <- classesToCheck)
      assert("" === badMethods(c).mkString("\n"))
  }

  def badMethods(className: String): List[String] = {
    val reader = {
      val in = Thread.currentThread.getContextClassLoader.getResourceAsStream(className)
      val reader = new ClassReader(in)
      in.close()
      reader
    }
    val visitor = new StrictFPVisitor
    reader.accept(visitor, ClassReader.SKIP_DEBUG)
    visitor.badMethods.toList
  }

  class StrictFPVisitor extends EmptyClassVisitor {
    import org.objectweb.asm.Opcodes.{ACC_ABSTRACT, ACC_STRICT}
    val badMethods = collection.mutable.ListBuffer[String]()
    override def visitMethod(arg0: Int, name: String, descriptor: String, signature: String, exceptions: Array[String]) = {
      if((arg0 & ACC_ABSTRACT) != 0 && (arg0 & ACC_STRICT) != 0)
        badMethods += name
      new EmptyMethodVisitor
    }
  }

}
