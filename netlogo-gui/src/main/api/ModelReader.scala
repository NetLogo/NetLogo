// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import org.nlogo.util.Utils
import collection.JavaConverters._

object ModelReader {

  val modelSuffix =
    if(Version.is3D) "nlogo3d" else "nlogo"

  val emptyModelPath =
    "/system/empty." + modelSuffix

  type ModelMap = java.util.Map[ModelSection, Array[String]]

  val SEPARATOR = "@#$#@#$#@"

  val sections = ModelSection.allSections

  def parseModel(model: String): ModelMap = {
    val map: collection.mutable.HashMap[ModelSection, Array[String]] =
      sections.map(_ -> Array[String]())(collection.breakOut)
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
    map.asJava
  }

  def parseVersion(map: ModelMap): String =
    map.get(ModelSection.Version)(0)

}
