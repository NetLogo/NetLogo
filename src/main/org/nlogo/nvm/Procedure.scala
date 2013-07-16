// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.nvm

import org.nlogo.api

class Procedure(
  val isReporter: Boolean,
  val name: String,
  val nameToken: api.Token,
  val argTokens: Seq[api.Token],
  _displayName: Option[String] = None,
  val parent: Procedure = null) {

  val filename = nameToken.filename; // used by cities include-file stuff
  val displayName = buildDisplayName(_displayName)
  var pos: Int = 0
  var end: Int = 0
  var usableBy = "OTPL"
  var localsCount = 0
  var topLevel = false
  private var _owner: api.SourceOwner = null
  val children = collection.mutable.Buffer[Procedure]()
  var args = Vector[String]()
  def isTask = parent != null

  // cache args.size() for efficiency with making Activations
  var size = 0

  // ExpressionParser doesn't know how many parameters the task is going to take;
  // that's determined by TaskVisitor. so for now this is mutable - ST 2/4/11
  val taskFormals = collection.mutable.Buffer[api.Let]()

  def getTaskFormal(n: Int, token: api.Token): api.Let = {
    while (taskFormals.size < n)
      taskFormals += api.Let("?" + n, token.start, token.end)
    taskFormals.last
  }

  var code = Array[Command]()

  private def buildDisplayName(displayName: Option[String]): String =
    if (isTask)
      "(command task from: " + parent.displayName + ")"
    else {
      def nameAndFile =
        Option(filename)
          .filter(_.nonEmpty)
          .map(name + " (" + _ + ")")
          .getOrElse(name)
      displayName.getOrElse("procedure " + nameAndFile)
    }

  def syntax: api.Syntax = {
    val right = Array.fill(args.size - localsCount)(api.Syntax.WildcardType)
    if (isReporter)
      api.Syntax.reporterSyntax(right, api.Syntax.WildcardType)
    else
      api.Syntax.commandSyntax(right)
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
  def owner_=(owner: api.SourceOwner) {
    _owner = owner
    children.foreach(_.owner = owner)
  }

}
