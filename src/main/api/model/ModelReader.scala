// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api.model

import org.nlogo.api
import org.nlogo.core.{Model, View}

object ModelReader {

  val SEPARATOR = "@#$#@#$#@"

  // The optional parser services here can be omitted if you would not like widget parsing
  // This should really be changed in the future to not need a compiler to parse widgets, but that is not
  // a things for today.  FD 4/17/14
  def parseModel(model: String, parser: Option[api.ParserServices]): Model = {
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
      throw new RuntimeException(
        "Models must have 12 sections, this had " + sections.size)

    val Vector(code, interface, info, turtleShapes, version, previewCommands, systemDynamics,
             behaviorSpace, hubNetClient, linkShapes, modelSettings, deltaTick) = sections
    new Model(code.mkString("\n"),
              if(parser.nonEmpty) WidgetReader.readInterface(interface.toList, parser.get) else List(View()),
              info.mkString("\n"), version.head,
              turtleShapes.toList, behaviorSpace.toList, linkShapes.toList, previewCommands.toList)
  }
}
