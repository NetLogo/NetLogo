// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core

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

case class LibraryInfo(
  name: String,
  codeName: String,
  shortDescription: String,
  longDescription: String,
  version: String,
  homepage: URL,
  downloadURL: URL,
  bundled: Boolean,
  installedVersionOpt: Option[String]
  ) {

  def status: LibraryStatus =
    installedVersionOpt.map {
      iv =>
        if (iv == version)
          UpToDate
        else
          CanUpdate
    }.getOrElse(CanInstall)

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
