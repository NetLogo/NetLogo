// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.nvm

import org.nlogo.{ core, api },
  api.{ Task => ApiTask },
  core.{ I18N, Let, Syntax }

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
  val formals: Array[Let]  // don't mutate please! Array for efficiency
  val lets: List[LetBinding]
  val locals: Array[AnyRef]
  def bindArgs(c: Context, args: Array[AnyRef]) {
    val n = formals.size
    var i = 0
    while(i < n) {
      c.let(formals(i), args(i))
      i += 1
    }
  }
}

object Task {
  def missingInputs(task: ApiTask, argCount: Int): String =
    if (task.syntax.minimum == 1)
      I18N.errors.get("org.nlogo.prim.task.missingInput")
    else
      I18N.errors.getN("org.nlogo.prim.task.missingInputs", task.syntax.minimum.toString, argCount.toString)
}

// Reporter tasks are pretty simple.  The body is simply a Reporter.  To run it, we swap closed-over
// variables into the context, bind actuals to formals, call report(), then unswap.

case class ReporterTask(body: Reporter, formals: Array[Let], lets: List[LetBinding], locals: Array[AnyRef])
extends Task with org.nlogo.api.ReporterTask {
  def syntax =
    Syntax.reporterSyntax(
      ret = Syntax.WildcardType,
      right = formals.map(_ => Syntax.WildcardType | Syntax.RepeatableType).toList)
  override def toString = "(reporter task)"
  def report(context: api.Context, args: Array[AnyRef]): AnyRef =
    context match {
      case e: ExtensionContext => report(e.nvmContext, args)
      case c: Context          => report(c, args)
    }
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
// make a new Activation, then call runExclusive() on the context, finally restoring some state on
// the way out (including a dead-agent check).  We may throw NonLocalExit if _report or _stop is
// called.

case class CommandTask(procedure: Procedure, formals: Array[Let], lets: List[LetBinding], locals: Array[AnyRef])
extends Task with org.nlogo.api.CommandTask {
  def syntax =
    Syntax.commandSyntax(right = formals.map(_ => Syntax.WildcardType | Syntax.RepeatableType).toList)
  override def toString = procedure.displayName
  def perform(context: api.Context, args: Array[AnyRef]) {
    context match {
      case e: ExtensionContext => perform(e.nvmContext, args)
      case c: Context          => perform(c, args)
    }
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
    try context.runExclusive()
    catch {
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
    // note that it's up to the caller to restore context.ip and catch NonLocalExit.  (it would be
    // nice if that handling could be encapsulated here instead, but I couldn't figure out how to do
    // it, especially not without changing the signature of this method which we can't do until 5.1
    // without breaking extensions.  currently the only call site in the main source tree is in
    // _foreach, so it might not seem so bad to put a little extra burden on the caller, but the
    // call sites may multiply in the future, and any extensions that use command tasks are call
    // sites too. Sigh. - ST 3/26/12)
  }
}
