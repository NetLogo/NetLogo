package org.nlogo.workspace

import org.nlogo.api.{ PackageManager => APIPM, ExtensionManager => APIEM, LibraryManager }
import org.nlogo.core.{ LibraryInfo, LibraryStatus }

object ExtensionInstaller {

  private var hasRun: Boolean = false

  def apply(extensionNames: Set[String], forceRun: Boolean = false): Unit = {
    if (hasRun && !forceRun) {
      return
    }
    hasRun = true

    println(APIEM.userExtensionsPath)
    val libraryManager = new LibraryManager(APIPM.userPackagesPath, APIEM.userExtensionsPath, () => {})
    libraryManager.reloadMetadata(isFirstLoad = false, useBundled = false)
    Thread.sleep(2500)
    val extensionInfos = libraryManager.getExtensionInfos
    println(extensionInfos.mkString("\n"))

    def isNeededExtension(extInfo: LibraryInfo): Boolean = {
      println(s"checking ${extInfo.codeName}")
      val isContained   = extensionNames.contains(extInfo.codeName)
      val isInstallable = (extInfo.status == LibraryStatus.CanInstall || extInfo.status == LibraryStatus.CanUpdate || forceRun)
      return isContained && isInstallable
    }

    val neededInfos = extensionInfos.filter(isNeededExtension)
    println(s"Found ${extensionInfos.size} extensions in the library, with ${neededInfos.size} of them needing updates.")
    neededInfos.foreach( (extInfo) => {
      val action = if (extInfo.status == LibraryStatus.CanInstall) { "Installing" } else { "Updating" }
      println(s"$action extension: ${extInfo.name} (${extInfo.codeName} ${extInfo.version})")
      libraryManager.installExtension(extInfo)
      println(s"${extInfo.name} done.")
      Thread.sleep(2500)
    })
  }

  def main(args: Array[String]): Unit = {
    ExtensionInstaller(args.toSet, true)
    println("Complete")
  }

}
