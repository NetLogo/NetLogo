// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.tortoise

import
  org.nlogo.api,
  org.nlogo.util,
    util.Utils.getResourceAsString,
    util.MersenneTwisterFast
import java.io.{ PrintWriter, StringWriter }
import sun.org.mozilla.javascript.internal.NativeArray

// There are two main entry points here: run() and eval().  The former runs compiled commands and
// collects all the lines of output and JSON updates generated.  The latter runs a compiled reporter
// and returns a single result value.

class Rhino {

  val engine =
    (new javax.script.ScriptEngineManager)
      .getEngineByName("rhino")
      .ensuring(_ != null, "JavaScript engine unavailable")

  // the original CoffeeScript for these are in headless/src/main/coffee. sbt compiles
  // them to JavaScript for us.  (and downloads json2.js direct from GitHub).
  // unlike V8, Rhino doesn't have JSON.stringify built-in, so we get it from json2.js

  private val scalaJSLibs = Seq("/js/dev/scalajs-corejslib.js", "/js/dev/engine.js")
//  private val scalaJSLibs = Seq("/js/engine-opt.js")

  val libs = Seq("/json2.js", "/js/compat.js", "/js/agentmodel.js") ++ scalaJSLibs
  for (lib <- libs)
    engine.eval(getResourceAsString(lib))

  // make a random number generator available
  engine.put("Random", new MersenneTwisterFast)

  // ensure exact matching results
  engine.put("StrictMath", Strict)

  // returns anything that got output-printed along the way, and any JSON
  // generated too
  def run(script: String): (String, String) = {

    import ScalaJSLookups.OverlordObj

    val sw = new StringWriter
    engine.getContext.setWriter(new PrintWriter(sw))
    engine.eval(s"(function () {\n $script \n }).call(this);")
    (sw.toString, engine.eval(s"JSON.stringify($OverlordObj.collectUpdates())").toString)

  }

  def eval(script: String): AnyRef =
    fromRhino(engine.eval(script))

  // translate from Rhino values to NetLogo values
  def fromRhino(x: AnyRef): AnyRef =
    x match {
      case a: NativeArray =>
        api.LogoList.fromIterator(
          Iterator.from(0)
            .map(x => fromRhino(a.get(x, a)))
            .take(a.getLength.toInt))
      // this should probably reject unknown types instead of passing them through.
      // known types: java.lang.Double, java.lang.Boolean, String
      case x =>
        x
    }

}
