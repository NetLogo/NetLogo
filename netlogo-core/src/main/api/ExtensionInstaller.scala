// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import java.io.IOException
import java.net.HttpURLConnection
import java.nio.file.{ Files, FileVisitResult, Path, Paths, SimpleFileVisitor, StandardCopyOption }
import java.nio.file.attribute.BasicFileAttributes

import net.lingala.zip4j.core.ZipFile

private[api] class ExtensionInstaller(extensionManager: ExtensionManager) {

  def install(ext: LibraryInfo): Unit = {

    val conn = ext.downloadURL.openConnection.asInstanceOf[HttpURLConnection]

    if (conn.getResponseCode == 200) {

      val urlPath  = ext.downloadURL.getPath.stripSuffix("/")
      val basename = urlPath.substring(urlPath.lastIndexOf('/') + 1).dropRight(4)
      val zipPath  = Files.createTempFile(basename, ".zip")
      Files.copy(conn.getInputStream, zipPath, StandardCopyOption.REPLACE_EXISTING)

      val extDir = Paths.get(ExtensionManager.userExtensionsPath, ext.codeName)
      if (!Files.isDirectory(extDir))
        Files.createDirectory(extDir)
      else
        extensionManager.reset()

      new ZipFile(zipPath.toFile).extractAll(extDir.toString)
      Files.delete(zipPath)

    }

  }

  def uninstall(ext: LibraryInfo): Unit = {

    val extDir = Paths.get(ExtensionManager.userExtensionsPath, ext.codeName)

    if (Files.exists(extDir))

      Files.walkFileTree(extDir, new SimpleFileVisitor[Path] {

        override def visitFile(file: Path, attrs: BasicFileAttributes) = delete(file)
        override def postVisitDirectory(dir: Path, ex: IOException)    = delete(dir)

        private def delete(path: Path) = {
          Files.delete(path)
          FileVisitResult.CONTINUE
        }

      })

  }

}
