// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.util

import java.io.File

object Utils {

  def getStackTrace(throwable: Throwable): String = {
    val stringWriter = new java.io.StringWriter
    val printWriter = new java.io.PrintWriter(stringWriter)
    throwable.printStackTrace(printWriter)
    // I don't know if this flushing is ever necessary, but I'm having an intermittent issue (ticket
    // #847) with incomplete stack traces (first line only), so I'm adding these flush() calls on
    // the off chance that could conceivably fix it - ST 12/15/09
    printWriter.flush()
    stringWriter.flush()
    stringWriter.toString.replace('\t', ' ')
  }

  ///

  def escapeSpacesInURL(url: String): String =
    url.flatMap{case ' ' => "%20"
                case c => c.toString}
  def unescapeSpacesInURL(url: String): String =
    url.replaceAll("%20", " ")

  ///

  def getResourceLines(path: String): Iterator[String] = {
    val in = new java.io.BufferedReader(
      new java.io.InputStreamReader(
        getClass.getResourceAsStream(path)))
    Iterator.continually(in.readLine()).takeWhile(_ != null)
  }

  def getResourceAsString(path: String): String =
    getResourceLines(path).mkString("", "\n", "\n")

  @throws(classOf[java.io.IOException])
  def url2String(sampleURL: String): String = {
    if(sampleURL.startsWith("/"))
      getResourceAsString(sampleURL)
    else {
      val massagedURL =
        if(!System.getProperty("os.name").startsWith("Mac")) {
          val badStart = "file://"
          if(sampleURL.indexOf(badStart) != -1)
            "file:/" + sampleURL.drop(badStart.size)
          else sampleURL
        }
        else sampleURL

      // UTF-8 is needed directly here because it seems that applets can't be
      // passed -D params. So, we can't use -Dfile.encoding=UTF-8 like we normally do.
      // This shouldn't hurt anything.
      reader2String(
        new java.io.InputStreamReader(
          new java.net.URL(massagedURL)
          .openStream(), "UTF-8"))
    }
  }

  @throws(classOf[java.io.IOException])
  def reader2String(reader: java.io.Reader): String =
    reader2String(reader, 8192) // arbitrary default

  // separate method with configurable bufferSize for easy testing with ScalaCheck.
  // for now we can't just use a default argument because most of our callers
  // are from Java - ST 12/22/09
  private[util] def reader2String(reader: java.io.Reader, bufferSize: Int): String = {
    assert(bufferSize > 0)
    val sb = new StringBuilder
    val buffer = Array.fill(bufferSize)('\u0000')
    Iterator.continually(reader.read(buffer))
      .takeWhile(_ != -1)
      .foreach(sb.appendAll(buffer, 0, _))
    reader.close()
    sb.toString
  }

  /**
   * Given a `requestedName` and some already `usedNames`, produces a unique name by
   *  appending a new (or incremented) parenthesized index to the `requestedBaseName`
   */
  def uniqueName(requestedName: String, usedNames: Iterable[String]): String = {
    val Pattern = """(.*?) *(?:\((\d+)\))?""".r
    val (requestedBaseName: String, requestedIndex: Option[Int]) =
      requestedName match {
        case Pattern(baseName, index) =>
          (baseName, Option(index).map(_.toInt))
      }
    val usedIndices: Iterable[Int] = usedNames.collect {
      case Pattern(baseName, index) if baseName == requestedBaseName =>
        Option(index).map(_.toInt).getOrElse(0)
    }
    val newIndex = usedIndices
      .filter(i => requestedIndex.map(i >= _).getOrElse(true))
      .reduceOption(_ max _)
      .map(_ + 1)
      .orElse(requestedIndex)
      .map(i => s" (${i})")
      .getOrElse("")
    requestedBaseName + newIndex
  }

  def isSymlink(file: File): Boolean = {
    val canon = if (file.getParent == null) {
      file
    } else {
      new File(file.getParentFile.getCanonicalFile, file.getName)
    }
    file.getAbsolutePath != file.getCanonicalPath
  }
}
