// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.nvm

import org.nlogo.api
import org.nlogo.api.{ AnonymousProcedure => ApiLambda }
import org.nlogo.core.{ AgentKind, I18N, Let, Syntax }

// anonymous procedures are created by the compiler via the `->` prim,
// which may appear in user code, or may be inserted by
// ExpressionParser during parsing, when a command / reporter is known to be expected.
//
// anonymous procedure take inputs: <code>[[_1 _2 etc ] -> ... </code> these are passed using Lets.
//
// anonymous procedures may close over two kinds of variables,
// let variables and procedure parameters (aka "locals"),
// so we have storage for both of those in the anonymous procedure.

sealed trait AnonymousProcedure {
  val formals: Array[Let] // don't mutate please! Array for efficiency
  val arguments: LambdaArgs
  val argsHandler: LambdaArgsHandler.Instruction
  val binding: Binding
  val locals: Array[AnyRef]

  def checkAgentClass(context: Context, agentClassString: String): Unit = {
    val kind = context.agent.kind

    if (!(((kind eq AgentKind.Observer) && agentClassString(0) == 'O') ||
          ((kind eq AgentKind.Turtle)   && agentClassString(1) == 'T') ||
          ((kind eq AgentKind.Patch)    && agentClassString(2) == 'P') ||
          ((kind eq AgentKind.Link)     && agentClassString(3) == 'L'))) {
      val instruction = context.activation.procedure.code(context.ip)
      val allowedKinds = agentClassString.collect {
        case 'O' => AgentKind.Observer
        case 'T' => AgentKind.Turtle
        case 'P' => AgentKind.Patch
        case 'L' => AgentKind.Link
      }
      throw new RuntimePrimitiveException(context, instruction,
        Instruction.agentKindError(context.agent.kind, allowedKinds))
    }
  }
}

object AnonymousProcedure {
  def missingInputs(lambda: ApiLambda, argCount: Int): String =
    if (lambda.syntax.minimum == 1)
      I18N.errors.get("org.nlogo.prim.lambda.missingInput")
    else
      I18N.errors.getN("org.nlogo.prim.lambda.missingInputs", lambda.syntax.minimum.toString, argCount.toString)

  def letBindingsToBinding(letBindings: List[LetBinding]): Binding = {
    val binding = new Binding()
    letBindings.foreach(lb => binding.let(lb.let, lb.value))
    binding
  }

  def displayString(procedureType: String, source: String): String =
    s"(anonymous $procedureType: $source)"

  // The `right` argument for AnonymousProcedure syntax. AnonymousProcedures
  // can more arguments than their arity; thus, `RepeatableType` is used.
  // `minimumOption` is used directly to control arity. This list is constructed
  // here for performance reasons. -- BCH 6/27/2017

  val rightArgs = List(Syntax.WildcardType | Syntax.RepeatableType)
}

import org.nlogo.nvm.AnonymousProcedure._

// anonymous reporters are pretty simple.  The body is simply a Reporter.
// To run it, we swap closed-over variables into the context,
// bind actuals to formals, call report(), then unswap.

