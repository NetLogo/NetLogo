// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import org.nlogo.core.ExtensionObject
import org.nlogo.core,
  core.{ Dump => CDump, LogoList, Nobody }

import scala.collection.JavaConverters._

object Dump extends CDump {

  val csv = new CSV({
    // Boxed integers are illegal Logo values, but we have legacy CSV generation code
    // that still uses them. Yuck. - ST 5/30/06, 4/16/14
    case i: java.lang.Integer =>
      i.toString
    case x =>
      logoObject(x.asInstanceOf[AnyRef], true, true)
  })

  def isKnownType(obj: AnyRef): Boolean =
    obj.isInstanceOf[java.lang.Boolean] ||
    obj.isInstanceOf[java.lang.Double] ||
    obj.isInstanceOf[String] ||
    obj.isInstanceOf[Agent] ||
    obj.isInstanceOf[AgentSet] ||
    obj.isInstanceOf[LogoList] ||
    obj.isInstanceOf[ExtensionObject] ||
    (obj eq Nobody)

  override def logoObject(obj: AnyRef, readable: Boolean, exporting: Boolean): String =
    dumpApiObject((obj, readable, exporting))

  val dumpApiObject: PartialFunction[(AnyRef, Boolean, Boolean), String] =
    dumpObject orElse {
      // We need to check this first, otherwise those who subclass from the base types
      // when defining an ExtensionObject will never have their dump(...) called
      case (eo: ExtensionObject, readable: Boolean, exporting: Boolean) =>
        // note that unless we directly call Dump.extensionObject we'll always be calling
        // reference = exporting.  I think that works since only the extension itself should
        // be calling !reference ev 2/29/08
        extensionObject(eo, readable, exporting, exporting)
      case (a: AgentSet, _, exporting: Boolean) =>
        agentset(a, exporting)
      case (a: Agent, _, exporting: Boolean) =>
        agent(a, exporting)
      case (anonProcedure: AnonymousProcedure, _, _) =>
        anonProcedure.toString
      case (null, _, _) =>
        "<null>"
      case (obj, _, _) =>
        "<" + obj.getClass.getSimpleName + ">"
    }

  def extensionObject(obj: ExtensionObject, readable: Boolean,
                      exporting: Boolean, reference: Boolean) =
    // The #{extension:type DATA}# format is treated as a literal when tokenizing
    "{{" + obj.getExtensionName + ":" + obj.getNLTypeName + " " +
      obj.dump(readable, exporting, reference) + "}}"

  // This is fugly. But it really is this complicated - ST 6/27/11
  def agentset(as: AgentSet, exporting: Boolean): String =
    if (!exporting)
      Option(as.printName).map(_.toLowerCase).getOrElse{
        val buf = new StringBuilder
        buf ++= "(agentset, " + as.count + " "
        as.kind match {
          case core.AgentKind.Turtle =>
            buf ++= "turtle"
            if (as.count != 1)
              buf += 's'
          case core.AgentKind.Link =>
            buf ++= "link"
            if (as.count != 1)
              buf += 's'
          case core.AgentKind.Patch =>
            buf ++= "patch"
            if (as.count != 1)
              buf ++= "es"
          case core.AgentKind.Observer =>
            buf ++= "observer"
        }
        buf += ')'
        buf.toString
      }
    else {
      val buf = new StringBuilder
      buf += '{'
      buf ++=
        (as.kind match {
          case core.AgentKind.Turtle =>
            Option(as.printName).map(_.toLowerCase) match {
              case None =>
                val ids = as.agents.asScala.map(_.id.toString).toSeq
                ("turtles" +: ids).mkString(" ")
              case Some("turtles") =>
                "all-turtles"
              case Some(breed) =>
                "breed " + breed
            }
          case core.AgentKind.Link =>
            Option(as.printName).map(_.toLowerCase) match {
              case None =>
                def linkString(link: Link) =
                  "[" + link.end1.id + " " + link.end2.id + " " + agentset(link.getBreed, true) + "]"
                val ids = as.agents.asScala.map(a => linkString(a.asInstanceOf[Link])).toSeq
                ("links" +: ids).mkString(" ")
              case Some("links") =>
                "all-links"
              case Some(breed) =>
                "breed " + breed
            }
          case core.AgentKind.Patch =>
            Option(as.printName).map(_.toLowerCase) match {
              case None =>
                def patchString(p: Patch) =
                  "[" + p.pxcor + " " + p.pycor + "]"
                val ids = as.agents.asScala.map(a => patchString(a.asInstanceOf[Patch])).toSeq
                ("patches" +: ids).mkString(" ")
              case Some("patches") =>
                "all-patches"
              case Some(x) =>
                x
            }
          case core.AgentKind.Observer =>
            "observer"
        })
      buf ++= "}"
      buf.toString
    }

  def agent(agent: Agent, exporting: Boolean) =
    if (agent.isInstanceOf[Observer])
      "observer"
    else if (agent.id == -1)
      "nobody"
    else {
      val (open, close) =
        if(exporting) ('{', '}')
        else ('(', ')')
      open + agent.toString + close
    }

  // not clear typeName really belongs here, but not sure where else to put it either - ST 6/27/11

  def typeName(obj: AnyRef): String =
    obj match {
      case eo: ExtensionObject =>
        eo.getNLTypeName
      case _ =>
        typeName(obj.getClass)
    }

  def typeName(clazz: Class[_]): String =
    clazz match {
      case _ if clazz eq classOf[java.lang.Boolean] =>
        "true/false"
      case _ if clazz eq classOf[java.lang.Double] =>
        "number"
      case _ if clazz eq classOf[String] =>
        "string"
      case _ if classOf[AgentSet].isAssignableFrom(clazz) =>
        "agentset"
      case _ if classOf[Turtle].isAssignableFrom(clazz) =>
        "turtle"
      case _ if classOf[Patch].isAssignableFrom(clazz) =>
        "patch"
      case _ if classOf[Link].isAssignableFrom(clazz) =>
        "link"
      case _ if classOf[Observer].isAssignableFrom(clazz) =>
        "observer"
      case _ if classOf[Agent].isAssignableFrom(clazz) =>
        "agent"
      case _ if classOf[LogoList].isAssignableFrom(clazz) =>
        "list"
      case _ if Nobody.getClass.isAssignableFrom(clazz) =>
        "nobody"
      case _ =>
        clazz.getSimpleName
    }

}
