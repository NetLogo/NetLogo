// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless
package misc

import java.io.{ ByteArrayOutputStream, PrintStream }

import org.nlogo.api.{ LabExporterVersion, Version }
import org.nlogo.util.AnyFunSuiteEx

// headless.Main.main() is our main entry point for command-line users.
// so it should have at least a little basic smoke testing.

class TestMain extends AnyFunSuiteEx {

  def capture(body: => Unit): (String, String) = {
    val out = new ByteArrayOutputStream
    val err = new ByteArrayOutputStream

    val oldOut = System.out
    val oldErr = System.err

    val newOut = new PrintStream(out)
    val newErr = new PrintStream(err)

    System.setOut(newOut)
    System.setErr(newErr)

    body

    newOut.close()
    newErr.close()

    System.setOut(oldOut)
    System.setErr(oldErr)

    (out.toString, err.toString)
  }

  test("version") {
    val (out, err) = capture {
      Main.main(Array("--version"))
    }
    assertResult("")(err)
    assertResult(s"${Version.version}\r?\n".r.matches(out))
  }

  test("bad arg") {
    val (out, err) = capture {
      Main.main(Array("--foobarbaz"))
    }
    assertResult("")(out)
    assertResult("unknown argument: --foobarbaz\r?\n".r.matches(err))
  }

  test("no args") {
    val (out, err) = capture {
      Main.main(Array())
    }
    assertResult("")(out)
    assertResult("you must specify --model\r?\n".r.matches(err))
  }

  test("missing filename") {
    val (out, err) = capture {
      Main.main(Array("--model"))
    }
    assertResult("")(out)
    assertResult("missing argument after --model\r?\n".r.matches(err))
  }

  test("no experiment specified") {
    val (out, err) = capture {
      Main.main(Array("--model", "foobarbaz.nlogox"))
    }
    assertResult("")(out)
    assertResult("you must specify either --setup-file or --experiment (or both)\r?\n".r.matches(err))
  }

  // duplicating stuff from TestBehaviorSpace, just
  // making sure it also works through Main.main()

  test("successful run") {
    val (out, err) = capture {
      Main.main(Array(
        "--threads", "1",   // avoid out-of-order output
        "--model", "models/test/lab/FireWithExperiments.nlogox",
        "--setup-file", "test/lab/protocols.xml",
        "--experiment", "runNumber",
        "--table", "-"))   // stdout
    }
    val expected =
      """|"BehaviorSpace results (VERSION)","Table version EXPORTER_VERSION"
         |"models/test/lab/FireWithExperiments.nlogox"
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
    assertResult(expected.replaceFirst("VERSION", Version.version)
                         .replaceFirst("EXPORTER_VERSION", LabExporterVersion.version))(
      out.replaceAll(
        """"\d\d/\d\d/\d\d\d\d \d\d:\d\d:\d\d:\d\d\d .\d\d\d\d"\r?\n""",
        "").replace("\r\n", "\n"))
  }

}
