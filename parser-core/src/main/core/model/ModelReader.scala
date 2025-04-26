// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core.model

import org.nlogo.core.{ LiteralParser, Model, ShapeParser }

object ModelReader {

  val SEPARATOR = "@#$#@#$#@"

  // This should really be changed in the future to not need a compiler to parse widgets, but that is not
  // a things for today.  FD 4/17/14
  def parseModel(model: String, parser: LiteralParser,
    additionalWidgetReaders: Map[String, WidgetReader],
    sourceConversion: String => String = identity): Model = {
    var sections = Vector[Vector[String]]()
    var sectionContents = Vector[String]()
    def sectionDone(): Unit = {
      sections :+= sectionContents
      sectionContents = Vector()
    }
    for(line <- io.Source.fromString(model).getLines())
      if(line.startsWith(SEPARATOR))
        sectionDone()
      else
        sectionContents :+= line
    sectionDone()

    if(sections.size != 12)
      throw new RuntimeException(
        "Models must have 12 sections, this had " + sections.size)

    val turtleShapes = ShapeParser.parseVectorShapes(sections(3))
    val linkShapes   = ShapeParser.parseLinkShapes(sections(9))
    new Model(
      code            = sections(0).mkString("\n"),
      widgets         = WidgetReader.readInterface(sections(1).toList, parser, additionalWidgetReaders, sourceConversion),
      info            = sections(2).mkString("\n"),
      version         = sections(4).head,
      turtleShapes    = turtleShapes.toList,
      linkShapes      = linkShapes.toList)

  }

  def formatModel(model: Model): String = {
    model.code + s"\n$SEPARATOR\n" +
      WidgetReader.formatInterface(model.widgets) + s"\n$SEPARATOR\n" +
      model.info + s"\n$SEPARATOR\n" +
      ShapeParser.formatVectorShapes(model.turtleShapes) + s"\n$SEPARATOR\n" +
      model.version + s"\n$SEPARATOR\n" +
      s"$SEPARATOR\n" +
      s"$SEPARATOR\n" +
      s"$SEPARATOR\n" +
      s"$SEPARATOR\n" +
      ShapeParser.formatLinkShapes(model.linkShapes) + s"\n$SEPARATOR\n" +
      s"\n$SEPARATOR\n"
  }
}
