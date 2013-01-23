// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.tortoise

import org.nlogo.{ api, compiler, nvm, prim, workspace }

object Compiler {

  // two main entry points. input is NetLogo, result is JavaScript.

  def compileReporter(logo: String): String =
    compile(logo, commands = false)

  def compileCommands(logo: String,
    oldProcedures: compiler.Compiler.ProceduresMap = nvm.CompilerInterface.NoProcedures): String =
    compile(logo, commands = true, oldProcedures)

  // TODO: this isn't actually used anymore, should it just be removed?
  def compileProcedure(logo: String): String = {
    val (defs, _) = compiler.Compiler.frontEnd(logo)  // Seq[ProcedureDefinition]
    compileProcedureDef(defs.head)
  }

  def compileProcedures(logo: String): String = {
    val (defs, _) = compiler.Compiler.frontEnd(logo)  // Seq[ProcedureDefinition]
    defs.map(compileProcedureDef).mkString("\n")
  }

  private def compileProcedureDef(pd: compiler.ProcedureDefinition): String = {
    val name = pd.procedure.name
    val body = generateCommands(pd.statements)
    val args = pd.procedure.args.mkString(", ")
    s"function $name ($args) {\n$body\n};"
  }

  ///

  // How this works:
  // - the header/footer stuff wraps the code in `to` or `to-report`
  // - the compile returns a Seq, whose head is a ProcedureDefinition
  //   containing some Statements (the procedure body)
  // - in the reporter case, the procedure body starts with the
  //   `__observer-code` command followed by the `report` command, so the
  //   actual reporter is the first (and only) argument to `report`

  def compile(logo: String, commands: Boolean,
      oldProcedures: compiler.Compiler.ProceduresMap = nvm.CompilerInterface.NoProcedures): String = {
    val wrapped =
      workspace.Evaluator.getHeader(api.AgentKind.Observer, commands) +
        logo + workspace.Evaluator.getFooter(commands)
    val (defs, _) = compiler.Compiler.frontEnd(wrapped, oldProcedures)  // Seq[ProcedureDefinition]
    if (commands) generateCommands(defs.head.statements)
    else genArg(defs.head.statements.tail.head.args.head)
  }

  ///

  def generateCommands(cs: compiler.Statements): String =
    cs.map(generateCommand).filter(_.nonEmpty).mkString("\n")

  ///

  def generateCommand(s: compiler.Statement): String = {
    def arg(i: Int) = genArg(s.args(i))
    def args = s.args.collect{ case x: compiler.ReporterApp => genArg(x) }.mkString(", ")
    s.command match {
      case _: prim._done             => ""
      case _: prim.etc._observercode => ""
      case _: prim.etc._while        => Prims.generateWhile(s)
      case _: prim.etc._if           => Prims.generateIf(s)
      case _: prim.etc._ifelse       => Prims.generateIfElse(s)
      case l: prim._let
        // arg 0 is the name but we don't access it because LetScoper took care of it.
        // arg 1 is the value.
                                     => s"var ${l.let.name} = ${arg(1)};"
      case _: prim._set              => s"${arg(0)} = ${arg(1)};"
      case call: prim._call          => s"${call.procedure.name}($args)"
      case _: prim.etc._report       => s"return $args;"
      case _: prim._asksorted        => Prims.generateAsk(s)
      case Prims.NormalCommand(op)   => s"$op($args)"
    }
  }

  def generateReporter(r: compiler.ReporterApp): String = {
    def arg(i: Int) = genArg(r.args(i))
    def args = r.args.collect{ case x: compiler.ReporterApp => genArg(x) }.mkString(", ")
    r.reporter match {
      case pure: nvm.Pure if r.args.isEmpty => compileLiteral(pure.report(null))
      case lv: prim._letvariable            => lv.let.name
      case pv: prim._procedurevariable      => pv.name
      case call: prim._callreport           => s"${call.procedure.name}($args)"
      case Prims.InfixReporter(op)          => s"(${arg(0)} $op ${arg(1)})"
      case Prims.NormalReporter(op)         => s"$op(${r.args.map(genArg).mkString(", ")})"
      case tv: prim._turtlevariable         => s"AgentSet.getVariable(${tv.vn})"
    }
  }

  def compileLiteral(x: AnyRef): String = x match {
    case ll: api.LogoList => ll.map(compileLiteral).mkString("[", ", ", "]")
    case x                => api.Dump.logoObject(x, readable = true, exporting = false)
  }

  // these could be merged into one function, genExpression
  // but i think the resulting code wold be confusing and potentially error prone.
  // having different functions for each is more clear.

  def genReporterApp(e: compiler.Expression) = e match {
    case r: compiler.ReporterApp => generateReporter(r)
  }
  def genArg(e: compiler.Expression) = genReporterApp(e)
  def genReporterBlock(e: compiler.Expression) = e match {
    case r: compiler.ReporterBlock => Compiler.generateReporter(r.app)
  }
  def genCommandBlock(e: compiler.Expression) = e match {
    case cb: compiler.CommandBlock => Compiler.generateCommands(cb.statements)
  }
}
