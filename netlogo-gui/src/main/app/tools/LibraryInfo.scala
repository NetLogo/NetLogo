// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.tools

import java.net.URL

case class LibraryInfo(
  name: String,
  shortDescription: String,
  longDescription: String,
  homepage: URL,
  downloadURL: URL,
  status: LibraryStatus) {

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
  case object CanUpdate extends LibraryStatus
  case object UpToDate extends LibraryStatus
}
