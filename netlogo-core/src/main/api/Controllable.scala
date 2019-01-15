// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import org.nlogo.core.CompilerException

/**
 * Interface provides access to the NetLogo controlling API's report and command methods found in
 * methods independent of App.app and headless.HeadlessWorkspace.  This is useful for making java
 * software that can run NetLogo in both GUI and Headless mode.
 */
trait Controllable {

  def command(source: String)

  def report(source: String): AnyRef

  @throws(classOf[java.io.IOException])
  def open(path: String, shouldAutoInstallLibs: Boolean = false): Unit

  @throws(classOf[CompilerException])
  def evaluateCommands(owner: JobOwner, source: String): Unit
  @throws(classOf[CompilerException])
  def evaluateCommands(owner: JobOwner, source: String, waitForCompletion: Boolean): Unit

  @throws(classOf[CompilerException])
  def evaluateReporter(owner: JobOwner, source: String): AnyRef
}
