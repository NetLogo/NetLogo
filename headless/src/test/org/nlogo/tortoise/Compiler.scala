// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.tortoise

import org.nlogo.{ api, headless, nvm }

object Compiler {

  private val ws = headless.HeadlessWorkspace.newInstance

  def compile(logo: String): String =
    ws.compileReporter(logo).code.head.args(0) match {
      case reporter: nvm.Reporter with nvm.Pure =>
        compileLiteral(reporter.report(null))
    }

  def compileLiteral(x: AnyRef): String =
    x match {
      case ll: api.LogoList =>
        ll.map(compileLiteral).mkString("[", ", ", "]")
      case x =>
        api.Dump.logoObject(x, readable = true, exporting = false)
    }

}
