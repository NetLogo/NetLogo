// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo
package org.nlogo.workspace

object ExtensionManagerException {
  sealed trait Cause {
    def message: String = "An error occurred in ExtensionManager"
  }
  case class ExtensionNotFound(extName: String) extends Cause {
    override def message: String = ExtensionManager.EXTENSION_NOT_FOUND + extName
  }
  case object NoExtensionName extends Cause {
    override def message: String = "Bad extension: Can't find extension name in Manifest."
  }
  case object NoClassManager extends Cause {
    override def message: String = "Bad extension: Couldn't locate Class-Manager tag in Manifest File"
  }
  case object InvalidClassManager extends Cause {
    override def message: String = "Bad extension: The ClassManager doesn't implement org.nlogo.api.ClassManager"
  }
  case class NotFoundClassManager(name: String) extends Cause {
    override def message: String = s"Can't find class $name in extension"
  }
  case object NoManifest extends Cause {
    override def message: String = "Bad extension: Can't find a Manifest file in extension"
  }
  case object UserHalted extends Cause {
    override def message: String = "User halted compilation"
  }
}

import ExtensionManagerException.Cause

class ExtensionManagerException(message: String, val cause: Cause) extends Exception(message) {
  def this(cause: Cause) = this(cause.message, cause)
}
