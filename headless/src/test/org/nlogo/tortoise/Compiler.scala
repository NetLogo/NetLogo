// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.tortoise

import org.nlogo.{ api, compiler, nvm, prim, workspace }

object Compiler {

  // two main entry points. input is NetLogo, result is JavaScript.

  def compileReporter(logo: String): String =
    compile(logo, commands = false)

  def compileCommands(logo: String): String =
    compile(logo, commands = true)

  ///

  // How this works:
  // - the header/footer stuff wraps the code in `to` or `to-report`
  // - the compile returns a Seq, whose head is a ProcedureDefinition
  //   containing some Statements (the procedure body)
  // - in the reporter case, the procedure body starts with the
  //   `__observer-code` command followed by the `report` command, so the
  //   actual reporter is the first (and only) argument to `report`

  def compile(logo: String, commands: Boolean): String = {
    val wrapped =
      workspace.Evaluator.getHeader(api.AgentKind.Observer, commands) +
        logo + workspace.Evaluator.getFooter(commands)
    val (defs, _) = compiler.Compiler.frontEnd(wrapped)  // Seq[ProcedureDefinition]
    if (commands)
      generateCommands(defs.head.statements)
    else
      defs.head.statements.tail.head.args.head match {
        case app: compiler.ReporterApp =>
          generateReporter(app)
      }
  }

  ///

  def generateCommands(cs: compiler.Statements): String =
    cs.map(generateCommand)
      .filter(_.nonEmpty)
      .mkString("\n")

  ///

  def generateCommand(s: compiler.Statement): String = {
    s.command match {
      case _: prim._done             => ""
      case _: prim.etc._observercode => ""
      case _: prim.etc._while        => Prims.generateWhile(s)
      case Prims.NormalCommand(op)   =>
        def args =
          s.args.collect{case x: compiler.ReporterApp =>
            generateReporter(x)}
                  .mkString(", ")
        s"$op($args);"
    }
  }

  def generateReporter(r: compiler.ReporterApp): String = {
    def arg(i: Int) =
      r.args(i) match {
        case app: compiler.ReporterApp =>
          generateReporter(app)
      }
    r.reporter match {
      case pure: nvm.Pure if r.args.isEmpty =>
        compileLiteral(pure.report(null))
      case Prims.InfixReporter(op) =>
        s"(${arg(0)} $op ${arg(1)})"
      case Prims.NormalReporter(op) =>
        val generatedArgs =
          r.args.indices.map(arg).mkString(", ")
        s"$op($generatedArgs)"
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
