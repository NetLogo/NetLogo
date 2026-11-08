// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.workspace

import java.nio.file.{ Files, FileVisitOption, Path, Paths }
import java.util.Locale

import org.nlogo.api.{ FileIO, PackageManager => APIPM }
import org.nlogo.core.{ FileModule, ResolvedModule }
import org.nlogo.util.PathUtils

trait AbstractCompilationEnvironment {

  def resolvePath(path: String): String

  def resolveModulePath(currentFile: Option[String], modulePath: Seq[String]): Option[ResolvedModule] = {
    val pathPrefixes =
      Seq( s"${resolvePath(currentFile.flatMap(x => Option(Paths.get(x).getParent).map(_.toString)).getOrElse(""))}/"
        , s"${APIPM.userPackagesPath.toString}/"
        , s"${APIPM.packagesPath.toString}/"
        ).map(PathUtils.standardize)

    pathPrefixes.foldLeft(None) {
      (p, pathPrefix) =>
        if (p.isDefined) { // If we already found some paths, just return those.
          p
        } else {
          val joined: String = modulePath.mkString("/").toLowerCase(Locale.ROOT)
          val path: String = PathUtils.standardize(resolvePath(s"$pathPrefix$joined"))

          // If the path points to a file, just return that file.
          val (pathWithExt, siblings) = if (FileIO.isRegularFile(s"$path.nlm")) {
            (s"$path.nlm", Seq(s"$path.nlm"))
          } else {
            (path, Seq())
          }

          // If the path points to a directory, search for module files in subdirectories. Note that there can be a file
          // and a directory "foo.nlm" and "foo" at the same level where "foo" is an arbitrary string, so we need to
          // check both cases separately. -- 2026-09-28 Kritphong M
          val descendants = if (FileIO.isDirectory(path)) {
            import scala.jdk.CollectionConverters.IteratorHasAsScala
            val fileIterator = Files.walk(Paths.get(path), FileVisitOption.FOLLOW_LINKS).iterator.asScala
            val isModuleFile = (x: Path) => Files.isRegularFile(x) && x.getFileName.toString.toLowerCase.endsWith(".nlm")
            fileIterator.filter(isModuleFile).map(_.toString).map(PathUtils.standardize).toSeq
          } else {
            Seq()
          }

          if (siblings.nonEmpty || descendants.nonEmpty) {
            Some(FileModule(pathPrefix, pathWithExt, siblings ++ descendants))
          } else {
            None
          }
        }
    }

  }

}
