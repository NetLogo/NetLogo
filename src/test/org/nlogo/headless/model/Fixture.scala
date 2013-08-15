// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless
package model

import scala.util.DynamicVariable
import org.scalatest.Assertions
import org.nlogo.api,
  api.{ LogoException, Version },
  api.ModelCreator._

/**
 * A DSL for testing models.
 */
trait Fixture extends Assertions {

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

  // runs the given model in a new workspace
  def runModel(model:Model)(f: => Unit) = {
    run(ws => ws.openFromSource(model.toString)){ f }
  }
  // run a model
  private def run(openModel: HeadlessWorkspace => Unit)(f: => Unit) {
    _workspace.withValue(HeadlessWorkspace.newInstance) {
      try {
        workspace.silent = true
        openModel(workspace)
        f
      }
      finally workspace.dispose()
    }
  }

  def checkError(commands: String) {
    intercept[LogoException] {
      workspace.command(commands)
    }
    workspace.clearLastLogoException()
  }
}
