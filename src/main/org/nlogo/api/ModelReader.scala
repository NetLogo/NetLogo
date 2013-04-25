// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import org.nlogo.util.Utils.getResourceLines

object ModelReader {

  val modelSuffix = "nlogo"

  val emptyModelPath =
    "/system/empty." + modelSuffix

  type ModelMap = Map[ModelSection, Seq[String]]

  val SEPARATOR = "@#$#@#$#@"

  val sections = ModelSection.allSections

  lazy val defaultShapes: Seq[String] =
    getResourceLines("/system/defaultShapes.txt").toSeq
  lazy val defaultLinkShapes: Seq[String] =
    getResourceLines("/system/defaultLinkShapes.txt").toSeq

  def parseModel(model: String): ModelMap = {
    var result: ModelMap = sections.map(_ -> Seq[String]()).toMap
    val sectionsIter = sections.iterator
    var sectionContents = Vector[String]()
    def sectionDone() {
      if(sectionsIter.hasNext)
        result += sectionsIter.next() -> sectionContents
      sectionContents = Vector()
    }
    for(line <- io.Source.fromString(model).getLines)
      if(line.startsWith(SEPARATOR))
        sectionDone()
      else
        sectionContents :+= line
    sectionDone()
    result
  }

  def parseVersion(map: ModelMap): String =
    map(ModelSection.Version).head

  def parseWidgets(lines: Seq[String]): Seq[Seq[String]] = {
    var widgets = Vector[Vector[String]]()
    var widget = Vector[String]()
    for(line <- lines)
      if(line.nonEmpty)
        widget :+= line
      else {
        if(!widget.forall(_.isEmpty))
          widgets :+= widget
        widget = Vector()
      }
    if(!widget.isEmpty)
      widgets :+= widget
    widgets
  }

  def stripLines(st: String): String =
    st.flatMap{
      case '\n' => "\\n"
      case '\\' => "\\\\"
      case '\"' => "\\\""
      case c => c.toString
    }

  def restoreLines(s: String): String = {
    @scala.annotation.tailrec
    def loop(acc: Vector[Char], rest: String): Vector[Char] = {
      if (rest.size < 2)
        acc ++ rest
      else if (rest.head == '\\')
        rest.tail.head match {
          case 'n'  => loop(acc :+ '\n', rest.tail.tail)
          case '\\' => loop(acc :+ '\\', rest.tail.tail)
          case '"'  => loop(acc :+ '"', rest.tail.tail)
          case _    => sys.error("invalid escape sequence in \"" + s + "\"")
        }
      else loop(acc :+ rest.head, rest.tail)
    }
    loop(Vector(), s).mkString
  }

}
