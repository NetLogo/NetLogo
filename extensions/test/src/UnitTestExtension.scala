import org.nlogo.api._
import org.nlogo.api.Syntax._
import org.nlogo.nvm.{ExtensionContext, ReporterLambda, CommandLambda}

/**
to run-tests
  test:setup [ crt 5 ]
  test:add [ crt 7 ] [ count turtles ] 12
  test:add [] [ count turtles ] 5
  test:run
  print test:summary
  print test:full-report
end
 */

// extension
class UnitTestExtension extends DefaultClassManager {
  def load(manager: PrimitiveManager) {
    manager.addPrimitive("add", new AddTest)
    manager.addPrimitive("setup", new TestSetup)
    manager.addPrimitive("run", new RunUnitTests)
    manager.addPrimitive("summary", new TestSummary)
    manager.addPrimitive("full-report", new FullTestReport)
  }
}

// primitives
class TestSetup extends DefaultCommand {
  override def getSyntax = commandSyntax(Array(TYPE_COMMAND_LAMBDA))
  def perform(args: Array[Argument], context: Context){
    // could possibly check to see if there is a setup method here already...
    UnitTestExtension.setup = Some(args(0).get.asInstanceOf[CommandLambda])
  }
}

class AddTest extends DefaultCommand {
  override def getSyntax = commandSyntax(Array(TYPE_STRING, TYPE_COMMAND_LAMBDA, TYPE_REPORTER_LAMBDA, TYPE_WILDCARD))
  def perform(args: Array[Argument], context: Context) = {
    def get[T](index:Int) = args(index).get.asInstanceOf[T]
    UnitTestExtension.addTest(Test(get[String](0),get[CommandLambda](1),get[ReporterLambda](2),get[AnyRef](3)))
  }
}

class RunUnitTests extends DefaultCommand {
  override def getSyntax = commandSyntax(Array())
  def perform(args: Array[Argument], context: Context){ UnitTestExtension.run(context) }
}

class TestSummary extends DefaultReporter {
  override def getSyntax = reporterSyntax(Array(), TYPE_STRING)
  def report(args: Array[Argument], context: Context) = UnitTestExtension.summaryWithFailsAndErrors
}

class FullTestReport extends DefaultReporter {
  override def getSyntax = reporterSyntax(Array(), TYPE_STRING)
  def report(args: Array[Argument], context: Context) = UnitTestExtension.fullReport
}

// main object
object UnitTestExtension {
  val tests = collection.mutable.ListBuffer[Test]()
  var setup: Option[CommandLambda] = None
  var results: Iterable[TestResult] = Nil
  def addTest(t: Test){ tests += t }
  def run(context:Context)  {
    results = tests.map(t => t.run(context))
    clear()
  }
  private def clear(){ tests.clear(); setup = None }
  def successes = results.collect{ case t: Pass => t }
  def failures = results.collect{ case t: Fail => t }
  def errors = results.collect{ case t: Error => t }
  def simpleSummary = {
    (if(failures.size + errors.size == 0) "Test Run Passed" else "Test Run Failed") + " - " +
    "Total "+results.size+", Failed "+failures.size+", Errors "+errors.size+", Passed "+successes.size
  }
  def summaryWithFailsAndErrors = {
    simpleSummary +
    (if(failures.size>0) ("\n"+failures.map(_.report).mkString("\n")) else "") +
    (if(errors.size>0) ("\n"+errors.map(_.report).mkString("\n")) else "")
  }
  def fullReport = simpleSummary + "\n" + results.map(_.report).mkString("\n")
}

// helpers
case class Test(name:String, command:CommandLambda, reporter: ReporterLambda, expectedResult:AnyRef){
  def run(c: Context) = {
    val context = c.asInstanceOf[ExtensionContext]
    val workspace = context.workspace
    try{
      workspace.clearAll()
      UnitTestExtension.setup.foreach(_.perform(context.nvmContext, Array()))
      command.perform(context.nvmContext, Array())
      val actualResult = reporter.report(context.nvmContext, Array())
      if(actualResult == expectedResult) pass else fail(actualResult, expectedResult)
    }
    catch { case e: LogoException => err(e) }
  }
  def pass = Pass(this)
  def fail(actual:Any, expected:Any) = Fail(this, "expected: " + expected + " but got: " + actual)
  def err(t: LogoException) = Error(this, t)
}

trait TestResult {
  val test: Test
  def report: String
}
case class Pass(test:Test) extends TestResult{
  def report: String = "PASS '" + test.name + "'"
}
case class Fail(test:Test, reason:String) extends TestResult{
  def report: String = "FAIL '" + test.name + "' " + reason
}
case class Error(test:Test, e:LogoException) extends TestResult{
  def report: String = "ERROR '" + test.name + "' " + e.getMessage
}
