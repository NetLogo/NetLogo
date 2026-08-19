// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core

import java.util.Base64

object BundledInclude {
  def fromXML(element: XMLElement): Option[BundledInclude] = {
    element match {
      case XMLElement("include", attributes, contents, _) =>
        attributes.get("key").map(BundledInclude(_, new String(Base64.getDecoder.decode(contents))))

      case _ =>
        None
    }
  }
}

case class BundledInclude(key: String, contents: String) {
  def toXML: XMLElement =
    XMLElement("include", Map("key" -> key), Base64.getEncoder.encodeToString(contents.getBytes), Seq())
}
