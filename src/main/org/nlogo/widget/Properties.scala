// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.widget

// Property is more convenient to use from Scala than Java, thanks to named and default arguments,
// so we put all of our Properties for this package here - ST 2/23/10

import collection.JavaConverters._
import org.nlogo.api.{ I18N, Property => P }

object Properties {
  implicit val i18nPrefix = I18N.Prefix("edit")

  val text = List(
    P("text", P.BigString, I18N.gui("text.text")),
    P("fontSize", P.Integer, I18N.gui("text.fontSize")),
    P("transparency", P.Boolean, I18N.gui("text.transparency")),
    P("color", P.Color, I18N.gui("text.color"))
  ).asJava
  val switch = List(
    P("nameWrapper", P.Identifier, I18N.gui("switch.globalVar"))
  ).asJava
  val dummySwitch = List(
    P("name", P.String, I18N.gui("hubnet.tag"))
  ).asJava
}
