package org.nlogo.properties

import org.scalatest.FunSuite
//import org.nlogo.api.CompilerServices
import org.nlogo.nvm.PresentationCompilerInterface
import org.nlogo.api.{ DummyCompilerServices, NetLogoLegacyDialect, NetLogoThreeDDialect, Version }
import org.nlogo.core.{ Femto }

class CodeEditorTests extends FunSuite {
  test("check the field if the total number of runs is below 2^31"){
    val compiler = Femto.get[PresentationCompilerInterface]("org.nlogo.compile.Compiler",
      if (Version.is3D) NetLogoThreeDDialect else NetLogoLegacyDialect)
    val codeDefault = CodeEditor("Vary variable",null)
    val workspace = new DummyCompilerServices {
      override def readFromString(s: String): AnyRef =
        compiler.readFromString(s)
    }
    codeDefault.set(
      // Number of total runs: (12 ^ 5) * 1 = 248,832
      // 2,147,483,648 > 248,832
      """["random-seed" 1 2 3 4 5 6 7 8 9 10 11 12]
        |["random-seed" 1 2 3 4 5 6 7 8 9 10 11 12]
        |["random-seed" 1 2 3 4 5 6 7 8 9 10 11 12]
        |["random-seed" 1 2 3 4 5 6 7 8 9 10 11 12]
        |["random-seed" 1 2 3 4 5 6 7 8 9 10 11 12]
      """.stripMargin)
    assert(codeDefault.fieldCheck(workspace, 1.asInstanceOf[AnyRef]))
    codeDefault.set(
      // Number of total runs: (12 ^ 5) * 10000 = 2488320000
      // 2,147,483,648 < 2,488,320,000
      """["random-seed" 1 2 3 4 5 6 7 8 9 10 11 12]
        |["random-seed" 1 2 3 4 5 6 7 8 9 10 11 12]
        |["random-seed" 1 2 3 4 5 6 7 8 9 10 11 12]
        |["random-seed" 1 2 3 4 5 6 7 8 9 10 11 12]
        |["random-seed" 1 2 3 4 5 6 7 8 9 10 11 12]
      """.stripMargin)
    assert(!codeDefault.fieldCheck(workspace, 10000.asInstanceOf[AnyRef]))
    codeDefault.set(
      // Number of total runs: (12 ^ 8) * 1 = 429,981,696
      // 2,147,483,648 > 429,981,696
      """["random-seed" 1 2 3 4 5 6 7 8 9 10 11 12]
        |["random-seed" 1 2 3 4 5 6 7 8 9 10 11 12]
        |["random-seed" 1 2 3 4 5 6 7 8 9 10 11 12]
        |["random-seed" 1 2 3 4 5 6 7 8 9 10 11 12]
        |["random-seed" 1 2 3 4 5 6 7 8 9 10 11 12]
        |["random-seed" 1 2 3 4 5 6 7 8 9 10 11 12]
        |["random-seed" 1 2 3 4 5 6 7 8 9 10 11 12]
        |["random-seed" 1 2 3 4 5 6 7 8 9 10 11 12]
      """.stripMargin)
    assert(codeDefault.fieldCheck(workspace, 1.asInstanceOf[AnyRef]))
      codeDefault.set(
      // Number of total runs: (12 ^ 9) * 1 = 5,159,780,352
      // 2,147,483,648 < 5,159,780,352
      """["random-seed" 1 2 3 4 5 6 7 8 9 10 11 12]
        |["random-seed" 1 2 3 4 5 6 7 8 9 10 11 12]
        |["random-seed" 1 2 3 4 5 6 7 8 9 10 11 12]
        |["random-seed" 1 2 3 4 5 6 7 8 9 10 11 12]
        |["random-seed" 1 2 3 4 5 6 7 8 9 10 11 12]
        |["random-seed" 1 2 3 4 5 6 7 8 9 10 11 12]
        |["random-seed" 1 2 3 4 5 6 7 8 9 10 11 12]
        |["random-seed" 1 2 3 4 5 6 7 8 9 10 11 12]
        |["random-seed" 1 2 3 4 5 6 7 8 9 10 11 12]
      """.stripMargin)
    assert(!codeDefault.fieldCheck(workspace, 1.asInstanceOf[AnyRef]))
      codeDefault.set(
      // Number of total runs: (12 ^ 8) * 10 = 4,299,816,960
      // 2,147,483,648 < 4,299,816,960
      """["random-seed" 1 2 3 4 5 6 7 8 9 10 11 12]
        |["random-seed" 1 2 3 4 5 6 7 8 9 10 11 12]
        |["random-seed" 1 2 3 4 5 6 7 8 9 10 11 12]
        |["random-seed" 1 2 3 4 5 6 7 8 9 10 11 12]
        |["random-seed" 1 2 3 4 5 6 7 8 9 10 11 12]
        |["random-seed" 1 2 3 4 5 6 7 8 9 10 11 12]
        |["random-seed" 1 2 3 4 5 6 7 8 9 10 11 12]
        |["random-seed" 1 2 3 4 5 6 7 8 9 10 11 12]
      """.stripMargin)
    assert(!codeDefault.fieldCheck(workspace, 10.asInstanceOf[AnyRef]))
  }
}
