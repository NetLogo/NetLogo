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
import org.nlogo.util.SlowTestTag
import org.nlogo.api
import org.nlogo.core.Model

class TestProfiler extends FixtureSuite  {

  // change to true temporarily to enable timing sensitive tests.  disabled by default
  // since they tend to fail intermittently if CPU load is high. - ST 6/10/10
  val timingSensitiveOK = false

  val useGenerator = api.Version.useGenerator
  if(!useGenerator)
    test("no generator", SlowTestTag) { implicit fixture =>
      import fixture._
      declare("extensions [profiler]")
      testCommand("profiler:start", result = RuntimeError(
        "Extension exception: The profiler extension requires the NetLogo bytecode " +
          "generator, which is currently turned off. See the org.nlogo.noGenerator " +
          "property."))
    }
  if(useGenerator)
    test("basics", SlowTestTag) { implicit fixture =>
      import fixture._
      declare(
        "extensions [profiler]\n" +
        "to dosomething crt 5 [ rt random 360 fd random 30 ] end\n" +
        "to-report saysomething report count turtles end\n" +
        "to wiggle rt random 360 fd 1 end\n" +
        "to somethingelse ask turtles [ set color saysomething wiggle ] end\n" +
        "to profiler-test profiler:start dosomething repeat 50 [ somethingelse ] end\n" +
        "to reload-profiler __reload-extensions end")
      testCommand("profiler-test")
      testReporter("profiler:calls \"somethingelse\"", "50")
      testReporter("profiler:calls \"nonexistent\"", "0")
      testReporter("profiler:calls \"wiggle\"", "250")
      testReporter("profiler:calls \"dosomething\"", "1")
      testCommand("somethingelse")
      testCommand("somethingelse")
      testReporter("profiler:calls \"somethingelse\"", "52")
    }
  if(useGenerator)
    test("stop", SlowTestTag) { implicit fixture =>
      import fixture._
      declare(
        "extensions [profiler]\n" +
        "to foo end")
      testCommand("profiler:start")
      testReporter("profiler:calls \"foo\"", "0")
      testCommand("foo")
      testCommand("profiler:stop")
      testReporter("profiler:calls \"foo\"", "1")
      testCommand("foo")
      testReporter("profiler:calls \"foo\"", "1")
    }
  if(useGenerator && timingSensitiveOK)
    // uses precision primitive to not be too picky about exact times
    test("wait", SlowTestTag) { implicit fixture =>
      import fixture._
      declare(
        "extensions [profiler]\n" +
        "to test1 wait 1 end\n" +
        "to test2 wait 0.1 end\n" +
        "to test3 wait 0.01 end")
      testCommand("profiler:reset")
      testCommand("profiler:start")
      testCommand("test1")
      testCommand("profiler:stop")
      testReporter("profiler:exclusive-time \"test1\" >= 1000", "true")
      testReporter("precision profiler:exclusive-time \"test1\" -3", "1000")
      testReporter("profiler:inclusive-time \"test1\" >= 1000", "true")
      testReporter("precision profiler:inclusive-time \"test1\" -3", "1000")
      testCommand("profiler:reset")
      testCommand("profiler:start")
      testCommand("test2")
      testCommand("profiler:stop")
      testReporter("profiler:exclusive-time \"test2\" >= 100", "true")
      testReporter("precision profiler:exclusive-time \"test2\" -2", "100")
      testReporter("profiler:inclusive-time \"test2\" >= 100", "true")
      testReporter("precision profiler:inclusive-time \"test2\" -2", "100")
      testCommand("profiler:reset")
      testCommand("profiler:start")
      testCommand("test3")
      testCommand("profiler:stop")
      testReporter("profiler:exclusive-time \"test3\" >= 10", "true")
      testReporter("precision profiler:exclusive-time \"test3\" -1", "10")
      testReporter("profiler:inclusive-time \"test3\" >= 10", "true")
      testReporter("precision profiler:inclusive-time \"test3\" -1", "10")
    }
  if(useGenerator && timingSensitiveOK)
    test("ask turtles", SlowTestTag) { implicit fixture =>
      import fixture._
      declare(
        "extensions [profiler]\n" +
        "globals [glob1]\n" +
        "to test1 ask turtles [ test2 ] end\n" +
        "to test2 wait 0.01 end")
      testCommand("crt 10")
      testCommand("profiler:reset")
      testCommand("profiler:start")
      testCommand("test1")
      testCommand("profiler:stop")
      testReporter("profiler:inclusive-time \"test2\" = profiler:exclusive-time \"test2\"", "true")
      testReporter("profiler:inclusive-time \"test1\" >= profiler:inclusive-time \"test2\"", "true")
      testReporter("profiler:exclusive-time \"test1\" < profiler:exclusive-time \"test2\"", "true")
      testReporter("profiler:exclusive-time \"test1\" <= 5", "true")
      testCommand("set glob1 profiler:exclusive-time \"test1\" + profiler:exclusive-time \"test2\"")
      testReporter("precision (glob1 - profiler:inclusive-time \"test1\") 8", "0")
    }
  if(useGenerator && timingSensitiveOK)
    test("nested asks", SlowTestTag) { implicit fixture =>
      import fixture._
      declare(
        "extensions [profiler]\n" +
        "globals [glob1 glob2 glob3]\n" +
        "to go ask turtles [ go-turtles1 ] ask patches [ go-patches ] end\n" +
        "to go-turtles1 wait 0.0001 go-turtles2 end\n" +
        "to go-turtles2 ask turtles with [true] [ go-turtles3 ] end\n" +
        "to go-turtles3 wait 0.0001 end\n" +
        "to go-patches wait 0.0001 end")
      testCommand("crt 10")
      testCommand("profiler:reset")
      testCommand("profiler:start")
      testCommand("repeat 10 [ go ]")
      testCommand("profiler:stop")
      testReporter("profiler:calls \"go-patches\" = count patches * 10", "true")
      testReporter("profiler:calls \"go-turtles1\" = count turtles * 10", "true")
      testReporter("profiler:calls \"go-turtles2\" = count turtles * 10", "true")
      testReporter("profiler:calls \"go-turtles3\" = count turtles * count turtles * 10", "true")
      testReporter("profiler:exclusive-time \"go-patches\" = profiler:inclusive-time \"go-patches\"", "true")
      testReporter("profiler:exclusive-time \"go-turtles3\" = profiler:inclusive-time \"go-turtles3\"", "true")
      testCommand("set glob3 profiler:exclusive-time \"go-turtles3\"")
      testCommand("set glob2 profiler:exclusive-time \"go-turtles2\"")
      testCommand("set glob1 profiler:exclusive-time \"go-turtles1\"")
      testReporter("precision (glob2 + glob3 - profiler:inclusive-time \"go-turtles2\") 13", "0")
      testReporter("precision (glob1 + glob2 + glob3 - profiler:inclusive-time \"go-turtles1\") 13", "0")
    }
  if(useGenerator && timingSensitiveOK)
    test("reporter procedures", SlowTestTag) { implicit fixture =>
      import fixture._
      declare(
        "extensions [profiler]\n" +
        "to-report some-value wait 0.1 report random 10 end")
      testCommand("crt 10")
      testCommand("profiler:reset")
      testCommand("profiler:start")
      testCommand("ask turtles [ print some-value ]")
      testCommand("profiler:stop")
      testReporter("profiler:calls \"some-value\"", "10")
      testReporter("profiler:exclusive-time \"some-value\" = profiler:inclusive-time \"some-value\"", "true")
      testReporter("precision profiler:exclusive-time \"some-value\" -3", "1000")
    }

  // kludginess ahead. this isn't really a test of the profiler extension per se.  the other
  // isReporter tests are in TestCompiler.  but this test case involves an extension primitive.  it
  // doesn't matter which extension, so we use profiler.  ideally we'd have a test scaffold that
  // lets us test extensions stuff without having an actual extension jar in hand.  but we don't,
  // and we don't want anything in test-fast or test-medium to depend on submodules like models and
  // extensions, so we put it here because it's a SlowTest - ST 1/19/12
  test("isReporter on extension prims", SlowTestTag) { implicit fixture =>
    import fixture._
    declare(Model(code = "extensions [profiler]"))
    assertResult(false) { workspace.compilerServices.isReporter("profiler:start") }
    assertResult(true) { workspace.compilerServices.isReporter("profiler:report") }
  }

}
