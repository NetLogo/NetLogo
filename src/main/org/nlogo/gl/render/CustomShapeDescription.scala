package org.nlogo.gl.render

import java.util.{ List => JList, ArrayList }

private class CustomShapeDescription(val name: String) {
  val lines: JList[String] = new ArrayList[String]
}
