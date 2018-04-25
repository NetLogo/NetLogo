// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.tools

import java.net.URI

case class ExtensionInfo(
  name: String,
  shortDescription: String,
  longDescription: String,
  homepage: URI,
  downloadURI: URI,
  status: ExtensionStatus)

sealed trait ExtensionStatus

object ExtensionStatus {
  case object CanInstall extends ExtensionStatus
  case object CanUpdate extends ExtensionStatus
  case object UpToDate extends ExtensionStatus
}
