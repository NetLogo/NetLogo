// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import org.nlogo.core.Syntax

sealed trait AnonymousProcedure {
  /** Used to specify the number of arguments required to run an anonymous procedure.
    *
    * Note that an anonymous procedure may be supplied with more arguments
    * depending on the number of arguments supplied to run/runresult.
    * This only used to calculate argument count at the moment.
    * Note that anonymous procedures as created by `->` are
    * variadic in the number of arguments they accept. For
    * instance, the anonymous procedure returned by <code>[[x y] -> x + y]</code>
    * can be run with 10 arguments and the last 8 will be ignored.
    * The current versions only makes assertions that the number of arguments is
    * greater than the syntax minimum.
    *
    * Future versions ''may'' make greater use of the information provided by
    * `syntax`. Primitives returning anonymous procedures will want to ensure the
    * anonymous procedure's syntax is not more restrictive than the expected argument(s).
    */
  def syntax: Syntax
}

trait AnonymousReporter extends AnonymousProcedure {
  /** Computes and reports a value
    *
    * When run by the `runresult` primitive or other primitives which take
    * anonymous reporters as arguments, this is run on the Job Thread.
    * Before invoking this, the NetLogo primitives which use anonymous reporters
    * will check that the number of arguments contained in `args` is
    * at least as long as the number of arguments specified by
    * the tasks [[org.nlogo.api.AnonymousProcedure#syntax syntax method]].
    * It is a user error to run `report` with fewer `args` than specified.
    *
    * @param c The [[org.nlogo.api.Context]] in which the reporter is being run.
    * @param args The arguments to the reporter
    * @return The value returned by the reporter
    */
  def report(c: Context, args: Array[AnyRef]): AnyRef
}

trait AnonymousCommand extends AnonymousProcedure {
  /** Performs an action
    *
    * When run by the `run` primitive, or other primitives which take
    * anonymous commands as arguments, this is run on the Job Thread.
    * Before invoking this, the NetLogo primitives which use anonymous
    * commands will check that the number of arguments contained
    * in `args` is at least as long as the number of arguments specified by
    * the anonymous command's [[org.nlogo.api.AnonymousProcedure#syntax syntax method]].
    * It is a user error to run `perform` with fewer `args` than specified.
    *
    * @param c The [[org.nlogo.api.Context]] in which the command is being run.
    * @param args The arguments to the command
    */
  def perform(c: Context, args: Array[AnyRef])
}
