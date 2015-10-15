// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.workspace

import org.nlogo.api.LogoException
import org.nlogo.core.CompilerException

/**
 * Interface provides access to the NetLogo controlling API's report and command methods found in
 * methods independent of App.app and headless.HeadlessWorkspace.  This is useful for making java
 * software that can run NetLogo in both GUI and Headless mode.
 */
trait Controllable {

  @throws(classOf[CompilerException])
  @throws(classOf[LogoException])
  def command(source: String)

  @throws(classOf[CompilerException])
  @throws(classOf[LogoException])
  def report(source: String): AnyRef

  @throws(classOf[CompilerException])
  @throws(classOf[LogoException])
  @throws(classOf[java.io.IOException])
  def open(path: String)

}
