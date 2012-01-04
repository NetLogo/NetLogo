// (C) 2012 Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.nvm

import org.nlogo.api

// tasks are created by the _task prim, which may appear in user code, or may be inserted by
// ExpressionParser during parsing, when a task is known to be expected.
//
// tasks take inputs: ?1, ?2, etc. these are passed using Lets.
//
// bindArgs binds formal inputs to actual inputs at runtime.  note that it's the caller's
// responsibility to ensure in advance that there will be enough actuals.  if there are extra
// actuals, they are ignored. - JC, ST 11/4/10, 2/6/11
//
// tasks may close over two kinds of variables, let variables and procedure parameters (aka
// "locals"), so we have storage for both of those in the task.

sealed trait Task {
  val formals: Array[api.Let]  // don't mutate please! Array for efficiency
  val lets: List[LetBinding]
  val locals: Array[AnyRef]
  def bindArgs(c: Context, args: Array[AnyRef]) {
    var i = 0
    var n = formals.size
    while(i < n) {
      c.let(formals(i), args(i))
      i += 1
    }
  }
  def missingInputs(n: Int) = {
    val plural =
      if(formals.size == 1) ""
      else "s"
    "task expected " + formals.size + " input" + plural + ", but only got " + n
  }
}

// Reporter tasks are pretty simple.  The body is simply a Reporter.  To run it, we swap closed-over
// variables into the context, bind actuals to formals, call report(), then unswap.

case class ReporterTask(body: Reporter, formals: Array[api.Let], lets: List[LetBinding], locals: Array[AnyRef])
extends Task with org.nlogo.api.ReporterTask {
  override def toString = "(reporter task)"
  def report(context: api.Context, args: Array[AnyRef]): AnyRef =
    report(context.asInstanceOf[ExtensionContext].nvmContext, args)
  def report(context: Context, args: Array[AnyRef]): AnyRef = {
    val oldLets = context.letBindings
    val oldLocals = context.activation.args
    context.activation.args = locals
    context.letBindings = lets
    bindArgs(context, args)
    val result = body.report(context)
    context.letBindings = oldLets
    context.activation.args = oldLocals
    result
  }
}

// Command tasks are a little more complicated.  The body is a Procedure.  To run it, we have to
// make a new Activation, then call runExclusive() on the context.  We also have to check if the
// turtle died and see if a "non-local exit" occurred (namely _report or _stop).

case class CommandTask(procedure: Procedure, formals: Array[api.Let], lets: List[LetBinding], locals: Array[AnyRef])
extends Task with org.nlogo.api.CommandTask {
  override def toString = procedure.displayName
  def perform(context: api.Context, args: Array[AnyRef]) {
    perform(context.asInstanceOf[ExtensionContext].nvmContext, args)
  }
  def perform(context: Context, args: Array[AnyRef]) {
    val oldLets = context.letBindings
    context.letBindings = lets
    bindArgs(context, args)
    val oldActivation = context.activation
    // the return address doesn't matter here since we're not actually using
    // _call and _return, we're just executing the body - ST 2/4/11
    context.activation = new Activation(procedure, context.activation, 0)
    context.activation.args = locals
    context.ip = 0
    val exited =
      try { context.runExclusive(); false }
      catch {
        case NonLocalExit if oldActivation.procedure.tyype == Procedure.Type.COMMAND =>
          true
        case ex: api.LogoException =>
          // the stuff in the finally block is going to throw away some of
          // the information we need to build an accurate stack trace, so we'd
          // better do it now - ST 9/11/11
          ex.fillInStackTrace()
          throw ex
      }
      finally {
        context.finished = context.agent.id == -1
        context.activation = oldActivation
        context.letBindings = oldLets
      }
    if(exited && context.activation.procedure.tyype == Procedure.Type.COMMAND)
      context.stop()
    // note that it's up to the caller to restore context.ip
  }
}
