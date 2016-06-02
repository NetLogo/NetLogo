// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import org.nlogo.core.Syntax

sealed trait Task {
  /** Used to specify the number of arguments required to run a task.
    *
    * Note that a task may be supplied with more arguments depending
    * on the number of arguments supplied to run/runresult.
    * This only used to calculate argument count at the moment.
    * Note that tasks as created by `task` are variadic in the number of arguments
    * they accept. For instance, the task returned by `task [?1 + ?2]`
    * can be run with 10 arguments and the last 8 will be ignored.
    * The current versions only makes assertions that the number of arguments is
    * greater than the syntax minimum.
    *
    * Future versions ''may'' make greater use of the information provided by
    * `syntax`. Primitives returning tasks will want to ensure the task's syntax is
    * not more restrictive than the expected argument(s).
    */
  def syntax: Syntax
}

trait ReporterTask extends Task {
  /** Computes and reports a value
    *
    * When run by the `runresult` primitive or other primitives which take
    * reporter tasks as arguments, this is run on the Job Thread.
    * Before invoking this, the NetLogo primitives which use tasks
    * will check that the number of arguments contained in `args` is
    * at least as long as the number of arguments specified by
    * the tasks [[org.nlogo.api.Task#syntax syntax method]].
    * It is a user error to run `report` with fewer `args` than specified.
    *
    * @param c The [[org.nlogo.api.Context]] in which the task is being run.
    * @param args The arguments to the task
    * @return The value returned by the task
    */
  def report(c: Context, args: Array[AnyRef]): AnyRef
}

trait CommandTask extends Task {
  /** Performs an action
    *
    * When run by the `run` primitive, or other primitives which take command
    * tasks as arguments, this is run on the Job Thread.
    * Before invoking this, the NetLogo primitives which use tasks
    * will check that the number of arguments contained in `args` is
    * at least as long as the number of arguments specified by
    * the tasks [[org.nlogo.api.Task#syntax syntax method]].
    * It is a user error to run `perform` with fewer `args` than specified.
    *
    * @param c The [[org.nlogo.api.Context]] in which the task is being run.
    * @param args The arguments to the task
    */
  def perform(c: Context, args: Array[AnyRef])
}
