// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.nvm

import org.nlogo.api
import api.{ SourceOwner }
import org.nlogo.core.{ Let, FrontEndProcedure, Token, Syntax }

class Procedure(
  val isReporter: Boolean,
  val nameToken: Token,
  val name: String,
  _displayName: Option[String],
  val argTokens: Seq[Token]   = Seq(),
  initialArgs: Vector[String] = Vector[String]()) extends FrontEndProcedure {

  args = initialArgs

  val fileName = nameToken.filename // used by cities include-file stuff
  val filename = fileName // alias, may not be needed
  override def procedureDeclaration = null

  lazy val displayName = buildDisplayName(_displayName)
  var pos: Int = 0
  var end: Int = 0
  var usableBy = "OTPL"
  var localsCount = 0
  private var _owner: SourceOwner = null
  val children = collection.mutable.Buffer[Procedure]()
  def isLambda = false
  var lets = Vector[Let]()

  def addLet(l: Let) = {
    lets = lets :+ l
  }

  // each Int is the position of that variable in the procedure's args list
  val alteredLets = collection.mutable.Map[Let, Int]()

  // cache args.size() for efficiency with making Activations
  var size = 0

  var code = Array[Command]()

  def parent: Procedure = null

  protected def buildDisplayName(displayName: Option[String]): String = {
    val nameAndFile =
      Option(fileName)
        .filter(_.nonEmpty)
        .map(name + " (" + _ + ")")
        .getOrElse(name)

    displayName.getOrElse("procedure " + nameAndFile)
  }

  override def syntax: Syntax = {
    val right = List.fill(args.size - localsCount)(Syntax.WildcardType)
    if (isReporter)
      Syntax.reporterSyntax(right = right, ret = Syntax.WildcardType)
    else
      Syntax.commandSyntax(right = right)
  }

  override def toString =
    super.toString + "[" + name + ":" + args.mkString("[", " ", "]") +
      ":" + usableBy + "]"

  def dump: String = {
    val buf = new StringBuilder
    if (isReporter)
      buf ++= "reporter "
    buf ++= displayName
    buf ++= ":"
    buf ++= args.mkString("[", " ", "]")
    buf ++= "{" + usableBy + "}:\n"
    for (i <- code.indices) {
      val command = code(i)
      buf ++= "[" + i + "]"
      buf ++= command.dump(3)
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
