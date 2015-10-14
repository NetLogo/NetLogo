// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app

// This is a little piece of ModelsLibraryDialog I wanted to write in Scala without having to
// convert the whole thing to Scala. - ST 2/27/11

import org.nlogo.util.Exceptions.handling
import org.nlogo.workspace.ModelsLibrary
import java.io.File
import collection.JavaConverters._

object ModelsLibraryIndexReader {
  def readInfoMap: java.util.Map[String, String] = {
    val result = new collection.mutable.HashMap[String, String]
    val input = io.Source.fromFile(ModelsLibrary.modelsRoot + File.separator + "index.txt")("UTF-8").getLines
    handling(classOf[java.io.IOException]) {
      for(Seq(name, description) <- input.grouped(2))
        result(name) = description
    }
    result.asJava
  }
}
