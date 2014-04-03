// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import org.nlogo.core, core.Syntax, Syntax._

object TypeNames {

  def aName(obj: AnyRef): String = {
    val result = name(obj)
    if (obj == org.nlogo.api.Nobody)
      result
    else
      core.TypeNames.addAOrAn(result)
  }

  def name(obj: AnyRef): String =
    obj match {
      case _: java.lang.Number =>
        core.TypeNames.name(NumberType)
      case _: java.lang.Boolean =>
        core.TypeNames.name(BooleanType)
      case _: String =>
        core.TypeNames.name(StringType)
      case _: LogoList =>
        core.TypeNames.name(ListType)
      case _: AgentSet =>
        core.TypeNames.name(AgentsetType)
      case Nobody =>
        core.TypeNames.name(NobodyType)
      case _: Turtle =>
        core.TypeNames.name(TurtleType)
      case _: Patch =>
        core.TypeNames.name(PatchType)
      case _: org.nlogo.api.Link =>
        core.TypeNames.name(LinkType)
      case _: ReporterTask =>
        core.TypeNames.name(ReporterTaskType)
      case _: CommandTask =>
        core.TypeNames.name(CommandTaskType)
      case null =>
        "null"
      case _ =>
        obj.getClass.getName
    }

  def getTypeConstant(clazz: Class[_]): Int =
    if (classOf[Agent].isAssignableFrom(clazz))
      AgentType
    else if (classOf[AgentSet].isAssignableFrom(clazz))
      AgentsetType
    else if (classOf[LogoList].isAssignableFrom(clazz))
      ListType
    else if (classOf[Turtle].isAssignableFrom(clazz))
      TurtleType
    else if (classOf[Patch].isAssignableFrom(clazz))
      PatchType
    else if (classOf[Link].isAssignableFrom(clazz))
      LinkType
    else if (classOf[ReporterTask].isAssignableFrom(clazz))
      ReporterTaskType
    else if (classOf[CommandTask].isAssignableFrom(clazz))
      CommandTaskType
    else if (classOf[String].isAssignableFrom(clazz))
      StringType
    else if (classOf[java.lang.Double].isAssignableFrom(clazz) || clazz == java.lang.Double.TYPE)
      NumberType
    else if (classOf[java.lang.Boolean].isAssignableFrom(clazz) || clazz == java.lang.Boolean.TYPE)
      BooleanType
    else if (classOf[AnyRef] eq clazz)
      WildcardType
    else
      // Sorry, probably should handle this better somehow.  ~Forrest (2/16/2007)
      throw new IllegalArgumentException(
        "no Syntax type constant found for " + clazz)

}
