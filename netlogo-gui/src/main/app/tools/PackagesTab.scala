// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.tools

import java.nio.file.Path

import org.nlogo.api.LibraryManager

class PackagesTab(manager: LibraryManager, updateStatus: String => Unit, recompile: () => Unit,
                  updateSource: (String => String) => Unit, extPathMappings: Map[String, Path])
  extends LibrariesTab(manager, manager.getPackageInfos, updateStatus, recompile, updateSource, extPathMappings) {

  override def statusName: String =
    "packages"

  override def addLibrariesToSource(source: String, requiredLibs: Set[String]): Option[String] =
    Option(s"${requiredLibs.map(lib => s"import $lib").mkString("\n")}\n$source")
}
