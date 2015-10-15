// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.generate

import org.scalatest.FunSuite
import java.lang.reflect.Method
import org.objectweb.asm.{ ClassReader, Type, MethodVisitor }
import org.nlogo.api.Version
import org.nlogo.nvm.Instruction

trait AllPrimitivesTester extends FunSuite {

  def makeVisitor(m: Method): MethodVisitor
  def filter(c: Class[_]): Boolean = true

  type PrimClass = Class[_ <: Instruction]

  // not all primitives are listed in tokens.txt, because some of them are only used internally so
  // they only have an internal name.  so we have to actually look on disk. - ST 2/12/09
  val primDir = new java.io.File("netlogo-headless/target/classes/org/nlogo/prim")

  if(Version.useGenerator)
    for(c <- allPrimitiveClasses(primDir))
      if(filter(c))
        test(c.getName) {
          processClass(c)
        }

  def allPrimitiveClasses(dir: java.io.File): List[PrimClass] =
    if(dir.getName.startsWith("."))
      Nil
    else
      dir.list().map(new java.io.File(dir, _)).toList.flatMap(fileOrDir =>
        if(fileOrDir.isDirectory())
          allPrimitiveClasses(fileOrDir)
        else if(fileOrDir.getName.endsWith(".class") &&
                !fileOrDir.getName.contains('$'))
          List(file2class(fileOrDir))
        else Nil)

  def file2class(f: java.io.File): PrimClass =
    Class.forName(f.getAbsolutePath
                  .split(java.io.File.separatorChar)
                  .toList
                  .dropWhile(_ != "org")
                  .mkString(".")
                  .replaceAll(".class$", ""))
      .asInstanceOf[PrimClass]

  def processClass(c: PrimClass) {
    val reader = PrimitiveCache.getClassReader(c)
    for(method <- BytecodeUtils.getMethods(c)) {
      val visitor = new EmptyClassVisitor {
        override def visitMethod(arg0: Int, name: String, descriptor: String,
                                 signature: String, exceptions: Array[String]) =
          if(name == method.getName && descriptor == Type.getMethodDescriptor(method))
            makeVisitor(method)
          else new EmptyMethodVisitor
      }
      reader.accept(visitor, ClassReader.SKIP_DEBUG)
    }
  }

}
