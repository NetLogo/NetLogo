// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

object ModelReader {

  val modelSuffix =
    if (Version.is3D) "nlogox3d" else "nlogox"

  val emptyModelPath =
    "/system/empty." + modelSuffix

}
