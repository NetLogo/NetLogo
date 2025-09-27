// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.hubnet.protocol

import org.nlogo.api.HubNetInterface.ClientInterface
import org.nlogo.core.{ LogoList, NamedWidget, Shape, Chooser => CoreChooser,
  Monitor => CoreMonitor, Plot => CorePlot, View => CoreView, Widget => CoreWidget }

@SerialVersionUID(1)
case class ComputerInterface(
  widgets: Seq[CoreWidget],
  turtleShapes: Seq[Shape],
  linkShapes: Seq[Shape],
  chooserChoices:collection.mutable.HashMap[String, LogoList] = collection.mutable.HashMap()) extends ClientInterface {

    /** Transient cache of valid tags */
  @transient private val clientWidgetTags: List[String] =
    if (widgets.isEmpty) Nil
    else
      "ALL PLOTS" :: widgets.map {
        case v: CoreView => "VIEW"
        case c: CoreChooser =>
          chooserChoices(c.varName) = LogoList(c.choices.map(_.value)*)
          c.display.getOrElse("")
        case m: CoreMonitor => m.display.getOrElse("")
        case w: NamedWidget => w.varName
        case p: CorePlot    => p.display.getOrElse("")
        case w: CoreWidget  => w.getClass.getSimpleName.toUpperCase
      }.toList

  def containsWidgetTag(tag: String) = clientWidgetTags.contains(tag)

  def containsViewWidget = widgets != null && widgets.exists(_.isInstanceOf[CoreView])

  override def toString =
    "ClientInterface(\n\t" + "TURTLE SHAPES = " + turtleShapes + "\n\t" +
    "LINK SHAPES = " + linkShapes + "\n\t" +
    "WIDGETS = " + widgets.mkString("\n") + ")"
}
