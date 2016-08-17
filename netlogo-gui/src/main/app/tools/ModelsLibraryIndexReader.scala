// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.tools

// This is a little piece of ModelsLibraryDialog I wanted to write in Scala without having to
// convert the whole thing to Scala. - ST 2/27/11

import java.io.{ File, IOException }
import java.util.{ Map => JMap }

import scala.collection.mutable
import scala.collection.JavaConverters._
import scala.io.Source

import org.nlogo.api.Exceptions.handling
import org.nlogo.workspace.ModelsLibrary

object ModelsLibraryIndexReader {
  def readInfoMap: JMap[String, String] = {
    val result = new mutable.HashMap[String, String]
    val input = Source.fromFile(ModelsLibrary.modelsRoot + File.separator + "index.txt")("UTF-8").getLines
    handling(classOf[IOException]) {
      for(Seq(name, description) <- input.grouped(2))
        result(name) = description
    }
    result.asJava
  }
}
