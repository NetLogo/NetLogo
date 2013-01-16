// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.tortoise

import sun.org.mozilla.javascript.internal.NativeArray
import java.io.StringWriter
import org.nlogo.api

object Rhino {

  val engine =
    (new javax.script.ScriptEngineManager)
      .getEngineByName("JavaScript")

  def run(script: String): String = {
    val sw = new StringWriter
    engine.getContext.setWriter(new java.io.PrintWriter(sw))
    engine.eval(script)
    sw.toString
  }

  def eval(script: String): AnyRef =
    fromRhino(engine.eval(script))

  def fromRhino(x: AnyRef): AnyRef =
    x match {
      case a: NativeArray =>
        api.LogoList.fromIterator(
          Iterator.from(0)
            .map(x => fromRhino(a.get(x, a)))
            .take(a.getLength.toInt))
      case _ =>
        x
    }

}
