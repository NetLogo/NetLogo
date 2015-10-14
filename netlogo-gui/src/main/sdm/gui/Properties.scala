// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.sdm.gui

// Property is more convenient to use from Scala than Java, thanks to named and default arguments,
// so we put all of our Properties for this package here - ST 2/23/10

import org.nlogo.api.{Property => P}
import collection.JavaConverters._

object Properties {
  val converter = Seq(
    P("nameWrapper", P.Identifier, "Name"),
    P("expressionWrapper", P.Reporter, "Expression")
  ).asJava
  val stock = Seq(
    P("nameWrapper", P.Identifier, "Name"),
    P("initialValueExpressionWrapper", P.Reporter, "Initial value"),
    P("allowNegative", P.Boolean, "Allow negative values")
  ).asJava
  val rate = Seq(
    P("nameWrapper", P.Identifier, "Name"),
    P("expressionWrapper", P.Reporter, "Expression")
  ).asJava
}
