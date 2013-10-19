// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.tortoise

import org.nlogo.{ api, compile, nvm, prim, workspace },
   compile._,
   nvm.FrontEndInterface.{ ProceduresMap, NoProcedures },
   org.nlogo.util.Femto,
   org.nlogo.shape.{LinkShape, VectorShape}

import collection.JavaConverters._

object Compiler {

  val frontEnd = Femto.get[FrontEndInterface](
    "org.nlogo.compile.front.FrontEnd")

  // three main entry points. input is NetLogo, result is JavaScript.

  def compileReporter(logo: String,
    oldProcedures: ProceduresMap = NoProcedures,
    program: api.Program = api.Program.empty()): String =
    compile(logo, commands = false, oldProcedures, program)

  def compileCommands(logo: String,
    oldProcedures: ProceduresMap = NoProcedures,
    program: api.Program = api.Program.empty()): String =
    compile(logo, commands = true, oldProcedures, program)

  def compileProcedures(
      logo: String,
      interfaceGlobals: Seq[String] = Seq(),
      interfaceGlobalCommands: String = "",
      dimensions: api.WorldDimensions = api.WorldDimensions.square(0),
      turtleShapeList: api.ShapeList = new api.ShapeList(api.AgentKind.Turtle),
      linkShapeList: api.ShapeList = new api.ShapeList(api.AgentKind.Link))
      : (String, api.Program, ProceduresMap) = {
    // (Seq[ProcedureDefinition], StructureParser.Results)
    val (defs, sp) =
      frontEnd.frontEnd(logo,
        program = api.Program.empty.copy(interfaceGlobals = interfaceGlobals))
    val js =
      new RuntimeInit(sp.program, dimensions, turtleShapeList, linkShapeList).init +
        defs.map(compileProcedureDef).mkString("", "\n", "\n") +
        compileCommands(interfaceGlobalCommands, program = sp.program)
    if (sp.program.linkBreeds.nonEmpty)
      throw new IllegalArgumentException("unknown language feature: link breeds")
    (js, sp.program, sp.procedures)
  }

  private def compileProcedureDef(pd: ProcedureDefinition): String = {
    val name = ident(pd.procedure.name)
    val body = generateCommands(pd.statements)
    val args = pd.procedure.args.map(ident).mkString(", ")
    s"function $name ($args) {\n$body\n};"
  }

