// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.tortoise

import org.nlogo.{ api, nvm, prim, workspace }
import org.nlogo.util.Femto

object Compiler {

  val compiler: nvm.CompilerInterface =
    Femto.scalaSingleton(classOf[nvm.CompilerInterface],
      "org.nlogo.compiler.Compiler")

  def compile(logo: String): String = {
    val wrapped =
      workspace.Evaluator.getHeader(api.AgentKind.Observer, commands = false) +
        logo + workspace.Evaluator.getFooter(commands = false)
    val results =
      compiler.compileMoreCode(wrapped, None, api.Program.empty(),
        nvm.CompilerInterface.NoProcedures, new api.DummyExtensionManager,
        nvm.CompilerFlags(foldConstants = false))
    compileReporter(results.head.code.head.args(0))
  }

  ///

  def compileReporter(r: nvm.Reporter): String = {
    def arg(i: Int) =
      compileReporter(r.args(i))
    r match {
      case pure: nvm.Pure if pure.args.isEmpty =>
        compileLiteral(pure.report(null))
      case Infix(op) =>
        s"(${arg(0)}) $op (${arg(1)})"
    }
  }

  object Infix {
    def unapply(r: nvm.Reporter): Option[String] =
      PartialFunction.condOpt(r) {
        case _: prim._plus     => "+"
        case _: prim._minus    => "-"
        case _: prim.etc._mult => "*"
        case _: prim.etc._div  => "/"
      }
  }

  def compileLiteral(x: AnyRef): String =
    x match {
      case ll: api.LogoList =>
        ll.map(compileLiteral).mkString("[", ", ", "]")
      case x =>
        api.Dump.logoObject(x, readable = true, exporting = false)
    }

}
