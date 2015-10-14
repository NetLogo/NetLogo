// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

object FileIO {

  @throws(classOf[java.io.IOException])
  def file2String(path: String) =
    org.nlogo.util.Utils.reader2String(
      new java.io.FileReader(path))

  @throws(classOf[java.io.IOException])
  def writeFile(path: String, text: String) {
    writeFile(path, text, false)
  }

  @throws(classOf[java.io.IOException])
  def writeFile(path: String, text: String, convertToPlatformLineBreaks: Boolean) {
    val file = new LocalFile(path)
    try {
      file.open(FileMode.Write)
      if (!convertToPlatformLineBreaks)
        file.print(text)
      else {
        val lineReader = new java.io.BufferedReader(
          new java.io.StringReader(text))
        val lines =
          Iterator.continually(lineReader.readLine()).takeWhile(_ != null)
        for(line <- lines)
          file.println(line)
      }
      file.close(true)
    }
    finally file.close(false)
  }

}
