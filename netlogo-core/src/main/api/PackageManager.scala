// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import java.nio.file.{ Path, Paths }

object PackageManager {

  val packageNotFoundStr: String = "Can't find package: "

  def packagesPath: Path =
    Paths.get(System.getProperty("netlogo.packages.dir", "packages"))

  def userPackagesPath: Path =
    FileIO.perUserExtensionDir("packages")

}
