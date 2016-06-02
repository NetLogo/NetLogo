// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.nvm

import org.nlogo.api.{ CommandTask => ApiCommandTask, ReporterTask => ApiReporterTask }
import org.nlogo.core.{ AgentKind, I18N, LogoList, Syntax, Token, TokenHolder }
import org.nlogo.agent.{ Agent, AgentSet, AgentBit, Turtle, Patch, Link, World }

object Instruction {
  def agentKindDescription(kind: AgentKind): String =
    kind match {
      case AgentKind.Observer =>
        "the observer"
      case AgentKind.Turtle =>
        "a turtle"
      case AgentKind.Patch =>
        "a patch"
      case AgentKind.Link =>
        "a link"
      case _ =>
        null
    }
}

abstract class Instruction extends InstructionJ with TokenHolder {

  var token: Token = null

  /// the bytecode generator uses these to store text for dump() to print

  var chosenMethod: java.lang.reflect.Method = null
  var source: String = null     // contains the source of this instruction only
  var fullSource: String = null // contains the source of this instruction and all arguments
  var disassembly = new Thunk[String]() {
    override def compute = ""
  }

  /// store frequently used stuff where it's fast to get at

  def init(workspace: Workspace) {
    if (workspace != null) {
      this.workspace = workspace
      world = workspace.world.asInstanceOf[World]
    }
    for (arg <- args)
      arg.init(workspace)
    agentBits = AgentBit.fromAgentClassString(agentClassString)
  }

  /// bytecode generator stuff

  def copyFieldsFrom(sourceInstr: Instruction) {
    this.workspace = sourceInstr.workspace
    this.world = sourceInstr.world
    token = sourceInstr.token
  }

  def copyMetadataFrom(srcInstr: Instruction): Unit = {
    token = srcInstr.token
    this.agentClassString = srcInstr.agentClassString
    this.source = srcInstr.source
    this.fullSource = srcInstr.fullSource
    this.storedSourceStartPosition = srcInstr.storedSourceStartPosition
    this.storedSourceEndPosition = srcInstr.storedSourceEndPosition
  }

  // overridden by GeneratedInstruction
  def extractErrorInstruction(ex: EngineException): Instruction =
    this

  // These methods are for evaluating arguments --
  // they serve the same sort of purpose that the "reportX()" methods
  // in Reporter used to serve.  ~Forrest(11/10/2006)
  //
  // Convenience methods that do type checking and casting

  def argEvalBooleanValue(context: Context, index: Int): Boolean =
    argEvalBoolean(context, index).booleanValue

  def argEvalBoolean(context: Context, index: Int): java.lang.Boolean =
    args(index).report(context) match {
      case b: java.lang.Boolean =>
        b
      case x =>
        throw new ArgumentTypeException(
          context, this, index, Syntax.BooleanType, x)
    }

  def argEvalDoubleValue(context: Context, index: Int): Double =
    argEvalDouble(context, index).doubleValue

  def argEvalDouble(context: Context, index: Int): java.lang.Double =
    args(index).report(context) match {
      case d: java.lang.Double =>
        d
      case x =>
        throw new ArgumentTypeException(
          context, this, index, Syntax.NumberType, x)
    }

  def argEvalIntValue(context: Context, index: Int): Int =
    argEvalDouble(context, index).intValue

  def argEvalString(context: Context, index: Int): String =
    args(index).report(context) match {
      case s: String =>
        s
      case x =>
        throw new ArgumentTypeException(
          context, this, index, Syntax.StringType, x)
    }

  def argEvalList(context: Context, index: Int): LogoList =
    args(index).report(context) match {
      case l: LogoList =>
        l
      case x =>
        throw new ArgumentTypeException(
          context, this, index, Syntax.ListType, x)
    }

  def argEvalAgent(context: Context, index: Int): Agent =
    args(index).report(context) match {
      case a: Agent =>
        if (a.id == -1)
          throw new EngineException(context, this,
            I18N.errors.getN("org.nlogo.$common.thatAgentIsDead", a.classDisplayName))
        a
      case x =>
        throw new ArgumentTypeException(
          context, this, index, Syntax.AgentType, x)
    }

  def argEvalTurtle(context: Context, index: Int): Turtle =
    args(index).report(context) match {
      case t: Turtle =>
        t
      case x =>
        throw new ArgumentTypeException(
          context, this, index, Syntax.TurtleType, x)
    }

  def argEvalPatch(context: Context, index: Int): Patch =
    args(index).report(context) match {
      case p: Patch =>
        p
      case x =>
        throw new ArgumentTypeException(
          context, this, index, Syntax.PatchType, x)
    }

  def argEvalLink(context: Context, index: Int): Link =
    args(index).report(context) match {
      case l: Link =>
        l
      case x =>
        throw new ArgumentTypeException(
          context, this, index, Syntax.LinkType, x)
    }

  def argEvalAgentSet(context: Context, index: Int): AgentSet =
    args(index).report(context) match {
      case s: AgentSet =>
        s
      case x =>
        throw new ArgumentTypeException(
          context, this, index, Syntax.AgentsetType, x)
    }

  def argEvalAgentSet(context: Context, index: Int, kind: AgentKind): AgentSet =
    args(index).report(context) match {
      case s: AgentSet =>
        if (s.kind != kind)
          throw new ArgumentTypeException(
            context, this, index, Syntax.getAgentSetMask(kind), s)
        s
      case x =>
        throw new ArgumentTypeException(
          context, this, index, Syntax.AgentsetType, x)
    }

