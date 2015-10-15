// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.nvm

import org.nlogo.api
import api.{ SourceOwner, Syntax, Let }
import org.nlogo.core.{ FrontEndProcedure, Token, Syntax => CoreSyntax }

class Procedure(
  val isReporter: Boolean,
  val nameToken: Token,
  val name: String,
  _displayName: Option[String],
  val parent: Procedure,
  initialArgs: Vector[String] = Vector[String]()) extends FrontEndProcedure {

  args = initialArgs
  val fileName = nameToken.filename // used by cities include-file stuff
  val filename = fileName // alias, may not be needed
  override def argTokens = Seq()
  override def procedureDeclaration = null

  val displayName = buildDisplayName(_displayName)
  var pos: Int = 0
  var end: Int = 0
  var usableBy = "OTPL"
  var localsCount = 0
  private var _owner: SourceOwner = null
  val children = collection.mutable.Buffer[Procedure]()
  def isTask = parent != null
  var lets = Vector[Let]()

  def addLet(l: Let) = {
    lets = lets :+ l
  }

  // each Int is the position of that variable in the procedure's args list
  val alteredLets = collection.mutable.Map[Let, Int]()

  // cache args.size() for efficiency with making Activations
  var size = 0

  // ExpressionParser doesn't know how many parameters the task is going to take;
  // that's determined by TaskVisitor. so for now this is mutable - ST 2/4/11
  val taskFormals = collection.mutable.Buffer[Let]()

  def getTaskFormal(n: Int, token: Token): Let = {
    while (taskFormals.size < n)
      taskFormals += new Let("?" + n, token.start, token.end)
    taskFormals.last
  }

  var code = Array[Command]()

  private def buildDisplayName(displayName: Option[String]): String =
    if (isTask)
      "(command task from: " + parent.displayName + ")"
    else {
      def nameAndFile =
        Option(fileName)
          .filter(_.nonEmpty)
          .map(name + " (" + _ + ")")
          .getOrElse(name)
      displayName.getOrElse("procedure " + nameAndFile)
    }

  override def syntax: CoreSyntax = {
    val right = Array.fill(args.size - localsCount)(Syntax.WildcardType)
    if (isReporter)
      Syntax.reporterSyntax(right, Syntax.WildcardType)
    else
      Syntax.commandSyntax(right)
  }

  override def toString =
    super.toString + "[" + name + ":" + args.mkString("[", " ", "]") +
      ":" + usableBy + "]"

  def dump: String = {
    val buf = new StringBuilder
    val indent = isTask
    if (indent)
      buf ++= "   "
    if (isReporter)
      buf ++= "reporter "
    buf ++= displayName
    if (parent != null)
      buf ++= ":" + parent.displayName
    buf ++= ":"
    buf ++= args.mkString("[", " ", "]")
    buf ++= "{" + usableBy + "}:\n"
    for (i <- code.indices) {
      if (indent)
        buf ++= "   "
      val command = code(i)
      buf ++= "[" + i + "]"
      buf ++= command.dump(if (indent) 6 else 3)
      buf ++= "\n"
    }
    for (p <- children) {
      buf ++= "\n"
      buf ++= p.dump
    }
    buf.toString
  }

  def init(workspace: Workspace) {
    size = args.size
    code.foreach(_.init(workspace))
    children.foreach(_.init(workspace))
  }

  def owner = _owner
  def owner_=(owner: SourceOwner) {
    _owner = owner
    children.foreach(_.owner = owner)
  }

}
