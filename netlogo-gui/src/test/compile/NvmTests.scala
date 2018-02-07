// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compile

import java.util.concurrent.atomic.AtomicBoolean

import org.scalatest.FunSuite

import org.nlogo.api.SimpleJobOwner
import org.nlogo.compile.api.{ Expression, ProcedureDefinition,
  ReporterApp, ReporterBuilder, StatementsBuilderBase }
import org.nlogo.core.{ AgentKind, Command => CoreCommand, Let,
  SourceLocation, Syntax, Token, TokenType, prim => coreprim },
    coreprim.{
      _call => _corecall, _callreport => _corecallreport, _carefully => _corecarefully,
      _const => _coreconst, _done => _coredone, _equal => _coreequal,
      _lessthan => _corelessthan, _let => _corelet, _letvariable => _coreletvariable,
      _repeat => _corerepeat, _return => _corereturn, _set => _coreset }
import org.nlogo.nvm.{ Binding, Command, Context, ExclusiveJob, Procedure }
import org.nlogo.prim.{ _call, _callreport, _carefully, _constdouble, _conststring,
  _done, _equal, _lessthan, _let, _letvariable, _repeat, _repeatlocal,
  _return, _setletvariable }

// Q: Why is there an nvm test in the compile package?
// A: Because much of nvm's behavior depends on the prim
//    package and `nvm` may not depend on `prim`. Perhaps
//    in some future, we will isolate things so that `nvm`
//    can be tested without reference to `prim`, until then
//    this test lives here. RG 1/30/17
class NvmTests extends FunSuite {
  def token(s: String): Token = Token(s.toUpperCase, TokenType.Command, null)(SourceLocation(0, 0, ""))

  val loc = SourceLocation(0, 0, "")


  def constInt(i: Int): ReporterApp =
    new ReporterApp(_coreconst(Double.box(i)), new _constdouble(Double.box(i)), loc)

  val one = constInt(1)
  val two = constInt(2)

  def constString(s: String): ReporterApp =
    new ReporterApp(_coreconst(s), new _conststring(s), loc)

  class _probe(condition: Context => Boolean, val name: String) extends Command {
    var satisfiedCondition = false

    override def perform(context: Context): Unit = {
      satisfiedCondition = condition(context)
      context.ip = next
    }

    def verify(): Unit = {
      assert(satisfiedCondition, name)
    }
  }

  case class _probesyntax() extends CoreCommand() {
    def syntax = Syntax.commandSyntax()
  }

  private lazy val stuffer = new ArgumentStuffer()
  trait Helper {
    lazy val workspace = new org.nlogo.nvm.DummyWorkspace()
    lazy val world = new org.nlogo.agent.World2D()
    lazy val owner = new SimpleJobOwner("Test", world.mainRNG, AgentKind.Observer)
    var probes = Seq.empty[_probe]

    val a = new Let("a")
    val b = new Let("b")

    def commandProcedure(name: String, i: Int = 0): Procedure = {
      val args = (0 until i).map(j => "PROCEDUREVAR" + j)
      val p = new Procedure(false, name.toUpperCase, token(name.toUpperCase), args.map(token _), null)
      p.topLevel = true
      p
    }

    def reporterProcedure(name: String): Procedure = {
      val p = new Procedure(true, name.toUpperCase, token(name.toUpperCase), Seq.empty[Token], null)
      p.topLevel = true
      p
    }

    // these may need to be split up at some point...
    def execute(proc: Procedure, stmtBuilder: StatementsBuilder): Unit = {
      // we add done because that's done for buttons and command center procedures "top-level procedures"
      assembleProcedure(proc, stmtBuilder.done)
      world.mainRNG.setSeed(0)
      exclusiveJob(proc).run()
      probes.foreach(_.verify())
    }

    def assembleProcedure(proc: Procedure, statements: StatementsBuilder): Unit = {
      val procDef = new ProcedureDefinition(proc, statements.build)
      procDef.accept(stuffer)
      new Assembler().assemble(procDef)
      proc.init(workspace)
    }

    def exclusiveJob(proc: Procedure): ExclusiveJob =
      new ExclusiveJob(owner, world.observers, proc, 0, null, world.mainRNG, new AtomicBoolean(false))

    def checkBinding(l: Let, value: AnyRef): _probe = {
      val p =
        new _probe( { (c: Context) => c.activation.binding.getLet(l) == value }, s"${l.name} is bound to $value")
      probes :+= p
      p
    }