  // bogus, will need work - ST 9/13/13
  def ident(s: String) =
    s.replaceAll("-", "_")
     .replaceAll("\\?", "_P")

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
    val (defs, _) = frontEnd.frontEnd(wrapped, oldProcedures, program)  // Seq[ProcedureDefinition]
    if (commands) generateCommands(defs.head.statements)
    else genArg(defs.head.statements.tail.head.args.head)
  }

  ///

  def generateCommands(cs: Statements): String =
    cs.map(generateCommand).filter(_.nonEmpty).mkString("\n")

  ///

  // Scalastyle is right to complain about these gruesomely large match statements,
  // but it isn't worth failing the build over (for the time being) - ST 10/14/13
  // scalastyle:off cyclomatic.complexity
  def generateCommand(s: Statement): String = {
    def arg(i: Int) = genArg(s.args(i))
    def args = s.args.collect{ case x: ReporterApp => genArg(x) }.mkString(", ")
    s.command match {
      case _: prim._done             => ""
      case _: prim.etc._observercode => ""
      case _: prim.etc._while        => Prims.generateWhile(s)
      case _: prim.etc._if           => Prims.generateIf(s)
      case _: prim.etc._ifelse       => Prims.generateIfElse(s)
      case l: prim._let
        // arg 0 is the name but we don't access it because LetScoper took care of it.
        // arg 1 is the value.
                                     => s"var ${ident(l.let.name)} = ${arg(1)};"
      case call: prim._call          => s"${ident(call.procedure.name)}($args)"
      case _: prim.etc._report       => s"return $args;"
      case _: prim.etc._stop         => "return"
      case _: prim._ask              => Prims.generateAsk(s, shuffle = true)
      case _: prim._asksorted        => Prims.generateAsk(s, shuffle = false)
      case _: prim._createturtles        => Prims.generateCreateTurtles(s, ordered = false)
      case _: prim._createorderedturtles => Prims.generateCreateTurtles(s, ordered = true)
      case _: prim._sprout               => Prims.generateSprout(s)
      case h: prim._hatch                => Prims.generateHatch(s, h.breedName)
      case Prims.NormalCommand(op)   => s"$op($args)"
      case r: prim._repeat           =>
        s"for(var i = 0; i < ${arg(0)}; i++) { ${genCommandBlock(s.args(1))} }"
      case _: prim._set              =>
        s.args(0).asInstanceOf[ReporterApp].reporter match {
          case p: prim._letvariable =>
            s"${ident(p.let.name)} = ${arg(1)};"
          case p: prim._observervariable =>
            s"Globals.setGlobal(${p.vn},${arg(1)})"
          case bv: prim._breedvariable =>
            s"""AgentSet.setBreedVariable("${bv.name}",${arg(1)})"""
          case p: prim._turtlevariable =>
            s"AgentSet.setTurtleVariable(${p.vn},${arg(1)})"
          case p: prim._turtleorlinkvariable if p.varName == "BREED" =>
            s"AgentSet.setBreed(${arg(1)})"
          case p: prim._turtleorlinkvariable =>
            val vn = api.AgentVariables.getImplicitTurtleVariables.indexOf(p.varName)
            s"AgentSet.setTurtleVariable($vn,${arg(1)})"
          case p: prim._patchvariable =>
            s"AgentSet.setPatchVariable(${p.vn},${arg(1)})"
          case x =>
            throw new IllegalArgumentException(
              "unknown settable: " + x.getClass.getSimpleName)
        }
      case _ =>
        throw new IllegalArgumentException(
          "unknown primitive: " + s.command.getClass.getSimpleName)
    }
  }

  def generateReporter(r: ReporterApp): String = {
    def arg(i: Int) = genArg(r.args(i))
    def commaArgs = argsSep(", ")
    def args =
      r.args.collect{ case x: ReporterApp => genArg(x) }
    def argsSep(sep: String) =
      args.mkString(sep)
    r.reporter match {
      case _: prim._nobody                  => "Nobody"
      case x: prim.etc._isbreed             => s"""${arg(0)}.isBreed("${x.breedName}")"""
      case b: prim.etc._breed               => s"""world.turtlesOfBreed("${b.getBreedName}")"""
      case b: prim.etc._breedsingular       => s"""world.getTurtleOfBreed("${b.breedName}", ${arg(0)})"""
      case b: prim.etc._breedhere           => s"""AgentSet.self().breedHere("${b.getBreedName}")"""
      case x: prim.etc._turtle              => s"world.getTurtle(${arg(0)})"
      case pure: nvm.Pure if r.args.isEmpty => compileLiteral(pure.report(null))
      case lv: prim._letvariable            => ident(lv.let.name)
      case pv: prim._procedurevariable      => ident(pv.name)
      case call: prim._callreport           => s"${ident(call.procedure.name)}($commaArgs)"
      case Prims.InfixReporter(op)          => s"(${arg(0)} $op ${arg(1)})"
      case Prims.NormalReporter(op)         => s"$op($commaArgs)"
      case bv: prim._breedvariable          => s"""AgentSet.getBreedVariable("${bv.name}")"""
      case tv: prim._turtlevariable         => s"AgentSet.getTurtleVariable(${tv.vn})"
      case tv: prim._turtleorlinkvariable   =>
        val vn = api.AgentVariables.getImplicitTurtleVariables.indexOf(tv.varName)
        s"AgentSet.getTurtleVariable($vn)"
      case pv: prim._patchvariable          => s"AgentSet.getPatchVariable(${pv.vn})"
      case r: prim._reference               => s"${r.reference.vn}"
      case ov: prim._observervariable       => s"Globals.getGlobal(${ov.vn})"
      case s: prim._word                    =>
        ("\"\"" +: args).map(arg => "Dump(" + arg + ")").mkString("(", " + ", ")")
      case w: prim._with =>
        val agents = arg(0)
        val filter = genReporterBlock(r.args(1))
        s"AgentSet.agentFilter($agents, function(){ return $filter })"
      case o: prim._of =>
        val agents = arg(1)
        val body = genReporterBlock(r.args(0))
        s"AgentSet.of($agents, function(){ return $body })"
      case p: prim.etc._patch               => s"Prims.patch($commaArgs)"
      case _: prim.etc._nopatches           => "new Agents([])"
      case _: prim.etc._noturtles           => "new Agents([])"
      case n: prim._neighbors               => s"Prims.getNeighbors()"
      case n: prim._neighbors4              => s"Prims.getNeighbors4()"
      case _: prim.etc._minpxcor            => "world.minPxcor"
      case _: prim.etc._minpycor            => "world.minPycor"
      case _: prim.etc._maxpxcor            => "world.maxPxcor"
      case _: prim.etc._maxpycor            => "world.maxPycor"
      case _ =>
        throw new IllegalArgumentException(
          "unknown primitive: " + r.reporter.getClass.getSimpleName)
    }
  }
  // scalastyle:on cyclomatic.complexity

  def compileLiteral(x: AnyRef): String = x match {
    case ll: api.LogoList => ll.map(compileLiteral).mkString("[", ", ", "]")
    case x                => api.Dump.logoObject(x, readable = true, exporting = false)
  }

  // these could be merged into one function, genExpression
  // but I think the resulting code would be confusing and potentially error prone.
  // having different functions for each is more clear.

  def genReporterApp(e: Expression) = e match {
    case r: ReporterApp => generateReporter(r)
  }
  def genArg(e: Expression) = genReporterApp(e)
  def genReporterBlock(e: Expression) = e match {
    case r: ReporterBlock => Compiler.generateReporter(r.app)
  }
  def genCommandBlock(e: Expression) = e match {
    case cb: CommandBlock => Compiler.generateCommands(cb.statements)
  }
}

