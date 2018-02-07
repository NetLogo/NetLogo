// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compile

import org.nlogo.core.{ Dialect, DummyCompilationEnvironment, Program }
import org.nlogo.api.{ AgentVariableNumbers, DummyExtensionManager, Version, NetLogoThreeDDialect, NetLogoLegacyDialect }
import org.nlogo.nvm.Procedure

import org.nlogo.util.{ ArityIndependent, TaggedFunSuite }

trait GeneratorHelper {
  def dialect: Dialect
  val compiler = new Compiler(dialect)
  val program = Program.fromDialect(dialect).copy(userGlobals = Seq("GLOB1"))
  def condense(disassembly: String) = disassembly.split("\n").map(_.trim).mkString("\n")

  def compile(source: String, preamble: String) = compiler.compileMoreCode(
    "to foo " + preamble + source + "\nend", None,
    program, new scala.collection.immutable.ListMap[String, Procedure](),
    new DummyExtensionManager, new DummyCompilationEnvironment()).head.code.head

  def disassembleCommand(source: String): String =
    condense(compile(source, "").disassembly.value)
  def disassembleReporter(source: String): String =
    condense(compile(source, "__ignore ").args.head.disassembly.value)
  def stripLineNumbers(disassembly: String) =
    disassembly.split("\n").filter(!_.matches("L\\d?")).toList
}

class TestGenerator extends TaggedFunSuite(ArityIndependent) with GeneratorHelper {
  def dialect = NetLogoLegacyDialect

  if(Version.useGenerator) {
    test("no arg reporter") {
      // the result is being passed to __ignore which expects an Object so we should
      // generate code that retrieves a stored Double
      assert(disassembleReporter("0").containsSlice(
        "GETFIELD org/nlogo/prim/_asm_procedurefoo_constdouble_0.kept1_value : Ljava/lang/Double"))
      // the results are being passed to _plus which wants doubles, so we should
      // inline the primitive constants
      assert(disassembleReporter("0 + timer").matches("(?s).*DCONST_0.*"))
      assert(disassembleReporter("1 + timer").matches("(?s).*DCONST_1.*"))
    }

    // We're testing here that since _if.offset is known at compile time, for
    // "ip += offset" in _if.perform, we generate "ICONST_2 IADD" rather than
    // "GETFIELD IADD"
    test("offset compiled to constant") {
      assertResult("""|ICONST_4
        |FRAME FULL [org/nlogo/prim/_asm_procedurefoo_if_0 org/nlogo/nvm/Context I] [org/nlogo/nvm/Context I]
        |PUTFIELD org/nlogo/nvm/Context.ip : I
        |RETURN""".stripMargin)(
          stripLineNumbers(disassembleCommand("if true [ __ignore 1 __ignore 2 __ignore 3 ]"))
            .takeRight(4).mkString("\n"))
    }

    // make sure we generate good code for nobody checking
    test("equalsNobody1") {
      assert(!disassembleReporter("glob1 = nobody")
        .matches("(?s).*recursivelyEqual.*"))
    }

    test("equalsNobody2") {
      assert(!disassembleReporter("nobody = glob1")
        .matches("(?s).*recursivelyEqual.*"))
    }

    test("equalsNobody3") {
      assert(!disassembleReporter("glob1 != nobody")
        .matches("(?s).*recursivelyEqual.*"))
    }

    test("equalsNobody4") {
      assert(!disassembleReporter("nobody != glob1")
        .matches("(?s).*recursivelyEqual.*"))
    }

    // make sure we don't choke when a rejiggered reporter has a nonrejiggered argument
    test("nonRejiggeredArgument") {
      // __boom is unrejiggered - ST 2/6/09
      assert(disassembleReporter("2 * __boom")
        .matches("(?s).*" +
          "LDC 2.0.*" +
          "GETFIELD org/nlogo/prim/_asm_procedurefoo_mult_0.keptinstr3 : Lorg/nlogo/prim/etc/_boom;.*" +
          "INVOKEVIRTUAL org/nlogo/prim/etc/_boom.report \\(Lorg/nlogo/nvm/Context;\\)Ljava/lang/Object;.*"))
    }
  }
}

abstract class ArityDependentTests(is3D: Boolean) extends TaggedFunSuite(ArityIndependent) with GeneratorHelper {
  val plabelVN: Int
  val xcorVN: Int
  def dialect = if (is3D) NetLogoThreeDDialect else NetLogoLegacyDialect
  val generatorName: String = if (is3D) "3D" else "2D"

  if (Version.useGenerator) {
    // make sure the generator chooses _constdouble's Double-returning method,
    // not the double-returning one, when the result is to be stored in a variable
    test(s"useBoxedConstant ($generatorName)") {
      val actual = disassembleCommand("set plabel 1")
      assert(actual.matches(s"(?s).*ICONST_${plabelVN}\n" +
        "ALOAD 2\n" +
        "INVOKEVIRTUAL org/nlogo/agent/Agent.setPatchVariable.*"))
    }

    // make sure we generate good code for comparison of a variable known to be numeric
    test(s"xcorEqualsNumber ($generatorName)") {
      assertResult(List(
        // context.agent.getTurtleVariableDouble
        "L0","ALOAD 1",
        "GETFIELD org/nlogo/nvm/Context.agent : Lorg/nlogo/agent/Agent;",
        "CHECKCAST org/nlogo/agent/Turtle",
        s"ICONST_${xcorVN}",
        "INVOKEVIRTUAL org/nlogo/agent/Turtle.getTurtleVariableDouble (I)D",
        // ... = 0
        "L1","DCONST_0",
        "L2","DSTORE 4",
        "DSTORE 2",
        "DLOAD 2",
        "DLOAD 4",
        "DCMPL",
        "IFNE L3",
        // return ... ? Boolean.TRUE : Boolean.FALSE
        "ICONST_1",
        "GOTO L4",
        "L3", "FRAME APPEND [D D]", "ICONST_0",
        "L4","FRAME SAME1 I", "IFEQ L5",
        "GETSTATIC java/lang/Boolean.TRUE : Ljava/lang/Boolean;",
        "GOTO L6",
        "L5","FRAME SAME", "GETSTATIC java/lang/Boolean.FALSE : Ljava/lang/Boolean;",
        "L6","FRAME SAME1 java/lang/Boolean", "ARETURN"
      ).mkString("\n"))(disassembleReporter("xcor = 0"))
    }
  }
}

class TestGeneratorTwoD extends ArityDependentTests(is3D = false) {
  val plabelVN = AgentVariableNumbers.VAR_PLABEL
  val xcorVN = AgentVariableNumbers.VAR_XCOR
}

class TestGeneratorThreeD extends ArityDependentTests(is3D = true) {
  val plabelVN = AgentVariableNumbers.VAR_PLABEL3D
  val xcorVN = AgentVariableNumbers.VAR_XCOR3D
}
