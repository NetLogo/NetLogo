package org.nlogo.hubnet.protocol

import org.nlogo.api.WidgetIO.{PlotSpec, ChooserSpec, ViewSpec, WidgetSpec}
import org.nlogo.api.{WidgetIO, CompilerServices, LogoList, Shape}

@SerialVersionUID(0)
case class ClientInterface(widgets: List[WidgetSpec],
                           turtleShapes: List[Shape],
                           linkShapes: List[Shape]) extends Serializable {
  def containsWidget(tag: String) = {
    def widgetNames(widgetSpecs: Iterable[WidgetSpec]): Iterable[String] = {
      import WidgetIO._
      (for (w <- widgetSpecs.toList) yield w match {
        case b: ButtonSpec => b.displayName.getOrElse(b.source)
        case s: SliderSpec => s.name
        case m: MonitorSpec => m.displayName.getOrElse(m.source.getOrElse(throw new IllegalStateException("bad monitor")))
        case s: SwitchSpec => s.name
        case c: ChooserSpec => c.name
        case o: OutputSpec => "OUTPUT"
        case i: InputBoxSpec => i.name
        case n: NoteSpec => "NOTE"
        case p: PlotSpec => p.name
        case v: ViewSpec => "VIEW"
      })
    }
    (tag == "ALL PLOTS" &&  widgets.exists(_.isInstanceOf[PlotSpec])) || widgetNames(widgets).exists(_==tag)
  }
  def containsViewWidget = widgets.exists(_.isInstanceOf[ViewSpec])
  def chooserChoices(compiler: CompilerServices): Map[String, LogoList] = {
    widgets.collect{ case c: ChooserSpec => c }.map{ c =>
      (c.name, compiler.readFromString("[" + c.choices + "]").asInstanceOf[LogoList])
    }.toMap
  }
}


