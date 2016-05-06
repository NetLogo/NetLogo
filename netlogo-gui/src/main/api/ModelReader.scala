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
}
