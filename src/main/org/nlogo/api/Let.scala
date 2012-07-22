// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

case class Let(varName: String = null,
               startPos: Int = -1,
               endPos: Int = -1,
               children: List[Let] = Nil)
