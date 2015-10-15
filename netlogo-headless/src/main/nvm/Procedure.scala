// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.nvm

import org.nlogo.{ api, core },
  core.{FrontEndProcedure, Let}

import scala.collection.immutable.ListMap

class Procedure(
  val isReporter: Boolean,
  val name: String,
  val nameToken: core.Token,
  val argTokens: Seq[core.Token],
  _displayName: Option[String] = None,
  val parent: Procedure = null,
  val procedureDeclaration: core.StructureDeclarations.Procedure = null,
  val taskFormals: Array[Let] = Array()) extends FrontEndProcedure {

  val filename = nameToken.filename; // used by cities include-file stuff
  val displayName = buildDisplayName(_displayName)
  var pos: Int = 0
  var end: Int = 0
  var localsCount = 0
  private var _owner: api.SourceOwner = null
  val children = collection.mutable.Buffer[Procedure]()
  def isTask = parent != null

  // cache args.size() for efficiency with making Activations
  var size = 0

  def getTaskFormal(n: Int): Let = taskFormals(n - 1)

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

  override def toString =
    super.toString + "[" + name + ":" + args.mkString("[", " ", "]") +
      ":" + agentClassString + "]"

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
    buf ++= "{" + agentClassString + "}:\n"
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

object Procedure {
  type ProceduresMap = ListMap[String, Procedure]
  val NoProcedures = ListMap[String, Procedure]()
}
