// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core

trait Dump {
  def logoObject(obj: AnyRef): String =
    logoObject(obj, false, false)

  def logoObject(obj: AnyRef, readable: Boolean, exporting: Boolean): String =
    dumpObject((obj, readable, exporting))

  val dumpObject: PartialFunction[(AnyRef, Boolean, Boolean), String] = {
    case (b: java.lang.Boolean, _, _) =>
      b.toString
    case (d: java.lang.Double, _, _) =>
      number(d)
    case (i: java.lang.Integer, _, _) =>
      throw new IllegalArgumentException("java.lang.Integer: " + i)
    case (s: String, readable: Boolean, _) =>
      if (readable)
        "\"" + StringEscaper.escapeString(s) + "\""
      else s
    case (Nobody, _, _) =>
      "nobody"
    case (l: LogoList, readable: Boolean, exporting: Boolean) =>
      list(l, readable, exporting)
  }

  def number(d: Double) = {
    val l = d.toLong
    if(l == d && l >= -9007199254740992L && l <= 9007199254740992L)
      l.toString
    else
      d.toString
  }

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

  def list(list: LogoList, readable: Boolean = false, exporting: Boolean = false) =
    iterator(list.scalaIterator, "[", "]", " ", readable, exporting)

  def iterator(iter: Iterator[AnyRef], prefix: String, suffix: String, delimiter: String, readable: Boolean, exporting: Boolean): String =
    iter.map(logoObject(_, readable, exporting))
      .mkString(prefix, delimiter, suffix)
}

object Dump extends Dump
