// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

/**
 * A host application should implement this interface in order to be notified of events occurring
 * within NetLogo.  Its methods are called by NetLogoListenerManager when these events occur.  The
 * events are typically user actions, but may also be triggered programmatically by the model.
 */

trait NetLogoListener extends java.util.EventListener {

  /**
   * Called when a model is opened.
   *
   * @param name name of the model that was opened
   */
  def modelOpened(name: String)

  /**
   * Called when the user presses a button in the NetLogo model.
   *
   * Note that the button may take some time to run and therefore may not pop back up until some
   * later.  Use <code>buttonStopped()</code> if you need to know when the button's action has
   * completed.
   */
  def buttonPressed(buttonName: String)

  /**
   * Called when a button in the NetLogo model has finished running and pops back up.
   *
   * The button may be a "once" button or a "forever" button.  If it is a "once" button, it pops
   * back up automatically when its action is completed.  If it is a "forever" button, it will pop
   * back up when clicked for a second time by the user.  (Some forever buttons may also pop back up
   * automatically, if the model is written that way.)
   */
  def buttonStopped(buttonName: String)

  /**
   * Called when the value of a slider changes.  Usually if the slider changes it is because the
   * user has changed it using the mouse, but the change may also be the result of code in the model
   * or code typed into the command center.
   */
  def sliderChanged(name: String, value: Double, min: Double, increment: Double, max: Double,
                    valueChanged: Boolean, buttonReleased: Boolean)

  /**
   * Called when the value of a switch changes.  Usually if the switch changes it is because the
   * user has changed it using the mouse, but the change may also be the result of code in the model
   * or code typed into the command center.
   */
  def switchChanged(name: String, value: Boolean, valueChanged: Boolean)

  /**
   * Called when the value of a chooser changes.  Usually if the chooser changes it is because the
   * user has changed it using the mouse, but the change may also be the result of code in the model
   * or code typed into the command center.
   */
  def chooserChanged(name: String, value: AnyRef, valueChanged: Boolean)

  /**
   * Called when the value of an input box changes.  Usually if the input box changes it is because
   * the user has changed it using the mouse, but the change may also be the result of code in the
   * model or code typed into the command center.
   */
  def inputBoxChanged(name: String, value: AnyRef, valueChanged: Boolean)

  /**
   * Called when the user enters text into the command center (and presses return).  Note that at
   * the time this method is called, the command may not have finished executing yet.
   *
   * @param owner     name of the widget that owns the code
   * @param text      the text the user entered
   * @param agentKind O, T, or P depending whether the user was
   *                  addressing the observer, turtles, or patches
   * @param errorMsg  message the user receives if there is a compiler error, or null
   */
  def commandEntered(owner: String, text: String, agentKind: Char, errorMsg: CompilerException)

  /**
   * Called when the Code tab is recompiled.
   *
   * @param text      the contents of the Code tab
   * @param errorMsg  message the user receives if there is a compiler error, or null
   */
  def codeTabCompiled(text: String, errorMsg: CompilerException)

  /**
   * Called when the tick counter changes, either by being advanced, or by being reset to 0.
   *
   * @param ticks new value of the tick counter
   */
  def tickCounterChanged(ticks: Double)

  /**
   * Called when the engine potentially schedules a view update.  "Potentially" because an actual
   * update might not take place for any number of reasons (view updates are off, the speed slider
   * is sped up, etc.).
   */
  def possibleViewUpdate()

}
