// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core

object ModelInfo {
  val sectionKey = "org.nlogo.modelsection.modelinfo"
  val empty = ModelInfo("", Seq.empty[String])
}

case class ModelInfo(
  title: String,
  tags: Seq[String]
)
