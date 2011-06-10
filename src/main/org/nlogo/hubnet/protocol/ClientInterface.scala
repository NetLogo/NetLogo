package org.nlogo.hubnet.protocol 

import org.nlogo.api.{LogoList, CompilerServices, Shape}

/**
 * Holds the specification for the client-side interface. 
 */
@SerialVersionUID(0)
class ClientInterface(
  // these are parsed and unparsed versions of the same strings.  ev 9/10/08
  // why the heck to we do this? why not just parse them in here?
  // why have a user parse them and pass in both? wtf? -JC 8/22/10
  var widgets: Seq[Seq[String]],
  var widgetDescriptions: Seq[String],
  var turtleShapes: Seq[Shape],
  var linkShapes: Seq[Shape],
  @transient compiler: CompilerServices) extends Serializable {

  // this is pretty lame (old comment)
  // its also lame that its mutable and a var...its only a var for serialization JC - 9/28/10
  var chooserChoices = collection.mutable.HashMap[String, LogoList]()

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

  /**
   * No-arg constructor for deserialization. 
   */
  def this() = this(null, null, null, null, null)

  /**
   * Returns true if a tag describes a widget in this interface. 
   */
  def containsWidget(tag: String) = clientWidgetTags.contains(tag)

  /**
   * Returns true if this interface contains a view widget. 
   */
  def containsViewWidget = widgets != null && widgets.exists(w => w(0) == "VIEW")

  override def toString =
    "ClientInterface(\n\t" + "TURTLE SHAPES = " + turtleShapes + "\n\t" +
    "LINK SHAPES = " + linkShapes + "\n\t" +
    "WIDGETS = " + widgetDescriptions + ")"

  @throws(classOf[java.io.IOException])
  private def writeObject(out: java.io.ObjectOutputStream) {
    List[Any](widgets, widgetDescriptions, turtleShapes.toList, linkShapes.toList, chooserChoices.toMap).foreach(out.writeObject)
  }

  @throws(classOf[java.io.IOException])
  @throws(classOf[ClassNotFoundException])
  private def readObject(in: java.io.ObjectInputStream) {
    widgets = in.readObject().asInstanceOf[Seq[Seq[String]]]
    widgetDescriptions = in.readObject().asInstanceOf[Seq[String]]
    turtleShapes = in.readObject().asInstanceOf[Seq[Shape]]
    linkShapes = in.readObject().asInstanceOf[Seq[Shape]]
    chooserChoices = collection.mutable.HashMap[String, LogoList]()
    chooserChoices ++= in.readObject().asInstanceOf[Map[String, LogoList]]
  }
}
