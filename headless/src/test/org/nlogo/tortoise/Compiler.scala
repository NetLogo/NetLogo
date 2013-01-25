// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.tortoise

import org.nlogo.{ api, compiler, nvm, prim, workspace }

object Compiler {

  // three main entry points. input is NetLogo, result is JavaScript.

  def compileReporter(logo: String): String =
    compile(logo, commands = false)

  def compileCommands(logo: String,
    oldProcedures: compiler.Compiler.ProceduresMap = nvm.CompilerInterface.NoProcedures,
    program: api.Program = api.Program.empty()): String =
    compile(logo, commands = true, oldProcedures, program)

  def compileProcedures(logo: String): String = {
    // (Seq[ProcedureDefinition], StructureParser.Results)
    val (defs, sp) = compiler.Compiler.frontEnd(logo)
    RuntimeInit(sp) + defs.map(compileProcedureDef).mkString("\n")
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
      oldProcedures: compiler.Compiler.ProceduresMap = nvm.CompilerInterface.NoProcedures,
      program: api.Program = api.Program.empty()): String = {
    val wrapped =
      workspace.Evaluator.getHeader(api.AgentKind.Observer, commands) +
        logo + workspace.Evaluator.getFooter(commands)
    val (defs, _) = compiler.Compiler.frontEnd(wrapped, oldProcedures, program)  // Seq[ProcedureDefinition]
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
      // we need ask, we just shouldn't rely on it for test results.
      case _: prim._ask              => Prims.generateAsk(s)
      case _: prim._asksorted        => Prims.generateAsk(s)
      case Prims.NormalCommand(op)   => s"$op($args)"
      case s: prim._setobservervariable => s"Globals.setGlobal(${s.vn},${arg(0)})"
      case s: prim._setturtlevariable   => s"AgentSet.setTurtleVariable(${s.vn},${arg(0)})"
      case s: prim._setpatchvariable    => s"AgentSet.setPatchVariable(${s.vn},${arg(0)})"
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
      case tv: prim._turtlevariable         => s"AgentSet.getTurtleVariable(${tv.vn})"
      case pv: prim._patchvariable          => s"AgentSet.getPatchVariable(${pv.vn})"
      case ov: prim._observervariable       => s"Globals.getGlobal(${ov.vn})"
    }
  }

  def compileLiteral(x: AnyRef): String = x match {
    case ll: api.LogoList => ll.map(compileLiteral).mkString("[", ", ", "]")
    case x                => api.Dump.logoObject(x, readable = true, exporting = false)
  }

  // these could be merged into one function, genExpression
  // but I think the resulting code would be confusing and potentially error prone.
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

// RuntimeInit generates JavaScript code that does any initialization that needs to happen
// before any user code runs, for example creating patches

object RuntimeInit{
  def apply(sp: compiler.StructureParser.Results): String =
    new RuntimeInit(sp).init
}

class RuntimeInit(sp: compiler.StructureParser.Results) {
  def init = globals + turtlesOwn + patchesOwn + initWorld

  // this is a dirty hack.
  // if there are patches-own variables, we need recreate the patches
  // really, we should just do this always
  // but it screws up a bunch of the tests and i dont want to
  // bite that off right now. -JC 1/24/13
  def initWorld =
    if (sp.program.patchesOwn.size > patchBuiltinCount)
      "world = new World(-5,5,-5,5)\n"
    else ""

  // if there are any globals,
  // tell the runtime how many there are, it will initialize them all to 0.
  // if not, do nothing.
  def globals = vars(sp.program.globals, "Globals")

  // tell the runtime how many *-own variables there are
  val turtleBuiltinCount =
    api.AgentVariables.getImplicitTurtleVariables(is3D = false).size
  val patchBuiltinCount =
    api.AgentVariables.getImplicitPatchVariables(is3D = false).size
  def turtlesOwn =
    vars(sp.program.turtlesOwn.drop(turtleBuiltinCount), "TurtlesOwn")
  def patchesOwn =
    vars(sp.program.patchesOwn.drop(patchBuiltinCount), "PatchesOwn")

  private def vars(s: Seq[String], initPath: String) =
    if (s.nonEmpty) s"$initPath.init(${s.size})\n"
    else ""
}
