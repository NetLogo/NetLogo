// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.tools

trait LibrariesCategoryInstaller {
  def install(lib: LibraryInfo): Unit
  def uninstall(lib: LibraryInfo): Unit
}
