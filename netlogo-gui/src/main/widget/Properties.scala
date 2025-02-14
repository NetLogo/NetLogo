// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.widget

// Property is more convenient to use from Scala than Java, thanks to named and default arguments,
// so we put all of our Properties for this package here - ST 2/23/10

import collection.JavaConverters._

import java.awt.GridBagConstraints

import org.nlogo.api.{ Property => P }
import org.nlogo.core.I18N

object Properties {
  implicit val i18nPrefix = I18N.Prefix("edit")

  val text = List(
    P("text", P.BigString, I18N.gui("text.text")),
    P("fontSize", P.Integer, I18N.gui("text.fontSize")),
    P("textColorLight", P.Color, I18N.gui("text.textLight"), gridWidth = GridBagConstraints.RELATIVE),
    P("textColorDark", P.Color, I18N.gui("text.textDark")),
    P("backgroundLight", P.Color, I18N.gui("text.backgroundLight"), gridWidth = GridBagConstraints.RELATIVE),
    P("backgroundDark", P.Color, I18N.gui("text.backgroundDark"))
  ).asJava
  val switch = List(
    P("nameWrapper", P.Identifier, I18N.gui("switch.globalVar"))
  ).asJava
  val dummySwitch = List(
    P("name", P.String, I18N.gui("hubnet.tag"))
  ).asJava
}
