// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import org.nlogo.util.Utils
import collection.JavaConverters._

object ModelReader {

  val modelSuffix = "nlogo"

  val emptyModelPath =
    "/system/empty." + modelSuffix

  type ModelMap = java.util.Map[ModelSection, Seq[String]]

  val SEPARATOR = "@#$#@#$#@"

  val sections = ModelSection.allSections

  lazy val defaultShapes: Seq[String] =
    Vector() ++ Utils.getResourceLines("/system/defaultShapes.txt")
  lazy val defaultLinkShapes: Seq[String] =
    Vector() ++ Utils.getResourceLines("/system/defaultLinkShapes.txt")

  def parseModel(model: String): ModelMap = {
    val map: collection.mutable.HashMap[ModelSection, Seq[String]] =
      sections.map(_ -> Seq[String]())(collection.breakOut)
    val lines = {
      val br = new java.io.BufferedReader(new java.io.StringReader(model))
      Iterator.continually(br.readLine()).takeWhile(_ != null)
    }
    val sectionsIter = sections.iterator
    val sectionContents = collection.mutable.Buffer[String]()
    def sectionDone() {
      if(sectionsIter.hasNext)
        map(sectionsIter.next()) = Vector() ++ sectionContents
      sectionContents.clear()
    }
    for(line <- lines)
      if(line.startsWith(SEPARATOR))
        sectionDone()
      else
        sectionContents += line
    sectionDone()
    map.asJava
  }

  def parseVersion(map: ModelMap): String =
    map.get(ModelSection.Version)(0)

  def parseWidgets(lines: Seq[String]): Seq[Seq[String]] = {
    val widgets = new collection.mutable.ListBuffer[List[String]]
    val widget = new collection.mutable.ListBuffer[String]
    for(line <- lines)
      if(line.nonEmpty)
        widget += line
      else {
        if(!widget.forall(_.isEmpty))
          widgets += widget.toList
        widget.clear()
      }
    if(!widget.isEmpty)
      widgets += widget.toList
    widgets.toList
  }

  def stripLines(st: String): String =
    st.flatMap{
      case '\n' => "\\n"
      case '\\' => "\\\\"
      case '\"' => "\\\""
      case c => c.toString
    }

  def restoreLines(s: String): String =
    if(s.size < 2)
      s
    else if(s.head == '\\')
      s.tail.head match {
        case 'n' => '\n' + restoreLines(s.tail.tail)
        case '\\' => '\\' + restoreLines(s.tail.tail)
        case '"' => '"' + restoreLines(s.tail.tail)
        case _ =>
          sys.error("invalid escape sequence in \"" + s + "\"")
      }
    else s.head + restoreLines(s.tail)

}
