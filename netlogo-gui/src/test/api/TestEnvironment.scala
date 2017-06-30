// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import java.io.{ File => JFile }

object TestEnvironment {
  val baseDirectory = new JFile("netlogo-gui")

  def projectFilePath(path: String): String = {
    new JFile(baseDirectory, path).getPath
  }
}
