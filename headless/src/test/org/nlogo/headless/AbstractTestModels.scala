// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless

import scala.util.DynamicVariable
import org.scalatest.FunSuite
import org.nlogo.api.{LogoException, Version}
import org.nlogo.api.ModelCreator

/**
 * A DSL for testing models.
 */
trait AbstractTestModels extends FunSuite with ModelCreator {

  /**
   * test a model created in code
   */
  def testModel(testName: String, model: Model)(f: => Unit) = {
    test(testName){ runModel(model){ f } }
  }

  def testModelCompileError(testName: String, model: Model)(f: Throwable => Unit) = {
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
  object observer {
    def >>(s: String) = workspace.command(s)
  }

  def testError(f: => Unit, message:String){
    val e = intercept[LogoException]{f}
    assert(e.getMessage === message)
  }

  // runs the given model in a new workspace
  def runModel(model:Model)(f: => Unit) = {
    // very useful, do not just remove.
    //println(model.toString)
    // println(".")
    run(ws => ws.openFromSource(model.toString)){ f }
  }
  // lods the model from the given file, and runs it in a new workspace
  def runModelFromFile(path: String)(f: => Unit) = run(ws => ws.open(path)){ f }

  // run a model
  private def run(openModel: HeadlessWorkspace => Unit)(f: => Unit) {
    _workspace.withValue(HeadlessWorkspace.newInstance) {
      workspace.silent = true
      openModel(workspace)
      try f finally workspace.dispose()
    }
  }

  def checkError(commands: String) {
    intercept[LogoException] {
      workspace.command(commands)
    }
    workspace.clearLastLogoException()
  }
}
