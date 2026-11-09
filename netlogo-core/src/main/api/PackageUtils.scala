// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import java.io.File

import org.nlogo.core.LibraryInfo

import scala.util.matching.Regex

object PackageUtils {
  private val dirRegex: Regex = """(.+)-(.+)""".r

  def packageMatches(dir: File, name: String, spec: Option[DepSpec]): Boolean = {
    dir.getName match {
      case dirRegex(dirName, version) if dirName.toLowerCase == name &&
                                         spec.forall(dep => VersionSpec.parse(version).exists(dep.isValid)) =>
        true

      case _ =>
        false
    }
  }

  def versionSpec(dir: File): Option[VersionSpec] = {
    dir.getName match {
      case dirRegex(_, version) =>
        VersionSpec.parse(version)

      case _ =>
        None
    }
  }

  def createSpec(info: LibraryInfo): Option[PackageSpec] = {
    val dependencies: Seq[DepSpec] = info.dependencies.flatMap(DepSpec.parse)

    VersionSpec.parse(info.version).map(PackageSpec(info.codeName.toLowerCase, _)(dependencies))
  }

  def createSpec(dir: File): Option[PackageSpec] = {
    dir.getName match {
      case dirRegex(name, version) =>
        VersionSpec.parse(version).map(PackageSpec(name.toLowerCase, _)(Seq()))

      case _ =>
        None
    }
  }

  def findDeps(spec: PackageSpec, installed: Seq[PackageSpec], available: Seq[PackageSpec]): Iterable[PackageSpec] = {
    val selected: Map[PackageSpec, Set[PackageSpec]] = findDepRanges(spec, installed, available).flatMap { (name, ranges) =>
      val sorted: Seq[(VersionRange, Set[PackageSpec])] = ranges.toSeq.sortBy(_._1.min)
      val combined: Seq[(VersionRange, Set[PackageSpec])] = sorted.foldLeft(sorted.take(1)) {
        case (acc, (range, parents)) =>
          val (lastRange, lastParents) = acc.last

          if (lastRange.intersects(range)) {
            acc.init :+ (lastRange.combine(range), lastParents ++ parents)
          } else {
            acc :+ (range, parents)
          }
      }

      combined.flatMap { (range, parents) =>
        available.find(spec => spec.name == name && spec.version == range.max).map((_, parents))
      }
    }

    selected.filter(_._2.exists(pkg => pkg == spec || selected.contains(pkg))).keys
  }

  private def findDepRanges(spec: PackageSpec, installed: Seq[PackageSpec],
                            available: Seq[PackageSpec]): Map[String, Map[VersionRange, Set[PackageSpec]]] = {

    spec.dependencies.foldLeft(Map[String, Map[VersionRange, Set[PackageSpec]]]()) {
      case (acc, DepSpec(name, comp, version)) =>
        val valid: Seq[PackageSpec] = available.filter(spec => spec.name == name && comp.op(spec.version, version))

        if (valid.isEmpty)
          throw PackageDepException(spec.name, spec.version.toString, name, version.toString)

        val versions: Seq[VersionSpec] = valid.map(_.version)
        val range = VersionRange(versions.min, versions.max)

        if (installed.exists(_.validFor(name, range))) {
          acc
        } else {
          valid.foldLeft(updateRange(acc, name, range, Set(spec))) {
            case (acc, spec) =>
              findDepRanges(spec, installed, available).foldLeft(acc) {
                case (acc, (name, ranges)) =>
                  ranges.foldLeft(acc) {
                    case (acc, (range, parents)) =>
                      updateRange(acc, name, range, parents)
                  }
              }
          }
        }
    }
  }

  private def updateRange(current: Map[String, Map[VersionRange, Set[PackageSpec]]], name: String, range: VersionRange,
                          parents: Set[PackageSpec]): Map[String, Map[VersionRange, Set[PackageSpec]]] = {
    val ranges: Map[VersionRange, Set[PackageSpec]] = current.getOrElse(name, Map())

    current + ((name, ranges + ((range, ranges.getOrElse(range, Set()) ++ parents))))
  }
}

case class VersionComp(op: (VersionSpec, VersionSpec) => Boolean)

object VersionComp {
  def parse(text: String): VersionComp = {
    text match {
      case "<" => VersionComp(_ < _)
      case "<=" => VersionComp(_ <= _)
      case "==" => VersionComp(_ == _)
      case ">=" => VersionComp(_ >= _)
      case ">" => VersionComp(_ > _)
    }
  }
}

case class VersionSpec(major: Int, minor: Int, patch: Int) extends Ordered[VersionSpec] {
  override def toString: String =
    s"$major.$minor.$patch"

  override def compare(other: VersionSpec): Int = {
    val one = major.compare(other.major)

    if (one == 0) {
      val two = minor.compare(other.minor)

      if (two == 0) {
        patch.compare(other.patch)
      } else {
        two
      }
    } else {
      one
    }
  }

  def min(other: VersionSpec): VersionSpec = {
    if (this < other) {
      this
    } else {
      other
    }
  }

  def max(other: VersionSpec): VersionSpec = {
    if (this > other) {
      this
    } else {
      other
    }
  }
}

object VersionSpec {
  private val versionRegex: Regex = """(\d+?)(?:.(\d+?))?(?:.(\d+?))?""".r

  def parse(text: String): Option[VersionSpec] = {
    text match {
      case versionRegex(major, minor, patch) =>
        Some(VersionSpec(major.toInt, Option(minor).fold(0)(_.toInt), Option(patch).fold(0)(_.toInt)))

      case _ =>
        None
    }
  }
}

case class VersionRange(min: VersionSpec, max: VersionSpec) {
  def intersects(range: VersionRange): Boolean =
    min <= range.max && max >= range.min

  def combine(range: VersionRange): VersionRange =
    VersionRange(min.max(range.min), max.min(range.max))
}

case class DepSpec(name: String, comp: VersionComp, version: VersionSpec) {
  def isValid(version: VersionSpec): Boolean =
    comp.op(this.version, version)
}

object DepSpec {
  private val depRegex: Regex = """\s*"(.+?)"\s*(<=|<|==|>=|>)\s*(.+?)\s*""".r

  def parse(text: String): Option[DepSpec] = {
    text match {
      case depRegex(name, compStr, versionStr) =>
        Some(DepSpec(name, VersionComp.parse(compStr), VersionSpec.parse(versionStr).getOrElse {
          throw PackageVersionException(name, versionStr)
        }))

      case _ =>
        None
    }
  }
}

case class PackageSpec(name: String, version: VersionSpec)(val dependencies: Seq[DepSpec]) {
  def validFor(name: String, range: VersionRange): Boolean =
    this.name == name && version >= range.min && version <= range.max

  override def toString: String =
    s"$name-$version"
}

case class PackageVersionException(name: String, version: String) extends Exception

case class PackageDepException(parent: String, parentVersion: String, child: String, childVersion: String)
  extends Exception
