// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless
package lang
package misc

// This stuff used to be in test/commands/ProfilerExtension.txt, but these tests tend
// to intermittently fail if your computer is slow because it's doing something else,
// so I'm making them separate (and possibly we should just get rid of the timing-based
// ones entirely, if we can't make them more robust) - ST 4/27/10

// This would be nicer looking if it were done using Josh's model testing DSL.  (At the time I made
// this, the DSL didn't support catching runtime errors, but now it does.) - ST 5/4/10

import org.nlogo.headless.test.RuntimeError
import org.nlogo.util.SlowTest
import org.nlogo.api
import org.nlogo.core.Model

class TestProfiler extends FixtureSuite  {

  // change to true temporarily to enable timing sensitive tests.  disabled by default
  // since they tend to fail intermittently if CPU load is high. - ST 6/10/10
  val timingSensitiveOK = false

  val useGenerator = api.Version.useGenerator
  if(!useGenerator)
    test("no generator", SlowTest.Tag) { implicit fixture =>
      import fixture._
      declare("extensions [profilo]")
      testCommand("profilo:start", result = RuntimeError(
        "Extension exception: The profiler extension requires the NetLogo bytecode " +
          "generator, which is currently turned off. See the org.nlogo.noGenerator " +
          "property."))
    }
  if(useGenerator)
    test("basics", SlowTest.Tag) { implicit fixture =>
      import fixture._
      declare(
        "extensions [profilo]\n" +
        "to dosomething crt 5 [ rt random 360 fd random 30 ] end\n" +
        "to-report saysomething report count turtles end\n" +
        "to wiggle rt random 360 fd 1 end\n" +
        "to somethingelse ask turtles [ set color saysomething wiggle ] end\n" +
        "to profiler-test profilo:start dosomething repeat 50 [ somethingelse ] end\n" +
        "to reload-profiler __reload-extensions end")
      testCommand("profiler-test")
      testReporter("profilo:calls \"somethingelse\"", "50")
      testReporter("profilo:calls \"nonexistent\"", "0")
      testReporter("profilo:calls \"wiggle\"", "250")
      testReporter("profilo:calls \"dosomething\"", "1")
      testCommand("somethingelse")
      testCommand("somethingelse")
      testReporter("profilo:calls \"somethingelse\"", "52")
    }
  if(useGenerator)
    test("stop", SlowTest.Tag) { implicit fixture =>
      import fixture._
      declare(
        "extensions [profilo]\n" +
        "to foo end")
      testCommand("profilo:start")
      testReporter("profilo:calls \"foo\"", "0")
      testCommand("foo")
      testCommand("profilo:stop")
      testReporter("profilo:calls \"foo\"", "1")
      testCommand("foo")
      testReporter("profilo:calls \"foo\"", "1")
    }
  if(useGenerator && timingSensitiveOK)
    // uses precision primitive to not be too picky about exact times
    test("wait", SlowTest.Tag) { implicit fixture =>
      import fixture._
      declare(
        "extensions [profilo]\n" +
        "to test1 wait 1 end\n" +
        "to test2 wait 0.1 end\n" +
        "to test3 wait 0.01 end")
      testCommand("profilo:reset")
      testCommand("profilo:start")
      testCommand("test1")
      testCommand("profilo:stop")
      testReporter("profilo:exclusive-time \"test1\" >= 1000", "true")
      testReporter("precision profilo:exclusive-time \"test1\" -3", "1000")
      testReporter("profilo:inclusive-time \"test1\" >= 1000", "true")
      testReporter("precision profilo:inclusive-time \"test1\" -3", "1000")
      testCommand("profilo:reset")
      testCommand("profilo:start")
      testCommand("test2")
      testCommand("profilo:stop")
      testReporter("profilo:exclusive-time \"test2\" >= 100", "true")
      testReporter("precision profilo:exclusive-time \"test2\" -2", "100")
      testReporter("profilo:inclusive-time \"test2\" >= 100", "true")
      testReporter("precision profilo:inclusive-time \"test2\" -2", "100")
      testCommand("profilo:reset")
      testCommand("profilo:start")
      testCommand("test3")
      testCommand("profilo:stop")
      testReporter("profilo:exclusive-time \"test3\" >= 10", "true")
      testReporter("precision profilo:exclusive-time \"test3\" -1", "10")
      testReporter("profilo:inclusive-time \"test3\" >= 10", "true")
      testReporter("precision profilo:inclusive-time \"test3\" -1", "10")
    }
  if(useGenerator && timingSensitiveOK)
    test("ask turtles", SlowTest.Tag) { implicit fixture =>
      import fixture._
      declare(
        "extensions [profilo]\n" +
        "globals [glob1]\n" +
        "to test1 ask turtles [ test2 ] end\n" +
        "to test2 wait 0.01 end")
      testCommand("crt 10")
      testCommand("profilo:reset")
      testCommand("profilo:start")
      testCommand("test1")
      testCommand("profilo:stop")
      testReporter("profilo:inclusive-time \"test2\" = profilo:exclusive-time \"test2\"", "true")
      testReporter("profilo:inclusive-time \"test1\" >= profilo:inclusive-time \"test2\"", "true")
      testReporter("profilo:exclusive-time \"test1\" < profilo:exclusive-time \"test2\"", "true")
      testReporter("profilo:exclusive-time \"test1\" <= 5", "true")
      testCommand("set glob1 profilo:exclusive-time \"test1\" + profilo:exclusive-time \"test2\"")
      testReporter("precision (glob1 - profilo:inclusive-time \"test1\") 8", "0")
    }
  if(useGenerator && timingSensitiveOK)
    test("nested asks", SlowTest.Tag) { implicit fixture =>
      import fixture._
      declare(
        "extensions [profilo]\n" +
        "globals [glob1 glob2 glob3]\n" +
        "to go ask turtles [ go-turtles1 ] ask patches [ go-patches ] end\n" +
        "to go-turtles1 wait 0.0001 go-turtles2 end\n" +
        "to go-turtles2 ask turtles with [true] [ go-turtles3 ] end\n" +
        "to go-turtles3 wait 0.0001 end\n" +
        "to go-patches wait 0.0001 end")
      testCommand("crt 10")
      testCommand("profilo:reset")
      testCommand("profilo:start")
      testCommand("repeat 10 [ go ]")
      testCommand("profilo:stop")
      testReporter("profilo:calls \"go-patches\" = count patches * 10", "true")
      testReporter("profilo:calls \"go-turtles1\" = count turtles * 10", "true")
      testReporter("profilo:calls \"go-turtles2\" = count turtles * 10", "true")
      testReporter("profilo:calls \"go-turtles3\" = count turtles * count turtles * 10", "true")
      testReporter("profilo:exclusive-time \"go-patches\" = profilo:inclusive-time \"go-patches\"", "true")
      testReporter("profilo:exclusive-time \"go-turtles3\" = profilo:inclusive-time \"go-turtles3\"", "true")
      testCommand("set glob3 profilo:exclusive-time \"go-turtles3\"")
      testCommand("set glob2 profilo:exclusive-time \"go-turtles2\"")
      testCommand("set glob1 profilo:exclusive-time \"go-turtles1\"")
      testReporter("precision (glob2 + glob3 - profilo:inclusive-time \"go-turtles2\") 13", "0")
      testReporter("precision (glob1 + glob2 + glob3 - profilo:inclusive-time \"go-turtles1\") 13", "0")
    }
  if(useGenerator && timingSensitiveOK)
    test("reporter procedures", SlowTest.Tag) { implicit fixture =>
      import fixture._
      declare(
        "extensions [profilo]\n" +
        "to-report some-value wait 0.1 report random 10 end")
      testCommand("crt 10")
      testCommand("profilo:reset")
      testCommand("profilo:start")
      testCommand("ask turtles [ print some-value ]")
      testCommand("profilo:stop")
      testReporter("profilo:calls \"some-value\"", "10")
      testReporter("profilo:exclusive-time \"some-value\" = profilo:inclusive-time \"some-value\"", "true")
      testReporter("precision profilo:exclusive-time \"some-value\" -3", "1000")
    }

  // kludginess ahead. this isn't really a test of the profiler extension per se.  the other
  // isReporter tests are in TestCompiler.  but this test case involves an extension primitive.  it
  // doesn't matter which extension, so we use profiler.  ideally we'd have a test scaffold that
  // lets us test extensions stuff without having an actual extension jar in hand.  but we don't,
  // and we don't want anything in test-fast or test-medium to depend on submodules like models and
  // extensions, so we put it here because it's a SlowTest.Tag - ST 1/19/12
  test("isReporter on extension prims", SlowTest.Tag) { implicit fixture =>
    import fixture._
    declare(Model(code = "extensions [profilo]"))
    assertResult(false) { workspace.isReporter("profilo:start") }
    assertResult(true) { workspace.isReporter("profilo:report") }
  }

}