    def checkBindingCount(i: Int): _probe = {
      val p = new _probe( { (c: Context) =>
        c.activation.binding.size == i && {
          var head = c.activation.binding.head
          var j = 0
          while (j < i) {
            head = head.next
            j += 1
          }
          head == Binding.EmptyBinding
        }
      }, s"contains only $i let-bindings")
      probes :+= p
      p
    }

    def checkNotBound(l: Let): _probe = {
      val p = new _probe({ (c: Context) =>
        try {
          c.activation.binding.getLet(l)
          false
        } catch {
          case e: NoSuchElementException => true
          case _: Exception => false
        }
      }, s"${l.name} is bound, but shouldn't be")
      probes :+= p
      p
    }
  }

  test("`repeat` doesn't lead to accumulation of let variables") { new Helper {
    val proc = commandProcedure("repeat")
    val containsA = checkBinding(a, Double.box(1))
    val containsSingleBinding = checkBindingCount(2) // one for A, one for Repeat

    val repeatBody = new StatementsBuilder() {
      let(a, one)
      probe(containsA)
      probe(containsSingleBinding)
    }

    val procedureBody = new StatementsBuilder() {
      statement(_corerepeat(), new _repeat(token("repeat")), Seq(two, repeatBody.buildBlock))
    }

    execute(proc, procedureBody)
  } }

  test("`loop` doesn't lead to accumulation of let variables") { new Helper {
    val proc = commandProcedure("loop")
    val containsA = checkBinding(a, Double.box(1))
    val containsSingleBinding = checkBindingCount(2) // one for A, one for B

    val stop = new StatementsBuilder() {
      statementEtc("_stop", "etc._stop", Seq())
    }

    val bEqualsTwo = new ReporterBuilder() {
      withReporter(_coreequal(), new _equal())
      withArg(new ReporterApp(_coreletvariable(b), new _letvariable(b), loc))
      withArg(two)
    }

    val loopBody = new StatementsBuilder() {
      let(a, one)
      probe(containsA)
      probe(containsSingleBinding)
      statementEtc("etc._if", Seq(bEqualsTwo.build, stop.buildBlock))
      statement(_coreset(), new _setletvariable(b), Seq(two))
    }

    val procedureBody = new StatementsBuilder() {
      let(b, one)
      statementEtc("etc._loop", Seq(loopBody.buildBlock))
    }
    execute(proc, procedureBody)
  } }

  test("`repeatlocal` doesn't lead to accumulation of let variables") { new Helper {
    val proc = commandProcedure("repeatlocal", 1)
    val containsA = checkBinding(a, Double.box(1))
    val containsSingleBinding = checkBindingCount(1)

    val repeatBody = new StatementsBuilder() {
      let(a, one)
      probe(containsA)
      probe(containsSingleBinding)
    }

    val procedureBody = new StatementsBuilder() {
      statement(_corerepeat(), new _repeatlocal(0), Seq(two, repeatBody.buildBlock))
    }

    execute(proc, procedureBody)
  } }

  test("`while` doesn't lead to accumulation of let variables") { new Helper {
    val proc = commandProcedure("while")
    val containsA = checkBinding(a, Double.box(1))
    val containsSingleBinding = checkBindingCount(2) // one for A, one for B

    val getB = new ReporterApp(_coreletvariable(b), new _letvariable(b), loc)

    val incrementB =
      new ReporterBuilder() {
        reporterAppEtc("etc._plus", "_plus")
        withArg(getB)
        withArg(one)
      }

    val whileBody = new StatementsBuilder() {
      let(a, one)
      probe(containsA)
      probe(containsSingleBinding)
      statement(_coreset(), new _setletvariable(b), Seq(incrementB.build))
    }

    val bLessThanThree =
      new ReporterBuilder() {
        withReporter(_corelessthan(), new _lessthan())
        withArg(getB)
        withArg(constInt(3))
      }

    val procedureBody = new StatementsBuilder() {
      let(b, one)
      statementEtc("etc._while", Seq(bLessThanThree.buildBlock, whileBody.buildBlock))
    }

    execute(proc, procedureBody)
  } }

  test("call/return restores context state") { new Helper {
    val containsA = checkBinding(a, Double.box(1))
    val containsSingleBinding = checkBindingCount(1)
    val caller = commandProcedure("caller")
    val callee = commandProcedure("callee")
    val callerBody = new StatementsBuilder() {
      let(a, one)
      statement(_corecall(callee), new _call(callee))
      probe(containsA)
      probe(containsSingleBinding)
    }
    val calleeBody = new StatementsBuilder() {
      let(b, two)
    }
    assembleProcedure(callee, calleeBody)
    execute(caller, callerBody)
  } }

