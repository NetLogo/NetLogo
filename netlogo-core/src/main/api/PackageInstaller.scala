// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import java.io.IOException
import java.net.HttpURLConnection
import java.nio.file.{ Files, FileVisitResult, Path, SimpleFileVisitor, StandardCopyOption }
import java.nio.file.attribute.BasicFileAttributes

import net.lingala.zip4j.ZipFile

import org.nlogo.core.LibraryInfo

private[api] class PackageInstaller(userPackagePath: Path) {

  def install(pkg: LibraryInfo): Unit = {

    val conn = pkg.downloadURL.openConnection.asInstanceOf[HttpURLConnection]

    if (conn.getResponseCode == 200) {

      val urlPath  = pkg.downloadURL.getPath.stripSuffix("/")
      val basename = urlPath.substring(urlPath.lastIndexOf('/') + 1).dropRight(4)
      val zipPath  = Files.createTempFile(basename, ".zip")
      Files.copy(conn.getInputStream, zipPath, StandardCopyOption.REPLACE_EXISTING)

      val pkgDir = userPackagePath.resolve(pkg.codeName)
      if (!Files.isDirectory(pkgDir)) {
        Files.createDirectory(pkgDir)
      }

      new ZipFile(zipPath.toFile).extractAll(pkgDir.toString)
      Files.delete(zipPath)

    }

  }

  def uninstall(pkg: LibraryInfo): Unit = {

    val pkgDir = userPackagePath.resolve(pkg.codeName)

    if (Files.exists(pkgDir))

      Files.walkFileTree(pkgDir, new SimpleFileVisitor[Path] {

        override def visitFile(file: Path, attrs: BasicFileAttributes) = delete(file)
        override def postVisitDirectory(dir: Path, ex: IOException)    = delete(dir)

        private def delete(path: Path) = {
          Files.delete(path)
          FileVisitResult.CONTINUE
        }

      })

  }

}
