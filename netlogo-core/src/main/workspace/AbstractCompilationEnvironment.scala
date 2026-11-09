// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.workspace

import java.io.File
import java.nio.file.{ Files, FileVisitOption, Path, Paths }
import java.util.Locale

import org.nlogo.api.{ DepSpec, FileIO, PackageManager => APIPM, PackageSpec, PackageUtils, VersionSpec }
import org.nlogo.core.ResolvedModule
import org.nlogo.util.PathUtils

import scala.util.matching.Regex

trait AbstractCompilationEnvironment {

  def resolvePath(path: String): String

  def getModuleVersion(from: Option[PackageSpec], name: String): Option[VersionSpec]
  def setModuleVersion(from: Option[PackageSpec], name: String, version: VersionSpec): Unit

  def resolveModulePath(currentFile: Option[String], modulePath: Seq[String], save: Boolean): Option[ResolvedModule] = {
    val pathPrefixes: Seq[Prefix] =
      Seq( Prefix(s"${resolvePath(currentFile.map(x => Paths.get(x).getParent.toString).getOrElse(""))}/", false)
         , Prefix(s"${APIPM.userPackagesPath.toString}/", true)
         , Prefix(s"${APIPM.packagesPath.toString}/", true)
         )

    val currentModule: Option[PackageSpec] = currentFile.flatMap(file => Option(new File(file).getParentFile))
                                               .flatMap(PackageUtils.createSpec)

    val name: String = modulePath.head.toLowerCase

    pathPrefixes.foldLeft(None) {
      case (p, Prefix(prefix, versioned)) =>
        if (p.isDefined) { // If we already found some paths, just return those.
          p
        } else {
          val path: String = PathUtils.standardize {
            if (versioned) {
              getModuleVersion(currentModule, name) match {
                case Some(version) =>
                  resolvePath(s"$prefix$name-$version/${modulePath.tail.mkString("/").toLowerCase(Locale.ROOT)}")

                case _ =>
                  val spec: Option[DepSpec] = currentModule.flatMap(readModuleVersion(_, name))

                  val paths: Array[File] = Option(new File(prefix).listFiles).getOrElse(Array[File]()).filter {
                    PackageUtils.packageMatches(_, name, spec)
                  }

                  if (paths.nonEmpty) {
                    val root: File = paths.maxBy(PackageUtils.versionSpec)

                    if (save)
                      PackageUtils.versionSpec(root).foreach(setModuleVersion(currentModule, name, _))

                    resolvePath(s"$prefix${root.getName}/${modulePath.tail.mkString("/").toLowerCase(Locale.ROOT)}")
                  } else {
                    resolvePath(s"$prefix${modulePath.mkString("/").toLowerCase(Locale.ROOT)}")
                  }
              }
            } else {
              resolvePath(s"$prefix${modulePath.mkString("/").toLowerCase(Locale.ROOT)}")
            }
          }

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
            Some(ResolvedModule(PathUtils.standardize(prefix), pathWithExt, siblings ++ descendants))
          } else {
            None
          }
        }
    }

  }

  private def readModuleVersion(from: PackageSpec, name: String): Option[DepSpec] = {
    val depsRegex: Regex = """\s*dependencies\s*=\s*(.+)""".r

    val path: Path = APIPM.userPackagesPath.resolve(s"${from.name}-${from.version}/package.ini")

    if (Files.exists(path)) {
      Files.readString(path).split('\n').collectFirst {
        case depsRegex(deps) =>
          deps.split(',').flatMap(DepSpec.parse)
      }.flatMap(_.find(_.name == name))
    } else {
      None
    }
  }

  private case class Prefix(path: String, versioned: Boolean)

}
