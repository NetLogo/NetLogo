// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compile

import org.nlogo.core.Nobody
import org.nlogo.nvm.Reporter
import org.nlogo.prim._
import org.nlogo.core.LogoList

object Literals {
  def makeLiteralReporter(value: AnyRef): Reporter =
    value match {
      case b: java.lang.Boolean => new _constboolean(b)
      case d: java.lang.Double => new _constdouble(d)
      case l: LogoList => new _constlist(l)
      case s: String => new _conststring(s)
      case Nobody => new _nobody
      case _ => new _const(value)
    }
}
