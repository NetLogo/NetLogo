// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core.model

import org.nlogo.core.{ LiteralParser, Model, ShapeParser }

object ModelReader {

  val SEPARATOR = "@#$#@#$#@"

  // This should really be changed in the future to not need a compiler to parse widgets, but that is not
  // a things for today.  FD 4/17/14
  def parseModel(model: String, parser: LiteralParser): Model = {
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

    val Vector(code, interface, info, turtleShapeLines, version, previewCommands, systemDynamics,
             behaviorSpace, hubNetClient, linkShapeLines, modelSettings, deltaTick) = sections
    val turtleShapes = ShapeParser.parseVectorShapes(turtleShapeLines)
    val linkShapes   = ShapeParser.parseLinkShapes(linkShapeLines)
    new Model(code.mkString("\n"), WidgetReader.readInterface(interface.toList, parser),
              info.mkString("\n"), version.head, turtleShapes.toList, behaviorSpace.toList,
              linkShapes.toList, previewCommands.toList)
  }

  def formatModel(model: Model, parser: LiteralParser): String = {
    model.code + s"\n$SEPARATOR\n" +
      WidgetReader.formatInterface(model.widgets, parser) + s"\n$SEPARATOR\n" +
      model.info + s"\n$SEPARATOR\n" +
      ShapeParser.formatVectorShapes(model.turtleShapes) + s"\n$SEPARATOR\n" +
      model.version + s"\n$SEPARATOR" +
      (if(model.previewCommands.nonEmpty) model.previewCommands.mkString("\n", "\n", "\n") else "\n") + s"$SEPARATOR\n" +
      s"$SEPARATOR" +
      (if(model.behaviorSpace.nonEmpty) model.behaviorSpace.mkString("\n", "\n", "\n") else "\n") + s"$SEPARATOR\n" +
      s"$SEPARATOR\n" +
      ShapeParser.formatLinkShapes(model.linkShapes) + s"\n$SEPARATOR\n" +
      s"$SEPARATOR\n"
  }
}
