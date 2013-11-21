// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.review

import org.nlogo.mirror.AgentKey
import org.nlogo.mirror.Mirrorable
import org.nlogo.mirror.WidgetKinds.Chooser
import org.nlogo.mirror.WidgetKinds.Monitor
import org.nlogo.mirror.WidgetKinds.Slider
import org.nlogo.mirror.WidgetKinds.Switch
import org.nlogo.widget.SwitchWidget
import org.nlogo.window.ChooserWidget
import org.nlogo.window.MonitorWidget
import org.nlogo.window.SliderWidget
import org.nlogo.window.Widget

trait MirrorableWidget extends Mirrorable {
  val index: Int
  def agentKey = AgentKey(kind, index)
}

class MirrorableMonitor(monitor: MonitorWidget, val index: Int) extends MirrorableWidget {
  import Monitor.Variables._
  override def kind = Monitor
  override val variables = Map(ValueString.id -> monitor.valueString)
}

class MirrorableSwitch(switch: SwitchWidget, val index: Int) extends MirrorableWidget {
  import Switch.Variables._
  override def kind = Switch
  override val variables = Map(IsOn.id -> Boolean.box(switch.isOn))
}

class MirrorableChooser(chooser: ChooserWidget, val index: Int) extends MirrorableWidget {
  import Chooser.Variables._
  override def kind = Chooser
  override val variables = Map(ValueObject.id -> chooser.valueObject)
}

class MirrorableSlider(slider: SliderWidget, val index: Int) extends MirrorableWidget {
  import Slider.Variables._
  override def kind = Slider
  override val variables = Map(
    SliderValue.id -> Double.box(slider.value),
    Minimum.id -> Double.box(slider.constraint.minimum),
    Increment.id -> Double.box(slider.constraint.increment),
    Maximum.id -> Double.box(slider.constraint.maximum)
  )
}

object MirrorableWidgets {
  def apply(widgets: Iterable[Widget]): Iterable[MirrorableWidget] =
    widgets.zipWithIndex.collect {
      case (w: MonitorWidget, i) => new MirrorableMonitor(w, i)
      case (w: SwitchWidget, i)  => new MirrorableSwitch(w, i)
      case (w: SliderWidget, i)  => new MirrorableSlider(w, i)
      case (w: ChooserWidget, i) => new MirrorableChooser(w, i)
    }
}