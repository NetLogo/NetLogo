// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

object ModelReader {

  def modelSuffix(is3D: Boolean) =
    if (is3D) "nlogo3d" else "nlogo"

  def emptyModelPath(is3D: Boolean) =
    "/system/empty." + modelSuffix(is3D)

  @deprecated("Use ModelReader.modelSuffix(compiler.dialect.is3D) instead", "6.1.0")
  lazy val modelSuffix: String = {
    System.err.println("""|ModelReader.modelSuffix is deprecated and may not reflect the appropriate suffix for the world / workspace.
                          |Query the model, world type, or dialect to determine whether NetLogo is 3D""".stripMargin)
    modelSuffix(Version.is3DInternal)
  }

  @deprecated("Use ModelReader.emptyModelPath(compiler.dialect.is3D) instead", "6.1.0")
  lazy val emptyModelPath: String = {
    System.err.println("""|ModelReader.emptyModelPath is deprecated and may not reflect the appropriate suffix for the workspace.
                          |Query the model, world type, or dialect to determine whether NetLogo is 3D""".stripMargin)
    emptyModelPath(Version.is3DInternal)
  }
}
