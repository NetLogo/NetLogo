// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import scala.collection.mutable.ArrayBuffer

class MetricsBooleanEditor(accessor: PropertyAccessor[Boolean], properties: ArrayBuffer[PropertyEditor[_]])
  extends BooleanEditor(accessor) {

  override def changed(): Unit =
  {
    properties.find(_.accessor.accessString == "runMetricsCondition") match
    {
      case Some(p) => p.setEnabled(!get.getOrElse(true))
      case None =>
    }
  }
}
