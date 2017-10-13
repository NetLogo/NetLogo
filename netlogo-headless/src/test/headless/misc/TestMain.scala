// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless
package misc

import org.scalatest.FunSuite
import org.nlogo.api

// headless.Main.main() is our main entry point for command-line users.
// so it should have at least a little basic smoke testing.

class TestMain extends FunSuite {

  def capture(body: => Unit): (String, String) = {
    val out = new java.io.ByteArrayOutputStream()
    val err = new java.io.ByteArrayOutputStream()
    Console.withOut(out) {
      Console.withErr(err) {
        body
      }
    }
    (out.toString, err.toString)
  }

  test("version") {
    val (out, err) = capture {
      Main.main(Array("--version"))
    }
    assertResult("")(err)
    assertResult(s"${api.TwoDVersion.version}\n")(out)
  }

  test("bad arg") {
    val (out, err) = capture {
      Main.main(Array("--foobarbaz"))
    }
    assertResult("")(out)
    assertResult("unknown argument: --foobarbaz\n")(err)
  }

  test("no args") {
    val (out, err) = capture {
      Main.main(Array())
    }
    assertResult("")(out)
    assertResult("you must specify --model\n")(err)
  }

  test("missing filename") {
    val (out, err) = capture {
      Main.main(Array("--model"))
    }
    assertResult("")(out)
    assertResult("missing argument after --model\n")(err)
  }

  test("no experiment specified") {
    val (out, err) = capture {
      Main.main(Array("--model", "foobarbaz.nlogo"))
    }
    val expected =
      "you must specify either --setup-file or --experiment (or both)\n"
    assertResult("")(out)
    assertResult(expected)(err)
  }

  // duplicating stuff from TestBehaviorSpace, just
  // making sure it also works through Main.main()

  test("successful run") {
    val (out, err) = capture {
      Main.main(Array(
        "--threads", "1",   // avoid out-of-order output
        "--model", "models/test/lab/FireWithExperiments.nlogo",
        "--setup-file", "test/lab/protocols.xml",
        "--experiment", "runNumber",
        "--table", "-"))   // stdout
    }
    val expected =
      """|"BehaviorSpace results (VERSION)"
         |"models/test/lab/FireWithExperiments.nlogo"
         |"runNumber"
         |"min-pxcor","max-pxcor","min-pycor","max-pycor"
         |"-100","100","-100","100"
         |"[run number]","[step]","behaviorspace-run-number"
         |"1","0","1"
         |"1","1","1"
         |"1","2","1"
         |"2","0","2"
         |"2","1","2"
         |"2","2","2"
         |"3","0","3"
         |"3","1","3"
         |"3","2","3"
         |""".stripMargin
    assertResult("")(err)
    // version number varies, date and time varies, compare carefully!
    assertResult(expected.replaceFirst("VERSION", api.TwoDVersion.version))(
      out.replaceAll(
        """"\d\d/\d\d/\d\d\d\d \d\d:\d\d:\d\d:\d\d\d .\d\d\d\d"\n""",
        ""))
  }

}
