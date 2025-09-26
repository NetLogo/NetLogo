// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless

import scala.util.DynamicVariable
import org.scalatest.funsuite.AnyFunSuite
import org.nlogo.api.{LogoException, Version}
import org.nlogo.api.ModelCreator
import org.nlogo.api.PlotCompilationErrorAction

/**
 * A DSL for testing models.
 */

trait AbstractTestModels extends AnyFunSuite with ModelCreator {

  /**
   * test a model created in code
   */
  def testModel(testName: String, model: Model)(f: => Unit) = {
    test(testName){ runModel(model){ f } }
  }

/**
   * Set the PlotCompilationErrorAction and then test a model created in code.
   * See netlogo-core/src/main/api/Controllable.scala for information on PlotCompilationErrorAction.
   */
  def testModelSetPlotCompilationErrorAction(testName: String, model: Model, plotCompilationErrorAction: PlotCompilationErrorAction)(f: => Unit) = {
    test(testName){ runModelSetPlotCompilationErrorAction(model, plotCompilationErrorAction){ f } }
  }

  def testModelCompileError(testName: String, model: Model)(f: Throwable => Unit) = {
    if (!Version.is3D)
      test(testName){
        val ex = intercept[Throwable]{
          runModel(model){}
        }
        f(ex)
      }
  }

  /**
   * test a model loaded from a file
   */
  def testModelFile(testName: String, path: String)(f: => Unit) = {
    test(testName){ runModelFromFile(path){ f } }
  }

  // use DynamicVariable to simplify calls to testModel - ST 3/4/10
  private val _workspace = new DynamicVariable[HeadlessWorkspace](null)
  def workspace = _workspace.value
  def world = _workspace.value.world

  def command(s: String): Unit = {
    workspace.command(s)
  }

  // part of the DSL enabling reporter("someValue") -> expected
  def reporter(s: String) = Reported(workspace.report(s))
  case class Reported(a:Any){
    def ->(r:Any) = r match {
      case Reported(ra) => assert(a === ra)
      case _ => assert(a === r)
    }
    def get = a
  }

  // also part of the DSL, enabling command-line like syntax: observer>>"somecommand"
  object ShovelableObserver { def >>(s: String) = workspace.command(s) }
  val observer = ShovelableObserver

  def testError(f: => Unit, message:String): Unit ={
    val e = intercept[LogoException]{f}
    assert(e.getMessage === message)
  }

  // runs the given model in a new workspace
  def runModel(model:Model)(f: => Unit) = {
    // very useful, do not just remove.
    // println(model.toString)
    // println(".")
    run(ws => ws.openModel(model)){ f }
  }

 // Set the PlotCompilationErrorAction and run the given model in a new workspace.
  def runModelSetPlotCompilationErrorAction(model:Model, plotCompilationErrorAction: PlotCompilationErrorAction)(f: => Unit) = {
    run(ws => {ws.setPlotCompilationErrorAction(plotCompilationErrorAction)
    ws.openModel(model)}){ f }
  }

  // loads the model from the given file, and runs it in a new workspace
  def runModelFromFile(path: String)(f: => Unit) = run(ws => ws.open(path)){ f }

  // run a model
  private def run(openModel: HeadlessWorkspace => Unit)(f: => Unit): Unit = {
    if (!Version.is3D) {
      _workspace.withValue(HeadlessWorkspace.newInstance) {
        try {
          workspace.silent = true
          openModel(workspace)
          f
        }
        finally workspace.dispose()
      }
    }
  }

  def checkError(commands: String): Unit = {
    intercept[LogoException] {
      workspace.command(commands)
    }
  }
}
