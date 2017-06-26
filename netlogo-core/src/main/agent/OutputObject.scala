// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent

case class OutputObject(caption: String, message: String, addNewline: Boolean, isTemporary: Boolean) {
  def get = {
    val buf = new StringBuilder(caption)
    if(caption.nonEmpty)
      buf ++= ": "
    buf ++= message
    if(addNewline)
      buf ++= "\n"
    buf.toString
  }
}