case class AnonymousReporter(
  body:      Reporter,
  formals:   Array[Let],
  arguments: LambdaArgs,
  binding:   Binding,
  locals:    Array[AnyRef],
  source:    String)
  extends AnonymousProcedure with org.nlogo.api.AnonymousReporter {

  @deprecated("Provide defined arguments for the anonymous reporter", "6.2.2")
  def this(body: Reporter, formals: Array[Let], binding: Binding, locals: Array[AnyRef], source: String) = {
    this(body, formals, LambdaArgs.fromFormals(formals), binding, locals, source)
    System.err.println("Constructing Anonymous Reporters without defined arguments is deprecated, please update")
  }

  @deprecated("Construct an anonymous reporter using Binding instead of List[LetBinding]", "6.0.1")
  def this(body: Reporter, formals: Array[Let], allLets: List[LetBinding], locals: Array[AnyRef]) = {
    this(body, formals, LambdaArgs.fromFormals(formals), letBindingsToBinding(allLets), locals, "")
    System.err.println("Constructing Anonymous Reporters using a list of bindings is deprecated, please update")
  }

  val argsHandler = LambdaArgsHandler.createInstruction(arguments, body)

  // anonymous reporters are allowed to take more than the number of arguments (hence repeatable-type)
  val syntax =
    Syntax.reporterSyntax(
      ret = Syntax.WildcardType,
      defaultOption = Some(formals.length),
      minimumOption = Some(formals.length),
      right = AnonymousProcedure.rightArgs,
      agentClassString = body.agentClassString)

  override def toString =
    AnonymousProcedure.displayString("reporter", source)

  def report(context: api.Context, args: Array[AnyRef]): AnyRef =
    context match {
      case e: ExtensionContext => report(e.nvmContext, args)
      case c: Context          => report(c, args)
      case _                   => throw new Exception(s"Unexpected context: $context")
    }

  def report(context: Context, args: Array[AnyRef]): AnyRef = {
    checkAgentClass(context, syntax.agentClassString)

    val reportFormals = argsHandler.updateRuntimeArgs(formals, args)

    // We replace this to set the arguments up correctly, the return address shouldn't matter
    val oldActivation = context.activation
    context.activation = new Activation(
      oldActivation.procedure,
      oldActivation.parent,
      locals,
      oldActivation.returnAddress,
      // Since `enterScope`ed `Binding` is dropped with this new `Activation`
      // we don't actually need to call `exitScope`. -- BCH 06/28/2017
      binding.enterScope(reportFormals, args)
    )
    try {
      body.report(context)
    } finally {
      context.activation = oldActivation
    }
  }
}


// Anonymous commands are a little more complicated.  The body is a Procedure.
// To run it, we have to make a new Activation, then call runExclusive()
// on the context, finally restoring some state on the way out (including a dead-agent check).
// We may throw NonLocalExit if _report or _stop is called.

case class AnonymousCommand(
  procedure: LiftedLambda,
  formals:   Array[Let],
  arguments: LambdaArgs,
  binding:   Binding,
  locals:    Array[AnyRef],
  source:    String)
extends AnonymousProcedure with org.nlogo.api.AnonymousCommand {

  @deprecated("Provide defined arguments for the anonymous command", "6.2.2")
  def this(procedure: LiftedLambda, formals: Array[Let], binding: Binding, locals: Array[AnyRef], source: String) = {
    this(procedure, formals, LambdaArgs.fromFormals(formals), binding, locals, source)
    System.err.println("Constructing Anonymous Commands without defined arguments is deprecated, please update")
  }

  @deprecated("Construct an anonymous command using Binding instead of List[LetBinding]", "6.0.1")
  def this(procedure: LiftedLambda, formals: Array[Let], allLets: List[LetBinding], locals: Array[AnyRef]) = {
    this(procedure, formals, LambdaArgs.fromFormals(formals), letBindingsToBinding(allLets), locals, "")
    System.err.println("Constructing Anonymous Commands using a list of bindings is deprecated, please update")
  }

  val argsHandler = LambdaArgsHandler.createCommand(arguments, procedure)

  val syntax =
    Syntax.commandSyntax(
      defaultOption = Some(formals.length),
      minimumOption = Some(formals.length),
      right = AnonymousProcedure.rightArgs,
      agentClassString = procedure.agentClassString)

  override def toString =
    AnonymousProcedure.displayString("command", source)
  // anonymous commands are allowed to take more than the number of arguments (hence repeatable-type)

  def perform(context: api.Context, args: Array[AnyRef]): Unit = {
    context match {
      case e: ExtensionContext => perform(e.nvmContext, args)
      case c: Context          => perform(c, args)
      case _                   =>
    }
  }

  def perform(context: Context, args: Array[AnyRef]): Unit = {
    checkAgentClass(context, syntax.agentClassString)
    val performFormals = argsHandler.updateRuntimeArgs(formals, args)

    val oldActivation = context.activation
    // the return address doesn't matter here since we're not actually using
    // _call and _return, we're just executing the body - ST 2/4/11
    context.activation = new Activation(
      procedure,
      oldActivation,
      locals,
      0,
      // Since `enterScope`ed `Binding` is dropped with this new `Activation`
      // we don't actually need to call `exitScope`. -- BCH 06/28/2017
      binding.enterScope(performFormals, args)
    )
    context.ip = 0
    try {
      context.runExclusive()
    } catch {
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
