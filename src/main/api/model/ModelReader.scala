// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api.model

import org.nlogo.api
import org.nlogo.core.{Model, View}

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
