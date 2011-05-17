package org.nlogo.api

import org.nlogo.util.{JCL,Utils}

object ModelReader {

  type ModelMap = java.util.Map[ModelSection, Array[String]]

  val SEPARATOR = "@#$#@#$#@"

  val sections =
    JCL.iterableToScalaIterable(
      java.util.EnumSet.allOf(classOf[ModelSection]))

  lazy val defaultShapes =
    Utils.getResourceAsStringArray("/system/defaultShapes.txt")
  lazy val defaultLinkShapes =
    Utils.getResourceAsStringArray("/system/defaultLinkShapes.txt")

  def parseModel(model: String): ModelMap = {
    val map = collection.mutable.HashMap(
      sections.map(_ -> Array[String]()).toSeq :_*)
    val lines = {
      val br = new java.io.BufferedReader(new java.io.StringReader(model))
      Iterator.continually(br.readLine()).takeWhile(_ != null)
    }
    val sectionsIter = sections.iterator
    val sectionContents = new collection.mutable.ArrayBuffer[String]
    def sectionDone() {
      if(sectionsIter.hasNext)
        map(sectionsIter.next()) = sectionContents.toArray
      sectionContents.clear()
    }
    for(line <- lines)
      if(line.startsWith(SEPARATOR))
        sectionDone()
      else
        sectionContents += line
    sectionDone()
    JCL.toJavaMap(map)
  }
  
  def parseVersion(map: ModelMap): String =
    map.get(ModelSection.VERSION)(0)

  def parseWidgets(lines: Array[String]): java.util.List[java.util.List[String]] = {
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
    JCL.toJavaList(widgets.map(JCL.toJavaList(_)))
  }

}
