// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo
package org.nlogo.workspace

object ExtensionManagerException {
  sealed trait Cause {
    def message: String = "An error occurred in ExtensionManager"
  }
  case class ExtensionNotFound(extName: String) extends Cause {
    override def message: String = ExtensionManager.EXTENSION_NOT_FOUND + extName
  }
  case class NoExtensionName(extName: String) extends Cause {
    override def message: String = s"Bad extension '$extName': Can't find extension name in Manifest."
  }
  case class NoClassManager(extName: String) extends Cause {
    override def message: String = s"Bad extension '$extName': Couldn't locate Class-Manager tag in Manifest File"
  }
  case class InvalidClassManager(extName: String) extends Cause {
    override def message: String = s"Bad extension '$extName': The ClassManager doesn't implement org.nlogo.api.ClassManager"
  }
  case class NotFoundClassManager(name: String) extends Cause {
    override def message: String = s"Can't find class $name in extension"
  }
  case class NoManifest(extName: String) extends Cause {
    override def message: String = s"Bad extension '$extName': Can't find a Manifest file in extension"
  }
  case object UserHalted extends Cause {
    override def message: String = "User halted compilation"
  }
}

import ExtensionManagerException.Cause

class ExtensionManagerException(message: String, val cause: Cause) extends Exception(message) {
  def this(cause: Cause) = this(cause.message, cause)
}
