// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import org.nlogo.core.CompilerException

/**
 * Interface provides access to the NetLogo controlling API's report and command methods found in
 * methods independent of App.app and headless.HeadlessWorkspace.  This is useful for making java
 * software that can run NetLogo in both GUI and Headless mode.
 */
trait Controllable {

  def command(source: String): Unit

  def report(source: String): AnyRef

  @throws(classOf[java.io.IOException])
  def open(path: String): Unit = open(path, false)
  def open(path: String, shouldAutoInstallLibs: Boolean): Unit

  @throws(classOf[CompilerException])
  def evaluateCommands(owner: JobOwner, source: String): Unit
  @throws(classOf[CompilerException])
  def evaluateCommands(owner: JobOwner, source: String, waitForCompletion: Boolean): Unit

  @throws(classOf[CompilerException])
  def evaluateReporter(owner: JobOwner, source: String): AnyRef

/**
 * Action to take if PlotManager.compileAllPlots returns error(s)
 * during a call to open in HeadlessWorkspace.
 * For the controlling API the default is to throw an exception.
 * For Behaviorspace we will set _plotCompilationErrorAction so that the errors are
 * are output for the first thread and ignored in subsequent threads.
 */
  private var _plotCompilationErrorAction: PlotCompilationErrorAction = PlotCompilationErrorAction.Throw

/**
 * @return  plotCompilationErrorAction  action to take if a plot compilation error occurs
*/
  def getPlotCompilationErrorAction(): PlotCompilationErrorAction = { _plotCompilationErrorAction }

/**
 *  @param plotCompilationErrorAction  action to take if a plot compilation error occurs
 *                                     Throw  - Throw the first error
 *                                     Output - Output all errors
 *                                     Ignore - Do nothing
*/
  def setPlotCompilationErrorAction(plotCompilationErrorAction: PlotCompilationErrorAction): Unit = { _plotCompilationErrorAction = plotCompilationErrorAction }



}
