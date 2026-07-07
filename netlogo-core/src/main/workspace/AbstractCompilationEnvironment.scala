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

          // If the path points to a directory, search for module files in subdirectories.
          if (FileIO.isDirectory(path)) {
            import scala.jdk.CollectionConverters.IteratorHasAsScala
            val fileIterator = Files.walk(Paths.get(path), FileVisitOption.FOLLOW_LINKS).iterator.asScala
            val isModuleFile = (x: Path) => Files.isRegularFile(x) && x.getFileName.toString.toLowerCase.endsWith(".nls")
            fileIterator.filter(isModuleFile).map(_.toString).toSeq
          } else if (FileIO.isRegularFile(s"$path.nls")) { // If the path points to a file, just return that file.
            Seq(s"$path.nls")
          } else { // Otherwise, return nothing and just move on to the next prefix.
            Seq()
          }
        }
    }

  }

}
