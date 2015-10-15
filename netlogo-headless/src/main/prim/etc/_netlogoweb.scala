// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import
  java.lang.{ Boolean => JBoolean }

import
  org.nlogo.nvm.{ Context, Reporter }

class _netlogoweb extends Reporter {
  override def report(context: Context) = JBoolean.FALSE
}
