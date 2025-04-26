// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core

import scala.util.matching.Regex

import LibraryStatus.{ CanInstall, CanUpdate, UpToDate }

trait LibraryManager {
  def installExtension(info: LibraryInfo): Unit
  def lookupExtension(name: String, version: String): Option[LibraryInfo]
  def reloadMetadata(): Unit
}

class DummyLibraryManager extends LibraryManager {
  def installExtension(info: LibraryInfo): Unit = {}
  def lookupExtension(name: String, version: String): Option[LibraryInfo] = None
  def reloadMetadata(): Unit = {}
}

import java.net.URL

object LibraryInfo {
  def isAvailableNewer(available: String, installed: String): Boolean = {
    val (availableMajor, availableMinor, availablePatch, availableExtra) = parseVersion(available)
    val (installedMajor, installedMinor, installedPatch, installedExtra) = parseVersion(installed)

    (availableMajor > installedMajor ||
      availableMinor > installedMinor ||
      availablePatch > installedPatch ||
      // this works for most basic scenarios, like: "" > "-beta1", "-beta2" > "-beta1", etc.
      // it might break on some edge cases, but our versions so far have been pretty simple.
      // -Jeremy B November 2020
      availableExtra > installedExtra)
  }

  def parseVersion(version: String): (Int, Int, Int, String) = {
    val versionRegex = new Regex("(\\d+)\\.(\\d+)(?:\\.(\\d+))?(.*)?")
    version match {
      case versionRegex(major, minor, patch, extra) =>
        (major.toInt, minor.toInt, Option(patch).map(_.toInt).getOrElse(0), Option(extra).getOrElse(""))
      case _ => throw new Exception(s"Unexpected version format: $version")
    }
  }
}

case class LibraryInfo(
  name: String,
  codeName: String,
  shortDescription: String,
  longDescription: String,
  version: String,
  homepage: URL,
  downloadURL: URL,
  bundled: Boolean,
  installedVersionOpt: Option[String],
  minNetLogoVersion: Option[String]
) {

  def status: LibraryStatus =
    installedVersionOpt.map {
      installed =>
        if (LibraryInfo.isAvailableNewer(version, installed))
          CanUpdate
        else
          UpToDate
    }.getOrElse(CanInstall)

  def canUninstall: Boolean =
    status != LibraryStatus.CanInstall && !bundled

  def isVersionRequirementMet(currentVersion: String): Boolean =
    minNetLogoVersion.map( (v) =>
      VersionUtils.isNetLogoVersionString(v) && (VersionUtils.numericValue(currentVersion) >= VersionUtils.numericValue(v))
    ).getOrElse(true)

  // We override `equals`, because we don't want to compare URLs directly. Checking equality
  // for URLs (which is what the case class would do otherwise), results in
  // network operations for checking whether the two hosts resolve to the same IP.
  // (See javadoc for java.net.URL#equals; Java 8)
  override def equals(that: Any) = that match {
    case l: LibraryInfo => toString == l.toString
    case _ => false
  }

}

sealed trait LibraryStatus

object LibraryStatus {
  case object CanInstall extends LibraryStatus
  case object CanUpdate  extends LibraryStatus
  case object UpToDate   extends LibraryStatus
}
