package org.nlogo.properties

import scala.collection.mutable.ArrayBuffer

class MetricsBooleanEditor(accessor: PropertyAccessor[Boolean], useTooltip: Boolean,
                           properties: ArrayBuffer[PropertyEditor[_]]) extends BooleanEditor(accessor, useTooltip)
{
    override def changed(): Unit =
    {
        properties.find(_.accessor.accessString == "runMetricsCondition") match
        {
            case Some(p) => p.setEnabled(!get.getOrElse(true))
            case None =>
        }
    }
}
