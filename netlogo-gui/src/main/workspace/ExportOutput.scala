// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.workspace

import java.util.StringTokenizer
import java.io.IOException

import org.nlogo.core.FileMode
import org.nlogo.api.{ Exceptions, LocalFile }

object ExportOutput {
  def silencingErrors(filename: String, text: String): Unit = {
    val file = new LocalFile(filename)
    try {
      file.open(FileMode.Write)
      printOutput(file, text)
      file.close(true)
    } catch {
      case ex: IOException =>
        try file.close(false)
        catch {
          case ex2: IOException => Exceptions.ignore(ex2)
        }
    }
  }

  def throwingErrors(filename: String, text: String): Unit = {
    val file = new LocalFile(filename)
    try {
      file.open(FileMode.Write)
      printOutput(file, text)
      file.close(true)
    } catch {
      case ex: IOException =>
        file.close(false)
        throw ex
    }
  }

  private def printOutput(file: LocalFile, text: String): Unit = {
    val lines = new StringTokenizer(text, "\n")
    while (lines.hasMoreTokens) {
      // note that since we always use println, we always output a final carriage return
      // even if the text doesn't have one; hmm, bug or feature? let's call it a feature
      file.println(lines.nextToken)
    }
  }
}
