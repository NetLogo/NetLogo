// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

object ModelReader {

  val modelSuffix =
    if(Version.is3D) "nlogo3d" else "nlogo"

  val emptyModelPath =
    "/system/empty." + modelSuffix
}
