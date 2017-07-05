// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.nvm

import org.nlogo.{ api, core },
  core.{ FrontEndProcedure, StructureDeclarations, Syntax, Token },
  api.SourceOwner

import scala.collection.immutable.ListMap

object Procedure {
  type ProceduresMap = ListMap[String, Procedure]
  val NoProcedures = ListMap[String, Procedure]()
}

class Procedure(
  val isReporter:           Boolean,
  val name:                 String,
  val nameToken:            Token,
  val argTokens:            Seq[Token],
  val procedureDeclaration: StructureDeclarations.Procedure,
  val baseDisplayName:      Option[String] = None) extends ProcedureJ with FrontEndProcedure {

    args = argTokens.map(_.text).toVector

    def this(p: FrontEndProcedure) =
      this(
        p.isReporter, p.name, p.nameToken, p.argTokens, p.procedureDeclaration,
        if (p.displayName == "") None else Some(p.displayName))

    val filename = nameToken.filename
    val isLambda = false

    var pos: Int = 0
    var end: Int = 0
    var localsCount = 0
    var size = 0
    var owner: SourceOwner = null

    // This is filled in by SourceTagger
    var displayName = baseDisplayName.getOrElse("")

    protected var children = collection.mutable.Buffer[Procedure]()

    def code = _code
    def code_=(newCode: Array[Command]): Unit = {
      _code = newCode
    }

    def addChild(p: org.nlogo.nvm.Procedure): Unit = {
      children += p
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
        ":" + agentClassString + "]"

    def init(workspace: org.nlogo.nvm.Workspace): Unit = {
      size = args.size
      code.foreach(_.init(workspace))
      children.foreach(_.init(workspace))
    }

    protected def buildDisplayName(displayName: Option[String]): String = {
      val nameAndFile =
        Option(filename)
          .filter(_.nonEmpty)
          .map(name + " (" + _ + ")")
          .getOrElse(name)

      displayName.getOrElse("procedure " + nameAndFile)
    }

    def dump: String = {
      val buf = new StringBuilder
      if (isReporter)
        buf ++= "reporter "
      buf ++= displayName
      buf ++= ":"
      buf ++= args.mkString("[", " ", "]")
      buf ++= "{" + agentClassString + "}:\n"
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
}
