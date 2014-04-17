// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api.model
import org.nlogo.api.Resource.getResourceLines
import org.nlogo.api

case class Model(code: String = "", widgets: List[Widget] = List(View()), info: String = "", version: String = "NetLogo 5.0",
  turtleShapes: List[String] = Model.defaultShapes, behaviorSpace: List[String] = Nil,
  linkShapes: List[String] = Model.defaultLinkShapes, previewCommands: List[String] = Nil) {

  def interfaceGlobals: List[String] = widgets.collect({case x:DeclaresGlobal => x}).map(_.varName)
  def constraints: Map[String, List[String]] = widgets.collect({case x:DeclaresConstraint => (x.varName, x.constraint)}).toMap
  def interfaceGlobalCommands: List[String] = widgets.collect({case x:DeclaresGlobalCommand => x}).map(_.command)

  if(widgets.collectFirst({case (w: View) => w}).isEmpty)
    throw new Exception("Every model must have at least a view...")

  def view: View = widgets.collectFirst({case (w: View) => w}).get
  def plots: List[Plot] = widgets.collect({case (w: Plot) => w})
}

object Model {
  lazy val defaultShapes: List[String] =
    getResourceLines("/system/defaultShapes.txt").toList
  lazy val defaultLinkShapes: List[String] =
    getResourceLines("/system/defaultLinkShapes.txt").toList
}

object ModelReader {

  val SEPARATOR = "@#$#@#$#@"

  def parseModel(model: String, parser: Option[api.ParserServices] = None): Model = {
    var sections = Vector[Vector[String]]()
    var sectionContents = Vector[String]()
    def sectionDone() {
      sections :+= sectionContents
      sectionContents = Vector()
    }
    for(line <- io.Source.fromString(model).getLines)
      if(line.startsWith(SEPARATOR))
        sectionDone()
      else
        sectionContents :+= line
    sectionDone()

    if(sections.size != 12)
      throw new Exception("Models must have 12 sections, this had " + sections.size)

    val Vector(code, interface, info, turtleShapes, version, previewCommands, systemDynamics,
             behaviorSpace, hubNetClient, linkShapes, modelSettings, deltaTick) = sections
    new Model(code.mkString("\n"),
              if(parser.nonEmpty) WidgetReader.readInterface(interface.toList, parser.get) else List(View()),
              info.mkString("\n"), version.head,
              turtleShapes.toList, behaviorSpace.toList, linkShapes.toList, previewCommands.toList)
  }
}