  test("callreport/report restores context state") { new Helper {
    val containsA = checkBinding(a, Double.box(1))
    val containsSingleBinding = checkBindingCount(1)
    val caller = commandProcedure("caller")
    val callee = reporterProcedure("callee")
    val callCalleeForValue = new ReporterBuilder() {
      withReporter(_corecallreport(callee), new _callreport(callee))
    }
    val callerBody = new StatementsBuilder() {
      let(a, one)
      _ignore(callCalleeForValue.build)
      probe(containsA)
      probe(containsSingleBinding)
    }
    val calleeBody = new StatementsBuilder() {
      let(b, two)
      statementEtc("_report", "etc._report", Seq(two))
    }
    assembleProcedure(callee, calleeBody)
    execute(caller, callerBody)
  } }

  test("`carefully` properly restores context in normal case") { new Helper {
    val containsA = checkBinding(a, Double.box(1))
    val doesNotContainB = checkNotBound(b)
    val containsSingleBinding = checkBindingCount(1)
    val proc = commandProcedure("carefully")
    val carefullyBody = new StatementsBuilder() {
      let(b, two)
    }
    val carefullyHandle = new StatementsBuilder() { }
    val procedureBody = new StatementsBuilder() {
      let(a, one)
      carefully(carefullyBody, carefullyHandle)
      probe(containsA)
      probe(doesNotContainB)
      probe(containsSingleBinding)
    }
    execute(proc, procedureBody)
    containsA.verify()
  } }

  test("`carefully` properly restores context after error case") { new Helper {
    val containsA = checkBinding(a, Double.box(1))
    val doesNotContainB = checkNotBound(b)
    // One of these is A, one is the binding for `errormessage`
    // In an ideal world, we would clear up the binding for `errormessage` once we
    // exit the handle block. Unfortunately, the architecture of nvm doesn't make
    // this easy - RG 2/1/17
    val containsSingleBinding = checkBindingCount(2)
    val proc = commandProcedure("carefully")
    val carefullyBody = new StatementsBuilder() {
      let(b, one)
      statementEtc("etc._error", Seq(constString("foo")))
    }
    val carefullyHandle = new StatementsBuilder() { }
    val procedureBody = new StatementsBuilder() {
      let(a, one)
      carefully(carefullyBody, carefullyHandle)
      probe(containsA)
      probe(doesNotContainB)
      probe(containsSingleBinding)
    }
    execute(proc, procedureBody)
  } }

  test("`carefully` properly restores context in error block in error case") { new Helper {
    val doesNotContainA = checkNotBound(a)
    val proc = commandProcedure("carefully")
    val carefullyBody = new StatementsBuilder() {
      let(a, one)
      statementEtc("etc._error", Seq(constString("foo")))
    }
    val carefullyHandle = new StatementsBuilder() {
      probe(doesNotContainA)
    }
    val procedureBody = new StatementsBuilder() {
      carefully(carefullyBody, carefullyHandle)
    }
    execute(proc, procedureBody)
  } }


  test("`carefully` properly restores context after error inside procedure call") { new Helper {
    val containsA = checkBinding(a, Double.box(1))
    val doesNotContainB = checkNotBound(b)
    val caller = commandProcedure("caller")
    val callee = commandProcedure("callee")
    val calleeBody = new StatementsBuilder() {
      let(b, two)
      statementEtc("etc._error", Seq(constString("foo")))
    }
    val carefullyBody = new StatementsBuilder() {
      statement(_corecall(callee), new _call(callee))
    }
    val carefullyHandle = new StatementsBuilder() { }
    val callerBody = new StatementsBuilder() {
      let(a, one)
      carefully(carefullyBody, carefullyHandle)
      probe(containsA)
      probe(doesNotContainB)
    }
    assembleProcedure(callee, calleeBody)
    execute(caller, callerBody)
  } }

  class StatementsBuilder extends StatementsBuilderBase {
    type ThisBuilder = StatementsBuilder
    def thisBuilder = this

    def let(l: Let, value: Expression): StatementsBuilder =
      statement(_corelet(Some(l)), new _let(l), Seq(value))

    def _ignore(app: ReporterApp): StatementsBuilder =
      statementEtc("etc._ignore", Seq(app))

    def carefully(block: StatementsBuilder, errorBlock: StatementsBuilder): StatementsBuilder = {
      val coreCarefully = _corecarefully()
      statement(coreCarefully, new _carefully(coreCarefully.let),
        Seq(block.buildBlock, errorBlock.buildBlock))
    }

    def probe(cmd: Command): StatementsBuilder =
      statement(_probesyntax(), cmd)

    def done = statement(_coredone(),    new _done())

    def end  = statement(_corereturn(),  new _return())
  }
}
