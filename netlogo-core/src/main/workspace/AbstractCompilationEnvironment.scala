// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.workspace

import java.nio.file.{ Files, FileVisitOption, Path, Paths }

import org.nlogo.api.{ FileIO, PackageManager => APIPM }

trait AbstractCompilationEnvironment {

  def resolvePath(path: String): String

  def resolveModulePath(currentFile: Option[String], modulePath: Seq[String]): Seq[String] = {

    val packageName: String = modulePath.headOption.getOrElse("").toLowerCase

    val pathPrefixes =
      Seq( currentFile.map(x => s"${Paths.get(x).getParent}/").getOrElse("")
         , s"${APIPM.userPackagesPath.resolve(packageName)}/"
         , s"${APIPM.    packagesPath.resolve(packageName)}/"
         )

    pathPrefixes.foldLeft(Seq()) {
      (p, pathPrefix) =>
        if (p.nonEmpty) { // If we already found some paths, just return those.
          p
        } else {
          val path = resolvePath(s"$pathPrefix${modulePath.mkString("/")}".toLowerCase)

          // If the path points to a file, just return that file.
          val siblings = if (FileIO.isRegularFile(s"$path.nlm")) Seq(s"$path.nlm") else Seq()

          // If the path points to a directory, search for module files in subdirectories. Note that there can be a file
          // and a directory "foo.nlm" and "foo" at the same level where "foo" is an arbitrary string, so we need to
          // check both cases separately. -- 2026-09-28 Kritphong M
          val descendants = if (FileIO.isDirectory(path)) {
            import scala.jdk.CollectionConverters.IteratorHasAsScala
            val fileIterator = Files.walk(Paths.get(path), FileVisitOption.FOLLOW_LINKS).iterator.asScala
            val isModuleFile = (x: Path) => Files.isRegularFile(x) && x.getFileName.toString.toLowerCase.endsWith(".nlm")
            fileIterator.filter(isModuleFile).map(_.toString).toSeq
          } else {
            Seq()
          }

          siblings ++ descendants
        }
    }

  }

}
