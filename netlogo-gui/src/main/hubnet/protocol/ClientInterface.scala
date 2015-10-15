// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.hubnet.protocol

import org.nlogo.api.{ CompilerServices, Shape}
import org.nlogo.core.LogoList

/**
 * Holds the specification for the client-side interface.
 */
@SerialVersionUID(0)
case class ClientInterface(
  // these are parsed and unparsed versions of the same strings.  ev 9/10/08
  // why the heck to we do this? why not just parse them in here?
  // why have a user parse them and pass in both? wtf? -JC 8/22/10
  widgets: Seq[Seq[String]],
  widgetDescriptions: Seq[String],
  turtleShapes: Seq[Shape],
  linkShapes: Seq[Shape],
  @transient compiler: CompilerServices,
  chooserChoices:collection.mutable.HashMap[String, LogoList] = collection.mutable.HashMap()) {

  /** Transient cache of valid tags */
  @transient private val clientWidgetTags: List[String] =
    if(widgets.isEmpty) Nil
    else
      "ALL PLOTS" :: widgets.map{ widget =>
        if (widget(0) == "VIEW") "VIEW"
        else {
          val tag = widget(5)
          if (widget(0) == "CHOOSER")
            chooserChoices(tag) = compiler.readFromString("[ " + widget(7) + " ]").asInstanceOf[LogoList]
          tag
        }
      }.toList

  def containsWidget(tag: String) = clientWidgetTags.contains(tag)
  def containsViewWidget = widgets != null && widgets.exists(w => w(0) == "VIEW")

  override def toString =
    "ClientInterface(\n\t" + "TURTLE SHAPES = " + turtleShapes + "\n\t" +
    "LINK SHAPES = " + linkShapes + "\n\t" +
    "WIDGETS = " + widgetDescriptions + ")"

}