  def argEvalReporterTask(context: Context, index: Int): ApiReporterTask =
    args(index).report(context) match {
      case t: ApiReporterTask =>
        t
      case x =>
        throw new ArgumentTypeException(
          context, this, index, Syntax.ReporterTaskType, x)
    }

  def argEvalCommandTask(context: Context, index: Int): ApiCommandTask =
    args(index).report(context) match {
      case t: ApiCommandTask =>
        t
      case x =>
        throw new ArgumentTypeException(
          context, this, index, Syntax.CommandTaskType, x)
    }

  ///

  def dump(indentLevel: Int = 3): String = {
    val buf = new StringBuilder(toString)
    if (source != null) {
      buf.append(" \"")
      buf.append(source)
      buf.append('"')
    }
    if (chosenMethod != null) {
      buf.append(' ')
      buf.append(describeMethod(chosenMethod))
    }
    if (args.nonEmpty) {
      buf.append('\n')
      for (i <- args.indices) {
        buf.append(" " * indentLevel * 2)
        buf.append(args(i).dump(indentLevel + 1))
        if (i < args.length - 1)
          buf.append('\n')
      }
    }
    buf.toString
  }

  private def describeMethod(m: java.lang.reflect.Method): String = {
    def shortClassName(s: String): String =
      s.split("\\.").last
    val types = m.getParameterTypes
    val buf = new StringBuilder
    buf.append(
      types.tail
        .map(tpe => shortClassName(tpe.getName))
        .mkString(","))
    if (types.size > 1)
      buf.append(' ')
    buf.append("=> ")
    buf.append(shortClassName(m.getReturnType.getName))
    buf.toString
  }

  /// display stuff

  // Note:  We are not caching these for efficiency
  // (that would make no sense!).  Instead, the reason we are
  // caching these start/end positions, is because the Inliner
  // phase of the compiler may copy instruction to new locations,
  // but we want all the Instructions to precompute their correct
  // start and end positions before the Inliner goes to work.
  // ~Forrest (10/12/2006)
  var storedSourceStartPosition = -1
  var storedSourceEndPosition = -1

  // We want this information for creating some error messages
  // (particularly ArgumentTypeExceptions) ~Forrest (11/10/2006)
  var storedSourceSnippet = ""

  /*
   * This method is handled specially by MethodRipper.
   * Thus, when called by rejiggered report_X() methods, it gives the right result,
   * instead returning the displayName() of the GeneratedInstruction. ~Forrest (summer 2006)
   */
  def displayName: String =
    if (token != null)
      token.text.toUpperCase
    else
      // well, returning some weird ugly internal class name
      // is better than nothing, I guess
      getClass.getSimpleName

  override def toString =
    getClass.getSimpleName

  def sourceStartPosition(): Int =
    if (storedSourceStartPosition > -1)
      storedSourceStartPosition
    else if (token == null)
      -1
    else {
      var begin = token.start
      for (arg <- args)
        if (arg.token != null) {
          val argBegin = arg.sourceStartPosition
          begin = begin min argBegin
        }
      storedSourceStartPosition = begin
      begin
    }

  def sourceEndPosition: Int =
    if (storedSourceEndPosition > -1)
      storedSourceEndPosition
    else if (token == null)
      -1
    else {
      var end = token.end
      for (arg <- args)
        if (arg.token != null) {
          val argEnd = arg.sourceEndPosition
          end = end max argEnd
        }
      storedSourceEndPosition = end
      end
    }

  /// checking of numeric types

  def validLong(d: Double): Long = {
    // 9007199254740992 is the largest/smallest integer
    // exactly representable in a double - ST 1/29/08
    if (d > 9007199254740992L || d < -9007199254740992L)
      throw new EngineException(null, this,
        s"$d is too large to be represented exactly as an integer in NetLogo")
    d.toLong
  }

  def newValidDouble(d: Double): java.lang.Double = {
    if (java.lang.Double.isInfinite(d) || java.lang.Double.isNaN(d))
      invalidDouble(d)
    Double.box(d)
  }

  def validDouble(d: Double): Double = {
    // yeah, this line is repeated from the previous method,
    // but factoring it out would cost us a method call, and this
    // is extremely efficiency-critical code, so... - ST 11/1/04
    if (java.lang.Double.isInfinite(d) || java.lang.Double.isNaN(d))
      invalidDouble(d)
    // Returning d makes it easier to insert validDouble() calls into
    // expressions without having to break those expressions up into
    // multiple statements.  The caller is free to ignore the return
    // value. - ST 11/1/04
    d
  }

  private def invalidDouble(d: Double): Nothing =
    // it's hard to get a context here in some situations because
    // of optimizations. the context will get set later.
    throw new EngineException(null, this,
      "math operation produced "
        + (if (java.lang.Double.isInfinite(d))
             "a number too large for NetLogo"
           else
             "a non-number"))
  def throwAgentClassException(context: Context, kind: AgentKind): Nothing = {
   val pairs =
     Seq(
       (AgentKind.Observer, 'O'), (AgentKind.Turtle, 'T'),
       (AgentKind.Patch, 'P'), (AgentKind.Link, 'L'))
   val allowedKinds =
     for {
       (kind, c) <- pairs
       if agentClassString.contains(c)
     }
     yield kind
   throw new EngineException(context, this,
     "this code can't be run by " + Instruction.agentKindDescription(kind) +
     (if (allowedKinds.size == 1)
        ", only " + Instruction.agentKindDescription(allowedKinds.head)
      else
        ""))
  }
}
