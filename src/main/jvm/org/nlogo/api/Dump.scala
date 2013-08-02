// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import collection.JavaConverters._

object Dump {

  private val dumper: Any => String = {
    // We don't allow Integers anymore as Logo values, but it's convenient to be able to dump them
    // in a CSV context. - ST 5/30/06
    case i: java.lang.Integer =>
      i.toString
    case x =>
      logoObject(x.asInstanceOf[AnyRef], true, true)
  }

  val csv = new CSV(dumper)

  def isKnownType(obj: AnyRef): Boolean =
    obj.isInstanceOf[java.lang.Boolean] ||
    obj.isInstanceOf[java.lang.Double] ||
    obj.isInstanceOf[String] ||
    obj.isInstanceOf[Agent] ||
    obj.isInstanceOf[AgentSet] ||
    obj.isInstanceOf[LogoList] ||
    obj.isInstanceOf[ExtensionObject] ||
    (obj eq Nobody)

  def logoObject(obj: AnyRef): String =
    logoObject(obj, false, false)

  def logoObject(obj: AnyRef, readable: Boolean, exporting: Boolean): String =
    obj match {
      // We need to check this first, otherwise those who subclass from the base types
      // when defining an ExtensionObject will never have their dump(...) called
      case eo: ExtensionObject =>
        // note that unless we directly call Dump.extensionObject we'll always be calling
        // reference = exporting.  I think that works since only the extension itself should
        // be calling !reference ev 2/29/08
        extensionObject(eo, readable, exporting, exporting)
      case i: java.lang.Integer =>
        sys.error("java.lang.Integer: " + i)
      case b: java.lang.Boolean =>
        b.toString
      case d: java.lang.Double =>
        number(d)
      case s: String =>
        if (readable)
          "\"" + StringUtils.escapeString(s) + "\""
        else s
      case a: AgentSet =>
        agentset(a, exporting)
      case a: Agent =>
        agent(a, exporting)
      case Nobody =>
        "nobody"
      case l: LogoList =>
        list(l, readable, exporting)
      case task: Task =>
        task.toString
      case null =>
        "<null>"
      case _ =>
        "<" + obj.getClass.getSimpleName + ">"
    }

  def extensionObject(obj: ExtensionObject, readable: Boolean,
                      exporting: Boolean, reference: Boolean) =
    // The #{extension:type DATA}# format is treated as a literal when tokenizing
    "{{" + obj.getExtensionName + ":" + obj.getNLTypeName + " " +
      obj.dump(readable, exporting, reference) + "}}"

  def number(obj: java.lang.Double) = {
    // If there is some more efficient way to test whether a double has no fractional part and lies
    // in IEEE 754's exactly representable range, I don't know it. - ST 5/31/06
    val d = obj.doubleValue
    val l = d.toLong
    if(l == d && l >= -9007199254740992L && l <= 9007199254740992L)
      l.toString
    else
      d.toString
  }

  def number(d: Double) = {
    val l = d.toLong
    if(l == d && l >= -9007199254740992L && l <= 9007199254740992L)
      l.toString
    else
      d.toString
  }

  def list(list: LogoList, readable: Boolean = false, exporting: Boolean = false) =
    iterator(list.scalaIterator, "[", "]", " ", readable, exporting)

  def iterator(iter: Iterator[AnyRef], prefix: String, suffix: String, delimiter: String, readable: Boolean, exporting: Boolean): String =
    iter.map(logoObject(_, readable, exporting))
      .mkString(prefix, delimiter, suffix)

  // This is fugly. But it really is this complicated - ST 6/27/11
  def agentset(as: AgentSet, exporting: Boolean): String =
    if (!exporting)
      Option(as.printName).map(_.toLowerCase).getOrElse{
        val buf = new StringBuilder
        buf ++= "(agentset, " + as.count + " "
        as.kind match {
          case AgentKind.Turtle =>
            buf ++= "turtle"
            if (as.count != 1)
              buf += 's'
          case AgentKind.Link =>
            buf ++= "link"
            if (as.count != 1)
              buf += 's'
          case AgentKind.Patch =>
            buf ++= "patch"
            if (as.count != 1)
              buf ++= "es"
          case AgentKind.Observer =>
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
          case AgentKind.Turtle =>
            Option(as.printName).map(_.toLowerCase) match {
              case None =>
                val ids = as.agents.asScala.map(_.id.toString).toSeq
                ("turtles" +: ids).mkString(" ")
              case Some("turtles") =>
                "all-turtles"
              case Some(breed) =>
                "breed " + breed
            }
          case AgentKind.Link =>
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
          case AgentKind.Patch =>
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
          case AgentKind.Observer =>
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
