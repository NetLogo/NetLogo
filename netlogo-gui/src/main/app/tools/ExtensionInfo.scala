// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.tools

import java.net.URL

case class ExtensionInfo(
  name: String,
  shortDescription: String,
  longDescription: String,
  homepage: URL,
  downloadURL: URL,
  status: ExtensionStatus) {

  // We want `equals` to only compare `name`, because:
  //  - That should be enough
  //  - Checking equality for URLs (which is what the case class would do otherwise),
  //    results in network operations for checking whether the two hosts resolve
  //    to the same IP. (See javadoc for java.net.URL#equals; Java 8)
  override def equals(that: Any) = that match {
    case ei: ExtensionInfo => name == ei.name
    case _ => false
  }
}

sealed trait ExtensionStatus

object ExtensionStatus {
  case object CanInstall extends ExtensionStatus
  case object CanUpdate extends ExtensionStatus
  case object UpToDate extends ExtensionStatus
}
