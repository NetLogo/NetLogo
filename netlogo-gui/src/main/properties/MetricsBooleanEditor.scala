package org.nlogo.properties

import scala.collection.mutable.ArrayBuffer
import java.awt.Container

class MetricsBooleanEditor(accessor: PropertyAccessor[Boolean], properties: ArrayBuffer[PropertyEditor[_]]) extends BooleanEditor(accessor)
{
    private def setEnabledRecursive(component: Container, state: Boolean): Unit = {
        component.getComponents().foreach(c => {
            setEnabledRecursive(c.asInstanceOf[Container], state)
            c.setEnabled(state)
        })
    }

    override def changed(): Unit =
    {
        properties.find(_.accessor.accessString == "runMetricsCombine") match
        {
            case Some(p) => p.setEnabled(!get.getOrElse(true))
            case None =>
        }

        properties.find(_.accessor.accessString == "runMetricsConditions") match
        {
            case Some(p) => setEnabledRecursive(p, !get.getOrElse(true))
            case None =>
        }
    }
}