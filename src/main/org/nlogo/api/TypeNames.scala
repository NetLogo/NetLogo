// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import Syntax._

object TypeNames {

  def aName(obj: AnyRef): String = {
    val result = name(obj)
    if (obj == org.nlogo.api.Nobody)
      result
    else
      addAOrAn(result)
  }

  def name(obj: AnyRef): String =
    obj match {
      case _: java.lang.Number =>
        name(NumberType)
      case _: java.lang.Boolean =>
        name(BooleanType)
      case _: String =>
        name(StringType)
      case _: LogoList =>
        name(ListType)
      case _: AgentSet =>
        name(AgentsetType)
      case Nobody =>
        name(NobodyType)
      case _: Turtle =>
        name(TurtleType)
      case _: Patch =>
        name(PatchType)
      case _: org.nlogo.api.Link =>
        name(LinkType)
      case _: ReporterTask =>
        name(ReporterTaskType)
      case _: CommandTask =>
        name(CommandTaskType)
      case null =>
        "null"
      case _ =>
        obj.getClass.getName
    }

  def aName(mask: Int): String =
    name(mask) match {
      case result @ ("NOBODY" | "anything") => result
      case result => addAOrAn(result)
    }

  def name(mask: Int): String = {
    var remainingMask = mask
    def subtract(bits: Int) {
      remainingMask -= (remainingMask & bits)
    }
    def compatible(bits: Int) =
      (remainingMask & bits) != 0
    subtract(RepeatableType)
    val result =
      if (compatible(ReferenceType)) {
        subtract(ReferenceType)
        "variable"
      } else if ((remainingMask & BracketedType) == BracketedType) {
        subtract(BracketedType)
        "list or block"
      } else if ((remainingMask & WildcardType) == WildcardType) {
        subtract(WildcardType)
        "anything"
      } else if ((remainingMask & Syntax.AgentType) == Syntax.AgentType) {
        subtract(Syntax.AgentType | NobodyType)
        "agent"
      } else if (compatible(NumberType)) {
        subtract(NumberType)
        "number"
      } else if (compatible(BooleanType)) {
        subtract(BooleanType)
        "TRUE/FALSE"
      } else if (compatible(StringType)) {
        subtract(StringType)
        "string"
      } else if (compatible(ListType)) {
        subtract(ListType)
        "list"
      } else if ((remainingMask & AgentsetType) == AgentsetType) {
        subtract(AgentsetType)
        "agentset"
      } else if (compatible(TurtlesetType)) {
        subtract(TurtlesetType)
        "turtle agentset"
      } else if (compatible(PatchsetType)) {
        subtract(PatchsetType)
        "patch agentset"
      } else if (compatible(LinksetType)) {
        subtract(LinksetType)
        "link agentset"
      } else if (compatible(TurtleType)) {
        subtract(TurtleType | NobodyType)
        "turtle"
      } else if (compatible(PatchType)) {
        subtract(PatchType | NobodyType)
        "patch"
      } else if (compatible(LinkType)) {
        subtract(LinkType | NobodyType)
        "link"
      } else if (compatible(ReporterTaskType)) {
        subtract(ReporterTaskType)
        "reporter task"
      } else if (compatible(CommandTaskType)) {
        subtract(CommandTaskType)
        "command task"
      } else if (compatible(NobodyType)) {
        subtract(NobodyType)
        "NOBODY"
      } else if (compatible(CommandBlockType)) {
        subtract(CommandBlockType)
        "command block"
      } else if ((remainingMask & ReporterBlockType) == ReporterBlockType) {
        subtract(ReporterBlockType)
        "reporter block"
      } else if (compatible(OtherBlockType)) {
        subtract(ReporterBlockType)
        "different kind of block"
      } else if (compatible(BooleanBlockType)) {
        subtract(BooleanBlockType)
        "TRUE/FALSE block"
      } else if (compatible(NumberBlockType)) {
        subtract(NumberBlockType)
        "number block"
      }
      else
        "(none)"
    remainingMask match {
      case 0 => result
      case  OptionalType => result + " (optional)"
      case _ => result + " or " + name(remainingMask)
    }
  }

  private def addAOrAn(str: String) =
    str.head.toUpper match {
      case 'A' | 'E' | 'I' | 'O' | 'U' =>
        "an " + str
      case _ =>
        "a " + str
    }

}
