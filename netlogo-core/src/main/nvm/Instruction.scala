// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.nvm

import java.util.{ List => JList, Locale }

import org.nlogo.core.{ AgentKind, I18N, LogoList, Syntax, Token, TokenHolder }
import org.nlogo.api.{ AnonymousReporter => ApiAnonymousReporter, AnonymousCommand => ApiAnonymousCommand }
import org.nlogo.agent.{ Agent, AgentSet, AgentBit, Turtle, Patch, Link }

object Instruction {
  def agentKindDescription(kind: AgentKind): String = {
    val key =
      kind match {
        case AgentKind.Observer => "observer"
        case AgentKind.Turtle   => "turtle"
        case AgentKind.Patch    => "patch"
        case AgentKind.Link     => "link"
      }
    if (key == null) null
    else I18N.errors.get("org.nlogo.prim.$common.agentKind." + key)
  }


  def agentKindError(kind: AgentKind, allowedKinds: Seq[AgentKind]): String = {
    val kindDescription = agentKindDescription(kind)
    lazy val allowedDescription = agentKindDescription(allowedKinds.head)
    if (allowedKinds.size == 1)
      I18N.errors.getN("org.nlogo.prim.$common.invalidAgentKind.alternative", kindDescription, allowedDescription)
    else
      I18N.errors.getN("org.nlogo.prim.$common.invalidAgentKind.simple", kindDescription)
  }
}

abstract class Instruction extends InstructionJ with TokenHolder {

  var token: Token = null

  var chosenMethod: java.lang.reflect.Method = null
  var source: String = null     // contains the source of this instruction only
  var fullSource: String = null // contains the source of this instruction and all arguments

  var disassembly = new Thunk[String]() {
    override def compute() = ""
  }

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

  def throwAgentClassException(context: Context, kind: AgentKind): Nothing = {
    val pairs =
      Seq(
        (AgentKind.Observer, 'O'), (AgentKind.Turtle, 'T'),
        (AgentKind.Patch, 'P'), (AgentKind.Link, 'L'))
    val allowedKinds =
      for {
        (thisKind, c) <- pairs
        if agentClassString.contains(c)
      } yield thisKind

    throw new RuntimePrimitiveException(
      context, this, Instruction.agentKindError(kind, allowedKinds))
  }

  def init(workspace: Workspace): Unit = {
    if (workspace != null) {
      this.workspace = workspace
      world = workspace.world.asInstanceOf[org.nlogo.agent.World]
    }
    for (arg <- args)
      arg.init(workspace)
    agentBits = AgentBit.fromAgentClassString(agentClassString)
  }

  def copyFieldsFrom(sourceInstr: Instruction): Unit = {
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

  def getFilename: String =
    Option(token).map(_.filename).orNull

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
          throw new RuntimePrimitiveException(context, this,
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

  def argEvalAnonymousReporter(context: Context, index: Int): ApiAnonymousReporter =
    args(index).report(context) match {
      case t: ApiAnonymousReporter =>
        t
      case x =>
        throw new ArgumentTypeException(
          context, this, index, Syntax.ReporterType, x)
    }

  def argEvalAnonymousCommand(context: Context, index: Int): ApiAnonymousCommand =
    args(index).report(context) match {
      case t: ApiAnonymousCommand =>
        t
      case x =>
        throw new ArgumentTypeException(
          context, this, index, Syntax.CommandType, x)
    }

  def argEvalSymbol(context: Context, argIndex: Int): Token = {
    args(argIndex).report(context) match {
      case t: Token => t
      case x =>
        throw new ArgumentTypeException(
          context, this, argIndex, Syntax.SymbolType, x)
    }
  }

  def argEvalCodeBlock(context: Context, argIndex: Int): JList[Token] = {
    args(argIndex).report(context) match {
      case l: JList[Token] @unchecked => l
      case x =>
        throw new ArgumentTypeException(context, this, argIndex, Syntax.CodeBlockType, x)
    }
  }

  def dump: String = dump(3)

  def dump(indentLevel: Int): String = {
    val buf = new StringBuilder(toString)
    if (source != null) {
      buf.append(" \"")
      buf.append(fullSource)
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

  def getPositionAndLength(): Array[Int] = {
    if (token == null)
      Array(-1, 0)
    else {
      val begin = storedSourceStartPosition
      val end = storedSourceEndPosition
      Array(begin, end - begin)
    }
  }


  /*
   * This method is handled specially by MethodRipper.
   * Thus, when called by rejiggered report_X() methods, it gives the right result,
   * instead returning the displayName() of the GeneratedInstruction. ~Forrest (summer 2006)
   */
  def displayName: String =
    if (token != null) {
      token.text.toUpperCase(Locale.ENGLISH)
    } else {
      // well, returning some weird ugly internal class name
      // is better than nothing, I guess
      getClass.getSimpleName
    }

  override def toString =
    getClass.getSimpleName


  def validLong(d: Double, context: Context): Long = {
    // 9007199254740992 is the largest/smallest integer
    // exactly representable in a double - ST 1/29/08
    if (d > 9007199254740992L || d < -9007199254740992L)
      throw new RuntimePrimitiveException(context, this,
        s"$d is too large to be represented exactly as an integer in NetLogo")
    d.toLong
  }

  def newValidDouble(d: Double, context: Context): java.lang.Double = {
    if (java.lang.Double.isInfinite(d) || java.lang.Double.isNaN(d))
      invalidDouble(d, context)
    Double.box(d)
  }

  def validDouble(d: Double, context: Context): Double = {
    // yeah, this line is repeated from the previous method,
    // but factoring it out would cost us a method call, and this
    // is extremely efficiency-critical code, so... - ST 11/1/04
    if (java.lang.Double.isInfinite(d) || java.lang.Double.isNaN(d))
      invalidDouble(d, context)
    // Returning d makes it easier to insert validDouble() calls into
    // expressions without having to break those expressions up into
    // multiple statements.  The caller is free to ignore the return
    // value. - ST 11/1/04
    d
  }

  private def invalidDouble(d: Double, context: Context): Nothing =
    throw new RuntimePrimitiveException(context, this,
      "math operation produced "
        + (if (java.lang.Double.isInfinite(d))
             "a number too large for NetLogo"
           else
             "a non-number"))
}
