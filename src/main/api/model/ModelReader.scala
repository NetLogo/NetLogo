// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api.model

import org.nlogo.api
import org.nlogo.core.{Model, View}

object ModelReader {

  val SEPARATOR = "@#$#@#$#@"

  // This should really be changed in the future to not need a compiler to parse widgets, but that is not
  // a things for today.  FD 4/17/14
  def parseModel(model: String, parser: api.ParserServices): Model = {
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
    new Model(code.mkString("\n"), WidgetReader.readInterface(interface.toList, parser),
              info.mkString("\n"), version.head, turtleShapes.toList, behaviorSpace.toList,
              linkShapes.toList, previewCommands.toList)
  }

  def formatModel(model: Model, parser: api.ParserServices): String = {
    model.code + s"\n$SEPARATOR\n" +
      WidgetReader.formatInterface(model.widgets, parser) + s"\n$SEPARATOR\n" +
      model.info + s"\n$SEPARATOR\n" +
      model.turtleShapes.mkString("\n") + s"\n$SEPARATOR\n" +
      model.version + s"\n$SEPARATOR" +
      (if(model.previewCommands.nonEmpty) model.previewCommands.mkString("\n", "\n", "\n") else "\n") + s"$SEPARATOR\n" +
      s"$SEPARATOR" +
      (if(model.behaviorSpace.nonEmpty) model.behaviorSpace.mkString("\n", "\n", "\n") else "\n") + s"$SEPARATOR\n" +
      s"$SEPARATOR\n" +
      model.linkShapes.mkString("\n") + s"\n$SEPARATOR\n" +
      s"$SEPARATOR\n"
  }
}