// RuntimeInit generates JavaScript code that does any initialization that needs to happen
// before any user code runs, for example creating patches

class RuntimeInit(program: api.Program, dimensions: api.WorldDimensions, turtleShapeList: api.ShapeList, linkShapeList: api.ShapeList) {
  import scala.collection.JavaConverters._
  import org.nlogo.tortoise.json.JSONSerializer

  def init = {
    import dimensions._
    var turtleShapesJson = "{}"
    if(!turtleShapeList.getNames.isEmpty) turtleShapesJson = JSONSerializer.serialize(turtleShapeList)
    var linkShapesJson = "{}"
    if(!linkShapeList.getNames.isEmpty) linkShapesJson = JSONSerializer.serialize(linkShapeList)
    globals + turtlesOwn + patchesOwn + breeds +
      s"world = new World($minPxcor, $maxPxcor, $minPycor, $maxPycor, $patchSize, " +
      s"$wrappingAllowedInY, $wrappingAllowedInX, $turtleShapesJson, $linkShapesJson, " +
      s"${program.interfaceGlobals.size});\n"
  }

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
  def breeds =
    program.breeds.values.map(
      b =>
        s"""Breeds.add("${b.name}", "${b.singular.toLowerCase}");\n""" +
          s"""Breeds.get("${b.name}").vars =""" +
          b.owns.mkString("[\"", "\", \"", "\"]") +
          ";"
    ).mkString("\n")

  private def vars(s: Seq[String], initPath: String) =
    if (s.nonEmpty) s"$initPath.init(${s.size})\n"
    else ""
}
