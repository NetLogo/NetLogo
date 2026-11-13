// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.workspace

import java.nio.file.{ Files, FileVisitOption, Path, Paths }
import java.util.Locale

import org.nlogo.api.{ FileIO, PackageManager => APIPM }
import org.nlogo.core.ResolvedModule
import org.nlogo.core.Fail._
import org.nlogo.core.{ I18N, ImportPathPrefix, SourceLocatable }

trait AbstractCompilationEnvironment {

  def resolvePath(path: String): String

  private def getAncestor(path: Path, levels: Int): Option[Path] = {
    if (path != null && levels > 0) {
      getAncestor(path.getParent, levels - 1)
    } else {
      Option(path)
    }
  }

  private def distanceTo(path: Path, filename: String): Option[Int] = {
    if (path == null) {
      None
    } else if (Files.isRegularFile(path.resolve(filename))) {
      Some(0)
    } else {
      distanceTo(path.getParent, filename).map(_ + 1)
    }
  }

  def resolveModulePath(
    currentFile: Option[String],
    importPathPrefix: Option[ImportPathPrefix],
    modulePath: Seq[String],
    locatable: Option[SourceLocatable]
  ): Option[ResolvedModule] = {
    val separator =
      System.getProperty("file.separator")
    val localPathBase =
      s"${resolvePath(currentFile.map(x => Paths.get(x).getParent.toString).getOrElse(""))}$separator"
    val distanceToPackageIni =
      distanceTo(Paths.get(localPathBase), "package.ini")
    val isPackageRootRelative =
      importPathPrefix.map(_ == ImportPathPrefix.PackageRoot).getOrElse(false)

    val upCount = importPathPrefix match {
      case Some(ImportPathPrefix.Up(n)) => n
      case Some(ImportPathPrefix.PackageRoot) => distanceToPackageIni.getOrElse(0)
      case _ => 0
    }

    val crossesPackageIni = distanceToPackageIni match {
      case Some(n) => upCount > n
      case None => false
    }

    if(isPackageRootRelative && distanceToPackageIni.isEmpty) {
      locatable.foreach(exception(I18N.errors.get("org.nlogo.workspace.AbstractCompilationEnvironment.noPackageIniFound "), _))
      None
    } else if (crossesPackageIni) {
      locatable.foreach(exception(I18N.errors.get("org.nlogo.workspace.AbstractCompilationEnvironment.upAbovePackageBoundary"), _))
      None
    } else {
      for {
        localPathPrefix <- getAncestor(Paths.get(localPathBase), upCount).map(x => s"${x.toString}$separator")

        repoPathPrefixes =
          Seq(s"${APIPM.userPackagesPath.toString}$separator" , s"${APIPM.packagesPath.toString}$separator")
        pathPrefixes =
          if (upCount > 0) Seq(localPathPrefix) else localPathPrefix +: repoPathPrefixes

        result <- pathPrefixes.foldLeft(None) { (p, pathPrefix) =>
          if (p.isDefined) { // If we already found some paths, just return those.
            p
          } else {
            val path = resolvePath(s"$pathPrefix${modulePath.mkString(separator).toLowerCase(Locale.ROOT)}")

            // If the path points to a file, just return that file.
            val (pathWithExt, siblings) = if (FileIO.isRegularFile(s"$path.nlm")) {
              (s"$path.nlm", Seq(s"$path.nlm"))
            } else {
              (path, Seq())
            }

            // If the path points to a directory, search for module files in subdirectories. Note that there can be a
            // file and a directory "foo.nlm" and "foo" at the same level where "foo" is an arbitrary string, so we need
            // to check both cases separately. -- 2026-09-28 Kritphong M
            val descendants = if (FileIO.isDirectory(path)) {
              import scala.jdk.CollectionConverters.IteratorHasAsScala
              val fileIterator = Files.walk(Paths.get(path), FileVisitOption.FOLLOW_LINKS).iterator.asScala
              val isModuleFile = (x: Path) => Files.isRegularFile(x) && x.getFileName.toString.toLowerCase.endsWith(".nlm")
              fileIterator.filter(isModuleFile).map(_.toString).toSeq
            } else {
              Seq()
            }

            if (siblings.nonEmpty || descendants.nonEmpty) {
              Some(ResolvedModule(resolvePath(pathPrefix), pathWithExt, siblings ++ descendants))
            } else {
              None
            }
          }
        }
      } yield result
    }
  }
}
