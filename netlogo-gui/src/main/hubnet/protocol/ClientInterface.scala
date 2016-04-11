// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.hubnet.protocol

import org.nlogo.core.{ LogoList, NamedWidget, Shape, Chooser => CoreChooser,
  Monitor => CoreMonitor, View => CoreView, Widget => CoreWidget }
import org.nlogo.api.CompilerServices

/**
 * Holds the specification for the client-side interface.
 */
@SerialVersionUID(0)
case class ClientInterface(
  // these are parsed and unparsed versions of the same strings.  ev 9/10/08
  // why the heck to we do this? why not just parse them in here?
  // why have a user parse them and pass in both? wtf? -JC 8/22/10
  widgets: Seq[CoreWidget],
  turtleShapes: Seq[Shape],
  linkShapes: Seq[Shape],
  chooserChoices:collection.mutable.HashMap[String, LogoList] = collection.mutable.HashMap()) {

  println("initializing client interface")
  println(widgets)
  /** Transient cache of valid tags */
  @transient private val clientWidgetTags: List[String] =
    if (widgets.isEmpty) Nil
    else
      "ALL PLOTS" :: widgets.map {
        case v: CoreView => "VIEW"
        case c: CoreChooser =>
          chooserChoices(c.varName) = LogoList(c.choices.map(_.value): _*)
          c.display.getOrElse("")
        case m: CoreMonitor => m.display.getOrElse("")
        case w: NamedWidget => w.varName
        case w: CoreWidget  => w.getClass.getSimpleName.toUpperCase
      }.toList

  def containsWidget(tag: String) = clientWidgetTags.contains(tag)

  def containsViewWidget = widgets != null && widgets.exists(_.isInstanceOf[CoreView])

  override def toString =
    "ClientInterface(\n\t" + "TURTLE SHAPES = " + turtleShapes + "\n\t" +
    "LINK SHAPES = " + linkShapes + "\n\t" +
    "WIDGETS = " + widgets.mkString("\n") + ")"

}
