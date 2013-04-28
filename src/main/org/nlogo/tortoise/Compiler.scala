// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.tortoise

import org.nlogo.{ api, nvm, parse, prim, workspace }
import nvm.CompilerInterface.{ ProceduresMap, NoProcedures }

object Compiler {

  // three main entry points. input is NetLogo, result is JavaScript.

  def compileReporter(logo: String,
    oldProcedures: ProceduresMap = NoProcedures,
    program: api.Program = api.Program.empty()): String =
    compile(logo, commands = false, oldProcedures, program)

  def compileCommands(logo: String,
    oldProcedures: ProceduresMap = NoProcedures,
    program: api.Program = api.Program.empty()): String =
    compile(logo, commands = true, oldProcedures, program)

  def compileProcedures(logo: String, minPxcor: Int = 0, maxPxcor: Int = 0, minPycor: Int = 0, maxPycor: Int = 0): (String, api.Program, ProceduresMap) = {
    // (Seq[ProcedureDefinition], StructureParser.Results)
    val (defs, sp) = parse.Parser.frontEnd(logo)
    val js =
      new RuntimeInit(sp.program, minPxcor, maxPycor, minPycor, maxPycor).init +
        defs.map(compileProcedureDef).mkString("\n")
    (js, sp.program, sp.procedures)
  }

  private def compileProcedureDef(pd: parse.ProcedureDefinition): String = {
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
      oldProcedures: ProceduresMap = NoProcedures,
      program: api.Program = api.Program.empty()): String = {
    val wrapped =
      workspace.Evaluator.getHeader(api.AgentKind.Observer, commands) +
        logo + workspace.Evaluator.getFooter(commands)
    val (defs, _) = parse.Parser.frontEnd(wrapped, oldProcedures, program)  // Seq[ProcedureDefinition]
    if (commands) generateCommands(defs.head.statements)
    else genArg(defs.head.statements.tail.head.args.head)
  }

  ///

  def generateCommands(cs: parse.Statements): String =
    cs.map(generateCommand).filter(_.nonEmpty).mkString("\n")

  ///

  def generateCommand(s: parse.Statement): String = {
    def arg(i: Int) = genArg(s.args(i))
    def args = s.args.collect{ case x: parse.ReporterApp => genArg(x) }.mkString(", ")
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
      case p: prim._setletvariable   => s"${p.let.name} = ${arg(0)};"
      case call: prim._call          => s"${call.procedure.name}($args)"
      case _: prim.etc._report       => s"return $args;"
      // we need ask, we just shouldn't rely on it for test results.
      case _: prim._ask              => Prims.generateAsk(s)
      case _: prim._asksorted        => Prims.generateAsk(s)
      case Prims.NormalCommand(op)   => s"$op($args)"
      case s: prim._setobservervariable => s"Globals.setGlobal(${s.vn},${arg(0)})"
      case s: prim._setturtlevariable   => s"AgentSet.setTurtleVariable(${s.vn},${arg(0)})"
      case s: prim._setturtleorlinkvariable =>
        val vn = api.AgentVariables.getImplicitTurtleVariables.indexOf(s.varName)
        s"AgentSet.setTurtleVariable($vn,${arg(0)})"
      case s: prim._setpatchvariable => s"AgentSet.setPatchVariable(${s.vn},${arg(0)})"
      case r: prim._repeat           =>
        s"for(var i = 0; i < ${arg(0)}; i++) { ${genCommandBlock(s.args(1))} }"
    }
  }

  def generateReporter(r: parse.ReporterApp): String = {
    def arg(i: Int) = genArg(r.args(i))
    def args = argsSep(", ")
    def argsSep(sep: String) =
      r.args.collect{ case x: parse.ReporterApp => genArg(x) }.mkString(sep)
    r.reporter match {
      case pure: nvm.Pure if r.args.isEmpty => compileLiteral(pure.report(null))
      case lv: prim._letvariable            => lv.let.name
      case pv: prim._procedurevariable      => pv.name
      case call: prim._callreport           => s"${call.procedure.name}($args)"
      case Prims.InfixReporter(op)          => s"(${arg(0)} $op ${arg(1)})"
      case Prims.NormalReporter(op)         => s"$op($args)"
      case tv: prim._turtlevariable         => s"AgentSet.getTurtleVariable(${tv.vn})"
      case tv: prim._turtleorlinkvariable   =>
        val vn = api.AgentVariables.getImplicitTurtleVariables.indexOf(tv.varName)
        s"AgentSet.getTurtleVariable($vn)"
      case pv: prim._patchvariable          => s"AgentSet.getPatchVariable(${pv.vn})"
      case ov: prim._observervariable       => s"Globals.getGlobal(${ov.vn})"
      case s: prim._word                    => "\"\" + " + argsSep(" + ")
      case w: prim._with =>
        val agents = arg(0)
        val filter = genReporterBlock(r.args(1))
        s"AgentSet.agentFilter($agents, function(){ return $filter })"
      case p: prim._patch                   => s"Prims.patch($args)"
      case n: prim._neighbors               => s"Prims.getNeighbors()"
    }
  }

  def compileLiteral(x: AnyRef): String = x match {
    case ll: api.LogoList => ll.map(compileLiteral).mkString("[", ", ", "]")
    case x                => api.Dump.logoObject(x, readable = true, exporting = false)
  }

  // these could be merged into one function, genExpression
  // but I think the resulting code would be confusing and potentially error prone.
  // having different functions for each is more clear.

  def genReporterApp(e: parse.Expression) = e match {
    case r: parse.ReporterApp => generateReporter(r)
  }
  def genArg(e: parse.Expression) = genReporterApp(e)
  def genReporterBlock(e: parse.Expression) = e match {
    case r: parse.ReporterBlock => Compiler.generateReporter(r.app)
  }
  def genCommandBlock(e: parse.Expression) = e match {
    case cb: parse.CommandBlock => Compiler.generateCommands(cb.statements)
  }
}

// RuntimeInit generates JavaScript code that does any initialization that needs to happen
// before any user code runs, for example creating patches

class RuntimeInit(program: api.Program, minPxcor: Int, maxPxcor: Int, minPycor: Int, maxPycor: Int) {

  def init =
    globals + turtlesOwn + patchesOwn +
      s"world = new World($minPxcor, $maxPxcor, $minPycor, $maxPycor);\n"

  // if there are any globals,
  // tell the runtime how many there are, it will initialize them all to 0.
  // if not, do nothing.
  def globals = vars(program.globals, "Globals")

  // tell the runtime how many *-own variables there are
  val turtleBuiltinCount =
    api.AgentVariables.getImplicitTurtleVariables.size
  val patchBuiltinCount =
    api.AgentVariables.getImplicitPatchVariables.size
  def turtlesOwn =
    vars(program.turtlesOwn.drop(turtleBuiltinCount), "TurtlesOwn")
  def patchesOwn =
    vars(program.patchesOwn.drop(patchBuiltinCount), "PatchesOwn")

  private def vars(s: Seq[String], initPath: String) =
    if (s.nonEmpty) s"$initPath.init(${s.size})\n"
    else ""
}
