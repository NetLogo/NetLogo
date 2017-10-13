// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless

import org.scalatest.{ FunSuite, Tag }

import org.nlogo.api.{ LogoException, ModelCreator, WorldDimensions3D }
import org.nlogo.util.{ ThreeDTag, TwoDTag }
import org.nlogo.fileformat

import scala.util.DynamicVariable

/**
 * A DSL for testing models.
 */
trait AbstractTestModels extends FunSuite with ModelCreator {
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

  // part of the DSL enabling reporter("someValue") -> expected
  def reporter(s: String) = Reported(workspace.report(s))

  // use DynamicVariable to simplify calls to testModel - ST 3/4/10
  private val _workspace = new DynamicVariable[HeadlessWorkspace](null)
  def workspace = _workspace.value
  def world = _workspace.value.world


  def isModel3D(model: Model): Boolean =
    model.view.dimensions.isInstanceOf[WorldDimensions3D]

  def isModelPath3D(modelPath: String): Boolean =
    fileformat.modelVersionAtPath(modelPath).map(_.is3D).getOrElse(false)

  def arityTag(is3D: Boolean): Tag =
    if (is3D) ThreeDTag else TwoDTag


  /**
   * Test a model, expecting an error (message unspecified)
   */
  def checkError(commands: String) {
    intercept[LogoException] {
      workspace.command(commands)
    }
  }

  /**
   * runs the given model in a new workspace
   */
  def runModel(is3D: Boolean, model:Model)(f: => Unit) = {
    // very useful, do not just remove.
    // println(model.toString)
    // println(".")
    run(is3D, ws => ws.openModel(model)){ f }
  }

  /**
   * loads the model from the given file, and runs it in a new workspace
   */
  def runModelFromFile(is3D: Boolean, path: String)(f: => Unit) =
    run(is3D, ws => ws.open(path)){ f }

  /**
   * test a model, expecting an error message
   */
  def testError(f: => Unit, message:String){
    val e = intercept[LogoException]{f}
    assert(e.getMessage === message)
  }

  /**
   * test a model created in code
   */
  def testModel(testName: String, model: Model)(f: => Unit) = {
    val is3D = isModel3D(model)
    test(testName, arityTag(is3D)){ runModel(is3D, model){ f } }
  }

  /**
   * test a model loaded from a file
   */
  def testModelFile(testName: String, path: String)(f: => Unit) = {
    val is3D = isModelPath3D(path)
    test(testName, arityTag(is3D)){ runModelFromFile(is3D, path){ f } }
  }

  /**
   * test that merely opening the model raises an error
   */
  def testModelCompileError(testName: String, model: Model)(f: Throwable => Unit) = {
    val is3D = isModel3D(model)
    test(testName, arityTag(is3D)){
      val ex = intercept[Throwable]{
        runModel(is3D, model){}
      }
      f(ex)
    }
  }

  // run a model
  private def run(is3D: Boolean, openModel: HeadlessWorkspace => Unit)(f: => Unit) {
    _workspace.withValue(HeadlessWorkspace.newInstance(is3D)) {
      try {
        workspace.silent = true
        openModel(workspace)
        f
      }
      finally workspace.dispose()
    }
  }
}
