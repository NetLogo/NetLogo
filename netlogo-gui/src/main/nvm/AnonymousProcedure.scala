// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.nvm

import org.nlogo.api, api.{ AnonymousProcedure => ApiLambda }
import org.nlogo.core.{ AgentKind, Let, I18N, Syntax }

// anonymous procedures are created by the compiler via the `->` prim,
// which may appear in user code, or may be inserted by
// ExpressionParser during parsing, when a command / reporter is known to be expected.
//
// anonymous procedure take inputs: <code>[[_1 _2 etc ] -> ... </code> these are passed using Lets.
//
// bindArgs binds formal inputs to actual inputs at runtime.  note that it's the caller's
// responsibility to ensure in advance that there will be enough actuals.  if there are extra
// actuals, they are ignored. - JC, ST 11/4/10, 2/6/11
//
// anonymous procedures may close over two kinds of variables,
// let variables and procedure parameters (aka "locals"),
// so we have storage for both of those in the anonymous procedure.

sealed trait AnonymousProcedure {
  val formals: Array[Let]  // don't mutate please! Array for efficiency
  val lets: List[LetBinding]
  val locals: Array[AnyRef]
  def checkAgentClass(context: Context, agentClassString: String): Unit = {
    val kind = context.agent.kind
    if (!(((kind == AgentKind.Observer) && agentClassString.contains('O')) ||
          ((kind == AgentKind.Turtle) && agentClassString.contains('T')) ||
          ((kind == AgentKind.Patch) && agentClassString.contains('P')) ||
          ((kind == AgentKind.Link) && agentClassString.contains('L')))) {
      val instruction = context.activation.procedure.code(context.ip)
      val allowedKinds = agentClassString.map {
        case 'O' => AgentKind.Observer
        case 'T' => AgentKind.Turtle
        case 'P' => AgentKind.Patch
        case 'L' => AgentKind.Link
      }
      throw new RuntimePrimitiveException(context, instruction,
        AbstractScalaInstruction.agentKindError(context.agent.kind, allowedKinds))
    }
  }

  def bindArgs(c: Context, args: Array[AnyRef]) {
    var i = 0
    var n = formals.size
    while(i < n) {
      c.let(formals(i), args(i))
      i += 1
    }
  }
}

object AnonymousProcedure {
  def missingInputs(lambda: ApiLambda, argCount: Int): String =
    if (lambda.syntax.minimum == 1)
      I18N.errors.get("org.nlogo.prim.lambda.missingInput")
    else
      I18N.errors.getN("org.nlogo.prim.lambda.missingInputs", lambda.syntax.minimum.toString, argCount.toString)
}

// anonymous reporters are pretty simple.  The body is simply a Reporter.
// To run it, we swap closed-over variables into the context,
// bind actuals to formals, call report(), then unswap.

case class AnonymousReporter(body: Reporter, formals: Array[Let], lets: List[LetBinding], locals: Array[AnyRef])
extends AnonymousProcedure with org.nlogo.api.AnonymousReporter {
  // anonymous reporters are allowed to take more than the number of arguments (hence repeatable-type)
  val syntax =
    Syntax.reporterSyntax(
      ret = Syntax.WildcardType,
      right = formals.map(_ => Syntax.WildcardType | Syntax.RepeatableType).toList,
      agentClassString = body.agentClassString)
  override def toString = "(anonymous reporter: [ " + body.fullSource + " ])"
  def report(context: api.Context, args: Array[AnyRef]): AnyRef =
    context match {
      case e: ExtensionContext => report(e.nvmContext, args)
      case c: Context          => report(c, args)
    }
  def report(context: Context, args: Array[AnyRef]): AnyRef = {
    checkAgentClass(context, syntax.agentClassString)
    val oldLets = context.letBindings
    val oldActivation = context.activation
    context.activation = new Activation(oldActivation.procedure, oldActivation.parent, locals, oldActivation.returnAddress)
    context.letBindings = lets
    bindArgs(context, args)
    try {
      body.report(context)
    } finally {
      context.letBindings = oldLets
      context.activation = oldActivation
    }
  }
}

// Anonymous commands are a little more complicated.  The body is a Procedure.
// To run it, we have to make a new Activation, then call runExclusive()
// on the context, finally restoring some state on the way out (including a dead-agent check).
// We may throw NonLocalExit if _report or _stop is called.

case class AnonymousCommand(procedure: Procedure, formals: Array[Let], lets: List[LetBinding], locals: Array[AnyRef])
extends AnonymousProcedure with org.nlogo.api.AnonymousCommand {
  val syntax =
    Syntax.commandSyntax(
      right = formals.map(_ => Syntax.WildcardType | Syntax.RepeatableType).toList,
      agentClassString = procedure.agentClassString)
  override def toString = procedure.displayName
  // anonymous commands are allowed to take more than the number of arguments (hence repeatable-type)
  def perform(context: api.Context, args: Array[AnyRef]) {
    context match {
      case e: ExtensionContext => perform(e.nvmContext, args)
      case c: Context          => perform(c, args)
    }
  }
  def perform(context: Context, args: Array[AnyRef]) {
    checkAgentClass(context, syntax.agentClassString)
    val oldLets = context.letBindings
    context.letBindings = lets
    bindArgs(context, args)
    val oldActivation = context.activation
    // the return address doesn't matter here since we're not actually using
    // _call and _return, we're just executing the body - ST 2/4/11
    context.activation = new Activation(procedure, oldActivation, locals, 0)
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
    // call sites may multiply in the future, and any extensions that use anonymous procedures
    // are call sites too. Sigh. - ST 3/26/12)
  }
}
