// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import java.nio.file.Path

object LibraryInfoDownloader extends InfoDownloader {
  override val prefsKey = "libraries"

  override def getPath(name: String): Path =
    FileIO.perUserExtensionFile(name)
}
